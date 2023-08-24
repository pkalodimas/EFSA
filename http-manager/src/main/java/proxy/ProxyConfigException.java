package proxy;

import java.io.IOException;

public class ProxyConfigException extends IOException {
	private static final long serialVersionUID = 1L;
	public ProxyConfigException(Exception e) {
		super(e);
	}
	public ProxyConfigException(String message) {
		super(message);
	}
}
