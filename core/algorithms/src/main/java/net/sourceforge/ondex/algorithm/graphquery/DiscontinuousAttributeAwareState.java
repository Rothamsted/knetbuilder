package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.*;

import java.util.Arrays;

/**
 * Allows only certain values for Attribute to be allowed
 *
 * @author hindlem
 */
public class DiscontinuousAttributeAwareState extends State {

    private final AttributeName att;
    private final boolean ignoreConceptWithoutAttribute;
    private final Object[] referenceValues;
    private final boolean includeMatching;

    /**
     * @param cc                            conceptclass set to restrict to
     * @param ignoreConceptWithoutAttribute if the Concept does not have the att its invalid?
     * @param referenceValues               the values you are comparing to
     * @param includeMatching               include only the values that match the referenceValues?
     */
    public DiscontinuousAttributeAwareState(ConceptClass cc, AttributeName att,
                                      boolean ignoreConceptWithoutAttribute, Object[] referenceValues,
                                      boolean includeMatching) {
        super(cc);
        this.att = att;
        this.ignoreConceptWithoutAttribute = ignoreConceptWithoutAttribute;
        Arrays.sort(referenceValues);
        this.referenceValues = referenceValues;
        this.includeMatching = includeMatching;
    }

    @Override
    public boolean isValid(ONDEXConcept c, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> path) {

        Attribute attribute = c.getAttribute(att);
        if (attribute == null) {
            if (ignoreConceptWithoutAttribute) {
                return false;
            } else {
                return true;
            }
        }
        Object value = attribute.getValue();
        int result = Arrays.binarySearch(referenceValues, value);
        if (includeMatching) return result > -1;
        else return result == -1;
    }

}
