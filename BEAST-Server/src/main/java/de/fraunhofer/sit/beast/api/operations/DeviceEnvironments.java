package de.fraunhofer.sit.beast.api.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.fraunhofer.sit.beast.api.data.UploadedFile;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceReservationFailedException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.EnvironmentStateManager;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;
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

@Path("/api/devices/{devid}/environments/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Device Environments", description = "Device Environments"))
public class DeviceEnvironments
{
	@Operation(method = "PUT", summary = "Saves the current state as the new saved state", description = "Saves the state")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@PUT
	@Path("/{saveName}")
	public void saveState(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("saveName") @Parameter(name = "saveName", description = "The id of device", required = true, in = ParameterIn.PATH) String saveName ) {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		try {
			EnvironmentStateManager.saveEnvironmentState(dev, saveName);
		} catch (APIExceptionWrapper w) {
			throw w;
		} catch (Exception e) {
			Database.logError(e);
			throw new APIExceptionWrapper(new APIException(503, "Internal error"));
		}
	}
	
	@Operation(method = "DELETE", summary = "Deletes a state", description = "Deletes a state")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@Path("/{saveName}")
	public void deleteState(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("saveName") @Parameter(name = "saveName", description = "The id of device", required = true, in = ParameterIn.PATH) String saveName ) {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		try {
			SavedEnvironment se = Database.INSTANCE.getSavedEnvironmentUnsafe(dev.getDeviceInfo().ID, saveName);
			if (se == null)
				throw new APIExceptionWrapper(new APIException(404, String.format("Environment %s not found for device %d", saveName, devid)));
			if (!se.user.equals(apiKey))
				throw new APIExceptionWrapper(new APIException(403, String.format("Environment %s cannot be delete by a different user", saveName)));
			EnvironmentStateManager.deleteEnvironmentState(dev, se);
		} catch (APIExceptionWrapper w) {
			throw w;
		} catch (Exception e) {
			Database.logError(e);
			throw new APIExceptionWrapper(new APIException(503, "Internal error"));
		}

	}
	
	
	@Operation(method = "GET", summary = "Loads the saved state", description = "Loads the saved state")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	@Path("/{saveName}/load")
	public void loadState(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("saveName") @Parameter(name = "saveName", description = "The id of device", required = true, in = ParameterIn.PATH) String saveName ) {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		try {
			EnvironmentStateManager.loadEnvironmentState(dev, saveName);
		} catch (APIExceptionWrapper w) {
			throw w;
		} catch (Exception e) {
			Database.logError(e);
			throw new APIExceptionWrapper(new APIException(503, "Internal error"));
		}
	}
	

	@Operation(method = "GET", summary = "Returns a list of saved states", description = "List of saved environment states")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavedEnvironment.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	public List<SavedEnvironment> listStates(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		try {
			return Database.INSTANCE.getSavedEnvironments(dev.getDeviceInfo().ID);
		} catch (APIExceptionWrapper w) {
			throw w;
		} catch (Exception e) {
			Database.logError(e);
			throw new APIExceptionWrapper(new APIException(503, "Internal error"));
		}

	}
}
