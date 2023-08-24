package ack;

import soap.GetAck;

/**
 * Response of the {@link GetAck} request.
 * @author avonva
 *
 */
public enum FileState {
	
	READY("READY"),
	WAIT("WAIT"),
	FAIL("FAIL"),
	ACCESS_DENIED("ACCESS_DENIED"),
	EXCEPTION("EXCEPTION"),
	OTHER("OTHER");
	
	private String headerName;
	
	/**
	 * Initialise the enumerator with the real 
	 * header name that is present in the xlsx
	 * @param headerName
	 */
	private FileState(String headerName) {
		this.headerName = headerName;
	}
	
	/**
	 * Get the header name related to the enum field
	 * @return
	 */
	public String getHeaderName() {
		return this.headerName;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static FileState fromString(String text) {
		
		for (FileState b : FileState.values()) {
			if (b.headerName.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return OTHER;
	}
}
