package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Device not found")
@XmlRootElement
public class DeviceNotFoundException extends APIException {


	public DeviceNotFoundException(int id) {
		super(404, String.format("Device with id %d not found", id));
	}

}
