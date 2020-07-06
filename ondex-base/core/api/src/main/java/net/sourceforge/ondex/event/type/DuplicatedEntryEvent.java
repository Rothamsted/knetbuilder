package net.sourceforge.ondex.event.type;

/**
 * EventType for duplicated entry errors.
 * 
 * @author taubertj
 * 
 */
public class DuplicatedEntryEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public DuplicatedEntryEvent(String message, String extension) {
		super(message, extension);
		super.desc = "A duplicated entry was inserted.";
	}

}
