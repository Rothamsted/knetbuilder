package net.sourceforge.ondex.event.type;

/**
 * EventType for missing relation type set.
 * 
 * @author taubertj
 * 
 */
public class RelationTypeSetMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public RelationTypeSetMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more RelationTypeSets are missing in the metadata.";
	}

}
