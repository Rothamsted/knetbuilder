package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * Will only traverse according to the given direction constraints according to the directionality of the edge.
 * N.B. If relation has the same from and to concept, then directionality is considered irrelevent and the relation will be traversed.
 *
 * @author hindlem
 */
public class DirectedEdgeTransition extends Transition {

    public enum EdgeTreatment {
        FORWARD, BACKWARD, BIDIRECTIONAL
    }

    private EdgeTreatment treatment;

    public DirectedEdgeTransition(RelationType rt, EdgeTreatment treatment) {
        super(rt);
        this.treatment = treatment;
    }


    public DirectedEdgeTransition(RelationType rt, int maxLength, EdgeTreatment treatment) {
        super(rt, maxLength);
        this.treatment = treatment;
    }

    @Override
    public boolean isValid(ONDEXRelation relation, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> path) {
        boolean priorCondition = super.isValid(relation, path);
        if (priorCondition) {

            if (treatment == EdgeTreatment.BIDIRECTIONAL)
                return true;

            ONDEXConcept concept = path.getEntity();
            boolean isOutgoing = relation.getKey().getFromID() == concept.getId();

            if (isOutgoing && treatment == EdgeTreatment.FORWARD) {
                return true;
            } else if (isOutgoing && treatment == EdgeTreatment.BACKWARD) {
                return relation.getKey().getFromID() == relation.getKey().getToID(); //ban loops else ok
            }
        }
        return false;
    }

}
