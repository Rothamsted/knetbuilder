package net.sourceforge.ondex.parser.uniprot.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.algorithm.annotationquality.GOTreeParser;
import net.sourceforge.ondex.algorithm.annotationquality.GoTerm;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.parser.uniprot.ArgumentNames;
import net.sourceforge.ondex.parser.uniprot.MetaData;
import net.sourceforge.ondex.parser.uniprot.Parser;
import net.sourceforge.ondex.parser.uniprot.sink.DbLink;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.sink.Publication;
import net.sourceforge.ondex.tools.MetaDataUtil;

/**
 * transforms the sink objects to concept with relations
 *
 * @author peschr
 */
public class Transformer {
    private ONDEXGraph graph;

    private ONDEXPluginArguments pa;

    private ConceptClass ccProtein;

    private ConceptClass ccPublication;

    private ConceptClass ccEC;

    private ConceptClass ccDisease;

    private ConceptClass ccMolFunc;

    private ConceptClass ccBioProc;

    private ConceptClass ccCelComp;

    private AttributeName attTaxId;

    private AttributeName attSequence;

    private AttributeName attTitle;

    private AttributeName attYear;

    private AttributeName attJournal;

    private AttributeName attPubType;
    
    private AttributeName attPhenotype;

    private HashMap<String, DataSource> dataSources = new HashMap<String, DataSource>();

    private DataSource dataSourceUniProt;
    private DataSource dataSourceUniProt_SwissProt;
    private DataSource dataSourceUniProt_TrEMBL;
    private DataSource dataSourcePubMed;
    private DataSource dataSourceEC;
    private DataSource dataSourceOMIM;
    private DataSource dataSourceDOI;
    private DataSource dataSourceGO;

    private EvidenceType etAutomaticallyCurated;
    private EvidenceType etManuallyCurated;
    private EvidenceType ev;

    private RelationType rtPublishedIn;
    private RelationType rtCatC;
    private RelationType rtInvIn;
    private RelationType rtHasFunc;
    private RelationType rtHasPart;
    private RelationType rtLocIn;

    private boolean addContextInformation = false;
    private GOTreeParser goTree;

    private HashMap<String, Integer> ecToConcept = new HashMap<String, Integer>();
    private HashMap<String, Integer> omimToConcept = new HashMap<String, Integer>();
    private HashMap<String, Integer> pubmedToConcept = new HashMap<String, Integer>();
    private HashMap<String, Integer> goToConcept = new HashMap<String, Integer>();

    public static HashSet<String> unknownCVs = new HashSet<String>();

    private Set<String> umambigProtinMetaData;

