/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.merger;

import static uk.ac.ncl.cs.ondex.merger.MergerTools.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ncl.cs.ondex.tools.Neighbour;

/**
 *
 * @author jweile
 */
public class ContextIndex extends HashMap<String, Set<MergeList>> {

    private ONDEXGraph graph;

    public ContextIndex(ONDEXGraph graph) {
        this.graph = graph;
    }

    public void indexByNeighbourhood(ConceptClass targetCC, ConceptClass neighbourCC) {

        log("Indexing " + targetCC.getFullname() + "...");

        for (ONDEXConcept c : conceptsOfCC(targetCC)) {

            Set<String> neighbourIds = new TreeSet<String>();

            for (Neighbour n : Neighbour.getNeighbourhood(c, graph)) {
                if (n.getConcept().inheritedFrom(neighbourCC)) {
                    neighbourIds.add(n.getConcept().getId()+"");
                }
            }

            if (neighbourIds.size() == 0) {
                continue;
            }

            String key = cat(neighbourIds,",");

            //Access the set corresponding to this neighbourhood
            Set<MergeList> set = get(key);
            if (set == null) {
                set = new HashSet<MergeList>();
                put(key,set);
            }

            //Try to find the matching set
            MergeList matchingList = null;
            ConceptClass bestType = null;
            for (MergeList list : set) {
                bestType = moreSpecific(list.getType(), c.getOfType());
                if (bestType != null) {
                    matchingList = list;
                    matchingList.setType(bestType);
                    break;
                }
            }
//            for (MergeList list : set) {
//                if (list.getType().equals(c.getOfType())) {
//                    matchingList = list;
//                    break;
//                }
//            }

            //If none exists, create a new one.
            if (matchingList == null) {
                matchingList = new MergeList();
                matchingList.setType(c.getOfType());
                set.add(matchingList);
            }

            //Add the concept to the list.
            matchingList.add(c);

        }


    }

    /**
     * returns an <code>IntSet</code> containing the IDs instances
     * of <code>ConceptClass cc</code>.
     *
     * @param cc The concerned <code>ConceptClass</code>.
     */
    private Set<ONDEXConcept> conceptsOfCC(ConceptClass cc) {
        Set<ONDEXConcept> set = new HashSet<ONDEXConcept>();

        for (ONDEXConcept c : graph.getConcepts()) {
            if (c.inheritedFrom(cc)) {
                set.add(c);
            }
        }

        return set;
    }

}
