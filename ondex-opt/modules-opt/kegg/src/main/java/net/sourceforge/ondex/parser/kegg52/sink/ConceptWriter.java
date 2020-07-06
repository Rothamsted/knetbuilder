/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.sink;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.type.NullValueEvent;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.util.ConceptAccMapping;
import net.sourceforge.ondex.parser.kegg52.util.Util;

/**
 * @author taubertj
 */
public class ConceptWriter {

    //conceptId
    private Map<String, Integer> writtenConceptTranslations = new HashMap<String, Integer>(100000);
    private Set<String> writtenConcepts = new HashSet<String>(100000);


    private HashSet<String> unknownAccessions = new HashSet<String>();
    private ONDEXGraph og;

    public ConceptWriter(ONDEXGraph og) {
        this.og = og;
        cvs = new HashMap<String, DataSource>(200);
        ccs = new HashMap<String, ConceptClass>(200);
    }

    public Integer getWrittenConceptId(String concept) {
        return writtenConceptTranslations.get(concept.toUpperCase());
    }

    public boolean conceptONDEXIDIsWritten(Integer id) {
        return writtenConceptTranslations.containsValue(id);
    }

    public boolean conceptParserIDIsWritten(String name) {
        return writtenConcepts.contains(name.toUpperCase());
    }

    public void cleanup() {
        writtenConcepts = null;
        writtenConceptTranslations = null;
        et = null;
        uan = null;
        taxan = null;
        ptan = null;
        urlan = null;
        gran = null;
        cvs = null;
        ccs = null;

    }

    private EvidenceType et;
    private AttributeName uan;
    private AttributeName taxan;
    private AttributeName ptan;
    private AttributeName urlan;
    private AttributeName gran;

    private HashMap<String, DataSource> cvs;
    private HashMap<String, ConceptClass> ccs;

    public void conceptAccsPostProcessing(Set<ConceptAcc> conceptAccs) {

        // fill list of used CVs
        Iterator<ConceptAcc> itCA = conceptAccs.iterator();
        ConceptAcc conceptAcc = null;
        while (itCA.hasNext()) {

            conceptAcc = itCA.next();
            String cv = conceptAcc.getElement_of();

            // get KEGG internal mapping of DataSource string
            if (ConceptAccMapping.mapping.containsKey(cv)) {
                conceptAcc.setElement_of(ConceptAccMapping.mapping.get(cv));
            }
        }
    }


