package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.TextMapper;
import uk.ac.ebi.utils.ids.IdUtils;

/**
 * An identifier mapper that is based on the extraction of the last part of an IRI/URI. For instance, it gets 
 * ExClass from http://www.example.com/example#ExClass.
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Apr 2017</dd></dl>
 *
 */
public class IRIBasedIdMapper implements TextMapper<OntClass>
{
	private String splitRegEx = "[#/]";
	
	@Override
	public String map ( OntClass ontCls, ONDEXGraph graph )
	{
		// TODO: null
		return IdUtils.iri2id ( ontCls.getURI (), splitRegEx );
	}

	/**
	 * The RE used with {@link String#split(String)}, i.e., the last fragment of the URI after a fragment that matches
	 * this expression is taken as mapped ID.  
	 */
	public String getSplitRegEx ()
	{
		return splitRegEx;
	}


	public void setSplitRegEx ( String splitRegEx )
	{
		this.splitRegEx = splitRegEx;
	}
	
}
