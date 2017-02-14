package uk.ac.ncl.cs.psimi2ondex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.psidev.mi.Attribute;
import net.sf.psidev.mi.AttributeList;
import net.sf.psidev.mi.Availability;
import net.sf.psidev.mi.BibrefType;
import net.sf.psidev.mi.BioSourceType;
import net.sf.psidev.mi.Confidence;
import net.sf.psidev.mi.CvType;
import net.sf.psidev.mi.DbReferenceType;
import net.sf.psidev.mi.ExperimentType;
import net.sf.psidev.mi.Interaction;
import net.sf.psidev.mi.InteractionElementType.InferredInteractionList.InferredInteraction;
import net.sf.psidev.mi.InteractionElementType.InferredInteractionList.InferredInteraction.Participant;
import net.sf.psidev.mi.Interactor;
import net.sf.psidev.mi.NamesType;
import net.sf.psidev.mi.NamesType.Alias;
import net.sf.psidev.mi.ParticipantType;
import net.sf.psidev.mi.ParticipantType.ExperimentalRoleList.ExperimentalRole;
import net.sf.psidev.mi.Source;
import net.sf.psidev.mi.XrefType;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.MetaDataLookup2;
import net.sourceforge.ondex.tools.MetaDataMatcher;

import org.apache.log4j.Level;

import uk.ac.ncl.cs.intbio.psimi.PSIMIProcessor;

/**
 * This class is the heart of the OndexPSIMIParser. It implements a number of
 * methods defined in the PSIMIProcessor interface. These methods get called
 * when new data entities become available.
 *
 * @author jweile
 */
public class OndexPsimiProcessor implements PSIMIProcessor {

    /**
     * The OndexParser object which governs the instantiation of this class.
     */
    private PsiMiParser parser;

    /**
     * Our current Ondex graph object.
     */
    private ONDEXGraph graph;


    //### Various dictionaries for metadata ###
    private MetaDataLookup2<ConceptClass> ccLookup;
    private MetaDataLookup2<DataSource> dataSourceLookup;
    private MetaDataLookup2<RelationType> rtLookup;
    private MetaDataLookup2<EvidenceType> etLookup;
    private MetaDataMatcher<DataSource> dataSourceMatcher;

    //### Specific metadata that are used in this class. ###
    private DataSource datasource;
    private ConceptClass ccExp, ccPub, ccInteractionFallBack, ccParticipant;
    private RelationType rtPubIn, rtExp, rtInferred, rtParticipates;
    private AttributeName anTaxid, anAA, anNA, anModelled, anIntraMolecular, anNegative, 
            anConfidences, anInferred, anAuthor, anYear, anJournal, anURL;
    private EvidenceType etIMPD;


//    /**
//     * A Dictionary for storing availability statements.
//     */
//    private Map<Integer, Availability> availabilities = new HashMap<Integer, Availability>();

    /**
     * A Map that links identifiers to Ondex concepts which have been created by this class.
     *
     * "ExperimentType_1" -> ONDEXConcept(CC=Experiment)
     * "Interactor_1" -> ONDEXConcept(CC=Interactor)
     * ...
     */
    private Map<String, ONDEXConcept> concepts = new HashMap<String, ONDEXConcept>();


    //#####CONSTRUCTOR#####

    public OndexPsimiProcessor(ONDEXGraph graph, PsiMiParser parser) throws Exception {
        this.parser = parser;
        this.graph = graph;

        ccLookup = new MetaDataLookup2<ConceptClass>(getLookupFileReader(), graph.getMetaData(), ConceptClass.class);
        dataSourceLookup = new MetaDataLookup2<DataSource>(getLookupFileReader(), graph.getMetaData(), DataSource.class);
        rtLookup = new MetaDataLookup2<RelationType>(getLookupFileReader(), graph.getMetaData(), RelationType.class);
        etLookup = new MetaDataLookup2<EvidenceType>(getLookupFileReader(), graph.getMetaData(), EvidenceType.class);
        dataSourceMatcher = new MetaDataMatcher<DataSource>(graph.getMetaData(), DataSource.class);

        initMetadata();
    }


