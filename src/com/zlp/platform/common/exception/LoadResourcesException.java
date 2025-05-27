package com.zlp.platform.common.exception;

public class LoadResourcesException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LoadResourcesException() {
		super();
	}

	public LoadResourcesException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoadResourcesException(String message) {
		super(message);
	}

	public LoadResourcesException(Throwable cause) {
		super(cause);
	}

}
