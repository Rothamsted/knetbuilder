package net.sourceforge.ondex.exception.type;

/**
 * Exception type for missing concept class.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class ConceptClassMissingException extends MetaDataMissingException {

	//####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7665712795810115303L;

	//####CONSTRUCTOR####

	/**
	 * standard constructor.
	 */
	public ConceptClassMissingException() {
	}

	/**
	 * constructor for custom message.
	 * 
	 * @param message the message.
	 */
	public ConceptClassMissingException(String message) {
		super(message);
	}

	public ConceptClassMissingException ( String message, Throwable cause )
	{
		super ( message, cause );
	}
	
	//####METHODS####
}
