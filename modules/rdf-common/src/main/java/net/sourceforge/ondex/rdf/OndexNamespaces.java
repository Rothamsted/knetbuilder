package net.sourceforge.ondex.rdf;

import java.util.HashMap;
import java.util.Map;

import info.marcobrandizi.rdfutils.namespaces.Namespaces;

/**
 * IRI/RDF namespaces related to ONDEX RDF modelling.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Apr 2017</dd></dl>
 *
 */
public class OndexNamespaces implements Namespaces
{
	@Override
	@SuppressWarnings ( "serial" )
	public Map<String, String> getNamespaces ()
	{
		return new HashMap<String, String> ()
		{{
			put ( "bk", "http://www.ondex.org/bioknet/terms/" );
			put ( "bkr", "http://www.ondex.org/bioknet/resources/" );
			put ( "bka", "http://www.ondex.org/bioknet/terms/attributes/" );
			put ( "bkds", "http://www.ondex.org/bioknet/terms/dataSources/" );
			put ( "bkev", "http://www.ondex.org/bioknet/terms/evidences/" );			
		}};
	}
}
