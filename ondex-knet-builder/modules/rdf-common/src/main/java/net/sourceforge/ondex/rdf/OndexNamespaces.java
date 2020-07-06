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
			put ( "bk", "http://knetminer.org/data/rdf/terms/biokno/" );
			put ( "bkr", "http://knetminer.org/data/rdf/resources/" );
			put ( "bka", "http://knetminer.org/data/rdf/terms/biokno/attributes/" );
			put ( "bkds", "http://knetminer.org/data/rdf/terms/biokno/dataSources/" );
			put ( "bkev", "http://knetminer.org/data/rdf/terms/biokno/evidences/" );			
		}};
	}
}
