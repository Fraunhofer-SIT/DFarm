package de.fraunhofer.sit.beast.internal.android.resetters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import de.fraunhofer.sit.beast.internal.android.AndroidDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;

/**
 * Prepares the Android device so that only a fixed set of files is available.
 * 
 * @author Marc Miltenberger
 */
public class AndroidFileSystemEnvironmentResetter implements IEnvironmentResetter {
	private static final Logger LOGGER = LogManager.getLogger(AndroidFileSystemEnvironmentResetter.class);
	static File BASE_FILE = new File("Environments/FS/");

	public static final String[] PATHES = new String[] { "/sdcard/", "/sdcard0/", "/sdcard1/", "/storage/sdcard/",
			"/storage/sdcard0/", "/storage/sdcard1/", "/storage/emulated/0/", "/storage/emulated/obb/", "/mnt/sdcard/",
			"/mnt/extSdCard/", "/mnt/ext_sd/", "/mnt/external/", "/media/sdcard/", "/data/local/tmp/" };

	public static class AndroidFSResetInformation {

		@DatabaseField(index = true, foreign = true)
		public SavedEnvironment env;

		@DatabaseField
		public String path;

		@DatabaseField
		public boolean isDirectory;

		@DatabaseField
		public int versionCode;

		@DatabaseField(index = true)
		public String hash;

		@DatabaseField
		public long size;

		public File getFile() {
			BASE_FILE.mkdirs();
			return new File(BASE_FILE, size + "_" + hash);
		}
	}

	private Dao<AndroidFSResetInformation, ?> daoResetInfo;

	public AndroidFileSystemEnvironmentResetter(Database db) throws SQLException {
		daoResetInfo = DaoManager.createDao(db.getConnectionSource(), AndroidFSResetInformation.class);
		;
		TableUtils.createTableIfNotExists(db.getConnectionSource(), AndroidFSResetInformation.class);
	}

	@Override
	public void resetToKnownState(IDevice device, SavedEnvironment env) throws SQLException, FileNotFoundException {

		if (device instanceof AndroidDevice) {
			AndroidDevice dev = (AndroidDevice) device;
			QueryBuilder<AndroidFSResetInformation, ?> db = daoResetInfo.queryBuilder();
			db.where().eq("env_id", env.id);
			CloseableIterator<AndroidFSResetInformation> it = db.iterator();
			Map<String, AndroidFSResetInformation> fileMap = new HashMap<>();
			try {
				while (it.hasNext()) {
					AndroidFSResetInformation a = it.next();
					fileMap.put(a.path, a);
				}
				final IFileListing listing = dev.getFileListing();
				for (String path : PATHES) {
					IFile file = listing.getFile(path);
					if (file.exists()) {
						restoreFirstStage(file, fileMap);
					}
				}
				for (AndroidFSResetInformation i : fileMap.values()) {
					LOGGER.info(String.format("Restoring %s: Restore %s", env.name, i.path));
					IFile f = listing.getFile(i.path);
					f.getParent().mkdirs();
					try {
						f.upload(i.getFile());
					} catch (Exception e) {
						LOGGER.error("Could not upload" + i.getFile(), e);
					}
				}
			} finally {
				it.closeQuietly();
			}

		}
	}

	private void restoreFirstStage(IFile file, Map<String, AndroidFSResetInformation> fileMap)
			throws FileNotFoundException {
		for (IFile d : file.listFiles()) {
			AndroidFSResetInformation m = fileMap.remove(d.getFullPath());
			if (m == null)
				m = fileMap.remove(d.getFullPath() + "/");
			if (m == null) {
				LOGGER.info(String.format("Restoring: Delete %s", d.getFullPath()));
				try {
					d.deleteRecursively();
				} catch (Exception e) {
					LOGGER.error("Could not delete " + d, e);
				}
				continue;
			} else {
				if (!m.isDirectory) {
					if (!d.isFile()) {
						try {
							d.deleteRecursively();
						} catch (Exception e) {
							LOGGER.error("Could not delete " + d, e);
						}
					}
					if (d.exists()) {
						String md5sum = d.getMD5Sum();
						if (d.getSize() != m.size || !m.hash.equals(md5sum)) {
							LOGGER.info(String.format("Restoring %s: Restore %s (%s, %d) with (%s, %d)", m.env.name,
									m.path, md5sum, d.getSize(), m.hash, m.size));
							try {
								d.upload(m.getFile());
							} catch (Exception e) {
								LOGGER.error("Could not upload " + d, e);
							}
						}
					}
				}
			}
			if (d.isDirectory())
				restoreFirstStage(d, fileMap);
		}

	}

	@Override
	public void saveAsKnownState(IDevice device, SavedEnvironment env) throws SQLException, IOException {
		if (device instanceof AndroidDevice) {
			final AndroidDevice dev = (AndroidDevice) device;
			final IFileListing listing = dev.getFileListing();
			for (String path : PATHES) {
				IFile file = listing.getFile(path);
				if (file.exists()) {
					save(env, file);
				}
			}
		}
	}

	private void save(SavedEnvironment env, IFile file) throws SQLException, IOException {
		for (IFile d : file.listFiles()) {
			try {
				AndroidFSResetInformation ri = new AndroidFSResetInformation();
				ri.env = env;
				ri.isDirectory = d.isDirectory();
				if (d.isFile()) {
					ri.hash = d.getMD5Sum();
					ri.size = d.getSize();
					File onDisk = ri.getFile();
					if (!onDisk.exists()) {
						File tmp = new File(onDisk.getAbsolutePath() + "-tmp");
						d.download(tmp);
						FileUtils.moveFile(tmp, onDisk);
					}
				}
				ri.path = d.getFullPath();
				daoResetInfo.create(ri);
			} catch (FileNotFoundException e) {
				LOGGER.warn(String.format("Ignoring: File %s not exist while saving environment", d.getFullPath()), e);
			}
			if (d.isDirectory())
				save(env, d);
		}
	}

	@Override
	public void deleteKnownState(SavedEnvironment env) throws SQLException {
		DeleteBuilder<AndroidFSResetInformation, ?> db = daoResetInfo.deleteBuilder();
		db.where().eq("env_id", env.id);
		db.delete();
	}

}
