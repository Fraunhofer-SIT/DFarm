

package de.fraunhofer.sit.beast.internal;

import java.io.File;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;


/**
 * Class for downloading files from Android devices
 * 
 * @author Marc Miltenberger
 * @author Steven Arzt
 *
 */
public class AndroidFileDownloader {

	private final IDevice device;
	private String lastError = "";

	public AndroidFileDownloader(IDevice device) {
		this.device = device;
	}

	/**
	 * Downloads a file from the device
	 * 
	 * @param path
	 *            The full path and file name of the file to download
	 * @param output
	 *            The path in which to store the downloaded file
	 * @return True if the operation succeeded, otherwise false
	 */
	public boolean pullFileFromDevice(String path, File output) {
		// We first try to pull the file normally.
		if (tryPull(path, output))
			return true;

		// If that doesn't work, we copy it away and try to pull from there
		if (pullHard(output, path))
			return true;

		// If that didn't work either, we're seriously out of luck
		return false;
	}

	/**
	 * Copies the remote file to a temporary location and then tries to pull it
	 * from there
	 * 
	 * @param tmpFile
	 *            The output file
	 * @param filename
	 *            The file on the device
	 * @return True if the operation succeeded, otherwise false
	 */
	private boolean pullHard(File tmpFile, String filename) {
		try {
			device.executeShellCommand("su 0 rm /data/local/tmp/pull.apk", new CollectingOutputReceiver());
			device.executeShellCommand("rm /data/local/tmp/pull.apk", new CollectingOutputReceiver());
			device.executeShellCommand("su 0 cp " + filename + " " + "/data/local/tmp/pull.apk",
					new CollectingOutputReceiver());
			device.executeShellCommand("su 0 chmod 777 /data/local/tmp/pull.apk", new CollectingOutputReceiver());
			device.pullFile("/data/local/tmp/pull.apk", tmpFile.getAbsolutePath());
			device.executeShellCommand("su 0 rm /data/local/tmp/pull.apk", new CollectingOutputReceiver());
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}

	/**
	 * Pulls a file from the device
	 * 
	 * @param tmpFile
	 *            The output file
	 * @param filename
	 *            The file on the device
	 * @return True if the operation succeeded, otherwise false
	 */
	private boolean tryPull(String path, File output) {
		try {
			device.pullFile(path, output.getAbsolutePath());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Gets the last error message that has occurred during a download
	 * 
	 * @return The last error message that has occurred during a download
	 */
	public String getLastError() {
		return lastError;
	}

}
