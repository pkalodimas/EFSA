package dataset;

/**
 * Enumerator that identifies the status of a {@link DcfDataset}
 * @author avonva
 *
 */
public enum DcfDatasetStatus {
	
	VALID("VALID"),
	PROCESSING("PROCESSING"),
	VALID_WITH_WARNING("VALID_WITH_WARNING"),
	REJECTED_EDITABLE("REJECTED EDITABLE"),
	REJECTED("REJECTED"),
	DELETED("DELETED"),
	SUBMITTED("SUBMITTED"),
	ACCEPTED_DWH("ACCEPTED DWH"),
	UPDATED_BY_DATA_RECEIVER("UPLOADED_BY_DATA_RECEIVER"),
	OTHER("OTHER");  // error state
	
	private String status;
	private String step;
	
	private DcfDatasetStatus(String status) {
		this.status = status;
	}
	
	public void setStep(String step) {
		this.step = step;
	}
	
	public String getStep() {
		return this.step;
	}
	
	/**
	 * Get the raw status
	 * @return
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Get the enumerator that matches the {@code text}
	 * @param text
	 * @return
	 */
	public static DcfDatasetStatus fromString(String text) {
		
		String myStatus = text.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
		
		for (DcfDatasetStatus b : DcfDatasetStatus.values()) {
			
			String otherStatus = b.status.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
			
			if (otherStatus.equalsIgnoreCase(myStatus)) {
				return b;
			}
		}
		
		return OTHER;
	}
}
