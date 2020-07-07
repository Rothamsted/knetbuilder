package net.sourceforge.ondex.parser.metacyc.parse.transformers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.parser.metacyc.MetaData;
import net.sourceforge.ondex.parser.metacyc.Parser;
import net.sourceforge.ondex.parser.metacyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.metacyc.objects.Pathway;
import net.sourceforge.ondex.parser.metacyc.objects.Reaction;

/**
 * Transforms a net.sourceforge.ondex.parser.metacyc.sink.Reaction to a Concept.
 * 
 * @author peschr
 */
public class ReactionTransformer extends AbstractTransformer {
	private ConceptClass ccReaction = null;

	private RelationType rtCsBy = null;

	private RelationType rtPdBy = null;

	private RelationType rtMemberIsPart = null;

	private AttributeName deltaGo = null;

	public ReactionTransformer(Parser parser) {
		super(parser);
		ccReaction = graph.getMetaData().getConceptClass(
				MetaData.CC_REACTION);
		if (ccReaction == null) {
			Parser.propagateEventOccurred(new ConceptClassMissingEvent(
					MetaData.CC_REACTION, Parser.getCurrentMethodName()));
		}
		rtCsBy = graph.getMetaData().getRelationType(
				MetaData.RT_CONSUMED_BY);
		if (rtCsBy == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					MetaData.RT_CONSUMED_BY, Parser.getCurrentMethodName()));
		}
		rtPdBy = graph.getMetaData().getRelationType(
				MetaData.RT_PRODUCED_BY);
		if (rtPdBy == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					MetaData.RT_PRODUCED_BY, Parser.getCurrentMethodName()));
		}
		rtMemberIsPart = graph.getMetaData().getRelationType(
				MetaData.RT_MEMBER_IS_PART_OF);
		if (rtMemberIsPart == null) {
			Parser.propagateEventOccurred(new RelationTypeMissingEvent(
					MetaData.RT_MEMBER_IS_PART_OF, Parser
							.getCurrentMethodName()));
		}
		deltaGo = graph.getMetaData().getAttributeName(
				MetaData.ATR_DELTAGO);
		if (deltaGo == null) {
			Parser.propagateEventOccurred(new AttributeNameMissingEvent(
					MetaData.CV_PubMedID, Parser.getCurrentMethodName()));
		}
	}

	@Override
	public void nodeToConcept(AbstractNode node) {
		Reaction reaction = (Reaction) node;
		ONDEXConcept concept = graph.getFactory().createConcept(
				reaction.getUniqueId(), dataSourceMetaC, ccReaction, etIMPD);
		if (reaction.getBalancedState() != null)
			concept.setAnnotation(reaction.getBalancedState());
		concept.createConceptName(constructFormula(reaction), false);

		if (reaction.getDeltaGo() != null) {
			concept.createAttribute(deltaGo, reaction.getDeltaGo(), false);
		}
		
		if (reaction.getDirection() != null) {
			concept.setAnnotation(reaction.getDirection());
		}

		reaction.setConcept(concept);
	}

	/**
	 * Creates a formulaic represtentation of the reaction
	 * 
	 * @param reaction
	 * @return a formulaic represtentation of the reaction
	 */
	public static String constructFormula(Reaction reaction) {
		StringBuilder reactionName = new StringBuilder();
		Iterator<AbstractNode> subs = reaction.getLeft().iterator();
		int left = reaction.getLeft().size();
		while (subs.hasNext()) {
			AbstractNode sub = subs.next();
			String name = sub.getCommonName();
			if (name == null)
				name = sub.getUniqueId();
			if (name == null) {
				if (sub.getSynonym().size() > 0)
					name = sub.getSynonym().iterator().next();
			}
			if (name == null)
				name = "<?>";

			reactionName.append(name);
			if (left > 1)
				reactionName.append(" + ");
			left--;
		}

		if (reaction.getBalancedState() == null) {
			reactionName.append(" => ");
		} else if (reaction.getBalancedState().startsWith("BALANCED")) {
			reactionName.append(" <=> ");
		} else if (reaction.getBalancedState().startsWith("UNBALANCED")) {
			reactionName.append(" => ");
		} else {
			reactionName.append(" => ");
		}

		Iterator<AbstractNode> prods = reaction.getRight().iterator();
		left = reaction.getRight().size();
		while (prods.hasNext()) {
			AbstractNode prod = prods.next();
			String name = prod.getCommonName();
			if (name == null)
				name = prod.getUniqueId();
			if (name == null) {
				if (prod.getSynonym().size() > 0)
					name = prod.getSynonym().iterator().next();
			}
			if (name == null)
				name = "<?>";
			reactionName.append(name);
			if (left > 1)
				reactionName.append(" + ");
			left--;
		}

		return reactionName.toString();
	}

	@Override
	public void pointerToRelation(AbstractNode node) {
		Reaction reaction = (Reaction) node;
		Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();

		for (AbstractNode leftNode : reaction.getLeft()) {
			// Compound,Protein (consumed by)=> Reaction
			relations.add(graph.getFactory().createRelation(leftNode.getConcept(),
					reaction.getConcept(), rtCsBy, etIMPD));
		}
		for (AbstractNode rightNode : reaction.getRight()) {
			// Compound,Protein (produced by)=> Reaction
			relations.add(graph.getFactory().createRelation(rightNode.getConcept(),
					reaction.getConcept(), rtPdBy, etIMPD));
		}
		for (AbstractNode unknown : reaction.getInUnknown()) {
			relations.add(graph.getFactory().createRelation(reaction.getConcept(),
					unknown.getConcept(), rtMemberIsPart, etIMPD));
		}
		
		for (Pathway way : reaction.getInPathway()) {
			// Reaction (member is part) => Pathway
			Set<ONDEXConcept> contexts = new HashSet<ONDEXConcept>();
			if (way.isBelongsToSuperPathway()) {
				contexts = super.getNonRedundant(null, way.getConcept());
			} else
				contexts.add(way.getConcept());

			super.copyContext(reaction.getConcept(), contexts);
			super.copyContext(graph.getFactory().createRelation(reaction.getConcept(),
					way.getConcept(), rtMemberIsPart, etIMPD), contexts);
			for (ONDEXRelation relation : relations) {
				relation.addTag(way.getConcept());
			}
			for (AbstractNode left : reaction.getLeft()) {
				if (!left.getConcept().getTags().contains(way.getConcept()))
					left.getConcept().addTag(way.getConcept());
			}
			for (AbstractNode right : reaction.getRight()) {
				if (!right.getConcept().getTags()
						.contains(way.getConcept()))
					right.getConcept().addTag(way.getConcept());
			}
		}
	}
}
