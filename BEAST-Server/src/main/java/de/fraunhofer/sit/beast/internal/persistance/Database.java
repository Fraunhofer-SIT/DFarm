package de.fraunhofer.sit.beast.internal.persistance;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.table.TableUtils;

import de.fraunhofer.sit.beast.api.data.android.AndroidDeviceInformation;
import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.internal.Config;
import de.fraunhofer.sit.beast.internal.ConfigBase;

public class Database {
	private static final Logger LOGGER = LogManager.getLogger(Database.class);
	protected Dao<DBMetaInfo, ?> daoDBMetaInfo;

	public static Database INSTANCE;

	private static final int DB_SCHEMA_VERSION = 1;
	private CIJdbcConnectionSource connectionSource;
	private DBMetaInfo metadata;
	private Dao<de.fraunhofer.sit.beast.internal.persistance.Error, ?> daoError;
	private Dao<AndroidDeviceInformation, ?> daoAndroidDeviceInformation;
	private Object write = new Object();
	private Dao<SavedEnvironment, String> daoSavedEnvironment;

	static {
		DataPersisterManager.registerDataPersisters(new DeviceStatePersister());
	}

	private Database() throws Throwable {
		final String username = ConfigBase.getString("Database.Username", false, "");
		final String password = ConfigBase.getString("Database.Password", false, "");

		final DatabaseType type = new SqliteDatabaseType();
		String jdbcURL = ConfigBase.getString("Database.JDBCURL", false, "jdbc:sqlite:Server.db");

		connectionSource = new CIJdbcConnectionSource(jdbcURL, type);
		connectionSource.setUsername(username);
		connectionSource.setPassword(password);

		// Make sure that all tables that we need exist in the database
		createTables();

		createDaos();

	}

	public List<SavedEnvironment> getSavedEnvironments(int deviceID) throws SQLException {
		return daoSavedEnvironment.queryBuilder().where().eq("device", deviceID).query();
	}

	public SavedEnvironment getSavedEnvironmentUnsafe(int deviceID, String environment) throws SQLException {
		List<SavedEnvironment> c = daoSavedEnvironment.queryBuilder().where().eq("device", deviceID).and()
				.eq("name", environment).query();
		if (c == null || c.isEmpty())
			return null;
		return c.get(0);

	}

	public SavedEnvironment getSavedEnvironment(int deviceID, String environment) throws SQLException {
		SavedEnvironment c = getSavedEnvironmentUnsafe(deviceID, environment);
		if (c == null)
			throw new APIExceptionWrapper(new APIException(404,
					String.format("Environment %s not found for device %d", environment, deviceID)));
		return c;

	}

	public void putSavedEnvironment(SavedEnvironment s) throws SQLException {
		daoSavedEnvironment.createOrUpdate(s);
	}

	public void deleteSavedEnvironment(SavedEnvironment s) throws SQLException {
		daoSavedEnvironment.delete(s);
	}

	protected void createTables() throws SQLException {

		TableUtils.createTableIfNotExists(connectionSource, Error.class);
		daoError = DaoManager.createDao(connectionSource, Error.class);
		TableUtils.createTableIfNotExists(connectionSource, DBMetaInfo.class);
		TableUtils.createTableIfNotExists(connectionSource, AndroidDeviceInformation.class);
		TableUtils.createTableIfNotExists(connectionSource, SavedEnvironment.class);
	}

	public synchronized int getNewId() throws SQLException {
		metadata.lastID++;
		if (metadata.lastID > Config.getDeviceEndRange())
			throw new RuntimeException("No more ID left");
		updateDBMetaInfo();
		return metadata.lastID;
	}

	public void updateDBMetaInfo() throws SQLException {
		synchronized (write) {
			daoDBMetaInfo.update(metadata);
		}
	}

