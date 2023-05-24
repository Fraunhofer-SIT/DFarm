package de.fraunhofer.sit.beast.api.data.devices;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Device requirements", discriminatorProperty="type")
@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
		@JsonSubTypes({ 
		  @Type(value = AndroidDeviceRequirements.class, name = "AndroidDeviceRequirements")
		})
public class DeviceRequirements {
	@Schema(required = true, description = "The minimum battery level (-1 if don't care)", defaultValue = "30")
	public int minBatteryLevel = 30;

	@Schema(required = false, description = "A required state", example="FREE")
	public DeviceState state = null;

	@Schema(required = true, description = "Type", example="AndroidDeviceRequirements")
	public String type;

	@Schema(required = false, description = "The device must be reserved by the given user", example="")
	public String reservedBy;
	
	@Schema(required = false, description = "Excluded device ids", example="")
	public Integer[] excludedIDs;
	
	
	
	public DeviceRequirements(String type) {
		this.type =type;
	}
	
	public DeviceRequirements() {
		
	}

	@Override
	public String toString() {
		return "DeviceRequirements [minBatteryLevel=" + minBatteryLevel + ", state=" + state + ", type=" + type
				+ ", reservedBy=" + reservedBy + "]";
	}
	
	
}
