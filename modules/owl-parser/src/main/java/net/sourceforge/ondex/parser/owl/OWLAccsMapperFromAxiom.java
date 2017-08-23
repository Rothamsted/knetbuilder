package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.parser2.TextsMapper;

/**
 * 
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Aug 2017</dd></dl>
 *
 */
public class OWLAccsMapperFromAxiom extends OBOWLAccessionsMapper
{
	public OWLAccsMapperFromAxiom ()
	{
		super ();
		this.setAccessionValuesMapper ( new OWLAxiomMapper () );
	}
	
	@Override
	public void setAccessionValuesMapper ( TextsMapper<OntClass> accessionValuesMapper ) {
		throw new IllegalArgumentException ( this.getClass ().getName () + " requires a OWLAxiomMapper as accessions mapper" );
	}

	protected void setAccessionValuesMapper ( OWLAxiomMapper accessionValuesMapper )
	{
		super.setAccessionValuesMapper ( accessionValuesMapper );
	}
	
	public String getMappedPropertyIri ()
	{
		return ( (OWLAxiomMapper) this.getAccessionValuesMapper () ).getPropertyIri ();
	}

	public void setMappedPropertyIri ( String mappedPropertyIri )
	{
		( (OWLAxiomMapper) this.getAccessionValuesMapper () ).setPropertyIri ( mappedPropertyIri );
	}	
}