	protected void createDaos() throws SQLException {

		try {
			initAllDaos();
		} catch (SQLException e) {
			LOGGER.error("An error occurred while creating Daos", e);
			throw e;
		}

		try {
			daoDBMetaInfo.setObjectCache(true);
		} catch (SQLException e) {
			LOGGER.error("An error occurred while enabling the object caches", e);
			throw e;
		}

		try (CloseableIterator<DBMetaInfo> itMetadata = daoDBMetaInfo.closeableIterator()) {
			if (itMetadata.hasNext())
				metadata = itMetadata.next();
			else {
				metadata = new DBMetaInfo();
				metadata.lastID = Config.getDeviceStartRange() - 1;
				metadata.schemaVersion = DB_SCHEMA_VERSION;
				daoDBMetaInfo.create(metadata);
			}
		} catch (Exception e) {
			LOGGER.error("Error while creating meta data record", e);
		}
	}

	public void initAllDaos() throws SQLException {
		if (daoDBMetaInfo == null)
			daoDBMetaInfo = DaoManager.createDao(connectionSource, DBMetaInfo.class);
		daoAndroidDeviceInformation = DaoManager.createDao(connectionSource, AndroidDeviceInformation.class);
		daoSavedEnvironment = DaoManager.createDao(connectionSource, SavedEnvironment.class);
	}

	public CIJdbcConnectionSource getConnectionSource() {
		return connectionSource;
	}

	public AndroidDeviceInformation getAndroidDeviceInfo(String serialNumber) throws SQLException {
		List<AndroidDeviceInformation> l = daoAndroidDeviceInformation.queryForEq("serialNumber", serialNumber);
		if (l == null || l.isEmpty())
			return null;
		return l.get(0);
	}

	public static void logError(IDevice device, Throwable t) {
		if (device == null) {
			Error error = new Error();
			error.stackTrace = ExceptionUtils.getStackTrace(t);
			error.text = t.getMessage();
			LOGGER.error("An error occurred", t);
			Database.INSTANCE.addException(error);
			return;
		}
		String text = String.format("An error with device %s was logged",
				device.getSerialNumber() + " " + device.getName() + " - " + device.getState());
		LOGGER.error(text, t);
		Throwable t1 = new RuntimeException(text, t);
		Error error = new Error();
		error.stackTrace = ExceptionUtils.getStackTrace(t1);
		error.text = t1.getMessage();
		Database.INSTANCE.addException(error);
	}

	public static void logError(Throwable t) {
		String st = ExceptionUtils.getStackTrace(t);
		LOGGER.error("An error occurred: ", t);
		Error error = new Error();
		error.stackTrace = st;
		error.text = t.getMessage();
		Database.INSTANCE.addException(error);
	}

	public static void logError(String text) {
		LOGGER.error("An error occurred: " + text);
		Error error = new Error();
		error.stackTrace = null;
		error.text = text;
		Database.INSTANCE.addException(error);
	}

	public static void logError(de.fraunhofer.sit.beast.internal.interfaces.IDevice device, Throwable t) {
		String text = String.format("An error with device %s was logged", device.toString());
		LOGGER.error(text, t);
		Error error = new Error();
		error.stackTrace = ExceptionUtils.getStackTrace(t);
		error.text = t.getMessage();
		error.deviceInformationID = device.getDeviceInfo().ID;
		Database.INSTANCE.addException(error);
	}

	private void addException(Error error) {
		try {
			daoError.create(error);
			LOGGER.error(String.format("An error occurred: %s", error.stackTrace));
		} catch (SQLException e) {
			// Well... Too bad...
			if (!e.getMessage().contains("CONSTRAINT")
					&& (e.getCause() == null || !e.getCause().getMessage().contains("CONSTRAINT")))
				LOGGER.error("Persisiting the exception to database failed", e);
		}
	}

	public void insert(AndroidDeviceInformation s) throws SQLException {
		synchronized (write) {
			s.ID = getNewId();
			daoAndroidDeviceInformation.create(s);
		}
	}

	public static void initialize() throws Throwable {
		INSTANCE = new Database();
	}

	public void updateDevice(DeviceInformation deviceInfo) throws SQLException {
		if (deviceInfo == null)
			throw new IllegalArgumentException("Null device given");
		if (deviceInfo instanceof AndroidDeviceInformation) {
			daoAndroidDeviceInformation.update((AndroidDeviceInformation) deviceInfo);
		} else
			throw new RuntimeException(String.format("Unsupported type: %s", deviceInfo.getClass().getName()));

	}

}
