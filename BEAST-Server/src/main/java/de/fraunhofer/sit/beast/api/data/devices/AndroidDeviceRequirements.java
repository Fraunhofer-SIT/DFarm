package de.fraunhofer.sit.beast.api.data.devices;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Android device requirements")
public class AndroidDeviceRequirements extends DeviceRequirements {
	public AndroidDeviceRequirements() {
		super("AndroidDeviceRequirements");
	}

	@Schema(required = false, description = "The minimum SDK version (-1 if don't care)")
	public int minSDKVersion = -1;

	@Schema(required = false, description = "The maximum SDK version (-1 if don't care)")
	public int maxSDKVersion = -1;
}
