package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application is not installed")
@XmlRootElement
public class AppNotInstalledException extends APIException {

	public AppNotInstalledException() {
		super(400, "App is not installed", "App is not installed");
	}

	public AppNotInstalledException(String name) {
		super(400, String.format("App %s is not installed", name), String.format("App %s is not installed", name));
	}

}
