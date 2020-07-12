package de.fraunhofer.sit.beast.internal.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.logging.Message;

import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.internal.ConfigBase;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;
import de.fraunhofer.sit.beast.internal.interfaces.ISniffing;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class AndroidSniffing implements ISniffing {

	private AndroidDevice device;
	private static final Logger LOGGER = Logger.getLogger(AndroidSniffing.class);

	public AndroidSniffing(AndroidDevice device) {
		this.device = device;
	}


	@Override
	public void startSniffing(int timeout) throws Exception {
		IFileListing fl = this.device.getFileListing();
		IFile certFile = fl.getFile(MessageFormat.format("/sdcard/ovpn/{0}.ovpn", this.device.getDeviceInfo().ID));
		
		
		if (this.device.getInstalledAppUnsafe("de.blinkt.openvpn") != null)
			//App is installed
			return;
		
		installOvpn();
		
		
		if(!certFile.exists()) {
			createCert();
		}
		
		try {
			this.device.executeOnDevice(
					"am start-activity -a android.intent.action.MAIN -e de.blinkt.openvpn.api.profileName test1 de.blinkt.openvpn/.api.ConnectVPN");
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while initializing the OpenVPN profile", e);
		}

	}

	private void installOvpn() throws IOException {
		File apk = de.fraunhofer.sit.beast.internal.utils.IOUtils.getResourcePath("/res/android/ovpnClient.apk");
		this.device.install(apk);
	}

	private void createCert() {
		final String masterServerIp = ConfigBase.getProperties().get("MasterServer.IP");
		final String masterServerPort = ConfigBase.getProperties().get("MasterServer.Port");
		IFileListing fl = this.device.getFileListing();
		IFile file = fl.getFile(MessageFormat.format("/sdcard/ovpn/{0}.ovpn", this.device.getDeviceInfo().ID));
		try {
			URL url = new URL(MessageFormat.format("http://{0}:{1}/createCert?={2}", masterServerIp, masterServerPort, this.device.getDeviceInfo().ID));	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			File tmpfile = File.createTempFile("tmp", ".cer");
			try (FileOutputStream out = new FileOutputStream(tmpfile)) {
				IOUtils.copy(con.getInputStream(), out);
			}
			file.upload(tmpfile);
			// Upload tmpfile to device
			
		} catch (IOException e) {
			LOGGER.error("error while creating cert-file", e);
		}

		
	}

	// Stop tcpdump and the vpn-connection manually
	@Override
	public void stopSniffing() {
		String disconnect = "am start-activity -a android.intent.action.MAIN de.blinkt.openvpn/.api.DisconnectVPN";
		this.device.executeOnDevice(disconnect);

	}

	// Get the TunInterface for this Object's Device.
	public String getTun() {
		return "";
	}

}
