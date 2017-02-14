package net.sourceforge.ondex.parser.sbml2;

import java.io.File;
import java.io.FilenameFilter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.MetaDataLookup;
import net.sourceforge.ondex.tools.MetaDataUtil;

import org.apache.log4j.Level;
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
 * @author Matthew Pocock, Jochen Weile
 */
public class Parser extends ONDEXParser
{
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


    public String getName() {
        return "SBML2 Parser";
    }

    public String getVersion() {
        return "25/06/2009";
    }

    @Override
    public String getId() {
        return "sbml2";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
        		new FileArgumentDefinition(
        				FileArgumentDefinition.INPUT_DIR,
        				"directory with SBML files", true, true, true, false),
                new StringArgumentDefinition(DATASOURCE_ARG, DATASOURCE_ARG_DESC, true, "unknown", false)
        };
    }

    public String[] requiresValidators() {
        return new String[0];
    }

    public void start() throws Exception {
        /* Moved to resolve bug (CORE-3) Add new parser to launcher does not work properly
           Having this check in the static block prevents the reflection of this class. System.exit(1) prevents
           the exception from being propagated and handled at a higher level.
           */
        // Load in the sbmlj native library. Note, this must be done once per VM, not once per class-loader!
//        try {
//        	printClassPath();

//            System.loadLibrary("sbmlj");
            /* Extra check to be sure we have access to libSBML: */
//            Class.forName("org.sbml.libsbml.libsbml");
//        } catch (UnsatisfiedLinkError e) {
//            throw new Exception("Error: Could not load the libSBML library.\n" +
//                    "The libsbmlj jar you use is apparently not linked to your local libsbml installation.\n" +
//                    "For information on how to build a correctly linked libsbml jar file see:\n" +
//                    "http://sbml.org/Software/libSBML/docs/java-api/libsbml-installation.html", e);
//        }

        File path = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_DIR));
        if (path == null) {
            throw new ParsingFailedException("Input directory required");
        }

        metaData = graph.getMetaData();

        File translationFile = new File(path.getAbsolutePath() + File.separator + "metadata.tsv");
        dataSourceLookup = new MetaDataLookup<DataSource>(translationFile, metaData, DataSource.class);
        ccLookup = new MetaDataLookup<ConceptClass>(translationFile, metaData, ConceptClass.class);
        rtLookup = new MetaDataLookup<RelationType>(translationFile, metaData, RelationType.class);

        mdu = new MetaDataUtil(metaData, dataSourceLookup);

        //FIXME: evidence and cv have be determined from an argument definition! 
        etSBML = requireEvidenceType("IMPD");
        dataSourceSBML = mdu.safeFetchDataSource((String) args.getUniqueValue(DATASOURCE_ARG));
        anStoch = mdu.safeFetchAttributeName("stoichiometry", Double.class);