    private void initMetadata() throws MetaDataMissingException {

        ccInteractionFallBack = parser.requireConceptClass(parser.getItFallBack());
        ccExp = parser.requireConceptClass("Experiment");
        ccPub = parser.requireConceptClass("Publication");
        ccParticipant = parser.requireConceptClass("Participant");

        rtPubIn = parser.requireRelationType("pub_in");
        rtExp = parser.requireRelationType("observed_in");
        rtInferred = parser.requireRelationType("inferred_from");
        rtParticipates = parser.requireRelationType("participates_in");

        etIMPD = parser.requireEvidenceType("IMPD");

        parser.requireDataSource("unknown");

        anTaxid = parser.requireAttributeName("TAXID");
        anInferred = parser.requireAttributeName("inferred");
        anAA = parser.requireAttributeName("AA");
        anNA = parser.requireAttributeName("NA");
        anAuthor = parser.requireAttributeName("AUTHORS");
        anYear = parser.requireAttributeName("YEAR");
        anJournal = parser.requireAttributeName("JOURNAL_REF");
        anURL = parser.requireAttributeName("URL");

        anModelled = parser.requireAttributeName("modelled");
        anIntraMolecular = parser.requireAttributeName("intraMolecular");
        anNegative = parser.requireAttributeName("negative");
        anConfidences = parser.requireAttributeName("confidences");

    }


    /**
     * Helper method that constructs a fresh reader on the metadata mapping file.
     * If no file has been specified, the internal file packaged in the jar will
     * be used.
     *
     * @return Reader object on the metadata mapping file.
     * @throws FileNotFoundException should never actually occur, as the file's
     * existence is already checked during argument processing.
     *
     */
    private Reader getLookupFileReader() throws FileNotFoundException {

        if (parser.getLookupFile() != null) {
            return new FileReader(parser.getLookupFile());
        } else {
            InputStream metadataStream = getClass().getClassLoader().getResourceAsStream("psimi2ondex_metadata.tsv");
            return new InputStreamReader(metadataStream);
        }

    }

    /**
     * A new set psimi entry starts. Clears all maps in order to start from scratch.
     */
    @Override
    public void nextEntry() {
//        availabilities.clear();
        concepts.clear();
    }

