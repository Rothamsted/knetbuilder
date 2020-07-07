package net.sourceforge.ondex.exception.type;

/**
 * Exception type for invoking methods without permission.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class AccessDeniedException extends RuntimeException {

	//####FIELDS####

	//####CONSTRUCTOR####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7002598921716556067L;

	/**
	 * standard constructor.
	 */
	public AccessDeniedException() {
		
	}
	
	/**
	 * constructor with custom message.
	 * @param message the message.
	 */
	public AccessDeniedException(String message) {
		super(message);
	}

    public AccessDeniedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AccessDeniedException(Throwable cause)
    {
        super(cause);
    }

    //####METHODS####
}
