package de.fraunhofer.sit.beast.BEASTServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.LogUtils;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;

public class AndroidDeviceTests {

	static {
		LogUtils.initialize();
	}

	@BeforeClass
	public static void init() {
		while (DeviceManager.DEVICE_MANAGER.getDevices(null).isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			System.out.println("Waiting for devices...");
		}
	}

	@Test
	public void testApp() throws Exception, Throwable {
		for (IDevice d : DeviceManager.DEVICE_MANAGER.getDevices(null)) {
			d.getInstalledApps();
			//Now load from cache
			d.getInstalledApps();
			AbstractApp app = d.getInstalledApp("com.android.musicfx");
			d.ping();
			AbstractApp app2 = d.getInstalledApp(app.id);
			d.install(getTestApp("com.gueei.applocker_3.apk"));
			System.out.println(app);
		}

	}

	private File getTestApp(String s) throws FileNotFoundException {
		File f = new File("../Apps/" + s);
		if (f.exists())
			return f;
		f = new File("Apps/" + s);
		if (!f.exists())
			throw new FileNotFoundException(f.getAbsolutePath());
		return f;
	}
}
