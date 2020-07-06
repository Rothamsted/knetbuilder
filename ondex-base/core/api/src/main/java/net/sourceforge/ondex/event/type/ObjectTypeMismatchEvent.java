package net.sourceforge.ondex.event.type;

/**
 * EventType for object type mismatch errors.
 * 
 * @author taubertj
 * 
 */
public class ObjectTypeMismatchEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public ObjectTypeMismatchEvent(String message, String extension) {
		super(message, extension);
		super.desc = "Object assigned was not of expected type.";
	}

}
