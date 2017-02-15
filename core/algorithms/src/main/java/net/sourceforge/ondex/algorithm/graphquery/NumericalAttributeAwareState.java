package net.sourceforge.ondex.algorithm.graphquery;

import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidNumericalComparisonException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidParamiterException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.*;

/**
 * This is an example class to show how you can extend State objects in the state machine
 *
 * @author hindlem
 */
public class NumericalAttributeAwareState extends State {

    private final AttributeName att;
    private final NumericalComparison comp;
    private final boolean ignoreConceptWithoutAttribute;
    private final double referenceValue;

    public enum NumericalComparison {
        GREATERTHAN, LESSTHAN, EQUALTO
    }

    /**
     * @param cc                            conceptclass set to restrict to
     * @param att                           the attributeName to apply restrictions to
     * @param ignoreConceptWithoutAttribute if the Concept does not have the att its invalid?
     * @param referenceValue                the value you are comparing to
     */
    public NumericalAttributeAwareState(ConceptClass cc, AttributeName att, NumericalComparison comp, boolean ignoreConceptWithoutAttribute, Number referenceValue) {
        super(cc);
        this.comp = comp;
        this.ignoreConceptWithoutAttribute = ignoreConceptWithoutAttribute;
        this.referenceValue = referenceValue.doubleValue();
        this.att = att;
    }

    @Override
    public boolean isValid(ONDEXConcept c, EvidencePathNode<ONDEXConcept, ONDEXRelation, State> path) {

        try {
            Attribute attribute = c.getAttribute(att);
            Object value = attribute.getValue();

            Number number;

            if (value instanceof Number) {
                number = (Number) value;
            } else {
                throw new InvalidParamiterException(value + " is not avalid number");
            }

            if (attribute == null) {
                if (ignoreConceptWithoutAttribute) {
                    return false;
                } else {
                    return true;
                }
            }

            switch (comp) {
                case GREATERTHAN:
                    return (number.doubleValue() > referenceValue);
                case LESSTHAN:
                    return (number.doubleValue() < referenceValue);
                case EQUALTO:
                    return (number.doubleValue() == referenceValue);
                default:
                    throw new InvalidNumericalComparisonException("unknown numerical operator");
            }
        } catch (InvalidNumericalComparisonException e) {
            e.printStackTrace();
        } catch (InvalidParamiterException e) {
            e.printStackTrace();
        }


        return true;
    }

}
