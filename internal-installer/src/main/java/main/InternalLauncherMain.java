package main;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.GithubChecker;
import dialog.Warning;

public class InternalLauncherMain {
	
	private static final Logger LOGGER = LogManager.getLogger(InternalLauncherMain.class);

	public static void main(String[] args) {

		try {
			// initialize the library
			GithubChecker.initialize();
			
			FlowActions actions = new FlowActions();
			actions.start();
		}
		catch (Exception e) {
			LOGGER.error("There are error ypon initialize", e);
			e.printStackTrace();
		    String trace = Warning.getStackTrace(e);
		    
		    Warning.warnUser("Generic error", 
		    		"XERRX: Generic runtime error. Please contact zoonoses_support@efsa.europa.eu. Error message " 
		    				+ trace, JOptionPane.ERROR_MESSAGE);
		}
	}
}
