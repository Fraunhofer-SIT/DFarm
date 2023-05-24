package de.fraunhofer.sit.beast.internal.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;

public class MainUtils {
    private static final Pattern EscapePattern = Pattern.compile(
            "([\\\\()*+?\"'&#/\\s])");
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSSZ"; 
    
	public static int tryParseInt(String number) {
		int parsed;
		try {
			parsed = Integer.parseInt(number);
		} catch (Exception e) {
			parsed = 0;
		}
		return parsed;
	}

	public static String escapePath(String path) {
        return EscapePattern.matcher(path).replaceAll("\\\\$1"); 
	}

	public static String escapePath(String[] path) {
		StringBuilder b = new StringBuilder("/");
		for (String s : path) {
			b.append(escapePath(s)).append("/");
		}
		if (b.length() > 0)
			b.setLength(b.length() - 1);
		return b.toString();
	}

	public static String escapeCommand(String command) {
		return command;
	}
	
	
	private static String publicHostname;
	public static String getHostName(){
		return publicHostname;
	}

	public static void setPublicHostname(String localAddr) {
		publicHostname = localAddr;
	}

}
