package net.sourceforge.ondex.algorithm.relationneighbours;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Allows the creation of a tree structure by adding child validators which are treated with the and boolean operation
 *
 * @author hindlem
 */
public class AndOperator implements LogicalRelationValidator {

    private final Set<LogicalRelationValidator> andOperators;

    /**
     * @param validators an array of validators that must have a length greater than 0
     * @throws InvalidLogicError if length < than 0
     */
    public AndOperator(LogicalRelationValidator[] validators) throws InvalidLogicError {
        andOperators = new HashSet<LogicalRelationValidator>(Arrays.asList(validators));
        if (andOperators.size() == 0) {
            throw new InvalidLogicError("And Operators must have conditions");
        }
    }

    /**
     * @param validators a set of validators that must have a length greater than 0
     * @throws InvalidLogicError if length < than 0
     */
    public AndOperator(Set<LogicalRelationValidator> validators) throws InvalidLogicError {
        andOperators = validators;
        if (andOperators.size() == 0) {
            throw new InvalidLogicError("And Operators must have conditions");
        }
    }

    /**
     * @param operator a validator to add to this argument
     */
    public void addLogicOperator(LogicalRelationValidator operator) {
        andOperators.add(operator);
    }

    public boolean isValidRelationAtDepth(ONDEXRelation relation,
                                          int currentPosition, ONDEXConcept conceptAtHead) {
        Iterator<LogicalRelationValidator> andIt = andOperators.iterator();
        while (andIt.hasNext()) {
            LogicalRelationValidator and = andIt.next();
            if (!and.isValidRelationAtDepth(relation, currentPosition, conceptAtHead)) {
                return false;
            }
        }
        return true;
    }
}
