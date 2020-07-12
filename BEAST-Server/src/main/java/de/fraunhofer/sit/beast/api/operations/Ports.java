package de.fraunhofer.sit.beast.api.operations;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.network.ports.ForwardingDirection;
import de.fraunhofer.sit.beast.api.data.network.ports.PortForwarding;
import de.fraunhofer.sit.beast.api.data.network.ports.Protocol;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Path("/api/devices/{devid}/network/ports/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Port forwardings", description = "Allows port forwardings"))
public class Ports
{
	@Operation(method = "PUT", summary = "Creates a port forwarding", description = "Creates a port forwarding")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PortForwarding.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@PUT
    public PortForwarding createPortForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("portOnDevice") @Parameter(name = "portOnDevice", description = "The port on the device", required = true, in = ParameterIn.QUERY, example="4000") int portOnDevice,
			@QueryParam("direction") @Parameter(name = "direction", description = "The direction of the port forwarding", required = true, in = ParameterIn.QUERY, example="true") ForwardingDirection direction,
			@QueryParam("protocol") @Parameter(name = "protocol", description = "The protocol.", required = true, in = ParameterIn.QUERY) Protocol protocol
    	) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		PortForwarding forwarding = new PortForwarding();
		forwarding.portOnDevice = portOnDevice;
		forwarding.direction = direction;
		forwarding.protocolOnDevice = protocol;
		device.getPortFowardings().createPortForwarding(forwarding);
		return forwarding;
	}
	

	@Operation(method = "DELETE", summary = "Deletes a port forwarding", description = "Deletes a port forwarding")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@POST
	@Path("/{id}")
	public void removeForwarding(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("id") @Parameter(name = "id", description = "The id of the forwarding", required = true, in = ParameterIn.PATH) int forwardingid
			 ) {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		for (PortForwarding p : device.getPortFowardings().listPortForwardings()) {
			if (p.id == forwardingid)
			{
				device.getPortFowardings().removePortForwarding(p);
				return;
			}
		}
		throw new NotFoundException(String.format("The port forwarding %d was not found for device %d", forwardingid, devid));
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
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		return device.getPortFowardings().listPortForwardings();
	}

}
