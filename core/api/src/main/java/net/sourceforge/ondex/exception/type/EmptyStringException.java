package net.sourceforge.ondex.exception.type;

/**
 * 
 * Exception type for empty input string.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class EmptyStringException extends WrongParameterException {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 2710082086249416951L;

	//####FIELDS####

	//####CONSTRUCTOR####
	
	/**
	 * standard constructor.
	 */
	public EmptyStringException() {
		
	}
	
	/**
	 * constructor with message.
	 * @param message the message.
	 */
	public EmptyStringException(String message) {
		super(message);
	}

	//####METHODS####
}
