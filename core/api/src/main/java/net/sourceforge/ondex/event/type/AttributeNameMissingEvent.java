package net.sourceforge.ondex.event.type;

/**
 * EventType for missing attribute names.
 * 
 * @author taubertj
 * 
 */
public class AttributeNameMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public AttributeNameMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more AttributeNames are missing in the metadata.";
	}

}
