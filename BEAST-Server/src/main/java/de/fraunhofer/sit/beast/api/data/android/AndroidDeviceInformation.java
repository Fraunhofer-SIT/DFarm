package de.fraunhofer.sit.beast.api.data.android;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;

import com.android.ddmlib.IDevice;
import com.j256.ormlite.field.DatabaseField;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.android.AndroidUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Schema(description = "Android Device Information", allOf = { DeviceInformation.class })
public class AndroidDeviceInformation extends DeviceInformation {

	private IDevice device;

	@XmlElement
	@Schema(example = "19", required = false, description = "API level", accessMode = AccessMode.READ_ONLY)
	@DatabaseField
	public int apiLevel;

	@XmlElement
	@Schema(example = "The serial number", required = false, description = "The serial number", accessMode = AccessMode.READ_ONLY)
	@DatabaseField(uniqueIndex = true)
	public final String serialNumber;

	@XmlElement
	@Schema(required = true, description = "A mapping from process (package) name to debug port, if the process is waiting for a debugger to attach. In the current implementation, values are never removed from this list.", accessMode = AccessMode.READ_ONLY)
	public HashMap<String, Integer> debugPorts = new HashMap<>();

	// Needed for ORMLite
	public AndroidDeviceInformation() {
		super("AndroidDeviceInformation");
		serialNumber = "";
	}

	public AndroidDeviceInformation(IDevice device) {
		super("AndroidDeviceInformation");
		this.device = device;
		apiLevel = device.getVersion().getApiLevel();
		serialNumber = device.getSerialNumber();
		name = device.getName();
		model = device.getProperty("ro.product.model");
		refresh();
	}

	public void refreshStart() {
		try {
			// batteryLevel = (short) (int) device.getBattery(FRESHNESS_BATTERY,
			// TimeUnit.MILLISECONDS).get();
			switch (device.getState()) {
			case BOOTLOADER:
				state = DeviceState.PREPARING;
				break;
			case DISCONNECTED:
			case OFFLINE:
			case UNAUTHORIZED:
			case RECOVERY:
			case SIDELOAD:
				state = DeviceState.ERROR;
				break;
			case ONLINE:
				if (state == DeviceState.DISCONNECTED)
					state = DeviceState.FREE;
				break;
			default:
				break;

			}
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public void refresh() {
		try {
			if (state == DeviceState.OCCUPIED && reservedBy == null)
				state = DeviceState.FREE;
			refreshStart();
			if (ID != 0)
				DeviceManager.DEVICE_MANAGER.getDeviceById(ID).changeState(state);
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public String getLongIdentifier() {
		return device.getSerialNumber() + " " + device.getName() + " - " + device.getState();
	}

	public void changeDevice(IDevice device) {
		this.device = device;
	}

}
