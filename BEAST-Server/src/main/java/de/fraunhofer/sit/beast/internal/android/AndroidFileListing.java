package de.fraunhofer.sit.beast.internal.android;

import com.android.ddmlib.IDevice;

import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;

public class AndroidFileListing implements IFileListing {

	private IDevice device;

	public AndroidFileListing(AndroidDevice androidDevice) {
		this.device = androidDevice.getAndroidDevice();
	}

	@Override
	public IFile getRoot() {
		return new AndroidFile(device, device.getFileListingService().getRoot());
	}

	@Override
	public IFile getFile(String path) {
		return new AndroidFile(device, path);
	}

	@Override
	public IFile getFile(String[] path) {
		return new AndroidFile(device, path);
	}

}
