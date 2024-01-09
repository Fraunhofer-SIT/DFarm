package de.fraunhofer.sit.beast.internal.interfaces;

import java.io.File;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

import de.fraunhofer.sit.beast.api.data.Key;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.internal.LogBuffer;

public interface IDevice {
	/**
	 * Installs an application
	 * 
	 * @param file
	 * @return the identifier of the app
	 */
	public String install(File file);

	public void uninstall(String id);

	public DeviceInformation getDeviceInfo();

	public String executeOnDevice(String command);

	public IFileListing getFileListing();

	public Map<String, ? extends AbstractApp> getInstalledApps();

	public boolean matchesDeviceRequirements(DeviceRequirements req);

	public void ping() throws Exception;

	public AbstractApp getInstalledApp(String id);

	public void changeState(DeviceState newState) throws SQLException;

	public void writePNGScreenshot(OutputStream outputStream);

	public void typeText(String text);

	public void tap(int x, int y);

	public void keyTyped(Key key);

	public void swipe(int x1, int y1, int x2, int y2, int duration);

	public IContactListing getContactListing();

	public IPortForwardingListing getPortFowardings();

	public ISniffing startSniffing();

	public LogBuffer getDeviceLog(String process);

}