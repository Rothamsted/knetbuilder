package net.sourceforge.ondex.parser.owl;

import net.sourceforge.ondex.parser.TextsMapper;

/**
 * <p>Extracts accessions from {@code owl:Axiom} annotations, leveraging {@link OWLAxiomMapper}.</p>
 * 
 * <p>This uses {@link OBOWLAccessionsMapper} and its {@link AccessionValuesMapper}, to which a {@link TextsMapper}
 * of type {@link OWLAxiomMapper} is passed. In practice, this means the same logics of mapping 
 * accessions, prefixes, added prefixes, data sources and evidence is applied to the string values that are extracted
 * by an {@link OWLAxiomMapper}. In even more practical terms, this maps the targets of {@code owl:Axiom} to 
 * ONDEX accessions.</p>
 * 
 * <p>See the Trait Ontology config file for details.</p>
 * 
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
		( (AccessionValuesMapper) this.getAccessionValuesMapper () ).setTextsMapper ( new OWLAxiomMapper () ); 
	}
		
	public String getMappedPropertyIri ()
	{
		return getOWLAxiomMapper ().getMappedPropertyIri ();
	}

	public void setMappedPropertyIri ( String mappedPropertyIri )
	{
		getOWLAxiomMapper ().setMappedPropertyIri ( mappedPropertyIri );
	}
	
	/**
	 * Do some checks+typecasting to return the {@link OWLAxiomMapper} that is assigned to {@link #getAccessionValuesMapper()}
	 * as {@link AccessionValuesMapper#getTextsMapper() texts mapper}. 
	 */
	private OWLAxiomMapper getOWLAxiomMapper () 
	{
		AccessionValuesMapper valMap = (AccessionValuesMapper) this.getAccessionValuesMapper ();
		OWLTextsMapper txtMapper = valMap.getTextsMapper ();
		if ( ! ( txtMapper instanceof OWLAxiomMapper ) ) throw new IllegalStateException (
			String.format ( 
				"Internal error: the %s.accessionValuesMapper must be of type %s", 
				this.getClass ().getSimpleName (),
				OWLAxiomMapper.class.getSimpleName ()
			)
		);
		return (OWLAxiomMapper) txtMapper;		
	}
}
