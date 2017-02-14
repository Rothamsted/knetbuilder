/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.RelationType;

/**
 *
 * @author jweile
 */
public class PropertyMaps extends HashMap<String,Map<String,Integer>> {


    /**
     * hash key for the count attribute.
     */
    public static final String KEY_COUNT = "count";

    /**
     * Returns the property maps corresponding to <code>cc</code> and all its supertypes.
     * @param md A concept class.
     * @return the property maps corresponding to <code>cc</code> and all its supertypes.
     */
    public List<Map<String, Integer>> getMapsHierarchyAware(MetaData md) {

        List<Map<String,Integer>> mapList = new ArrayList<Map<String, Integer>>();

        while (md != null) {
            mapList.add(getMaps(md));
            if (md instanceof ConceptClass) {
                md = ((ConceptClass)md).getSpecialisationOf();
            } else if (md instanceof RelationType) {
                md = ((RelationType)md).getSpecialisationOf();
            }
        }

        return mapList;

    }

     /**
     * returns the property map corresponding to the conceptclass <code>cc</code>.
     * @param md a concept class.
     * @return the property map corresponding to the conceptclass <code>cc</code>.
     */
    public Map<String, Integer> getMaps(MetaData md) {

        String ccId = md.getId();
        Map<String,Integer> map = get(ccId);
        if (map == null) {
            map = new HashMap<String, Integer>();
            put(ccId,map);
        }
        return map;

    }


    /**
     * Performs increment operation on the value for the given key in the given map.
     * @param map
     * @param key
     */
    public static void increment(Map<String, Integer> map, String key) {
        Integer i = map.get(key);
        if (i == null) {
            map.put(key,1);
        } else {
            map.put(key,i+1);
        }
    }

}