    /**
     * Called when a new Source object becomes available. The parser will try to
     * find a corresponding Ondex Metadata DataSource object and assign it to the field
     * <code>datasource</code>.
     * @param s a Source object
     */
    @Override
    public void processSource(Source s) {
        datasource = null;

        /*
         * Try to establish DataSource by PSI-MI Ontology term mapping.
         */
        String psiOntoId = getPsimiTerm(s.getXref());
        if (psiOntoId != null) {
            datasource = dataSourceLookup.get(psiOntoId);
            if (datasource == null) {
                complainAboutMissingCV(psiOntoId, "");
            }
        }

        /*
         * If unsuccessful try name based mapping instead.
         */
        if (datasource == null && s.getNames() != null) {
            datasource = dataSourceMatcher.get(s.getNames().getShortLabel());
            if (datasource == null && s.getNames().getFullName() != null) {
                datasource = dataSourceMatcher.get(s.getNames().getFullName());
            }
        }

        /*
         * If all else fails set source as `unknown'.
         */
        if (datasource == null) {
            try {
                datasource = parser.requireDataSource("unknown");
            } catch (DataSourceMissingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Called when a new Availability has been read. 
     * @param a an Availability object.
     */
    @Override
    public void processAvailability(Availability a) {

//        availabilities.put(a.getId(), a); 

    }

    /**
     * Called when a new Experiment description has been read. Delegates to the
     * private method <code>ONDEXConcept createExperimentConcept(ExperimentType e)</code>.
     * @param e An experiment description object
     */
    @Override
    public void processExperiment(ExperimentType e) {
        
        makeExperimentConcept(e);

    }

    /**
     * Creates and registers a concept of type experiment and fills it with the
     * information found in the ExperimentType object e.
     * @param e The experiment description object that holds the information
     * which will be transferred to the new concept.
     * @return the OndexConcept that was created.
     */
    private ONDEXConcept makeExperimentConcept(ExperimentType e) {

        ONDEXConcept c = createAndRegister(ccExp, e.getId());

        transferNames(e.getNames(), c);
        linkPublication(e.getBibref(), c);
        transferXRefs(e.getXref(), c);

        //set host organisms as TAXID GDSs
        if (e.getHostOrganismList() != null) {
            for (ExperimentType.HostOrganismList.
                    HostOrganism organism : e.getHostOrganismList().getList()) {
                c.createAttribute(anTaxid, organism.getNcbiTaxId()+"", false);//Fixing gds type to string
                //FIXME: could cause problem if more than one organism is listed
            }
        }

        //translate the detection-method into concept evidence
        if (e.getInteractionDetectionMethod() != null) {
            CvType idm = e.getInteractionDetectionMethod();
            String term = getPsimiTerm(idm.getXref());
            if (term != null ) {
                EvidenceType et = etLookup.get(term);
                if (et != null) {
                    c.addEvidenceType(et);
                } else {
                    complainAboutMissingEvidenceType(idm.getNames().getShortLabel(), term);
                }
            }
        } else {
            parser.complain("Missing interaction detection method in experiment #"+e.getId());
        }

        //add confidences as Attribute
        if (e.getConfidenceList() != null) {
            Map<String,String> confObj = new HashMap<String,String>();
            for (Confidence conf : e.getConfidenceList().getList()) {
                confObj.put(conf.getUnit().getNames().getShortLabel(), conf.getValue());
                c.createAttribute(anConfidences, confObj, false);
            }
        }

        return c;
    }

    /**
     * Called when a new Interactor element was read. Calls the private method
     * <code>ONDEXConcept createInteractorConcept(Interactor i)</code>.
     *
     * @param i
     */
    @Override
    public void processInteractor(Interactor i) {

        makeInteractorConcept(i);
        
    }

    /**
     * Determines the appropriate ConceptClass according to
     * <code>Interactor i</code>'s type. Then creates and registers a new concept
     * of that ConceptClass and fills it with content from <code>i</code>.
     * @param i the Interactor which will be translated to a concept.
     * @return the Concept that was created.
     * @throws NullValueException
     * @throws AccessDeniedException
     */
    private ONDEXConcept makeInteractorConcept(Interactor i) {

        /*
         * Determine concept class according to interactor type.
         */
        ConceptClass ccInteractor = null;

        String typeRef = getPsimiTerm(i.getInteractorType().getXref());

        if (typeRef != null) {

            ccInteractor = ccLookup.get(typeRef);

            if (ccInteractor == null) {
                complainAboutMissingCC(typeRef, "");
            }

        } else {
            //TODO extract concept class from name-based mapping
        }

        /*
         * If all else fails, use fallback class.
         */
        if (ccInteractor == null) {
            ccInteractor = ccParticipant;
        }

        /*
         * Create actual concept
         */
        ONDEXConcept c = createAndRegister(ccInteractor, "Interactor_" + i.getId());

        transferNames(i.getNames(), c);
        transferXRefs(i.getXref(), c);

        /*
         * Assign organism field as TAXID Attribute
         */
        BioSourceType organism = i.getOrganism();
        if (organism != null) {
            //convert taxid int to string to conform to metadata definition
            c.createAttribute(anTaxid, organism.getNcbiTaxId()+"", false);
            //TODO maybe extract potential compartment info
        }

        /*
         * Add sequence information as concept Attribute if user flag is set.
         */
        if (parser.isParsingSequences() && i.getSequence() != null) {
            if (ccInteractor.isAssignableTo(graph.getMetaData().getConceptClass("Protein"))) {
                c.createAttribute(anAA, i.getSequence(), false);
            } else if (ccInteractor.isAssignableTo(graph.getMetaData().getConceptClass("RNA"))) {
                c.createAttribute(anNA, i.getSequence(), false);
            }
        }

        //TODO process interactor attributes

        return c;

    }

    /**
     * Called when a new interaction has been parsed. Determines the appropriate
     * ConceptClass according to <code>Interaction i</code>'s type. Then creates
     * and registers a new concept of that ConceptClass and fills it with
     * content from <code>i</code>.
     * @param i The interaction object to be translated.
     */
    @Override
    public void processInteraction(Interaction i) {

//        log("Processing interaction #"+i.getId());
//        System.err.println("Processing interaction #"+i.getId());

        ONDEXConcept c = null;

        ConceptClass ccInteraction = null;
        if (i.getInteractionType() != null) {


            /*
             * PSIMI interactions can have multiple types at the same time.
             * In this case we create separate interaction concepts for each type.
             */
            for (CvType type : i.getInteractionType()) {
                String typeTerm = getPsimiTerm(type.getXref());

                if (typeTerm != null) {
                    ccInteraction = ccLookup.get(typeTerm);
                    if (ccInteraction == null) {
                        complainAboutMissingCC(type.getNames().getShortLabel() ,typeTerm);

                        //TODO: get CC from name matching?
                    }
                } else {
                    //TODO: get CC from name matching?
                }

                if (ccInteraction != null) {
                    c = createAndRegister(ccInteraction, "Interaction_"+i.getId());
                    fillInteractionConcept(c, i);
                } else {
                    c = createAndRegister(ccInteractionFallBack, "Interaction_"+i.getId());
                    fillInteractionConcept(c, i);
                }

            }


        } else {

            c = createAndRegister(ccInteractionFallBack, "Interaction_"+i.getId());
            fillInteractionConcept(c, i);
            
        }

    }

    /**
     * Fills the actual content into an interaction concept based on an interaction
     * object (i).
     * @param c the concept to fill
     * @param i the interaction object to read from.
     */
    private void fillInteractionConcept(ONDEXConcept c, Interaction i) {
        
        transferNames(i.getNames(), c);
        transferXRefs(i.getXref(), c);

//        Availability availability = i.getAvailabilityRef() != null ?
//                    availabilities.get(i.getAvailabilityRef()):
//                    i.getAvailability();
//
//        if (availability != null) {
//            
//        }

        /*
         * Create links to experiments and temporarily index them.
         */
        Map<Integer,ONDEXRelation> expLinks = new HashMap<Integer, ONDEXRelation>();
        Map<Integer,ONDEXConcept> expConcepts = new HashMap<Integer, ONDEXConcept>();

        if (i.getExperimentList() != null) {
            for (Object o : i.getExperimentList().getExperimentRefOrExperimentDescription()) {
                ONDEXConcept expConcept = null;
                int expId = -1;
                if (o instanceof Integer) {
                    expId = (Integer)o;
                    expConcept = concepts.get(ccExp.getId()+"_"+expId);
                } else if (o instanceof ExperimentType) {
                    expConcept = makeExperimentConcept((ExperimentType)o);
                    expId = ((ExperimentType)o).getId();
                }
                ONDEXRelation r = graph.getFactory().createRelation(c, expConcept, rtExp, etIMPD);

                expConcepts.put(expId, expConcept);
                expLinks.put(expId,r);
            }
        }

        /*
         * Process participants and index them temporarily.
         */
        Map<Integer,ONDEXConcept> participants = new HashMap<Integer, ONDEXConcept>();
        processParticipants(c, i.getParticipantList().getList(), participants);

        /*
         * Process inferred interactions, referring to indexes of experiments and participants.
         */
        if (i.getInferredInteractionList() != null) {
            processInferredInteractions(c, i.getInferredInteractionList().getList(), participants, expConcepts);
        }

        //Note: interaction type is already interpreted as concept class

        if (i.isModelled() != null && i.isModelled()) {
            c.createAttribute(anModelled, true, false);
        }

        if (i.isIntraMolecular() != null && i.isIntraMolecular()) {
            c.createAttribute(anIntraMolecular, true, false);
        }
//        else {//TODO introduce flag whether to create intraMolecular Attribute
//            c.createGDS(anIntraMolecular, false, false);
//        }

        if (i.isNegative() != null && i.isNegative()) {
            c.createAttribute(anNegative, true, false);
        }

        /*
         * create confidences on edge between interaction and experiment
         */
        if (i.getConfidenceList() != null) {
            for (Confidence conf : i.getConfidenceList().getList()) {
                Map<String,String> confObj = new HashMap<String,String>();
                confObj.put(conf.getUnit().getNames().getShortLabel(), conf.getValue());
                if (conf.getExperimentRefList() != null) {
                    //assign confidence to links to concerned experiment
                    for (int expId : conf.getExperimentRefList().getList()) {
                        ONDEXRelation rel = expLinks.get(expId);
                        rel.createAttribute(anConfidences, confObj, false);
                    }
                } else {
                    for (ONDEXRelation rel : expLinks.values()) {
                        rel.createAttribute(anConfidences, confObj, false);
                    }
                }
            }
        }

        //TODO process interaction parameters

        //TODO process interaction attributes

    }

    /**
     * Processes the list of participants of an interaction. All participants
     * are resolved to interactors, linked with the interaction concept and indexed.
     * @param c interaction concept
     * @param plist list of participants to process
     * @param participants dictionary in which the participant concepts will be indexed.
     */
    private void processParticipants(ONDEXConcept c, List<ParticipantType> plist,
                                            Map<Integer,ONDEXConcept> participants) {

        for (ParticipantType participant : plist) {

            /*
             * Resolve participants to interactors/ interactions
             */
            ONDEXConcept cPart = null;
            if (participant.getInteractorRef() != null) {
                cPart = concepts.get("Interactor_"+participant.getInteractorRef());
            } else if (participant.getInteractionRef() != null) {
                cPart = concepts.get("Interaction_"+participant.getInteractionRef());
            } else if (participant.getInteractor() != null) {
                cPart = makeInteractorConcept(participant.getInteractor());
            }

            if (cPart != null) {

                /*
                 * Indexing
                 */
                participants.put(participant.getId(), cPart);

                /*
                 * Determine relation type for connecting interactor to interaction.
                 */
                RelationType rtPart = null;

                if (c.getOfType().isAssignableTo(graph.getMetaData().getConceptClass("MI:0208"))) {//genetic interaction

                    for (ExperimentalRole erole : participant.getExperimentalRoleList().getList()) {
                        String psiTerm = getPsimiTerm(erole.getXref());
                        if (psiTerm.matches("MI:(049[6-8]|058[1-2])")) {//matches genetic interaction participant roles
                            rtPart = rtLookup.get(psiTerm);
                            break;
                        }
                    }

                /*
                 * if it's not a genetic interaction use biological role to determine participation type
                 */
                } else if (participant.getBiologicalRole() != null) {

                    String psiTerm = getPsimiTerm(participant.getBiologicalRole().getXref());

                    if (psiTerm != null) {
                        rtPart = rtLookup.get(psiTerm);
                    } else {
                        //TODO try to match names
                    }

                } else {
                    rtPart = rtParticipates;
                }

                ONDEXRelation partRel = graph.getFactory().createRelation(cPart, c, rtPart, etIMPD);


//                for (FeatureElementType feature : participant.getFeatureList().getList()) {
//                    //TODO: process features
//                }

                //process confidences
                if (participant.getConfidenceList() != null) {
                    Map<String,String> confObj = new HashMap<String,String>();
                    for (Confidence conf : participant.getConfidenceList().getList()) {
                        confObj.put(conf.getUnit().getNames().getShortLabel(), conf.getValue());
                    }
                    /*
                     * FIXME: confidence is specific to experiment, but we have
                     * no way of establishing a link from a Attribute to a different
                     * concept in Ondex.
                     */
                    partRel.createAttribute(anConfidences, confObj, false);
                }


            } else {
                parser.complain("Null participant!");
            }
        }
    }


    /**
     * private field for exclusive use of the processInferredInteractions method.
     */
    private int lastIIid = 0;

    /**
     * processes a list of inferred interactions.
     * @param c An interaction concept which spawns the inferred interactions.
     * @param iis List of inferred interaction objects
     * @param participants known participants of interaction c
     * @param experiments known experiments in which c has been observed.
     */
    private void processInferredInteractions(ONDEXConcept c, List<InferredInteraction> iis,
                                            Map<Integer,ONDEXConcept> participants,
                                            Map<Integer,ONDEXConcept> experiments) {

        for (InferredInteraction ii: iis) {

            /*
             * ignore all interactions with features. They are too complicated
             * to be properly represented in Ondex. Also they are not relevant
             * for our purposes.
             */
            for (Participant partRef : ii.getParticipantList()) {
                if (partRef.getParticipantFeatureRef() != null) {
                    return;
                }
            }

            /*
             * Create inferred interaction concept. And link it with the
             * original interaction concept.
             */
            ONDEXConcept cii = graph.getFactory().createConcept("InferredInteraction"+(++lastIIid), datasource, ccInteractionFallBack, etIMPD);
            graph.getFactory().createRelation(cii, c, rtInferred, etIMPD);

            /*
             * Link participants
             */
            for (Participant partRef : ii.getParticipantList()) {
                if (partRef.getParticipantRef() != null) {
                    ONDEXConcept participant = participants.get(partRef.getParticipantRef());
                    graph.getFactory().createRelation(participant, cii, rtParticipates, etIMPD);
                }
            }

            /*
             * Flag this interaction concept as inferred.
             */
            cii.createAttribute(anInferred, Boolean.TRUE, false);

            /*
             * Link out to experiments.
             */
            if (ii.getExperimentRefList() != null) {
                for (Integer expRef : ii.getExperimentRefList().getList()) {
                    ONDEXConcept cExp = experiments.get(expRef);
                    graph.getFactory().createRelation(cii, cExp, rtInferred, etIMPD);
                }
            } else {
                for (ONDEXConcept cExp : experiments.values()) {
                    graph.getFactory().createRelation(cii, cExp, rtInferred, etIMPD);
                }
            }
        }
    }


    /**
     * Called whenever an attribute list has been parsed.
     * @param attributes
     */
    @Override
    public void processAttributes(AttributeList attributes) {
        //TODO find something to do with global attributes
    }


    /**
     * Takes a bibref object and turns it into a publication concept. Then
     * it links a given concept to it using a "pub_in" relation.
     *
     * @param bibref the BibRef object.
     * @param c A concept which will be linked to the publication.
     */
    private void linkPublication(BibrefType bibref, ONDEXConcept c) {

        /*
         * avoid nullpointers
         */
        if (bibref == null) {
            return;
        }

        if (bibref.getXref() != null) {

            String dbac = bibref.getXref().getPrimaryRef().getDbAc();
            DataSource pubdataSource = dataSourceLookup.get(dbac);

            if (pubdataSource != null) {

                String acc = bibref.getXref().getPrimaryRef().getId();
                String pid = ccPub.getId() + "_" + pubdataSource+":"+acc;

                //create publication concept
                ONDEXConcept pubConcept = concepts.get(pid);
                if (pubConcept == null) {
                    pubConcept = graph.getFactory().createConcept(pid, datasource, ccPub, etIMPD);
                    concepts.put(pid, pubConcept);
                }

                //annotate publication concept with cross reference
                pubConcept.createConceptAccession(acc, pubdataSource, true);

                //link publication concept to given concept
                ONDEXRelation r = graph.getRelation(c, pubConcept, rtPubIn);
                if (r == null) {
                    graph.getFactory().createRelation(c, pubConcept, rtPubIn, etIMPD);
                }

            } else {
                complainAboutMissingCV(dbac, "");
            }

        } else {//attributeList definition
            String authors = null, year = null, journal = null, url = null;

            for (Attribute attribute : bibref.getAttributeList().getList()) {

                if (attribute.getNameAc().equalsIgnoreCase("MI:0636")) {//=author list
                    authors = attribute.getValue();
                } else if (attribute.getNameAc().equalsIgnoreCase("MI:0886")) {//=publication year
                    year = attribute.getValue();
                } else if (attribute.getNameAc().equalsIgnoreCase("MI:0885")) {//=journal
                    journal = attribute.getValue();
                } else if (attribute.getNameAc().equalsIgnoreCase("MI:0614")) {//=url
                    url = attribute.getValue();
                }

            }

            if ((authors != null && year != null) || url != null) {

                String pid = url != null ? 
                    ccPub.getId() + "_" + url :
                    ccPub.getId() + "_" + authors + "_" + year;

                ONDEXConcept pubConcept = concepts.get(pid);
                if (pubConcept == null) {
                    pubConcept = graph.getFactory().createConcept(pid, datasource, ccPub, etIMPD);
                    concepts.put(pid, pubConcept);
                }

                if (authors != null) {
                    pubConcept.createAttribute(anAuthor, authors, false);
                }
                if (year != null) {
                    pubConcept.createAttribute(anYear, year, false);
                }
                if (url != null) {
                    pubConcept.createAttribute(anURL, url, false);
                }
                if (journal != null) {
                    pubConcept.createAttribute(anJournal, journal, false);
                }

            } else {
                parser.complain("Unable to identify publication!");
            }

        }

    }

    /**
     * Transfers all names from a given Names object to a given OndexConcept
     * @param names the names object to be transferred
     * @param c the target concept.
     */
    private void transferNames(NamesType names, ONDEXConcept c) {

        /*
         * Avoid nullpointers.
         */
        if (names == null) {
            return;
        }

        /*
         * Find preferred name.
         */
        if (names.getFullName() != null && names.getFullName().length() > 0) {
            c.createConceptName(names.getFullName(), true);
        } else if (names.getShortLabel() != null && names.getShortLabel().length() > 0){
            c.createConceptName(names.getShortLabel(), true);
        }

        /*
         * transfer all remaining names
         */
        if (names.getAliasList() != null) {

            for (Alias alias : names.getAliasList()) {
                /*
                 * Watch out for concept accessions in the name field.
                 */
                if (alias.getTypeAc() != null && !(alias.getTypeAc().equals("MI:0301")//gene name
                                                || alias.getTypeAc().equals("MI:0302")//gene name synonym
                                                || alias.getTypeAc().equals("MI:0306"))) {//orf name

                    DataSource namespace = dataSourceLookup.get(alias.getTypeAc());
                    if (namespace != null) {
                        c.createConceptAccession(alias.getValue(), namespace, false);
                    } else {
                        complainAboutMissingCV(alias.getType(), alias.getTypeAc());
                    }

                } else if (alias.getValue() != null && alias.getValue().length() > 0){
                    c.createConceptName(alias.getValue(), false);
                }
            }

        }
    }


    /**
     * Transfer cross-references from a given XRef object to a target concept.
     * @param xref cross-reference container object
     * @param c target concept.
     */
    private void transferXRefs(XrefType xref, ONDEXConcept c) {

        if (xref == null) {
            return;
        }

        if (xref.getPrimaryRef() != null) {

            if (xref.getPrimaryRef().getDbAc() != null) {
                DataSource namespace = dataSourceLookup.get(xref.getPrimaryRef().getDbAc());
                if (namespace != null) {
                    c.createConceptAccession(xref.getPrimaryRef().getId(), namespace, true);
                } else {
                    complainAboutMissingCV(xref.getPrimaryRef().getDb(),
                            xref.getPrimaryRef().getDbAc());
                }
            } else {
                parser.complain("Cross-reference fails to specify DB accession");
            }
        } else {
            parser.complain("invalid XRef: Missing primaryRef! ");
        }

        if (xref.getSecondaryRefList() != null) {
            for (DbReferenceType dbref : xref.getSecondaryRefList()) {
                if (dbref.getDbAc() != null) {
                    DataSource namespace = dataSourceLookup.get(dbref.getDbAc());
                    if (namespace != null) {
                        c.createConceptAccession(dbref.getId(), namespace, false);
                    } else {
                        complainAboutMissingCV(dbref.getDb(), dbref.getDbAc());
                    }
                } else {
                    parser.complain("Cross-reference fails to specify DB accession");
                }
            }
        }
        
    }

    /**
     * Creates a concept and indexes it in the concept dictionary
     * @param cc concept class to use
     * @param id the concept's id will be used as part of the PID. pid = cc_id
     * @return the concept
     */
    private ONDEXConcept createAndRegister(ConceptClass cc, int id) {
        String pid = cc.getId() + "_" + id;
        return createAndRegister(cc, pid);
    }

    /**
     * Creates a concept and indexes it in the concept dictionary
     * @param cc concept class to use
     * @param pid the concept's pid
     * @return the concept
     */
    private ONDEXConcept createAndRegister(ConceptClass cc, String pid) {
        ONDEXConcept c = graph.getFactory().createConcept(pid, datasource, cc, etIMPD);
        concepts.put(pid, c);
        return c;
    }

    /**
     * Helper method for extracting PSIMI ontology terms from an XREF list.
     * @param xref
     * @return
     */
    private String getPsimiTerm(XrefType xref) {
        if (xref == null) {
            return null;
        } else if (isPSIMIRef(xref.getPrimaryRef())) {
            return xref.getPrimaryRef().getId();
        } else {
            for (DbReferenceType ref : xref.getSecondaryRefList()) {
                if (isPSIMIRef(ref)) {
                    return ref.getId();
                }
            }
        }
        return null;
    }

    /**
     * determines whether a given DBReference is a PSI-MI ontology reference
     * @param ref the reference in question
     * @return true if it is a PSI-MI ontology reference, false otherwise
     */
    private boolean isPSIMIRef(DbReferenceType ref) {
        return (ref.getDbAc() != null && ref.getDbAc().equals("MI:0488")) ||
                (ref.getDb() != null && ref.getDb().equals("psi-mi"));
    }

    private void complainAboutMissingCC(String name, String accession) {

        EventType event = new ConceptClassMissingEvent("Unknown XRef: " +
                name + " ("+accession + ")", "");
        event.setLog4jLevel(Level.WARN);

        parser.fireEventOccurred(event);

    }

    /**
     * Fires an ondex error event.
     * @param name Name of entity that has no corresponding EvidenceType
     * @param accession Accession of entity in PSI-MI ontology that has no corresponding EvidenceType.
     */
    private void complainAboutMissingEvidenceType(String name, String accession) {

        EventType event = new EvidenceTypeMissingEvent("Unknown XRef: " +
                name + " ("+accession + ")", "");
        event.setLog4jLevel(Level.WARN);

        parser.fireEventOccurred(event);

    }

    /**
     * Fires an Ondex error event
     * @param name Name of entity that has no corresponding DataSource
     * @param accession Accession of entity in PSI-MI ontology that has no corresponding DataSource.
     */
    private void complainAboutMissingCV(String name, String accession) {

        EventType event = new DataSourceMissingEvent("Unknown XRef: " +
                name + " ("+accession + ")", "");
        event.setLog4jLevel(Level.WARN);

        parser.fireEventOccurred(event);

    }


//    /**
//     * Fires an Ondex error event.
//     * @param message The error message
//     */
//    private void complain(String message) {
//
//        EventType event = new ParsingErrorEvent(message, "");
//        event.setLog4jLevel(Level.WARN);
//        parser.fireEventOccurred(event);
//
//    }
//
//    private void log(String message) {
//
//        EventType event = new GeneralOutputEvent(message, "");
//        event.setLog4jLevel(Level.INFO);
//        parser.fireEventOccurred(event);
//
//    }

}
