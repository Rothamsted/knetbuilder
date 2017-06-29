package net.sourceforge.ondex.parser.owl;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.DataSourcePrototype;
import net.sourceforge.ondex.core.utils.EvidenceTypePrototype;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser.SimpleIdMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * Maps an owl:Class to an {@link ONDEXConcept}. Uses several helper mappers to map concept elements such as
 * names or accessions. They can be configured via Spring (see existing XMLs).
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLConceptMapper implements ConceptMapper<OntClass>
{
	private SimpleIdMapper<OntClass> idMapper;
	private SimpleLabelMapper<OntClass> descriptionMapper;

	private SimpleLabelMapper<OntClass> preferredNameMapper;
	private Set<OWLNamesMapper> additionalNameMappers = Collections.emptySet ();
	private Set<OWLAccessionsMapper> accessionsMappers = Collections.emptySet ();
	
	private Set<OWLSimpleConceptRelMapper> conceptRelationMappers = Collections.emptySet ();
	
	private EvidenceTypePrototype evidenceTypePrototype = EvidenceTypePrototype.IMPD;
	
	private DataSourcePrototype dataSourcePrototype = DataSourcePrototype.OWL_PARSER;
	
	
	/**
	 * @see above.
	 */
	@Override
	public ONDEXConcept map ( OntClass ontCls, ONDEXElemWrapper<ConceptClass> ccw )
	{		
		String conceptId = idMapper.map ( ontCls );
		String description = StringUtils.trimToEmpty ( descriptionMapper.map ( ontCls ) );
		
		ConceptClass cc = ccw.getElement ();
		ONDEXGraph graph = ccw.getGraph ();
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		EvidenceType evidence = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
		DataSource ds = graphw.getDataSource ( this.getDataSourcePrototype () );
		
		ONDEXConcept concept = graphw.getConcept ( conceptId );

		// We've already visited this, so let's stop here and let's prevent loops
		if ( concept != null ) return concept;
		
		// This will certainly create it, a bit redundant, but it's OK.
		concept = graphw.getConcept ( conceptId, "", description, ds, cc, evidence );

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

	/**
	 * Defaults to {@link EvidenceTypePrototype#IMPD}
	 */
	public EvidenceTypePrototype getEvidenceTypePrototype ()
	{
		return evidenceTypePrototype;
	}

	public void setEvidenceTypePrototype ( EvidenceTypePrototype evidenceTypePrototype )
	{
		this.evidenceTypePrototype = evidenceTypePrototype;
	}

	/**
	 * Defaults to {@link DataSourcePrototype#OWL_PARSER}
	 */
	public DataSourcePrototype getDataSourcePrototype ()
	{
		return dataSourcePrototype;
	}

	public void setDataSourcePrototype ( DataSourcePrototype dataSourcePrototype )
	{
		this.dataSourcePrototype = dataSourcePrototype;
	}

}
