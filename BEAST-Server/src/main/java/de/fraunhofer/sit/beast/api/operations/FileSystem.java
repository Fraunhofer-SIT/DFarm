package de.fraunhofer.sit.beast.api.operations;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.fraunhofer.sit.beast.api.data.FileOnDevice;
import de.fraunhofer.sit.beast.api.data.UploadedFile;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceNotFoundException;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Path("/api/devices/{devid}/filesystem")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "FileSystem", description = "Shows information about the filesystem"))
public class FileSystem {
	@RequestBody(description = "File to upload", required = true, content = {
			@Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UploadedFile.class, type = "object")) })
	@Operation(method = "POST", summary = "Uploads a file", description = "Uploads a file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))), })
	@POST
	@Path("/content")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void upload(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY) String path,
			@Parameter(hidden = true) MultipartFormDataInput input) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("file");
		if (inputParts == null)
			throw new EOFException();

		for (InputPart inputPart : inputParts) {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			try {
				inputStream = inputPart.getBody(InputStream.class, null);
				outputStream = dev.getFileListing().getFile(path).openWrite();
				org.apache.commons.io.IOUtils.copy(inputStream, outputStream);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				throw 
new APIExceptionWrapper(new APIException(500, "Reading error", "An error occurred while reading the input"));
			} finally {
				org.apache.commons.io.IOUtils.closeQuietly(inputStream);
				org.apache.commons.io.IOUtils.closeQuietly(outputStream);
			}


		}

		throw new APIExceptionWrapper(new APIException(400, "No input supplied", "Please upload a file"));
	}
	
	@Operation(method = "GET", summary = "Lists files", description = "List files")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FileOnDevice.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = APIException.class))), 
			@ApiResponse(responseCode = "400", description = "Not a directory", content = @Content(schema = @Schema(implementation = APIException.class))), 
	})
	@GET
	@Path("/list")
	public List<FileOnDevice> listFiles(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY, example="/") String path) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		IFile p = dev.getFileListing().getFile(path);
		if (!p.exists())
			throw new APIExceptionWrapper( new APIException(404, String.format("File not found: %s", path)));
		if (!p.isDirectory()) 
			throw new APIExceptionWrapper(new APIException(400, String.format("Not a directory: %s", path)));
		List<FileOnDevice> files = new ArrayList<FileOnDevice>();
		for (IFile file : p.listFiles()) {
			try {
				files.add(new FileOnDevice(file));
			} catch (FileNotFoundException e) {
				throw new APIExceptionWrapper(new APIException(404, String.format("File not found: %s", file.getShortName())));
			}
		}
		return files;
	}

	@Operation(method = "GET", summary = "Downloads a file", description = "Downloads a file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/octet-stream", schema = @Schema(description = "The file", format = "binary", type = "string"))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))), })
	@GET
	@Path("/content")
	@Produces("application/octet-stream")
	public Response download(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", in = ParameterIn.QUERY, description = "The path", required = true) String path)  {
		IDevice dev = getDevice(apiKey, devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		String filename =path.replace('\\', '/');
		filename = filename.substring(filename.lastIndexOf('/') + 1);
		InputStream input = dev.getFileListing().getFile(path).openRead();

		return Response.ok(input, MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.header("content-disposition", String.format("attachement; filename=\"%s\"", filename)).build();

	}

	
	@Operation(method = "DELETE", summary = "Deletes a file or folder", description = "Delete a file or folder")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = APIException.class))), 
	})
	@DELETE
	public void delete(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY, example="/") String path) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		IFile p = dev.getFileListing().getFile(path);
		p.deleteRecursively();
	}

	
	@Operation(method = "PUT", summary = "Creates a directory", description = "Creates a directory")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = APIException.class))),  
	})
	@Path("CreateDirectory")
	@PUT
	public void mkdir(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY, example="/") String path) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		IFile p = dev.getFileListing().getFile(path);
		p.mkdirs();
	}

	
	@Operation(method = "GET", summary = "Gets information about a file", description = "Gets information about a file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=FileOnDevice.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = APIException.class))),  
	})
	@Path("/info")
	@GET
	public FileOnDevice getInfo(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY, example="/") String path) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		IFile p = dev.getFileListing().getFile(path);
		return new FileOnDevice(p);
	}

	
	@Operation(method = "GET", summary = "Gets a md5 hashsum of a file in hex", description = "A md5 hashsum")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(type="string", implementation=String.class, format="binary"), examples= { @ExampleObject(value="d41d8cd98f00b204e9800998ecf8427e")})),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = AccessDeniedException.class))),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content(schema = @Schema(implementation = APIException.class))),  
	})
	@Path("/info/md5")
	@GET
	public String getMD5(@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@QueryParam("path") @Parameter(name = "path", description = "Path", required = true, in = ParameterIn.QUERY, example="/") String path) throws Exception {
		IDevice dev = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		dev.getDeviceInfo().updateLastUsed(apiKey);
		IFile p = dev.getFileListing().getFile(path);
		return p.getMD5Sum();
	}

	
	
	private IDevice getDevice(String apiKey, int devid) {
		return DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
	}
}