    public Transformer(ONDEXGraph graph, ONDEXPluginArguments pa, boolean addContextInformation, GOTreeParser goTree) {
        this.graph = graph;
        this.pa = pa;
        this.addContextInformation = addContextInformation;
        this.goTree = goTree;
        
        MetaDataUtil mdu = new MetaDataUtil(graph.getMetaData(), null);
        ConceptClass ccThing = graph.getMetaData().getConceptClass("Thing");
        if (ccThing == null)
        	graph.getMetaData().getFactory().createConceptClass("Thing");
        
        etManuallyCurated = mdu.safeFetchEvidenceType(MetaData.IMPD_MANUALLY_CURATED);
        etAutomaticallyCurated = mdu.safeFetchEvidenceType(MetaData.IMPD_AUTOMATICALLY_CURATED);
        
        dataSourceUniProt = mdu.safeFetchDataSource(MetaData.CV_UniProt);
        dataSourceUniProt_SwissProt = mdu.safeFetchDataSource(MetaData.CV_UniProt_SwissProt);
        dataSourceUniProt_TrEMBL = mdu.safeFetchDataSource(MetaData.CV_UniProt_TrEMBL);
        dataSourcePubMed = mdu.safeFetchDataSource(MetaData.CV_PubMed);
        dataSourceDOI = mdu.safeFetchDataSource(MetaData.CV_DOI);
        dataSourceGO = mdu.safeFetchDataSource(MetaData.CV_GO);
        dataSourceEC = mdu.safeFetchDataSource(MetaData.CV_EC);
        dataSourceOMIM = mdu.safeFetchDataSource(MetaData.CV_OMIM);
        
        attTaxId = mdu.safeFetchAttributeName(MetaData.ATR_TAXID, String.class);
        attSequence = mdu.safeFetchAttributeName(MetaData.ATR_SEQUENCE, String.class);
        attTitle = mdu.safeFetchAttributeName(MetaData.ATR_TITLE, String.class);
        attYear = mdu.safeFetchAttributeName(MetaData.ATR_YEAR, Integer.class);
        attJournal = mdu.safeFetchAttributeName(MetaData.ATR_JOURNAL, String.class);
        attPubType = mdu.safeFetchAttributeName(MetaData.ATR_PUBTYPE, String.class);
        attPhenotype = mdu.safeFetchAttributeName(MetaData.ATR_PHENOTYPE, String.class);
        
        ccEC = mdu.safeFetchConceptClass(MetaData.CC_EC, "", ccThing);
        ccMolFunc = mdu.safeFetchConceptClass(MetaData.CC_MOLFUNC, "", ccThing);
        ccBioProc = mdu.safeFetchConceptClass(MetaData.CC_BIOPROC, "", ccThing);
        ccCelComp = mdu.safeFetchConceptClass(MetaData.CC_CELCOMP, "", ccThing);
        ccDisease = mdu.safeFetchConceptClass(MetaData.CC_DISEASE, "", ccThing);
        ccProtein = mdu.safeFetchConceptClass(MetaData.CC_Protein, "", ccThing);
        ccPublication = mdu.safeFetchConceptClass(MetaData.CC_Publication, "", ccThing);
        
        rtPublishedIn = mdu.safeFetchRelationType(MetaData.RT_PUBLISHED_IN, "");
        rtCatC = mdu.safeFetchRelationType(MetaData.RT_CAT_CLASS, "");
        rtInvIn = mdu.safeFetchRelationType(MetaData.RT_INVOLVED_IN, "");
        rtHasFunc = mdu.safeFetchRelationType(MetaData.RT_HAS_FUNCTION, "");
        rtHasPart = mdu.safeFetchRelationType(MetaData.RT_PARTICIPATES_IN, "");
        rtLocIn = mdu.safeFetchRelationType(MetaData.RT_LOCATED_IN, "");
        
        dataSources.put("INTERPRO", mdu.safeFetchDataSource(MetaData.CV_InterPro));
        dataSources.put("EMBL", mdu.safeFetchDataSource(MetaData.CV_EMBL));
        dataSources.put("PFAM", mdu.safeFetchDataSource(MetaData.CV_Pfam));
        dataSources.put("PRODOM", mdu.safeFetchDataSource(MetaData.CV_ProDom));
        dataSources.put("PROSITE", mdu.safeFetchDataSource(MetaData.CV_PROSITE));
        dataSources.put("REFSEQ", mdu.safeFetchDataSource(MetaData.CV_RefSeq));
        dataSources.put("PIR", mdu.safeFetchDataSource(MetaData.CV_PIR));
        dataSources.put("UNIGENE", mdu.safeFetchDataSource(MetaData.CV_UniGene));
        dataSources.put("KEGG", mdu.safeFetchDataSource(MetaData.CV_KEGG));
        dataSources.put("EC", mdu.safeFetchDataSource(MetaData.CV_EC));
        dataSources.put("GO", mdu.safeFetchDataSource(MetaData.CV_GO));
        dataSources.put("GENEID", mdu.safeFetchDataSource(MetaData.CV_GeneId));
        dataSources.put("ENSEMBL", mdu.safeFetchDataSource(MetaData.CV_ENSEMBL));
        dataSources.put("ENSEMBLPLANTS", mdu.safeFetchDataSource(MetaData.CV_ENSEMBL));
        dataSources.put("GRAMENE", mdu.safeFetchDataSource(MetaData.CV_GRAMENE));
        dataSources.put("PRINTS", mdu.safeFetchDataSource(MetaData.CV_PRINTS));
        dataSources.put("TAIR", mdu.safeFetchDataSource(MetaData.CV_TAIR));
        dataSources.put("MIM", mdu.safeFetchDataSource(MetaData.CV_OMIM));
        dataSources.put("ENSEMBLFUNGI", mdu.safeFetchDataSource(MetaData.CV_SGD));

        umambigProtinMetaData = new HashSet<String>();
        umambigProtinMetaData.add(MetaData.CV_UniProt);
        umambigProtinMetaData.add(MetaData.CV_ENSEMBL);
        umambigProtinMetaData.add(MetaData.CV_SGD);
      //  umambigProtinMetaData.add(MetaData.CV_EMBL);
      //  umambigProtinMetaData.add(MetaData.CV_RefSeq);
      //  umambigProtinMetaData.add(MetaData.CV_PIR);
    }


