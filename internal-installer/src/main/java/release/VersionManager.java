package release;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.GithubConfig;
import config.GithubChecker;
import dialog.ProgressDialog;
import version_manager.VersionComparator;
import zip_manager.ZipManager;

/**
 * Manage the application versions and updates.
 * @author avonva
 * @author shahaal
 *
 */
public class VersionManager {
	
	private static final Logger LOGGER = LogManager.getLogger(VersionManager.class);

	private final static String INSTALL_OK_FILE = "\\install.ok";
	private String lastRelease;
	private ReleaseParser parser;

	/**
	 * Download the last release of the application from github
	 * @return
	 * @throws IOException
	 */
	public String getLastRelease() throws IOException {
		
		if (lastRelease == null) {
			
			GithubConfig config = new GithubConfig();
			
			String username = config.getValue(GithubConfig.REPOSITORY_OWNER);
			String repo = config.getValue(GithubConfig.REPOSITORY_NAME);

			GetLastRelease req = new GetLastRelease(username, repo);
			lastRelease = req.get();
		}
		
		return lastRelease;
	}
	
	/**
	 * Get an instance of the release parser to get the release
	 * information
	 * @return
	 * @throws IOException
	 */
	public ReleaseParser getParserInstance() throws IOException {
		
		if (parser == null) {
			
			String lastRelease = getLastRelease();
	
			// check if user has last version
			this.parser = new ReleaseParser(lastRelease);
		}
		
		return parser;
	}
	
	/**
	 * Check if a correct installation of the application is present
	 * into the application folder
	 * @return
	 */
	public boolean isApplicationInstalled() {
		File file = getInstallOkFile();
		return file.exists();
	}
	
	private File getInstallOkFile() {
		GithubConfig config = new GithubConfig();
		String folder = config.getAppPath();
		String filePath = folder + INSTALL_OK_FILE;
		File file = new File(filePath);
		return file;
	}
	
	/**
	 * Get the version of the application which is already installed
	 * in the user machine
	 * @return
	 */
	public String getCurrentAppVersion() {
		GithubConfig config = new GithubConfig();
		String currentVersion = config.getApplicationVersion();
		LOGGER.info("The current installed version is " + currentVersion);
		return currentVersion;
	}
	
	/**
	 * Get the latest released version published on github
	 * @return
	 * @throws IOException
	 */
	public String getLatestOfficialVersion() throws IOException {

		ReleaseParser parser = getParserInstance();

		String latestVersion = parser.getVersion();
		LOGGER.info("The latest released version published on github is " + latestVersion);
		return latestVersion;
	}
	
	/**
	 * Check if the user has the latest version or not
	 * @return
	 * @throws IOException
	 */
	public boolean isOldVersion() throws IOException {

		String currentVersion = getCurrentAppVersion();
		String latestVersion = getLatestOfficialVersion();
		
		VersionComparator versionComparator = new VersionComparator();
		int compare = versionComparator.compare(currentVersion, latestVersion);
		
		if (compare == -1) {
			LOGGER.info("The version needs update from " + currentVersion 
					+ " => " + latestVersion);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Download the last release
	 * @return filename of the file containing the last release
	 * @throws IOException
	 */
	private String downloadLastRelease() throws IOException {
		
		ReleaseParser parser = getParserInstance();
		String endpoint = parser.getDownloadUrl();
		int contentLength = parser.getSize();
		
		
		// outputfile in temp folder
		String attachmentFilename = GithubChecker.newTempFile("zip");
		
		// start the download process in background
		try(FileDownloader downloader = new FileDownloader(endpoint, 
				contentLength, attachmentFilename)) {
			
			ProgressDialog dialog = new ProgressDialog();
			
			downloader.setProgressBar(dialog.getProgressBar());
			
			// close the dialog at the end
			downloader.setSwingWorkerFinishedListener(new SwingWorkerFinishedListener() {
				
				@Override
				public void finished() {
					dialog.close();
				}
			});
			
			downloader.execute();
			
			while(!downloader.isFinished()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		LOGGER.info("The filename of the file containing the last release: " + attachmentFilename);
		return attachmentFilename;
	}
	
	/**
	 * Download last release and replace the old with the new
	 * @throws IOException
	 */
	public void updateVersion() throws IOException {
		
		// download zip of the release
		String newVersionFilename = this.downloadLastRelease();
		
		String unzippedFolder = newVersionFilename + "_unzipped";
		
		// extract the zip file
		ZipManager.extractFolder(newVersionFilename, unzippedFolder);
		
		// move all the old version files into the oldReleases folder
		OldVersionTracker tracker = new OldVersionTracker();
		tracker.moveOldFiles();
		tracker.moveNewFiles(unzippedFolder);
		
		createInstallOkFile();
		
		LOGGER.info("The update of the version was successful.");
		
	}
	
	/**
	 * Create a new ok file meaning that the installation
	 * was successful
	 * @throws IOException
	 */
	public void createInstallOkFile() throws IOException {
		File file = getInstallOkFile();
		file.createNewFile();
	}
}
