package net.sourceforge.ondex.event.type;

import org.apache.log4j.Level;

/**
 * EventType for missing data files.
 * 
 * @author taubertj
 * 
 */
public class DataFileMissingEvent extends EventType {
    private static final String DESCR = "One or more required data files are missing.";

    /**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public DataFileMissingEvent(String message, String extension) {
		super(message, extension, DESCR);
	}

    public DataFileMissingEvent(String message, String extension, Level log4jLevel)
    {
        super(message, extension, DESCR, log4jLevel);
    }
}
