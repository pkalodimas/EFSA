package pending_request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.soap.SOAPException;

import dcf_log.IDcfLogParser;

/**
 * Launcher for a set or {@link IPendingRequest}, that is,
 * one {@link PendingRequestThread} is created for each {@link IPendingRequest}.
 * Every thread is independent and runs in background.
 * @author avonva
 * @author shahaal
 */
public class PendingRequestLauncher {
	
	private List<PendingRequestThread> pool;
	private Collection<PendingRequestListener> listeners;
	private IDcfLogParser parser;
	private boolean started;
	
	/**
	 * Prepare the launcher
	 */
	public PendingRequestLauncher(IDcfLogParser parser) {
		this.listeners = new ArrayList<>();
		this.parser = parser;
		this.pool = new ArrayList<>();
	}
	
	/**
	 * listen to the requests status changes
     * to update database/graphics accordingly
	 * @param listener
	 */
	public void addPendingRequestListener(PendingRequestListener listener) {
		
		if (this.started)
			throw new IllegalStateException("Cannot add a pending listener after the Launcher is started");
		
		this.listeners.add(listener);
	}
	
	/**
	 * Add requests to be processed
	 * @param request
	 * @throws SOAPException
	 * @throws IOException
	 */
	public void startPendingRequests(IPendingRequest... requests) {
		for (IPendingRequest request : requests)
			this.startRequest(request);
	}

	/**
	 * Start a single request
	 * @param req
	 * @throws SOAPException
	 * @throws IOException
	 */
	private void startRequest(IPendingRequest req) {
		
		// do not restart already started requests
		for (PendingRequestThread startedReq : this.pool) {
			if (startedReq.getRequest().getLogCode().equals(req.getLogCode()))
				return;
		}
		
		// set the listeners
		for (PendingRequestListener listener : this.listeners)
			req.addPendingRequestListener(listener);
		
		// prepare the single request launcher
		PendingRequestThread launcher = new PendingRequestThread(req, this.parser);
		
		// add the launcher to the pool
		this.pool.add(launcher);
		
		launcher.start();
	}
}
