package net.sourceforge.ondex.exception.type;

/**
 * Exception type for a producer failing due to a unparseable file.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class ParsingFailedException extends PluginConfigurationException {

	//####FIELDS####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -7465993088291572113L;

	//####CONSTRUCTOR####
	
	/**
	 * standard constructor.
	 */
	public ParsingFailedException() {
		
	}

	/**
	 * constructor with custom message.
	 * @param message the message.
	 */
	public ParsingFailedException(String message) {
		super(message);
	}
	
	public ParsingFailedException(Throwable cause) {
		super(cause); 
	}



	//####METHODS####
}
