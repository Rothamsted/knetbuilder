package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * A element in the state machine that determines if a OndexEntity is valid
 *
 * @author hindlem
 */
public interface StateMachineComponent<E extends ONDEXEntity, P extends EvidencePathNode> {

    /**
     * @param c the ONDEXEntity to evaluate
     * @param p the path previously traversed
     * @return if this entity is valid as a next step
     */
    public boolean isValid(E c, P p);

}
