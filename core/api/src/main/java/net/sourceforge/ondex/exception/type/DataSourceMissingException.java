package net.sourceforge.ondex.exception.type;

/**
 * Exception type for missing DataSource.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class DataSourceMissingException extends MetaDataMissingException {

	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7665712795810115303L;

	//####CONSTRUCTOR####

	/**
	 * standard constructor.
	 */
	public DataSourceMissingException() {
	}

	/**
	 * constructor for custom message.
	 * 
	 * @param message the message.
	 */
	public DataSourceMissingException(String message) {
		super(message);
	}

	//####METHODS####
}
