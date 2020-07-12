package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "File not found")
@XmlRootElement
public class FileNotFoundException extends APIException {


	public FileNotFoundException(String path) {
		super(404, String.format("File %s not found", path));
	}

}
