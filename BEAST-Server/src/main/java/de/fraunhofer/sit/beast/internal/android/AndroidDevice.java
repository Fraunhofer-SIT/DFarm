package de.fraunhofer.sit.beast.internal.android;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.logcat.LogCatHeader;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ForwardingBlockingDeque;

import de.fraunhofer.sit.beast.api.data.Key;
import de.fraunhofer.sit.beast.api.data.android.AndroidDeviceInformation;
import de.fraunhofer.sit.beast.api.data.android.Intent;
import de.fraunhofer.sit.beast.api.data.devices.AndroidDeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceRequirements;
import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.internal.AndroidFileDownloader;
import de.fraunhofer.sit.beast.internal.Config;
import de.fraunhofer.sit.beast.internal.ConfigBase;
import de.fraunhofer.sit.beast.internal.LogBuffer;
import de.fraunhofer.sit.beast.internal.interfaces.IContactListing;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;
import de.fraunhofer.sit.beast.internal.interfaces.IPortForwardingListing;
import de.fraunhofer.sit.beast.internal.interfaces.ISniffing;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.utils.LookAheadBufferedReader;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class AndroidDevice implements de.fraunhofer.sit.beast.internal.interfaces.IDevice {

	private static final String PM_LIST_PACKAGES = "pm list packages";

	private static final Logger LOGGER = LogManager.getLogger(AndroidDevice.class);

	private static final String LAST_UPDATE_TIME = "lastUpdateTime";

	private static final String FIRST_INSTALL_TIME = "firstInstallTime";

	private static final String CODE_PATH = "codePath";

	private static final String VERSION_NAME = "versionName";

	private static final String PKG_FLAGS = "pkgFlags";

	private static final String VERSION_CODE = "versionCode";

	private static final String TARGET_SDK = "targetSdk";

	private static final String PACKAGE = "Package [";

	private static final String DUMPSYS_GET_PACKAGEINFO = "dumpsys";

	private static final String DUMPSYS_GET_PACKAGEINFO_SPECIFIC = "dumpsys package %s";

	private static final SimpleDateFormat ANDROID_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private IDevice device;
	private AndroidDeviceInformation deviceInformation;

	private static class Input {
		private String command;

		public Input() {

		}

		public Input(String command) {
			this.command = command;
		}

		@Override
		public String toString() {
			return command;
		}

	}

	private static class InputText extends Input {
		public String text;

		public InputText(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return "input text \"" + text + "\"";
		}
	}

	private static class InputKeys extends Input {
		private String keys;

		public InputKeys(String keys) {
			this.keys = keys;
		}

		@Override
		public String toString() {
			return "input keyevent " + keys;
		}
	}

	private ArrayDeque<Input> inputQueue = new ArrayDeque<>();

	private final LoadingCache<String, AndroidApp> cacheApps = CacheBuilder.newBuilder()
			.build(new CacheLoader<String, AndroidApp>() {

				@Override
				public AndroidApp load(String key) throws Exception {
					return getInstalledApp(key);
				}

			});

	private boolean hasAppsInCache;

	private Thread inputHandler;

	public AndroidDevice(IDevice device) {
		this.device = device;
		deviceInformation = new AndroidDeviceInformation(device);
		try {
			device.executeShellCommand("svc power stayon false", new NullOutputReceiver());
		} catch (Exception e) {
			LOGGER.error("Could not turn screen off", e);
		}
	}

	public AndroidDevice(IDevice device, AndroidDeviceInformation info) {
		this.device = device;
		this.deviceInformation = info;
		try {
			device.executeShellCommand("svc power stayon false", new NullOutputReceiver());
		} catch (Exception e) {
			LOGGER.error("Could not turn screen off", e);
		}
	}

	@Override
	public String install(File file) {
		
		try {
			ProcessManifest manifest = new ProcessManifest(file); 
			String packageName;
			try {
				packageName = manifest.getPackageName();
			} finally {
				manifest.close();
			}
			// Use with the -g switch, the app is installed with all runtime permissions
			// pre-granted,
			// as if the app had a targetSdkVersion below 22. This can be handy for rapid
			// testing,
			// though it is not indicative of what the user will see.
			device.installPackage(file.getAbsolutePath(), true, "-g");
			cacheApps.invalidate(packageName);
			return packageName;
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public void uninstall(String packageName) {
		try {
			String error = device.uninstallPackage(packageName);
			if (error != null)
				throw new APIExceptionWrapper(new APIException(500, String.format("Uninstall error - %s", error)));
			cacheApps.invalidate(packageName);
		} catch (InstallException e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public DeviceInformation getDeviceInfo() {
		return deviceInformation;
	}

	@Override
	public String executeOnDevice(String command) {
		try {
			CollectingOutputReceiver rec = new CollectingOutputReceiver();
			device.executeShellCommand(command, rec);
			return rec.getOutput();
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public AndroidApp getInstalledApp(String id) {
		try {
			AndroidApp app = getInstalledAppUnsafe(id);
			if (app == null)
				throw new APIExceptionWrapper(new APIException(404, String.format("App not found: %s", id)));
			return app;
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	public AndroidApp getInstalledAppUnsafe(String id)
			throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
		AndroidApp app = cacheApps.getIfPresent(id);
		if (app != null)
			return app;

		final CollectingOutputReceiver packageInfo = new CollectingOutputReceiver();

		device.executeShellCommand(String.format(DUMPSYS_GET_PACKAGEINFO_SPECIFIC, id), packageInfo, 2000000,
				TimeUnit.MILLISECONDS);

		Map<String, AndroidApp> items = readAppInfo(packageInfo);
		app = items.get(id);
		return app;
	}

	@Override
	public Map<String, AndroidApp> getInstalledApps() {
		try {
			final CollectingOutputReceiver packageInfo = new CollectingOutputReceiver();
			if (!hasAppsInCache) {
				device.executeShellCommand(DUMPSYS_GET_PACKAGEINFO, packageInfo, 2000000, TimeUnit.MILLISECONDS);
				return readAppInfo(packageInfo);
			} else {
				final HashMap<String, AndroidApp> items = new HashMap<String, AndroidApp>();
				CollectingOutputReceiver packages = new CollectingOutputReceiver();
				try {
					device.executeShellCommand(PM_LIST_PACKAGES, packages, 30000, TimeUnit.MILLISECONDS);
				} catch (Exception e) {

				}

				final BufferedReader reader = new BufferedReader(new StringReader(packages.getOutput()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("package:")) {
						// Trim away .package.
						line = line.substring(8);
						items.put(line, getInstalledApp(line));
					}
				}
				return items;

			}
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	private Map<String, AndroidApp> readAppInfo(final CollectingOutputReceiver packageInfo) throws IOException {
		final HashMap<String, AndroidApp> items = new HashMap<String, AndroidApp>();
		String line;
		final LookAheadBufferedReader readerPackageInfo = new LookAheadBufferedReader(
				new StringReader(packageInfo.getOutput()), 1024 * 1024);

		String packageName = null;
		AndroidApp item = null;
		while ((line = readerPackageInfo.readLine()) != null) {
			line = line.trim();
			if (line.startsWith(PACKAGE)) {
				line = line.substring(PACKAGE.length(), line.indexOf("]"));
				packageName = line;
				item = new AndroidApp(packageName);
				items.put(packageName, item);
			} else {
				if (item != null) {
					if (line.contains("install permissions:")) {
						List<String> entries = readList(readerPackageInfo);
						item.installPermissions = new ArrayList<AndroidPermission>(entries.size());
						for (String entry : entries) {
							item.installPermissions.add(AndroidPermission.parsePermission(entry));
						}
					}
					
					if (line.contains("runtime permissions:")) {
						List<String> entries = readList(readerPackageInfo);
						item.runtimePermissions = new ArrayList<AndroidPermission>(entries.size());
						for (String entry : entries) {
							item.runtimePermissions.add(AndroidPermission.parsePermission(entry));
						}
					}
					if (line.contains("declared permissions:")) {
						List<String> entries = readList(readerPackageInfo);
						item.declaredPermissions = new ArrayList<AndroidPermission>(entries.size());
						for (String entry : entries) {
							item.declaredPermissions.add(AndroidPermission.parsePermission(entry));
						}
					}
					if (line.contains("=")) {
						if (line.contains(TARGET_SDK) && line.contains(VERSION_CODE)) {
							try {
								final String[] lineParts = line.split(" ");
								final String[] partsVersionCode = lineParts[0].split("=");
								final String[] partsTargetSDK = lineParts[1].split("=");
								final String versionCodeValue = partsVersionCode[1];
								final String targetSDKValue = partsTargetSDK[1];
								item.versionCode = MainUtils.tryParseInt(versionCodeValue);
								item.targetSDK = MainUtils.tryParseInt(targetSDKValue);
							} catch (Exception e) {
								LOGGER.error(String.format("Could not parse %s", line), e);
							}
						}

						int sepPos = line.indexOf("=");
						if (sepPos < 0)
							continue;

						final String name = line.substring(0, sepPos);
						final String value = line.substring(sepPos + 1);

						
						if (name.equalsIgnoreCase(PKG_FLAGS)) {
							String[] flagList = value.substring(1, value.length() - 1).trim().split(" ");
							for (String s : flagList) {
								int val = 0;
								switch (s) {
								case "SYSTEM":
									item.systemApp = true;
									break;
								case "ALLOW_BACKUP":
									item.allowBackup = true;
									break;
								}
							}
						} else if (name.equalsIgnoreCase(VERSION_NAME))
							item.versionName = value;
						else if (name.equalsIgnoreCase(CODE_PATH))
							item.codePath = value;
						else if (name.equalsIgnoreCase(FIRST_INSTALL_TIME)) {
							try {
								Date date = ANDROID_DATE_FORMAT.parse(value);
								item.firstDateInstall = date;
							} catch (ParseException e) {
								LOGGER.error(String.format("Could not parse %s", line), e);
							}
						} else if (name.equalsIgnoreCase(LAST_UPDATE_TIME)) {
							try {
								Date date = ANDROID_DATE_FORMAT.parse(value);
								item.lastUpdateTime = date;

							} catch (ParseException e) {
								LOGGER.error(String.format("Could not parse %s", line), e);
							}
						}
					}

				}
			}
		}
		hasAppsInCache = true;
		for (AndroidApp app : items.values()) {
			cacheApps.put(app.id, app);
		}
		return items;
	}

	private List<String> readList(final LookAheadBufferedReader readerPackageInfo) throws IOException {
		List<String> entries = new ArrayList<>();
		int numOfSpaces = -1;
		String cline;
		while ((cline = readerPackageInfo.lookAheadLine()) != null) {
			if (cline.isEmpty())
				break;
			if (numOfSpaces == -1)
				numOfSpaces = countStartSpaces(cline);
			else if (numOfSpaces != countStartSpaces(cline))
				break;
			cline = cline.trim();
			entries.add(cline);
		}
		return entries;
	}

	private int countStartSpaces(String line) {
		int count = 0;
		for (int i = 0; i < line.length(); i++) {
			if (Character.isWhitespace(line.charAt(i)))
				count++;
			else
				return count;
		}
		return count;
	}

	@Override
	public IFileListing getFileListing() {
		return new AndroidFileListing(this);
	}

	public IDevice getAndroidDevice() {
		return device;
	}

	@Override
	public boolean matchesDeviceRequirements(DeviceRequirements req) {
		deviceInformation.refresh();
		if (req.reservedBy != null) {
			if (!req.reservedBy.equals(getDeviceInfo().reservedBy))
				return false;
		}
		if (req.state != null && req.state != deviceInformation.state && deviceInformation.state != null)
			return false;
		if (req.excludedIDs != null && Arrays.asList(req.excludedIDs).contains(getDeviceInfo().ID))
			return false;
		if (req.minBatteryLevel != -1) {
			if (deviceInformation.batteryLevel < req.minBatteryLevel)
				return false;
		}
		if (req instanceof AndroidDeviceRequirements) {
			AndroidDeviceRequirements r = (AndroidDeviceRequirements) req;
			if (deviceInformation.apiLevel < r.minSDKVersion) {
				return false;
			}
			if (deviceInformation.apiLevel > r.maxSDKVersion && r.maxSDKVersion != -1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return deviceInformation.getLongIdentifier();
	}

	@Override
	public void ping() throws Exception {
		device.executeShellCommand("ls", new NullOutputReceiver());
	}

	@Override
	public void changeState(DeviceState newState) throws SQLException {
		deviceInformation.state = newState;
		if (newState == DeviceState.FREE || newState == DeviceState.PREPARING) {
			try {
				device.executeShellCommand("svc power stayon false", new NullOutputReceiver());
			} catch (Exception e) {
				LOGGER.error("Could not turn screen off", e);
			}
		}
		if (newState != DeviceState.OCCUPIED)
			deviceInformation.reservedBy = null;
		else {
			//Someone reserved this device. We better make sure that the phone is ready and unlocked.
			try {
				//turnScreenOn();
			} catch (Exception e) {
				LOGGER.error("An error occurred while turning screen on", e);
			}
			try {
				device.executeShellCommand("svc power stayon usb", new NullOutputReceiver());
			} catch (Exception e) {
				LOGGER.error("Could not keep screen on", e);
			}
			String pinCode = ConfigBase.getProperties().get("Android.PINCode");
			if (pinCode != null && !pinCode.isEmpty()) {
				LOGGER.info("Entering PIN-Code automatically");
				try {
					Thread.sleep(1000);
					boolean locked = isLocked();
					LOGGER.info("Is locked");
					if (locked) {
						device.executeShellCommand("input touchscreen swipe 530 1420 530 320", new NullOutputReceiver());
						Thread.sleep(1000);
						device.executeShellCommand("input text " + pinCode, new NullOutputReceiver());
						Thread.sleep(1000);
						device.executeShellCommand("input keyevent 66", new NullOutputReceiver());
						Thread.sleep(1000);
						locked = isLocked();
						if (locked) 
							LOGGER.error("Is still locked: " + device.getName());
						else
							LOGGER.info("Is unlocked");
					} else
						LOGGER.info("Screen was already unlocked.");
				} catch (Exception e) {
					LOGGER.error("Could not input PIN", e);
				}
			} else
				LOGGER.info("Entering PIN-Code disabled");
		}
		Database.INSTANCE.updateDevice(deviceInformation);
	}

	public boolean isLocked()
			throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
		CollectingOutputReceiver collect = new CollectingOutputReceiver();
		device.executeShellCommand("dumpsys window", collect);
		LineIterator it = IOUtils.lineIterator(new StringReader(collect.getOutput()));
		boolean locked = true;
		while (it.hasNext()) {
			String l = it.nextLine();
			if (l.contains("mDreamingLockscreen"))
			{
				locked = false;
				if (l.contains("mDreamingLockscreen=true"))
					locked =true;
				break;
			}
		}
		return locked;
	}

	private synchronized void turnScreenOn() throws TimeoutException, AdbCommandRejectedException, IOException {
		deviceInformation.updateLastUsed();
		final RawImage rawImage = device.getScreenshot();
		if (rawImage == null) {
			return;
		}
		int IndexInc = rawImage.bpp >> 3;
		int index = 0;
		for (int y = 0; y < rawImage.height; y += 2) {
			for (int x = 0; x < rawImage.width; x += 2) {
				int value = rawImage.getARGB(index);
				if (value > -16777210)
					return;
				index += IndexInc;
			}
		}
		
		//Screen is off.
		keyTyped(Key.KEYCODE_POWER);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			LOGGER.warn("Interrupted", e);
		}
	}

	@Override
	public void writePNGScreenshot(OutputStream outputStream) {
		try {
			deviceInformation.updateLastUsed();
			final RawImage rawImage = device.getScreenshot();
			if (rawImage == null) {
				return;
			}
			final BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);

			int index = 0;
			int IndexInc = rawImage.bpp >> 3;
			for (int y = 0; y < rawImage.height; y++) {
				for (int x = 0; x < rawImage.width; x++) {
					int value = rawImage.getARGB(index);
					index += IndexInc;
					image.setRGB(x, y, value);
				}
			}

			if (!ImageIO.write(image, "png", outputStream)) {
				throw new IOException("Failed to find png writer");
			}
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public void typeText(String text) {
		try {

			text = text.replace(" ", "%s");
			synchronized (inputQueue) {
				InputText i = searchForInputQueueItem(InputText.class);
				if (i != null) {
					i.text += text;
					System.out.println(inputQueue);
					return;
				}
				executeInputOnDevice(new InputText(text));
			}
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	private <T> T searchForInputQueueItem(Class<T> class1) {
		Input i = inputQueue.peekLast();
		if (class1.isInstance(i)) {
			@SuppressWarnings("unchecked")
			T inp = (T) i;
			return inp;
		}
		return null;
	}

	@Override
	public void tap(int x, int y) {
		try {
			executeInputOnDevice(new Input(String.format("input tap %d %d", x, y)));
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}

	}

	private void executeInputOnDevice(Input input) {
		if (inputHandler == null) {
			inputHandler = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						Input inp;
						String k = null;
						synchronized (inputQueue) {

							inp = inputQueue.poll();
							if (inp != null)
								k = inp.toString();
						}
						if (inp != null) {
							executeOnDevice(k);
							continue;
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
				}
			});
			inputHandler.setName("Input handler for " + device.getName());
			inputHandler.setDaemon(true);
			inputHandler.start();
		}
		this.inputQueue.add(input);
		System.out.println(inputQueue.toString());
	}

	public boolean pullInstalledApp(AndroidApp app, File appFile) {
		String apkFileOnDevice = app.codePath;
		AndroidFileDownloader downloader = new AndroidFileDownloader(device);
		if (!downloader.pullFileFromDevice(apkFileOnDevice + "/Base.apk", appFile)) {
			if (!downloader.pullFileFromDevice(apkFileOnDevice + "/base.apk", appFile)) {
				String d = apkFileOnDevice;
				if (d.endsWith("/"))
					d = d.substring(0, d.length() - 1);
				String[] dirName = d.split("/");
				String filename = apkFileOnDevice + "/" + dirName[dirName.length - 1] + ".apk";
				if (!downloader.pullFileFromDevice(filename, appFile)) {
					// If ART is disabled:
					if (!downloader.pullFileFromDevice(apkFileOnDevice, appFile)) {
						// We failed. Show the user the last error that has
						// occurred. The problem could have been with any of the
						// tries above, but we don't know
						String lastError = downloader.getLastError();
						if (lastError != null && !lastError.isEmpty()) {
							String sAPI = device.getProperty("ro.build.version.sdk");
							String additionalMsg = "";
							if (sAPI != null) {
								int api = MainUtils.tryParseInt(sAPI);
								if (api >= 23) {
									additionalMsg = "\nSince Android N, the device needs to be rooted in order to access some applications.\nPlease check whether su is installed.";
								}
							}
							Database.logError(this, new RuntimeException(String
									.format("Could not pull APK file from device: %s%s", lastError, additionalMsg)));
							return false;
						}
						Database.logError(this, new RuntimeException(
								String.format("Could not pull APK file from device: Unknown error")));
						return false;
					}
				}

			}
		}
		return true;

	}

	public void startActivity(Intent intent, boolean forceStopBefore, boolean waitForDebugger) {
		String cintent = intent.getCommandSyntax();
		executeThrowOutput(
				"am start-activity " + (forceStopBefore ? "-S " : "") + (waitForDebugger ? "-D " : "") + cintent);
	}

	public void startService(Intent intent) {
		String cintent = intent.getCommandSyntax();
		executeThrowOutput("am start-service " + cintent);
	}

	public void startForegroundService(Intent intent) {
		String cintent = intent.getCommandSyntax();
		executeThrowOutput("am start-foreground-service " + cintent);
	}

	public void stopService(Intent intent) {
		String cintent = intent.getCommandSyntax();
		executeThrowOutput("am stop-service " + cintent);
	}

	public void broadcast(Intent intent, String receiverPermission) {
		String cintent = intent.getCommandSyntax();
		executeThrowOutput("am start-foreground-service "
				+ (receiverPermission != null ? "--receiver-permission " + receiverPermission + " " : "") + cintent);
	}

	public void executeThrowOutput(String command) {
		String s = executeOnDevice(command);
		if (s.contains("Exception occurred while executing"))
			throw new APIExceptionWrapper(new APIException(400, s));
		if (s.contains("Error: "))
			throw new APIExceptionWrapper(new APIException(400, s));
		LOGGER.debug(command + "\n" + s);
	}

	@Override
	public void swipe(int x1, int y1, int x2, int y2, int duration) {
		executeInputOnDevice(new Input(String.format("input swipe %d %d %d %d %d", x1, y1, x2, y2, duration)));
	}

	@Override
	public void keyTyped(Key key) {
		try {
			int keyCode;
			switch (key) {
			case KEYCODE_0:
				keyCode = AndroidKeyCodes.KEYCODE_0;
				break;
			case KEYCODE_1:
				keyCode = AndroidKeyCodes.KEYCODE_1;
				break;
			case KEYCODE_2:
				keyCode = AndroidKeyCodes.KEYCODE_2;
				break;
			case KEYCODE_3:
				keyCode = AndroidKeyCodes.KEYCODE_3;
				break;
			case KEYCODE_4:
				keyCode = AndroidKeyCodes.KEYCODE_4;
				break;
			case KEYCODE_5:
				keyCode = AndroidKeyCodes.KEYCODE_5;
				break;
			case KEYCODE_6:
				keyCode = AndroidKeyCodes.KEYCODE_6;
				break;
			case KEYCODE_7:
				keyCode = AndroidKeyCodes.KEYCODE_7;
				break;
			case KEYCODE_8:
				keyCode = AndroidKeyCodes.KEYCODE_8;
				break;
			case KEYCODE_9:
				keyCode = AndroidKeyCodes.KEYCODE_9;
				break;
			case KEYCODE_A:
				keyCode = AndroidKeyCodes.KEYCODE_A;
				break;
			case KEYCODE_ALT_LEFT:
				keyCode = AndroidKeyCodes.KEYCODE_ALT_LEFT;
				break;
			case KEYCODE_ALT_RIGHT:
				keyCode = AndroidKeyCodes.KEYCODE_ALT_RIGHT;
				break;
			case KEYCODE_APOSTROPHE:
				keyCode = AndroidKeyCodes.KEYCODE_APOSTROPHE;
				break;
			case KEYCODE_AT:
				keyCode = AndroidKeyCodes.KEYCODE_AT;
				break;
			case KEYCODE_B:
				keyCode = AndroidKeyCodes.KEYCODE_B;
				break;
			case KEYCODE_BACK:
				keyCode = AndroidKeyCodes.KEYCODE_BACK;
				break;
			case KEYCODE_BACKSLASH:
				keyCode = AndroidKeyCodes.KEYCODE_BACKSLASH;
				break;
			case KEYCODE_C:
				keyCode = AndroidKeyCodes.KEYCODE_C;
				break;
			case KEYCODE_CALL:
				keyCode = AndroidKeyCodes.KEYCODE_CALL;
				break;
			case KEYCODE_CAMERA:
				keyCode = AndroidKeyCodes.KEYCODE_CAMERA;
				break;
			case KEYCODE_CLEAR:
				keyCode = AndroidKeyCodes.KEYCODE_CLEAR;
				break;
			case KEYCODE_COMMA:
				keyCode = AndroidKeyCodes.KEYCODE_COMMA;
				break;
			case KEYCODE_D:
				keyCode = AndroidKeyCodes.KEYCODE_D;
				break;
			case KEYCODE_DEL:
				keyCode = AndroidKeyCodes.KEYCODE_DEL;
				break;
			case KEYCODE_DPAD_CENTER:
				keyCode = AndroidKeyCodes.KEYCODE_DPAD_CENTER;
				break;
			case KEYCODE_DPAD_DOWN:
				keyCode = AndroidKeyCodes.KEYCODE_DPAD_DOWN;
				break;
			case KEYCODE_DPAD_LEFT:
				keyCode = AndroidKeyCodes.KEYCODE_DPAD_LEFT;
				break;
			case KEYCODE_DPAD_RIGHT:
				keyCode = AndroidKeyCodes.KEYCODE_DPAD_RIGHT;
				break;
			case KEYCODE_DPAD_UP:
				keyCode = AndroidKeyCodes.KEYCODE_DPAD_UP;
				break;
			case KEYCODE_E:
				keyCode = AndroidKeyCodes.KEYCODE_E;
				break;
			case KEYCODE_ENDCALL:
				keyCode = AndroidKeyCodes.KEYCODE_ENDCALL;
				break;
			case KEYCODE_ENTER:
				keyCode = AndroidKeyCodes.KEYCODE_ENTER;
				break;
			case KEYCODE_ENVELOPE:
				keyCode = AndroidKeyCodes.KEYCODE_ENVELOPE;
				break;
			case KEYCODE_EQUALS:
				keyCode = AndroidKeyCodes.KEYCODE_EQUALS;
				break;
			case KEYCODE_EXPLORER:
				keyCode = AndroidKeyCodes.KEYCODE_EXPLORER;
				break;
			case KEYCODE_F:
				keyCode = AndroidKeyCodes.KEYCODE_F;
				break;
			case KEYCODE_FOCUS:
				keyCode = AndroidKeyCodes.KEYCODE_FOCUS;
				break;
			case KEYCODE_G:
				keyCode = AndroidKeyCodes.KEYCODE_G;
				break;
			case KEYCODE_GRAVE:
				keyCode = AndroidKeyCodes.KEYCODE_GRAVE;
				break;
			case KEYCODE_H:
				keyCode = AndroidKeyCodes.KEYCODE_H;
				break;
			case KEYCODE_HEADSETHOOK:
				keyCode = AndroidKeyCodes.KEYCODE_HEADSETHOOK;
				break;
			case KEYCODE_HOME:
				keyCode = AndroidKeyCodes.KEYCODE_HOME;
				break;
			case KEYCODE_I:
				keyCode = AndroidKeyCodes.KEYCODE_I;
				break;
			case KEYCODE_J:
				keyCode = AndroidKeyCodes.KEYCODE_J;
				break;
			case KEYCODE_K:
				keyCode = AndroidKeyCodes.KEYCODE_K;
				break;
			case KEYCODE_L:
				keyCode = AndroidKeyCodes.KEYCODE_L;
				break;
			case KEYCODE_LEFT_BRACKET:
				keyCode = AndroidKeyCodes.KEYCODE_LEFT_BRACKET;
				break;
			case KEYCODE_M:
				keyCode = AndroidKeyCodes.KEYCODE_M;
				break;
			case KEYCODE_MENU:
				keyCode = AndroidKeyCodes.KEYCODE_MENU;
				break;
			case KEYCODE_MINUS:
				keyCode = AndroidKeyCodes.KEYCODE_MINUS;
				break;
			case KEYCODE_N:
				keyCode = AndroidKeyCodes.KEYCODE_N;
				break;
			case KEYCODE_NOTIFICATION:
				keyCode = AndroidKeyCodes.KEYCODE_NOTIFICATION;
				break;
			case KEYCODE_NUM:
				keyCode = AndroidKeyCodes.KEYCODE_NUM;
				break;
			case KEYCODE_O:
				keyCode = AndroidKeyCodes.KEYCODE_O;
				break;
			case KEYCODE_P:
				keyCode = AndroidKeyCodes.KEYCODE_P;
				break;
			case KEYCODE_PERIOD:
				keyCode = AndroidKeyCodes.KEYCODE_PERIOD;
				break;
			case KEYCODE_PLUS:
				keyCode = AndroidKeyCodes.KEYCODE_PLUS;
				break;
			case KEYCODE_POUND:
				keyCode = AndroidKeyCodes.KEYCODE_POUND;
				break;
			case KEYCODE_POWER:
				keyCode = AndroidKeyCodes.KEYCODE_POWER;
				break;
			case KEYCODE_Q:
				keyCode = AndroidKeyCodes.KEYCODE_Q;
				break;
			case KEYCODE_R:
				keyCode = AndroidKeyCodes.KEYCODE_R;
				break;
			case KEYCODE_RIGHT_BRACKET:
				keyCode = AndroidKeyCodes.KEYCODE_RIGHT_BRACKET;
				break;
			case KEYCODE_S:
				keyCode = AndroidKeyCodes.KEYCODE_S;
				break;
			case KEYCODE_SEARCH:
				keyCode = AndroidKeyCodes.KEYCODE_SEARCH;
				break;
			case KEYCODE_SEMICOLON:
				keyCode = AndroidKeyCodes.KEYCODE_SEMICOLON;
				break;
			case KEYCODE_SHIFT_LEFT:
				keyCode = AndroidKeyCodes.KEYCODE_SHIFT_LEFT;
				break;
			case KEYCODE_SHIFT_RIGHT:
				keyCode = AndroidKeyCodes.KEYCODE_SHIFT_RIGHT;
				break;
			case KEYCODE_SLASH:
				keyCode = AndroidKeyCodes.KEYCODE_SLASH;
				break;
			case KEYCODE_SOFT_RIGHT:
				keyCode = AndroidKeyCodes.KEYCODE_SOFT_RIGHT;
				break;
			case KEYCODE_SPACE:
				keyCode = AndroidKeyCodes.KEYCODE_SPACE;
				break;
			case KEYCODE_STAR:
				keyCode = AndroidKeyCodes.KEYCODE_STAR;
				break;
			case KEYCODE_SYM:
				keyCode = AndroidKeyCodes.KEYCODE_SYM;
				break;
			case KEYCODE_T:
				keyCode = AndroidKeyCodes.KEYCODE_T;
				break;
			case KEYCODE_TAB:
				keyCode = AndroidKeyCodes.KEYCODE_TAB;
				break;
			case KEYCODE_U:
				keyCode = AndroidKeyCodes.KEYCODE_U;
				break;
			case KEYCODE_V:
				keyCode = AndroidKeyCodes.KEYCODE_V;
				break;
			case KEYCODE_VOLUME_DOWN:
				keyCode = AndroidKeyCodes.KEYCODE_VOLUME_DOWN;
				break;
			case KEYCODE_VOLUME_UP:
				keyCode = AndroidKeyCodes.KEYCODE_VOLUME_UP;
				break;
			case KEYCODE_W:
				keyCode = AndroidKeyCodes.KEYCODE_W;
				break;
			case KEYCODE_X:
				keyCode = AndroidKeyCodes.KEYCODE_X;
				break;
			case KEYCODE_Y:
				keyCode = AndroidKeyCodes.KEYCODE_Y;
				break;
			case KEYCODE_Z:
				keyCode = AndroidKeyCodes.KEYCODE_Z;
				break;
			case KEYCODE_WAKEUP:
				keyCode = AndroidKeyCodes.KEYCODE_WAKEUP;
				break;
			default:
				throw new RuntimeException(String.format("Key %s not understood.", key));
			}
			synchronized (inputQueue) {
				InputKeys i = searchForInputQueueItem(InputKeys.class);
				if (i != null) {
					i.keys += " " + String.valueOf(keyCode);
					return;
				}
				executeInputOnDevice(new InputKeys(String.valueOf(keyCode)));
			}
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public IContactListing getContactListing() {
		return new AndroidContactListing(this);
	}

	@Override
	public IPortForwardingListing getPortFowardings() {
		return new AndroidPortForwardingListing(this);
	}

	@Override
	public ISniffing startSniffing() {

		return new AndroidSniffing(this);

	}

	
	
	/**
	 * A reference to the LogCat receiver Task. It is null until a logcat request is made. At that point, it is initialized and a thread is created. 
	 * This thread will run forever. LogCatListeners are removed when no longer needed.
	 */
	private LogCatReceiverTask logCatReceiver = null;
	
	@Override
	public LogBuffer getDeviceLog(String process) {
		if (logCatReceiver == null) {
			LOGGER.debug(String.format("registering logcat receiver for device %s ", getAndroidDevice()));
			logCatReceiver = new LogCatReceiverTask(getAndroidDevice());
			String threadName = String.format("LogCat listener for %s ", getAndroidDevice());
			new Thread(logCatReceiver, threadName).start();
		}
		LOGGER.debug(String.format("started streaming logcat messages for process %s on device %s", process, getAndroidDevice()));
		LogBuffer log = new LogBuffer();
		LogCatListener logrec = new LogCatListener() {
			
			@Override
			public void log(List<LogCatMessage> msgList) {
				for (LogCatMessage msg : msgList) {
					if (log.isClosed()) {
						logCatReceiver.removeLogCatListener(this);
						LOGGER.debug(String.format("stopped streaming logcat messages for process %s on device %s", process, getAndroidDevice()));
						return;
					}
					if (process == null || process.equals(msg.getHeader().getAppName())) {
						LogCatHeader header = msg.getHeader();
						String appn = header.getAppName();
						if (appn == null)
							appn = "UNKNOWN";
						String s = header.getTimestamp() + ": "
				                + header.getLogLevel().getPriorityLetter() + "/" + appn + "/"
				                + header.getTag() + " (" + header.getTid() + "/"
				                + header.getPid() + "): "
				                + msg.getMessage();
						log.writeMessage(s);
					}
				}
			}
		};
		log.setListener(logCatReceiver, logrec);
		logCatReceiver.addLogCatListener(logrec);
		return log;
	}

}
