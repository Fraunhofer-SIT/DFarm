package de.fraunhofer.sit.beast.internal.android;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.InstallException;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;

public class AndroidUtils {
	private static final Logger LOGGER = LogManager.getLogger(AndroidUtils.class);

	public static APIExceptionWrapper translateAndroidException(Throwable t) {
		if (t instanceof APIExceptionWrapper) {
			return (APIExceptionWrapper) t;
		}
		
		LOGGER.error("An exception occurred", t);
		if (t instanceof InstallException) {
			String msg = t.getMessage();
			if (msg == null) {
				if (t.getCause() != null)
					msg = ExceptionUtils.getMessage(t.getCause());
			}
			if (msg != null && msg.contains("INSTALL_FAILED_ALREADY_EXISTS"))
			{
				return new APIExceptionWrapper(new APIException(500, "App already installed", t.getMessage()));
			}
			if (msg == null)
				return new APIExceptionWrapper(new APIException(500, "An unknown error occurred: " + t.getMessage(), "An unknown error occurred: " + msg));
		}

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
