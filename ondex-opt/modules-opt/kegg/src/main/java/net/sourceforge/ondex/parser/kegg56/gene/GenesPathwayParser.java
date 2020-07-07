/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.gene;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;

import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg56.MetaData;
import net.sourceforge.ondex.parser.kegg56.Parser;
import net.sourceforge.ondex.parser.kegg56.data.Entry;
import net.sourceforge.ondex.parser.kegg56.data.Pathway;
import net.sourceforge.ondex.parser.kegg56.sink.Concept;
import net.sourceforge.ondex.parser.kegg56.sink.Relation;
import net.sourceforge.ondex.parser.kegg56.util.DPLPersistantSet;

/**
 * @author taubertj
 */
public class GenesPathwayParser {

    public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
                                     DPLPersistantSet<Relation> relationsCache) throws MetaDataMissingException, InconsistencyException {

        EntityCursor<Pathway> cursor = pathways.getCursor();
        int all = (int) pathways.size();

        int count = 0;
        for (Pathway pathway : cursor) {
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equalsIgnoreCase("group")) {

                    // create new protein complex
                    Concept protcmplx = new Concept("GROUP:" + pathway.getId()
                            + "_" + entry.getId() + "_PR", MetaData.CV_KEGG,
                            MetaData.CC_PROTCOMP);
                    // a protein complex is a protein itself
                    protcmplx.setDescription("kegg protein complex");

                    // get relations to components of proteincomplx
                    boolean hasComponents = false;
                    for (Entry component : entry.getComponents().values()) {
                        // add each concept id from each component
                        for (String id : component.getConceptIDs()) {
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
                                if (relationsCache.contains(r.pk)) {
                                    r = relationsCache.get(r.pk);
                                }
                                r.addContext(pathway.getId());
                                relationsCache.add(r);
                                hasComponents = true;
                            } else {
                                try {
                                    throw new Exception(id + " not in written concepts");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
                System.out.println(count + " of " + pathways.size()
                        + " Gene Pathways Parsed");
            }
            try {
                cursor.update(pathway);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        pathways.closeCursor(cursor);

        Parser.getUtil().writeRelations(relationsCache);
    }
}
