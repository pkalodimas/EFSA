package dcf_log;

import java.io.File;

import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.Environment;
import soap.ExportCatalogueFile;
import soap_interface.IExportCatalogueFile;
import user.IDcfUser;

/**
 * Class used to download a dcf Log document starting from a log code.
 * Since a log is not immediately available in DCF, a polling
 * strategy can be used. In particular, it is possible to set the inter
 * attempts time and a limit to the maximum number of attempts.
 * @author avonva
 * @author shahaal
 *
 */
public class DcfLogDownloader implements IDcfLogDownloader {

	private static final Logger LOGGER = LogManager.getLogger(DcfLogDownloader.class);

	private IExportCatalogueFile exportCatFile;  // soap object
	private boolean waiting;
	
	public DcfLogDownloader() {
		this.waiting = true;
	}
	
	public DcfLogDownloader(IExportCatalogueFile exportCatFile) {
		this.exportCatFile = exportCatFile;
		this.waiting = true;
	}
	
	/**
	 * Download a log without polling strategy
	 * @param logCode the code of the log to download
	 * @throws SOAPException
	 */
	@Override
	public File getLog(IDcfUser user, Environment env, String logCode) throws SOAPException {
		
		if (this.exportCatFile == null)
			this.exportCatFile = new ExportCatalogueFile();
		
		File log = this.exportCatFile.exportLog(env, user, logCode);
		
		if (log != null)
			LOGGER.info("Log successfully downloaded, file=" + log);

		return log;
	}
	
	/**
	 * Download a log from DCF with a polling strategy
	 * @param logCode the code of the log to download
	 * @param interAttemptsTime waiting time before trying again to download the log
	 * @throws SOAPException 
	 */
	@Override
	public File getLog(IDcfUser user, Environment env, String logCode, long interAttemptsTime) throws SOAPException {
		return getLog(user, env, logCode, interAttemptsTime, -1);
	}
	
	/**
	 * Download a log from DCF with a polling strategy
	 * @param logCode the code of the log to download
	 * @param interAttemptsTime waiting time before trying again to download the log
	 * @param maxAttempts max number of allowed attempts (prevents DOS)
	 * @throws SOAPException 
	 */
	@Override
	public synchronized File getLog(IDcfUser user, Environment env, String logCode, 
			long interAttemptsTime, int maxAttempts) throws SOAPException {
		
		// if maxAttempts is > 0 then a limit is applied
		boolean isLimited = maxAttempts > 0;
		
		File log = null;
		
		// number of tried attempts
		int attemptsCount = 1;
		
		// polling
		while(true) {

			String diagnostic = "Getting log=" + logCode + ", attempt n°=" + attemptsCount;
			
			// add maximum number of attempts if limited attempts
			if(isLimited)
				diagnostic += "/" + maxAttempts;
			
			LOGGER.info(diagnostic);

			log = getLog(user, env, logCode);
			
			if(log == null)
				LOGGER.info("Log=" + logCode + " not available yet in DCF");

			// if the log was found or no another attempt is available
			if(log != null || (isLimited && attemptsCount >= maxAttempts))
				break;
			
			LOGGER.info("Waiting " + (interAttemptsTime/1000.00) 
					+ " seconds and then retry to download log=" + logCode);
			
			// wait inter attempts time
			try {
				this.waiting = true;
				this.wait(interAttemptsTime);
			} catch(InterruptedException e) {
				LOGGER.error("Error during wait: ", e);
				e.printStackTrace();
			}
			
			this.waiting = false;
			
			// go to the next attempt
			attemptsCount++;
		}
		
		return log;
	}

	public boolean isWaiting() {
		return this.waiting;
	}
	
	@Override
	public synchronized void skipWait() {
		if(this.isWaiting())
			this.notify();
	}
}
