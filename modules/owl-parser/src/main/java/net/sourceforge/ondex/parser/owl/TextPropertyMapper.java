package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.parser.SimpleDescriptionMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * Maps a literal property (eg, rdfs:label) of OWL classes to a string label. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Apr 2017</dd></dl>
 *
 */
public class TextPropertyMapper 
	implements SimpleLabelMapper<OntClass>, SimpleDescriptionMapper<OntClass>
{
	protected String propertyIri;
	
	/**
	 * The parameter must have a single value for the property {@link #getPropertyIri()}, else the result is undetermined.
	 */
	@Override
	public String map ( OntClass ontCls )
	{
		OntModel model = ontCls.getOntModel ();
		return JENAUTILS
			.getObject ( model, ontCls, model.getProperty ( getPropertyIri() ), true )
			.flatMap ( JENAUTILS::literal2Value )
			.orElse ( null );
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
