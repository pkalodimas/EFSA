package pending_request;

public enum PendingRequestPriority {
	HIGH("HIGH"),
	LOW("LOW");
	
	private String key;
	private PendingRequestPriority(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public static PendingRequestPriority fromString(String text) {
		
		for (PendingRequestPriority priority : PendingRequestPriority.values()) {
			if (priority.getKey().equals(text))
				return priority;
		}
		
		return null;
	}
}
