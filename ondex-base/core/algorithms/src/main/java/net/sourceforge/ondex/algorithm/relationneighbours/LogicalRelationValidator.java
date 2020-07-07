package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A validator for passing to a Relation Neighbours Search
 * @author hindlem
 *
 */
public interface LogicalRelationValidator {

	/**
	 * Called by RelationNeighboursSearch in a thread lock. If the return is false then the cluster will not be expanded to this relation and target/source concept
	 * @param relation the relation to validate (conceptAtHead will be either from or to on this relation)
	 * @param currentPosition the current depth from the seed in relations (where the first relations of the seed are depth 1)
	 * @param conceptAtHead the concept at the head (i.e. last touched from the seed) this is idependent of its a from or to relationship. This concept will be part of the end cluster unless its a seed concept. 
	 * @return the validity of the relation based on the implementing Validators criteria
	 */
	public boolean isValidRelationAtDepth(ONDEXRelation relation,
			int currentPosition,
			ONDEXConcept conceptAtHead);

}
