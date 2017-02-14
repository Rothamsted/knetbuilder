/*
 * Created on 19-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.relation;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.data.Subtype;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptWriter;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;
import net.sourceforge.ondex.parser.kegg52.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author taubertj
 */
public class RelationPathwayParser {

    private static String stripEnding(String s) {
        if (s.indexOf("_GE") > -1 || s.indexOf("_PR") > -1
                || s.indexOf("_EN") > -1) {
            return (s.substring(0, s.length() - 3)).toUpperCase();
        }
        return s;
    }

    private ConceptWriter cw;

    private Set<String> ecrelErrors = new HashSet<String>(50);

    private Set<String> hiddenCompounds = new HashSet<String>(500);

    private DPLPersistantSet<Relation> relationsCache;

    private Util util;

    private ArrayList<String> writtenConcepts = new ArrayList<String>();

    private String currentPathway = null;

    public void parseAndWrite(DPLPersistantSet<Pathway> pathways,
                              DPLPersistantSet<Relation> relationsCache) {

        this.relationsCache = relationsCache;
        this.cw = Parser.getConceptWriter();
        this.util = Parser.getUtil();

        EntityCursor<Pathway> cursor = pathways.getCursor();
        Iterator<Pathway> itPath = cursor.iterator();
        while (itPath.hasNext()) {
            Pathway pathway = itPath.next();
            currentPathway = pathway.getId();
            Iterator<net.sourceforge.ondex.parser.kegg52.data.Relation> itRel = pathway
                    .getRelations().iterator();
            while (itRel.hasNext()) {
                net.sourceforge.ondex.parser.kegg52.data.Relation r = itRel
                        .next();
                Entry entry1 = r.getEntry1();
                Entry entry2 = r.getEntry2();

                // enzyme-enzyme relation means each partner have to be an
                // enzyme
                if (r.getType().equalsIgnoreCase("ECrel")) {
                    // get subtype of relation
                    Iterator<Subtype> subtypeIt = r.getSubtype().iterator();
                    while (subtypeIt.hasNext()) {
                        Subtype subtype = subtypeIt.next();

                        if (subtype.getName().equalsIgnoreCase("compound"))
                            processIntermediate_ofEnzyme(pathway, entry1,
                                    entry2, subtype);
                        else if (subtype.getName().equalsIgnoreCase(
                                "hidden compound"))
                            processIntermediate_ofEnzyme(pathway, entry1,
                                    entry2, subtype);
                        else
                            System.out.println("ECrel " + subtype.getName()
                                    + " as a subtype has been ignored");
                    }
                    // gene expression interaction where both entries are
                    // proteins
                } else if (r.getType().equalsIgnoreCase("GErel")) {

                    Iterator<Subtype> subtypeIt = r.getSubtype().iterator();
                    while (subtypeIt.hasNext()) {
                        Subtype subtype = subtypeIt.next();

                        if (subtype.getName().equalsIgnoreCase("expression"))
                            processExpressed_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "repression"))
                            processRepressed_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase("indirect"))
                            processIndirect_by(entry1, entry2);
                        else
                            System.out.println("GErel " + subtype.getName()
                                    + " as a subtype has been ignored");
                    }
                } else if (r.getType().equalsIgnoreCase("PCrel")) {

                    if (r.getSubtype().size() == 0) {
                        // protein compound interaction there is no subtype
                        processInteracts_with(entry1, entry2);
                    } else {

                        // protein compound interaction
                        Iterator<Subtype> subtypeIt = r.getSubtype().iterator();

                        // get subtype of relation
                        while (subtypeIt.hasNext()) {
                            Subtype subtype = subtypeIt.next();

                            if (subtype.getName()
                                    .equalsIgnoreCase("activation"))
                                processActivated_byCompound(entry1, entry2);
                            else if (subtype.getName().equalsIgnoreCase(
                                    "inhibition"))
                                processInhibited_byCompound(entry1, entry2);
                            else if (subtype.getName().equalsIgnoreCase(
                                    "binding/association"))
                                processBinds_toCompound(entry1, entry2);
                            else
                                System.out.println("PCrel " + subtype.getName()
                                        + " as a subtype has been ignored");
                        }
                    }

                } else if (r.getType().equalsIgnoreCase("PPrel")) {

                    // protein protein interaction
                    Iterator<Subtype> subtypeIt = r.getSubtype().iterator();

                    // get subtype of relation
                    while (subtypeIt.hasNext()) {
                        Subtype subtype = subtypeIt.next();

                        if (subtype.getName().equalsIgnoreCase("activation"))
                            processActivated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "inhibition"))
                            processInhibited_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "phosphorylation"))
                            processPhosphorylated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "dephosphorylation"))
                            processDephosphorylated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase("indirect"))
                            processIndirect_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "dissociation"))
                            processDissociated_from(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "binding/association"))
                            processBinds_to(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "methylation"))
                            processMethylated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "glycosylation"))
                            processGlycosylated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "demethylation"))
                            processDemethylated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "ubiquitination")
                                || subtype.getName().equals("ubiquination"))
                            processUbiquinated_by(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase(
                                "state change")
                                || subtype.getName().equals("state"))
                            processStatechange_from(entry1, entry2);
                        else if (subtype.getName().equalsIgnoreCase("compound"))
                            processIntermediate_ofProtein(pathway, entry1,
                                    entry2, subtype);
                        else
                            System.out.println("PPrel " + subtype.getName()
                                    + " as a subtype has been ignored");
                    }
                    // maplink between map and enzyme
                } else if (r.getType().equalsIgnoreCase("maplink")) {
                    Iterator<Subtype> subtypeIt = r.getSubtype().iterator();
                    while (subtypeIt.hasNext()) {
                        Subtype subtype = subtypeIt.next();

                        if (subtype.getName().equalsIgnoreCase("compound"))
                            processIntermediate_ofPathway(pathway, entry1,
                                    entry2, subtype);
                        else
                            System.out.println("maplink " + subtype.getName()
                                    + " as a subtype has been ignored");
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

        for (String s : ecrelErrors)
            System.out.println(s);
        ecrelErrors.clear();

        System.out.println("Writing relations");
        util.writeRelations(relationsCache);

        hiddenCompounds.clear();
        writtenConcepts.clear();

        cw = null;
        util = null;
    }

    private void processActivated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ac_by between protein and protein
                        Relation ac_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_ACTIVATED_BY);
                        ac_by.setFrom_element_of(MetaData.CV_KEGG);
                        ac_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(ac_by.pk)) {
                            ac_by = relationsCache.get(ac_by.pk);
                        }
                        ac_by.addContext(currentPathway);
                        relationsCache.add(ac_by);
                    } else {
                        System.out.println(concept2ID + " not found. processActivated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processActivated_by");
            }
        }
    }

    private void processActivated_byCompound(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = it2.next();
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ac_by between protein and compound
                        Relation ac_by = new Relation(conceptID, concept2ID,
                                MetaData.RT_ACTIVATED_BY);
                        ac_by.setFrom_element_of(MetaData.CV_KEGG);
                        ac_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(ac_by.pk)) {
                            ac_by = relationsCache.get(ac_by.pk);
                        }
                        ac_by.addContext(currentPathway);
                        relationsCache.add(ac_by);
                    } else {
                        System.out.println(concept2ID + " not found. processActivated_byCompound");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processActivated_byCompound");
            }
        }
    }

    private void processBinds_to(Entry entry1, Entry entry2) {
        // go through all concepts of entry2
        Iterator<String> it = entry2.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry1
                Iterator<String> it2 = entry1.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // bi_to between two proteins
                        Relation bi_to = new Relation(concept2ID, conceptID,
                                MetaData.RT_BINDS_TO);
                        bi_to.setFrom_element_of(MetaData.CV_KEGG);
                        bi_to.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(bi_to.pk)) {
                            bi_to = relationsCache.get(bi_to.pk);
                        }
                        bi_to.addContext(currentPathway);
                        relationsCache.add(bi_to);
                    } else {
                        System.out.println(concept2ID + " not found. processBinds_to");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processBinds_to");
            }
        }
    }

    private void processBinds_toCompound(Entry entry1, Entry entry2) {
        // go through all concepts of entry2
        Iterator<String> it = entry2.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = it.next();
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry1
                Iterator<String> it2 = entry1.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // bi_to between protein and compound
                        Relation bi_to = new Relation(conceptID, concept2ID,
                                MetaData.RT_BINDS_TO);
                        bi_to.setFrom_element_of(MetaData.CV_KEGG);
                        bi_to.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(bi_to.pk)) {
                            bi_to = relationsCache.get(bi_to.pk);
                        }
                        bi_to.addContext(currentPathway);
                        relationsCache.add(bi_to);
                    } else {
                        System.out.println(concept2ID + " not found. processBinds_toCompound");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processBinds_toCompound");
            }
        }
    }

    private void processDemethylated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // dm_by between two proteins
                        Relation dm_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_DEMETHYLATED_BY);
                        dm_by.setFrom_element_of(MetaData.CV_KEGG);
                        dm_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(dm_by.pk)) {
                            dm_by = relationsCache.get(dm_by.pk);
                        }
                        dm_by.addContext(currentPathway);
                        relationsCache.add(dm_by);
                    } else {
                        System.out.println(concept2ID + " not found. processDemethylated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processDemethylated_by");
            }
        }
    }

    private void processDephosphorylated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // de_by between two proteins
                        Relation de_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_DEPHOSPHORYLATED_BY);
                        de_by.setFrom_element_of(MetaData.CV_KEGG);
                        de_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(de_by.pk)) {
                            de_by = relationsCache.get(de_by.pk);
                        }
                        de_by.addContext(currentPathway);
                        relationsCache.add(de_by);
                    } else {
                        System.out.println(concept2ID + " not found. processDephosphorylated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processDephosphorylated_by");
            }
        }
    }

    private void processDissociated_from(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // di_fr between two proteins
                        Relation di_fr = new Relation(concept2ID, conceptID,
                                MetaData.RT_DISSOCIATED_FROM);
                        di_fr.setFrom_element_of(MetaData.CV_KEGG);
                        di_fr.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(di_fr.pk)) {
                            di_fr = relationsCache.get(di_fr.pk);
                        }
                        di_fr.addContext(currentPathway);
                        relationsCache.add(di_fr);
                    } else {
                        System.out.println(concept2ID + " not found. processDissociated_from");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processDissociated_from");
            }
        }
    }

    private void processExpressed_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> cof21it = entry1.getConceptIDs().iterator();
        while (cof21it.hasNext()) {
            String conceptID = stripEnding(cof21it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> cofe2it = entry2.getConceptIDs().iterator();
                while (cofe2it.hasNext()) {
                    String concept2ID = stripEnding(cofe2it.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ex_by between two proteins
                        Relation ex_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_EXPRESSED_BY);
                        ex_by.setFrom_element_of(MetaData.CV_KEGG);
                        ex_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(ex_by.pk)) {
                            ex_by = relationsCache.get(ex_by.pk);
                        }
                        ex_by.addContext(currentPathway);
                        relationsCache.add(ex_by);
                    } else {
                        System.out.println(concept2ID + " not found. processExpressed_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processExpressed_by");
            }
        }
    }

    private void processGlycosylated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ac_by between two proteins
                        Relation gl_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_GLYCOSYLATED_BY);
                        gl_by.setFrom_element_of(MetaData.CV_KEGG);
                        gl_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(gl_by.pk)) {
                            gl_by = relationsCache.get(gl_by.pk);
                        }
                        gl_by.addContext(currentPathway);
                        relationsCache.add(gl_by);
                    } else {
                        System.out.println(concept2ID + " not found. processGlycosylated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processGlycosylated_by");
            }
        }
    }

    private void processIndirect_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // id_by between two proteins
                        Relation id_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_INDERECTLY_EFFECTED_BY);
                        id_by.setFrom_element_of(MetaData.CV_KEGG);
                        id_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(id_by.pk)) {
                            id_by = relationsCache.get(id_by.pk);
                        }
                        id_by.addContext(currentPathway);
                        relationsCache.add(id_by);
                    } else {
                        System.out.println(concept2ID + " not found. processIndirect_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processIndirect_by");
            }
        }
    }

    private void processInhibited_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // in_by between protein and protein
                        Relation in_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_INHIBITED_BY);
                        in_by.setFrom_element_of(MetaData.CV_KEGG);
                        in_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(in_by.pk)) {
                            in_by = relationsCache.get(in_by.pk);
                        }
                        in_by.addContext(currentPathway);
                        relationsCache.add(in_by);
                    } else {
                        System.out.println(concept2ID + " not found. processInhibited_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processInhibited_by");
            }
        }
    }

    private void processInhibited_byCompound(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = it.next();
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // in_by between compound and protein
                        Relation in_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_INHIBITED_BY);
                        in_by.setFrom_element_of(MetaData.CV_KEGG);
                        in_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(in_by.pk)) {
                            in_by = relationsCache.get(in_by.pk);
                        }
                        in_by.addContext(currentPathway);
                        relationsCache.add(in_by);
                    } else {
                        System.out.println(concept2ID + " not found. processInhibited_byCompound");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processInhibited_byCompound");
            }
        }
    }

    private void processInteracts_with(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = (stripEnding(it.next()) + "_PR");
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = it2.next().toUpperCase();
                    if (concept2ID.startsWith("CPD:")
                            || concept2ID.startsWith("GL:")) {
                        // it_wi between a compound and a protein
                        Relation it_wi = new Relation(concept2ID, conceptID,
                                MetaData.RT_INTERACTS_WITH);
                        it_wi.setFrom_element_of(MetaData.CV_KEGG);
                        it_wi.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(it_wi.pk)) {
                            it_wi = relationsCache.get(it_wi.pk);
                        }
                        it_wi.addContext(currentPathway);
                        relationsCache.add(it_wi);
                    } else {
                        System.out.println(concept2ID + " not found. processInteracts_with");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processInteracts_with");
            }
        }
    }

    private void processIntermediate_ofEnzyme(Pathway pathway, Entry entry1,
                                              Entry entry2, Subtype subtype) {

        boolean hidden = subtype.getName().equalsIgnoreCase("hidden compound");

        // check it qualifies as reaction
        if (entry1.getReaction() == null) {
            ecrelErrors.add("Entry " + entry1.getName() + " in pathway "
                    + pathway.getId() + " part of ECrel but no reactions.");
            return;
        }

        // check it qualifies as reaction
        if (entry2.getReaction() == null) {
            ecrelErrors.add("Entry " + entry2.getName() + " in pathway "
                    + pathway.getId() + " part of ECrel but no reactions.");
            return;
        }

        // reaction for entry1, might be multiple
        for (String reactionID1 : entry1.getReaction().split(" ")) {

            if (!cw.conceptParserIDIsWritten(reactionID1)) {
                System.out.println("Reaction id 1 " + reactionID1
                        + " in pathway " + pathway.getId()
                        + " missing in written concepts.");
                return;
            }

            // reaction for entry2, might be multiple
            for (String reactionID2 : entry2.getReaction().split(" ")) {
                if (!cw.conceptParserIDIsWritten(reactionID2)) {
                    System.out.println("Reaction id 2 " + reactionID2
                            + " in pathway " + pathway.getId()
                            + " missing in written concepts.");
                    return;
                }

                // compound is a qualifier for the ordering relationship between
                // two reaction steps
                Entry compound = pathway.getEntries().get(subtype.getValue());
                if (compound != null) {
                    Iterator<String> it3 = compound.getConceptIDs().iterator();
                    while (it3.hasNext()) {
                        String comp = it3.next().toUpperCase();
                        if (!cw.conceptParserIDIsWritten(comp)) {
                            System.out.println("Compound id " + comp
                                    + " in pathway " + pathway.getId()
                                    + " missing in written concepts.");
                            continue;
                        }
                        Relation sh_im = new Relation(reactionID1, reactionID2,
                                MetaData.RT_SHARED_INTERMEDIATE);
                        sh_im.setFrom_element_of(MetaData.CV_KEGG);
                        sh_im.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(sh_im.pk)) {
                            sh_im = relationsCache.get(sh_im.pk);
                        }
                        sh_im.addContext(currentPathway);
                        relationsCache.add(sh_im);
                        if (hidden)
                            hiddenCompounds.add(comp);
                    }
                }
            }
        }
    }

    private void processIntermediate_ofPathway(Pathway pathway, Entry entry1,
                                               Entry entry2, Subtype subtype) {

        // decide which entry is the map entry
        if (entry1.getType().equals("map")) {

            // check if map is present
            if (!cw.conceptParserIDIsWritten(entry1.getName())) {
                ecrelErrors.add("Missing KEGG map " + entry1.getName());
                return;
            }

            // check it qualifies as reaction
            if (entry2.getReaction() == null) {
                ecrelErrors.add("Entry " + entry2.getId() + " in pathway "
                        + pathway.getId() + " part of ECrel but no reactions.");
                return;
            }

            // reaction for entry2, might be multiple
            for (String reactionID2 : entry2.getReaction().split(" ")) {
                if (!cw.conceptParserIDIsWritten(reactionID2)) {
                    System.out.println("Reaction id 2 " + reactionID2
                            + " in pathway " + pathway.getId()
                            + " missing in written concepts.");
                    return;
                }

                // compound is a qualifier for the ordering relationship between
                // two reaction steps (one is pathway map)
                Entry compound = pathway.getEntries().get(subtype.getValue());
                if (compound != null) {
                    Iterator<String> it3 = compound.getConceptIDs().iterator();
                    while (it3.hasNext()) {
                        String comp = it3.next().toUpperCase();
                        if (!cw.conceptParserIDIsWritten(comp)) {
                            System.out.println("Compound id " + comp
                                    + " in pathway " + pathway.getId()
                                    + " missing in written concepts.");
                            continue;
                        }
                        Relation sh_im = new Relation(entry1.getName(),
                                reactionID2, MetaData.RT_SHARED_INTERMEDIATE);
                        sh_im.setFrom_element_of(MetaData.CV_KEGG);
                        sh_im.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(sh_im.pk)) {
                            sh_im = relationsCache.get(sh_im.pk);
                        }
                        sh_im.addContext(currentPathway);
                        relationsCache.add(sh_im);
                    }
                }
            }
        } else {

            // check if map is present
            if (!cw.conceptParserIDIsWritten(entry2.getName())) {
                ecrelErrors.add("Missing KEGG map " + entry2.getName());
                return;
            }

            // check it qualifies as reaction
            if (entry1.getReaction() == null) {
                ecrelErrors.add("Entry " + entry1.getId() + " in pathway "
                        + pathway.getId() + " part of ECrel but no reactions.");
                return;
            }

            // reaction for entry1, might be multiple
            for (String reactionID1 : entry1.getReaction().split(" ")) {
                if (!cw.conceptParserIDIsWritten(reactionID1)) {
                    System.out.println("Reaction id 1 " + reactionID1
                            + " in pathway " + pathway.getId()
                            + " missing in written concepts.");
                    return;
                }

                // compound is a qualifier for the ordering relationship between
                // two reaction steps (one is pathway map)
                Entry compound = pathway.getEntries().get(subtype.getValue());
                if (compound != null) {
                    Iterator<String> it3 = compound.getConceptIDs().iterator();
                    while (it3.hasNext()) {
                        String comp = it3.next().toUpperCase();
                        if (!cw.conceptParserIDIsWritten(comp)) {
                            System.out.println("Compound id " + comp
                                    + " in pathway " + pathway.getId()
                                    + " missing in written concepts.");
                            continue;
                        }
                        Relation sh_im = new Relation(reactionID1, entry2
                                .getName(), MetaData.RT_SHARED_INTERMEDIATE);
                        sh_im.setFrom_element_of(MetaData.CV_KEGG);
                        sh_im.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(sh_im.pk)) {
                            sh_im = relationsCache.get(sh_im.pk);
                        }
                        sh_im.addContext(currentPathway);
                        relationsCache.add(sh_im);
                    }
                }
            }
        }
    }

    private void processIntermediate_ofProtein(Pathway pathway, Entry entry1,
                                               Entry entry2, Subtype subtype) {
        HashSet<String> proteins1 = new HashSet<String>();
        HashSet<String> proteins2 = new HashSet<String>();

        // got through all concepts of entry1
        Iterator<String> it2 = entry1.getConceptIDs().iterator();
        while (it2.hasNext()) {
            String conceptID = stripEnding(it2.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                proteins1.add(conceptID);
            }
        }

        // go through all concepts of entry2
        it2 = entry2.getConceptIDs().iterator();
        while (it2.hasNext()) {
            String conceptID = stripEnding(it2.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                proteins2.add(conceptID);
            }
        }

        // go through all pairs of proteins
        Iterator<String> protein1it = proteins1.iterator();
        while (protein1it.hasNext()) {
            String protein1 = protein1it.next().toUpperCase();
            Iterator<String> protein2it = proteins2.iterator();
            while (protein2it.hasNext()) {
                String protein2 = protein2it.next().toUpperCase();
                Entry compound = pathway.getEntries().get(subtype.getValue());
                if (compound != null) {
                    Iterator<String> it3 = compound.getConceptIDs().iterator();
                    while (it3.hasNext()) {
                        // sh_im between protein1 and protein2 and compound
                        Relation sh_im = new Relation(protein1, protein2, 
                                MetaData.RT_SHARED_INTERMEDIATE);
                        sh_im.setFrom_element_of(MetaData.CV_KEGG);
                        sh_im.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(sh_im.pk)) {
                            sh_im = relationsCache.get(sh_im.pk);
                        }
                        sh_im.addContext(currentPathway);
                        relationsCache.add(sh_im);
                    }
                }
            }
        }
    }

    private void processMethylated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // me_by between two proteins
                        Relation me_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_METHYLATED_BY);
                        me_by.setFrom_element_of(MetaData.CV_KEGG);
                        me_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(me_by.pk)) {
                            me_by = relationsCache.get(me_by.pk);
                        }
                        me_by.addContext(currentPathway);
                        relationsCache.add(me_by);
                    } else {
                        System.out.println(concept2ID + " not found. processMethylated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processMethylated_by");
            }
        }
    }

    private void processPhosphorylated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ph_by between two proteins
                        Relation ph_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_PHOSPHORYLATED_BY);
                        ph_by.setFrom_element_of(MetaData.CV_KEGG);
                        ph_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(ph_by.pk)) {
                            ph_by = relationsCache.get(ph_by.pk);
                        }
                        ph_by.addContext(currentPathway);
                        relationsCache.add(ph_by);
                    } else {
                        System.out.println(concept2ID + " not found. processPhosphorylated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processPhosphorylated_by");
            }
        }
    }

    private void processRepressed_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> cofe1it = entry1.getConceptIDs().iterator();
        while (cofe1it.hasNext()) {
            String conceptID = stripEnding(cofe1it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> cofe2it = entry2.getConceptIDs().iterator();
                while (cofe2it.hasNext()) {
                    String concept2ID = stripEnding(cofe2it.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // re_by between two proteins
                        Relation re_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_REPRESSED_BY);
                        re_by.setFrom_element_of(MetaData.CV_KEGG);
                        re_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(re_by.pk)) {
                            re_by = relationsCache.get(re_by.pk);
                        }
                        re_by.addContext(currentPathway);
                        relationsCache.add(re_by);
                    } else {
                        System.out.println(concept2ID + " not found. processRepressed_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processRepressed_by");
            }
        }
    }

    private void processStatechange_from(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // st_fr between two proteins
                        Relation st_fr = new Relation(concept2ID, conceptID,
                                MetaData.RT_STATE_CHANGED_FROM);
                        st_fr.setFrom_element_of(MetaData.CV_KEGG);
                        st_fr.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(st_fr.pk)) {
                            st_fr = relationsCache.get(st_fr.pk);
                        }
                        st_fr.addContext(currentPathway);
                        relationsCache.add(st_fr);
                    } else {
                        System.out.println(concept2ID + " not found. processStatechange_from");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processStatechange_from");
            }
        }
    }

    private void processUbiquinated_by(Entry entry1, Entry entry2) {
        // go through all concepts of entry1
        Iterator<String> it = entry1.getConceptIDs().iterator();
        while (it.hasNext()) {
            String conceptID = stripEnding(it.next()) + "_PR";
            if (cw.conceptParserIDIsWritten(conceptID)) {
                // go through all concepts of entry2
                Iterator<String> it2 = entry2.getConceptIDs().iterator();
                while (it2.hasNext()) {
                    String concept2ID = stripEnding(it2.next()) + "_PR";
                    if (cw.conceptParserIDIsWritten(concept2ID)) {
                        // ub_by between two proteins
                        Relation ub_by = new Relation(concept2ID, conceptID,
                                MetaData.RT_UBIQUINATED_BY);
                        ub_by.setFrom_element_of(MetaData.CV_KEGG);
                        ub_by.setTo_element_of(MetaData.CV_KEGG);
                        if (relationsCache.contains(ub_by.pk)) {
                            ub_by = relationsCache.get(ub_by.pk);
                        }
                        ub_by.addContext(currentPathway);
                        relationsCache.add(ub_by);
                    } else {
                        System.out.println(concept2ID + " not found. processUbiquinated_by");
                    }
                }
            } else {
                System.out.println(conceptID + " not found. processUbiquinated_by");
            }
        }
    }
}
