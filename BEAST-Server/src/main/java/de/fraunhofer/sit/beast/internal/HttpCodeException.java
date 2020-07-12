package de.fraunhofer.sit.beast.internal;

public class HttpCodeException extends Exception {

	public final int responseCode;

	public HttpCodeException(int responseCode, String string) {
		super(string);
		this.responseCode = responseCode;
	}

}
