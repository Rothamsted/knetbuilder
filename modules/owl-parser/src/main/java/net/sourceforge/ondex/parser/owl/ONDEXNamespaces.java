package net.sourceforge.ondex.parser.owl;

import java.util.HashMap;
import java.util.Map;

import info.marcobrandizi.rdfutils.namespaces.Namespaces;

/**
 * ONDEX-related IRI/RDF namespaces.
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
			put ( "obo", "http://purl.obolibrary.org/obo/" );
			put ( "oboInOwl", "http://www.geneontology.org/formats/oboInOwl#" );
		}};
	}
}
