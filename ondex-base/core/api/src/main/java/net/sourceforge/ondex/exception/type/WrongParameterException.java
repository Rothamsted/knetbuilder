package net.sourceforge.ondex.exception.type;

/**
 * 
 * exception type for invoking a method with invalid parameters.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class WrongParameterException extends RuntimeException {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7197821387029424651L;

	//####FIELDS####

	//####CONSTRUCTOR####

    public WrongParameterException() {
    }

    public WrongParameterException(String message) {
        super(message);
    }

    public WrongParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongParameterException(Throwable cause) {
        super(cause);
    }


    //####METHODS####
}
