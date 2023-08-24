package config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General settings and start up checks
 * 
 * @author shahaal
 * @author avonva
 *
 */
public class GithubChecker {
	
	private static final Logger LOGGER = LogManager.getLogger(GithubChecker.class);

	public static final String TEMP_FOLDER_NAME = "temp";
	public static final String TEMP_FOLDER = TEMP_FOLDER_NAME + System.getProperty("file.separator");
	public static final String DEFAULT_VERSION = "0.0.0"; // default version used if no version is found

	/**
	 * Check if a directory exists. If not create it.
	 * 
	 * @param dirName
	 */
	public static void checkDir(String dirName) {

		// create temporary folder if needed
		File file = new File(dirName);

		LOGGER.info("Creating directory with name: " + dirName);

		// create the directory
		if (!file.exists()) {
			boolean created = file.mkdir();

			if (!created) {
				LOGGER.error("Cannot create temp directory");
				return;
			}
		}
	}

	public static void initialize() {
		checkDir(TEMP_FOLDER);

		GithubConfig r = new GithubConfig();
		checkDir(r.getApplicationFolder());
	}

	/**
	 * Delete all the temporary files
	 * 
	 * @throws IOException
	 */
	public static void clearTemp() throws IOException {
		File src = new File(TEMP_FOLDER);
		for (File file : src.listFiles())
			FileUtils.forceDelete(file);
		LOGGER.info("The temp files have been deleted successfully");
	}

	public static String newTempFile(String format) {
		return TEMP_FOLDER + System.getProperty("file.separator") + "temp_" + System.currentTimeMillis() + "." + format;
	}
}