    /**
     * @param protein
     */
    public ONDEXConcept transform(Protein protein) throws InvalidPluginArgumentException {

        if (protein.isManuallyCurated()) {
            ev = this.etManuallyCurated;
        } else {
            ev = this.etAutomaticallyCurated;
        }
        
        ONDEXConcept proteinConcept;
        if(protein.getDataset().equalsIgnoreCase("Swiss-Prot")){
        	proteinConcept = graph.getFactory().createConcept(protein.getPID(), dataSourceUniProt_SwissProt, ccProtein, ev);
        }else if(protein.getDataset().equalsIgnoreCase("TrEMBL")){
        	proteinConcept = graph.getFactory().createConcept(protein.getPID(), dataSourceUniProt_TrEMBL, ccProtein, ev);
        }else{
        	System.out.println("Protein has unknown dataset");
        	proteinConcept = graph.getFactory().createConcept(protein.getPID(), dataSourceUniProt, ccProtein, ev);
        }

        proteinConcept.setAnnotation(protein.getEntryStats());
        
        if(protein.getDisruptionPhenotype() != null){
//        	proteinConcept.setDescription("Disruption Phenotype: "+protein.getDisruptionPhenotype());
        	proteinConcept.createAttribute(attPhenotype, protein.getDisruptionPhenotype(), true);
        }

        Iterator<String> cvIt = protein.getAccessions().keySet().iterator();
        while (cvIt.hasNext()) {
            String cvName = cvIt.next();
            DataSource dataSource = graph.getMetaData().getDataSource(cvName);
            if (dataSource == null) {
                Parser.propagateEventOccurred(new DataSourceMissingEvent(cvName, Parser.getCurrentMethodName()));
                protein.getNames().add(cvName);
            }

            Iterator<String> itacc = protein.getAccessions().get(cvName).iterator();
            while (itacc.hasNext()) {
                String value = itacc.next().trim();
//                System.out.println("itAcc: "+ value +", ds:"+ dataSource.getId() +",cv:"+ cvName);

                if (dataSource != null && value != null && value.length() > 0) {
                    if (dataSource.getId().equals(MetaData.CV_EC)) {
                        lookforECAccession(value, dataSource, proteinConcept);
                    } else if (dataSource.getId().equals(MetaData.CV_TAIR)) {
                        int prefix = value.indexOf(":");
                        if (prefix > -1) {
                            value = value.substring(prefix, value.length());
                            proteinConcept.createConceptAccession(value, dataSource, false);
//                            System.out.println("1: "+value);
                        }

                        int spliceVarient = value.indexOf(".");
                        if (spliceVarient > -1) {
                            String locus = value.substring(0, spliceVarient);
                            proteinConcept.createConceptAccession(locus, dataSource, true);
//                            System.out.println("2: "+value);
                        }
                    } else if (umambigProtinMetaData.contains(dataSource.getId())) {
                    	if(dataSource.getId().equals(MetaData.CV_UniProt) || dataSource.getId().equals(MetaData.CV_SGD)){
                        	proteinConcept.createConceptAccession(value, dataSource, false);
//                        	System.out.println("unambig: " + value);
                        }
//                    	proteinConcept.createConceptAccession(value, dataSource, false);
                    } else {
                        proteinConcept.createConceptAccession(value, dataSource, true);
//                        System.out.println("3: "+value);
                    }
                }
            }
        }

        if (addContextInformation) proteinConcept.addTag(proteinConcept);
        if (protein.getTaxId() != null)
            proteinConcept.createAttribute(attTaxId, protein.getTaxId(), false);
        if (protein.getSequence() != null) {
            // proteinConcept.createAttribute(attSequence, protein.getSequence(), false);
        } else {
            System.err.println("unknown error, but protein has no sequence data " + protein.toString());
        }


        for (DbLink dbLink : protein.getDbReferences()) {
            DataSource dataSource = null;
//            lookForTAIRAccession(dbLink.getAccession().trim(), proteinConcept);
            if ((dataSource = dataSources.get(dbLink.getDbName().toUpperCase())) != null) {
                String value = dbLink.getAccession().trim();
//                System.out.print("DbLink: value:"+ value +", ds:"+ dataSource.getId());
                if (value.length() > 0 && proteinConcept.getConceptAccession(value, dataSource) == null)

                    if (dataSource.getId().equals(MetaData.CV_EC)) {
                        lookforECAccession(value, dataSource, proteinConcept);
                    } else if (dataSource.getId().equals(MetaData.CV_OMIM)) {
                        lookforOMIMAccession(value, proteinConcept);
                    } else if (dataSource.getId().equals(MetaData.CV_GO) && goTree != null) {
                        lookforGOAccessions(dbLink, proteinConcept);
                    } else if (umambigProtinMetaData.contains(dataSource.getId())) {
                    	if(dataSource.getId().equals(MetaData.CV_UniProt) || dataSource.getId().equals(MetaData.CV_SGD)){
                        	proteinConcept.createConceptAccession(value, dataSource, false);
//                        	System.out.println("unambig2: " + value);
                        }
//                    	else if(dataSource.getId().equals(MetaData.CV_ENSEMBL) && value.contains(".")){
//                    	proteinConcept.createConceptAccession(value, dataSource, false);
////                        System.out.println("1:"+value);
//                        }
//                        else {
//                        	proteinConcept.createConceptAccession(value, dataSource, true);
////                        	System.out.println("2:"+value);
//                        }
//                        System.out.print("value:"+ value);
                    } else { // for others like InterPro, PFAM
                       // proteinConcept.createConceptAccession(value, dataSource, true);
                    }
             // ToDo: for others like InterPro, PFAM, create concepts & relations like in OMIM, GO above
                
                //handle DBs that can have a locus as id (e.g. LOC_Os02g15760.1, AT1G12345.2)
//                if(dataSource.getId().equals(MetaData.CV_TAIR) ||
//                		dataSource.getId().equals(MetaData.CV_ENSEMBL)){
//                    int prefix = value.indexOf(":");
//                    if (prefix > -1) {
//                        value = value.substring(prefix, value.length());
//                        proteinConcept.createConceptAccession(value, dataSource, false);
////                        System.out.println("3:" +value);
//                    }
//
//                    int spliceVarient = value.indexOf(".");
//                    if (spliceVarient > -1) {
//                        String locus = value.substring(0, spliceVarient);
//                        proteinConcept.createConceptAccession(locus, dataSource, true);
////                        System.out.println("4:" +locus);
//                    }
//                }

            } else {
                unknownCVs.add(dbLink.getDbName());
            }
        }

        Iterator<String> preferedNames = protein.getPreferedNames().iterator();
        while (preferedNames.hasNext()) {
            String prefName = preferedNames.next().trim();
            proteinConcept.createConceptName(prefName, true);
        }

        Iterator<String> names = protein.getNames().iterator();
        while (names.hasNext()) {
            String name = names.next().trim();
            if (!lookForTAIRAccession(name, proteinConcept)) {
                if (proteinConcept.getConceptName(name) == null)
                    proteinConcept.createConceptName(name, false);
            }
        }
        Boolean hideLargeScaleRefs = (Boolean) pa.getUniqueValue(ArgumentNames.HIDE_LARGE_SCALE_PUBLICATIONS_ARG);
        for (Publication pub : protein.getPublication()) {
            for (DbLink dbLink : pub.getReferences()) {
                if (dbLink.getDbName().equals("PubMed")) {
                    //ignore large scale publications
                    if ((hideLargeScaleRefs != null && hideLargeScaleRefs)) {
                        if (pub.isLargeScalePaper()) {
                            continue;
                        }
                    }
                    String value = dbLink.getAccession().toUpperCase().trim();
                    if (pubmedToConcept.get(value) == null) {
                        ONDEXConcept publicationConcept = graph.getFactory().createConcept(value, dataSourceUniProt, ccPublication, ev);
                        publicationConcept.createConceptName("PMID:" + value, true);
                        publicationConcept.createConceptAccession(value, dataSourcePubMed, false);
                        pubmedToConcept.put(value, publicationConcept.getId());

                        // set DOI accession
                        for (DbLink ref : pub.getReferences()) {
                            if (ref.getDbName().equals("DOI")) {
                                String doi = ref.getAccession().toUpperCase().trim();
                                if (publicationConcept.getConceptAccession(doi, dataSourceDOI) == null) {
                                    publicationConcept.createConceptAccession(doi, dataSourceDOI, false);
                                }
                            }
                        }

//                        if (pub.getTitle() != null && !pub.getTitle().equals("")) {
//                            if (publicationConcept.getAttribute(attTitle) == null)
//                                publicationConcept.createAttribute(attTitle, pub.getTitle(), false);
//                        }
//
//                        if (pub.getYear() > 0) {
//                            if (publicationConcept.getAttribute(attYear) == null)
//                                publicationConcept.createAttribute(attYear, pub.getYear(), false);
//                        }

//                        if (pub.getJournalName() != null && !pub.getJournalName().equals("")) {
//                            if (publicationConcept.getAttribute(attJournal) == null)
//                                publicationConcept.createAttribute(attJournal, pub.getJournalName(), false);
//                        }

                        if (pub.getScopes().size() > 0 && pub.getScopes().toString().length() > 0) {
                            publicationConcept.setAnnotation(pub.getScopes().toString());
                            publicationConcept.createAttribute(attPubType, pub.getScopes().toString(), false);
                        }

                    }
                    ONDEXConcept publicationConcept = graph.getConcept(pubmedToConcept.get(value));
                    ONDEXRelation relation = graph.getFactory().createRelation(proteinConcept, publicationConcept,
                            rtPublishedIn, ev);
                    if (addContextInformation) {
                        publicationConcept.addTag(proteinConcept);
                        relation.addTag(proteinConcept);
                    }
                }

            }
        }
        
        return proteinConcept;
    }

