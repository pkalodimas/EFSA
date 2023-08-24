package converter;

public class ExceptionConverter {

	/**
	 * Convert the exception stack trace into a printable
	 * string which contains all the information needed.
	 * @param e
	 * @return
	 */
	public static String getStackTrace(Exception e) {
		
		StringBuilder sb = new StringBuilder();
		
		String name = e.getClass().getName();
		
		sb.append(name).append("\n");
		
		String message = e.getMessage();
		for (StackTraceElement ste : e.getStackTrace()) {
	        sb.append("\n\tat ");
	        sb.append(ste);
	    }
	    String trace = message + " " + sb.toString();
	    
	    return trace;
	}
}
