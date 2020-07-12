package de.fraunhofer.sit.beast.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Reads a simple map from a config file.
 * 
 * @author Marc Miltenberger
 */
public final class ConfigBase {

	private static final Logger LOGGER = LogManager.getLogger(ConfigBase.class);

	private final static Map<String, String> properties = new HashMap<String, String>();

	public static Map<String, String> getProperties() {
		return properties;
	}

	static {
		try {
			loadFile("server.conf");

		} catch (Exception ex) {

		}
	}

	private ConfigBase() {
	}

	public static void loadFile(String file) {
		try {
			File f = new File(file);
			if (f.exists()) {
				LOGGER.info(String.format("Loading configuration from %s...", f.getCanonicalPath()));
				loadSettings(new FileReader(f));
			} else
				LOGGER.warn(String.format("No configuration file at %s", f.getCanonicalPath()));
		} catch (IOException ex) {
			// should never happen
		}
	}

	public static void loadFile(File file) {
		try {
			if (file.exists())
				loadSettings(new FileReader(file));
		} catch (FileNotFoundException ex) {
			// should never happen
		}
	}

	/**
	 * Loads the settings using the specified reader
	 * 
	 * @param reader the reader to use
	 */
	public static void loadSettings(Reader reader) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(reader);
			while (bf.ready()) {
				String s = bf.readLine();
				if (s == null)
					break;

				if (s.contains("#"))
					s = s.substring(0, s.indexOf("#"));
				if (s.trim().isEmpty() || !s.contains("="))
					continue;

				int pos = s.indexOf("=");
				String s1 = s.substring(0, pos).trim();
				String s2 = s.substring(pos + 1).trim();
				getProperties().put(s1, s2);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (bf != null)
				IOUtils.closeQuietly(bf);
			IOUtils.closeQuietly(reader);
		}
	}

	public static String getString(String key, boolean warnIfNotFound, String defaultValue) {
		String s = getProperties().get(key);
		if (s != null)
			return s;

		if (warnIfNotFound)
			LOGGER.warn(String.format("Key %s was not found in config file", key));
		return defaultValue;
	}

	public static String getString(String key, boolean warnIfNotFound) {
		return getString(key, warnIfNotFound, null);
	}

	public static String getString(String key) {
		return getString(key, true);
	}

	public static boolean hasKey(String key) {
		return getProperties().containsKey(key);
	}

	/**
	 * Returns the message integer
	 * 
	 * @param key the key
	 * @return the message integer
	 */
	public static int getInt(String key) {
		try {
			return Integer.parseInt(getString(key).trim());
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getInt(String key, int defaultValue) {
		return getInt(key, true, defaultValue);
	}

	/**
	 * Returns the message integer
	 * 
	 * @param key the key
	 * @return the message integer
	 */
	public static int getInt(String key, boolean warn, int defaultValue) {
		try {
			String s = getString(key, warn);
			if (s != null && !s.isEmpty())
				return Integer.parseInt(s.trim());
			else
				return defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static long getLong(String key, long defaultValue) {
		try {
			String s = getString(key);
			if (s != null && !s.isEmpty())
				return Long.parseLong(s.trim());
			else
				return defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		try {
			String s = getProperties().get(key);
			if (s == null)
				return defaultValue;
			s = s.trim();
			return s.equals("1") || s.toLowerCase().equals("true");
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static double getDouble(String key, double defaultValue, boolean warnIfNotFound) {
		try {
			String s = getString(key, warnIfNotFound);
			if (s != null && !s.isEmpty()) {
				Locale theLocale = Locale.US;
				NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
				return numberFormat.parse(s.trim()).doubleValue();
			} else
				return defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static double getDouble(String key, double defaultValue) {
		return getDouble(key, defaultValue, true);
	}

	public static float getFloat(String key, float defaultValue, boolean warnIfNotFound) {
		try {
			String s = getString(key, warnIfNotFound);
			if (s != null && !s.isEmpty()) {
				Locale theLocale = Locale.US;
				NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
				return numberFormat.parse(s.trim()).floatValue();
			} else
				return defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static float getFloat(String key, float defaultValue) {
		return getFloat(key, defaultValue, true);
	}

}
