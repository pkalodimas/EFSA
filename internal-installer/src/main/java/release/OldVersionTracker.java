package release;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.GithubChecker;
import config.GithubConfig;

public class OldVersionTracker {
	
	private static final Logger LOGGER = LogManager.getLogger(OldVersionTracker.class);

	private GithubConfig config;
	private String appPath;

	public OldVersionTracker() {
		this.config = new GithubConfig();
		this.appPath = config.getAppPath();
	}

	/**
	 * Move the old files of the application into the old folder
	 * 
	 * @throws IOException
	 */
	public void moveOldFiles() throws IOException {
		
		File app = new File(this.appPath);

		if (!app.exists()) {
			LOGGER.error("The application folder " + this.appPath + " does not exist");
			throw new IOException("The application folder " + this.appPath + " does not exist");
		}	

		String dbFolder = config.getValue(GithubConfig.APP_DB_FOLDER);
		String tempFolder = GithubChecker.TEMP_FOLDER_NAME;

		// for each file of the app folder
		for (File file : app.listFiles()) {

			String filename = file.getName();

			// skip the database/temp/jre folders and github jar
			if (filename.equalsIgnoreCase(dbFolder) || filename.equals(tempFolder))
				continue;

			// move file into the old folder
			deleteFile(file);
		}
	}

	/**
	 * Move the files of the new version into the app folder
	 * 
	 * @param newFilesFolder
	 * @throws IOException
	 */
	public void moveNewFiles(String newFilesFolder) throws IOException {

		LOGGER.info("Moving the files of the new version..");
		
		File folder = new File(newFilesFolder);

		// if single directory, set as root the children folder
		if (folder.listFiles().length == 1 && folder.listFiles()[0].isDirectory()) {
			folder = folder.listFiles()[0];
		}

		// move the new files into the app folder
		for (File file : folder.listFiles()) {
			moveFile(file, this.appPath);
		}

		// clear the temporary files
		GithubChecker.clearTemp();
	}

	/**
	 * Delete a file or a directory
	 * 
	 * @param src
	 * @throws IOException
	 */
	private void deleteFile(File src) throws IOException {

		if (src.isDirectory()) {
			FileUtils.deleteDirectory(src);
		} else {
			FileUtils.forceDelete(src);
		}
	}

	/**
	 * Move a file into another location
	 * 
	 * @param file
	 * @param location
	 * @throws IOException
	 */
	private void moveFile(File src, String location) {

		String target = location + System.getProperty("file.separator") + src.getName();
		File dest = new File(target);

		LOGGER.info("Moving " + src + " to " + target);

		try {

			// if we have a directory
			if (src.isDirectory()) {

				// if it already exists overwrite it
				if (dest.exists()) {
					FileUtils.deleteDirectory(dest);
				}

				// move the directory
				FileUtils.moveDirectory(src, dest);
			} else {

				// if file and exists, overwrite it
				if (dest.exists()) {
					FileUtils.forceDelete(dest);
				}

				// move file
				FileUtils.moveFile(src, dest);
			}
		} catch (IOException e) {
			LOGGER.error("An error occurred upon move of file", e);
			e.printStackTrace();
		}
	}
}
