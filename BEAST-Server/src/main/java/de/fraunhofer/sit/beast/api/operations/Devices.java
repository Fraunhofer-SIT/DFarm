package de.fraunhofer.sit.beast.api.operations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceReservationFailedException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
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
				return dev.getDeviceInfo();
			} catch (APIExceptionWrapper e) {
				//That's fine...
			}
		}
		throw new APIExceptionWrapper(new DeviceReservationFailedException(590, "No device available", "No device available"));
	}

	
	

	@POST
	@Path("/{devid}/execute")
	@Operation(method = "POST", summary = "Executes a command on the device", description = "Executes a command on the device and returns the result")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "590", description = "No device available", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@Produces(MediaType.APPLICATION_JSON)
	public String executeOnDevice(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid,
			@javax.ws.rs.QueryParam(value = "command") @Parameter(name = "command", in = ParameterIn.QUERY, description = "The command", required = true) String command)
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
			@PathParam(value = "devid") @Parameter(name = "devid", in = ParameterIn.PATH, description = "The device id", required = true) int devid)
			throws Throwable {
		DeviceManager.DEVICE_MANAGER.release(apiKey, devid);
	}

	@GET
	@Path("/releaseAll")
	@Operation(method = "GET", summary = "Release all devices reserved by the API key", description = "Release all devices reserved by the API key")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))) })
	@Produces(MediaType.APPLICATION_JSON)
	public void releaseAllDevices(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey)			throws Throwable {
		DeviceManager.DEVICE_MANAGER.releaseAll(apiKey);
	}
}
