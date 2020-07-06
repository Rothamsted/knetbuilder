/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.sink;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.GenomeParser;
import net.sourceforge.ondex.parser.kegg53.MetaData;

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

    public int getWrittenConceptId(String concept) throws ConceptNotExistingException {
        if (!writtenConceptTranslations.containsKey(concept.toUpperCase())) {
            throw new ConceptNotExistingException(concept.toUpperCase());
        }
        return writtenConceptTranslations.get(concept.toUpperCase());
    }

    public boolean conceptONDEXIDIsWritten(int id) {
        return writtenConceptTranslations.containsValue(id);
    }

    public boolean conceptParserIDIsWritten(String name) {
        return writtenConcepts.contains(name.toUpperCase());
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
        while (itCA.hasNext()) {

            ConceptAcc conceptAcc = itCA.next();
            String cv = conceptAcc.getElement_of();
            
            // get KEGG internal mapping of DataSource string
            if (MetaData.getMapping(cv) != null) {
                conceptAcc.setElement_of(MetaData.getMapping(cv));
            }
        }
    }

    private static final Pattern colonPattern = Pattern.compile(":");

    /**
     * @param concept      ke
     * @param genomeParser
     * @throws MetaDataMissingException
     */
    public void createConcept(Concept concept, GenomeParser genomeParser, boolean speciesSpecific) throws MetaDataMissingException {

        writtenConcepts.add(concept.getId().trim().toUpperCase());

        String[] result = colonPattern.split(concept.getId());
        if (result.length == 2 && speciesSpecific) {
            GenomeParser.Taxonomony taxonomy = genomeParser.getTaxonomony(result[0].toLowerCase());
            if (taxonomy != null) {
                concept.setTaxid(String.valueOf(taxonomy.getTaxNumber()));
            } else {
                //System.err.println(result[0] + " is unknown KEGG species");
                throw new RuntimeException(result[0] + " is unknown KEGG species");
            }
        }
        if (concept.getConceptAccs() != null)
            conceptAccsPostProcessing(concept.getConceptAccs());

        if (et == null)
            et = og.getMetaData().getEvidenceType(MetaData.EVIDENCE_IMPD);
        if (et == null)
            throw new EvidenceTypeMissingException(MetaData.EVIDENCE_IMPD);

        if (uan == null)
            uan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_UBIQUITOUS);
        if (uan == null)
            throw new AttributeNameMissingException(MetaData.ATTR_NAME_UBIQUITOUS);

        if (taxan == null)
            taxan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_TAXID);
        if (taxan == null)
            throw new AttributeNameMissingException(MetaData.ATTR_NAME_TAXID);

        if (ptan == null)
            ptan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_POSTAG);
        if (ptan == null)
            throw new AttributeNameMissingException(MetaData.ATTR_NAME_POSTAG);

        if (urlan == null)
            urlan = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_URL);
        if (urlan == null)
            throw new AttributeNameMissingException(MetaData.ATTR_NAME_URL);

        if (gran == null) {
            gran = og.getMetaData().getAttributeName(MetaData.ATTR_NAME_GRAPHICAL);
            if (gran == null)
                throw new AttributeNameMissingException(MetaData.ATTR_NAME_GRAPHICAL);
        }

        if (concept == null)
            throw new NullPointerException("Concept is null");

        if (concept.getId() == null)
            throw new NullPointerException("Concept ID is null");

        DataSource dataSource = cvs.get(concept.getElement_of());
        if (dataSource == null) {
            if (concept.getElement_of().trim().length() == 0)
                throw new DataSourceMissingException("empty cv " + concept.getId());

            dataSource = og.getMetaData().getDataSource(concept.getElement_of());
            cvs.put(concept.getElement_of(), dataSource);
        }

        if (dataSource == null)
            throw new DataSourceMissingException(concept.getElement_of());

        ConceptClass cc = ccs.get(concept.getOf_type_fk());
        if (cc == null) {
            cc = og.getMetaData().getConceptClass(concept.getOf_type_fk());
            ccs.put(concept.getOf_type_fk(), cc);
        }

        if (cc == null)
            throw new ConceptClassMissingException(concept.getOf_type_fk());

        String description = "";
        if (concept.getDescription() != null)
            description = concept.getDescription();

        ONDEXConcept ac = og.getFactory().createConcept(
                concept.getId().toUpperCase(), description, dataSource, cc, et);

        writtenConceptTranslations.put(concept.getId().trim().toUpperCase(), ac.getId());
        //System.out.println("WRITTEN " + ac.getPID().toUpperCase() + " " + concept.getId().toUpperCase().trim());

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

        if (concept.getTaxid() != null && concept.getTaxid().length() > 0)
            ac.createAttribute(taxan, concept.getTaxid(), true);

        if (concept.getUrl() != null && concept.getUrl().length() > 0)
            ac.createAttribute(urlan, concept.getUrl(), false);

        if (concept.getConceptNames() != null) {
            for (ConceptName conceptName : concept.getConceptNames()) {
                ac.createConceptName(conceptName.getName(), conceptName.isPreferred());
            }
        }

        if (concept.getConceptAccs() != null) {
            for (ConceptAcc conceptAcc : concept.getConceptAccs()) {
                if (!conceptAcc.getElement_of().equalsIgnoreCase("RN")) {

                    String metadatadb = MetaData.getMapping(conceptAcc.getElement_of());

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
                        else if (conceptAcc.getElement_of().equals(MetaData.CV_UNIPROT))
                            conceptAcc.setAmbiguous(false);
                        else if (conceptAcc.getElement_of().equals(MetaData.CV_EC)
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

    public static class ConceptNotExistingException extends Exception {

        /**
		 * default
		 */
		private static final long serialVersionUID = 1L;

		public ConceptNotExistingException(String message) {
            super(message);
        }
    }
}
