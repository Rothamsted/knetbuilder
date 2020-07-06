package net.sourceforge.ondex.exception.type;

/**
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class NullValueException extends WrongParameterException {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 2710082086249416951L;

	//####FIELDS####

	//####CONSTRUCTOR####
	
	/**
	 * standard constructor.
	 */
	public NullValueException() {
		
	}
	
	/**
	 * constructor with message.
	 * @param message the message.
	 */
	public NullValueException(String message) {
		super(message);
	}

	//####METHODS####
}
