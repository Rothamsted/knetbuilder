package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * EventType for null value errors.
 * 
 * @author taubertj
 * 
 */
public class NullValueEvent extends EventType {
	
	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public NullValueEvent(String message, String extension) {
		super(message,extension);
		super.desc = "One or more values are null.";
		this.setLog4jLevel(Level.ERROR);
	}
	
}
