package net.sourceforge.ondex.parser.chebi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.data.ChemicalStructure;
import org.apache.log4j.Logger;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.formats.IChemFormatMatcher;
import org.openscience.cdk.io.formats.MDLFormat;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Parser for the Chemical Entities of Biological Interest (ChEBI)
 *
 * @author taubertj
 *
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Status(status = StatusType.STABLE, description = "Tested December 2013 (Jacek Grzebyta)")
public class Parser extends ONDEXParser implements MetaData {

    private static final String BRAND_NAMES = "BRAND Names";

    private static final String CHARGE = "Charge";

    private static final String CHEBI_ID = "ChEBI ID";

    private static final String CHEBI_NAME = "ChEBI Name";

    private static final String DEFINITION = "Definition";

    private static final String FORMULAE = "Formulae";

    private static final String INCHI = "InChI";

    private static final String INCHIKEY = "InChIKey";

    private static final String INN = "INN";

    private static final String IUPAC_NAMES = "IUPAC Names";

    private static final String LAST_MODIFIED = "Last Modified";

    private static final String MASS = "Mass";

    private static final String SECONDARY_CHEBI_ID = "Secondary ChEBI ID";

    private static final String SMILES = "SMILES";

    private static final String STAR = "Star";

    private static final String SYNONYMS = "Synonyms";

    private Logger log = Logger.getLogger(getClass());

    AttributeName anChemicalStructure, anInChIKey, anInChI, anStar, anCharge,
            anMass, anModified;

    IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();

    DataSource elementOf;

    EvidenceType evidencetype;

    List<IAtomContainer> molecules = new ArrayList<IAtomContainer>();

