package net.sourceforge.ondex.ovtk2.ui.editor.relation;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.editor.relation.RelationTableCellEditor.STATE;

/**
 * Clones relations except for changing one attribute.
 * 
 * @author taubertj
 */
public class RelationCloner {

	/**
	 * current ONDEXGraph to modify
	 */
	ONDEXGraph graph = null;

	/**
	 * Clones relations on the given graph.
	 * 
	 * @param graph
	 *            ONDEXGraph to modify
	 */
	public RelationCloner(ONDEXGraph graph) {
		this.graph = graph;
	}

	/**
	 * Clone relation with having a new from concept.
	 * 
	 * @param oldR
	 *            old ONDEXRelation
	 * @param newC
	 *            new concept to use at state
	 * @param state
	 *            the positional state for new concept, e.g. from
	 * @return new ONDEXRelation
	 */
	public ONDEXRelation clone(ONDEXRelation oldR, ONDEXConcept newC, STATE state) {

		// clone new relation
		ONDEXRelation newR = null;

		switch (state) {

		case FROM:
			// relation with new from concept
			newR = graph.getRelation(newC, oldR.getToConcept(), oldR.getOfType());
			if (newR == null)
				newR = graph.createRelation(newC, oldR.getToConcept(), oldR.getOfType(), oldR.getEvidence());
			break;
		case TO:
			// relation with new to concept
			newR = graph.getRelation(oldR.getFromConcept(), newC, oldR.getOfType());
			if (newR == null)
				newR = graph.createRelation(oldR.getFromConcept(), newC, oldR.getOfType(), oldR.getEvidence());
			break;
		default:
			throw new RuntimeException("State not allowed: " + state);
		}

		// copies everything else
		copyEverythingElse(oldR, newR);

		return newR;
	}

	/**
	 * Clone relation with having a new RelationType.
	 * 
	 * @param oldR
	 *            old ONDEXRelation
	 * @param newRT
	 *            new RelationType to use
	 * @return new ONDEXRelation
	 */
	public ONDEXRelation clone(ONDEXRelation oldR, RelationType newRT) {

		// first clone concept with new ConceptClass
		ONDEXRelation newR = graph.getRelation(oldR.getFromConcept(), oldR.getToConcept(), oldR.getOfType());
		if (newR == null)
			graph.createRelation(oldR.getFromConcept(), oldR.getToConcept(), oldR.getOfType(), oldR.getEvidence());

		// copies everything else
		copyEverythingElse(oldR, newR);

		return newR;
	}

	/**
	 * Calls all other subroutines for coping informations of relation.
	 * 
	 * @param oldR
	 *            old ONDEXRelation
	 * @param newR
	 *            new ONDEXRelation
	 */
	private void copyEverythingElse(ONDEXRelation oldR, ONDEXRelation newR) {
		// copies context across
		copyContext(oldR, newR);

		// transfer all Attribute from old to new
		copyGDS(oldR, newR);
	}

	/**
	 * Copies all context information from old relation to new relation.
	 * 
	 * @param oldR
	 *            old ONDEXRelation
	 * @param newR
	 *            new ONDEXRelation
	 */
	private void copyContext(ONDEXRelation oldR, ONDEXRelation newR) {

		// transfer context from old relation to new relation
		for (ONDEXConcept c : oldR.getTags()) {
			newR.addTag(c);
		}
	}

	/**
	 * Copies all Attribute from old relation to new relation.
	 * 
	 * @param oldR
	 *            old ONDEXRelation
	 * @param newR
	 *            new ONDEXRelation
	 */
	private void copyGDS(ONDEXRelation oldR, ONDEXRelation newR) {

		// iterate over all old Attribute
		for (Attribute attribute : oldR.getAttributes()) {
			// clone old Attribute on new relation only if not yet exists
			if (newR.getAttribute(attribute.getOfType()) != null)
				// old Attribute gets overridden
				newR.deleteAttribute(attribute.getOfType());
			else
				newR.createAttribute(attribute.getOfType(), attribute.getValue(), attribute.isDoIndex());
		}
	}
}
