package net.sourceforge.ondex.algorithm.graphquery.exceptions;

/**
 * Thrown when a Rank can not be applied to a route
 * @author hindlem
 *
 */
public class UnrankableRouteException extends Exception {
	/**
	 * 
	 * @param error error message
	 */
	public UnrankableRouteException(String error) {
		super(error);
	}

	private static final long serialVersionUID = 1L;
	
}
