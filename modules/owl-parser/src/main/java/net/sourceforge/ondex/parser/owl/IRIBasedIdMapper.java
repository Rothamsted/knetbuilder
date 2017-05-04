package net.sourceforge.ondex.parser.owl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser.SimpleIdMapper;

/**
 * An identifier mapper that is based on the extraction of the last part of an IRI/URI. For instance, it gets 
 * ExClass from http://www.example.com/example#ExClass.
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Apr 2017</dd></dl>
 *
 */
public class IRIBasedIdMapper implements SimpleIdMapper<OntClass>
{
	private String splitRegEx = "[#/]";
	
	@Override
	public String map ( OntClass ontCls )
	{
		// TODO: null
		String iri= ontCls.getURI (); 
		try
		{
			String[] frags = iri.split ( splitRegEx );
			String lastFrag = frags [ frags.length - 1 ];
			lastFrag = URLDecoder.decode ( lastFrag, "UTF-8" );
			lastFrag = lastFrag.replaceAll ( "[\\s\\+\\-\\:\\.\\?]", "_" );
			return lastFrag;
		}
		catch ( UnsupportedEncodingException ex ) {
			throw new RuntimeException ( String.format ( 
				"Internal error while extracting an ID from the uri <%s>: %s", iri, ex.getMessage ()
			), ex );
		}
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