    public void createConcept(Concept concept) {

        writtenConcepts.add(concept.getId());

        Util.getTaxidForConcept(concept);

        if (concept.getConceptAccs() != null) {
            conceptAccsPostProcessing(concept.getConceptAccs());
        }

        if (et == null) {
            et = og.getMetaData().getEvidenceType(MetaData.EVIDENCE_IMPD);
        }
        if (uan == null) {
            uan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_UBIQUITOUS);
        }
        if (taxan == null) {
            taxan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_TAXID);
        }
        if (ptan == null) {
            ptan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_POSTAG);
        }
        if (urlan == null) {
            urlan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_URL);
        }
        if (gran == null) {
            gran = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_GRAPHICAL);
        }

        if (concept == null)
            throw new NullPointerException("Concept is null");

        if (concept.getId() == null)
            throw new NullPointerException("Concept ID is null");

        if (et == null)
            throw new NullPointerException("EvidenceType returned null");

        DataSource dataSource = cvs.get(concept.getElement_of());
        if (dataSource == null) {

            if (concept.getElement_of().trim().length() == 0) {
                throw new NullPointerException("empty cv " + concept.getId());
            }

            dataSource = og.getMetaData().getDataSource(concept.getElement_of());
            if (dataSource == null) {
                System.out.println("DataSource not found: " + concept.getElement_of());
            }
            cvs.put(concept.getElement_of(), dataSource);
        }

        ConceptClass cc = ccs.get(concept.getOf_type_fk());
        if (cc == null) {
            cc = og.getMetaData().getConceptClass(concept.getOf_type_fk());
            ccs.put(concept.getOf_type_fk(), cc);
        }

        if (dataSource == null)
            throw new NullPointerException("DataSource returned null");

        if (cc == null)
            throw new NullPointerException("ConceptClass returned null");

        String description = "";
        if (concept.getDescription() != null)
            description = concept.getDescription();

        if (Parser.DEBUG) {
            System.out.println("MAKING CONCEPT");
            System.out.println("ID-->" + concept.getId());
            System.out.println("DESC-->" + description);
            System.out.println("DataSource-->" + dataSource.getId());
            System.out.println("CC-->" + cc.getId());
        }

        ONDEXConcept ac = og.getFactory().createConcept(
                concept.getId().toUpperCase(), description, dataSource, cc, et);

        if (ac == null) {
            Parser.propagateEventOccurred(new NullValueEvent("ConceptCreation returned null", "CreateConcept - run " + Thread.currentThread()));
        }
        writtenConceptTranslations.put(ac.getPID().toUpperCase(), ac.getId());

        for (String context : concept.getContext()) {
            Integer existingId = writtenConceptTranslations.get(context.toUpperCase());
            if (existingId != null)
                ac.addTag(og.getConcept(existingId));
            else
                System.err.println("|" + context.toUpperCase() + "|Context not found");
        }

        if (concept.isSelfContext()) {
            ac.addTag(ac);
        }

        if (concept.getGraphical_x() != 0 && concept.getGraphical_y() != 0) {
            Point2D.Float p = new Point2D.Float(
                    concept.getGraphical_x(),
                    concept.getGraphical_y());
            ac.createAttribute(gran, p, false);
        }

        if (concept.getTaxid() != null)
            ac.createAttribute(taxan, concept.getTaxid(), true);

        if (concept.getUrl() != null)
            ac.createAttribute(urlan, concept.getUrl(), false);

        if (concept.getConceptNames() != null) {
            Iterator<ConceptName> it = concept.getConceptNames().iterator();
            while (it.hasNext()) {
                ConceptName conceptName = it.next();
                ac.createConceptName(conceptName.getName(), conceptName.isPreferred());
            }
        }

        if (concept.getConceptAccs() != null) {
            Iterator<ConceptAcc> it = concept.getConceptAccs().iterator();
            while (it.hasNext()) {
                ConceptAcc conceptAcc = it.next();

                if (!conceptAcc.getElement_of().equalsIgnoreCase("RN")) {

                    String metadatadb = MetaData.getMapping(conceptAcc.getElement_of().toUpperCase());

                    if (metadatadb == null) {
                        metadatadb = conceptAcc.getElement_of().toUpperCase();
                    }

                    DataSource accDataSource = cvs.get(metadatadb);
                    if (accDataSource == null) {

                        if (metadatadb != null
                                && metadatadb.trim().length() > 0
                                && !unknownAccessions.contains(metadatadb)) {
                            accDataSource = og.getMetaData().getDataSource(metadatadb);
                            if (accDataSource == null) {
                                unknownAccessions.add(metadatadb);
                                System.out.println("Unknown Accession database: " + metadatadb);
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    if (conceptAcc.getConcept_accession().length() > 0) {

                        //if (concept.getOf_type_fk().equals(MetaData.CC_PROTEIN)
                        //		&& accCV.getId(s).equals(MetaData.CV_TAIR))
                        //	conceptAcc.setAmbiguous(true);
                        if (conceptAcc.getElement_of().equals(MetaData.CV_CAS))
                            conceptAcc.setAmbiguous(false);
                        if (conceptAcc.getElement_of().equals(MetaData.CV_UNIPROT))
                            conceptAcc.setAmbiguous(false);
                        if (conceptAcc.getElement_of().equals(MetaData.CV_EC)
                                && concept.getOf_type_fk().equals(MetaData.CV_EC))
                            conceptAcc.setAmbiguous(false);
                        else if (conceptAcc.getElement_of().equals(MetaData.CV_EC))
                            conceptAcc.setAmbiguous(true);


                        ac.createConceptAccession(conceptAcc.getConcept_accession().toUpperCase(), accDataSource, conceptAcc.isAmbiguous());
                    }
                }
            }
        }
    }
}
