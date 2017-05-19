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
 * TODO: comment me!
 *
 * TODO: add the class mentioned in equivalent.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 May 2017</dd></dl>
 *
 */
public abstract class OWLSimpleConceptRelMapper extends OWLRelMapper<OntClass, ONDEXElemWrapper<ONDEXConcept>>
{
	@Override
	public Stream<ONDEXRelation> map ( OntClass ontCls, ONDEXElemWrapper<ONDEXConcept> conceptw )
	{
		ONDEXConcept concept = conceptw.getElement ();
		ONDEXGraph graph = conceptw.getGraph ();
		
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		OWLConceptMapper conceptMapper = this.getConceptMapper ();
		if ( conceptMapper.getConceptClassMapper () == null ) 
			conceptMapper.setConceptClassMapper ( this.getConceptClassMapper () );		
		
		RelationType relType = graphw.getRelationType ( this.getRelationTypePrototype () );
		EvidenceType evidence = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
	
		return this.getRelatedClasses ( ontCls )
		.map ( targetOntCls -> CachedGraphWrapper.getInstance ( graph ).getRelation ( 
			concept, conceptMapper.map ( targetOntCls, graph ), relType, evidence 
		));
	}	
	
	protected abstract Stream<OntClass> getRelatedClasses ( OntClass fromCls );

}
