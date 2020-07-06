/**
 * 
 */
package net.sourceforge.ondex.exception.type;

/**
 * Exception type for wrong or missing producer arguments.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class WrongArgumentException extends PluginConfigurationException {

	// ####FIELDS####
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 3417734580803358911L;

	// ####CONSTRUCTOR####
	/**
	 * constructor.
	 */
	public WrongArgumentException() {
	}

	/**
	 * constructor with custom message.
	 * 
	 * @param message
	 */
	public WrongArgumentException(String message) {
		super(message);
	}

	// ####METHODS####
}
