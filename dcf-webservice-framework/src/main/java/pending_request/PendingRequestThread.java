package pending_request;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import dcf_log.IDcfLogParser;
import soap.DetailedSOAPException;

/**
 * Launcher for a single {@link IPendingRequest} in background.
 * @author avonva
 *
 */
public class PendingRequestThread extends Thread {

	private static final Logger LOGGER = LogManager.getLogger(PendingRequestThread.class);
	
	private boolean finished;
	private IPendingRequest request;
	private IDcfLogParser parser;
	
	/**
	 * Prepare the launcher with a single {@link IPendingRequest} which need to be started
	 * @param requests
	 */
	public PendingRequestThread(IPendingRequest request, IDcfLogParser parser) {
		this.request = request;
		this.parser = parser;
		this.finished = false;
	}
	
	public IPendingRequest getRequest() {
		return this.request;
	}
	
	/**
	 * Start all the pending requests of the user
	 * @throws SQLException
	 * @throws SOAPException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public void run() {
		try {
			startRequest();
		} catch (SOAPException e) {
			LOGGER.error("Connection/authorization error", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Log was retrieved but cannot open/parse it", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOGGER.error("This should never happen, cannot pause the thread", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Start the single erquest
	 * @throws SOAPException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void startRequest() throws SOAPException, IOException, InterruptedException {
		
		boolean done = false;
		
		// this cycle is needed to reiterate the
		// start procedure if no connection error is found
		while(!done) {
			try {
				this.request.start(this.parser);  // start the request
				done = true;
			}
			catch(DetailedSOAPException e) {
				if (e.isConnectionProblem()) {
					
					// bad connection, wait connection
					LOGGER.error("Bad internet connection. The pending request=" 
							+ this.request + " will be relaunched in one minute", e);
					
					Thread.sleep(60000);
				}
				else {
					throw e;
				}
			}
		}
		
		this.finished = true;
	}
	
	public boolean isFinished() {
		return this.finished;
	}
}
