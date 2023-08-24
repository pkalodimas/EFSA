package log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Object which allows to write the System.out and System.err
 * flows into a file, in order to track the errors.
 * @author avonva
 *
 */
public class ConsolePrinter {

	private static ConsolePrinter logger;
	
	private PrintStream printStream;
	private FileOutputStream outputStream;
	
	// hide constructor
	private ConsolePrinter() {}
	
	/**
	 * Get an instance of the {@link ConsolePrinter}
	 * @return
	 */
	public static ConsolePrinter getInstance() {
		
		if (logger == null)
			ConsolePrinter.logger = new ConsolePrinter();
			
		return logger;
	}
	
	/**
	 * Stop logging and close the stream which writes in the log file
	 * @throws IOException
	 */
	public void close() throws IOException {
		
		// reset the standard error output
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
		
		if (printStream == null)
			return;

		outputStream.close();
		printStream.close();
	}
	
	public File start(int sessionId, File logDirectory) throws IOException {
		return this.start(String.valueOf(sessionId), logDirectory);
	}
	
	public File start(long sessionId, File logDirectory) throws IOException {
		return this.start(String.valueOf(sessionId), logDirectory);
	}
	
	/**
	 * Start logging the current session of the application
	 * @param sessionId session id which identifies the current launch of the application
	 * @param logDirectory directory where the log will be created
	 * @return {@link File} reference to the log file which is created
	 * @throws IOException
	 */
	public File start(String sessionId, File logDirectory) throws IOException {
		
		// if the directory does not exist create it
		if (!logDirectory.exists()) {
			
			boolean created = logDirectory.mkdir();
			
			// if cannot create => error
			if (!created)
				throw new IOException("Cannot open/create the directory to save logs: " 
			+ logDirectory.getAbsolutePath());
		}
		
		// if the file is not a directory => error
		if (!logDirectory.isDirectory())
			throw new IOException("The specified directory for saving logs is not a directory: "
					+ logDirectory.getAbsolutePath());
		
		// create a logging file
		Calendar dCal = Calendar.getInstance();
		Format formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

		// create a reference for the log file
		File log = new File(
				logDirectory.getPath() + System.getProperty("file.separator")
				+ "date=" + formatter.format(dCal.getTime())
				+ " session=" + sessionId
				+ ".txt");

		// Initialise the stream for writing the log
		this.outputStream = new FileOutputStream(log);

		// set the logging file as output for the standard error messages
		this.printStream = new PrintStream(outputStream);
		
		// route the system flows into the log file
		System.setErr(printStream);
		System.setOut(printStream);
		
		return log;
	}
}
