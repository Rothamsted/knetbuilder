package net.sourceforge.ondex.algorithm.relationneighbours;

/**
 * Indicates an error in the logic
 * @author hindlem
 *
 */
public class InvalidLogicError extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param message a logical error message
	 * 
	 */
	public InvalidLogicError(String message) {
		super(message);
	}
	
}
