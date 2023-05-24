package de.fraunhofer.sit.beast.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOUtils {

	private static final Logger logger = LogManager.getLogger(IOUtils.class);

	// this data structure keeps track of files created in temp directory
	public static final Set<File> concurrentTempCleanupSet = Collections
			.newSetFromMap(new ConcurrentHashMap<File, Boolean>());

	static {
		// register cleanup thread
		Runtime.getRuntime().addShutdownHook(new CleanTempDirOnShutdownThread(IOUtils.concurrentTempCleanupSet));
	}

	public static void init() {
		// Just make sure that the class gets loaded and
		// the static class initializer gets executed
	}

	

	public static InputStream getResource(String source) throws FileNotFoundException {
		return getResource(source, IOUtils.class);
	}

	public static File getResourcePath(String source) throws IOException {
		return getResourcePath(source, IOUtils.class);
	}

	public static File getResourcePath(String source, Class<?> clazz) throws IOException {
		// Do we have a valid source file?
		if (source == null || source.isEmpty())
			return null;


		// Create a temp file. We preserve the extension in case the file processing
		// relies on it.
		String extension = ".tmp";
		{
			int idx = source.lastIndexOf(".");
			if (idx != -1) {
				extension = source.substring(idx);
			}
		}
		File f = File.createTempFile("BEAST", extension);
		concurrentTempCleanupSet.add(f);

		// The parent folder should exist, but better be safe than sorry
		f.getParentFile().mkdirs();

		// Write out the file
		try (InputStream inp = IOUtils.getResource(source, clazz)) {
			try (FileOutputStream output = new FileOutputStream(f)) {
				if (inp == null)
					throw new IllegalArgumentException(
							String.format("Could not find input %s in same jar as %s", source, clazz.getName()));
				org.apache.commons.io.IOUtils.copy(inp, output);
			}
		}

		return f;
	}

	public static InputStream getResource(String source, Class<?> clazz) throws FileNotFoundException {
		if (!source.startsWith("/"))
			source = "/" + source;

		InputStream str = clazz.getResourceAsStream(source);
		if (str != null)
			return str;

		CodeSource csource = clazz.getProtectionDomain().getCodeSource();
		if (csource == null)
			throw new RuntimeException("No code source for IOUtils");
		URL url = csource.getLocation();
		
		File projectDir = new File(url.getFile()).getParentFile();
		
		if (url.getPath().endsWith("/build/classes/"))
			projectDir = new File(url.getFile()).getParentFile().getParentFile();
		
		File f = new File(projectDir, source.substring(1));

		if (f.exists())
			return new FileInputStream(f);
		if (projectDir.getName().equals("target"))
			projectDir = projectDir.getParentFile();
		f = new File(projectDir, source.substring(1));
		if (f.exists())
			return new FileInputStream(f);
		else
			return null;
	}

	public static void deleteFilesAndEmptyDirectories(File rootDir, File[] files) {
		for (File file : files) {
			if (null == file) {
				continue;
			}

			try {
				File f = file;
				while (!f.equals(rootDir)) {
					if (f.exists() && f.canWrite()) {
						File[] containedFiles = f.listFiles();
						if (null == containedFiles || containedFiles.length == 0) {
							boolean deleted = f.delete();
							logger.info(String.format("Deletion of file or directory %s %s", f.getAbsolutePath(),
									(deleted ? "succeeded" : "failed")));

							if (deleted) {
								f = f.getParentFile();
							} else {
								break;
							}

						} else {
							break;
						}
					}
				}
			} catch (Exception e) {
				// log the exception an go on to the next file
				logger.warn(String.format("When trying to clean %s at file/directory %s got exception!",
						rootDir.getAbsolutePath(), file.getAbsolutePath()), e);
			}

		}
	}

	/**
	 * Extracts entries from a {@link ZipFile} in a specific directory to a path.
	 * Each entry path stays intact.
	 * 
	 * @param zipFile       the ZipFile
	 * @param pathPrefix    the path that should be extracted
	 * @param destDirectory the destination directory
	 * @throws IOException
	 */
	public static void extractDirectoryFromZip(ZipFile zipFile, String pathPrefix, File destDirectory)
			throws IOException {

		if (!destDirectory.exists()) {
			destDirectory.mkdir();
		}

		Enumeration<? extends ZipEntry> elements = zipFile.entries();
		String destinationDirectoryName = destDirectory.getCanonicalPath();
		while (elements.hasMoreElements()) {
			ZipEntry entry = elements.nextElement();
			if (entry.getName().startsWith(pathPrefix)) {
				if (!entry.isDirectory()) {
					try (InputStream is = zipFile.getInputStream(entry)) {
						File destinationFile = new File(destDirectory, entry.getName());
						if (!destinationFile.getCanonicalPath().startsWith(destinationDirectoryName)) {
							throw new IOException("Directory Traversal");
						}
						FileUtils.copyInputStreamToFile(is, destinationFile);
					}

				} else {
					File dir = new File(destDirectory, entry.getName());
					if (!dir.getCanonicalPath().startsWith(destinationDirectoryName)) {
						throw new IOException("Directory Traversal");
					}
					dir.mkdir();
				}
			}

		}

	}

	/**
	 * Extracts a single file entry from a {@link ZipFile}
	 * 
	 * @param zipFile
	 * @param filePath path to the entry to be extracted
	 * @param destFile where the entry should be extracted to
	 * @return true if the file exists, false if not
	 * @throws IOException
	 * 
	 */

	public static boolean extractFileFromZip(ZipFile zipFile, String filePath, File destFile) throws IOException {
		ZipEntry file = zipFile.getEntry(filePath);
		if (file == null)
			return false;
		extractEntryFromZip(zipFile, file, destFile);
		return true;
	}

	/**
	 * Extracts a single entry from a {@link ZipFile}
	 * 
	 * @param zipFile
	 * @param entry    path to the entry to be extracted
	 * @param destFile where the entry should be extracted to
	 * @throws IOException
	 */
	public static void extractEntryFromZip(ZipFile zipFile, ZipEntry entry, File destFile) throws IOException {
		FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), destFile);
	}

}
