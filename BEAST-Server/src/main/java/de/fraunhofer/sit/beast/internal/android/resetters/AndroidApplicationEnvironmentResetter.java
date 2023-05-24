package de.fraunhofer.sit.beast.internal.android.resetters;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import de.fraunhofer.sit.beast.internal.android.AndroidApp;
import de.fraunhofer.sit.beast.internal.android.AndroidDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.persistance.DBMetaInfo;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.Error;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;
import de.fraunhofer.sit.beast.internal.utils.IOUtils;

/**
 * Prepares the Android device so that only a fixed set of applications is installed.
 * @author Marc Miltenberger
 */
public class AndroidApplicationEnvironmentResetter implements IEnvironmentResetter {
	private static final Logger LOGGER = LogManager.getLogger(AndroidApplicationEnvironmentResetter.class);
	static File BASE_FILE = new File("Environments/Apps/");
	
	public static class AndroidApplicationResetInformation {
	
		@DatabaseField(index=true, foreign=true)
		public SavedEnvironment env;
	
		@DatabaseField
		public String packageName;
		
		@DatabaseField
		public int versionCode;

		public File getAppFile() {
			BASE_FILE.mkdirs();
			return new File(BASE_FILE, packageName + "_" + versionCode + ".apk");
		}

		public boolean isCompanionApp() {
			return packageName.equals(AndroidApp.COMPANION_APP_PACKAGE_NAME);
		}
	}
	private Dao<AndroidApplicationResetInformation, ?> daoResetInfo;
	
	public AndroidApplicationEnvironmentResetter(Database db) throws SQLException {
		daoResetInfo = DaoManager.createDao(db.getConnectionSource(), AndroidApplicationResetInformation.class);;
		TableUtils.createTableIfNotExists(db.getConnectionSource(), AndroidApplicationResetInformation.class);
	}

	@Override
	public void resetToKnownState(IDevice device, SavedEnvironment env) throws SQLException {


		if (device instanceof AndroidDevice) {
			AndroidDevice dev = (AndroidDevice) device;
			Map<String, AndroidApp> installed = dev.getInstalledApps();
			QueryBuilder<AndroidApplicationResetInformation, ?> db = daoResetInfo.queryBuilder();
			db.where().eq("env_id", env.id);
			CloseableIterator<AndroidApplicationResetInformation> it = db.iterator();
			try {
				while (it.hasNext()) {
					AndroidApplicationResetInformation a = it.next();
					AndroidApp installedVersion = installed.remove(a.packageName);
					if (a.isCompanionApp())
						continue;
					if (installedVersion != null) {
						if (installedVersion.versionCode == a.versionCode) {
							continue;
						} else {
							if (a.getAppFile().exists()) {
								LOGGER.info(String.format("Restoring %s: Uninstalling %s, since the installed version %d is different from the environment %d", env.name, a.packageName, installedVersion.versionCode, a.versionCode));
								dev.uninstall(a.packageName);
								dev.install(a.getAppFile());
							} else
								LOGGER.info(String.format("Restoring %s: No app on disk found for %s", env.name, a.packageName));
						}
					} else {
						LOGGER.info(String.format("Restoring %s: Reinstalling %s", env.name, a.packageName));
						if (a.getAppFile().exists())
							dev.install(a.getAppFile());
						else
							LOGGER.info(String.format("Restoring %s: No app on disk found for %s", env.name, a.packageName));
					}
				}
				for (AndroidApp i : installed.values()) {
					LOGGER.info(String.format("Restoring %s: Uninstalling %s", env.name, i.getPackageName()));
					dev.uninstall(i.getPackageName());
				}
				try {
					dev.uninstall(AndroidApp.COMPANION_APP_PACKAGE_NAME);
				} catch (Exception e) {
				}
			} finally {
				it.closeQuietly();
			}
			
		}		
	}

	@Override
	public void saveAsKnownState(IDevice device, SavedEnvironment env) throws SQLException {
		if (device instanceof AndroidDevice) {
			AndroidDevice dev = (AndroidDevice) device;
			for (AndroidApp app : dev.getInstalledApps().values()) {
				AndroidApplicationResetInformation ri = new AndroidApplicationResetInformation();
				ri.env = env;
				ri.packageName = app.getPackageName();
				ri.versionCode = app.versionCode;
				if (!ri.getAppFile().exists())
					dev.pullInstalledApp(app, ri.getAppFile());
				daoResetInfo.create(ri);
			}
		}
	}

	@Override
	public void deleteKnownState(SavedEnvironment env) throws SQLException {
		DeleteBuilder<AndroidApplicationResetInformation, ?> db = daoResetInfo.deleteBuilder();
		db.where().eq("env_id", env.id);
		db.delete();
	}


}
