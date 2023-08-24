package message;

public enum TrxCode {
	TRXOK("TRXOK"),
	TRXKO("TRXKO"),
	OTHER("OTHER");
	
	private String headerName;
	
	/**
	 * Initialise the enumerator with the real 
	 * header name that is present in the xlsx
	 * @param headerName
	 */
	private TrxCode(String headerName) {
		this.headerName = headerName;
	}
	
	/**
	 * Get the header name related to the enum field
	 * @return
	 */
	public String getHeaderName() {
		return headerName;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static TrxCode fromString(String text) {
		
		for (TrxCode b : TrxCode.values()) {
			if (b.headerName.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return OTHER;
	}
}
