package net.sourceforge.ondex.event.type;

/**
 * EventType for general output.
 * 
 * @author taubertj
 * 
 */
public class GeneralOutputEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public GeneralOutputEvent(String message, String extension) {
		super(message, extension);
		super.desc = "Output of some information.";
	}
}
