package net.sourceforge.ondex.algorithm.graphquery.pathrank;

import net.sourceforge.ondex.algorithm.graphquery.AttributeFunctions;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.Transition;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.IncorrectAttributeValueType;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.UnrankableRouteException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;

import java.util.Set;

/**
 * Ranking on numerical attributes of relations derived from transition
 *
 * @author hindlem
 */
public class TransitionNumericalRank extends NumericalRank {

    //the transition the attribute is ranking on
    private Set<Transition> transitions;

    /**
     * @param relativeRank  the relative rank of this NumericalRank against others
     * @param att           the attribute in question
     * @param transitions   the state with the tested attribute
     * @param sm            the state machine the route was extracted using
     * @param og            the current ONDEX graph
     * @param method        the method for summing attribute value across a path
     * @param invertedOrder lower values are better?
     * @param modulusValues absolute values?
     */
    public TransitionNumericalRank(int relativeRank,
                                   AttributeName att,
                                   Set<Transition> transitions,
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
        this.transitions = transitions;
    }

    public void checkAttributeNameisNumerical() throws IncorrectAttributeValueType {
        if (!Number.class.isAssignableFrom(att.getDataType())) {
            throw new IncorrectAttributeValueType(att.getDataType() + " is not a type of number and therefore can not be used to rank paths");
        }
    }

    @Override
    public boolean containsRankableElements(EvidencePathNode path) {
        return path.containsStateMachineComponents(transitions);
    }

    @Override
    public int compare(EvidencePathNode path1, EvidencePathNode path2) {

        double rv1 = Double.NEGATIVE_INFINITY;
        double rv2 = Double.NEGATIVE_INFINITY;

        if (invertedOrder) {
            rv1 = Double.POSITIVE_INFINITY;
            rv2 = Double.POSITIVE_INFINITY;
        }

        if (containsRankableElements(path1)) {
            Set<ONDEXEntity> relations1 = path1.getEntities(transitions);

            Double[] values = AttributeFunctions.valuesToArray(relations1, att);

            if (values != null)
                rv1 = getRelativeRank(values, method);
        }

        if (containsRankableElements(path2)) {
            Set<ONDEXEntity> relations2 = path2.getEntities(transitions);

            Double[] values = AttributeFunctions.valuesToArray(relations2, att);

            if (values != null)
                rv2 = getRelativeRank(values, method);
        }


        if (invertedOrder) {
            return Double.compare(rv1, rv2);
        }
        return (Double.compare(rv1, rv2) * -1);
    }

    @Override
    public double getComparativeValue(EvidencePathNode path) throws UnrankableRouteException {
        if (containsRankableElements(path)) {
            Set<ONDEXEntity> relations = path.getEntities(transitions);
            Double[] values = AttributeFunctions.valuesToArray(relations, att);

            if (values == null) {
                throw new UnrankableRouteException("No qualifying values on relations in route");
            }

            return getRelativeRank(values, method);
        }
        throw new UnrankableRouteException("No qualifying relations in route");
    }


}
