package net.sourceforge.ondex.parser.sbml2a;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.InvalidPluginArgumentException;

import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import net.sourceforge.ondex.tools.MetaDataLookup;
import net.sourceforge.ondex.tools.MetaDataUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.xml.stax.SBMLReader;

/**
 * Parser for SBML, using libSBML, targeting SBML level 2.
 *
 * Exstended to insert default ConceptClass and RelationType is SBO term
 * is missing or not in metaData
 *
 * @author Matthew Pocock, Jochen Weile, Christian BrRenninkmeijer
 */
@Status(description = "Tested September 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Parser extends net.sourceforge.ondex.parser.sbml2.Parser {

    private ONDEXGraphMetaData metaData;
    private MetaDataUtil mdu;
    private MetaDataLookup<DataSource> dataSourceLookup;
    private MetaDataLookup<ConceptClass> ccLookup;
    private MetaDataLookup<RelationType> rtLookup;

    private EvidenceType etSBML;
    private DataSource dataSourceSBML;
    private AttributeName anStoch;

    private Model model;

    private Map<String, ONDEXConcept> compartmentsById;
    private Map<String, ONDEXConcept> speciesById;

    private HashSet<String> unmatchedKeys = new HashSet<String>();

    public static final String DATASOURCE_ARG = "DataSource";
    public static final String DATASOURCE_ARG_DESC = "ID of datasource (Ondex:DataSource) where the data has been obtained from.";

    public static final String IMPD_FULLNAME_ARG = "IMPDFullName";
    public static final String IMPD_FULLNAME_ARG_DESC = "Full name to be used for IMPD EvidenceType if and only if there is EvidenceType with id \"IMPD\"already in the graph.";

    public static final String IMPD_DESCRIPTION_ARG = "IMPDDescription";
    public static final String IMPD_DESCRIPTION_ARG_DESC = "Description to be used for IMPD EvidenceType if and only if there is EvidenceType with id \"IMPD\"already in the graph.";

    public static final String METADATA_ARG = "MetaData";
    public static final String METADATA_ARG_DESC = "Mapping information for SBO terms";

    public static final String DATA_ARG = "Data";
    public static final String DATA_ARG_DESC = "SBML Data to be parsed in";

    private String IMPDFullName;
    private String IMPDDescription;

    private static final Logger logger = Logger.getLogger(Parser.class);

    public String getName() {
        return "SBML2 Parser ignoreing missing SBO";
    }

    public String getVersion() {
        return "1/04/2010";
    } 

    @Override
    public String getId() {
        return "sbml2a";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(METADATA_ARG, METADATA_ARG_DESC,true, true, false, true),
                new FileArgumentDefinition(DATA_ARG, DATA_ARG_DESC,  true, true, false, true),
                new StringArgumentDefinition(IMPD_FULLNAME_ARG, IMPD_FULLNAME_ARG_DESC, true, "IMPD", false),
                new StringArgumentDefinition(IMPD_DESCRIPTION_ARG, IMPD_DESCRIPTION_ARG_DESC, true, "", false),
                new StringArgumentDefinition(DATASOURCE_ARG, DATASOURCE_ARG_DESC, true, "unknown", false)
        };
    }

    public String[] requiresValidators() {
        return new String[0];
    }

    private EvidenceType getIMPDEvidenceType() throws InvalidPluginArgumentException{
        EvidenceType evidenceType = graph.getMetaData().getEvidenceType("IMPD");;
        if (evidenceType != null) {
            return evidenceType;
        }
        return graph.getMetaData().createEvidenceType("IMPD",
                ((String) args.getUniqueValue(IMPD_FULLNAME_ARG)),
                ((String) args.getUniqueValue(IMPD_DESCRIPTION_ARG)));
    }

    public void start() throws Exception {
        log("SBMLCB started");
 
        metaData = graph.getMetaData();

        String metaFileName = (String)(args.getUniqueValue(METADATA_ARG));
        File metaFile = new File(metaFileName);

        //MetaDataLookup can handle null InputStream
        dataSourceLookup = new MetaDataLookup<DataSource>(metaFile , metaData, DataSource.class);
        ccLookup = new MetaDataLookup<ConceptClass>(metaFile , metaData, ConceptClass.class);
        rtLookup = new MetaDataLookup<RelationType>(metaFile , metaData, RelationType.class);

        log("SBML2a metaDatalookup done");

        mdu = new MetaDataUtil(metaData, dataSourceLookup);

        //FIXED
        etSBML = getIMPDEvidenceType();
        dataSourceSBML = mdu.safeFetchDataSource((String) args.getUniqueValue(DATASOURCE_ARG));

        anStoch = mdu.safeFetchAttributeName("stoichiometry", Double.class);

        log("SBML2a anStoch created");

        List<String> fileNames = args.getObjectValueList(DATA_ARG, String.class);

        // unable to find any files to process
        if (fileNames == null || fileNames.size() == 0) {
            throw new ParsingFailedException("No input files present");
        }

        for (String toLoad : fileNames) {
            log("SBMLCB parsing "+toLoad);
            resetParser();
            String scrubbed = Utils.ScrubXML(toLoad);
            log("Processing: " + scrubbed);

            log("Parsing sbml file");
            SBMLDocument doc = SBMLReader.readSBML(scrubbed);

            log("done readSBML");
            model = doc.getModel();

            log("Processing compartments");
            compartmentsById = processCompartments();
            log("Processing species");
            speciesById = processSpecies();
            log("Processing reactions");
            processReactions();
        }

        if (unmatchedKeys.size() > 0) {
            StringBuilder sb = new StringBuilder("Unmatched Keys:\n" +
                    "===============\n");
            for (String key : unmatchedKeys) {
                sb.append(key + "\n");
            }
            System.err.println(sb.toString());
        }
    }

    private Map<String, ONDEXConcept> processCompartments() throws PluginConfigurationException {
 
        AttributeName compartmentTypeA = mdu.safeFetchAttributeName("compartmentType", String.class);
        AttributeName spatialDimensionsA = mdu.safeFetchAttributeName("spatialDimensions", Long.class);
        AttributeName sizeA = mdu.safeFetchAttributeName("size", Double.class);
        AttributeName constantA = mdu.safeFetchAttributeName("constantSize", Boolean.class);
        RelationType locatedIn = mdu.safeFetchRelationType("located_in", "located_in");

        Map<String, ONDEXConcept> compartmentsById = new HashMap<String, ONDEXConcept>();
        Map<ONDEXConcept, String> allOutsideOfs = new HashMap<ONDEXConcept, String>();
        ListOf<Compartment> compartments = model.getListOfCompartments();
        for (int i = 0; i < compartments.size(); i++) {
            Compartment compartment = compartments.get(i);

            ConceptClass cc = determineCC(compartment, TypeOfConcept.COMPARTMENT);

            ONDEXConcept concept = graph.getFactory().createConcept(compartment.getId(), dataSourceSBML, cc, etSBML);

            compartmentsById.put(compartment.getId(), concept);

            Map<Qualifier, Set<String>> annoMap = extractRDFAnnotation(compartment);
            for (Qualifier annoKey : annoMap.keySet()) {
                if (annoKey.equals(Qualifier.BQB_IS) || annoKey.equals(Qualifier.BQB_HAS_PART) || annoKey.equals(Qualifier.BQB_IS_DESCRIBED_BY)) {
                    Set<String> accessions = annoMap.get(annoKey);
                    assignAccessions(concept, accessions);
                } else {
                    logFail("unrecognized annotation key: " + annoKey);
                }
            }

            if (compartment.isSetName()) {
                concept.createConceptName(compartment.getName(), true);
            }

            if (compartment.isSetNotes()) {
                concept.setDescription(compartment.getNotesString());
            }

            // compartment type as an attribute
            if (compartment.isSetCompartmentType()) {
                concept.createAttribute(compartmentTypeA, compartment.getCompartmentType(), true);
            }

            //compartment spatal dimensions as an attribute
            concept.createAttribute(spatialDimensionsA, (long)compartment.getSpatialDimensions(), false);

            // compartment size as an attribute
            if (compartment.isSetSize()) {
                concept.createAttribute(sizeA, compartment.getSize(), false);
            }

            // the units of the size attribute - units are associated with particular values in sbml
            //compartment.getUnits();

            // constant size flag as an attribute
            concept.createAttribute(constantA, compartment.getConstant(), false);

            // outside values will be modelled using relations later
            if (compartment.isSetOutside()) {
                allOutsideOfs.put(concept, compartment.getOutside());
            }
        }

        for (Map.Entry<ONDEXConcept, String> cs : allOutsideOfs.entrySet()) {
            ONDEXConcept outer = compartmentsById.get(cs.getValue());
            graph.getFactory().createRelation(cs.getKey(), outer, locatedIn, etSBML);
        }
        return compartmentsById;
    }

    private Map<String, ONDEXConcept> processSpecies() throws PluginConfigurationException {
        Map<String, ONDEXConcept> speciesById = new HashMap<String, ONDEXConcept>();

        AttributeName initialAmount = mdu.safeFetchAttributeName("initialAmount", Double.class);
        AttributeName initialConcentration = mdu.safeFetchAttributeName("initialConcentration", Double.class);
        AttributeName constantAmount = mdu.safeFetchAttributeName("constantAmount", Boolean.class);
        AttributeName boundaryCondition = mdu.safeFetchAttributeName("boundaryCondition", Boolean.class);
        RelationType locatedIn = mdu.safeFetchRelationType("located_in", "located_in");
        DataSource dataSourceMips = mdu.safeFetchDataSource("MIPS");

        ListOf<Species> speci = model.getListOfSpecies();
        for (int i = 0; i < speci.size(); i++) {
            Species species = speci.get(i);

            ConceptClass cc = determineCC(species, TypeOfConcept.SPECIES);

            if (cc != null) {
                ONDEXConcept speciesC = graph.getFactory().createConcept(species.getId(), dataSourceSBML, cc, etSBML);
                speciesById.put(species.getId(), speciesC);

                Map<Qualifier, Set<String>> annoMap = extractRDFAnnotation(species);
                for (Qualifier annoKey : annoMap.keySet()) {
                    if (annoKey.equals(Qualifier.BQB_IS) || annoKey.equals(Qualifier.BQB_HAS_PART) || annoKey.equals(Qualifier.BQB_IS_DESCRIBED_BY)) {
                        Set<String> accessions = annoMap.get(annoKey);
                        assignAccessions(speciesC, accessions);
                    } else {
                        logFail("unrecognized annotation key: " + annoKey);
                    }
                }

                if (species.isSetName()) {
                    for (String potentialYName : species.getName().split(":")) {
                        if (potentialYName.matches("Y\\w{2}\\d{3}\\w{1,2}(\\-\\w)?")) {
                            speciesC.createConceptAccession(potentialYName, dataSourceMips, false);
                        } else if (potentialYName.matches("Q\\d{4}")) {
                            speciesC.createConceptAccession(potentialYName, dataSourceMips, false);
                        } else {
                            speciesC.createConceptName(potentialYName, true);
                        }
                    }
                }

                if (species.isSetNotes()) {
                    speciesC.setDescription(species.getNotesString());
                }

                if (species.isSetCompartment()) {
                    ONDEXConcept compC = compartmentsById.get(species.getCompartment());
                    if (compC != null) {
                        graph.getFactory().createRelation(speciesC, compC, locatedIn, etSBML);
                    } else {
                        logFail("Failed to resolve compartment concept: " + species.getCompartment());
                    }
                }

                if (species.isSetInitialAmount()) {
                    speciesC.createAttribute(initialAmount, species.getInitialAmount(), false);
                }

                if (species.isSetInitialConcentration()) {
                    speciesC.createAttribute(initialConcentration, species.getInitialConcentration(), false);
                }

                speciesC.createAttribute(constantAmount, species.getConstant(), false);
                speciesC.createAttribute(boundaryCondition, species.getBoundaryCondition(), false);
            } else {
                logFail("Unable to determine concept class for species " + species.getId());
            }

            // ignore all unit properties
        }

        return speciesById;
    }

    private void processReactions() throws PluginConfigurationException {

        ListOf<Reaction> reactions = model.getListOfReactions();
        for (int i = 0; i < reactions.size(); i++) {
            Reaction reaction = reactions.get(i);

            ConceptClass cc = determineCC(reaction, TypeOfConcept.REACTION);
            RelationType hasReactantR = determineRT(reaction, TypeOfRelation.REACTANT);
            RelationType hasProductR = determineRT(reaction, TypeOfRelation.PRODUCT);
            RelationType modifiesR = determineRT(reaction, TypeOfRelation.MODIFIER);

            if (cc != null) {
                ONDEXConcept reactionC = graph.getFactory().createConcept(reaction.getId(), dataSourceSBML, cc, etSBML);

                Map<Qualifier, Set<String>> annoMap = extractRDFAnnotation(reaction);
                for (Qualifier annoKey : annoMap.keySet()) {
                    if (annoKey.equals(Qualifier.BQB_IS) || annoKey.equals(Qualifier.BQB_HAS_PART) || annoKey.equals(Qualifier.BQB_IS_DESCRIBED_BY)) {
                        Set<String> accessions = annoMap.get(annoKey);
                        assignAccessions(reactionC, accessions);
                    } else {
                        logFail("unrecognized annotation key: " + annoKey);
                    }
                }

                if (reaction.isSetName()) {
                    reactionC.createConceptName(reaction.getName(), true);
                }

                if (reaction.isSetNotes()) {
                    reactionC.setDescription(reaction.getNotesString());
                }


                processSpeciesReferences(reactionC, reaction.getListOfReactants(), hasReactantR);
                processSpeciesReferences(reactionC, reaction.getListOfProducts(), hasProductR);
                processSpeciesReferences(reactionC, reaction.getListOfModifiers(), modifiesR);

            } else {
                logFail("Failed to determine concept class for reaction: " + reaction.getId());
            }


        }
    }

    private void processSpeciesReferences(ONDEXConcept reactionC, ListOf<? extends SimpleSpeciesReference> references, RelationType rt) {
    	if (references == null) {
    		return;
    	}
        for (int j = 0; j < references.size(); j++) {
            SimpleSpeciesReference reactantRef = references.get(j);
            ONDEXConcept speciesC = speciesById.get(reactantRef.getSpecies());
            if (speciesC != null) {
                ONDEXRelation reactantRel = graph.getFactory().createRelation(speciesC, reactionC, rt, etSBML);
                if (reactantRel != null) {

                    if (reactantRef instanceof SpeciesReference) {
                        reactantRel.createAttribute(anStoch, ((SpeciesReference) reactantRef).getStoichiometry(), false);
                        // TODO: handle stoich math
                    }

                } else {
                    logFail("Failed to create relation!");
                }
            } else {
                logFail("Unable to resolve species reference: " + reactantRef.getSpecies());
            }

        }
    }

    private ConceptClass determineCC(SBase element, TypeOfConcept type) throws PluginConfigurationException {
        if (element.isSetSBOTerm()) {
            ConceptClass cc = ccLookup.get(element.getSBOTermID());
            if (cc != null) {
                return cc;
            } else {
                log("No translation entry found for: " + element.getSBOTermID());
            }
        } else {
            log("Element " +element.getMetaId() + "named: \""+ element.getElementName() +
                    "\" is missing an SBO term");
        }
        SBase parent = element.getParentSBMLObject();
        ConceptClass parentCC = null;
        if (parent != null && (parent.isSetSBOTerm())) {
            parentCC = determineCC(parent, type);
        }
        return mdu.safeFetchConceptClass(type.getId(), type.getDescription(), parentCC);
    }

    private RelationType determineRT(AbstractNamedSBase element, TypeOfRelation type) {
        if (element.isSetSBOTerm()) {
            String key = element.getSBOTermID() + ":" + type.getModifier();
            RelationType rt = rtLookup.get(key);
            if (rt != null) {
                return rt;
            } else {
                log("No relation found for SBO term; " + element.getSBOTermID() +
                        " and modifier "+type.getModifier());
                return mdu.safeFetchRelationType(type.getId(), type.getDescription());
            }
        } else {
            log ("Element " +element.getId() + "named: \""+ element.getName() +
                    "\" is missing an SBO term!");
            return mdu.safeFetchRelationType(type.getId(), type.getDescription());
        }
    }

    private void assignAccessions(ONDEXConcept c, Set<String> accs) {
        for (String acc : accs) {
            String[] split = dataSourceLookup.recognize(acc);
            if (split != null) {
                String cvStr = split[0], value = split[1];
                DataSource dataSource = dataSourceLookup.get(cvStr);
                if (dataSource != null) {
                    c.createConceptAccession(value, dataSource, false);
                }
            } else {
                unmatchedKeys.add(acc);
            }
        }
    }

    private Map<Qualifier, Set<String>> extractRDFAnnotation(SBase element) {
        EnumMap<Qualifier, Set<String>> rdfMap = new EnumMap<Qualifier, Set<String>>(Qualifier.class);
        if (element.isSetAnnotation()) {
        	Annotation ann = element.getAnnotation();
        	for (CVTerm cvterm : ann.getListOfCVTerms()) {
        		rdfMap.put(cvterm.getBiologicalQualifierType(), new HashSet<String>(cvterm.getResources()));
        	}
        }
        return rdfMap;
    }

    private void resetParser() {
        model = null;
        compartmentsById = null;
        speciesById = null;
    }

    private void log(String s) {
        fireEventOccurred(new GeneralOutputEvent(s, ""));
    }

    private void logFail(String s) {
        EventType e = new InconsistencyEvent(s, "");
        e.setLog4jLevel(Level.ERROR);
        fireEventOccurred(e);
    }
}
