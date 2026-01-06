package com.summithillsoftware.ultimate.model;

public class InvalidEventException extends RuntimeException {
	private static final long serialVersionUID = 1l;

	public InvalidEventException() {
		super();
	}

	public InvalidEventException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public InvalidEventException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidEventException(Throwable throwable) {
		super(throwable);
	}

}
