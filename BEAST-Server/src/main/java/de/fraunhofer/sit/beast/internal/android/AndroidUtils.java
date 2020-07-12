package de.fraunhofer.sit.beast.internal.android;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.android.ddmlib.InstallException;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;

public class AndroidUtils {
	private static final Logger LOGGER = LogManager.getLogger(AndroidUtils.class);

	public static APIExceptionWrapper translateAndroidException(Throwable t) {
		if (t instanceof APIExceptionWrapper) {
			return (APIExceptionWrapper) t;
		}
		
		if (t instanceof InstallException) {
			String msg = t.getMessage();
			if (msg.contains("INSTALL_FAILED_ALREADY_EXISTS"))
			{
				return new APIExceptionWrapper(new APIException(500, "App already installed", t.getMessage()));
				
			}
			System.out.println();
		}
		LOGGER.error("An exception occurred", t);

		return new APIExceptionWrapper(new APIException(500, t.getMessage(), t.getMessage()));
	}

	public static String getBinding(String data, String value) {
		if (value == null)
			return "";
		
		return " --bind " + data + ":s:'" + value + "'";
	}

	public static String getBinding(String data, int value) {
		return " --bind " + data + ":i:" + value;
	}

}
