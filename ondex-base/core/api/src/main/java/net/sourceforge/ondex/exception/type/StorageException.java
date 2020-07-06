package net.sourceforge.ondex.exception.type;

/**
 * Exception type for failed serialisation of objects.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class StorageException extends DataLossException {

	//####FIELDS####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -2151231270141876583L;

    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
}
