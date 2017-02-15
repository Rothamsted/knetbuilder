package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author hindlem
 */
public class OrOperator implements LogicalRelationValidator {

    private Set<LogicalRelationValidator> orOperators;

    /**
     * @param validators an array of validators that must have a length greater than 0
     * @throws InvalidLogicError if length < than 0
     */
    public OrOperator(LogicalRelationValidator[] validators) {
        orOperators = new HashSet<LogicalRelationValidator>(Arrays.asList(validators));
    }

    /**
     * @param validators a set of validators that must have a length greater than 0
     * @throws InvalidLogicError if length < than 0
     */
    public OrOperator(Set<LogicalRelationValidator> validators) throws InvalidLogicError {
        orOperators = validators;
        if (orOperators.size() == 0) {
            throw new InvalidLogicError("Or Operators must have children");
        }
    }

    /**
     * @param validators a validator to add to this argument
     */
    public void addLogicOperator(LogicalRelationValidator validators) {
        orOperators.add(validators);
    }

    public boolean isValidRelationAtDepth(ONDEXRelation relation,
                                          int currentPosition, ONDEXConcept conceptAtHead) {
        Iterator<LogicalRelationValidator> orIt = orOperators.iterator();
        while (orIt.hasNext()) {
            LogicalRelationValidator or = orIt.next();
            if (or.isValidRelationAtDepth(relation, currentPosition, conceptAtHead)) {
                return true;
            }
        }
        return false;
    }

}
