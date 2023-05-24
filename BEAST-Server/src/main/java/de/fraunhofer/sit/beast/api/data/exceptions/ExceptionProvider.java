package de.fraunhofer.sit.beast.api.data.exceptions;

import java.io.EOFException;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fraunhofer.sit.beast.internal.exceptions.NoStacktraceException;
import de.fraunhofer.sit.beast.internal.persistance.Database;


@Provider
public class ExceptionProvider implements ExceptionMapper<Throwable> {

	private static final Logger logger = LogManager.getLogger(ExceptionProvider.class);

	@Override
	public Response toResponse(Throwable t) {
		APIException ex = doTransform(t); 
		return Response.status(ex.code, ex.description).entity(ex).type(MediaType.APPLICATION_JSON_TYPE).build();
	}

	private static APIException doTransform(Throwable t) {
		if (t instanceof ExecutionException)
			t = t.getCause();
		if (t instanceof APIExceptionWrapper)
			return ((APIExceptionWrapper) t).exception;
		if (t instanceof EOFException) {
			return new APIException(599, "EOS", "End of stream reached; did you miss to supply a file");
		}
		if (t instanceof NoStacktraceException) {
			return new APIException(599, t.getMessage(), t.getMessage());
		}
		if (t instanceof SecurityException)
			return new AccessDeniedException(t.getMessage());
		Database.logError(t);
		return new APIException(598, "Unknown error: " + t.getMessage(), "Unknown error: " + ExceptionUtils.getStackTrace(t));
	}

}
