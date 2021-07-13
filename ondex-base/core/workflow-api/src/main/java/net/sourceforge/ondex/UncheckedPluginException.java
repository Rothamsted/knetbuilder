package net.sourceforge.ondex;

import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.utils.OndexPluginUtils;

/**
 * Wraps {@link PluginException} with a corresponding unchecked exception. 
 * Used in helpers like {@link OndexPluginUtils#runPlugin(ONDEXPlugin, java.util.Map)}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 May 2021</dd></dl>
 *
 */
public class UncheckedPluginException extends RuntimeException
{
	private static final long serialVersionUID = -4536756862599922592L;

	public UncheckedPluginException ( String message )
	{
		super ( message );
	}

	public UncheckedPluginException ( String message, Throwable cause )
	{
		super ( message, cause );
	}
}
