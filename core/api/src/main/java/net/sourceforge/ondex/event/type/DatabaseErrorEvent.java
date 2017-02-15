package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * EventType for persistent database errors.
 * 
 * @author taubertj
 * 
 */
public class DatabaseErrorEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public DatabaseErrorEvent(String message, String extension) {
		super(message, extension);
		super.desc = "An error occurred with the persistent layer.";
		this.setLog4jLevel(Level.ERROR);
	}

}
