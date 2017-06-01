package net.sourceforge.ondex.parser.owl;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;

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
	
	public OwlRecursiveRelMapper ()
	{
		super ();
	}

	/**
	 * @see above.
	 */
	public Stream<ONDEXRelation> map ( OntModel model, ONDEXGraph graph ) 
	{
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		if ( conceptMapper.getConceptClassMapper () == null ) 
			conceptMapper.setConceptClassMapper ( this.getConceptClassMapper () );
		
		// The top ontology class is a concept class, ontology subclasses are instances of the top class, 
		// all other descendants are instances too, plus they have subclass relations, but they don't have the suclass
		// relation to the top class (which isn't even a concept)
		//
		String topClsIri = this.getConceptClassMapper ().getClassIri ();
		OntClass topOntCls = model.getOntClass ( topClsIri );
		if ( topOntCls == null )
		{
			log.warn ( "No subclass found for top class <{}>", topClsIri );
			return Stream.empty ();
		}

		log.info ( "Scanning from the top class <{}>", topClsIri );
		
		return
			this
			.getRelatedClasses ( topOntCls )	
			.flatMap ( ontChild -> this.map ( ontChild, graph ) );
	}
	
	/**
	 * Manages the recursion described above from the subtree rooted at rootCls. 
	 */
	protected Stream<ONDEXRelation> map ( OntClass rootCls, ONDEXGraph graph )
	{
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		RelationType relType = graphw.getRelationType ( this.getRelationTypePrototype () );
		EvidenceType evidence = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
		
		ONDEXConcept rootConcept = conceptMapper.map ( rootCls, graph );

				
		// First get all the links at this hierarchical level
		Stream<ONDEXRelation> result =
			this.getRelatedClasses ( rootCls )
			.map ( child -> CachedGraphWrapper.getInstance ( graph ).getRelation ( 
				conceptMapper.map ( child, graph ), rootConcept, relType, evidence 
			));
		
		// Next, recurse and add up more 
		for ( OntClass child: (Iterable<OntClass>) () -> this.getRelatedClasses ( rootCls ).iterator () )
			result = Stream.concat ( result, this.map ( child, graph ) );
		
		return result;
	}

	/**
	 * At each new node met during the {@link #map(OntClass, ONDEXGraph) recursive visit} of this mapper, new 
	 * (typically downward) OWL classes to follow are told by this method. It's also up to it to avoid circular paths and 
	 * infinite loops. Normally, OWL ontologies don't have this problems on explicit relations like owl:subClassOf.   
	 */
	protected abstract Stream<OntClass> getRelatedClasses ( OntClass fromCls );
}