    //matches EC concepts
    private static final Pattern ec = Pattern.compile("([0-9]){1,1}((((\\.[0-9]{1,3})|(\\.-)){0,2}(\\.-){1,1})|(((\\.[0-9]{1,3})|(-\\.)){1,2}((\\.[0-9]{1,3})|(\\.-)){1,1})){1,1}");

    /**
     * Evaluates if a value is an EC term and creates a proper concept if it is
     *
     * @param value          potential EC value
     * @param dataSource             the cv you think it is (can be null)
     * @param proteinConcept protein the accession is on
     */
    private void lookforECAccession(String value, DataSource dataSource, ONDEXConcept proteinConcept) {
        value = value.toUpperCase();
        if ((dataSource != null && dataSource.getId().equals(MetaData.CV_EC))
                || value.startsWith("EC")
                || ec.matcher(value).find()) {

            if (value.startsWith("EC")) {
                value = value.substring(2, value.length()).trim();
            }

            if (ecToConcept.get(value) == null) {
                ONDEXConcept ecConcept = graph.getFactory().createConcept(value, dataSourceUniProt, ccEC, ev);
                ecConcept.createConceptAccession(value, dataSourceEC, false);
                ecToConcept.put(value, ecConcept.getId());
            }

            ONDEXConcept ecConcept = graph.getConcept(ecToConcept.get(value));
            ONDEXRelation rel = graph.getFactory().createRelation(proteinConcept, ecConcept, rtCatC, ev);

            if (addContextInformation) {
	            ecConcept.addTag(ecConcept);
	            proteinConcept.addTag(ecConcept);
	            rel.addTag(ecConcept);
            }    

        }
    }

