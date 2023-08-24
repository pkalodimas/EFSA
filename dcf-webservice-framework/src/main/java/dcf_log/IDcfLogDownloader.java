package dcf_log;

import java.io.File;

import javax.xml.soap.SOAPException;

import config.Environment;
import user.IDcfUser;

public interface IDcfLogDownloader {
	
	/**
	 * Download a log without polling strategy
	 * @param logCode the code of the log to download
	 * @throws SOAPException
	 */
	public File getLog(IDcfUser user, Environment env, String logCode) throws SOAPException;
	
	/**
	 * Download a log from DCF with a polling strategy
	 * @param logCode the code of the log to download
	 * @param interAttemptsTime waiting time before trying again to download the log
	 * @throws SOAPException 
	 */
	public File getLog(IDcfUser user, Environment env, String logCode, long interAttemptsTime) throws SOAPException;
	
	/**
	 * Download a log from DCF with a polling strategy
	 * @param logCode the code of the log to download
	 * @param interAttemptsTime waiting time before trying again to download the log
	 * @param maxAttempts max number of allowed attempts (prevents DOS)
	 * @throws SOAPException 
	 */
	public File getLog(IDcfUser user, Environment env, String logCode, long interAttemptsTime, int maxAttempts) throws SOAPException;
	
	/**
	 * Skip the wait process and try to get the log immediately
	 */
	public void skipWait();
}
