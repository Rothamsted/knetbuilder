/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.merger;

import java.util.Collection;
import java.util.HashSet;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import uk.ac.ncl.cs.ondex.merger.Merger;

/**
 *
 * @author jweile
 */
public class MergerTools {
    /**
     * Returns a nicely formatted string that describes the <code>ONDEXConcept c</code>.
     * If <code>c.getId() == 1</code> and <code>c.getPID().equals("lacZ")</code>, then
     * <code>pretty(c)</code> returns <code>Concept #1 ("lacZ")</code>.
     *
     * @param c The concerned <code>ONDEXConcept</code>.
     * @return a <code>String</code> containing output described above.
     */
    public static String pretty(ONDEXConcept c) {
        return c.getOfType() + " #" + c.getId() + " (" + c.getPID() + ") -- source: " + c.getElementOf();
    }

    /**
     * Creates a nicely formatted bullet point list describing all <code>OndexConcept</code>s
     * corresponding to the concept ids in the given integer list <code>cids</code>
     * @param cids a list of concept ids.
     * @return a <code>String</code> containing the output described above.
     */
    private static String prettyCs(Collection<ONDEXConcept> cs) {
        StringBuilder b = new StringBuilder();
        for (ONDEXConcept c : cs) {
            b.append("  * " + pretty(c) + "\n");
        }
        return b.toString();
    }

    /**
     * Concatenates all members of the collection <code>ss</code>
     * using the delimiter <code>delim</code>.
     * <h4>Example:</h4>
     * ss: {"1","2","3"}<br>
     * delim: "|"<br>
     * output: "1|2|3"<br>
     *
     * @param ss    the set of strings to concatenate
     * @param delim the delimter to use
     * @return a <code>String</code> containing the above concatenation.
     */
    public static String cat(Collection<String> ss, String delim) {
        if (ss.size() == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (String s : ss) {
            b.append(s + delim);
        }
        b.delete(b.length() - delim.length(), b.length());
        return b.toString();
    }

    /**
     * Checks whether <code>b</code> holds. If not it logs an an
     * inconsistency event with the message <code>string</code>.
     *
     * @param b      the condition to check on.
     * @param string the message to log in case <code>b</code>
     *               does not hold.
     */
    public static void check(boolean b, String string) {
        if (!b) {
            complain(string);
        }
    }


    private static Logger logger = Logger.getLogger(Merger.class);

    public static void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    public static void complain(String msg) {
        logger.log(Level.WARN, msg);
    }

    /**
     * determines the most specific concept class for a list of concept ids.
     *
     * @param cids the concept ids.
     * @throws InconsistencyException
     */
    public static ConceptClass determineCC(Collection<ONDEXConcept> cs, String groupID, ONDEXGraph graph) throws InconsistencyException {

        HashSet<ConceptClass> ccs = new HashSet<ConceptClass>();
        for (ONDEXConcept c : cs) {
            ccs.add(c.getOfType());
        }

        if (ccs.size() == 1) {
            return ccs.iterator().next();
        }

        ConceptClass mostSpecific = graph.getMetaData().getConceptClass("Thing");
        for (ConceptClass cc : ccs) {

            mostSpecific = moreSpecific(cc, mostSpecific);

            if (mostSpecific == null) {
                throw new InconsistencyException("Inconsistent concept class " +
                        "assignment in merge group " + groupID + ":\n" + prettyCs(cs));
            }
        }

        return mostSpecific;
    }

    /**
     * determines the deeper of two given concept classes in terms
     * of hierarchy.<br>
     * Worst case runtime: O(d) where d = hierarchy depth
     *
     * @param cc1
     * @param cc2
     * @return the deeper of the two, or null if on different branches.
     */
    public static ConceptClass moreSpecific(ConceptClass cc1, ConceptClass cc2) {
        if (cc1.isAssignableFrom(cc2)) {
            return cc2;
        } else if (cc1.isAssignableTo(cc2)) {
            return cc1;
        } else {
            return null;
        }
    }
}
