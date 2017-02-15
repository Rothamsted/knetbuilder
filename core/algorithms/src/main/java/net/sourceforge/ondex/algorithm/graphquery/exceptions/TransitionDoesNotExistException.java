package net.sourceforge.ondex.algorithm.graphquery.exceptions;

/**
 * 
 * @author hindlem
 *
 */
public class TransitionDoesNotExistException extends Exception {

	/**
	 * 
	 * @param error error message
	 */
	public TransitionDoesNotExistException(String error) {
		super(error);
	}

	private static final long serialVersionUID = 1L;
}
