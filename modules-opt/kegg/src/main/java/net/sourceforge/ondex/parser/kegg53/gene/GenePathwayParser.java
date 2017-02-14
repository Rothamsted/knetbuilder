/*
 * Created on 29.04.2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.gene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;


/**
 * @author Jan
 */
public class GenePathwayParser {

    private DPLPersistantSet<Pathway> pathways;

    public GenePathwayParser(DPLPersistantSet<Pathway> pathways) {
        this.pathways = pathways;
    }

    public Map<String, Set<Entry>> parse() {
        final Pattern colonSplit = Pattern.compile(":");
        final Pattern spaceSplit = Pattern.compile("[ | ]+");

        Map<String, Set<Entry>> gene2GeneEntries = new HashMap<String, Set<Entry>>();
        EntityCursor<Pathway> cursor = pathways.getCursor();
        for (Pathway pathway : cursor) {
            for (Entry entry : pathway.getEntries().values()) {
                // it has to stay "gene"! because this is a KEGG type and has nothing to do with ConceptClass
                if (entry.getType().equalsIgnoreCase("gene")) {
                    String[] results = spaceSplit.split(entry.getName().toUpperCase());
                    String lastOrg = null;
                    for (String result : results) {
                        String[] parts = colonSplit.split(result);

                        if (parts.length == 2) {
                            String id = result;
                            lastOrg = parts[0];

                            Set<Entry> entries = gene2GeneEntries.get(id);
                            if (entries == null) {
                                entries = new HashSet<Entry>(1000);
                                gene2GeneEntries.put(id, entries);
                            }
                            entries.add(entry);
                            entry.getConceptIDs().add(id.toUpperCase());
                        } else if (parts.length == 1) {
                            if (lastOrg == null) {
                                lastOrg = pathway.getId().split(":")[0];
                                System.out.println("Warning: Unknown organism for gene in pathway for gene \"" + result + "\" and pathway \"" + pathway.getId() + "\" assuming gene is " + lastOrg);
                            }

                            String id = lastOrg + ":" + result;

                            Set<Entry> entries = gene2GeneEntries.get(id);
                            if (entries == null) {
                                entries = new HashSet<Entry>(1000);
                                gene2GeneEntries.put(id, entries);
                            }
                            entries.add(entry);
                            entry.getConceptIDs().add(id.toUpperCase());
                        } else {
                            System.out.println("Warning unusual gene format in pathway \"" + result + "\"");
                        }
                    }
                }
            }
            try {
                cursor.update(pathway);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        pathways.closeCursor(cursor);

        return gene2GeneEntries;
    }
}
