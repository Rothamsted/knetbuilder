/*
 * Created on 27-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.comp;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Merges pathway maps information with parsing results from compound and glycan
 * database.
 *
 * @author taubertj
 */
public class CompPathwayMerger {

    /**
     * Include only those compounds mentioned in pathway maps.
     *
     * @param pathways KGML files
     * @param concepts extracted from compound and glycan files
     */
    public void mergeAndWrite(DPLPersistantSet<Pathway> pathways,
                              Map<String, Concept> concepts) {
        final Pattern spaceSplit = Pattern.compile(" ");

        EntityCursor<Pathway> cursor = pathways.getCursor();
        Iterator<Pathway> itPath = cursor.iterator();
        // writes links and conceptIDs
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            Concept concept = null;

            // go through all entries and find compound concepts
            Iterator<Entry> itEntries = pathway.getEntries().values()
                    .iterator();
            while (itEntries.hasNext()) {
                Entry entry = itEntries.next();
                if (entry.getType().equalsIgnoreCase("compound")) {
                    String[] results = spaceSplit.split(entry.getName()
                            .toUpperCase());
                    for (String result : results) {
                        // necessary as glycans are referred to in both ways
                        result = result.replaceAll("GLYCAN", "GL");
                        concept = concepts.get(result);

                        // if compound not in LIGAND database
                        if (concept == null) {
                            concept = new Concept(result, MetaData.CV_KEGG,
                                    MetaData.CC_COMPOUND);
                            concept.setDescription("abstract compound entry");
                        }

                        // URL of KEGG link
                        if (entry.getLink() != null) {
                            concept.setUrl(entry.getLink());
                        }

                        // these concepts have been created for this entry
                        entry.getConceptIDs().add(concept.getId());
                        if (!Parser.getConceptWriter()
                                .conceptParserIDIsWritten(concept.getId())) {
                            // write concept to ONDEX
                            Parser.getUtil().writeConcept(concept);
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
	}
}
