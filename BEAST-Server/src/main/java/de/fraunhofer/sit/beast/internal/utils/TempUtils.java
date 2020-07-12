package de.fraunhofer.sit.beast.internal.utils;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;

public class TempUtils {
	public static File TEMP_DIRECTORY = new File("Temp");
	private static final AtomicLong FILE_COUNTER = new AtomicLong();
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				clear();
			}
		}));
	}
	
	public static void clear() {
		File tmp = getTempDirectory();
		if (tmp.exists())
			FileUtils.deleteQuietly(tmp);
		tmp.mkdirs();
	}
	
	private static File getTempDirectory() {
		return new File(TEMP_DIRECTORY, "BEAST");
	}

	public static File createFile() {
		File m = new File(getTempDirectory(), String.valueOf(FILE_COUNTER.incrementAndGet()));
		m.getParentFile().mkdirs();
		return m;
	}
}
