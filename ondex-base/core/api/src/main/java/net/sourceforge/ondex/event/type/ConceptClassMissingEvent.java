package net.sourceforge.ondex.event.type;

/**
 * EventType for missing concept classes.
 * 
 * @author taubertj
 * 
 */
public class ConceptClassMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public ConceptClassMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more ConceptClasses are missing in the metadata.";
	}

}
