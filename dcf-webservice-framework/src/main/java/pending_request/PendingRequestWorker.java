package pending_request;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingWorker;
import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcf_log.DcfLogParser;
import dcf_log.IDcfLogParser;

/**
 * Thread which is always active. It launches and listens to {@link IPendingRequest}
 * status changes, and updates the application interface using the
 * {@link #statusChanged(PendingRequestStatusChangedEvent)} method.
 * @author avonva
 *
 */
public abstract class PendingRequestWorker extends SwingWorker<Void, PendingRequestStatusChangedEvent> {

	private static final Logger LOGGER = LogManager.getLogger(PendingRequestWorker.class);
	
	private PendingRequestLauncher pool;
	
	public PendingRequestWorker() {
		this(new DcfLogParser());
	}
	
	public PendingRequestWorker(IDcfLogParser parser) {
		this.pool = new PendingRequestLauncher(parser);
		
		// set the listener for the requests
		this.pool.addPendingRequestListener(new PendingRequestListener() {
			
			@Override
			public void statusChanged(PendingRequestStatusChangedEvent event) {
				publish(event);
			}
		});
	}

	@Override
	protected void process(List<PendingRequestStatusChangedEvent> events) {
		
		LOGGER.debug("Processing events=" + events);
		
		// update the UI according to 
		// the status of the pending request
		for(PendingRequestStatusChangedEvent event : events)
			this.statusChanged(event);
	}
	
	@Override
	protected Void doInBackground() throws IOException {

		// this thread will never finish
		// in order to listen to the new
		// pending requests
		while(true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				LOGGER.error("Error in doInBackground ", e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Add a new request to the pool. It will be
	 * started as soon as possible.
	 * @param request
	 * @throws SOAPException
	 * @throws IOException
	 */
	public void startPendingRequests(IPendingRequest... requests) {
		
		LOGGER.debug("Starting new requests=" + Arrays.asList(requests));
		
		this.pool.startPendingRequests(requests);
	}
	
	@Override
	protected void done() {}
	
	/**
	 * Update database and user interface according to the
	 * received event
	 * @param event
	 */
	public abstract void statusChanged(PendingRequestStatusChangedEvent event);
}
