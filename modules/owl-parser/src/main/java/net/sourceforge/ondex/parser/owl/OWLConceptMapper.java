package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser.SimpleIdMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * Maps an owl:Class to an {@link ONDEXConcept}. This is similar to {@link OWLConceptClassMapper}, except it
 * hasn't any root IRI property, and has {@link #getConceptClassMapper()} instead, to associate every mapped
 * ONDEX concept to a concept class. Moreover, an ONDEX concept has additional attributes (e.g., names) and 
 * further relations (in addition to {@link OwlRecursiveRelMapper} such as {@link OwlSubClassRelMapper}) that are 
 * followed from the concept itself ({@link #getConceptRelationMappers()}).
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLConceptMapper implements ConceptMapper<OntClass>
{
	private OWLConceptClassMapper conceptClassMapper;
	
	private SimpleIdMapper<OntClass> idMapper;
	private SimpleLabelMapper<OntClass> preferredNameMapper;
	private Set<OWLNamesMapper> additionalNameMappers = Collections.emptySet ();
	private SimpleLabelMapper<OntClass> descriptionMapper;
	
	private Set<OWLAccessionsMapper> accessionsMappers = Collections.emptySet ();
	
	private Set<OWLSimpleConceptRelMapper> conceptRelationMappers = Collections.emptySet ();
	

	/**
	 * @see above.
	 */
	@Override
	public ONDEXConcept map ( OntClass ontCls, ONDEXGraph graph )
	{
		OntModel model = ontCls.getOntModel ();
		String conceptId = idMapper.map ( ontCls );
		String description = descriptionMapper.map ( ontCls );
		
		ConceptClass cc = this.conceptClassMapper.map ( model, graph );
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		// TODO: factorise
		EvidenceType evidence = graphw.getEvidenceType ( "IMPD", "IMPD", "" );
		
		// TODO: attach the file?
		DataSource ds = graphw.getDataSource ( DataSourcePrototype.OWL_PARSER );
		
		ONDEXConcept concept = graphw.getConcept ( conceptId, "", description, ds, cc, evidence );

		if ( this.preferredNameMapper != null )
		{
			String clsLabel = preferredNameMapper.map ( ontCls );
			if ( clsLabel != null )
				concept.createConceptName ( clsLabel, true );
		}
		
		ONDEXElemWrapper<ONDEXConcept> conceptw = new ONDEXElemWrapper<ONDEXConcept> ( concept, graph );

		// Additional names
		this.additionalNameMappers
		.stream ()
		.forEach ( mapper -> mapper.map ( ontCls, conceptw ).count () );
				
		// Accessions
		this.accessionsMappers
		.stream ()
		.forEach ( mapper -> mapper.map ( ontCls, conceptw ).count () );

		// typically non sub class relations
		this.conceptRelationMappers
		.stream ()
		.peek ( mapper -> { if ( mapper.getConceptMapper () == null ) mapper.setConceptMapper ( this ); } ) 
		.forEach ( mapper -> mapper.map ( ontCls, conceptw ).count () );
		
		return concept;
	}

	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return conceptClassMapper;
	}

	/**
	 * If this remains null, it's set by the component using it, e.g., @see the {@link OwlRecursiveRelMapper}.
	 */	
	public void setConceptClassMapper ( OWLConceptClassMapper conceptClassMapper )
	{
		this.conceptClassMapper = conceptClassMapper;
	}

	/**
	 * The ID mapper that is used to create an {@link ConceptClass#getId() identifier for the mapped concept class}.
	 * This is usually {@link IRIBasedIdMapper}. 
	 */	
	public SimpleIdMapper<OntClass> getIdMapper ()
	{
		return idMapper;
	}

	public void setIdMapper ( SimpleIdMapper<OntClass> idMapper )
	{
		this.idMapper = idMapper;
	}

	/**
	 * A label mapper that is used to map an RDF literal property of an owl:Class to a preferred name.-
	 * For instance, this might be {@link TextPropertyMapper} configured with rdfs:label.
	 */	
	public SimpleLabelMapper<OntClass> getPreferredNameMapper ()
	{
		return preferredNameMapper;
	}

	public void setPreferredNameMapper ( SimpleLabelMapper<OntClass> nameMapper )
	{
		this.preferredNameMapper = nameMapper;
	}

	/**
	 * Additional name mappers, which work like {@link #getPreferredNameMapper()}.
	 */
	public Set<OWLNamesMapper> getAdditionalNameMappers ()
	{
		return additionalNameMappers;
	}

	public void setAdditionalNameMappers ( Set<OWLNamesMapper> additionalNameMappers )
	{
		this.additionalNameMappers = additionalNameMappers;
	}

	/**
	 * A description mapper that is used to map a literal property of an owl:Class to {@link ConceptClass#getDescription()}-
	 * For instance, this might be {@link TextPropertyMapper} configured with rdfs:comment.
	 */		
	public SimpleLabelMapper<OntClass> getDescriptionMapper ()
	{
		return descriptionMapper;
	}

	public void setDescriptionMapper ( SimpleLabelMapper<OntClass> descriptionMapper )
	{
		this.descriptionMapper = descriptionMapper;
	}

	public Set<OWLAccessionsMapper> getAccessionsMappers ()
	{
		return accessionsMappers;
	}

	public void setAccessionsMappers ( Set<OWLAccessionsMapper> accessionsMappers )
	{
		this.accessionsMappers = accessionsMappers;
	}

	public Set<OWLSimpleConceptRelMapper> getConceptRelationMappers ()
	{
		return conceptRelationMappers;
	}

	public void setConceptRelationMappers ( Set<OWLSimpleConceptRelMapper> conceptRelationMappers )
	{
		this.conceptRelationMappers = conceptRelationMappers;
	}
	
}
