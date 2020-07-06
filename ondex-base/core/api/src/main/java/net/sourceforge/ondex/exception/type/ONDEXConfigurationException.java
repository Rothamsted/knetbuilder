package net.sourceforge.ondex.exception.type;

/**
 * Thrown when the base configuration of Ondex is corrupted.
 * This most probably indicates a problem with the configuration files, 
 * the plugins directory or the data directory.
 * @author jweile
 *
 */
public class ONDEXConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ONDEXConfigurationException() {
		super();
	}

	public ONDEXConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ONDEXConfigurationException(String message) {
		super(message);
	}

	public ONDEXConfigurationException(Throwable cause) {
		super(cause);
	}

}
