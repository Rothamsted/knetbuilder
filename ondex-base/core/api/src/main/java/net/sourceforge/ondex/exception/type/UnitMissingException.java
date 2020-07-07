package net.sourceforge.ondex.exception.type;

/**
 * Exception type for missing unit.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class UnitMissingException extends MetaDataMissingException {

	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7665712795810115303L;

	//####CONSTRUCTOR####

	/**
	 * standard constructor.
	 */
	public UnitMissingException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * constructor for custom message.
	 * 
	 * @param message the message.
	 */
	public UnitMissingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	//####METHODS####
}
