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
import javax.ws.rs.PUT;
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
import de.fraunhofer.sit.beast.api.data.contacts.Contact;
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

@Path("/api/devices/{devid}/contacts/")
@Produces(MediaType.APPLICATION_JSON)
@SecuritySchemes(value = @SecurityScheme(type = SecuritySchemeType.APIKEY, description = "Your API key", in = SecuritySchemeIn.HEADER, name = "APIKey", paramName = "APIKey"))
@Tags(@Tag(name = "Contacts", description = "Manages the contacts"))
public class Contacts
{
	@Operation(method = "PUT", summary = "Adds or updates a contact", description = "Adds or uploads a contact")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AbstractApp.class))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })	@PUT
    @Consumes("application/json")
	public void addContact(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@RequestBody(required=true, content= {@Content(schema=@Schema(implementation=Contact.class)) })			Contact contact
			) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		device.getContactListing().insertOrUpdateContact(contact);
	}
	

	@Operation(method = "DELETE", summary = "Deletes a contact", description = "Deletes a contact")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@DELETE
	@POST
	@Path("/{id}")
	public void uninstallApplication(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid,
			@PathParam("id") @Parameter(name = "id", description = "The id of the contact", required = true, in = ParameterIn.PATH) int contactid
			 ) {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceByIdChecked(apiKey, devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		device.getContactListing().deleteContact(contactid);
	}


	@Operation(method = "GET", summary = "Lists contacts", description = "Lists contacts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
			@ApiResponse(responseCode = "500", description = "Internal error", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "400", description = "Wrong input", content = @Content(schema = @Schema(implementation = APIException.class))),
			@ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = APIException.class))), })
	@GET
	public Collection<? extends Contact> getContacts(
			@org.jboss.resteasy.annotations.jaxrs.HeaderParam("APIKey") @Parameter(hidden = true) String apiKey,
			@PathParam("devid") @Parameter(name = "devid", description = "The id of device", required = true, in = ParameterIn.PATH) int devid) throws Exception {
		IDevice device = DeviceManager.DEVICE_MANAGER.getDeviceById(devid);
		device.getDeviceInfo().updateLastUsed(apiKey);
		return device.getContactListing().getContacts();
	}

}
