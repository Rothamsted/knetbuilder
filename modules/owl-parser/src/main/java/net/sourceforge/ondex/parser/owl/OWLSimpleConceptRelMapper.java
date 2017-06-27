package net.sourceforge.ondex.parser.owl;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.utils.CachedGraphWrapper;
import net.sourceforge.ondex.core.utils.ONDEXElemWrapper;

/**
 * 
 * Maps a relation that a concept might have with other concepts.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 May 2017</dd></dl>
 *
 */
public abstract class OWLSimpleConceptRelMapper extends OWLRelMapper<OntClass, ONDEXElemWrapper<ONDEXConcept>>
{
	/**
	 * The fromOntCls was mapped to fromConceptWrp and we're passing both here because they're both needed to
	 * build the final relation.
	 *  
	 */
	@Override
	public Stream<ONDEXRelation> map ( OntClass fromOntCls, ONDEXElemWrapper<ONDEXConcept> fromConceptWrp )
	{
		ONDEXConcept concept = fromConceptWrp.getElement ();
		ONDEXGraph graph = fromConceptWrp.getGraph ();
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		
		RelationType relType = graphw.getRelationType ( this.getRelationTypePrototype () );
		EvidenceType evidence = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
		
		return this.getRelatedClasses ( fromOntCls )
		.map ( targetOntCls -> CachedGraphWrapper.getInstance ( graph ).getRelation ( 
			concept, conceptMapper.map ( targetOntCls, ONDEXElemWrapper.of ( concept.getOfType (), graph ) ), relType, evidence 
		));
	}	
	
	protected abstract Stream<OntClass> getRelatedClasses ( OntClass fromCls );

}
