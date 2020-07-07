package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Everything is true for this validator
 * 
 * Pluralism (noun) -  the belief that there is more than one kind of fundamental
 * reality or of fundamental existents.
 * 
 * @author hindlem
 *
 */
public class DefaultPluralisticValidator implements LogicalRelationValidator {

	public boolean isValidRelationAtDepth(ONDEXRelation relation,
			int currentPosition, ONDEXConcept conceptAtHead){
		return true;
	}

}
