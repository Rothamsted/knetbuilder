package net.sourceforge.ondex.event.type;

/**
 * EventType for missing DataSource.
 * 
 * @author taubertj
 * 
 */
public class DataSourceMissingEvent extends EventType {

	/**
	 * Constructor for a customized message with extension.
	 * 
	 * @param message
	 *            String
	 * @param extension
	 *            String
	 */
	public DataSourceMissingEvent(String message, String extension) {
		super(message, extension);
		super.desc = "One or more DataSources are missing in the metadata.";
	}

}
