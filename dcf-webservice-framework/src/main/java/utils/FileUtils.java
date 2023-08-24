package utils;

import java.io.File;

public class FileUtils {

	public static final String TEMP_FOLDER = "temp" + System.getProperty("file.separator");
	
	/**
	 * Create temporary directory if not present
	 * @param path
	 */
	public static void mkdir(String path) {
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdir();
	}
	
	public static File createTempFile(String filename, String format) {
		
		mkdir(TEMP_FOLDER);
		
		File file = new File(TEMP_FOLDER + filename + format);
		return file;
	}
}
