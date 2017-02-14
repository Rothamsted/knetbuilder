package net.sourceforge.ondex.parser.carbbank;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
@Custodians(custodians = {"Victor Lesk"}, emails = {"v.lesk at imperial.ac.uk"})
public class Parser extends ONDEXParser
{

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)

        };
    }

    public String getName() {
        return "carbbank";
    }

    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getId() {
        return "carbbank";
    }


    public String[] requiresValidators() {
        return new String[0];
    }

    Hashtable<String, ONDEXConcept> publicationHashtable;
    Hashtable<String, ONDEXConcept> compoundHashtable;

    public void start() throws Exception {
        String argument = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);

        System.out.println(argument);
        File file = new File(argument);

        // one zip file can contain multiple other files
        ZipFile zip = new ZipFile(file);

        // iterate over all entries in zip file
        Enumeration<? extends ZipEntry> entries = zip.entries();
        if (!(entries.hasMoreElements())) {
            throw new Exception("No files detected in carbbank zip archive.");
        }

        ZipEntry entry = entries.nextElement();

        if (!(("/" + entry.getName()).endsWith("carbbank.txt"))) {
            throw new Exception("First file in carbbank zip archive has unexpected name:" + entry.getName());
        }

        if (entries.hasMoreElements()) {
            throw new Exception("More than one file found in carbbank zip archive. Carbbank zip archive should contain just one file, carbbank.zip.");
        }

        System.out.println("Opening entry: " + entry.getName());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                zip.getInputStream(entry)));


        //This is what the original CarbBank paper claims to provide for each entry:
        //From Trends in Biochemical Sciences, 14 (Dec 1989), 475ff
        //C# Carbbank accession number
        //CU Curator name
        //DA Entry date and revision date
        //AU Authors of Citation
        //TI Title of Citation
        //JL Journal of Citation
        //NT Notes relevant to Citation
        //SP Spectroscopic methods used in structure determination
        //BA Biological source, activity
        //BI Information about binding studies
        //KW Key words for record
        //GC Glycoconjugate info
        //CB Other CCSD records, cross-reference
        //CA Chemical abstracts, cross-reference
        //PA Patent literature, cross-reference
        //PR Protein database, cross-reference
        //SD Spectroscopic database, cross-reference
        //OT Other databases, cross-reference

        //Another CarbBank letter is Glycobiology vol 1, no 2 p 113, 1991
        //Another CarbBank letter is Glycobiology vol 2, no 6 pp 505-507, 1992

        //The abbreviation used in the CarbBank zip file provided by GlycomeDB seem to be here
        //and no-where else on the web: http://www.dkfz.de/spec/dipl_diss/alex_loss.pdf.
        //The important table is on page 68 and refers [Bun98], namely:
        //Peter Bunsmann, Konzeption und Realization ener relationalen, molekuelstruktur-
        //orienterten Datenbank fuer glykowissenschaftliche Anwendungen mit einem Internet/WWW
        //basierten Zugriff. Universitaet Hildesheim, Institut fuer Physik und technishe Informatik,
        //(1998).

        //Abbreviations according to p68 Table. (P) means it pertains to the publication part of the entry, (G) means it pertains to the glycan, (E) means it pertains to the entry itself
        //AG Aglycon (G)                        ///////////////////////
        //AM Analytical method (P)				//MT Molecular type (G)
        //AN Antigen information (P)        	//NC Non-carbohydrate (G)
        //AU Authors (P)						//NT Notes (?)
        //BA Biological activity (G)			//PA Protein attachment site (G)
        //BI Binding to lectins etc. (G)		//PM Parent molecule (G)
        //BS Biological source	(G)				//SB Submitted by (E)
        //CT Citation (P)						//SC Structure code (G)
        //CC CCSD accession number (G)			//SI Structure index (G)
        //DA Entry date	(E)						//ST Synthetic target name (G)
        //DB references to other database (G,P)	//TI Title of article (P)
        //EI Enzyme information	(P?)			//TN Trivial name (G)
        //FC CarbBank full complexity index (?) //VR Variable region (G)
        //MF Molecular formula (G)				//VS Verification code (not present)

        ONDEXGraphMetaData metaData = graph.getMetaData();

        EvidenceType evidenceType = metaData.getEvidenceType("IMPD");
        RelationType publishedInRelationType = metaData.getRelationType("pub_in");

        //Attributes for comp
        int counter = 0;
        publicationHashtable = new Hashtable<String, ONDEXConcept>();
        compoundHashtable = new Hashtable<String, ONDEXConcept>();

        // this table is a mapping from external database references used in CARBBANK to their corresponding names in ONDEX meta data
        Hashtable<String, String> externalDBRefs = new Hashtable<String, String>();
        externalDBRefs.put("SwissProt", "UNIPROTKB");
        externalDBRefs.put("PMID", "NLM");
        externalDBRefs.put("PIR", "PIR");
        externalDBRefs.put("PIR1", "PIR");
        externalDBRefs.put("PIR2", "PIR");
        externalDBRefs.put("PIR3", "PIR");
        externalDBRefs.put("EMBL", "PROID");
        externalDBRefs.put("OGlycBase", "O-GLYCBASE");
        externalDBRefs.put("OGlycbase", "O-GLYCBASE");
        HashSet<String> missedDBRefs = new HashSet<String>();

        // only initialise meta data once
        CarbbankPublicationInfo.initialiseMetaData(graph);
        CarbbankCompoundInfo.initialiseMetaData(graph);

        String currentLine = null;

        while (reader.ready()) {//Null-valued Attribute are currently possible, this should not be permitted.
            //Find beginning of record
            do {
                currentLine = reader.readLine();
            }
            while (!currentLine.startsWith(";"));

            CarbbankCompoundInfo compoundInfo = new CarbbankCompoundInfo();
            CarbbankPublicationInfo publicationInfo = new CarbbankPublicationInfo();

            String field = new String("");
            String publicationKey = null;
            String compoundKey = null;

            //Assimilate non-structure elements of record

            do {
                while (!(currentLine.startsWith("AG:") || currentLine.startsWith("CC:") || currentLine.startsWith("NT:") || currentLine.startsWith("TN:") ||
                        currentLine.startsWith("AM:") || currentLine.startsWith("DA:") || currentLine.startsWith("PA:") || currentLine.startsWith("VR:") ||
                        currentLine.startsWith("AN:") || currentLine.startsWith("DB:") || currentLine.startsWith("PM:") || currentLine.startsWith("VS:") ||
                        currentLine.startsWith("AU:") || currentLine.startsWith("SB:") || currentLine.startsWith("-----") ||
                        currentLine.startsWith("BA:") || currentLine.startsWith("FC:") || currentLine.startsWith("SC:") ||
                        currentLine.startsWith("MF:") || currentLine.startsWith("SI:") ||
                        currentLine.startsWith("BS:") || currentLine.startsWith("MT:") || currentLine.startsWith("ST:") ||
                        currentLine.startsWith("CT:") || currentLine.startsWith("NC:") || currentLine.startsWith("TI:"))) {
                    field += " " + currentLine;
                    currentLine = reader.readLine();
                }

                if (field.startsWith("AG:")) {
                    compoundInfo.aglycon.add(field.substring(4).trim());
                } else if (field.startsWith("BA:")) {
                    compoundInfo.biologicalActivity.add(field.substring(4).trim());
                } else if (field.startsWith("BS:")) {
                    compoundInfo.biologicalSource.add(field.substring(4).trim());
                }
                // the concept accession is important for later on mapping to KEGG etc.
                else if (field.startsWith("CC:")) {
                    compoundInfo.ccsdAccession = field.substring(4).trim();
                } else if (field.startsWith("MF:")) {
                    compoundInfo.molecularFormula = field.substring(4).trim();
                } else if (field.startsWith("MT:")) {
                    compoundInfo.molecularType = field.substring(4).trim();
                } else if (field.startsWith("NC:")) {
                    compoundInfo.nonCarbohydrate.add(field.substring(4).trim());
                } else if (field.startsWith("PA:")) {
                    compoundInfo.proteinAttachmentSite.add(field.substring(4).trim());
                }
                // PM could be represented as a relation, I need to investigate a bit more
                else if (field.startsWith("PM:")) {
                    compoundInfo.parentMolecule.add(field.substring(4).trim());
                } else if (field.startsWith("SC:")) {
                    compoundInfo.structureCode.add(field.substring(4).trim());
                } else if (field.startsWith("SI:")) {
                    compoundKey = field.substring(4).trim();
                    compoundInfo.structureIndex = compoundKey;
                } else if (field.startsWith("ST:")) {
                    compoundInfo.syntheticTarget.add(field.substring(4).trim());
                } else if (field.startsWith("TN:")) {
                    compoundInfo.trivialName.add(field.substring(4).trim());
                } else if (field.startsWith("VR:")) {
                    compoundInfo.variableRegion.add(field.substring(4).trim());
                } else if (field.startsWith("AM:")) {
                    publicationInfo.analyticalMethod.add(field.substring(4).trim());
                }
                // not sure if antigen information is best on publication... leave for now
                else if (field.startsWith("AN:")) {
                    publicationInfo.antigenInfo.add(field.substring(4).trim());
                } else if (field.startsWith("AU:")) {
                    publicationInfo.authors = field.substring(4).trim();
                } else if (field.startsWith("CT:")) {
                    publicationInfo.citation = field.substring(4).trim();
                    publicationKey = publicationInfo.citation;
                } else if (field.startsWith("TI:")) {
                    publicationInfo.title = field.substring(4).trim();
                } else if (field.startsWith("DB:")) {
                    String[] split = field.substring(4).trim().split(":");
                    // pairs of PMID:9433765, first data source, then reference
                    if (split.length == 2) {
                        if (externalDBRefs.containsKey(split[0])) {
                            DataSource external = metaData.getDataSource(externalDBRefs.get(split[0]));
                            if (split[1].indexOf("--") > 0)
                                split[1] = split[1].substring(0, split[1].indexOf("--")).trim();
                            if (external != null) {
                                if (split[0].equals("PMID")) {
                                    publicationInfo.references.add(new DBRef(split[1], external));
                                } else {
                                    compoundInfo.references.add(new DBRef(split[1], external));
                                }
                            } else {
                                System.out.println("Reference to non-existing meta data DataSource: " + externalDBRefs.get(split[0]));
                            }
                        } else {
                            missedDBRefs.add(split[0]);
                        }
                    }
                }

                field = currentLine;
                currentLine = reader.readLine();
            }
            while (!(field.startsWith("-----")));

            //Assimilate structure
            currentLine = reader.readLine();
            while (!currentLine.startsWith("=")) {
                compoundInfo.structure += currentLine + "\n";
                currentLine = reader.readLine();
            }

            //If compound or publication has not been seen before, add it to the hash table
            if (!(compoundKey.isEmpty()) && !(publicationInfo.citation.isEmpty())) {
                if (!(compoundHashtable.containsKey(compoundKey))) {
                    ONDEXConcept compoundConcept = compoundInfo.createONDEXConcept();
                    compoundHashtable.put(compoundKey, compoundConcept);
                }
                if (!(publicationHashtable.containsKey(publicationKey))) {
                    ONDEXConcept publicationConcept = publicationInfo.createONDEXConcept();
                    publicationHashtable.put(publicationKey, publicationConcept);
                }

                // get participating concepts
                ONDEXConcept fromConcept = compoundHashtable.get(compoundKey);
                ONDEXConcept toConcept = publicationHashtable.get(publicationKey);

                // only create relation if not existing yet
                if (graph.getRelation(fromConcept, toConcept, publishedInRelationType) == null) {

                    //Relate compound to publication by "pub_in"
                    graph.getFactory().createRelation
                            (fromConcept,
                                    toConcept,
                                    publishedInRelationType,
                                    evidenceType);
                }
            } else {
                if (compoundKey.isEmpty()) {
                    System.out.println("Well-formed compound index field headed by SI: was not detected for CARBBANK entry " + counter + ".");
                }
                if (publicationInfo.citation.isEmpty()) {
                    System.out.println("Well-formed publication citation headed by CT: was not detected within CARBBANK entry " + counter + ".");
                }
                throw new Exception("CARBBANK entry " + counter + " is malformed.");
            }

            ++counter;
        }

        System.out.println("Missing external DB refs: " + missedDBRefs);

        reader.close();

        System.out.println(counter + " entries.");
    }
}
