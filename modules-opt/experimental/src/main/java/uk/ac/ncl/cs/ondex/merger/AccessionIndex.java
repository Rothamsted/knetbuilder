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
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 *
 * @author jweile
 */
public class AccessionIndex extends HashMap<String, MergeList>{

    private ONDEXGraph graph;

    public AccessionIndex(ONDEXGraph graph) {
        this.graph = graph;
    }

    

    /**
     * <p>Creates an index of the concepts in <code>ConceptClass cc</code>
     * according to their accessions in the namespace <code>DataSource cv</code>.</p>
     * 
     * <p>The class operates like a <code>Map</code>, mapping <code>String</code>s
     * representing accessions (or accession sets, if <code>setMode == true</code>)
     * to a <code>MergeSet</code> containing all the concepts that bear the
     * concerned accession (or set of accessions).</p>
     *
     * @param setMode            determines whether an entry is created for the whole
     *                           set of accessions in a concept or for every single accession in it.
     * @param multipleAccAllowed whether more than one accession is allowed
     *                           per concept.
     */
    public void indexByAccession(ConceptClass cc, DataSource dataSource, boolean setMode, boolean multipleAccAllowed, boolean missingAccAllowed) {
        log("Indexing " + cc.getFullname() + "...");

        for (ONDEXConcept c : conceptsOfCC(cc)) {
            Set<String> accs = accessionOfNamespace(c, dataSource);

            check(multipleAccAllowed || accs.size() <= 1,
                    pretty(c) + " has more than one key accession: " + accs);
            check(missingAccAllowed || accs.size() > 0,
                    pretty(c) + " has no key accession!");

            if (accs.size() == 0) {
                continue;
            }

            if (setMode) {
                String accCat = cat(accs, "|");
                accs.clear();
                accs.add(accCat);
            }

            //search for existing list
            MergeList list = null;
            for (String acc : accs) {
                MergeList returnedList = get(acc);
                if (returnedList != null) {
                    if (list == null) {
                        list = returnedList;
                    } else if (!list.equals(returnedList)) {
                        //merge the mergelists :(
                        list.join(returnedList);
                    }
                }
            }

            //if not found: create
            if (list == null) {
                list = new MergeList();
            }

            //register the concept
            list.add(c);

            //register list for all accessions
            for (String acc : accs) {
                put(acc, list);
            }

        }

    }

    /**
     * Returns a set of strings representing all accessions of the
     * <code>ONDEXConcept c</code> that are elements of the
     * <code>DataSource cv</code>.
     */
    private Set<String> accessionOfNamespace(ONDEXConcept c, DataSource dataSource) {
        Set<String> set = new TreeSet<String>();

        for (ConceptAccession acc : c.getConceptAccessions()) {
            if (acc.getElementOf().equals(dataSource)) {
                set.add(acc.getAccession());
            }
        }

        return set;
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
