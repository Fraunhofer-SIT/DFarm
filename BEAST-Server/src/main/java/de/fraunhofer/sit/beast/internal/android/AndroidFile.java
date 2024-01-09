package de.fraunhofer.sit.beast.internal.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.google.common.base.Joiner;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.api.data.exceptions.FileNotFoundException;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import de.fraunhofer.sit.beast.internal.utils.TempUtils;

public class AndroidFile implements IFile {

	private IDevice device;
	private FileEntry entry;
	private String[] path;

	public AndroidFile(IDevice device, FileEntry fileEntry) {
		this.device = device;
		this.entry = fileEntry;
		this.path = entry.getPathSegments();
	}

	public AndroidFile(IDevice device, String path) {
		this.device = device;
		FileEntry entry = searchEntry(device, path);
		this.entry = entry;
	}

	public AndroidFile(IDevice device, String[] path) {
		this.device = device;
		FileEntry entry = searchEntry(device, path);
		this.path = path;
		this.entry = entry;
	}

	private FileEntry searchEntry(IDevice device, String path) {
		if (path.startsWith("/"))
			path = path.substring(1);
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		String[] split = path.split(FileListingService.FILE_SEPARATOR);
		if (path.isEmpty())
			split = new String[0];
		this.path = split;
		return searchEntry(device, split);
	}

	private FileEntry searchEntry(IDevice device, String[] path) {
		FileEntry root = device.getFileListingService().getRoot();
		FileEntry entry = root;
		if (path.length > 0) {
			for (String s : path) {
				FileEntry c = entry.findChild(s);
				if (c == null) {
					try {
						for (FileEntry i : device.getFileListingService().getChildrenSync(entry)) {
							if (i.getName().equals(s)) {
								c = i;
								break;
							}
						}
					} catch (Exception e) {
						throw AndroidUtils.translateAndroidException(e);
					}

				}
				entry = c;
				if (c == null)
					return null;
			}
		}
		return entry;
	}

	@Override
	public String getShortName() {
		if (path.length == 0)
			return "/";
		return path[path.length - 1];
	}

	@Override
	public boolean isDirectory() {
		if (entry == null)
			throw new APIExceptionWrapper(new FileNotFoundException(getFullPath()));
		return entry.isDirectory();
	}

	@Override
	public boolean isFile() {
		if (entry == null)
			throw new APIExceptionWrapper(new FileNotFoundException(getFullPath()));
		return !entry.isDirectory();
	}

	@Override
	public long getSize() {
		return entry.getSizeValue();
	}

	@Override
	public boolean exists() {
		return entry != null;
	}

	@Override
	public InputStream openRead() {
		File file = TempUtils.createFile();
		try {
			download(file);
			return new FileInputStream(file);
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public void download(File file) {
		try {
			device.pullFile(getFullPath(), file.getAbsolutePath());
		} catch (SyncException e) {
			throw new APIExceptionWrapper(new APIException(404, String.format("File not found: %s", path)));
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	public void createSymbolicLink(AndroidFile source) {
		executeShellCommandThrowOnOutput(
				"ln -s \"" + MainUtils.escapePath(source.path) + "\" \"" + MainUtils.escapePath(path) + "\"");
	}

	@Override
	public void upload(File file) {
		try {
			device.pushFile(file.getAbsolutePath(), getFullPath());
		} catch (SyncException e) {
			throw new APIExceptionWrapper(new APIException(404, String.format("File not found: %s", path)));
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public OutputStream openWrite() {
		File file = TempUtils.createFile();
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			final OutputStream os = outputStream;
			return new OutputStream() {
				@Override
				public void close() throws IOException {
					os.close();
					try {
						upload(file);
						file.delete();
					} catch (Exception e) {
						throw new RuntimeException(AndroidUtils.translateAndroidException(e));
					}
				}

				@Override
				public void write(int b) throws IOException {
					os.write(b);
				}

				@Override
				public void write(byte[] b) throws IOException {
					os.write(b);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					os.write(b, off, len);
				}
			};
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	protected void move(AndroidFile destFilename) {
		executeShellCommandThrowOnOutput(
				"mv \"" + MainUtils.escapePath(path) + "\" \"" + MainUtils.escapePath(destFilename.path) + "\"");
	}

	public String getFullPath() {
		return '/' + Joiner.on('/').join(path);
	}

	public synchronized AndroidFile getTempFileOnDevice() {
		AndroidFile f = new AndroidFile(device, new String[] { "data", "local", "tmp" });
		Set<String> ex = new HashSet<>();
		for (IFile fl : f.listFiles()) {
			ex.add(fl.getShortName());
		}
		while (true) {
			String file = "beast" + UUID.randomUUID().toString();
			if (!ex.contains(file)) {
				return new AndroidFile(device, new String[] { "data", "local", "tmp", file });
			}
		}
	}

	@Override
	public void deleteRecursively() {
		executeShellCommandThrowOnOutput("rm -rf \"" + MainUtils.escapePath(path) + "\"");
		entry = null;
	}

	public void executeShellCommandThrowOnOutput(String cmd) {
		CollectingOutputReceiver output = new CollectingOutputReceiver();
		try {
			device.executeShellCommand(cmd, output);
			String o = output.getOutput();
			if (!o.isEmpty())
				throw new APIExceptionWrapper(
						new APIException(500, o, String.format("Could not perform operation %s: %s", cmd, o)));
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public void mkdirs() {
		executeShellCommandThrowOnOutput("mkdir -p \"" + MainUtils.escapePath(path) + "\"");
		entry = searchEntry(device, path);
	}

	@Override
	public List<IFile> listFiles() {
		try {
			List<IFile> res = null;
			for (FileEntry i : device.getFileListingService().getChildrenSync(entry)) {
				if (res == null) {
					res = new ArrayList<>();
				}
				res.add(new AndroidFile(device, i));
			}
			if (res == null)
				return Collections.emptyList();
			else
				return res;
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public String toString() {
		return getFullPath();
	}

	@Override
	public String[] getPathParts() {
		return path;
	}

	public Date getLastModified() {
		if (entry == null)
			throw new APIExceptionWrapper(new FileNotFoundException(getFullPath()));
		try {
			SimpleDateFormat df;
			if (entry.getDate().contains(":"))
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			else
				df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date dt = df.parse(entry.getDate());
			if (dt.getTime() < 0)
				return null;
			return dt;
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public String getMD5Sum() {
		CollectingOutputReceiver output = new CollectingOutputReceiver();
		try {
			device.executeShellCommand("md5sum \"" + MainUtils.escapePath(path) + "\"", output);
			String md5 = output.getOutput().trim();
			if (md5.contains("No such file or directory"))
				throw new APIExceptionWrapper(new FileNotFoundException(getFullPath()));
			md5 = md5.substring(0, md5.indexOf(' '));
			return md5;
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public IFile getParent() {
		try {
			if (entry != null)
				return new AndroidFile(device, entry.getParent());
			if (path.length == 1)
				// Root
				return this;
			String[] parentPath = new String[path.length - 1];
			System.arraycopy(path, 0, parentPath, 0, parentPath.length);
			return new AndroidFile(device, parentPath);
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

}
