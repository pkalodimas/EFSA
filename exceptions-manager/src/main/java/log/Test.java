package log;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	
	private static final Logger LOGGER = LogManager.getLogger(Test.class);

	public static void main(String[] args) throws IOException {
		
		File logDirectory = new File("logs");
		ConsolePrinter printer = ConsolePrinter.getInstance();
		printer.start(System.nanoTime(), logDirectory);
		
		LOGGER.info("Test: System.out flow into the log file");
		LOGGER.error("Test: System.err flow into the log file");
		
		try {
			throw new Exception("Test: exception into log file");
		}
		catch(Exception e) {
			LOGGER.error("Error in Test: exception into log file", e);
			e.printStackTrace();
		}
	}
}
