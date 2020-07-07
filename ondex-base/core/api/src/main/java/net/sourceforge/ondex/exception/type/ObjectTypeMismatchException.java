package net.sourceforge.ondex.exception.type;

/**
 * exception type for invoking methods with an object parameter of the wrong type.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class ObjectTypeMismatchException extends WrongParameterException {

	//####FIELDS####

	//####CONSTRUCTOR####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -510864213889587239L;

	/**
	 * standard constructor.
	 */
	public ObjectTypeMismatchException() {
		
	}
	
	/**
	 * constructor with custom message.
	 */
	public ObjectTypeMismatchException(String message) {
		super(message);
	}

	//####METHODS####
}
