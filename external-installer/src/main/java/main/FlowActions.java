package main;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.ProxyConfig;
import dialog.Warning;
import release.ProxyConfigException;
import release.VersionManager;

public class FlowActions {
	
	private static final Logger LOGGER = LogManager.getLogger(FlowActions.class);

	private VersionManager check;
	
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
			LOGGER.error("Error in the version update process", e);
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
	 * @return
	 */
	private boolean askConfirmationForInstall(String current, String official) {
		
		// ask for installing the new version
		// otherwise ask for downloading it
		int val = Warning.askUser("Update needed!", 
				"An update of the tool is available ("
						+ current + " => " + official + ")" + ". Do you want to download it?", 
				JOptionPane.INFORMATION_MESSAGE | JOptionPane.YES_NO_OPTION);
		
		return val == JOptionPane.YES_OPTION;
	}
	
	/**
	 * Launch the application
	 */
	private void launchApp() {
		
		AppLauncherDelegate launcher = new AppLauncherDelegate();
		try {
			
			launcher.launchApp();
			
		} catch (InterruptedException | IOException e) {
            LOGGER.error("Error in launching app ", e);
			e.printStackTrace();
			
			Warning.warnUser("Error", 
					"Cannot launch the application. Please contact zoonoses_support@efsa.europa.eu. Error message: " 
							+ Warning.getStackTrace(e), 
							JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void showConnectionError() {
		
		ProxyConfig config = new ProxyConfig();
		
		Warning.warnUser("Error", 
				"ERROR: Cannot download the latest version of the tool. Check your connection.\n"
				+ "If you are using a custom proxy address, please check also if "
				+ "proxy hostname and port are correct in the proxy configuration file (" 
						+ config.getConfigPath() + ") ",
						JOptionPane.ERROR_MESSAGE);
	}
	
	private void showConfigurationError(String proxyConfig) {
		
		ProxyConfig config = new ProxyConfig();
		
		Warning.warnUser("Error", 
				"ERROR: Invalid proxy hostname/port (" 
						+ proxyConfig 
						+ "). Check the proxy configuration file (" 
						+ config.getConfigPath() + ").",
						JOptionPane.ERROR_MESSAGE);
	}
	
	private void showUnknownHostError(String hostname) {
		
		Warning.warnUser("Error", 
				"ERROR: Unknown proxy hostname: " + hostname,
						JOptionPane.ERROR_MESSAGE);
	}
	
	
	/**
	 * Install the latest version of the application
	 * @return
	 */
	private boolean installLatestVersion() {
		
		try {
			check.updateVersion();
			
		} catch (IOException e) {
			LOGGER.error("Error in installing the latest version ",e);
			e.printStackTrace();

			if (e instanceof UnknownHostException) {
				showUnknownHostError(e.getMessage());
			}
			else if (e instanceof ProxyConfigException) {
				showConfigurationError(e.getMessage());
			}
			else if (e instanceof ConnectException){
				showConnectionError();
			}
			else {
			    String trace = Warning.getStackTrace(e);
			    
			    Warning.warnUser("Generic error", 
			    		"XERRX: Generic runtime error. Please contact zoonoses_support@efsa.europa.eu. Error message " 
			    				+ trace, JOptionPane.ERROR_MESSAGE);
			}
			
			return false;
		}
		
		return true;
	}
}
