package de.fraunhofer.sit.beast.internal.persistance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * A connection source that can set custom pragmas for the backend database
 * 
 * @author Steven Arzt
 *
 */
public class CIJdbcConnectionSource extends JdbcConnectionSource {

	private static final int BUSY_TIMEOUT = 20000;

	public static final String SQLITE_PRAGMA_OPEN_MODE = "open_mode";
	public static final String SQLITE_PRAGMA_BUSY_TIMEOUT = "busy_timeout";
	public static final String SQLITE_PRAGMA_SHARED_CACHE = "shared_cache";

	public static final int SQLITE_OPEN_MODE_READONLY = 0x00000001;
	public static final int SQLITE_OPEN_MODE_READWRITE = 0x00000002;
	public static final int SQLITE_OPEN_MODE_CREATE = 0x00000004;
	public static final int SQLITE_OPEN_MODE_DELETEONCLOSE = 0x00000008;
	public static final int SQLITE_OPEN_MODE_EXCLUSIVE = 0x00000010;
	public static final int SQLITE_OPEN_MODE_AUTOPROXY = 0x00000020;
	public static final int SQLITE_OPEN_MODE_URI = 0x00000040;
	public static final int SQLITE_OPEN_MODE_MEMORY = 0x00000080;
	public static final int SQLITE_OPEN_MODE_MAIN_DB = 0x00000100;
	public static final int SQLITE_OPEN_MODE_TEMP_DB = 0x00000200;
	public static final int SQLITE_OPEN_MODE_TRANSIENT_DB = 0x00000400;
	public static final int SQLITE_OPEN_MODE_MAIN_JOURNAL = 0x00000800;
	public static final int SQLITE_OPEN_MODE_TEMP_JOURNAL = 0x00001000;
	public static final int SQLITE_OPEN_MODE_SUBJOURNAL = 0x00002000;
	public static final int SQLITE_OPEN_MODE_MASTER_JOURNAL = 0x00004000;
	public static final int SQLITE_OPEN_MODE_NOMUTEX = 0x00008000;
	public static final int SQLITE_OPEN_MODE_FULLMUTEX = 0x00010000;
	public static final int SQLITE_OPEN_MODE_SHAREDCACHE = 0x00020000;
	public static final int SQLITE_OPEN_MODE_PRIVATECACHE = 0x00040000;
	public static final int SQLITE_OPEN_MODE_WAL = 0x00080000;

	protected String username;
	protected String password;

	public CIJdbcConnectionSource(String connectionString, DatabaseType type) throws SQLException {
		super(connectionString, type);
	}

	@Override
	protected DatabaseConnection makeConnection(Logger logger) throws SQLException {
		Properties properties = new Properties();
		if (!username.isEmpty())
			properties.setProperty("user", username);

		if (!password.isEmpty())
			properties.setProperty("password", password);

		// Fix multi-threading
		if (databaseType instanceof SqliteDatabaseType) {
			properties.setProperty(SQLITE_PRAGMA_OPEN_MODE, Integer.toString(SQLITE_OPEN_MODE_FULLMUTEX
					| SQLITE_OPEN_MODE_CREATE | SQLITE_OPEN_MODE_READWRITE | SQLITE_OPEN_MODE_URI));
			properties.setProperty(SQLITE_PRAGMA_BUSY_TIMEOUT, Integer.toString(BUSY_TIMEOUT));
			properties.setProperty(SQLITE_PRAGMA_SHARED_CACHE, "false");
		}

		Connection conn = DriverManager.getConnection(getUrl(), properties);

		if (databaseType instanceof SqliteDatabaseType) {
			try (Statement stat = conn.createStatement()) {
				stat.execute("PRAGMA busy_timeout=" + Integer.toString(BUSY_TIMEOUT));
				stat.execute("PRAGMA synchronous=NORMAL");
				stat.execute("PRAGMA journal_mode=WAL");
				stat.execute("PRAGMA locking_mode=EXCLUSIVE");
			}
		}
		DatabaseConnection connection = new com.j256.ormlite.jdbc.JdbcDatabaseConnection(conn);

		connection.setAutoCommit(true);
		return connection;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
		super.setUsername(username);
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
		super.setPassword(password);
	}

}
