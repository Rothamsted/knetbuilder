package net.sourceforge.ondex.event.type;

/**
 * EventType for wrong parameters.
 * 
 * @author lysenkoa
 * 
 */
public class UnspecifiedErrorEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public UnspecifiedErrorEvent(String message, String extension) {
		super(message, extension);
		super.desc = "An unspecified error has occured: ";
	}
}
