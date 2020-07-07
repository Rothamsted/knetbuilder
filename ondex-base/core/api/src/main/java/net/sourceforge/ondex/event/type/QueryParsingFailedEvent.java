package net.sourceforge.ondex.event.type;

/**
 * EventType for query parsing errors.
 * 
 * @author taubertj
 * 
 */
public class QueryParsingFailedEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public QueryParsingFailedEvent(String message, String extension) {
		super(message, extension);
		super.desc = "The parsing of a query failed.";
	}

}
