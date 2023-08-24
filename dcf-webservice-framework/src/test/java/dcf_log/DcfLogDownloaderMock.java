package dcf_log;

import java.io.File;

import javax.xml.soap.SOAPException;

import config.Environment;
import user.IDcfUser;

public class DcfLogDownloaderMock implements IDcfLogDownloader {

	@Override
	public File getLog(IDcfUser user, Environment env, String logCode) throws SOAPException {
		return new File("Log.xml");
	}

	@Override
	public File getLog(IDcfUser user, Environment env, String logCode, long interAttemptsTime) throws SOAPException {
		return new File("Log.xml");
	}

	@Override
	public File getLog(IDcfUser user, Environment env, String logCode, long interAttemptsTime, int maxAttempts) throws SOAPException {
		return new File("Log.xml");
	}

	@Override
	public void skipWait() {}

}
