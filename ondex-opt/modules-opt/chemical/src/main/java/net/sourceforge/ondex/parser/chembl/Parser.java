package net.sourceforge.ondex.parser.chembl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.data.ChemicalStructure;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/**
 * Parser for the ChEMBL database
 *
 * @author taubertj
 *
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Status(status = StatusType.STABLE, description = "Tested December 2013 (Jacek Grzebyta)")
public class Parser extends ONDEXParser implements MetaData {

    private static final String ONLY_REFERENCED = "OnlyReferenced";
    private static final String CHEMBL_ID = "chembl_id";
    private static final String CHEBI_ID = "chebi_id";
    AttributeName anChemicalStructure;
    IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
    DataSource dsCHEMBL, dsCHEBI;
    EvidenceType evidencetype;
    ConceptClass ofType;
    // track unmapped keys
    Set<String> unmappedKeys = new HashSet<String>();

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                    new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                    FileArgumentDefinition.INPUT_FILE_DESC, true, true,
                    false),
                    new BooleanArgumentDefinition(ONLY_REFERENCED,
                    "Import only referenced ChEMBL entries", false, false)};
    }

    @Override
    public String getId() {
        return "chembl";
    }

    @Override
    public String getName() {
        return "ChEMBLdb";
    }

    @Override
    public String getVersion() {
        return "31.01.2013";
    }

    /**
     * initialise Ondex meta-data
     */
    private void initMetaData() {

        // basic concept meta data
        dsCHEMBL = graph.getMetaData().getDataSource(DS_CHEMBL);
        dsCHEBI = graph.getMetaData().getDataSource(DS_CHEBI);
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
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // setup meta data
        initMetaData();

        // option to parse only referenced entries
        Boolean referenced = (Boolean) args.getUniqueValue(ONLY_REFERENCED);

        // parse all accessions contained in graph
        Set<String> accessions = new HashSet<String>();
        if (referenced) {
            for (ONDEXConcept c : graph.getConcepts()) {
                for (ConceptAccession ca : c.getConceptAccessions()) {
                    accessions.add(ca.getAccession());
                }
            }
        }

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

        // read SDF file one by one
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));

        // guess format of file and construct reader
        ISimpleChemObjectReader chemReader = new MDLV2000Reader();

        // build up each single compound to save memory
        int count = 0;
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            String line = reader.readLine();
            // marks the end of one compound
            if (line.startsWith("$$$$")) {
                sb.append(line + "\n");

                // reuse old reader object to save memory
                chemReader.setReader(new ByteArrayInputStream(sb.toString()
                        .getBytes()));

                // check that file contains molecules
                if (chemReader.accepts(Molecule.class)) {

                    // read content of file
                    ChemFile content = (ChemFile) chemReader
                            .read((ChemObject) new ChemFile());

                    // list all molecules
                    List<IAtomContainer> containersList = ChemFileManipulator
                            .getAllAtomContainers(content);
                    for (IAtomContainer ac : containersList) {
                        if (!referenced
                                || accessions.contains(ac
                                .getProperty(CHEMBL_ID))) {
                            addMolecule(ac);
                            count++;
                        }
                    }
                }

                // empty StringBuffer
                sb.delete(0, sb.length());
            } else {
                sb.append(line + "\n");
            }
        }
        reader.close();

        fireEventOccurred(new GeneralOutputEvent("Total molecules parsed:"
                + count, getCurrentMethodName()));

        fireEventOccurred(new GeneralOutputEvent("Missing mappings for: "
                + unmappedKeys, getCurrentMethodName()));
    }

    /**
     * Converts IAtomContainer to ONDEXConcept
     *
     * @param ac IAtomContainer
     */
    private void addMolecule(IAtomContainer ac) {
        // there should be a ChEMBL ID
        String id = "";
        if (ac.getProperties().containsKey(CHEMBL_ID)) {
            id = (String) ac.getProperty(CHEMBL_ID);
        } else {
            fireEventOccurred(new InconsistencyEvent(
                    "ChEMBLdb ID missing for: " + ac, getCurrentMethodName()));
        }

        // create concept prototype
        ONDEXConcept c = graph.getFactory().createConcept(id, dsCHEMBL,
                ofType, evidencetype);

        // parse all properties of molecule
        for (Entry<Object, Object> entry : ac.getProperties().entrySet()) {

            // cast key and value to String
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // add primary concept accession
            if (key.equals(CHEMBL_ID)) {
                loadConcept(c, value, dsCHEMBL, false);
            } // add secondary concept accession
            else if (key.equals(CHEBI_ID)) {
                // one ID per line
                for (String s : value.split("\n")) {
                    loadConcept(c, s, dsCHEMBL, true);
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
                            loadConcept(c, s, ds, true);
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

            // construct SMILE string for molecule
            StringWriter smilesString = new StringWriter();
            SMILESWriter sw = new SMILESWriter(smilesString);
            sw.write(ac);
            sw.close();
            cs.setSMILES(smilesString.toString());

            // add attribute
            c.createAttribute(anChemicalStructure, cs, false);
        } catch (CDKException cdk) {
            fireEventOccurred(new InconsistencyEvent(
                    "CDK Problem constructing MOL and SMILES for: "
                    + ac.getProperty(CHEMBL_ID), getCurrentMethodName()));
            cdk.printStackTrace();
        } catch (IOException e) {
            fireEventOccurred(new InconsistencyEvent(
                    "IO Problem constructing MOL and SMILES for: "
                    + ac.getProperty(CHEMBL_ID), getCurrentMethodName()));
            e.printStackTrace();
        }
    }

    private void loadConcept(ONDEXConcept c, String accession, DataSource elementOf, boolean ambiguous) {
        // check accession
        String newAcc = accession.trim();
        for (String p : newAcc.split(",")) {
            c.createConceptAccession(p.trim(), elementOf, ambiguous);
        }
    }
}
