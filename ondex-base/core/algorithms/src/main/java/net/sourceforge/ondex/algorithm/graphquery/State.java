package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * A state in the state machine that determines if a concept is a valid position
 * The State must represent a ConceptClass of the metagraph
 *
 * @author hindlem
 */
public class State implements StateMachineComponent<ONDEXConcept, EvidencePathNode<ONDEXConcept, ONDEXRelation, State>> {

    private final ConceptClass cc;

    /**
     * @param cc the required ConceptClass this State represents
     */
    public State(ConceptClass cc) {
        this.cc = cc;
    }

    /**
     * @return the required ConceptClass this State represents
     */
    public ConceptClass getValidConceptClass() {
        return cc;
    }

    /**
     * @param c the concept to check
     * @return is it valid to traverse this concept
     */
    public boolean isValid(ONDEXConcept c, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> p) {
        return cc.equals(c.getOfType());
    }


    public String toString() {
        return "State for " + cc.getId();
    }

}
