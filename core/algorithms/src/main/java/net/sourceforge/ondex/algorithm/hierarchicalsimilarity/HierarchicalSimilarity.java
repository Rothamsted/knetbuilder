package net.sourceforge.ondex.algorithm.hierarchicalsimilarity;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.algorithm.relationneighbours.DepthInsensitiveRTValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.RelationNeighboursSearch;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;

/**
 * Implementation of Hierarchical Evaluation Measure [Kiritchenko et al. 2005]
 * to determine the hierarchical similarity of to concepts.<br>
 * And a extended version for two sets of concepts [Daraselia et al. 2006]
 *
 * @author keywan
 */
public class HierarchicalSimilarity {

    public static final String is_a = "is_a";
    public static final String is_p = "member_of";

    private RelationNeighboursSearch rn;
    private Map<Integer, Set<ONDEXConcept>> processed;

    /**
     * initialization of parameters; hierarchical evaluation measure considers
     * only is_a and is_p relations of an ONDEX Graph
     *
     * @param graph ONDEX Graph
     */
    public HierarchicalSimilarity(ONDEXGraph graph) {

        RelationType rtISA = graph.getMetaData()
                .getRelationType(is_a);
        if (rtISA == null) {
            RelationType rt = graph.getMetaData().getRelationType(is_a);
            if (rt == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("Missing:", is_a));
            }
            rtISA = graph.getMetaData().getFactory().createRelationType(is_a, rt);
        }