//        DataSource sbmlCv = metaData.createCV("SBML", "SBML", "Importeed from SBML file");
//        EvidenceType sbmlEt = metaData.getFactory().createEvidenceType("SBML_import", "Imported from SBML model");
//        ConceptClass thingCC = metaData.getConceptClass("thing");
//        ConceptClass modelCC = mdu.safeFetchConceptClass("SBML Model", "SBML Model", thingCC);
//        AttributeName sboTermIdAN = mdu.safeFetchAttributeName("sboTermId", String.class);


        File[] files = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml") || name.endsWith(".zip");
            }
        });

        // unable to find any files to process
        if (files == null || files.length == 0) {
            throw new ParsingFailedException("No input files present");
        }

        for (File toLoad : files) {
            resetParser();
            log("Processing: " + toLoad);

            log("Parsing sbml file");
            SBMLDocument doc = SBMLReader.readSBML(toLoad.getAbsolutePath());

            model = doc.getModel();

//            System.err.println("Creating model");
//            ONDEXConcept modelC = graph.getFactory().createConcept(model.getId(), sbmlCv, modelCC, sbmlEt);

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


//    private void printClassPath() {
//        for (URL url : ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs()) {
//            System.out.println(url.getFile());
//        } 
//    	System.out.println(System.getProperty("java.class.path"));
//	}

    private Map<String, ONDEXConcept> processCompartments() throws PluginConfigurationException {
        // compartments
//        ConceptClass compartmentCC = mdu.safeFetchConceptClass("Compartment", "Cellular Compartment", ccThing);

        AttributeName compartmentTypeA = mdu.safeFetchAttributeName("compartmentType", String.class);
        AttributeName spatialDimensionsA = mdu.safeFetchAttributeName("spatialDimensions", Long.class);
        AttributeName sizeA = mdu.safeFetchAttributeName("size", Double.class);
        AttributeName constantA = mdu.safeFetchAttributeName("constantSize", Boolean.class);
        RelationType locatedIn = mdu.safeFetchRelationType("located_in", "located_in");

        Map<String, ONDEXConcept> compartmentsById = new HashMap<String, ONDEXConcept>();
        Map<ONDEXConcept, String> allOutsideOfs = new HashMap<ONDEXConcept, String>();
        ListOf<Compartment> compartments = model.getListOfCompartments();
        for (int i = 0; i < compartments.size(); i++) {
//            System.err.println("Compartment #" + i);
            Compartment compartment = compartments.get(i);

            ConceptClass cc = determineCC(compartment);

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

            // compartment spatal dimensions as an attribute
            concept.createAttribute(spatialDimensionsA, compartment.getSpatialDimensions(), false);

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

//        ConceptClass speciesCC = mdu.safeFetchConceptClass("SBML_Species", "SBML Species", ccThing);
//        AttributeName speciesType = mdu.safeFetchAttributeName("speciesType", String.class);
        AttributeName initialAmount = mdu.safeFetchAttributeName("initialAmount", String.class);
        AttributeName initialConcentration = mdu.safeFetchAttributeName("initialConcentration", String.class);
        AttributeName constantAmount = mdu.safeFetchAttributeName("constantAmount", String.class);
        AttributeName boundaryCondition = mdu.safeFetchAttributeName("boundaryCondition", String.class);
        RelationType locatedIn = mdu.safeFetchRelationType("located_in", "located_in");
        DataSource dataSourceMips = mdu.safeFetchDataSource("MIPS");

        ListOf<Species> speci = model.getListOfSpecies();
        for (int i = 0; i < speci.size(); i++) {
            Species species = speci.get(i);

            ConceptClass cc = determineCC(species);

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
//        ConceptClass reactionCC = mdu.safeFetchConceptClass("SBML_Reaction", "SBML Reaction", thingCC);
//        RelationType hasReactantR = mdu.safeFetchRelationType("cs_by", "consumed_by");
//        RelationType hasProductR = mdu.safeFetchRelationType("pd_by", "produced_by");
//        RelationType modifiesR = mdu.safeFetchRelationType("modifies", "modifies");

        ListOf<Reaction> reactions = model.getListOfReactions();
        for (int i = 0; i < reactions.size(); i++) {
            Reaction reaction = reactions.get(i);

            ConceptClass cc = determineCC(reaction);
            RelationType hasReactantR = determineRT(reaction, "taken");
            RelationType hasProductR = determineRT(reaction, "given");
            RelationType modifiesR = determineRT(reaction, "modifies");

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


//    private ONDEXRelation createRelation(RelationType relationTypeSet,
//                                            ONDEXConcept from,
//                                            ONDEXConcept to,
//                                            ONDEXConcept context,
//                                            EvidenceType sbmlEt)
//    {
//        ONDEXRelation relation = graph.getFactory().createRelation(
//                from, to, relationTypeSet, sbmlEt);
//        relation.addContext(context);
//        return relation;
//    }


//    private ONDEXConcept createConcept(SBase element,ConceptClass cc,DataSource sbmlCv,EvidenceType sbmlEt,
//    									AttributeName sboTermIdAN,ONDEXConcept context) {
//        if(cc == null) {
//            throw new NullPointerException("Null concept class cc");
//        }
//        ONDEXConcept concept = graph.getFactory().createConcept(element.getId(), sbmlCv, cc, sbmlEt);
//        concept.addContext(context);
//
//        if(element.isSetName()) {
//            concept.createConceptName(element.getName(), true);
//        }
//
//        if(element.isSetNotes()) {
//            concept.setDescription(element.getNotesString());
//        }
//
//        return concept;
//    }

    private ConceptClass determineCC(AbstractNamedSBase element) throws PluginConfigurationException {
        if (element.isSetSBOTerm()) {
            ConceptClass cc = ccLookup.get(element.getSBOTermID());
            if (cc != null) {
                return cc;
            } else {
                throw new PluginConfigurationException("No translation entry found for: " + element.getSBOTermID());
            }
        } else {
            throw new ParsingFailedException("Element " + element.getName() + " is missing an SBO term!");
        }
    }

    private RelationType determineRT(AbstractNamedSBase element, String modifier) throws PluginConfigurationException {
        if (element.isSetSBOTerm()) {
            String key = element.getSBOTermID() + ":" + modifier;
            RelationType rt = rtLookup.get(key);
            if (rt != null) {
                return rt;
            } else {
                throw new PluginConfigurationException("No translation entry found for: " + key);
            }
        } else {
            throw new ParsingFailedException("Element " + element.getName() + " is missing an SBO term!");
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
