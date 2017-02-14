/*
 * Created on 16-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.enzyme;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.MetaData;
import net.sourceforge.ondex.parser.kegg53.Parser;
import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.sink.Concept;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg53.sink.Relation;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * @author taubertj
 */ 
public class EnzymePathwayParser {

    private ArrayList<String> writtenIds = new ArrayList<String>();

    public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
                              DPLPersistantSet<Relation> relationsCache) throws MetaDataMissingException {

        final Pattern plusSplit = Pattern.compile("[+]");
        final Pattern spaceSplit = Pattern.compile(" ");

        EntityCursor<Pathway> cursor = pathways.getCursor();
        for (Pathway pathway : cursor) {
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equalsIgnoreCase(MetaData.CC_ENZYME)) {
                    String[] results = spaceSplit.split(entry.getName().toUpperCase());
                    for (String result : results) {

                        //String[] ecs = FastSplit.fastSplit(result, FastSplit.COLON);
                        String[] plus = plusSplit.split(result);
                        for (String idpart : plus) {
                            String id = idpart.toUpperCase() + "_EN";

                            //Enzyme for ec number
                            Concept concept_enzyme = new Concept(id, MetaData.CV_KEGG, MetaData.CC_ENZYME);
                            concept_enzyme.setDescription("Infered enzyme for EC in pathway");
                            if (entry.getLink() != null)
                                concept_enzyme.setUrl(entry.getLink());

                            entry.getConceptIDs().add(concept_enzyme.getId());

                            concept_enzyme.addContext(pathway.getId());

                            if (!writtenIds.contains(concept_enzyme.getId())) {
                                writtenIds.add(concept_enzyme.getId());
                                Parser.getUtil().writeConcept(concept_enzyme);
                            }

                            //Introduce new ec class
                            Concept ec = new Concept(idpart, MetaData.CV_KEGG, MetaData.CC_EC);
                            if (!Parser.getConceptWriter().conceptParserIDIsWritten(ec.getId())) {
                                ec.setDescription("ec from entry name");

                                ConceptName cn = new ConceptName(ec.getId(), ec.getId());
                                //the first name is preferred
                                cn.setPreferred(true);
                                ec.getConceptNames().add(cn);

                                if (ec.getId().toUpperCase().startsWith("EC:")) {
                                    String ecNumber = ec.getId().replaceAll("[^0-9|\\.|\\-]", "");
                                    ConceptAcc ca = new ConceptAcc(ecNumber, ecNumber, MetaData.CV_EC);
                                    ec.getConceptAccs().add(ca);
                                }

                                if (!writtenIds.contains(ec.getId())) {
                                    writtenIds.add(ec.getId());
                                    Parser.getUtil().writeConcept(ec, false);
                                }
                            }

                            //relation between ec and enzyme
                            Relation cat_c = new Relation(concept_enzyme.getId(), ec.getId(), MetaData.RT_CATALYSEING_CLASS);
                            cat_c.setFrom_element_of(MetaData.CV_KEGG);
                            cat_c.setTo_element_of(MetaData.CV_KEGG);
                            if (relationsCache.contains(cat_c.pk))
                                cat_c = relationsCache.get(cat_c.pk);
                            cat_c.addContext(pathway.getId());
                            relationsCache.add(cat_c);

                            Relation m_isp = new Relation(ec.getId(), pathway.getId(), MetaData.RT_MEMBER_PART_OF);
                            m_isp.setFrom_element_of(MetaData.CV_KEGG);
                            m_isp.setTo_element_of(MetaData.CV_KEGG);
                            if (relationsCache.contains(m_isp.pk))
                                m_isp = relationsCache.get(m_isp.pk);
                            m_isp.addContext(pathway.getId());
                            relationsCache.add(m_isp);
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
        Parser.getUtil().writeRelations(relationsCache);

        writtenIds.clear();
    }
}
