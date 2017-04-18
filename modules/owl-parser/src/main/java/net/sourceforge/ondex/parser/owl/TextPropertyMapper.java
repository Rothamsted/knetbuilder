package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

import net.sourceforge.ondex.parser.SimpleDescriptionMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Apr 2017</dd></dl>
 *
 */
public class TextPropertyMapper implements SimpleLabelMapper<OntClass>, SimpleDescriptionMapper<OntClass>
{
	private String propertyIri;

	@Override
	public String map ( OntClass ontCls )
	{
		OntModel model = ontCls.getOntModel ();
		RDFNode nval = ontCls.getPropertyValue ( model.getProperty ( propertyIri ) );
		if ( nval == null || !nval.canAs ( Literal.class ) ) return "";
		return nval.asLiteral ().getLexicalForm ();
	}

	public String getPropertyIri ()
	{
		return propertyIri;
	}

	public void setPropertyIri ( String propertyIri )
	{
		this.propertyIri = propertyIri;
	}	
}
