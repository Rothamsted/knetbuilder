package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Inverts the truth of a validator
 * 
 * @author keywan
 *
 */
public class NotValidator implements LogicalRelationValidator{

	private LogicalRelationValidator validator;

	public NotValidator(LogicalRelationValidator validator) {
		this.validator = validator;
	}
	
	@Override
	public boolean isValidRelationAtDepth(ONDEXRelation relation,
			int currentPosition, ONDEXConcept conceptAtHead) {
		
		return !validator.isValidRelationAtDepth(relation, 
				currentPosition, 
				conceptAtHead);
		
	}

}
