package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * A transition in the state machine that determines if a relation is a valid path
 * The transition must represent a RelationType in the meta graph
 *
 * @author hindlem
 */
public class Transition implements StateMachineComponent<ONDEXRelation, EvidencePathNode<ONDEXConcept, ONDEXRelation, State>> {

    private final RelationType rt;

    //defines the maximum length of continuous transitions of this type allowed
    private final int maxLength;

    /**
     * @param rt        the required RelationType this Transition represents
     * @param maxLength the maximum number of loops this transition can make if the from and to states are equal
     */
    public Transition(RelationType rt, int maxLength) {
        this.rt = rt;
        this.maxLength = maxLength;
    }

    /**
     * @param rt the required RelationType this Transition represents
     *           NB if the from and to states are equal there is no restriction on the number of iterations made
     */
    public Transition(RelationType rt) {
        this(rt, Integer.MAX_VALUE);
    }

    /**
     * @return the required RelationType this State represents
     */
    public RelationType getValidRelationType() {
        return rt;
    }

    /**
     * @param relation the relation to check
     * @return is it valid to traverse this relation at this point in the path
     */
    public boolean isValid(ONDEXRelation relation, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> path) {
        return isValid(relation) && !(maxLength < Integer.MAX_VALUE
                && path.getLength() + 1 > maxLength);
    }

    /**
     * Pre-check for all relations that <i>may</i> be accepted by <code>isValid(ONDEXRelation, StateMachineDerivedPath)</code>.
     *
     * @param relation the relation to check
     * @return true if the relation is valid, ignoring the path tag
     */
    public boolean isValid(ONDEXRelation relation) {
        return relation.getOfType().equals(rt);
    }

    public String toString() {
        return "Transition on " + rt.getId();
    }
}
