package com.summithillsoftware.ultimate.util;

public class ObjectStoreError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ObjectStoreError() {
		super();
	}

	public ObjectStoreError(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ObjectStoreError(String detailMessage) {
		super(detailMessage);
	}

	public ObjectStoreError(Throwable throwable) {
		super(throwable);
	}

}
