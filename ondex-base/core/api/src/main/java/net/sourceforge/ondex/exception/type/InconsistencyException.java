package net.sourceforge.ondex.exception.type;

/**
 * Exception type for a failing producer due to a data inconsistency.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class InconsistencyException extends PluginConfigurationException {

	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7758766995322188379L;

	//####CONSTRUCTOR####

	/**
	 * standard constructor.
	 */
	public InconsistencyException() {
		
	}

	/**
	 * constructor with custom message.
	 * @param message
	 */
	public InconsistencyException(String message) {
		super(message);
	}

	//####METHODS####
}