    ConceptClass ofType;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{new FileArgumentDefinition(
            FileArgumentDefinition.INPUT_FILE,
            FileArgumentDefinition.INPUT_FILE_DESC, true, true, false)};
    }

    @Override
    public String getId() {
        return "chebi";
    }

    @Override
    public String getName() {
        return "Chemical Entities of Biological Interest (ChEBI)";
    }

    @Override
    public String getVersion() {
        return "08.09.2011";
    }

    /**
     * initialise Ondex meta-data
     */
    private void initMetaData() {

        // basic concept meta data
        elementOf = graph.getMetaData().getDataSource(DS_CHEBI);
        ofType = graph.getMetaData().getConceptClass(CC_COMP);
        evidencetype = graph.getMetaData().getEvidenceType(ET_IMPD);

        // ChemicalStruture attribute
        anChemicalStructure = graph.getMetaData().getAttributeName(
                ATTR_CHEMICAL_STRUCTURE);
        if (anChemicalStructure == null) {
            anChemicalStructure = graph
                    .getMetaData()
                    .getFactory()
                    .createAttributeName(ATTR_CHEMICAL_STRUCTURE,
                            ATTR_CHEMICAL_STRUCTURE, ChemicalStructure.class);
        }
        // InChIKey attribute
        anInChIKey = graph.getMetaData().getAttributeName(ATTR_INCHIKEY);
        if (anInChIKey == null) {
            anInChIKey = graph
                    .getMetaData()
                    .getFactory()
                    .createAttributeName(ATTR_INCHIKEY, ATTR_INCHIKEY,
                            String.class);
        }
        // InChI attribute
        anInChI = graph.getMetaData().getAttributeName(ATTR_INCHI);
        if (anInChI == null) {
            anInChI = graph.getMetaData().getFactory()
                    .createAttributeName(ATTR_INCHI, ATTR_INCHI, String.class);
        }
        // Star attribute
        anStar = graph.getMetaData().getAttributeName(ATTR_STAR);
        if (anStar == null) {
            anStar = graph.getMetaData().getFactory()
                    .createAttributeName(ATTR_STAR, ATTR_STAR, Integer.class);
        }
        // Charge attribute
        anCharge = graph.getMetaData().getAttributeName(ATTR_CHARGE);
        if (anCharge == null) {
            anCharge = graph
                    .getMetaData()
                    .getFactory()
                    .createAttributeName(ATTR_CHARGE, ATTR_CHARGE,
                            Integer.class);
        }
        // Mass attribute
        anMass = graph.getMetaData().getAttributeName(ATTR_MASS);
        if (anMass == null) {
            anMass = graph.getMetaData().getFactory()
                    .createAttributeName(ATTR_MASS, ATTR_MASS, Double.class);
        }
        // Last modified attribute
        anModified = graph.getMetaData().getAttributeName(ATTR_LAST_MODIFIED);
        if (anModified == null) {
            anModified = graph
                    .getMetaData()
                    .getFactory()
                    .createAttributeName(ATTR_LAST_MODIFIED,
                            ATTR_LAST_MODIFIED, String.class);
        }
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // setup meta data
        initMetaData();

        // file name of file to parse
        File file = new File(
                (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
        fireEventOccurred(new GeneralOutputEvent("Reading: "
                + file.getAbsolutePath(), getCurrentMethodName()));

        // open file as stream, handle compressed files
        InputStream inputStream = new FileInputStream(file);
        if (file.getAbsolutePath().endsWith(".gz")) {
            inputStream = new GZIPInputStream(inputStream);
        }

        // guess format of file and construct reader
        ReaderFactory readerFactory = new ReaderFactory();
        readerFactory.registerFormat((IChemFormatMatcher) MDLFormat
                .getInstance());
        readerFactory.registerFormat((IChemFormatMatcher) SDFFormat
                .getInstance());

        // split SDF file into all its molecules for better error detection
        BufferedReader br = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder buf = new StringBuilder();
        List<String> errors = new ArrayList<String>();
        int i = 1;
        while (br.ready()) {

            // parse one entry
            String line = br.readLine();
            if (!line.startsWith("$$$$")) {
                buf.append(line);
                buf.append("\n");
            } else {
                buf.append(line);
                String s = buf.toString();
                buf.delete(0, buf.length());

                // transform current entry
                ISimpleChemObjectReader reader = readerFactory
                        .createReader(new ByteArrayInputStream(s.getBytes()));

                // check that file contains molecules
                if (reader.accepts(Molecule.class)) {

                    try {
                        // read content of file
                        ChemFile content = (ChemFile) reader
                                .read((ChemObject) new ChemFile());

                        // list all molecules
                        List<IAtomContainer> containersList = ChemFileManipulator
                                .getAllAtomContainers(content);
                        for (IAtomContainer ac : containersList) {
                            molecules.add(ac);
                        }
                    } catch (CDKException ex) {
                        String[] split = s.split("\n");
                        String chebi = "";
                        for (int j = 0; j < split.length; j++) {
                            if (split[j].startsWith("> <ChEBI ID>")) {
                                chebi = split[j + 1];
                            }
                        }
                        errors.add("Line " + i + " (" + chebi + "): "
                                + ex.getMessage());
                    }
                }
            }
            i++;
        }
        br.close();

        // report captured errors to user
        if (errors.size() > 0) {
            System.err.println("The following CDK errors occurred:");
            for (String s : errors) {
                System.err.println(s);
            }
        }

        fireEventOccurred(new GeneralOutputEvent("Total molecules parsed:"
                + molecules.size(), getCurrentMethodName()));

        // track unmapped keys
        Set<String> unmappedKeys = new HashSet<String>();

        // iterate over all molecules
        for (IAtomContainer ac : molecules) {

            // there should be a ChEBI ID
            String id = "";
            if (ac.getProperties().containsKey(CHEBI_ID)) {
                id = (String) ac.getProperty(CHEBI_ID);
            } else {
                fireEventOccurred(new InconsistencyEvent(
                        "ChEBI ID missing for: " + ac.getProperty(CHEBI_NAME),
                        getCurrentMethodName()));
            }

            // create concept prototype
            ONDEXConcept c = graph.getFactory().createConcept(id, elementOf,
                    ofType, evidencetype);

            // cache existing SMILES
            String smiles = null;

            // parse all properties of molecule
            for (Entry<Object, Object> entry : ac.getProperties().entrySet()) {

                // cast key and value to String
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                // add primary concept accession
                if (key.equals(CHEBI_ID)) {
                    c.createConceptAccession(value.replaceAll("CHEBI:", ""),
                            elementOf, false);
                } // add preferred name
                else if (key.equals(CHEBI_NAME)) {
                    c.createConceptName(value, true);
                } // use Definition as concept description
                else if (key.equals(DEFINITION)) {
                    c.setDescription(value);
                } // use Formulae as concept annotation
                else if (key.equals(FORMULAE)) {
                    c.setAnnotation(value);
                } // add secondary concept accession
                else if (key.equals(SECONDARY_CHEBI_ID)) {
                    // one ID per line
                    for (String s : value.split("\n")) {
                        c.createConceptAccession(s, elementOf, true);
                    }
                } // add InChIKey as attribute
                else if (key.equals(INCHIKEY)) {
                    c.createAttribute(anInChIKey, value, true);
                } // add InChI as attribute
                else if (key.equals(INCHI)) {
                    c.createAttribute(anInChI, value, true);
                } // cache existing SMILES
                else if (key.equals(SMILES)) {
                    smiles = value;
                } // add Star as attribute
                else if (key.equals(STAR)) {
                    c.createAttribute(anStar, Integer.valueOf(value), false);
                } // add Charge as attribute
                else if (key.equals(CHARGE)) {
                    // integer parsing doesn't like +2
                    c.createAttribute(anCharge,
                            Integer.valueOf(value.replace("+", "")), false);
                } // add Mass as attribute
                else if (key.equals(MASS)) {
                    try {
                        c.createAttribute(anMass, Double.valueOf(value), false);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                } // add last modified as attribute
                else if (key.equals(LAST_MODIFIED)) {
                    c.createAttribute(anModified, value, false);
                } // add other Synonyms
                else if (key.equals(SYNONYMS) || key.equals(IUPAC_NAMES)
                        || key.equals(INN) || key.equals(BRAND_NAMES)) {
                    // one name per line
                    for (String s : value.split("\n")) {
                        c.createConceptName(s, false);
                    }
                } // no mapping yet
                else if (!key.startsWith("cdk")) {
                    if (!dataSourceMapping.containsKey(key)) {
                        unmappedKeys.add(key);
                    } else {
                        // check data source
                        DataSource ds = graph.getMetaData().getDataSource(
                                dataSourceMapping.get(key));
                        if (ds != null) {
                            // multiple accessions
                            for (String s : value.split("\n")) {
                                c.createConceptAccession(s, ds, true);
                            }
                        } else {
                            fireEventOccurred(new DataSourceMissingEvent(
                                    "Mapping: " + key + " to "
                                    + dataSourceMapping.get(key),
                                    getCurrentMethodName()));
                        }
                    }
                }
            }

            try {
                // add chemical structure to concept
                ChemicalStructure cs = new ChemicalStructure();

                // construct chemical structure in MOL format
                StringWriter molString = new StringWriter();
                    MDLV2000Writer mw = new MDLV2000Writer(molString);
                    mw.write(ac);
                    mw.close();

                    cs.setMOL(molString.toString());

                    // check pre-existing SMILIES
                    if (smiles != null) {
                        cs.setSMILES(smiles);
                    } else {
                        // construct SMILE string for molecule
                        StringWriter smilesString = new StringWriter();
                        SMILESWriter sw = new SMILESWriter(smilesString);
                        sw.write(ac);
                        sw.close();
                        cs.setSMILES(smilesString.toString());
                    }

                // add attribute
                c.createAttribute(anChemicalStructure, cs, false);
            } catch (Exception cdk) {
                log.warn(String.format("error: '%s' for accession '%s'", cdk.getMessage(),ac.getProperty(CHEBI_ID)));
                //cdk.printStackTrace();
            }
        }

        fireEventOccurred(new GeneralOutputEvent("Missing mappings for: "
                + unmappedKeys, getCurrentMethodName()));
    }

}
