package net.sourceforge.ondex.event.type;

/**
 * EventType for inconsistency in data.
 * 
 * @author taubertj
 * 
 */
public class InconsistencyEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public InconsistencyEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more inconsistencies have been found in the data.";
	}

}
