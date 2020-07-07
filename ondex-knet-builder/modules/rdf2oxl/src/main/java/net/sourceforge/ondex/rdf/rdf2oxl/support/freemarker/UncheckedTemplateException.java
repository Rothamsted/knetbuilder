package net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker;

import freemarker.template.TemplateException;

/**
 * Unchecked variant of {@link TemplateException} used by FreeMarker.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@SuppressWarnings ( "serial" )
public class UncheckedTemplateException extends RuntimeException
{
	public UncheckedTemplateException ( String message, TemplateException cause )
	{
		super ( message, cause );
	}	
}
