package net.sourceforge.ondex.exception.type;

/**
 * Exception type for wrong producer configurations.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class PluginConfigurationException extends PluginException {

	// ####FIELDS####

	/**
	 * serial version id.
	 */
	private static final long serialVersionUID = -412356132322235963L;

    public PluginConfigurationException()
    {
    }

    public PluginConfigurationException(String s)
    {
        super(s);
    }

    public PluginConfigurationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public PluginConfigurationException(Throwable throwable)
    {
        super(throwable);
    }
}
