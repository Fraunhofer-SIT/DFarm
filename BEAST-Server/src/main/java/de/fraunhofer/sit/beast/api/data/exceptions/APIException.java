package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An exception has occurred while processing the request.")
@XmlRootElement
public class APIException {

	@Schema(description = "A short description of the error that occured", required = true, example = "Internal Error")
	public String title;

	@Schema(description = "A more detailed description of the error that occured", required = true, example = "Out of storage space")
	public String description;

	@XmlTransient
	public int code;

	public APIException() {

	}

	public APIException(int code, String title, String description) {
		this.code = code;
		this.title = title;
		this.description = description;
	}

	public APIException(int code, String description) {
		this.code = code;
		this.title = description;
		this.description = description;
	}

}
