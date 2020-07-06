package net.sourceforge.ondex.algorithm.graphquery;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * @author hindlem
 */
public class AttributeFunctions {
    /**
     * @param entities relations to extract the given attribute values on (where they exist)
     * @param att
     * @return an array of the attribute values where they exist (can be null if the path is unrankable)
     */
    public static Double[] valuesToArray(Set<ONDEXEntity> entities, AttributeName att) {
        List<Double> values1 = new ArrayList<Double>();
        for (ONDEXEntity entity : entities) {
            Attribute value = entity.getAttribute(att);
            if (value != null)
                values1.add(((Number) value.getValue()).doubleValue());
        }
        return values1.toArray(new Double[values1.size()]);
    }

}
