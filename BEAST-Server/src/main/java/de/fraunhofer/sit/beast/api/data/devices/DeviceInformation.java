package de.fraunhofer.sit.beast.api.data.devices;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.j256.ormlite.field.DatabaseField;

import de.fraunhofer.sit.beast.api.data.android.AndroidDeviceInformation;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@XmlType(name = "DeviceInformation")
@Schema(description = "Information about a device", required=true, discriminatorProperty="type", discriminatorMapping= {
		@DiscriminatorMapping(schema = AndroidDeviceInformation.class, value = "AndroidDeviceInformation"),
		@DiscriminatorMapping(schema = DeviceInformation.class, value = "DeviceInformation")
		})
@JsonSubTypes({
@Type(value = AndroidDeviceInformation.class, name = "AndroidDeviceInformation"),
})
@JsonTypeInfo(use = Id.NAME,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
public abstract class DeviceInformation {
	@XmlElement
	@Schema(example = "0", required = true, description = "The id of the device", accessMode=AccessMode.READ_ONLY)
	@DatabaseField(id=true)
	public int ID;

	@XmlElement
	@Schema(example = "The name", required = false, description = "The name", accessMode=AccessMode.READ_ONLY)
	@DatabaseField
	public String name;
	
	@XmlElement
	@Schema(required = true, description = "The state of the device", accessMode=AccessMode.READ_ONLY)
	@DatabaseField
	public volatile DeviceState state;

	@XmlElement
	@Schema(required = true, description = "The battery level (from 0 to 100 percent or -1 if unknown)", accessMode=AccessMode.READ_ONLY)
	public short batteryLevel = -1;

	@DatabaseField
	@Schema(required = false, description = "Who is using the device at the moment", accessMode=AccessMode.READ_ONLY)
	public String reservedBy;

	@DatabaseField
	@JsonIgnore
	@JsonIgnoreProperties
	public long lastUsed = -1;

	@Schema(required = true, description = "Type", example="AndroidDeviceRequirements", accessMode=AccessMode.READ_ONLY)
	public String type;
	
	public DeviceInformation(String type) {
		this.type = type;
	}

	public abstract void refresh() ;

	@JsonIgnore
	@JsonIgnoreProperties
	public abstract String getLongIdentifier();

	public void updateLastUsed() {
		lastUsed = System.currentTimeMillis();
	}

	/**
	 * Updates the last used entry only if the device has been
	 * reserved by the given api key
	 * @param apiKey
	 */
	public void updateLastUsed(String apiKey) {
		if (reservedBy != null && reservedBy.equals(apiKey))
			updateLastUsed();
	}

}
