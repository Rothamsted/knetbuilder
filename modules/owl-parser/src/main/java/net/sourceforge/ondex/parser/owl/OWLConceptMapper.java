package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.parser.ConceptMapper;
import net.sourceforge.ondex.parser.SimpleIdMapper;
import net.sourceforge.ondex.parser.SimpleLabelMapper;

/**
 * Maps an owl:Class to an {@link ONDEXConcept}. This is similar to {@link OWLConceptClassMapper}, except it
 * hasn't any root IRI property, and has {@link #getConceptClassMapper()} instead, to associate every mapped
 * ONDEX concept to a concept class. 
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
public class OWLConceptMapper implements ConceptMapper<OntClass>
{
	private OWLConceptClassMapper conceptClassMapper;
	
	private SimpleIdMapper<OntClass> idMapper;
	private SimpleLabelMapper<OntClass> labelMapper;
	private SimpleLabelMapper<OntClass> descriptionMapper;

	/**
	 * @see above.
	 */
	@Override
	public ONDEXConcept map ( OntClass ontCls, ONDEXGraph graph )
	{
		OntModel model = ontCls.getOntModel ();
		String conceptId = idMapper.map ( ontCls );
		String clsLabel = labelMapper.map ( ontCls );
		String description = descriptionMapper.map ( ontCls );
		
		ConceptClass cc = this.conceptClassMapper.map ( model, graph );
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		// TODO: what is it?!
		EvidenceType evidence = graphw.getEvidenceType ( "IMPD", "IMPD", "" );
		
		// TODO: attach the file?
		DataSource ds = graphw.getDataSource ( "owlParser", "The OWL Parser", "" );
		
		ONDEXConcept concept = graphw.getConcept ( conceptId, "", description, ds, cc, evidence );
		concept.createConceptName ( clsLabel, true );
		concept.createConceptAccession ( conceptId, ds, false );
		return concept;
	}

	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return conceptClassMapper;
	}

	/**
	 * If this remains null, it's set by the component using it, e.g., @see the {@link OwlSubClassRelMapper}.
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
	 * A label mapper that is used to map a literal property of an owl:Class to {@link ConceptClass#getFullname()}-
	 * For instance, this might be {@link TextPropertyMapper} configured with rdfs:label.
	 */	
	public SimpleLabelMapper<OntClass> getLabelMapper ()
	{
		return labelMapper;
	}

	public void setLabelMapper ( SimpleLabelMapper<OntClass> labelMapper )
	{
		this.labelMapper = labelMapper;
	}

	/**
	 * A label mapper that is used to map a literal property of an owl:Class to {@link ConceptClass#getDescription()}-
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
	
}
