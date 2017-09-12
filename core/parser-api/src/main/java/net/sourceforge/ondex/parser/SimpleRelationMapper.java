package net.sourceforge.ondex.parser;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.CachedGraphWrapper;
import net.sourceforge.ondex.core.util.prototypes.EvidenceTypePrototype;
import net.sourceforge.ondex.core.util.prototypes.RelationTypePrototype;

/**
 * A simple {@link ConceptBasedRelMapper} uses a {@link RelationTypePrototype constant relation type} to 
 * {@link #map(ONDEXConcept, ONDEXConcept, ONDEXGraph) build ONDEX relations between the inputs it receives}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class SimpleRelationMapper implements ConceptBasedRelMapper
{
	private RelationTypePrototype relationTypePrototype;
	private EvidenceTypePrototype evidenceTypePrototype = EvidenceTypePrototype.IMPD;

	@Override
	public ONDEXRelation map ( ONDEXConcept c1, ONDEXConcept c2, ONDEXGraph graph )
	{
		CachedGraphWrapper graphw = CachedGraphWrapper.getInstance ( graph );
		
		RelationType relType = graphw.getRelationType ( this.getRelationTypePrototype () );
		EvidenceType evidenceType = graphw.getEvidenceType ( this.getEvidenceTypePrototype () );
				
		return graphw.getRelation ( c1, c2, relType, evidenceType );
	}

	public RelationTypePrototype getRelationTypePrototype ()
	{
		return relationTypePrototype;
	}

	public void setRelationTypePrototype ( RelationTypePrototype relationTypePrototype )
	{
		this.relationTypePrototype = relationTypePrototype;
	}

	public EvidenceTypePrototype getEvidenceTypePrototype ()
	{
		return evidenceTypePrototype;
	}

	public void setEvidenceTypePrototype ( EvidenceTypePrototype evidenceTypePrototype )
	{
		this.evidenceTypePrototype = evidenceTypePrototype;
	}

}
