package net.sourceforge.ondex.parser.owl;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * The recursive mapper. This starts from a top level OWL class, which is mapped as a concept class 
 * ({@link #getConceptClassMapper()}), and recursively follow some relation (e.g., rdfs:subClassOf) downwards, 
 * invoking {@link #getConceptMapper()} for all new OWL classes that are visited.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 May 2017</dd></dl>
 *
 */
public abstract class OwlRecursiveRelMapper extends OWLRelMapper<OntModel, ONDEXGraph>
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	private TopClassesProvider topClassesProvider = new OwlRootClassesProvider ();
	private boolean doMapRootsToConcepts = true;

	private OWLConceptClassMapper conceptClassMapper;
	
	public OwlRecursiveRelMapper ()
	{
		super ();
	}

	
	/**
	 * @see above.
	 */
	public Stream<ONDEXRelation> map ( OntModel model, ONDEXGraph graph ) 
	{		
		// The top ontology class is a concept class, ontology subclasses are instances of the top class, 
		// all other descendants are instances too, plus they have subclass relations, but they don't have the subclass
		// relation to the top class (which isn't even a concept)
		//
		TopClassesProvider topProv = this.getTopClassesProvider ();

		Iterator<OntClass> topItr = topProv.apply ( model );
		if ( !topItr.hasNext () )
		{
			log.warn ( "The top classes provider '' doesn't return anything", topItr.getClass ().getName () );
			return Stream.empty ();
		}
		
		@SuppressWarnings ( "unchecked" )
		Stream<ONDEXRelation> result[] = new Stream [] { Stream.empty () };
		
		topProv.apply ( model ).forEachRemaining ( topOntCls -> 
		{
			String uri = topOntCls.getURI ();
			if ( uri == null ) return;
			
			log.info ( "Scanning from the top class <{}>", uri );
			
			ConceptClass cc = this.getConceptClassMapper ().map ( topOntCls, graph );
			ONDEXElemWrapper<ConceptClass> ccw = ONDEXElemWrapper.of ( cc, graph );
			
			result [ 0 ] = this.isDoMapRootsToConcepts () 
				? this.map ( topOntCls, ccw )
				: Stream.concat ( 
						result [ 0 ], 
						this
						.getRelatedClasses ( topOntCls )	
						.flatMap ( ontChild -> this.map ( ontChild, ccw ) )
					);
		});

		return result [ 0 ];
	}
	
	/**
	 * Manages the recursion described above from the subtree rooted at rootCls. 
	 */
	protected Stream<ONDEXRelation> map ( OntClass rootCls, ONDEXElemWrapper<ConceptClass> ccw )
	{
		ONDEXGraph graph = ccw.getGraph ();
		
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		RelationType relType = graphw.getRelationType ( this.getRelationTypePrototype () );
		EvidenceType evidence = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
		
		ONDEXConcept rootConcept = conceptMapper.map ( rootCls, ccw );
				
		// First get all the links at this hierarchical level
		Stream<ONDEXRelation> result =
			this.getRelatedClasses ( rootCls )
			.map ( child ->
			{
				synchronized ( this ) {
					return CachedGraphWrapper.getInstance ( graph ).getRelation ( 
						conceptMapper.map ( child, ccw ), rootConcept, relType, evidence 
					);
				}
			});
		
		// Next, recurse and add up more 
		for ( OntClass child: (Iterable<OntClass>) () -> this.getRelatedClasses ( rootCls ).iterator () )
			result = Stream.concat ( result, this.map ( child, ccw ) );
		
		return result;
	}
	
	/**
	 * At each new node met during the {@link #map(OntClass, ONDEXGraph) recursive visit} of this mapper, new 
	 * (typically downward) OWL classes to follow are told by this method. It's also up to it to avoid circular paths and 
	 * infinite loops. Normally, OWL ontologies don't have this problems on explicit relations like owl:subClassOf.   
	 */
	protected abstract Stream<OntClass> getRelatedClasses ( OntClass fromCls );

	/**
	 * This determines the strategy used to pick up the root classes that the mapper should tart from. 
	 */
	public TopClassesProvider getTopClassesProvider ()
	{
		return topClassesProvider;
	}


	public void setTopClassesProvider ( TopClassesProvider topClassesProvider )
	{
		this.topClassesProvider = topClassesProvider;
	}

	/**
	 * If this is true, root OWL classes selected by the {@link #getTopClassesProvider() root classes provider}, will be
	 * mapped to concepts, possibly in addition to being mapped to concept classes (this depends on {@link #getConceptClassMapper()}).
	 */
	public boolean isDoMapRootsToConcepts ()
	{
		return doMapRootsToConcepts;
	}


	public void setDoMapRootsToConcepts ( boolean doMapRootsToConcepts )
	{
		this.doMapRootsToConcepts = doMapRootsToConcepts;
	}


	public OWLConceptClassMapper getConceptClassMapper ()
	{
		return conceptClassMapper;
	}


	public void setConceptClassMapper ( OWLConceptClassMapper conceptClassMapper )
	{
		this.conceptClassMapper = conceptClassMapper;
	}
	
}
