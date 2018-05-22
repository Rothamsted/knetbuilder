package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.ConceptClassPrototype;
import net.sourceforge.ondex.parser.ConceptClassMapper;
import net.sourceforge.ondex.parser.DecoratingMapper;
import net.sourceforge.ondex.parser.DefaultConceptClassMapper;
import net.sourceforge.ondex.parser.Mapper;
import net.sourceforge.ondex.parser.TextMapper;

/**
 * Maps an OWL class that is on top of an ontology hierarchy.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class OWLTopConceptClassMapper extends DecoratingMapper<OntClass, ConceptClass>
  implements ConceptClassMapper<OntClass>
{	
	private Set<OntClass> topClasses = null;
	
	/** 
	 * Maps each class to its top concept class. This is cached to make the mapper performant. 
	 * This also makes the mapper stateful, but usually a new instance is created for each new OWL file mapped.
	 * 
	 */ 
	private Map<OntClass, ConceptClass> cache = new HashMap<> ();
	
	/**
	 * Quick hack to report a message once only. This is also stateful information, but see
	 * above.
	 */
	private boolean singleInheritanceReported = false;
	
	private ConceptClassPrototype genericConceptClass = Utils.GENERIC_ONTOLOGY_CONCEPT_CLASS;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	public OWLTopConceptClassMapper ()
	{
		super ( new DefaultConceptClassMapper<> () );
	}
	

	@Override
	public ConceptClass map ( OntClass cls, ONDEXGraph graph )
	{
		OntClass topCls = this.getTopClass ( cls );
		
		if ( topCls == null ) 
		{
			log.debug ( "Assigning the generic concept class to {}", cls.getURI () );
			return this.cache.computeIfAbsent ( 
				cls.getOntModel ().getOntClass ( OWL.Thing.getURI () ), 
				k -> CachedGraphWrapper.getInstance ( graph ).getConceptClass ( this.getGenericConceptClass () )
			);
		}
		
		return this.cache.computeIfAbsent ( topCls, c -> this.getMyBaseMapper ().map ( c, graph ) );
	}

	
	private OntClass getTopClass ( OntClass cls )
	{
		if ( this.getTopClasses ().contains ( cls ) ) return cls;
		if ( OWL.Thing.equals ( cls ) ) return null;
		
		Set<OntClass> parents = JENAUTILS.toStream ( cls.listSuperClasses ( true ) )
		.collect ( Collectors.toSet () );
		
		if ( parents.size () > 1 )
		{
			String clsUri = cls.getURI ();
			log.trace ( 
				  "The class <{}> has multiple parents, you might get unexpected concept class mappings, consider using a single "
				+ "constant concept class to represent all the terms of this ontology (using ConstantConceptClassMapper)",
				clsUri
			);
			if ( !this.singleInheritanceReported )
				log.warn (
					"The ontology has classes with multiple parents, (e.g., <{}>). you might get unexpected concept class "
				+ "mappings, consider using a single constant concept class to represent all the terms of this ontology "
				+ "(using ConstantConceptClassMapper)",
				clsUri
			);
			this.singleInheritanceReported = true;
		}

		for ( OntClass parent: parents )
		{
			OntClass result = this.getTopClass ( parent );
			if ( result != null ) return result;
		}
		return null;
	}
	
	public Set<OntClass> getTopClasses ()
	{
		return topClasses;
	}

	public void setTopClasses ( Set<OntClass> topClasses )
	{
		this.topClasses = topClasses;
	}
	
	public void setTopClassIris ( String... iris )
	{
		Set<OntClass> clss = new HashSet<> ();
		OntModel model = ModelFactory.createOntologyModel ( OntModelSpec.OWL_MEM );
		
		if ( iris != null ) for ( String iri: iris)
			clss.add ( model.createClass ( iri ) );
		
		this.setTopClasses ( clss );
	}
	
	public ConceptClassPrototype getGenericConceptClass ()
	{
		return genericConceptClass;
	}

	public void setGenericConceptClass ( ConceptClassPrototype genericConceptClass )
	{
		this.genericConceptClass = genericConceptClass;
	}

	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	private DefaultConceptClassMapper<OntClass> getMyBaseMapper ()
	{
		return (DefaultConceptClassMapper<OntClass>) (Mapper) this.baseMapper;
	}
	
	
	public TextMapper<OntClass> getIdMapper ()
	{
		return this.getMyBaseMapper().getIdMapper ();
	}

	public void setIdMapper ( TextMapper<OntClass> idMapper )
	{
		this.getMyBaseMapper().setIdMapper ( idMapper );
	}

	public TextMapper<OntClass> getFullNameMapper ()
	{
		return this.getMyBaseMapper().getFullNameMapper ();
	}

	public void setFullNameMapper ( TextMapper<OntClass> fullNameMapper )
	{
		this.getMyBaseMapper().setFullNameMapper ( fullNameMapper );
	}

	public TextMapper<OntClass> getDescriptionMapper ()
	{
		return this.getMyBaseMapper().getDescriptionMapper ();
	}

	public void setDescriptionMapper ( TextMapper<OntClass> descriptionMapper )
	{
		this.getMyBaseMapper().setDescriptionMapper ( descriptionMapper );
	}

	public ConceptClassMapper<OntClass> getParentMapper ()
	{
		return this.getMyBaseMapper().getParentMapper ();
	}

	public void setParentMapper ( ConceptClassMapper<OntClass> parentMapper )
	{
		this.getMyBaseMapper().setParentMapper ( parentMapper );
	}
	
}
