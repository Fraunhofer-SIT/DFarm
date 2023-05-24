package de.fraunhofer.sit.beast.api.operations;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.google.common.base.Charsets;

import de.fraunhofer.sit.beast.api.data.Key;
import de.fraunhofer.sit.beast.api.data.UploadedFile;
import de.fraunhofer.sit.beast.api.data.android.Intent;
import de.fraunhofer.sit.beast.api.data.android.MonkeyOptions;
import de.fraunhofer.sit.beast.api.data.contacts.IMAddress;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceNotFoundException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceReservationFailedException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.android.AndroidDevice;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Path("/api/devices/{devid}/input")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Input", description = "Provides interfaces to input controls"))
public class Input {
	private static final Logger logger = LogManager.getLogger(Input.class);

	@Operation(method = "GET", summary = "Taps on screen", description = "Taps on screen")
	@Path("/tap")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = DeviceNotFoundException.class)))})
	@GET
	public void tap(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("x") @Parameter(name = "x", in = ParameterIn.QUERY, description = "The x coordinate", required = true) int x,
			@QueryParam("y") @Parameter(name = "y", in = ParameterIn.QUERY, description = "The y coordinate", required = true) int y
			)  {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		dev.tap(x, y);

	}

	@Operation(method = "GET", summary = "Swipes on screen", description = "Swipes on screen")
	@Path("/swipe")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = DeviceNotFoundException.class)))})
	@GET
	public void swipe(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("x1") @Parameter(name = "x1", in = ParameterIn.QUERY, description = "The first x coordinate", required = true) int x1,
			@QueryParam("y1") @Parameter(name = "y1", in = ParameterIn.QUERY, description = "The first y coordinate", required = true) int y1,
			@QueryParam("x2") @Parameter(name = "x2", in = ParameterIn.QUERY, description = "The second x coordinate", required = true) int x2,
			@QueryParam("y2") @Parameter(name = "y2", in = ParameterIn.QUERY, description = "The second y coordinate", required = true) int y2,
			@QueryParam("durationMs") @Parameter(name = "durationMs", in = ParameterIn.QUERY, description = "The duration in ms", required = false, example="10") int durationMs
			)  {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		dev.swipe(x1, y1, x2, y2, durationMs);
	}

	@Operation(method = "GET", summary = "Key event", description = "A key event")
	@Path("/keyevent")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = DeviceNotFoundException.class)))})
	@GET
	public void keyTyped(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("key") @Parameter(name = "key", in = ParameterIn.QUERY, description = "The key", required = true, schema=@Schema(implementation=Key.class)) Key key)
			 {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		dev.keyTyped(key);
	}

	@Operation(method = "POST", summary = "Inputs text", description = "Inputs text")
	@Path("/typeText")
	@POST
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "Device not found", content = @Content(schema = @Schema(implementation = DeviceNotFoundException.class)))})	
	public void typeText(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The text", required = true) String text
			)  {
		if (text.startsWith("\"") && text.endsWith("\""))
			text = text.substring(1, text.length() - 1);
		if (text.isEmpty())
			return;
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		dev.typeText(text);

	}


	@POST
    @Consumes("application/json")
	@Path("/android/startMonkey")
	@Operation(method = "POST", summary = "Starts the monkey application exerciser", description = "Starts the monkey application exerciser, which randomly executes inputs on the device. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content( schema =  @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.TEXT_PLAIN)
	public String startMonkey(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The options of monkey", required = true) MonkeyOptions monkeyOptions) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException, IllegalArgumentException, IllegalAccessException
	{
		String s = monkeyOptions.getCommandSyntax();
		logger.info("Starting monkey: " + s);
		System.out.println("Starting monkey: " + s);
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		AtomicBoolean finished = new AtomicBoolean();
		StringBuilder sb = new StringBuilder();
		Thread thr = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					d.getAndroidDevice().executeShellCommand(s, new IShellOutputReceiver() {

						@Override
						public void addOutput(byte[] data, int offset, int length) {
							String s = new String(data, offset, length, Charsets.UTF_8);
							sb.append(s);
							String log = "Monkey output: " + s;
							System.out.println(log);
							logger.info(log);
						}

						@Override
						public void flush() {
						}

						@Override
						public boolean isCancelled() {
							return false;
						}
						
					}, 1, TimeUnit.DAYS);
					finished.set(true);
				} catch (Exception e) {
					logger.error("An error occurred while running " + s, e);
				}
			}
			
		});
		thr.setDaemon(true);
		thr.start();
		long ms = System.currentTimeMillis();
		while (!finished.get()) {
			if (System.currentTimeMillis() - ms > 2000) {
				return "[Showing first 2 seconds of monkey output]\n" + sb.toString();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	@POST
    @Consumes("application/json")
	@Path("/android/startActivity")
	@Operation(method = "POST", summary = "Sends an intent to the system to start an activity", description = "Sends an intent to the system to start an activity. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public void startActivity(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The intent ot send", required = false) Intent intent,
			@QueryParam("forceStopBefore") @Parameter(name = "forceStopBefore", in = ParameterIn.QUERY, description = "Whether to force stop the application before", required = false, example="false") boolean forceStopBefore,
			@QueryParam("waitForDebugger") @Parameter(name = "waitForDebugger", in = ParameterIn.QUERY, description = "Whether to wait for a debugger", required = false, example="false") boolean waitForDebugger
			)
			  {
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		d.startActivity(intent, forceStopBefore, waitForDebugger);
	}
	
	@POST
    @Consumes("application/json")
	@Path("/android/startService")
	@Operation(method = "POST", summary = "Sends an intent to the system to start a service", description = "Sends an intent to the system to start a service. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public void startService(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The intent ot send", required = false) Intent intent
			) {
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		d.startService(intent);
	}
	
	@POST
    @Consumes("application/json")
	@Path("/android/startForegroundService")
	@Operation(method = "POST", summary = "Sends an intent to the system to start a service in foreground", description = "Sends an intent to the system to start a service in foreground. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public void startForegroundService(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The intent ot send", required = false) Intent intent
			) {
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		d.startForegroundService(intent);
	}

	@POST
    @Consumes("application/json")
	@Path("/android/stopService")
	@Operation(method = "POST", summary = "Sends an intent to the system to stop a service", description = "Sends an intent to the system to stop a service. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public void stopService(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The intent ot send", required = false) Intent intent
			) {
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		d.stopService(intent);
	}

	@POST
    @Consumes("application/json")
	@Path("/android/broadcast")
	@Operation(method = "POST", summary = "Sends an intent to the system to send a message to broadcast receivers", description = "Sends an intent to the system to send a message to broadcast receivers. Android only")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "401", description = "Not an android device", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "510", description = "No device available", content = @Content(schema = @Schema(implementation = DeviceReservationFailedException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public void broadcast(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(description = "The intent ot send", required = false) Intent intent, 
			@QueryParam("receiverPermission") @Parameter(name = "receiverPermission", in = ParameterIn.QUERY, description = "The permission a receiver needs to have", required = false, example="") String receiverPermission
			) {
		AndroidDevice d = getAndroidDevice(apiKey, devid);
		d.broadcast(intent, receiverPermission);
	}

	private AndroidDevice getAndroidDevice(String apiKey, int devid) {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		if (!(dev instanceof AndroidDevice))
			throw new APIExceptionWrapper(new APIException(401, String.format("Not an Android device, but %s", dev.getClass().getName())));
		AndroidDevice d = (AndroidDevice) dev;
		return d;
	}
}
