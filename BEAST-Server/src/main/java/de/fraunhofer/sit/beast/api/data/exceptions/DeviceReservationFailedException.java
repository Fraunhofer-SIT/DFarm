package de.fraunhofer.sit.beast.api.data.exceptions;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "No device could be reserved")
@XmlRootElement
public class DeviceReservationFailedException extends APIException {
	public DeviceReservationFailedException() {

	}

	public DeviceReservationFailedException(int code, String title, String description) {
		super(code, title, description);
	}

}
