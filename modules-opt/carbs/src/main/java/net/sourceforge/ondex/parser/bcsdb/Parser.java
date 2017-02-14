package net.sourceforge.ondex.parser.bcsdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.parser.ONDEXParser;

/**
 *
 */
@DatabaseTarget(name = "BCSDB", description = "Bacterial Carbohydrate Structure DataBase", version = "pre-release BCSDB-3 gamma version", url = "http://www.glyco.ac.ru/bcsdb3")
@Custodians(custodians = {"Victor Lesk"}, emails = {"v.lesk at imperial.ac.uk"})
@Status(description = "declared on 28/07/2010", status = StatusType.DISCONTINUED) 
public class Parser extends ONDEXParser
{

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
                        FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)

        };
    }

    public String getName() {
        return "BCSDB";
    }

    public String getVersion() {
        return "0.1";
    }

    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public String getId() {
        return "bcsdb";
    }


    @Override
    public void start() throws Exception {
        String argument = (String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE);

        System.out.println(argument);
        File file = new File(argument);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));

        //Structure of database records are as follows, according to
        //http://www.glyco.ac.ru/bcsdb/cgi-csdb/csduser.cgi?mode=submit

        //Abbreviations list. (P) means it pertains to the publication part of the entry, (G) means it pertains to the glycan, (E) means it pertains to the entry itself, (R) means it pertains to the relation,.
        //An asterisk (*) means the field should always be present.

        //(? )ID: Serial number of record.
        //(R )TH: 0 or 1. 1 only if "structure was elucidated exactly in this paper", otherwise 0.
        //(P*)AU: Authors list.
        //(P*)TI: Article title.
        //(P*)JN: Name of journal.
        //(P*)PY: Publication year.
        //(P*)VL: Volume  - the main volume number
        //(P*)VL1: Volume  - the number in parentheses if applicable
        //(P*)PG1: Start page
        //(P )PG2: End page
        //(P )PB: Publishing company
        //(P )RL: URL to article
        //(P )EA: Corresponding author's e-mail address
        //(P*)AD: Full addresses of institutions involved, separated by SEMICOLONS
        //(P*)AB: Full abstract
        //(G*)ST1: Linearly encoded structure, using BCSDB-specific protocol which is described here:
        //            http://www.glyco.ac.ru/bcsdb/help/rules.html
        //(G*)ST2: Type of entry. This can take on the following values
        //            biol      biological repeating unit of the polymer
        //			  chem      chemical repeating unit of the polymer
        // 			  cyclo     repeating unit of cyclic structure
        //			  homo      homopolymer
        //            oligo     oligomeric structure
        //            mono      monomer
        //           this field can also be empty.
        //(G )ST3: Molecular mass
        //(R )SL: Structure location within publication
        //(G )AG: Aglycon information
        //(G )MF: Molecular formula, for mono and oligosaccharides only
        //(O*)SO: Biological source e.g. Campylobacter jejuni NCTC11168 or Campylobacter sp. if species unknown.
        //(O*)KD: Taxonomic group of organism
        //(O )PHL: Phylum of organism
        //(H )HO: Host organism
        //(H )OTI: Organ or tissue from which structure was extracted
        //(H )DSS: Host disease associated with structure
        //(G )NC: Trivial name of compound
        //(G )CC: Classes and roles of compound (CSV)
        //(P )MT: Methods used to get the structure (CSV)
        //(G )BA: Biological activity notes
        //(E )EI: Enzymes associated with compound (CSV)
        //(G )BG: Biosynthesis and genetic notes
        //(G )SY: Chemical synthesis notes
        //(P*)KW: Keywords for paper (CSV)
        //(? )NT: Any other notes
        //(G )3D: 3D-structure and conformation data notes
        //(? )NMRH: Proton NMR assignment
        //(? )NMRC: 13C NMR assignment
        //(? )NMRS: Solvent used in NMR
        //(? )NMRT: Temperature in Kelvins for NMR
        //(? )RR: BCSDB ID's of other structures in paper (CSV)
        //(G )CA: Chemical abstracts accession number
        //(G )RN: Chemical abstracts substance registry number
        //(? )PT: Patent number
        //(? )PR: Protein databases accession numbers
        //(? )SD: Spectroscopic databases accession number
        //(O )TAX: TAXID
        //(G )OT: Other database accession numbers
        //(?*)U1: Submitter surname
        //(?*)U2: Submission date to BCSDB
        //(? )U3 : Zelinsky Institute ID of this paper
        //(? )U4 : Zelinsky Institute IDs of related papers

        Hashtable<String, String> externalDBRefs = new Hashtable<String, String>();
        externalDBRefs.put("PMID", "NLM");
        externalDBRefs.put("PM", "NLM");
        externalDBRefs.put("DOI", "DOI");
        HashSet<String> missedDBRefs = new HashSet<String>();

        ONDEXGraphMetaData metaData = graph.getMetaData();

        DataSource dataSource = metaData.getFactory().createDataSource("BCSDB", "bacterial carbohydrate structure database");

        //Types of node that may be found in database: "Comp" (like kegg Compound) and Publication
        ConceptClass hostOrganismConceptClass = metaData.getConceptClass("Organism");
        ConceptClass diseaseConceptClass = metaData.getConceptClass("Disease");

        EvidenceType evidenceType = metaData.getEvidenceType("IMPD");
        RelationType publishedInRelationType = metaData.getRelationType("pub_in");
        RelationType expressedByRelationType = metaData.getRelationType("ex_by");
        RelationType locatedInRelationType = metaData.getRelationType("located_in");
        RelationType involvesRelationType = metaData.getRelationType("inv_in");

        String currentLine = null;

        int counter = 0;
        Hashtable<String, ONDEXConcept> publicationHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> compoundHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> organismHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> hostHashtable = new Hashtable<String, ONDEXConcept>();
        Hashtable<String, ONDEXConcept> diseaseHashtable = new Hashtable<String, ONDEXConcept>();

        // only initialise meta data once
        BcsdbOrganismInfo.initialiseMetaData(graph);
        BcsdbPublicationInfo.initialiseMetaData(graph);
        BcsdbCompoundInfo.initialiseMetaData(graph);

        //This parser assumes that consecutive records are separated by a blank line.
        while (reader.ready()) {
            String compoundKey = "";
            String publicationKey = "";
            String diseaseKey = "";

            BcsdbOrganismInfo organismsInfo = new BcsdbOrganismInfo();
            BcsdbPublicationInfo publicationInfo = new BcsdbPublicationInfo();
            BcsdbCompoundInfo compoundInfo = new BcsdbCompoundInfo();

            String[] hostStrings = new String[0];


            do//Some of these fields contain comma-separated values. Also null-valued Attribute are currently possible, this should not be permitted.
            {
                currentLine = reader.readLine();

                //Unrecorded fields
                if (currentLine.startsWith("ID:") || currentLine.startsWith("TH:") || currentLine.startsWith("SL:") ||
                        currentLine.startsWith("NT:") || currentLine.startsWith("NMRH:") || currentLine.startsWith("NMRC:") ||
                        currentLine.startsWith("NMRS:") || currentLine.startsWith("NMRT:") || currentLine.startsWith("RR:") ||
                        currentLine.startsWith("U1:") || currentLine.startsWith("U2:") || currentLine.startsWith("U3") ||
                        currentLine.startsWith("U4")) {
                }//Unrecorded

                //Publication related fields
                else if (currentLine.startsWith("AU:")) {
                    publicationInfo.authorsString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("TI:")) {
                    publicationKey = currentLine.substring(4).trim();
                    publicationInfo.articleTitleString = publicationKey;
                } else if (currentLine.startsWith("JN:")) {
                    publicationInfo.journalString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("PY:")) {
                    publicationInfo.yearPublishedString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("VL:")) {
                    publicationInfo.volumeString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("VL1:")) {
                    publicationInfo.volumeParenString = currentLine.substring(5).trim();
                } else if (currentLine.startsWith("PG1:")) {
                    publicationInfo.startPageString = currentLine.substring(5).trim();
                } else if (currentLine.startsWith("PG2:")) {
                    publicationInfo.endPageString = currentLine.substring(5).trim();
                } else if (currentLine.startsWith("PB:")) {
                    publicationInfo.publisherString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("RL:")) {
                    String line = currentLine.substring(4).trim();
                    if (line.contains("://"))
                        publicationInfo.urlString = line;
                    else {
                        String[] split = line.split(":");
                        // RL: PMID: 17892864, first data source, then reference
                        if (split.length == 2) {
                            split[0] = split[0].trim();
                            split[1] = split[1].trim();
                            if (externalDBRefs.containsKey(split[0])) {
                                DataSource external = metaData.getDataSource(externalDBRefs.get(split[0]));
                                if (external != null) {
                                    publicationInfo.references.add(new DBRef(split[1], external));
                                } else {
                                    System.out.println("Reference to non-existing meta data DataSource: " + externalDBRefs.get(split[0]));
                                }
                            } else {
                                missedDBRefs.add(split[0]);
                            }
                        }
                    }
                } else if (currentLine.startsWith("EA:")) {
                }//Unrecorded
                else if (currentLine.startsWith("AD:")) {
                }//Unrecorded
                else if (currentLine.startsWith("AB:")) {
                }//Unrecorded
                else if (currentLine.startsWith("MT:")) {
                    publicationInfo.analyticalMethodString = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("KW:")) {
                    publicationInfo.keywordsString = currentLine.substring(4).trim();
                }

                //Compound related fields
                else if (currentLine.startsWith("ST1:")) {
                    compoundKey = currentLine.substring(5).trim();
                    compoundInfo.linearStructure = compoundKey;
                } else if (currentLine.startsWith("ST2:")) {
                    compoundInfo.structureType = currentLine.substring(5).trim();
                } else if (currentLine.startsWith("ST3:")) {
                    compoundInfo.molecularMass = currentLine.substring(5).trim();
                } else if (currentLine.startsWith("AG:")) {
                    compoundInfo.aglyconInfo = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("MF:")) {
                    compoundInfo.molecularFormula = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("NC:")) {
                    compoundInfo.trivialName = currentLine.substring(4).trim();
                } else if (currentLine.startsWith("CC:")) {
                }//Unrecorded
                else if (currentLine.startsWith("BA:")) {
                }//Unrecorded
                else if (currentLine.startsWith("BG:")) {
                }//Unrecorded
                else if (currentLine.startsWith("SY:")) {
                }//Unrecorded
                else if (currentLine.startsWith("3D:")) {
                }//Unrecorded
                else if (currentLine.startsWith("CA:")) {
                }//Unrecorded

                else if (currentLine.startsWith("SO:")) {
                }//Ignored, redundant with taxid
                else if (currentLine.startsWith("KD:")) {
                }//Ignored, redundant with taxid
                else if (currentLine.startsWith("PHL:")) {
                }//Ignored, redundant with taxid
                else if (currentLine.startsWith("TAX:")) {
                    organismsInfo.taxids = currentLine.substring(5).trim().split(",");
                }

                //Host related field. Bug - Can be multiple organisms separated by commas.
                else if (currentLine.startsWith("HO:")) {
                    hostStrings = currentLine.substring(4).trim().split(",");
                }

                //Tissue related field
                else if (currentLine.startsWith("OTI:")) {
                }//for later

                //Disease related field
                else if (currentLine.startsWith("DSS:")) {
                    diseaseKey = currentLine.substring(5).trim();
                }

                //Enzyme related field
                else if (currentLine.startsWith("EI:")) {
                }//for later

                //Database related fields
                //Not handled yet
                else if (currentLine.startsWith("CA:")) {
                }//Accessions not handled yet.
                else if (currentLine.startsWith("RN:")) {
                }//Accessions not handled yet.
                else if (currentLine.startsWith("PR:")) {
                }//Accessions not handled yet.
                else if (currentLine.startsWith("SD:")) {
                }//Accessions not handled yet.
                else if (currentLine.startsWith("OT:")) {
                }//Accessions not handled yet.
                else if (currentLine.startsWith("PT:")) {
                }//Accessions not handled yet.
                else if (!(currentLine.trim().isEmpty())) {
                    System.out.println("/" + currentLine + "/");
                    System.out.println("/" + currentLine.trim() + "/");
                    System.out.println("/" + currentLine.trim().length() + "/");
                    throw new Exception("Unrecognized line start " + currentLine + " encountered in BCSDB parser.");
                }
            }
            while (!(currentLine.trim().isEmpty()));

            if (!compoundKey.isEmpty() && !publicationKey.isEmpty()) {
                //If compound or publication has not been seen before, add it to the hash table
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

                //Parse organisms which express compound, and their taxids. Add appropriate concepts.
                for (int i = 0; i < organismsInfo.taxids.length; ++i) {
                    organismsInfo.taxids[i].trim();

                    if (!(organismHashtable.containsKey(organismsInfo.taxids[i]))) {
                        ONDEXConcept organismConcept = organismsInfo.createONDEXConcept(i);
                        organismHashtable.put(organismsInfo.taxids[i], organismConcept);
                    }

                    // get participating concepts
                    fromConcept = compoundHashtable.get(compoundKey);
                    toConcept = organismHashtable.get(organismsInfo.taxids[i]);

                    // only create relation if not existing yet
                    if (graph.getRelation(fromConcept, toConcept, expressedByRelationType) == null) {

                        //Relate compound to publication by "ex_by"
                        graph.getFactory().createRelation
                                (fromConcept,
                                        toConcept,
                                        expressedByRelationType,
                                        evidenceType);
                    }
                }

                for (String hostString : hostStrings) {
                    hostString = hostString.trim();
                    if (hostString.length() > 0) {

                        ONDEXConcept hostConcept = null;
                        if (!(hostHashtable.containsKey(hostString))) {
                            hostConcept = graph.getFactory().createConcept(hostString, dataSource, hostOrganismConceptClass, evidenceType);
                            hostConcept.createConceptName(hostString, false);
                            hostHashtable.put(hostString, hostConcept);
                        } else {
                            hostConcept = hostHashtable.get(hostString);
                        }

                        for (String organismString : organismsInfo.taxids) {
                            fromConcept = organismHashtable.get(organismString);

                            if (graph.getRelation(fromConcept, hostConcept, locatedInRelationType) == null) {
                                graph.getFactory().createRelation
                                        (fromConcept,
                                                hostConcept,
                                                locatedInRelationType,
                                                evidenceType);
                            }
                        }
                    }
                }


                if (!(diseaseKey.isEmpty())) {
                    if (!(diseaseHashtable.containsKey(diseaseKey))) {
                        ONDEXConcept diseaseConcept = graph.getFactory().createConcept(diseaseKey, dataSource, diseaseConceptClass, evidenceType);
                        diseaseConcept.createConceptName(diseaseKey, false);
                        diseaseHashtable.put(diseaseKey, diseaseConcept);
                    }

                    for (int i = 0; i < organismsInfo.taxids.length; ++i) {

                        fromConcept = organismHashtable.get(organismsInfo.taxids[i]);
                        toConcept = diseaseHashtable.get(diseaseKey);

                        if (graph.getRelation(fromConcept, toConcept, involvesRelationType) == null) {
                            graph.getFactory().createRelation
                                    (fromConcept,
                                            toConcept,
                                            involvesRelationType,
                                            evidenceType);
                        }
                    }
                }
            }
            ++counter;
        }
        reader.close();

        System.out.println(counter + " entries.");

        System.out.println("Missing external DB refs: " + missedDBRefs);
    }
}
