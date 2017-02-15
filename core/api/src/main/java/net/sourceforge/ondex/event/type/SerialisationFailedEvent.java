package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * EventType for serialisation errors.
 * 
 * @author taubertj
 * 
 */
public class SerialisationFailedEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public SerialisationFailedEvent(String message, String extension) {
		super(message, extension);
		super.desc = "An error during ONDEX object serialisation occurred.";
		this.setLog4jLevel(Level.ERROR);
	}

}
