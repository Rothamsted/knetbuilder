package net.sourceforge.ondex.event.type;

/**
 * EventType for parsing errors.
 * 
 * @author taubertj
 * 
 */
public class ParsingErrorEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public ParsingErrorEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more lines from input file could not be parsed.";
	}

}
