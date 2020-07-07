package net.sourceforge.ondex.tools.tab.exporter;

/**
 * An exception that is thrown when an n/a attribute is requested from an Ondex entity
 * @author hindlem
 *
 */
public class InvalidOndexEntityException extends Exception {
	/**
	 * 
	 * @param error error message
	 */
	public InvalidOndexEntityException(String error) {
		super(error);
	}

	private static final long serialVersionUID = 1L;
	
}
