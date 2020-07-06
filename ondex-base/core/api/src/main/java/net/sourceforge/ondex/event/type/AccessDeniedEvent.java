package net.sourceforge.ondex.event.type;

/**
 * EventType for access denied errors.
 * 
 * @author taubertj
 * 
 */
public class AccessDeniedEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public AccessDeniedEvent(String message, String extension) {
		super(message, extension);
		super.desc = "Access to an element was denied.";
	}

}
