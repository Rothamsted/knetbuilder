package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * EventType for data file errors.
 * 
 * @author taubertj
 * 
 */
public class DataFileErrorEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public DataFileErrorEvent(String message, String extension) {
		super(message, extension);
		super.desc = "An error occurred with one or more data files.";
		this.setLog4jLevel(Level.ERROR);
	}

}
