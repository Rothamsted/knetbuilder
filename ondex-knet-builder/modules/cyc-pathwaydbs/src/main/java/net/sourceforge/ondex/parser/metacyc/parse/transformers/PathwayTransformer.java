package net.sourceforge.ondex.parser.metacyc.parse.transformers;

import java.util.Iterator;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.metacyc.MetaData;
import net.sourceforge.ondex.parser.metacyc.Parser;
import net.sourceforge.ondex.parser.metacyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.metacyc.objects.ECNumber;
import net.sourceforge.ondex.parser.metacyc.objects.Pathway;
/**
 * Transforms a net.sourceforge.ondex.parser.metacyc.sink.Pathway to a Concept.
 * @author peschr
 */
public class PathwayTransformer extends AbstractTransformer {
	private ConceptClass ccPathway;
	private RelationType rtMemberIsPart = null;
	private RelationType rtPrecededBy = null;
	ONDEXConcept c;
	
	public PathwayTransformer(Parser parser) {
		super(parser);
		ccPathway = graph.getMetaData().getConceptClass(MetaData.CC_PATHWAY);
		if (ccPathway == null) {
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(MetaData.CC_PATHWAY, Parser.getCurrentMethodName()));
		}
		rtMemberIsPart = graph.getMetaData()
			.getRelationType(MetaData.RT_MEMBER_IS_PART_OF);
		if (rtMemberIsPart == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(MetaData.RT_MEMBER_IS_PART_OF, Parser.getCurrentMethodName()));
		}
		rtPrecededBy = graph.getMetaData()
			.getRelationType(MetaData.RT_PRECEDED_BY);
		if (rtPrecededBy == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(MetaData.RT_PRECEDED_BY, Parser.getCurrentMethodName()));
		}
	}

	@Override
	public void nodeToConcept(AbstractNode node) {
		Pathway pathway = (Pathway) node;
		ONDEXConcept concept = graph.getFactory().createConcept(pathway.getUniqueId(), dataSourceMetaC,
				ccPathway, etIMPD);
		concept.addTag(concept);
	
		pathway.setConcept(concept);
	}

	@Override
	public void pointerToRelation(AbstractNode node) {
		Pathway pathway = (Pathway) node;
		Iterator<Pathway> i= pathway.getPathwayLinks().iterator();
		while(i.hasNext()){
			Pathway pathwayNode = i.next();
			if ( pathway.isSuperPathWay())
				pathwayNode.setBelongsToSuperPathway(true);
			pathwayNode.getConcept().addTag(node.getConcept());
			graph.getFactory().createRelation(pathway.getConcept(),pathwayNode.getConcept(), rtPrecededBy, etIMPD).addTag(node.getConcept());
		}

		i= pathway.getSubPathway().iterator();
		while(i.hasNext()){
			Pathway pathwayNode = i.next();
			if ( pathway.isSuperPathWay())
				pathwayNode.setBelongsToSuperPathway(true);
			if ( !pathwayNode.getConcept().getTags().contains(node.getConcept()))
				pathwayNode.getConcept().addTag(node.getConcept());
			graph.getFactory().createRelation(pathwayNode.getConcept(),pathway.getConcept(), rtMemberIsPart, etIMPD).addTag(node.getConcept());
		}
	
		for(ECNumber ec:pathway.getEcNumbers()) {
			graph.getFactory().createRelation(ec.getConcept(),pathway.getConcept(), rtMemberIsPart, etIMPD);
		}
		
		if(pathway.getSuperPathWay() != null){
			
			//graph.createRelation(s,pathway.getConcept(),pathway.getSuperPathWay().getConcept(), rtMemberIsPart, etIMPD);	
		}
	}
}