    //matches GO concepts
    private static final Pattern go = Pattern.compile("GO:\\d{7}");

    /**
     * Finds out the ConceptClass of the GO term and creates the appropriate
     * relation between the protein and the GO term
     * <p/>
     *
     * @param dblink         dblink
     * @param proteinConcept protein the accession is on
     */
    private void lookforGOAccessions(DbLink dblink, ONDEXConcept proteinConcept) {
        String value = dblink.getAccession().trim();

        if (go.matcher(value).find()) {

            //find out the name-space of the GO term
            ConceptClass cc = null;
            RelationType rt = null;

            int id = Integer.parseInt(value.substring(3));
            int namespace = goTree.getNamespaceOfTerm(id);

            switch (namespace) {
                case GoTerm.DOMAIN_BIOLOGICAL_PROCESS:
                    cc = ccBioProc;
                    rt = rtHasPart;
                    break;
                case GoTerm.DOMAIN_MOLECULAR_FUNCTION:
                    cc = ccMolFunc;
                    rt = rtHasFunc;
                    break;
                case GoTerm.DOMAIN_CELLULAR_COMPONENT:
                    cc = ccCelComp;
                    rt = rtLocIn;
                    break;
                default:
                    break;
            }

            ONDEXConcept goConcept;

            //create GO concept
            if (goToConcept.get(value) == null) {
                goConcept = graph.getFactory().createConcept(value, dataSourceUniProt, cc, ev);
                goConcept.createConceptAccession(value, dataSourceGO, false);
                goToConcept.put(value, goConcept.getId());
            } else {
                goConcept = graph.getConcept(goToConcept.get(value));
            }

            Set<String> evidences = dblink.getEvidence();

            List<EvidenceType> v = new ArrayList<EvidenceType>(evidences.size());
//            v.add(ev);

            for (String evidence : evidences) {
                EvidenceType evi = graph.getMetaData().getEvidenceType(evidence);
                if (evi == null) {
                    evi = graph.getMetaData().getFactory().createEvidenceType(evidence);
                }
                v.add(evi);
            }
            ONDEXRelation relation = graph.getRelation(proteinConcept, goConcept, rt);
            if (relation == null) {
                graph.createRelation(proteinConcept, goConcept, rt, v);
            } else {
                for (EvidenceType evidencesR : v)
                    relation.addEvidenceType(evidencesR);
            }
        }
    }

