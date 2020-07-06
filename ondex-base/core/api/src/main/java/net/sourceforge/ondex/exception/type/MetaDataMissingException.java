package net.sourceforge.ondex.exception.type;

/**
 * Exception type for a failing due to missing metadata.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class MetaDataMissingException extends PluginException {

	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -2029419916769745016L;

	//####CONSTRUCTOR####

	/**
	 * standard constructor.
	 */
	public MetaDataMissingException() {
		
	}

	/**
	 * constructor for custom messages.
	 * @param message the message.
	 */
	public MetaDataMissingException(String message) {
		super(message);
	}

	//####METHODS####
}
