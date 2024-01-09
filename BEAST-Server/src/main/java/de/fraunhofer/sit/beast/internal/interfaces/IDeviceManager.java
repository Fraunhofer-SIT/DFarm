package de.fraunhofer.sit.beast.internal.interfaces;

import java.util.Collection;

import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;

public interface IDeviceManager {

	Collection<? extends IDevice> getDevices(DeviceRequirements reqFilter);

	IDevice getDeviceById(int devid);

}
