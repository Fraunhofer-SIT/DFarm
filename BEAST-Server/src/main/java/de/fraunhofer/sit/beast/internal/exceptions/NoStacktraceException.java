package de.fraunhofer.sit.beast.internal.exceptions;

/**
 * Use this exception for meaningful exceptions, for which the stacktrace is not
 * needed. We can display this kind of exception in form of a nice dialog and
 * not the nasty "Report a bug dialog"
 * 
 * @author Marc Miltenberger
 */
public class NoStacktraceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoStacktraceException(String msg) {
		super(msg);
	}

}
