package de.fraunhofer.sit.beast.internal;

public class HttpCodeException extends Exception {

	private static final long serialVersionUID = -9077068205892240577L;

	public final int responseCode;

	public HttpCodeException(int responseCode, String string) {
		super(string);
		this.responseCode = responseCode;
	}

}
