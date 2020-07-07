package net.sourceforge.ondex.event.type;

/**
 * EventType for missing evidence types.
 * 
 * @author taubertj
 * 
 */
public class EvidenceTypeMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public EvidenceTypeMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more EvidenceTypes are missing in the metadata.";
	}

}
