package net.sourceforge.ondex.event.type;

/**
 * EventType for missing relation type.
 * 
 * @author taubertj
 * 
 */
public class RelationTypeMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public RelationTypeMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more RelationTypes are missing in the metadata.";
	}

}