    //matches OMIM concepts
    private static final Pattern omim = Pattern.compile("\\d{6}");

    /**
     * Evaluates if a value is an OMIM term and creates a proper concept if it is
     *
     * @param value          potential OMIM value
     * @param proteinConcept protein the accession is on
     */
    private void lookforOMIMAccession(String value, ONDEXConcept proteinConcept) {
        if (omim.matcher(value).find()) {
            if (omimToConcept.get(value) == null) {
                ONDEXConcept con = graph.getFactory().createConcept(value, dataSourceUniProt, ccDisease, ev);
                con.createConceptAccession(value, dataSourceOMIM, false);
                omimToConcept.put(value, con.getId());
            }
            ONDEXConcept omimConcept = graph.getConcept(omimToConcept.get(value));

            graph.getFactory().createRelation(proteinConcept, omimConcept, rtInvIn, ev);
        }
    }

    //matches atg (TAIR/TIGR) accessions
    private static final Pattern atgPattern = Pattern.compile("(AT[CM1-5][G][0-9]+([.][0-9]+)?)", Pattern.CASE_INSENSITIVE);

    /**
     * Looks for a tair accession on the given name and creates an appropriate accession if there is one
     *
     * @param name           the potential TAIR term
     * @param proteinConcept the protein concept
     * @return if this was a tair accession
     */
    private boolean lookForTAIRAccession(String name, ONDEXConcept proteinConcept) {
        name = name.toUpperCase();
        Matcher m = atgPattern.matcher(name);
        if (m.find()) {
            DataSource dataSource = dataSources.get("TAIR");
            String tairAcc = m.group(1);
//            if(tairAcc.indexOf(".") > 0){
//            	if (proteinConcept.getConceptAccession(tairAcc, dataSource) == null){
//            		proteinConcept.createConceptAccession(tairAcc, dataSource, false);
//            	}	
//            }
//            else{
//                if (proteinConcept.getConceptAccession(tairAcc, dataSource) == null){
//                    proteinConcept.createConceptAccession(tairAcc, dataSource, true);
//                } 
//            }
        
            return true;
        }
        return false;
    }
}
