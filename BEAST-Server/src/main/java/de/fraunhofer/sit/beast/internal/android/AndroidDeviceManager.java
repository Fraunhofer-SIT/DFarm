package de.fraunhofer.sit.beast.internal.android;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData.DebuggerStatus;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;

import de.fraunhofer.sit.beast.api.data.android.AndroidDeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.internal.ConfigBase;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.EnvironmentStateManager;
import de.fraunhofer.sit.beast.internal.exceptions.NoStacktraceException;
import de.fraunhofer.sit.beast.internal.interfaces.IDeviceManager;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;

public class AndroidDeviceManager implements IDeviceManager {

	private static final Logger LOGGER = LogManager.getLogger(AndroidDeviceManager.class);

	private static final Map<Integer, AndroidDevice> cachedConnectedDeviceList = new HashMap<Integer, AndroidDevice>();

	public static final AndroidDeviceManager MANAGER = new AndroidDeviceManager();

	private File adbLocation;

	private AndroidDeviceManager() {
		install();
	}

	public File getADBLocation() {
		return adbLocation;
	}

	private void install() {
		DdmPreferences.setTimeOut(10000);

		AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {
			@Override
			public void deviceConnected(IDevice device) {
				connectDevice(device);
			}

			@Override
			public void deviceDisconnected(IDevice device) {
				LOGGER.warn(String.format("Device disconnected: %s", device.getName()));
				removeDevice(device);
			}

			@Override
			public void deviceChanged(IDevice device, int changeMask) {
			}
		});

		AndroidDebugBridge.init(true);

		AndroidDebugBridge.addClientChangeListener(new IClientChangeListener() {

			@Override
			public void clientChanged(Client client, int changeMask) {
				int port = client.getDebuggerListenPort();
				String packageName = client.getClientData().getClientDescription();
				if (packageName != null) {
					if (client.getClientData().getDebuggerConnectionStatus() == DebuggerStatus.WAITING) {
						LOGGER.info(String.format("found debug port %d for process %s", port, packageName));
						setDeviceDebugPortForProcess(client.getDevice(), packageName, port);

					} else if (client.getClientData().getDebuggerConnectionStatus() == DebuggerStatus.ATTACHED) {
						LOGGER.info(String.format("debugger attached to process %s. removing port from listing",
								packageName));
						clearDeviceDebugPortForProcess(client.getDevice(), packageName);
					}
				}
			}
		});

		String adbL = ConfigBase.getString("Android.ADB.Location", true);
		if (adbL == null)
			throw new NoStacktraceException("No Android.ADB.Location specified");
		File adbLocation = new File(adbL);
		if (!adbLocation.exists() || !adbLocation.isFile()) {
			throw new NoStacktraceException(
					String.format("ADB Location %s does not exist or is not a file", adbLocation));
		}
		this.adbLocation = adbLocation;
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbLocation.getAbsolutePath(), true);
		for (IDevice d : bridge.getDevices()) {
			connectDevice(d);
		}

	}

	protected AndroidDeviceInformation getInformation(IDevice device) throws SQLException {
		if (device.getSerialNumber() == null) {
			NoStacktraceException e = new NoStacktraceException("Device has no serial number");
			Database.logError(device, e);
			throw e;
		}
		AndroidDeviceInformation s = Database.INSTANCE.getAndroidDeviceInfo(device.getSerialNumber());
		if (s == null) {
			s = new AndroidDeviceInformation(device);
			Database.INSTANCE.insert(s);
		} else {
			s.changeDevice(device);
		}
		s.refreshStart();

		SavedEnvironment db = Database.INSTANCE.getSavedEnvironmentUnsafe(s.ID,
				EnvironmentStateManager.DEFAULT_ENVIRONMENT);
		if (db == null) {
			// EnvironmentStateManager.saveEnvironmentState(dev,
			// EnvironmentStateManager.DEFAULT_ENVIRONMENT);
		} else {

			// EnvironmentStateManager.loadEnvironmentState(dev,
			// EnvironmentStateManager.DEFAULT_ENVIRONMENT);
		}
		return s;
	}

	private void setDeviceDebugPortForProcess(IDevice device, String processname, int port) {
		synchronized (cachedConnectedDeviceList) {
			Iterator<AndroidDevice> it = cachedConnectedDeviceList.values().iterator();
			while (it.hasNext()) {
				AndroidDevice dev = it.next();
				if (dev.getAndroidDevice().equals(device)) {
					((AndroidDeviceInformation) dev.getDeviceInfo()).debugPorts.put(processname, port);
					break;
				}
			}
		}
	}

	private void clearDeviceDebugPortForProcess(IDevice device, String processname) {
		synchronized (cachedConnectedDeviceList) {
			Iterator<AndroidDevice> it = cachedConnectedDeviceList.values().iterator();
			while (it.hasNext()) {
				AndroidDevice dev = it.next();
				if (dev.getAndroidDevice().equals(device)) {
					((AndroidDeviceInformation) dev.getDeviceInfo()).debugPorts.remove(processname);
					break;
				}
			}
		}
	}

	@Override
	public Collection<? extends AndroidDevice> getDevices(DeviceRequirements reqFilter) {
		synchronized (cachedConnectedDeviceList) {
			if (reqFilter == null)
				// Clone to prevent CMEs
				return new ArrayList<AndroidDevice>(cachedConnectedDeviceList.values());
			else {
				List<AndroidDevice> res = new ArrayList<>();
				for (AndroidDevice device : cachedConnectedDeviceList.values()) {
					try {

						if (device.matchesDeviceRequirements(reqFilter))
							res.add(device);
					} catch (APIExceptionWrapper e) {
						Database.logError(device, e);
					}
				}
				return res;
			}
		}
	}

	private void removeDevice(IDevice device) {
		synchronized (cachedConnectedDeviceList) {
			Iterator<AndroidDevice> it = cachedConnectedDeviceList.values().iterator();
			while (it.hasNext()) {
				AndroidDevice dev = it.next();
				if (dev.getAndroidDevice().equals(device)) {
					try {
						dev.changeState(DeviceState.DISCONNECTED);
					} catch (SQLException e) {
						Database.logError(dev, e);
					}
					it.remove();
					break;
				}
			}
		}
	}

	@Override
	public de.fraunhofer.sit.beast.internal.interfaces.IDevice getDeviceById(int devid) {
		synchronized (cachedConnectedDeviceList) {
			return cachedConnectedDeviceList.get(devid);
		}
	}

	private void connectDevice(IDevice device) {
		LOGGER.info(String.format("Device connected: %s", device.getName()));
		long msBefore = System.currentTimeMillis();
		while (System.currentTimeMillis() - msBefore < 30000) {
			if (device.isOnline())
				break;
		}
		if (device.isOnline())
			LOGGER.info(String.format("Device is online now: %s", device.getName()));
		else
			LOGGER.info(String.format("Timeout: Device is %s now: %s", device.getState(), device.getName()));
		try {
			AndroidDeviceInformation info = getInformation(device);
			if (info.name.toLowerCase().startsWith("tp") || (info.model != null && info.model.startsWith("tp_link")))
				// tp link is too old!
				return;
			AndroidDevice dev = new AndroidDevice(device, info);

			if (info.state == DeviceState.PREPARING) {
				LOGGER.info(String.format("Redo preparing for: %s", device.getName()));
				// restart prepare
				DeviceManager.prepareAsync(dev);
			}
			synchronized (cachedConnectedDeviceList) {
				cachedConnectedDeviceList.put(dev.getDeviceInfo().ID, dev);
			}
		} catch (Exception e) {
			Database.logError(device, e);
		}
	}

}
