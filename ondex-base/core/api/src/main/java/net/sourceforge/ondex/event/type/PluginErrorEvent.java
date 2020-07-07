package net.sourceforge.ondex.event.type;

/**
 * EventType for producer errors.
 * 
 * @author hindlem
 * 
 */
public class PluginErrorEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public PluginErrorEvent(String message, String extension) {
		super(message, extension);
		super.desc = "Plugin reported errors and could not complete";
	}

}
