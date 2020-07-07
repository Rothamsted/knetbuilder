package net.sourceforge.ondex.event.type;

/**
 * EventType for wrong parameters.
 * 
 * @author taubertj
 * 
 */
public class WrongParameterEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public WrongParameterEvent(String message, String extension) {
		super(message, extension);
		super.desc = "An error was present in your parameters.";
	}
}
