package de.fraunhofer.sit.beast.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.AccessDeniedException;
import de.fraunhofer.sit.beast.api.data.exceptions.DeviceNotFoundException;
import de.fraunhofer.sit.beast.api.data.exceptions.ExceptionProvider;
import de.fraunhofer.sit.beast.internal.android.AndroidDeviceManager;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IDeviceManager;
import de.fraunhofer.sit.beast.internal.persistance.Database;

public class DeviceManager implements IDeviceManager {
	private static final Logger LOGGER = Logger.getLogger(DeviceManager.class);
	private static final int TIME_WATCHDOG_SLEEP = 60 * 1000;
	public static final DeviceManager DEVICE_MANAGER = new DeviceManager();
	private static final long TIMEOUT_RELEASE = 1000 * 60 * 10;

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(64);


	private LoadingCache<Integer, IDevice> deviceCache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IDevice>() {

		@Override
		public IDevice load(Integer devid) throws Exception {
			for (IDeviceManager m : MANAGERS) {
				IDevice d = m.getDeviceById(devid);
				if (d != null)
					return d;
			}
			throw new APIExceptionWrapper(new DeviceNotFoundException(devid));
		}

	});

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
	}

	private static IDeviceManager[] MANAGERS = new IDeviceManager[] { AndroidDeviceManager.MANAGER };

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
					long unused = System.currentTimeMillis() - device.getDeviceInfo().lastUsed;
					if (unused > TIMEOUT_RELEASE) {
						if (pool == null)
							pool = Executors.newFixedThreadPool(64);
						pool.submit(new Runnable() {

							@Override
							public void run() {
								if (device.getDeviceInfo().reservedBy != null) {
									LOGGER.info(String.format("Device %s was reserved by %d seconds, but unused since %s", device, device.getDeviceInfo().reservedBy, unused / 1000));
									try {
										releaseUnchecked(device);
									} catch (Exception e) {
										if (prevState != DeviceState.ERROR) {
											watchDogError(device, e);
										}
									}
								}
							}
						});
					}
				} catch (Exception e) {
					if (prevState != DeviceState.ERROR) {
						watchDogError(device, e);
					}
				}
			}
			if (pool != null)
			{
				pool.shutdown();
				try {
					pool.awaitTermination(1, TimeUnit.DAYS);
				} catch (InterruptedException e) {
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
		//Probe for devices
		getDevices(null);
	}

	@Override
	public IDevice getDeviceById(int devid) {
		try {
			return deviceCache.get(devid);
		} catch (Exception e) {
			if (e.getCause() instanceof APIExceptionWrapper)
				throw (APIExceptionWrapper)e.getCause();
			throw new RuntimeException(e);
		}
	}

	public synchronized IDevice getDeviceByIdChecked(String apiKey, int devid)  {
		if (apiKey == null)
		{
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

	public synchronized void reserve(String apiKey, IDevice device)  {
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

	public synchronized void release(String apiKey, int devid) {
		IDevice d = getDeviceByIdChecked(apiKey, devid);
		try {
			releaseUnchecked(d);
		} catch (SQLException e) {
			Database.logError(e);
		}

	}

	private static void releaseUnchecked(IDevice d) throws SQLException {
		if (d.getDeviceInfo().reservedBy == null)
			return;
		d.getDeviceInfo().reservedBy = null;
		prepareAsync(d);
		Database.INSTANCE.updateDevice(d.getDeviceInfo());
	}

	public synchronized void releaseAll(String apiKey) {
		DeviceRequirements r = new DeviceRequirements();
		r.reservedBy = apiKey;
		for (IDevice d : getDevices(r)) {
			release(apiKey, d.getDeviceInfo().ID);
		}
	}

	public static void prepareAsync(IDevice d) throws SQLException {

		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					EnvironmentStateManager.setEnvironment(d, DeviceState.FREE, EnvironmentStateManager.DEFAULT_ENVIRONMENT);
				}
				catch (SQLException e) {
					e.printStackTrace();
				}

			}
		});




	}
}
