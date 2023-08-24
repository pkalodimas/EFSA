package main;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.GithubConfig;
import config.ProxyConfig;
import dialog.Warning;
import release.ProxyConfigException;
import release.VersionManager;

public class FlowActions {
	private static final Logger LOGGER = LogManager.getLogger(FlowActions.class);

	private final VersionManager check;

	public FlowActions() {
		check = new VersionManager();
	}

	/**
	 * Start the update process
	 */
	public void start() {
		// if no application is found install the latest version
		// and launch it
		if (!check.isApplicationInstalled()) {
			boolean installed = installLatestVersion();
			if (installed)
				launchApp();
			return;
		}

		// if there is a working application
		boolean isOld = false;
		String current = null;
		String official = null;
		try {
			isOld = check.isOldVersion();
			if (isOld) {
				current = check.getCurrentAppVersion();
				official = check.getLatestOfficialVersion();
			}
		} catch (IOException e) {
			LOGGER.error("There is an error upon updating process of the application", e);
			e.printStackTrace();
			launchApp();
			return;
		}

		// if last version is already used
		if (!isOld) {
			launchApp();
			return;
		}

		// ask user for installing
		boolean installApp = askConfirmationForInstall(current, official);

		// if no is chosen, open standard app
		if (!installApp) {
			launchApp();
			return;
		}

		// otherwise install the latest version
		// and then launch it
		boolean installed = installLatestVersion();

		if (installed)
			launchApp();
	}

	/**
	 * Do you want to install the latest version?
	 * 
	 * @return
	 */
	private boolean askConfirmationForInstall(String current, String official) {
		// ask for installing the new version
		// otherwise ask for downloading it
		int val = Warning.askUser("Update needed!", "An update of the tool is available (" + current + " => " + official
				+ ")" + ". Do you want to download it?", JOptionPane.INFORMATION_MESSAGE | JOptionPane.YES_NO_OPTION);
		return val == JOptionPane.YES_OPTION;
	}

	/**
	 * Launch the application
	 */
	private void launchApp() {
		// check first if the proxy is inside the main app folder (only for win10
		// installer!)
		checkProxyFile();

		AppLauncherDelegate launcher = new AppLauncherDelegate();
		try {
			launcher.launchApp();
		} catch (InterruptedException | IOException e) {
			LOGGER.error("There are errors concerning launch", e);
			e.printStackTrace();

			Warning.warnUser("Error",
					"Cannot launch the application. Please contact zoonoses_support@efsa.europa.eu. Error message: "
							+ Warning.getStackTrace(e),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * the method check if the proxy is inside the main app folder otherwise copy it
	 * from the installer folder (only for win10 internal users!)
	 */
	private void checkProxyFile() {
		GithubConfig config = new GithubConfig();
		String proxyFile = GithubConfig.getProxyPath();
		String filename = config.getAppPath() + System.getProperty("file.separator") + proxyFile;
		File fileInApp = new File(filename);
		LOGGER.info("checking proxy file ... ");

		// create the directory
		if (!fileInApp.exists()) {
			try {
				FileUtils.copyFile(new File(proxyFile), fileInApp);
			} catch (IOException e) {
				LOGGER.error("Cannot copy proxy file!", e);
				e.printStackTrace();
			}
		}
	}

	private void showConnectionError() {
		ProxyConfig config = new ProxyConfig();
		LOGGER.warn("ERROR: Cannot download the latest version of the tool. Check your connection."
				+ "If you are using a custom proxy address, please check also if proxy hostname and port are correct in the proxy configuration file (\""
				+ config.getConfigPath() + ") ");

		Warning.warnUser("Error",
				"ERROR: Cannot download the latest version of the tool. Check your connection.\n"
						+ "If you are using a custom proxy address, please check also if "
						+ "proxy hostname and port are correct in the proxy configuration file ("
						+ config.getConfigPath() + ") ",
				JOptionPane.ERROR_MESSAGE);
	}

	private void showConfigurationError(String proxyConfig) {
		ProxyConfig config = new ProxyConfig();
		LOGGER.warn("ERROR: Invalid proxy hostname/port " + proxyConfig + ".Check the proxy configuration file (" + config.getConfigPath() + ")");
		
		Warning.warnUser(
				"Error", "ERROR: Invalid proxy hostname/port (" + proxyConfig
						+ "). Check the proxy configuration file (" + config.getConfigPath() + ").",
				JOptionPane.ERROR_MESSAGE);
	}

	private void showUnknownHostError(String hostname) {
		Warning.warnUser("Error", "ERROR: Unknown proxy hostname: " + hostname, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Install the latest version of the application
	 * 
	 * @return
	 */
	private boolean installLatestVersion() {
		try {
			check.updateVersion();
		} catch (IOException e) {
			LOGGER.error("There is an error upon checking version", e);
			e.printStackTrace();

			if (e instanceof UnknownHostException) {
				showUnknownHostError(e.getMessage());
			} else if (e instanceof ProxyConfigException) {
				showConfigurationError(e.getMessage());
			} else if (e instanceof ConnectException) {
				showConnectionError();
			} else {
				String trace = Warning.getStackTrace(e);

				Warning.warnUser("Generic error",
						"The update could not be downloaded. If you are on a corporate network, please try to connect to another network. If the issue persists ask for support in the relevant functional mailbox."
								+ trace,
						JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
		return true;
	}
}
