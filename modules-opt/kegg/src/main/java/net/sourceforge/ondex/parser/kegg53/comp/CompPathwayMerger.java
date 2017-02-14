/*
 * Created on 27-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.comp;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.MetaData;
import net.sourceforge.ondex.parser.kegg53.Parser;
import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.sink.Concept;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

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
                              Map<String, Concept> concepts) throws MetaDataMissingException {
        final Pattern spaceSplit = Pattern.compile(" ");

        EntityCursor<Pathway> cursor = pathways.getCursor();
        // writes links and conceptIDs
        for (Pathway pathway : cursor) {

            // go through all entries and find compound concepts
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equalsIgnoreCase("compound")) {
                    String[] results = spaceSplit.split(entry.getName().toUpperCase().trim());
                    for (String result : results) {
                        // necessary as glycans are referred to in both ways
                        result = result.trim().toUpperCase().replaceAll("GLYCAN", "GL");
                        Concept concept = concepts.get(result);

                        // if compound not in LIGAND database
                        if (concept == null) {
                            System.err.println("Entry \"" + result + "\"  is referenced in pathway \"" + pathway.getId() + "\" but was not found in compound, drug or glycan file");

                            concept = new Concept(result, MetaData.CV_KEGG,
                                    MetaData.CC_COMPOUND);
                            concept.setDescription("abstract compound entry");
                        }

                        // URL of KEGG link
                        if (entry.getLink() != null) {
                            concept.setUrl(entry.getLink());
                        }

                        // these concepts have been created for this entry
                        entry.getConceptIDs().add(concept.getId().trim().toUpperCase());
                        if (!Parser.getConceptWriter()
                                .conceptParserIDIsWritten(concept.getId())) {
                            // write concept to ONDEX
                            Parser.getUtil().writeConcept(concept, false);
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
