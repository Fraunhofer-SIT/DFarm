package de.fraunhofer.sit.beast.internal.utils;

import java.io.File;
import java.util.Set;

/**
 * The purpose of this thread is to remove stuff in the java.io.tempdir when the
 * vm shuts down.
 * 
 */
public class CleanTempDirOnShutdownThread extends Thread {

	private final Set<File> concurrentTempCleanupSet;

	public CleanTempDirOnShutdownThread(Set<File> concurrentTempCleanupSet) {
		this.concurrentTempCleanupSet = concurrentTempCleanupSet;
	}

	@Override
	public void run() {
		File[] files = concurrentTempCleanupSet.toArray(new File[concurrentTempCleanupSet.size()]);
		final File rootDir = new File(System.getProperty("java.io.tmpdir"));
		IOUtils.deleteFilesAndEmptyDirectories(rootDir, files);
	}

}
