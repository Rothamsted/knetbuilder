package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser2.AbstractTextsMapper;
import net.sourceforge.ondex.parser2.IdentityScanner;
import net.sourceforge.ondex.parser2.Scanner;
import net.sourceforge.ondex.parser2.TextMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OWLTextsMapper extends AbstractTextsMapper<OntClass, RDFNode> implements RdfPropertyConfigurator
{
	public OWLTextsMapper ()
	{
		super ( new RdfPropertyScanner (), new LiteralMapper () );
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
