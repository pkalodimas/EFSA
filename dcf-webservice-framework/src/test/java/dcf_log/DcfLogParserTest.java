package dcf_log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DcfLogParserTest {

	private static final String ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	/**
	 * Get the timestamp contained in the string
	 * 
	 * @param date
	 * @return
	 */
	public static Timestamp getTimestamp(String date) {

		SimpleDateFormat format = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);

		Timestamp ts = null;

		try {
			Date parsedDate = format.parse(date);
			ts = new Timestamp(parsedDate.getTime());

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return ts;
	}

	@Test
	public static void parseLog() throws IOException {

		File file = new File("test-files" + System.getProperty("file.separator") + "log.xml");

		DcfLogParser parser = new DcfLogParser();
		DcfLog log = parser.parse(file);

		assertEquals("Web service", log.getAction());
		assertEquals(getTimestamp("2018-01-31T12:37:07.984+01:00"), log.getTransmissionDate());
		assertEquals(getTimestamp("2018-01-31T12:37:08.257+01:00"), log.getProcessingDate());
		assertEquals("AMRPROG", log.getCatalogueCode());
		assertEquals("1.3.2", log.getCatalogueVersion());
		assertEquals("DRAFT MINOR UNRESERVED", log.getCatalogueStatus());
		assertEquals("updateCatalogue", log.getMacroOpName());
		assertEquals(DcfResponse.OK, log.getMacroOpResult());
		assertEquals(1, log.getMacroOpLogs().size());
		assertEquals("The catalogue AMRPROG has been updated", log.getMacroOpLogs().iterator().next());
		assertEquals(1, log.getLogNodes().size());
		assertTrue(log.isMacroOperationCorrect());

		// try again, it should be an idempotent operation
		log = parser.parse(file);
		assertEquals(1, log.getMacroOpLogs().size());
		assertEquals(0, log.getValidationErrors().size());
		assertEquals(1, log.getLogNodes().size());
	}

	@Test
	public static void parseLog2() throws IOException {
		File file = new File("test-files" + System.getProperty("file.separator") + "log2.xml");

		DcfLogParser parser = new DcfLogParser();
		DcfLog log = parser.parse(file);

		assertEquals("Web service", log.getAction());
		assertEquals(getTimestamp("2018-02-06T09:20:11.113+01:00"), log.getTransmissionDate());
		assertEquals(getTimestamp("2018-02-06T09:20:17.386+01:00"), log.getProcessingDate());
		assertEquals("ABUNDANCE", log.getCatalogueCode());
		assertEquals("4.5.7", log.getCatalogueVersion());
		assertEquals("DRAFT MINOR RESERVED", log.getCatalogueStatus());
		assertEquals("publishMinor", log.getMacroOpName());
		assertEquals(DcfResponse.AP, log.getMacroOpResult());
		assertEquals(1, log.getMacroOpLogs().size());
		assertEquals(
				"The status of the catalogue ABUNDANCE does not allow the publishMinor operation. File processing is aborted.",
				log.getMacroOpLogs().iterator().next());
		assertEquals(0, log.getLogNodes().size());
		assertEquals(0, log.getValidationErrors().size());
		assertFalse(log.isMacroOperationCorrect());
	}
}
