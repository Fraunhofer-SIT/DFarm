package de.fraunhofer.sit.beast.api.operations;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.Connection.Listener;
import org.eclipse.jetty.server.HttpConnection;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceReservationFailedException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.LogBuffer;
import de.fraunhofer.sit.beast.internal.android.AndroidDeviceManager;
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

@Path("/api/devices/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Devices", description = "Information about devices"))
public class Devices
{
	private static final Logger LOGGER = LogManager.getLogger(Devices.class);

	
	/**
	 * <b>CAUTION</b>
	 * Note that this method is handled differently in commander mode, where the main server
	 * calls getDevices on all slaves (this method is called on the slave server) and aggregates
	 * the results.
	 * This means that this method is not called in commander mode.
	 * @param apiKey
	 * @param deviceRequirements
	 * @return the devices
	 */
	@Operation(method = "GET", summary = "Returns a list of devices", description = "Returns devices")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceInformation.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	public List<DeviceInformation> getDevicesUnconditionally(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey) {
		Collection<IDevice> dev = DeviceManager.DEVICE_MANAGER.getDevices(null);
		List<DeviceInformation> devInfo = new ArrayList<DeviceInformation>();
		for (IDevice d : dev) {
			devInfo.add(d.getDeviceInfo());
		}
		LOGGER.info("Returning a list of " +dev.size() + " devices");
		return devInfo;
	}
	
	/**
	 * <b>CAUTION</b>
	 * Note that this method is handled differently in commander mode, where the main server
	 * calls getDevices on all slaves (this method is called on the slave server) and aggregates
	 * the results.
	 * This means that this method is not called in commander mode.
	 * @param apiKey
	 * @param deviceRequirements
	 * @return the devices
	 */
	@Operation(method = "POST", summary = "Returns a list of devices", description = "Returns devices")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceInformation.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@POST
    @Consumes("application/json")
	public List<DeviceInformation> getDevices(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@RequestBody(required=true, content= {@Content(schema=@Schema(implementation=DeviceRequirements.class)) })			DeviceRequirements deviceRequirements) {
		Collection<IDevice> dev = DeviceManager.DEVICE_MANAGER.getDevices(deviceRequirements);
		List<DeviceInformation> devInfo = new ArrayList<DeviceInformation>();
		for (IDevice d : dev) {
			devInfo.add(d.getDeviceInfo());
		}
		LOGGER.info("Returning a list of " +dev.size() + " devices, whcih satisfy " + deviceRequirements);
		return devInfo;
	}
	
