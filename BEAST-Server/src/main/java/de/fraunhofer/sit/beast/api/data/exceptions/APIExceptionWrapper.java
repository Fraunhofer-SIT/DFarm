package de.fraunhofer.sit.beast.api.data.exceptions;

public class APIExceptionWrapper extends RuntimeException {

	public final APIException exception;

	public APIExceptionWrapper(APIException exception) {
		super(exception.code + " - " + exception.title + ": " + exception.description);
		this.exception = exception;
	}

}
