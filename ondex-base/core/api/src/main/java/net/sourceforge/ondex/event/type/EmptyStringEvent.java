package net.sourceforge.ondex.event.type;

/**
 * EventType for empty string errors.
 * 
 * @author taubertj
 * 
 */
public class EmptyStringEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public EmptyStringEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more string has zero length.";
	}

}
