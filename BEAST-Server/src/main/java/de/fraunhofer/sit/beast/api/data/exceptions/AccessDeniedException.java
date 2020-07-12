package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access denied")
@XmlRootElement
public class AccessDeniedException extends APIException {


	public AccessDeniedException(String msg) {
		super(403, String.format("Access denied", msg));
	}

}
