package de.fraunhofer.sit.beast.api.operations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(security = {
		@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "APIKey") }, info = @Info(title = "D-FARM API", version = "0.2", description = "D-FARM API", contact = @Contact(email = "helpdesk@codeinspect.de", name = "CodeInspect Support Team", url = "https://codeinspect.de")))
public class Information extends Application {

}
