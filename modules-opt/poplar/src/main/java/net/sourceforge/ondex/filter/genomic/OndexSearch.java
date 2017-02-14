package net.sourceforge.ondex.filter.genomic;

import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Straight forward in-memory search implementation.
 *
 * @author keywan
 */
public class OndexSearch {

    /**
     * Searches different fields of a concept for a query or pattern
     *
     * @param ac
     * @param p
     * @param search
     * @return true iff one of the concept fields contains the query
     */
    public static boolean find(ONDEXConcept ac, Pattern p, String search) {

        Pattern undoHighlight = Pattern.compile("<SPAN style=\"BACKGROUND-COLOR: #ffff00\">(.*?)</SPAN>");
        boolean found = false;

        // search in pid
        String pid = ac.getPID();
        if (isMatching(p, search, pid))
            found = true;

        // search in annotation
        String anno = ac.getAnnotation();
        if (isMatching(p, search, anno))
            found = true;

        // search in description
        String desc = ac.getDescription();
        if (isMatching(p, search, desc))
            found = true;

        // search in concept names
        for (ConceptName cno : ac.getConceptNames()) {
            String cn = cno.getName();
            if (isMatching(p, search, cn))
                found = true;
        }

        // search in concept accessions
        for (ConceptAccession ca : ac.getConceptAccessions()) {
            String accession = ca.getAccession();
            if (isMatching(p, search, accession))
                found = true;
        }

        for (Attribute attribute : ac.getAttributes()) {
        	if(attribute.getOfType().getId().equals("AA") ||
        			attribute.getOfType().getId().equals("NA")){
        		continue;
        	}
        	if(attribute.getOfType().getId().startsWith("AA_") ||
        			attribute.getOfType().getId().startsWith("NA_")){
        		continue;
        	}
            String value = attribute.getValue().toString();
            //undo previous highlighting
            String undoText = undoHighlight.matcher(value).replaceAll("$1");

            if (isMatching(p, search, undoText)) {

                //search and replace all matching regular expressions
                String newGDS = p.matcher(undoText).replaceAll("<SPAN style=\"BACKGROUND-COLOR: #ffff00\">$0</SPAN>");
                attribute.setValue(newGDS);
                found = true;
            }
        }

        // if nothing found
        return found;
    }

    /**
     * Tests if there is a match of this query within the search string
     *
     * @param p      the pattern (if null then literal match .contains is used)
     * @param query  the query to match
     * @param target the target to match to
     * @return is there a match
     */
    public static boolean isMatching(Pattern p, String query, String target) {
        if (p != null) {
            // Matcher m = p.matcher(target); //.matches requires the matching
            // of the whole word this is too restrictive
            return p.matcher(target).find();
        } else {
            return target.toUpperCase().contains(query.toUpperCase());
        }
    }

}
