/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.genes;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;

import java.util.Iterator;

/**
 * @author taubertj
 */
public class GenesPathwayParser {

    public static void parseAndWrite(DPLPersistantSet<Pathway> pathways,
                                     DPLPersistantSet<Relation> relationsCache) {

        EntityCursor<Pathway> cursor = pathways.getCursor();
        int all = (int) pathways.size();

        Iterator<Pathway> itPath = cursor.iterator();
        int count = 0;
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            Iterator<Entry> itEntries = pathway.getEntries().values()
                    .iterator();
            while (itEntries.hasNext()) {
                Entry entry = itEntries.next();
                if (entry.getType().equalsIgnoreCase("group")) {

                    // create new protein complex
                    Concept protcmplx = new Concept("GROUP:" + pathway.getId()
                            + "_" + entry.getId() + "_PR", MetaData.CV_KEGG,
                            MetaData.CC_PROTCOMP);
                    // a protein complex is a protein itself
                    protcmplx.setDescription("kegg protein complex");

                    // get relations to components of proteincomplx
                    boolean hasComponents = false;
                    Iterator<Entry> entryCompIt = entry.getComponents()
                            .values().iterator();
                    while (entryCompIt.hasNext()) {
                        Entry component = entryCompIt.next();
                        // add each concept id from each component
                        Iterator<String> conIDsit = component.getConceptIDs()
                                .iterator();
                        while (conIDsit.hasNext()) {
                            String id = conIDsit.next().toUpperCase();
                            if (id.indexOf("_GE") > -1
                                    || id.indexOf("_PR") > -1
                                    || id.indexOf("_EN") > -1) {
                                id = id.substring(0, id.length() - 3);
                            }

                            String cIDPR = id + "_PR";
                            String cIDEN = id + "_EN";
                            String cIDGE = id + "_GE";

                            if (Parser.getConceptWriter()
                                    .conceptParserIDIsWritten(cIDPR)) {
                                Relation r = new Relation(cIDPR, protcmplx
                                        .getId(), MetaData.RT_IS_PART_OF);
                                r.setFrom_element_of(MetaData.CV_KEGG);
                                r.setTo_element_of(MetaData.CV_KEGG);
                                if (relationsCache.contains(r.pk)) {
                                    r = relationsCache.get(r.pk);
                                }
                                r.addContext(pathway.getId());
                                relationsCache.add(r);
                                hasComponents = true;
                            } else if (Parser.getConceptWriter()
                                    .conceptParserIDIsWritten(cIDEN)) {
                                Relation r = new Relation(cIDEN, protcmplx
                                        .getId(), MetaData.RT_IS_PART_OF);
                                r.setFrom_element_of(MetaData.CV_KEGG);
                                r.setTo_element_of(MetaData.CV_KEGG);
                                if (relationsCache.contains(r.pk)) {
                                    r = relationsCache.get(r.pk);
                                }
                                r.addContext(pathway.getId());
                                relationsCache.add(r);
                                hasComponents = true;
                            } else if (Parser.getConceptWriter()
                                    .conceptParserIDIsWritten(cIDGE)) {
                                Relation r = new Relation(cIDGE, protcmplx
                                        .getId(), MetaData.RT_IS_PART_OF);
                                r.setFrom_element_of(MetaData.CV_KEGG);
                                r.setTo_element_of(MetaData.CV_KEGG);
                                if (relationsCache.contains(r.pk)) {
                                    r = relationsCache.get(r.pk);
                                }
                                r.addContext(pathway.getId());
                                relationsCache.add(r);
                                hasComponents = true;
                            } else if (Parser.getConceptWriter()
                                    .conceptParserIDIsWritten(id)) {
                                Relation r = new Relation(id,
                                        protcmplx.getId(),
                                        MetaData.RT_IS_PART_OF);
                                r.setFrom_element_of(MetaData.CV_KEGG);
                                r.setTo_element_of(MetaData.CV_KEGG);
                                if (relationsCache.contains(r.pk)) {
                                    r = relationsCache.get(r.pk);
                                }
                                r.addContext(pathway.getId());
                                relationsCache.add(r);
                                hasComponents = true;
                            } else {
                                Parser
                                        .propagateEventOccurred(new GeneralOutputEvent(
                                                id + " not in written concepts",
                                                ""));
                            }
                        }
                    }

                    if (hasComponents) {
                        // let entry know about its concepts
                        entry.getConceptIDs().add(protcmplx.getId());
                        // write concept to file
                        Parser.getUtil().writeConcept(protcmplx);
                    }
                }
            }
            count++;
            if (count % 10 == 0 || count == all) {
                System.out.println(count + "of " + pathways.size()
                        + " Gene Pathways Parsed");
            }
            try {
                cursor.update(pathway);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        pathways.closeCursor(cursor);

        System.out.println("Writing relations");
        Parser.getUtil().writeRelations(relationsCache);
        System.out.println("Done!!");

    }
}
