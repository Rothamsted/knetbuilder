package net.sourceforge.ondex.exception.type;

/**
 * Exception type for memory leak events
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class DataLossException extends RuntimeException {


	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 914167520812200509L;
	
    public DataLossException() {
    }

    public DataLossException(String message) {
        super(message);
    }

    public DataLossException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataLossException(Throwable cause) {
        super(cause);
    }
}
