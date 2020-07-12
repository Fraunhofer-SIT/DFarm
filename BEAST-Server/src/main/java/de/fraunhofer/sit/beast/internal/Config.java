package de.fraunhofer.sit.beast.internal;

public class Config {
	
	public static int getDeviceStartRange() {
		return Integer.parseInt(ConfigBase.getProperties().get("DeviceRange").split("-")[0]);
	}
	
	public static int getDeviceEndRange() {
		return Integer.parseInt(ConfigBase.getProperties().get("DeviceRange").split("-")[1]);
	}

}
