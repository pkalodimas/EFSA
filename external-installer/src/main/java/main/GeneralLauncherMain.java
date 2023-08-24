package main;

import javax.swing.JOptionPane;

import config.GithubChecker;
import dialog.Warning;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeneralLauncherMain {
	
	private static final Logger LOGGER = LogManager.getLogger(GeneralLauncherMain.class);

	public static void main(String[] args) {

		try {
			// initialise the library
			GithubChecker.initialize();
			
			FlowActions actions = new FlowActions();
			actions.start();
		}
		catch (Exception e) {
			LOGGER.error("Generic error during runtime: ", e);
			e.printStackTrace();
		    String trace = Warning.getStackTrace(e);
		    
		    Warning.warnUser("Generic error", 
		    		"XERRX: Generic runtime error. Please contact zoonoses_support@efsa.europa.eu. Error message " 
		    				+ trace, JOptionPane.ERROR_MESSAGE);
		}
	}
}
