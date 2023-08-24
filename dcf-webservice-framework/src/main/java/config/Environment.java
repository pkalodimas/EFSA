package config;

public enum Environment {
	PRODUCTION("PROD"),
	TEST("TEST");
	
	private String key;
	private Environment(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public static Environment fromString(String text) {
		
		for (Environment env : Environment.values()) {
			if (env.getKey().equals(text))
				return env;
		}
		
		return null;
	}
}