	@GET
	@Path("{devid}")
	@Operation(method = "GET", summary = "Gets device information", description = "Gets device information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DeviceInformation.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "590", description = "No device available", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public DeviceInformation getDeviceInformation(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid)
		
			throws Throwable {
		return DeviceManager.DEVICE_MANAGER.getDeviceById(devid).getDeviceInfo();
	}
	
	@GET
	@Path("{devid}/log")
	@Operation(method = "GET", summary = "Get device logs", description = "Gets Logs for a certain process. Swagger-generated clients do not support stream types -> use getDeviceLogsCall(...).execute().body().byteStream() instead.")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getDeviceLog(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid,
			@QueryParam(value = "process") @Parameter(name = "process", in = ParameterIn.QUERY, description = "The process id", required = false) String process)
		
			throws Throwable {
		
		final LogBuffer log = DeviceManager.DEVICE_MANAGER.getDeviceById(devid).getDeviceLog(process);
		
		StreamingOutput streamingOutput = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				while (true) {
					
					try {
						String s = log.getMessage();
						output.write(s.getBytes());
						output.write('\n');
						output.flush();
					} catch (Exception e) {
						// if the connection closes or times out, close the LogBuffer
						// to notify whatever produces the messages to quit.
						log.close();
						return;
					}
				}
			}
		};
		return Response.ok(streamingOutput).header("Stream", "Yes").build();
	}
	

	@Operation(method = "GET", summary = "Downloads a screenshot", description = "Downloads a screenshot")
	@Path("{devid}/screenshot")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "image/png", schema = @Schema(description = "string", format = "binary", type = "string"))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))), })
	@GET
	@Produces("image/png")
	public StreamingOutput getScreenshot(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) {
		final IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		
		return new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				dev.writePNGScreenshot(output);
				
			}
			
		};

	}
	
	@Operation(method = "GET", summary = "Pings the device", description = "Pings the device, signalling that it's still needed by the client.")
	@Path("{devid}/ping")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DeviceInformation.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))), })
	@GET
	public DeviceInformation ping(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) {
		final IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		try {
			dev.ping();
		} catch (Exception e) {
			throw new APIExceptionWrapper(new APIException(500, "Ping failed: " + e.getMessage()));
		}

		return dev.getDeviceInfo();
	}
	
	

	@POST
    @Consumes("application/json")
	@Path("/reserve")
	@Operation(method = "POST", summary = "Reserves a device", description = "Reserves a device and returns it (if successful)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DeviceInformation.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "590", description = "No device available", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public DeviceInformation reserveDevice(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
		 @RequestBody(description = "Device requirements", required = false) DeviceRequirements ddeviceRequirements)
			throws Throwable {
		ddeviceRequirements.state = DeviceState.FREE;
		Collection<IDevice> devices = DeviceManager.DEVICE_MANAGER.getDevices(ddeviceRequirements);
		for (IDevice dev : devices) {
			try {	
				/**
				 * <b>CAUTION</b>
				 * Note that this method is handled differently in commander mode, where the main server
				 * calls getDevices on all slaves (this method is called on the slave server) and aggregates
				 * the results.
				 * This means that this method is not called in commander mode.
				 * @param apiKey
				 * @param deviceRequirements
				 * @return the devices
				 */

				DeviceManager.DEVICE_MANAGER.reserve(apiKey, dev);
				DeviceInformation d =  dev.getDeviceInfo();
				HttpServletRequest http = ResteasyProviderFactory.getInstance().getContextData(HttpServletRequest.class);
				d.managerHostname = http.getLocalAddr();
				return d;
			} catch (APIExceptionWrapper e) {
				//That's fine...
			}
		}
		if (devices.isEmpty())
			LOGGER.warn("No device matched " +  ddeviceRequirements.toString());
		else
			LOGGER.warn("No device out ouf matching" + devices.size() + " could be reserved");
		throw new APIExceptionWrapper(new DeviceReservationFailedException(590, "No device available", "No device available"));
	}

	
	

	@GET
	@Path("/{devid}/execute")
	@Operation(method = "GET", summary = "Executes a command on the device", description = "Executes a command on the device and returns the result")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "590", description = "No device available", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public String executeOnDevice(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid,
			@jakarta.ws.rs.QueryParam(value = "command") @Parameter(name = "command", in = ParameterIn.QUERY, description = "The command", required = true) String command)
			throws Throwable {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		return dev.executeOnDevice(command);

	}

	@GET
	@Path("/{devid}/release")
	@Operation(method = "GET", summary = "Releases a device", description = "Releases a device which was reserved previously by the same API key.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))) })
	@Produces(MediaType.APPLICATION_JSON)
	public void releaseDevice(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid,
			@QueryParam(value = "reset") @Parameter(name = "reset", in = ParameterIn.QUERY, description = "Whether to reset the device") Boolean reset
			)
			throws Throwable {
		if (reset == null)
			reset = true;
		DeviceManager.DEVICE_MANAGER.release(apiKey, devid, reset);
	}

	@GET
	@Path("/releaseAll")
	@Operation(method = "GET", summary = "Release all devices reserved by the API key", description = "Release all devices reserved by the API key")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))) })
	@Produces(MediaType.APPLICATION_JSON)
	public void releaseAllDevices(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@QueryParam(value = "reset") @Parameter(name = "reset", in = ParameterIn.QUERY, description = "Whether to reset the device") Boolean reset
			)			throws Throwable {
		if (reset == null)
			reset = true;
		DeviceManager.DEVICE_MANAGER.releaseAll(apiKey, reset);
	}
}
