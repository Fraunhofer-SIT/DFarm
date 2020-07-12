package de.fraunhofer.sit.beast.api.operations;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.fraunhofer.sit.beast.api.data.UploadedFile;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.utils.IOUtils;
import de.fraunhofer.sit.beast.internal.utils.TempUtils;
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

@Path("/api/devices/{devid}/apps/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Apps", description = "Manages the applications"))
public class Apps
{
	@RequestBody(description = "App to install", required = true, content = {
			@Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UploadedFile.class, type = "object")) })
	@Operation(method = "POST", summary = "Installs an app", description = "Uploads a file and installs it as an app")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@POST
	@Path("/installApplication")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public AbstractApp installApplication(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@Parameter(hidden = true) MultipartFormDataInput input) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("file");
		if (inputParts == null)
			throw new EOFException();

		for (InputPart inputPart : inputParts) {
			
			InputStream inputStream = inputPart.getBody(InputStream.class, null);
			java.io.File file = TempUtils.createFile();
			try {
				try (FileOutputStream output = new FileOutputStream(file)) {
					org.apache.commons.io.IOUtils.copy(inputStream, output);
					output.close();
					return device.install(file);
				}
			} finally {
				file.delete();
			}
		}

		return null;
	}
	

	@Operation(method = "DELETE", summary = "Uninstalls an app", description = "Uninstalls an app")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@Path("{appid}")
	public void uninstallApplication(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("appid") @Parameter(name = "appid", description = "The id of app", required = true, in = ParameterIn.PATH) String appid) {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		device.uninstall(appid);
	}

	@Operation(method = "GET", summary = "Shows information about a specific app", description = "Shows information about a specific app")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	@Path("{appid}")
	public AbstractApp getInstalledAppInfo(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("appid") @Parameter(name = "appid", description = "The id of app", required = true, in = ParameterIn.PATH) String appid,
			@Parameter(hidden = true) MultipartFormDataInput input) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		return device.getInstalledApp(appid);
	}


	@Operation(method = "GET", summary = "Lists installed apps", description = "Lists installed apps")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AbstractApp.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	public Collection<? extends AbstractApp> getInstalledApps(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		device.getDeviceInfo().updateLastUsed(apiKey);

		return device.getInstalledApps().values();
	}

}
