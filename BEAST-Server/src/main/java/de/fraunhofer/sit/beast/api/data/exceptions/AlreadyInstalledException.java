package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application is already installed")
@XmlRootElement
public class AlreadyInstalledException extends APIException {

	public AlreadyInstalledException() {
		super(400, "Already installed", "Already installed");
	}

	public AlreadyInstalledException(String app) {
		super(400, String.format("App %s is already installed", app),
				String.format("App %s is already installed", app));
	}
}
