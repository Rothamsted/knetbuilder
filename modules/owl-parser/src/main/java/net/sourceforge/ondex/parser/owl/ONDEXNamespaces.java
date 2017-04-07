package net.sourceforge.ondex.parser.owl;

import java.util.HashMap;
import java.util.Map;

import info.marcobrandizi.rdfutils.namespaces.Namespaces;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Apr 2017</dd></dl>
 *
 */
public class ONDEXNamespaces implements Namespaces
{
	@Override
	@SuppressWarnings ( "serial" )
	public Map<String, String> getNamespaces ()
	{
		return new HashMap<String, String> ()
		{{
			// Just to make tests and examples
			put ( "foo", "http://www.example.com/foo#" );
		}};
	}
}
