package utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	/**
	 * Trasform a date string into a timestamp
	 * @param dateString
	 * @param dateFormat
	 * @return
	 * @throws ParseException
	 */
	public static Timestamp getTimestampFromString(String dateString, 
			String dateFormat) throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
	    Date parsedDate = format.parse(dateString);
	    Timestamp ts = new Timestamp(parsedDate.getTime());
	    return ts;
	}
}