        RelationType rtISP = graph.getMetaData()
                .getRelationType(is_p);
        if (rtISP == null) {
            RelationType rt = graph.getMetaData().getRelationType(is_p);
            if (rt == null) {
                ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new RelationTypeMissingEvent("Missing:", is_p));
            }
            rtISP = graph.getMetaData().getFactory().createRelationType(is_p, rt);
        }

        processed = new HashMap<Integer, Set<ONDEXConcept>>();
        rn = new RelationNeighboursSearch(graph);

        DepthInsensitiveRTValidator validator = new DepthInsensitiveRTValidator();
        validator.addOutgoingRelationType(rtISA);
        validator.addOutgoingRelationType(rtISP);
        rn.setValidator(validator);
    }


    /**
     * Calculates a local similarity for two concepts according to their
     * distance in the ontology.
     *
     * @param x any ONDEX concept
     * @param y any ONDEX concept
     * @return an Evaluation object including precision, recall, F-Score
     */
    public Evaluation getEvaluation(ONDEXConcept x, ONDEXConcept y) {
        double precision = precision(x, y);
        double recall = recall(x, y);
        double score = fScore(x, y);

        return new Evaluation(precision, recall, score);
    }

    /**
     * Calculates a global similarity for two sets of concepts,
     * which maximizes the global asymmetric similarity function
     *
     * @param setX set of first ONDEX concepts
     * @param setY set of second ONDEX concepts
     * @return Evaluation object including precision, recall, F-Score
     */
    public Evaluation getEvaluation(Set<ONDEXConcept> setX, Set<ONDEXConcept> setY) {
        Evaluation first = getEvaluationAsymetric(setX, setY);
        Evaluation second = getEvaluationAsymetric(setY, setX);

        if (first.getScore() > second.getScore()) {
            return first;
        } else {
            return second;
        }
    }

    /**
     * Returns average precision, recall, F-Score for a set of predictions for particular concept
     * The accuracy is compared against the closest "golden standard" node to the particular prediction
     *
     * @param setX set of first ONDEX concepts
     * @param setY set of second ONDEX concepts
     * @return Evaluation object including precision, recall, F-Score
     */
    public Evaluation getEvaluationAsymetric(Set<ONDEXConcept> setX, Set<ONDEXConcept> setY) {
        double precisionVal = 0;
        double recallVal = 0;
        double scoreVal = 0;
        double TP = 0;
        double allPred = setX.size();
        double allTrueInRef = setY.size();
        MostCommonAncestors mcaComparator = new MostCommonAncestors();
        for (ONDEXConcept x : setX) {
            mcaComparator.setBase(x);
            ONDEXConcept y = Collections.max(setY, mcaComparator);
            precisionVal += precision(x, y);
            recallVal += recall(x, y);
            scoreVal += fScore(x, y);
//			System.out.println("MaxSimilar FScore("+x.getPID()+","+y.getPID()+") = "+fScore(x, y));
            if (x.equals(y)) {
                TP += 1;
            }
        }

        return new Evaluation(precisionVal / setX.size(), recallVal / setX.size(), scoreVal / setX.size(),
                TP, allPred, allTrueInRef);
    }

    class MostCommonAncestors implements Comparator<ONDEXConcept> {

        private ONDEXConcept base;

        public void setBase(ONDEXConcept compareTo) {
            base = compareTo;
        }

        /**
         * Returns a negative integer, zero, or a positive integer as the first argument
         * is less than, equal to, or greater than the second
         */
        public int compare(ONDEXConcept o1, ONDEXConcept o2) {
            double o1a = commonAncestors(base, o1);
            double o1b = commonAncestors(base, o2);
            if (o1a == o1b) {
                return 0;
            } else if (o1a < o1b) {
                return -1;
            }
            return 1;
        }
    }

    /**
     * Determines for a given concept the most similar concept from a set of
     * concepts according to the F-score
     *
     * @param x    given concept
     * @param setY set of ONDEX concept
     * @return the most similar concept
     */
    public ONDEXConcept maxSimilar(ONDEXConcept x,
                                   Set<ONDEXConcept> setY) {
        Iterator<ONDEXConcept> setYIt = setY.iterator();
        ONDEXConcept mostSimilarConcept = setYIt.next();
        double maxScore = 0;
        while (setYIt.hasNext()) {
            ONDEXConcept y = setYIt.next();
            double score = fScore(x, y);
            if (score > maxScore) {
                maxScore = score;
                mostSimilarConcept = y;
            }
        }
        return mostSimilarConcept;
    }


    /**
     * hierarchical precision function
     *
     * @param x first concept
     * @param y second concept
     * @return hP = commonAncestors(x,y) / numberAncestor(y)
     */
    public double precision(ONDEXConcept x, ONDEXConcept y) {
        if (numberAncestor(y) == 0) return 0;
        double comAnc = commonAncestors(x, y);
        double numAnc = numberAncestor(y);

        return comAnc / numAnc;
    }

    /**
     * hierarchical recall function
     *
     * @param x first concept
     * @param y second concept
     * @return hR = commonAncestors(x,y) / numberAncestor(x)
     */
    public double recall(ONDEXConcept x, ONDEXConcept y) {
        if (numberAncestor(x) == 0) return 0;
        double comAnc = commonAncestors(x, y);
        double numAnc = numberAncestor(x);

        return comAnc / numAnc;
    }

    /**
     * hierarchical F-score function
     *
     * @param x first concept
     * @param y second concept
     * @return hF = 2*hP*hR / hP+hR
     */
    public double fScore(ONDEXConcept x, ONDEXConcept y) {
        double recall = recall(x, y);
        double precision = precision(x, y);
        double score = 0;
        if (recall + precision != 0) {
            score = (2d * recall * precision) / (recall + precision);
        }
        return score;
    }

    /**
     * the number of ancestor nodes from a concept to the root the root itself
     * is not included.
     *
     * @param x ONDEX concept
     * @return number of ancestors
     */
    public double numberAncestor(ONDEXConcept x) {
        if (!processed.containsKey(x.getId())) {
            rn.search(x);
            Set<ONDEXConcept> view = rn.getFoundConcepts();
            processed.put(x.getId(), view);
        }
        Set<ONDEXConcept> ancestors = processed.get(x.getId());
        double ancestor = ancestors.size() - 1;

//		System.out.println("NumberAncestor("+x.getPID()+") = "+ancestor);

        return ancestor;
    }

    /**
     * Calculates the number of common ancestors for two concepts
     *
     * @param x first concept
     * @param y second concept
     * @return the number of common ancestors
     */
    public double commonAncestors(ONDEXConcept x, ONDEXConcept y) {

        if (!processed.containsKey(x.getId())) {
            rn.search(x);
            Set<ONDEXConcept> view1 = rn.getFoundConcepts();
            processed.put(x.getId(), view1);
        }
        Set<ONDEXConcept> setX = processed.get(x.getId());

        if (!processed.containsKey(y.getId())) {
            rn.search(y);
            Set<ONDEXConcept> view2 = rn.getFoundConcepts();
            processed.put(y.getId(), view2);
        }
        Set<ONDEXConcept> setY = processed.get(y.getId());

        Set<ONDEXConcept> intersection = BitSetFunctions.and(setX, setY);
        double commonAncestor = intersection.size() - 1;

//		System.out.println("CommonAncestor("+x.getPID()+","+y.getPID()+") = "+commonAncestor);

        return commonAncestor;
    }
}
