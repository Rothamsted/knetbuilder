package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser.AbstractTextsMapper;
import net.sourceforge.ondex.parser.Scanner;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * The OWL texts mapper. This maps a literal property associated to a class to a string. 
 * 
 * This is done via {@link RdfPropertyScanner} and {@link LiteralMapper}. This mapper needs the 
 * {@link #getPropertyIri() property URI} to be used to fetch literals.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OWLTextsMapper extends AbstractTextsMapper<OntClass, RDFNode> implements RdfPropertyConfigurator
{
	protected OWLTextsMapper ( Scanner<OntClass, RDFNode> scanner, TextMapper<RDFNode> sourceItemMapper )
	{
		super ( scanner, sourceItemMapper );
	}

	public OWLTextsMapper () {
		this ( new RdfPropertyScanner (), new LiteralMapper () );
	}

	@Override
	public String getPropertyIri () {
		return ( (RdfPropertyScanner) getScanner () ).getPropertyIri ();
	}

	@Override
	public void setPropertyIri ( String propertyIri ) {
		( (RdfPropertyScanner) getScanner () ).setPropertyIri ( propertyIri );		
	}
}
