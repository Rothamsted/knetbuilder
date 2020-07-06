package net.sourceforge.ondex.exception.type;

/**
 * Exception type for a failing producer.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class PluginException extends Exception {

	//####FIELDS####
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -5795104036749009479L;


    public PluginException()
    {
    }

    public PluginException(String s)
    {
        super(s);
    }

    public PluginException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public PluginException(Throwable throwable)
    {
        super(throwable);
    }
}
