package net.sourceforge.ondex.parser.oglycbase;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 */
@Custodians(custodians = {"Victor Lesk"}, emails = {"v.lesk at imperial.ac.uk"})
public class Parser extends ONDEXParser
{

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, true, false)
        };
    }

    public String getName() {
        return "oglycbase";
    }

    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getId() {
        return "oglycbase";
    }


    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        // which directory to parse XML files
        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));

        //Structure of database records are as follows:
        // >   		    Entry accession number and entry date
        // GLYCPROT:	Glycoprotein name, and alternative names
        // SPECIES:	    Species
        // DB_REF:      Crossreferences to PIR, SWISS-PROT, PDB and PROSITE.
        // OGLYCAN:	    Type of carbohydrate linked to serine or threonine
        // SER:		    Residue numbers of the O-linked serines
        // THR:		    Residue numbers of the O-linked threonines
        // ASN: 		Residue numbers of the N-linked asparagines
        // TRP: 		Residue numbers of the C-linked tryptophans
        // REFERENCES:  References of O-glycan assignment.
        // SEQ:		    Sequence length, including signal peptide.
        // SEQUENCE     in one letter code. ex:	 STPSTPNASKLPGHSTNGT
        // Assignment                            ...ST.N.......stn..

        Hashtable<String, String> externalDBRefs = new Hashtable<String, String>();
        externalDBRefs.put("SWISS", "UNIPROTKB");
        externalDBRefs.put("EMBL", "UNIPROTKB");
        externalDBRefs.put("InterPro", "IPRO");
        externalDBRefs.put("PFam", "PFAM");
        externalDBRefs.put("SMART", "SMART");
        externalDBRefs.put("PROSITE", "PROSITE");
        externalDBRefs.put("PMID", "NLM");
        externalDBRefs.put("PDB", "PDB");
        externalDBRefs.put("MEDLINE", "NLM");
        externalDBRefs.put("MIM", "OMIM");
        externalDBRefs.put("HSSP", "HSSP");
        externalDBRefs.put("PRINTS", "PRINTS");
        externalDBRefs.put("Genew", "HGNC");
        //Ignore glycosuitedb accessions since they are wrong -
        //they are just duplicates of the SWISSPROT accessions.
        HashSet<String> missedDBRefs = new HashSet<String>();

        ONDEXGraphMetaData metaData = graph.getMetaData();

        DataSource dataSource = metaData.getFactory().createDataSource("OGLYCBASE", "O-GLYCBASE");

        ConceptClass organismConceptClass = metaData.getConceptClass("Organism");

        EvidenceType evidenceType = metaData.getEvidenceType("IMPD");

        RelationType publishedInRelationType = metaData.getRelationType("pub_in");
        RelationType expressedByRelationType = metaData.getRelationType("ex_by");

        int counter = 0;

        String currentLine = reader.readLine();

        if (!(currentLine.startsWith(">"))) {
            throw new Exception("File start character is expected to be '>'.");
        } else {
            currentLine = currentLine.substring(1).trim();
        }

        Hashtable<String, ONDEXConcept> publicationHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> proteinHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> organismHashtable = new Hashtable<String, ONDEXConcept>();

        OglycbasePublicationInfo.initialiseMetaData(graph);
        OglycbaseGlycoproteinInfo.initialiseMetaData(graph);


        do {
            OglycbaseGlycoproteinInfo proteinInfo = new OglycbaseGlycoproteinInfo();

            String speciesKey = "";

            Set<String> referencesStrings = new HashSet<String>();

            while (!(currentLine.startsWith("GLYCPROT:"))) {
                proteinInfo.idString += currentLine.trim();
                currentLine = reader.readLine();
            }

            currentLine = currentLine.substring(9).trim();

            while (!(currentLine.startsWith("SPECIES:"))) {
                proteinInfo.glycprotString += " ";
                proteinInfo.glycprotString += currentLine.trim();
                currentLine = reader.readLine();
            }
            proteinInfo.glycprotString = proteinInfo.glycprotString.trim();

            currentLine = currentLine.substring(8).trim().replaceAll(".", "");

            String[] splitSpecies = currentLine.split(",");
            speciesKey = splitSpecies[0];

            currentLine = reader.readLine();
            if (!(currentLine.startsWith("DB_REF:"))) {
                throw new Exception("Line encountered between species and dbref.");
            }

            currentLine = currentLine.substring(7).trim();

            while (!(currentLine.startsWith("OGLYCAN:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    String[] split = currentLine.split(",");
                    if (split.length < 2) {
                        throw new Exception("Corrupt accession encountered:" + currentLine);
                    } else {
                        split[0] = split[0].trim();
                        if (externalDBRefs.containsKey(split[0])) {
                            DataSource external = metaData.getDataSource(externalDBRefs.get(split[0]));
                            if (external != null) {
                                proteinInfo.references.add(new DBRef(split[1].trim(), external));
                            } else {
                                System.out.println("Reference to non-existing meta data DataSource: " + externalDBRefs.get(split[0]));
                            }
                        } else if (!(split[0].equals("GlycosuiteDB"))) {
                            missedDBRefs.add(split[0]);
                        }
                    }
                }
                currentLine = reader.readLine();
            }

            currentLine = currentLine.substring(8).trim();

            while (!(currentLine.startsWith("SER:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    proteinInfo.oglycanString += currentLine.trim();
                }
                currentLine = reader.readLine();
            }

            currentLine = currentLine.substring(4).trim().substring(13).trim();

            String accumulator = "";

            while (!(currentLine.contains("Predicted:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.serExpStrings = currentLine.trim().split(",");

            currentLine = currentLine.substring(11).trim();
            accumulator = "";

            while (!(currentLine.startsWith("THR:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.serPreStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(4).trim().substring(13).trim();
            accumulator = "";

            while (!(currentLine.contains("Predicted:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.thrExpStrings = accumulator.trim().split(",");

            currentLine = currentLine.replace("Predicted", "").trim();
            accumulator = "";

            while (!(currentLine.startsWith("ASN:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.thrPreStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(4).trim().substring(13).trim();
            accumulator = "";

            while (!(currentLine.contains("Predicted:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.asnExpStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(11).trim();
            accumulator = "";

            while (!(currentLine.startsWith("TRP:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.asnPreStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(4).trim().substring(13).trim();
            accumulator = "";

            while (!(currentLine.contains("Predicted:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.trpExpStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(11).trim();
            accumulator = "";

            while (!(currentLine.startsWith("REFERENCES:"))) {
                if (currentLine.length() > 5 || currentLine.replaceAll("-", "").trim().length() > 0) {
                    accumulator += currentLine;
                }
                currentLine = reader.readLine().trim();
            }
            proteinInfo.trpPreStrings = accumulator.trim().split(",");

            currentLine = currentLine.substring(11).trim();

            boolean boolMultipleReferences = currentLine.startsWith("1:");
            accumulator = "";

            while (!(currentLine.startsWith("SEQ:"))) {
                if (boolMultipleReferences &&
                        (!(accumulator.isEmpty())) &&
                        currentLine.charAt(0) != ' ' &&
                        currentLine.charAt(1) == ':' &&
                        currentLine.charAt(2) == ' ') {
                    referencesStrings.add(accumulator.substring(3).trim());
                    accumulator = currentLine.trim();
                } else {
                    accumulator += " ";
                    accumulator += currentLine.trim();
                }
                currentLine = reader.readLine().trim();
            }

            if (boolMultipleReferences) {
                referencesStrings.add(accumulator.substring(3).trim());
            } else if ((accumulator.length() > 5 || accumulator.replaceAll("-", "").trim().length() > 0)
                    &&
                    !(accumulator.trim().equals("Unpublished"))) {
                referencesStrings.add(accumulator.trim());
            }

            proteinInfo.numseq = Integer.valueOf(currentLine.substring(4).trim());

            currentLine = reader.readLine();

            while ((!(currentLine.startsWith(">"))) &&
                    (!(currentLine.startsWith("COMMENT:"))) &&
                    (!(currentLine.startsWith("COMMEN:T")))) {
                proteinInfo.sequenceString += currentLine.trim().split(" ")[0];
                if (reader.ready()) {
                    currentLine = reader.readLine();
                } else {
                    break;
                }
            }

            while (reader.ready() && (!(currentLine.startsWith(">")))) {
                if (currentLine.startsWith("COMMENT:")) {
                    currentLine = currentLine.replace("COMMENT:", "").trim();
                } else if (currentLine.startsWith("COMMEN:T")) {
                    currentLine = currentLine.replace("COMMEN:T", "").trim();
                } else {
                    currentLine = currentLine.trim();
                }
                if (currentLine.length() > 5 && currentLine.replaceAll("-", "").trim().length() > 0) {
                    proteinInfo.commentStrings.add(currentLine);
                }
                currentLine = reader.readLine();
            }

            //If protein or publication has not been seen before, add it to the hash table

            ONDEXConcept proteinConcept = null;
            ONDEXConcept organismConcept = null;

            if ((!(proteinInfo.glycprotString.isEmpty()))) {
                if (!(proteinHashtable.containsKey(proteinInfo.glycprotString))) {
                    proteinConcept = proteinInfo.createONDEXConcept();
                    proteinHashtable.put(proteinInfo.glycprotString, proteinConcept);
                } else {
                    proteinConcept = proteinHashtable.get(proteinInfo.glycprotString);
                }
            } else {
                throw new Exception("Glycoprotein without name encountered in OGlycBASE parser.");
            }

            if (!speciesKey.isEmpty()) {
                if (!(organismHashtable.containsKey(speciesKey))) {
                    organismConcept = graph.getFactory().createConcept(speciesKey, dataSource, organismConceptClass, evidenceType);
                    organismConcept.createConceptName(speciesKey, true);
                    organismHashtable.put(speciesKey, organismConcept);
                } else {
                    organismConcept = organismHashtable.get(speciesKey);
                }
            }

            proteinInfo.sequenceByGlycosylationString = proteinInfo.sequenceString.substring(proteinInfo.numseq);
            proteinInfo.sequenceString = proteinInfo.sequenceString.substring(0, proteinInfo.numseq);

            if (proteinInfo.sequenceString.length() != proteinInfo.sequenceByGlycosylationString.length() ||
                    proteinInfo.sequenceString.contains(".")) {
                System.err.println(proteinInfo.numseq);
                System.err.println(proteinInfo.sequenceString);
                System.err.println(proteinInfo.sequenceByGlycosylationString);
                throw new Exception("Corrupt sequence/glycosequence information for glycoprotein without name encountered OGlycBASE parser.");
            }

            if (proteinConcept != null && organismConcept != null && graph.getRelation(proteinConcept, organismConcept, expressedByRelationType) == null) {
                //Relate protein to organism by ex_by (expressed-by) relation
                graph.getFactory().createRelation
                        (proteinConcept, organismConcept, expressedByRelationType, evidenceType);
            }

            if (referencesStrings.size() > 0) {
                for (String referenceString : referencesStrings) {
                    OglycbasePublicationInfo publicationInfo = new OglycbasePublicationInfo(referenceString);

                    if (!publicationInfo.citationString.isEmpty()) {
                        ONDEXConcept publicationConcept = null;

                        if (!(publicationHashtable.containsKey(publicationInfo.citationString))) {
                            publicationConcept = publicationInfo.createONDEXConcept();
                            publicationHashtable.put(publicationInfo.citationString, publicationConcept);
                        } else {
                            publicationConcept = publicationHashtable.get(publicationInfo.citationString);
                        }

                        if (proteinConcept != null && graph.getRelation(proteinConcept, publicationConcept, publishedInRelationType) == null) {
                            //Relate protein to publication by "pub_in"
                            graph.getFactory().createRelation
                                    (proteinConcept, publicationConcept, publishedInRelationType, evidenceType);
                        }
                    }
                }
            }
            ++counter;
        }
        while (reader.ready());

        reader.close();

        System.out.println(counter + " entries.");
    }
}