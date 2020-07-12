package de.fraunhofer.sit.beast.internal.interfaces;

import de.fraunhofer.sit.beast.api.data.android.AndroidDeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.internal.android.AndroidApp;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Schema(description = "Information about an app", discriminatorProperty="type", discriminatorMapping= {
		@DiscriminatorMapping(schema = AndroidApp.class, value = "AndroidApp"),
		@DiscriminatorMapping(schema = AbstractApp.class, value = "AbstractApp")
		}, subTypes= {AndroidApp.class})
public abstract class AbstractApp {
	@Schema(required = true, description = "An identifier for the app", accessMode=AccessMode.READ_ONLY)
	public final String id;
	@Schema(required = true, description = "The type of app", accessMode=AccessMode.READ_ONLY)
	public final String type;

	public AbstractApp(String id, String type) {
		this.id = id;
		this.type = type;
	}
}
