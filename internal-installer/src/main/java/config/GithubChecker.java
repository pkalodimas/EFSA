package config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * General settings and start up checks
 * @author shahaal
 * @author avonva
 *
 */
public class GithubChecker {
	
	private static final Logger LOGGER = LogManager.getLogger(GithubChecker.class);
	
	// template folder name
	public static final String TEMP_FOLDER_NAME = "temp";
	// default version used if no version is found
	public static final String DEFAULT_VERSION = "0.0.0";
	// temp folder path
	public static String TEMP_FOLDER_PATH = "";
	
	/**
	 * return the temp folder path in AppData
	 * @author shahaal
	 * @return
	 */
	public static void setTempFolder(String appPath) {
		TEMP_FOLDER_PATH = appPath + 
				System.getProperty("file.separator")+TEMP_FOLDER_NAME + 
				System.getProperty("file.separator");
	}
	
	/**
	 * Check if a directory exists. If not create it.
	 * @param dirName
	 */
	public static void checkDir(String dirName) {
		
		// create temporary folder if needed
		File file = new File(dirName);
		
		LOGGER.info("Creating " + dirName);
		
		// create the directory
		if (!file.exists()) {
			boolean created = file.mkdir();
			
			if (!created) {
				LOGGER.error("Cannot create "+dirName+" directory");
				return;
			}
		}
	}
	
	/**
	 * check the esistence of the app and the temp folder
	 * @author shahaal
	 */
	public static void initialize() {
		
		GithubConfig config = new GithubConfig();
		String appPath = config.getAppPath();
		setTempFolder(appPath);
		// check app and temp folder
		checkDir(appPath);
		checkDir(TEMP_FOLDER_PATH);
	}

	/**
	 * Delete all the temporary files
	 * @throws IOException
	 */
	public static void clearTemp() throws IOException {
		File src = new File(TEMP_FOLDER_PATH);
		for (File file : src.listFiles())
			FileUtils.forceDelete(file);
	}
	
	public static String newTempFile(String format) {
		return TEMP_FOLDER_PATH + "temp_" + System.currentTimeMillis() + "." + format;
	}
}
