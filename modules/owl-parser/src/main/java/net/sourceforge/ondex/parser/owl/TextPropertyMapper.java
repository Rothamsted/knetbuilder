package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser.SimpleDescriptionMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * Maps a literal property (eg, rdfs:label) of OWL classes to a string label. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Apr 2017</dd></dl>
 *
 */
public class TextPropertyMapper implements SimpleLabelMapper<OntClass>, SimpleDescriptionMapper<OntClass>
{
	private String propertyIri;

	/**
	 * The parameter must have a single value for the property {@link #getPropertyIri()}, else the result is undetermined.
	 */
	@Override
	public String map ( OntClass ontCls )
	{
		OntModel model = ontCls.getOntModel ();
		RDFNode nval = ontCls.getPropertyValue ( model.getProperty ( propertyIri ) );
		if ( nval == null || !nval.canAs ( Literal.class ) ) return "";
		return nval.asLiteral ().getLexicalForm ();
	}

	/**
	 * The property that this mapper deals with. Examples are rdfs:label, rdfs:comment, skos:label.
	 */
	public String getPropertyIri ()
	{
		return propertyIri;
	}

	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}	
}
