package dcf_log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IDcfLogParser {

	/**
	 * Parse a log file and build a DcfLog object
	 * with the log information
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public DcfLog parse(File file) throws IOException;
	
	/**
	 * Parse an input stream which contains a log	
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public DcfLog parse(InputStream input) throws IOException;
}
