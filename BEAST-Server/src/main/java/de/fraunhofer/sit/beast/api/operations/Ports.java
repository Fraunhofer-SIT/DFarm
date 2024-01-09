package de.fraunhofer.sit.beast.api.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.network.ports.ForwardingDirection;
import de.fraunhofer.sit.beast.api.data.network.ports.PortForwarding;
import de.fraunhofer.sit.beast.api.data.network.ports.Protocol;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.Redirector;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/devices/{devid}/network/ports/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Port forwardings", description = "Allows port forwardings"))
public class Ports {
	static final Logger LOGGER = LogManager.getLogger(Ports.class);

	@Operation(method = "PUT", summary = "Creates a port forwarding", description = "Creates a port forwarding")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PortForwarding.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/ondevice/")
	public PortForwarding createPortForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("portOnDevice") @Parameter(name = "portOnDevice", description = "The port on the device", required = true, in = ParameterIn.QUERY, example = "4000") int portOnDevice,
			@QueryParam("direction") @Parameter(name = "direction", description = "The direction of the port forwarding", required = true, in = ParameterIn.QUERY, example = "true") ForwardingDirection direction,
			@QueryParam("protocol") @Parameter(name = "protocol", description = "The protocol.", required = true, in = ParameterIn.QUERY) Protocol protocol)
			throws APIExceptionWrapper {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		LOGGER.info("Create port forwarding on device " + devid + " using portOnDevice=" + portOnDevice + ", direction="
				+ direction + ", protocol=" + protocol);
		device.getDeviceInfo().updateLastUsed(apiKey);
		PortForwarding forwarding = new PortForwarding();
		forwarding.portOnDevice = portOnDevice;
		forwarding.direction = direction;
		forwarding.protocolOnDevice = protocol;
		forwarding.id = forwarding.portOnDevice;
		device.getPortFowardings().createPortForwarding(forwarding);
		return forwarding;
	}

	@Operation(method = "DELETE", summary = "Deletes a port forwarding", description = "Deletes a port forwarding")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@Path("/ondevice/{id}")
	public void removeForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("id") @Parameter(name = "id", description = "The id of the forwarding", required = true, in = ParameterIn.PATH) int forwardingid)
			throws APIExceptionWrapper {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		for (PortForwarding p : device.getPortFowardings().listPortForwardings()) {
			if (p.id == forwardingid) {
				LOGGER.info("Create delete forwarding on " + devid + " using portOnDevice=" + p.portOnDevice
						+ ", portOnHost=" + p.portOnHostMachine + ", direction=" + p.direction + ", protocol="
						+ p.protocolOnDevice);
				device.getPortFowardings().removePortForwarding(p);
				return;
			}
		}
		throw new NotFoundException(
				String.format("The port forwarding %d was not found for device %d", forwardingid, devid));
	}

	@Operation(method = "GET", summary = "Lists port forwardings", description = "Lists port forwardings")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PortForwarding.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	public Collection<PortForwarding> getPorts(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid)
			throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		return device.getPortFowardings().listPortForwardings();
	}

	private static Map<Integer, Redirector> redirectors = new HashMap<>();

	@Operation(method = "PUT", summary = "Creates a port forwarding on host", description = "Forward a local port (not a device port). This is necessary to debug using adb, which only allows debug clients to attach from localhost.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Integer.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@PUT
	@Path("/onhost/{port}")
	public Integer createLocalPortForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device (ignored, used only to determine which controller is responsible)", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("port") @Parameter(name = "port", description = "The port on the host", required = true, in = ParameterIn.PATH, example = "4000") int port)
			throws Exception {

		if (redirectors.containsKey(port)) {
			throw new APIExceptionWrapper(new APIException(400, "Port already forwarded"));
		}
		LOGGER.info("Create local port forwarding on device " + devid + " using port=" + port);
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		Redirector r = new Redirector(port);
		r.start();
		LOGGER.info(String.format("Redirecting local port %d to %d ", port, r.getSocket().getLocalPort()));
		redirectors.put(port, r);
		return r.getSocket().getLocalPort();
	}

	@Operation(method = "DELETE", summary = "Delete a port forwarding on host", description = "Stop forwarding a local port.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@Path("/onhost/{port}")
	public void deleteLocalPortForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device (ignored, used only to determine which controller is responsible)", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("port") @Parameter(name = "port", description = "The port on the host", required = true, in = ParameterIn.PATH, example = "4000") int port)
			throws Exception {

		Redirector r = redirectors.remove(port);

		if (r == null) {
			throw new APIExceptionWrapper(new APIException(400, "Port not forwarded"));
		}
		LOGGER.info("Delete local port forwarding on device " + devid + " using port=" + port);
		r.stop();
	}

}
