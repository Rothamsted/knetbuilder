package net.sourceforge.ondex.ovtk2.filter.genomic;

import java.util.Comparator;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Class to compare two ONDEXConcept given a Number Attribute.
 *
 * @author taubertj
 */
public class GDSComparator implements Comparator<ONDEXConcept> {

    AttributeName an;

    /**
     * Requires AttributeName for Number Attribute.
     *
     * @param an
     */
    public GDSComparator(AttributeName an) {
        this.an = an;
    }

    @Override
    public int compare(ONDEXConcept o1, ONDEXConcept o2) {
        Attribute attribute1 = o1.getAttribute(an);
        Attribute attribute2 = o2.getAttribute(an);
        // both concepts must have a Attribute of the same type
        if (attribute1 != null && attribute2 != null) {
            Object v1 = attribute1.getValue();
            Object v2 = attribute2.getValue();
            // and it has to be a Number
            if (Number.class.isAssignableFrom(v1.getClass())
                    && Number.class.isAssignableFrom(v2.getClass())) {
                Number n1 = (Number) v1;
                Number n2 = (Number) v2;
                // this decides increasing / decreasing order
                return n1.intValue() - n2.intValue();
            }
        }
        return 0;
    }
}