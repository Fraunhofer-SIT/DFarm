package de.fraunhofer.sit.beast.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogUtils {

	private static Logger logger;
	private static File logConfigFile;
	private static boolean hasConfigured;

	public static File getUsedLogConfigFile() {
		return logConfigFile;
	}

	public static Logger getLogger() {
		if (logger == null)
			throw new RuntimeException("Product not set");
		return logger;
	}

	public static void initialize() {
		if (hasConfigured)
			return;
		hasConfigured = true;
		File propertiesFile = null;

		// Look for a system propertiy
		String propFile = System.getProperty("log4j.configuration");
		if (propFile != null && !propFile.isEmpty()) {
			propertiesFile = new File(propFile);
			if (!propertiesFile.exists())
				propertiesFile = null;
		}

		// If we have log4j configuration file in the current folder, we load it
		if (propertiesFile == null) {
			propertiesFile = new File("log4j.properties");
			if (!propertiesFile.exists())
				propertiesFile = null;
		}

		// Load the file
		InputStream inp = null;
		try {
			if (propertiesFile != null) {
				System.out.println(
						String.format("Loading logger configuration from %s...", propertiesFile.getCanonicalPath()));
				inp = new FileInputStream(propertiesFile);
				logConfigFile = propertiesFile;
			} else {
				// Try to load the file from our JAR
				URL url = LogUtils.class.getResource("/log4j.properties");
				if (url != null) {
					inp = url.openStream();
					System.out.println("Loading logger configuration from JAR file...");
				}
			}

			// Do we have a configuration file?
			if (inp != null) {
				Properties props = new Properties();
				props.load(inp);
				Object p = props.get("log4j.appender.logfile.File");
				if (p == null)
					System.out.println("No log file placeholders to configure.");
				else {
					String s = p.toString();
					s = s.replace("%X{DateTime}", new SimpleDateFormat("yyyy.MM.dd 'at' HH-mm-ss z")
							.format(new Date(System.currentTimeMillis())));
					props.setProperty("log4j.appender.logfile.File", s);
					System.out.println("Log file placeholders configured.");
				}
				PropertyConfigurator.configure(props);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (inp != null) {
				IOUtils.closeQuietly(inp);
			}
		}

		logger = Logger.getLogger("Generic");
		logger.info("Logging infrastructure initialized.");
	}

}
