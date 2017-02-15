package net.sourceforge.ondex.algorithm.graphquery.pathrank;

import net.sourceforge.ondex.algorithm.graphquery.AttributeFunctions;
import net.sourceforge.ondex.algorithm.graphquery.State;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.UnrankableRouteException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;

import java.util.Set;

/**
 * Ranking on numerical attributes of concepts derived from state
 *
 * @author hindlem
 */
public class StateNumericalRank extends NumericalRank {

    //the state the attribute is ranking on
    private State state;

    /**
     * @param relativeRank  the relative rank of this NumericalRank against others
     * @param att           the attribute in question
     * @param st            the state with the tested attribute
     * @param sm            the state machine the route was extracted using
     * @param og            the current ONDEX graph
     * @param method        the method for summing attribute value across a path
     * @param invertedOrder lower values are better?
     * @param modulusValues absolute values?
     */
    public StateNumericalRank(int relativeRank,
                              AttributeName att,
                              State st,
                              StateMachine sm,
                              ONDEXGraph og,
                              ComparisonMethod method,
                              boolean invertedOrder,
                              boolean modulusValues) {
        super(relativeRank,
                invertedOrder,
                modulusValues,
                att,
                og,
                method);
        state = st;
    }

    @Override
    public double getComparativeValue(EvidencePathNode r) throws UnrankableRouteException {
        if (containsRankableElements(r)) {
            Set<ONDEXEntity> concepts = r.getEntities(state);
            Double[] values = AttributeFunctions.valuesToArray(concepts, att);

            if (values == null) {
                throw new UnrankableRouteException("No qualifying values on concepts in route");
            }

            return getRelativeRank(values, method);
        }
        throw new UnrankableRouteException("No qualifying concepts in route");
    }

    @Override
    public boolean containsRankableElements(EvidencePathNode r) {
        return r.containsStateMachineComponent(state);
    }

    @Override
    public int compare(EvidencePathNode r1, EvidencePathNode r2) {

        double c1 = Double.NEGATIVE_INFINITY;
        double c2 = Double.NEGATIVE_INFINITY;

        if (invertedOrder) {
            c1 = Double.POSITIVE_INFINITY;
            c2 = Double.POSITIVE_INFINITY;
        }

        if (containsRankableElements(r1)) {
            Set<ONDEXEntity> concepts1 = r1.getEntities(state);

            Double[] values = AttributeFunctions.valuesToArray(concepts1, att);
            if (values != null)
                c1 = getRelativeRank(values, method);
        }

        if (containsRankableElements(r2)) {
            Set<ONDEXEntity> concepts2 = r2.getEntities(state);

            Double[] values = AttributeFunctions.valuesToArray(concepts2, att);
            if (values != null)
                c2 = getRelativeRank(values, method);
        }


        if (invertedOrder) {
            return Double.compare(c1, c2);
        }

        return (Double.compare(c1, c2) * -1);
    }


}
