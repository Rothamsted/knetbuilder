package net.sourceforge.ondex.algorithm.graphquery.pathrank;

import net.sourceforge.ondex.algorithm.graphquery.exceptions.IncorrectAttributeValueType;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.UnrankableRouteException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;

import java.util.Comparator;

/**
 * @author hindlem
 */
public abstract class NumericalRank implements Comparable<NumericalRank>, Comparator<EvidencePathNode> {

    protected final int relativeRank;
    protected boolean invertedOrder = false;
    protected boolean modulusValues = false;

    protected final AttributeName att;
    protected final ONDEXGraph og;
    protected final ComparisonMethod method;

    /**
     * @param relativeRank  the relative rank of this NumericalRank against others
     * @param invertedOrder lower values are better?
     * @param modulusValues absolute values?
     * @param att           the attribute in question
     * @param og            the current ONDEX graph
     * @param method        the method for summing attribute value across a path
     */
    public NumericalRank(int relativeRank,
                         boolean invertedOrder,
                         boolean modulusValues,
                         AttributeName att,
                         ONDEXGraph og,
                         ComparisonMethod method) {
        this.relativeRank = relativeRank;
        this.invertedOrder = invertedOrder;
        this.modulusValues = modulusValues;
        this.method = method;
        this.att = att;
        this.og = og;

        try {
            checkAttributeNameisNumerical(); //not sure if this should be in the constuctor but it is too expensive to do elseware
        } catch (IncorrectAttributeValueType e) {
            e.printStackTrace();
        }
    }

    /**
     * A definition of types of comparison methods
     *
     * @author hindlem
     */
    public enum ComparisonMethod {
        INTEPENENTPROBABLITIES, MEAN, SUM
    }

    /**
     * compute the relative rank of the values according to the comparison method
     *
     * @param values the values to derive the rank from
     * @param method the method to compute the rank of these numbers
     * @return the relative rank of these values
     */
    protected double getRelativeRank(Double[] values, ComparisonMethod method) {
        double result;

        if (modulusValues) {
            modulusArray(values);
        }

        switch (method) {
            case INTEPENENTPROBABLITIES:
                result = product(values);
                break;
            case MEAN:
                result = mean(values);
                break;
            case SUM:
                result = sum(values);
                break;
            default:
                throw new RuntimeException("Unknown Comparison Method");
        }

        return result;
    }

    /**
     * absolute values in an array
     *
     * @param values the array to absolute values in
     */
    private static void modulusArray(Double[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.abs(values[i]);
        }
    }

    /**
     * Method for calculating a single representative value from a StateDerivedRoute according to the comparison method that can be used to compare paths
     *
     * @param r the route to calculate on
     * @return a comparable rank for this route (only valid within this NumericalRank)
     */
    public abstract double getComparativeValue(EvidencePathNode r) throws UnrankableRouteException;

    /**
     * Attempts to decide if a route can be ranked on (not guarenteed to always be false when not possible)
     *
     * @param r the route to rank on
     * @return the decision
     */
    public abstract boolean containsRankableElements(EvidencePathNode r);

    /**
     * @param r1
     * @param r2
     * @return
     */
    public abstract int compare(EvidencePathNode r1, EvidencePathNode r2);

    /**
     * computes the mean of the array
     *
     * @param p an array of doubles of any length
     * @return the mean (0 if given empty array)
     */
    private static double mean(Double[] p) {
        if (p.length == 0) {
            return 0d;
        }
        return sum(p) / p.length;
    }

    /**
     * compute the sum of the array
     *
     * @param p an array of doubles of any length
     * @return the sum of the numbers (0 if given empty array)
     */
    private static double sum(Double[] p) {
        double sum = 0;  // sum of all the elements
        for (double i : p) {
            sum += i;
        }
        return sum;
    }

    /**
     * Compute the product of the array
     *
     * @param p a array of doubles of any length
     * @return the product of the numbers (0 if given empty array)
     */
    private static double product(Double[] p) {
        if (p.length == 0) {
            return 0d;
        }
        double product = p[0];  // sum of all the elements
        for (int i = 1; i < p.length; i++) {
            product = product * p[i];
        }
        return product;
    }

    @Override
    public int compareTo(NumericalRank o) {
        return (relativeRank < o.relativeRank ? -1 : (relativeRank == o.relativeRank ? 0 : 1));
    }

    public int hashCode() {
        return relativeRank;
    }

    /**
     * Is the rank inverted against its natural order?
     *
     * @return lowest is best?
     */
    public boolean isInvertedOrder() {
        return invertedOrder;
    }

    /**
     * Take the absolute of all values before calculating rank
     *
     * @return values will be absoluted?
     */
    public boolean isModulusValues() {
        return modulusValues;
    }

    /**
     * @return the relative rank of this rank compared to other numerical ranks (-ve infinity being the highest and +ve infinity the lowest)
     */
    public int getRelativeRankofRank() {
        return relativeRank;
    }

    /**
     * @throws IncorrectAttributeValueType i.e the attribute value is not a number
     */
    private void checkAttributeNameisNumerical() throws IncorrectAttributeValueType {
        if (!Number.class.isAssignableFrom(att.getDataType())) {
            throw new IncorrectAttributeValueType(att.getDataType() + " is not a type of number and therefore can not be used to rank paths");
        }
    }

}
