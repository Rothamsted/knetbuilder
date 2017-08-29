package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * a {@link TextMapper} that maps the lexical value of an {@link #getPropertyIri() RDF property} to a string.
 * 
 * This is based on {@link OWLTextsMapper}.  
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 Jul 2017</dd></dl>
 *
 */
public class OWLTextMapper implements TextMapper<OntClass>, RdfPropertyConfigurator
{
	private OWLTextsMapper helper = new OWLTextsMapper ();

	public OWLTextMapper () {}

	@Override
	public String map ( OntClass source, ONDEXGraph graph )
	{
		return helper.map ( source, graph ).findFirst ().orElse ( null );
	}

	@Override
	public String getPropertyIri () {
		return this.helper.getPropertyIri ();
	}

	@Override
	public void setPropertyIri ( String propertyIri ) {
		this.helper.setPropertyIri ( propertyIri );
	}
}
