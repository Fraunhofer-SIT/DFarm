package de.fraunhofer.sit.beast.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.CacheLoader;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceNotFoundException;
import de.fraunhofer.sit.beast.internal.android.AndroidDevice;
import de.fraunhofer.sit.beast.internal.android.AndroidDeviceManager;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IDeviceManager;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;

public class DeviceManager implements IDeviceManager {
	private static final Logger LOGGER = LogManager.getLogger(DeviceManager.class);
	private static final int TIME_WATCHDOG_SLEEP = 1000;
	public static final DeviceManager DEVICE_MANAGER = new DeviceManager();
	private static final long TIMEOUT_RELEASE = 1000 * 60 * 5;

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);

	private CacheLoader<Integer, IDevice> deviceCache = new CacheLoader<Integer, IDevice>() {

		@Override
		public IDevice load(Integer devid) throws Exception {
			for (IDeviceManager m : MANAGERS) {
				IDevice d = m.getDeviceById(devid);
				if (d != null) {
					if (d.getDeviceInfo() != null)
						d.getDeviceInfo().managerHostname = MainUtils.getHostName();
					return d;
				}
			}
			throw new APIExceptionWrapper(new DeviceNotFoundException(devid));
		}

	};

	private DeviceManager() {
		Thread watchDog = new Thread(new Runnable() {

			@Override
			public void run() {
				startWatchdog();
			}

		});
		watchDog.setDaemon(true);
		watchDog.setName("Android watchdog");
		watchDog.start();
		Thread batwatchDog = new Thread(new Runnable() {

			@Override
			public void run() {
				startBatteryWatchdog();
			}

		});
		batwatchDog.setDaemon(true);
		batwatchDog.setName("Android battery watchdog");
		batwatchDog.start();
	}

	private static IDeviceManager[] MANAGERS = new IDeviceManager[] { AndroidDeviceManager.MANAGER };
	private static boolean doPrepare = false;

	protected static void startWatchdog() {
		while (true) {
			try {
				Thread.sleep(TIME_WATCHDOG_SLEEP);
			} catch (InterruptedException e) {
			}
			Collection<? extends IDevice> allDevices = DEVICE_MANAGER.getDevices(null);
			ExecutorService pool = null;
			for (IDevice device : allDevices) {
				DeviceState prevState = device.getDeviceInfo().state;
				try {
					device.ping();
					// Let's hope everything is alright
					if (device.getDeviceInfo().reservedBy == null)
						device.changeState(DeviceState.FREE);
					else
						device.changeState(DeviceState.OCCUPIED);
					long unused = System.currentTimeMillis() - device.getDeviceInfo().lastUsed;
					if (unused > TIMEOUT_RELEASE) {
						if (pool == null)
							pool = Executors.newFixedThreadPool(64);
						pool.submit(new Runnable() {

							@Override
							public void run() {
								if (device.getDeviceInfo().reservedBy != null) {
									LOGGER.info(
											String.format("Device %s was reserved by %s, but unused for %d",
													device, device.getDeviceInfo().reservedBy, unused / 1000));
									try {
										releaseUnchecked(device, true);
									} catch (Exception e) {
										if (prevState != DeviceState.ERROR) {
											watchDogError(device, e);
										}
									}
								}
							}
						});
					}
				} catch (Throwable e) {
					LOGGER.error("A problem with the watchdog", e);
					if (prevState != DeviceState.ERROR && e instanceof Exception) {
						watchDogError(device, (Exception) e);
					}
				}
			}
			if (pool != null) {
				pool.shutdown();
				try {
					pool.awaitTermination(1, TimeUnit.DAYS);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	protected static void startBatteryWatchdog() {
		while (true) {
			try {
				Thread.sleep(TIME_WATCHDOG_SLEEP * 10);
			} catch (InterruptedException e) {
			}
			Collection<? extends IDevice> allDevices = DEVICE_MANAGER.getDevices(null);
			for (IDevice device : allDevices) {
				DeviceState prevState = device.getDeviceInfo().state;
				try {
					device.ping();
					DeviceInformation d = device.getDeviceInfo();
					if (d.state == DeviceState.FREE) {
						
						if (device instanceof AndroidDevice) {
							AndroidDevice dev = (AndroidDevice) device;
							try {
								d.batteryLevel = (short) (int) dev.getAndroidDevice().getBattery(500, TimeUnit.MILLISECONDS).get();
							} catch (Exception e) {
								
							}
						}

					}
				} catch (Throwable e) {
					LOGGER.error("A problem with the watchdog", e);
					if (prevState != DeviceState.ERROR && e instanceof Exception) {
						watchDogError(device, (Exception) e);
					}
				}
			}
		}
	}
	private static void watchDogError(IDevice device, Exception e) {
		Database.logError(device, e);
		try {
			device.changeState(DeviceState.ERROR);
		} catch (SQLException e1) {
			Database.logError(e1);
		}
	}

	@Override
	public Collection<IDevice> getDevices(DeviceRequirements reqFilter) {
		List<IDevice> deviceList = new ArrayList<>();
		for (IDeviceManager m : MANAGERS) {
			deviceList.addAll(m.getDevices(reqFilter));
		}
		return deviceList;
	}

	public void initialize() {
		// Probe for devices
		getDevices(null);
	}

	@Override
	public IDevice getDeviceById(int devid) {
		try {
			return deviceCache.load(devid);
		} catch (Exception e) {
			if (e.getCause() instanceof APIExceptionWrapper)
				throw (APIExceptionWrapper) e.getCause();
			throw new RuntimeException(e);
		}
	}

	public synchronized IDevice getDeviceByIdChecked(String apiKey, int devid) {
		if (apiKey == null) {
			String s = System.getenv("OVERRIDE_API_KEY");
			if (s != null)
				apiKey = s;
		}
		if (apiKey == null || apiKey.isEmpty())
			throw new APIExceptionWrapper(new APIException(400, "No API key supplied"));
		IDevice id = getDeviceById(devid);
		reserve(apiKey, id);

		try {
			Database.INSTANCE.updateDevice(id.getDeviceInfo());
		} catch (SQLException e) {
			Database.logError(e);
		}
		return id;
	}

	public synchronized void reserve(String apiKey, IDevice device) {
		String rby = device.getDeviceInfo().reservedBy;
		if (rby == null) {
			DeviceState cstate = device.getDeviceInfo().state;
			if (cstate != null && cstate != DeviceState.FREE)
				LOGGER.warn(String.format("Device is not free, but %s", cstate));
			try {
				device.changeState(DeviceState.OCCUPIED);
				device.getDeviceInfo().reservedBy = apiKey;
				device.getDeviceInfo().updateLastUsed();
				Database.INSTANCE.updateDevice(device.getDeviceInfo());
			} catch (SQLException e) {
				Database.logError(device, e);
			}
		} else if (!rby.equals(apiKey)) {
			throw new APIExceptionWrapper(new AccessDeniedException(String.format("Reserved by ", rby)));
		}
		device.getDeviceInfo().updateLastUsed(apiKey);
	}

	public synchronized void release(String apiKey, int devid, boolean reset) {
		IDevice d = getDeviceByIdChecked(apiKey, devid);
		try {
			releaseUnchecked(d, reset);
		} catch (SQLException e) {
			Database.logError(e);
		}

	}

	private static void releaseUnchecked(IDevice d, boolean reset) throws SQLException {
		if (d.getDeviceInfo().reservedBy == null)
			return;
		if (reset) {
			d.getDeviceInfo().reservedBy = null;
			Database.INSTANCE.updateDevice(d.getDeviceInfo());
			prepareAsync(d);
		} else
			d.changeState(DeviceState.FREE);
	}

	public synchronized void releaseAll(String apiKey, boolean reset) {
		DeviceRequirements r = new DeviceRequirements();
		r.reservedBy = apiKey;
		for (IDevice d : getDevices(r)) {
			release(apiKey, d.getDeviceInfo().ID, reset);
		}
	}

	public static void prepareAsync(IDevice d) throws SQLException {

		if (doPrepare)
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						EnvironmentStateManager.setEnvironment(d, DeviceState.FREE,
								EnvironmentStateManager.DEFAULT_ENVIRONMENT, false);
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			});

	}
}
