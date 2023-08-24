package release;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import http.HttpManager;

/**
 * Thread to download the latest release from an endpoing
 * in background, while a progress bar is shown.
 * @author avonva
 *
 */
public class FileDownloader extends SwingWorker<Void, Integer> implements AutoCloseable {
	
	private SwingWorkerFinishedListener swingWorkerFinishedListener;
	
	private JProgressBar progressBar;
	private boolean finished;
	
	private String endpoint;
	private String outputFilename;
	
	// total bytes read and number of bytes read up to now
	private long contentLength;
	private long contentDownloaded;
	
	// stuff that needs to be closed at the end of the process
	private InputStream inputStream;
	private FileOutputStream outputStream;
	private ReadableByteChannel inputChannel;
	
	/**
	 * Download a version from the endpoint
	 * @param endpoint url where the version can be downloaded
	 * @param contentLength length of the file to be downloaded
	 * @param outputFilename where to store the downloaded file
	 * @throws IOException
	 */
	public FileDownloader(String endpoint, long contentLength, String outputFilename) throws IOException {
		
		this.endpoint = endpoint;
		this.contentLength = contentLength;
		this.outputFilename = outputFilename;
		
		this.finished = false;
		
		this.outputStream = new FileOutputStream(outputFilename);
		
		// connect to the endpoint
		connect();
	}
	
	/**
	 * Called at the end of the process
	 * @param swingWorkerFinishedListener
	 */
	public void setSwingWorkerFinishedListener(SwingWorkerFinishedListener 
			swingWorkerFinishedListener) {
		this.swingWorkerFinishedListener = swingWorkerFinishedListener;
	}
	
	/**
	 * Set a progress bar for the process
	 * @param progressBar
	 */
	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	/**
	 * Connect to the endpoint
	 * @throws IOException
	 */
	private void connect() throws IOException {
		System.out.println("Connecting to: " + endpoint);
		URLConnection con = HttpManager.openConnection(endpoint);
		this.inputStream = con.getInputStream();
		this.inputChannel = Channels.newChannel(inputStream);
	}

	@Override
	protected void process(List<Integer> chunks) {

		// get the bytes read in the phases
		long bytesRead = 0;
		for (int chunk : chunks) {
			bytesRead += chunk;
		}

		// update counter of total read bytes
		this.contentDownloaded += bytesRead;

		// compute the relative progress
		double progress = ((contentDownloaded * 100)/ contentLength);
		
		System.out.println("Read " + bytesRead + " bytes; download progress: " + progress + " %");
		
		// update also the progress bar
		if (this.progressBar != null)
			this.progressBar.setValue((int)(progress));
	}
	
	@Override
	protected Void doInBackground() throws IOException {
		
		byte[] buffer = new byte[327680];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
		    outputStream.write(buffer, 0, bytesRead);
			publish(bytesRead);
		}
		
		return null;
	}
	
	@Override
	protected void done() {
		
		System.out.println("Download finished");
		
		this.finished = true;
		
		// call finished listener
		if (swingWorkerFinishedListener != null)
			swingWorkerFinishedListener.finished();
	}
	
	@Override
	public void close() throws IOException {
		this.outputStream.close();
		this.inputChannel.close();
		this.inputStream.close();
	}
	
	/**
	 * Check if the process is finished or not
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * Get the filename where the version is downloaded
	 * @return
	 */
	public String getOutputFilename() {
		return outputFilename;
	}
}
