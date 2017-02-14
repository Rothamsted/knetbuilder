package net.sourceforge.ondex.parser.kegg52.gene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg52.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.sink.Sequence;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class ParseGeneFile implements Runnable {

    private static final String DEFINITION = "DEFINITION";
    private static final String ORTHOLOGY = "ORTHOLOGY";
    private static final String ENTRY = "ENTRY";
    private static final String NAME = "NAME";
    private static final String DBLINKS = "DBLINKS";
    private static final String AASEQ = "AASEQ";
    private static final String NTSEQ = "NTSEQ";

    private String org;
    private boolean allSeq;
    private String fileName;
    private DPLPersistantSet<Sequence> sequenceCache;
    private DPLPersistantSet<Relation> relationsCache;
    private Set<String> foundGenes = new HashSet<String>();
    private Map<String, Boolean> missingGenes = new HashMap<String, Boolean>();
    private Map<String, Set<String>> references;

    private Set<String> nonEnzymeGenes;
    private Set<String> enzymeGenes;
    private String organismFullName;

    public ParseGeneFile(
            Set<String> nonEnzymeGenes,
            Set<String> enzymeGenes,
            String org,
            String organismFullName,
            String fileName,
            boolean allSeq,
            DPLPersistantSet<Sequence> sequenceCache,
            DPLPersistantSet<Relation> relationsCache,
            Map<String, Set<String>> references) {
        this.nonEnzymeGenes = nonEnzymeGenes;
        this.enzymeGenes = enzymeGenes;
        this.org = org.toUpperCase();
        this.fileName = fileName;
        this.allSeq = allSeq;
        this.sequenceCache = sequenceCache;
        this.relationsCache = relationsCache;
        this.references = references;
    }

    public void run() {
        final Pattern tabsOrSpace = Pattern.compile("[ \t]+");
        try {

            InputStream is = null;

            if (fileName.endsWith("tar.gz")) {
                TarInputStream tis = new TarInputStream(new GZIPInputStream(new FileInputStream(fileName)));

                TarEntry entry;
                while ((entry = tis.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (name.equalsIgnoreCase(organismFullName)) {
                        is = tis;
                        break;
                    }
                }
            }

            File file = new File(fileName);

            int detectedEnding = ZipEndings.getPostfix(file);

            if (is == null) {
                switch (detectedEnding) {
                    case ZipEndings.GZ:
                        is = new GZIPInputStream(new FileInputStream(file));
                        break;
                    case ZipEndings.ZIP:
                        is = new ZipInputStream(new FileInputStream(file));
                        break;
                    default:
                        break;
                }
            }

            BufferedReader reader;

            if (is != null) reader = new BufferedReader(new InputStreamReader(is));
            else reader = new BufferedReader(new FileReader(file));

            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith(ENTRY)) {

                    line = tabsOrSpace.split(line)[1].toUpperCase();
                    boolean isEnzyme = enzymeGenes.contains(line);

                    if (allSeq || isEnzyme || nonEnzymeGenes.contains(line)) {
                        foundGenes.add(line);
                        parseConceptFromFile(org, line, isEnzyme, reader);
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Parser.propagateEventOccurred(new DataFileErrorEvent(e.getMessage(), ""));
            e.printStackTrace();
        } catch (IOException e) {
            Parser.propagateEventOccurred(new DataFileErrorEvent(e.getMessage(), ""));
            e.printStackTrace();
        }

        //compare sets of found genes with given genes
        if (!foundGenes.containsAll(nonEnzymeGenes)) { //IE MISSING GENES
            Iterator<String> it = nonEnzymeGenes.iterator();
            while (it.hasNext()) {
                String gene = it.next();
                //if a gene was missing, add it to clashed genes
                if (!foundGenes.contains(gene)) {
                    missingGenes.put(gene, Boolean.FALSE); //no sync exclusive to this object on an org
                }
            }
        }

        if (!foundGenes.containsAll(enzymeGenes)) { //IE MISSING GENES
            Iterator<String> it = enzymeGenes.iterator();
            while (it.hasNext()) {
                String gene = it.next();
                //if a gene was missing, add it to missing genes
                if (!foundGenes.contains(gene)) {
                    missingGenes.put(gene, Boolean.TRUE); //no sync exclusive to this object on an org
                }
            }
        }

        nonEnzymeGenes = null;
        enzymeGenes = null;
        foundGenes = null;
    }

    private StringBuilder naSeq = new StringBuilder(200);
    private StringBuilder aaSeq = new StringBuilder(200);

    private final Pattern spaceSplit = Pattern.compile(" ");
    private final Pattern commaSplit = Pattern.compile(",");
    private final Pattern colonSplit = Pattern.compile(":");
    private final Pattern leadingWhiteSpace = Pattern.compile("^[ \t]+");


    /**
     * Parse everything belonging to a gene concept from file
     *
     * @param org      the organism as prefixed in KEGG eg.. hsa, mmu, ath
     * @param gene     kegg ID
     * @param isEnzyme Is this gene encoding an enzyme?
     * @param reader   The Reader attached to KEGG org specific gene file
     * @throws IOException
     */
    public void parseConceptFromFile(
            String org,
            String gene,
            boolean isEnzyme,
            BufferedReader reader) throws IOException {


        String id = (org + ':' + gene).toUpperCase();

        List<Concept> conceptCache = new ArrayList<Concept>();

        //Gene as a concept
        Concept concept_gene = new Concept(id + "_GE", MetaData.CV_KEGG, MetaData.CC_GENE);

        ConceptName cn = new ConceptName(concept_gene.getId(), gene);
        cn.setPreferred(true);
        concept_gene.getConceptNames().add(cn);

        String link = "http://www.genome.jp/dbget-bin/www_bget?" + org.toLowerCase() + "+" + gene.toLowerCase();
        concept_gene.setUrl(link);

        Sequence ntseq = new Sequence(id + "_" + MetaData.ATTR_NAME_NUCLEIC_ACID);
        ntseq.setConcept_fk(concept_gene.getId());
        ntseq.setSequence_type_fk(MetaData.ATTR_NAME_NUCLEIC_ACID);

        //Protein derive from gene
        Concept concept_protein = new Concept(id + "_PR", MetaData.CV_KEGG, MetaData.CC_PROTEIN);
        concept_protein.setDescription("derived protein");

        cn = new ConceptName(concept_protein.getId(), gene);
        cn.setPreferred(true);
        concept_protein.getConceptNames().add(cn);

        Sequence aaseq = new Sequence(id + "_" + MetaData.ATTR_NAME_AMINO_ACID);
        aaseq.setConcept_fk(concept_protein.getId());
        aaseq.setSequence_type_fk(MetaData.ATTR_NAME_AMINO_ACID);

        //Relation between Gene and Protein
        Relation en_by = new Relation(concept_protein.getId(), concept_gene.getId(), MetaData.RT_ENCODED_BY);
        en_by.setFrom_element_of(MetaData.CV_KEGG);
        en_by.setTo_element_of(MetaData.CV_KEGG);
        if (references.containsKey(id)) {
            for (String context : references.get(id))
                en_by.addContext(context);
        } else {
            System.out.println(id + " not found in referenced list.");
        }

        //Enzyme derive from protein
        Concept concept_enzyme = new Concept(id + "_EN", MetaData.CV_KEGG, MetaData.CC_ENZYME);
        concept_enzyme.setDescription("derived enzyme");

        //Relation between Enzyme and Protein
        Relation is_a = new Relation(concept_enzyme.getId(), concept_protein.getId(), MetaData.RT_IS_A);
        is_a.setFrom_element_of(MetaData.CV_KEGG);
        is_a.setTo_element_of(MetaData.CV_KEGG);
        if (references.containsKey(id)) {
            for (String context : references.get(id))
                is_a.addContext(context);
        }

        //same state variables to see, where we are
        boolean inDefinition = false;
        boolean inDblinks = false;
        boolean inAAseq = false;
        boolean inNTseq = false;

        boolean finished = false;

        Set<String> ecTerms = new HashSet<String>();

        while (reader.ready()) {
            String line = reader.readLine();

            //this ends an entry
            if (line.indexOf("///") > -1 || finished) {

                //gene is standard entry
                Parser.getUtil().writeConcept(concept_gene);

                //if there is an aaseq, its a protein
                if (aaSeq.length() > 0) {
                    aaseq.setSeq(aaSeq.toString());
                    aaSeq.setLength(0);
                    conceptCache.add(concept_protein);
                    relationsCache.add(en_by);
                    sequenceCache.add(aaseq);
                }

                if (naSeq.length() > 0) {
                    ntseq.setSeq(naSeq.toString());
                    naSeq.setLength(0);
                    sequenceCache.add(ntseq);
                }

                //if its an enzyme it has to be also a protein
                if (isEnzyme) {
                    if (aaseq.getSeq() == null) {
                        conceptCache.add(concept_protein);
                        relationsCache.add(en_by);
                    }
                    conceptCache.add(concept_enzyme);
                    relationsCache.add(is_a);

                    //if there is an ec number given in definition use it as cat_c
                    String description = concept_gene.getDescription().toUpperCase();

                    parseECFromString(description, concept_enzyme.getId(), conceptCache, relationsCache);

                    for (String term : ecTerms) {
                        parseECFromString(term, concept_enzyme.getId(), conceptCache, relationsCache);
                    }
                }
                Parser.getUtil().writeConcepts(conceptCache);
                return;
            } else if (line.length() > 0) {

                int oldLineSize = line.length();
                line = leadingWhiteSpace.matcher(line).replaceFirst("");

                if (oldLineSize == line.length() || line.length() == 0) {
                    inDefinition = false;
                    inDblinks = false;
                    inAAseq = false;
                    inNTseq = false;
                }

                //parse multiline definition as description
                if (inDefinition || line.startsWith(DEFINITION)) {
                    inDefinition = true;

                    if (concept_gene.getDescription() == null) {
                        int indexof = line.indexOf(DEFINITION);
                        concept_gene.setDescription(line.substring(indexof + DEFINITION.length()).trim());
                    } else {
                        concept_gene.setDescription(concept_gene.getDescription() + " " + line.trim());

                        List<String> names = getNames(concept_gene.getDescription());
                        Iterator<String> nameIt = names.iterator();
                        while (nameIt.hasNext()) {
                            String name = nameIt.next();
                            if (name.indexOf("EC:") > -1) {
                                ecTerms.add(name);
                                continue;
                            }
                            cn = new ConceptName(concept_gene.getId(), name);
                            concept_gene.getConceptNames().add(cn);
                            cn = new ConceptName(concept_protein.getId(), name);
                            concept_protein.getConceptNames().add(cn);
                        }
                    }
                } else if (line.startsWith(ORTHOLOGY)) {
                    int indexof = line.indexOf(ORTHOLOGY);
                    line = line.substring(indexof + ORTHOLOGY.length()).trim();
                    int ec = line.indexOf("[EC:");
                    if (ec > -1) {
                        ecTerms.add(line.substring(ec));
                    }

                    //parse name line into several concept names
                } else if (line.startsWith(NAME)) {
                    int indexof = line.indexOf(NAME);
                    String[] result = commaSplit.split(line.substring(indexof + NAME.length()).trim());
                    for (int i = 0; i < result.length; i++) {
                        String name = result[i].trim();

                        if (name.length() == 0) {
                            continue;
                        }

                        //concept name for gene
                        ConceptName conceptNameGE = new ConceptName(
                                concept_gene.getId(), name);
                        if (concept_gene.getConceptNames() == null || concept_gene.getConceptNames().size() == 0) {
                            //the first name is preferred
                            conceptNameGE.setPreferred(true);
                        }
                        concept_gene.getConceptNames().add(conceptNameGE);

                        //concept name for protein
                        ConceptName conceptNamePR = new ConceptName(
                                concept_protein.getId(), name);
                        if (concept_protein.getConceptNames() == null || concept_gene.getConceptNames().size() == 0) {
                            //the first name is preferred
                            conceptNamePR.setPreferred(true);
                        }
                        concept_protein.getConceptNames().add(conceptNamePR);

                        //concept name for enzyme
                        ConceptName conceptNameEN = new ConceptName(
                                concept_enzyme.getId(), name);
                        if (concept_enzyme.getConceptNames() == null || concept_gene.getConceptNames().size() == 0) {
                            //the first name is preferred
                            conceptNameEN.setPreferred(true);
                        }
                        concept_enzyme.getConceptNames().add(conceptNameEN);
                    }
                }

                //parse multiline dblinks into concept accs
                else if (inDblinks || line.startsWith(DBLINKS)) {
                    inDblinks = true;
                    int indexof = line.indexOf(DBLINKS);
                    if (indexof > -1) {
                        line = line.substring(indexof + DBLINKS.length());
                    }
                    String[] result = colonSplit.split(line.trim());
                    if (result.length == 2) {
                        //first comes db, then list of accs
                        String db = result[0];
                        String acc = spaceSplit.matcher(result[1]).replaceAll("").trim();

                        if (db.length() == 0) {
                            System.err.println("Unknown DB :" + line);
                        }

                        String[] accs = spaceSplit.split(acc);
                        for (String accSplit : accs) {

                            //concept acc for gene
                            ConceptAcc conceptAccGE = new ConceptAcc(
                                    concept_gene.getId(), accSplit, db);

                            concept_gene.getConceptAccs().add(conceptAccGE);

                            //concept acc for protein
                            ConceptAcc conceptAccPR = new ConceptAcc(
                                    concept_protein.getId(), accSplit, db);

                            concept_protein.getConceptAccs().add(conceptAccPR);

                            //concept acc for enzyme
                            ConceptAcc conceptAccEN = new ConceptAcc(
                                    concept_enzyme.getId(), accSplit, db);

                            concept_enzyme.getConceptAccs().add(conceptAccEN);
                        }
                    } else {
                        System.err.println("Unknown accession format: " + line);
                    }
                } else if (line.startsWith(AASEQ)) {
                    inAAseq = true;
                    aaSeq.setLength(0);
                } else if (line.startsWith(NTSEQ)) {
                    inNTseq = true;
                    naSeq.setLength(0);
                } else if (inAAseq) {
                    aaSeq.append(line);
                } else if (inNTseq) {
                    naSeq.append(line);
                }

            } else {
                //if we have an empty line after aa and na are filled
                if (aaSeq.length() > 0 && naSeq.length() > 0) {
                    finished = true;
                }
            }
        }
        Parser.getUtil().writeConcepts(conceptCache);
    }

    /**
     * Parses EC terms for String annotation and annotates them to enzyme
     *
     * @param annotation
     * @param enzyme_id
     * @param conceptCache
     * @param relationsCache2
     */
    private void parseECFromString(String annotation, String enzyme_id, List<Concept> conceptCache, DPLPersistantSet<Relation> relationsCache2) {

        int ecIndex = annotation.indexOf("[EC:");

        if (ecIndex > -1) {
            annotation = annotation.substring(ecIndex + 4);

            int endIndex = annotation.indexOf("]");
            if (endIndex > 0) {
                annotation = annotation.substring(0, endIndex);
            }
            String[] result = spaceSplit.split(annotation);
            for (String ecID : result) {
                //new ec concept if not already exists

                if (!Parser.getConceptWriter().conceptParserIDIsWritten(ecID.toUpperCase())) {
                    Concept ec = new Concept(ecID, MetaData.CV_KEGG, MetaData.CC_EC);
                    ec.setDescription("parsed ec from gene annotation");
                    ConceptName cn = new ConceptName(ec.getId(), ec.getId());
                    if (ec.getConceptNames() == null || ec.getConceptNames().size() == 0) {
                        cn.setPreferred(true);
                    }
                    ec.getConceptNames().add(cn);
                    ec.getConceptAccs().add(new ConceptAcc(ec.getId(), ec.getId(), MetaData.CV_EC));
                    conceptCache.add(ec);
                }
                Relation cat_c = new Relation(enzyme_id, ecID, MetaData.RT_CATALYSEING_CLASS);
                cat_c.setFrom_element_of(MetaData.CV_KEGG);
                cat_c.setTo_element_of(MetaData.CV_KEGG);
                String id = enzyme_id.substring(0, enzyme_id.length() - 3);
                if (references.containsKey(id)) {
                    for (String context : references.get(id))
                        cat_c.addContext(context);
                } else {
                    System.out.println(id + " not found in referenced list.");
                }
                relationsCache2.add(cat_c);
            }
        }
    }

    public Map<String, Boolean> getMissingGenes() {
        return missingGenes;
    }

    public String getOrg() {
        return org.toLowerCase();
    }

    public void setOrg(String org) {
        this.org = org.toUpperCase();
    }

    private static ArrayList<String> exclusionEndingList;
    private static ArrayList<String> exclusionList;
    private static ArrayList<String> emptyReturn = new ArrayList<String>(0);

    /**
     * @param description the string to split into names
     * @return the names found
     */
    protected static List<String> getNames(String description) {

        if (exclusionList == null) {
            exclusionList = new ArrayList<String>(1);
            exclusionList.add("transporter".toUpperCase());
            exclusionList.add("catalytic".toUpperCase());
        }

        if (description.contains("unknown protein") || description.contains("hypothetical protein")) {
            return emptyReturn;
        }
        description = description.replace("family protein", "");
        description = description.replace("putative", "");
        description = description.replaceAll("[,]", "");

        ArrayList<String> names = new ArrayList<String>();

        int ecIndex = description.indexOf("[EC:");

        if (ecIndex > -1)
            description = description.substring(0, ecIndex);

        int startOpen = description.indexOf('(');
        if (startOpen > -1) {
            int endOpen = description.indexOf(')');
            if (endOpen > -1 && startOpen < endOpen) {
                String name = description.substring(startOpen + 1, endOpen).trim();
                description = description.replace(description.substring(startOpen, endOpen + 1), "");
                if (name.length() > 0
                        && checkEndsConditions(name)
                        && !exclusionList.contains(name.toUpperCase())
                        && !name.contains("transferring")
                        && !name.contains("binding"))
                    names.add(name);
            }
        }

        String[] moreNames = description.split("[;/]");
        for (String name : moreNames) {
            name = name.trim();
            if (name.length() > 0
                    && checkEndsConditions(name)
                    && !exclusionList.contains(name.toUpperCase())
                    && !name.contains("transferring")
                    && !name.contains("binding"))
                names.add(name);
        }

        return names;
    }

    public static boolean checkEndsConditions(String name) {

        if (exclusionEndingList == null) {
            exclusionEndingList = new ArrayList<String>(1);
            exclusionEndingList.add("ase");
            exclusionEndingList.add("compounds");
            exclusionEndingList.add("enzyme");
            exclusionEndingList.add("carrier");
            exclusionEndingList.add("substances");
            exclusionEndingList.add("ase");
        }

        Iterator<String> endIt = exclusionEndingList.iterator();
        while (endIt.hasNext()) {
            String end = endIt.next();
            if (name.endsWith(end)) {
				return false;
			}
		}
		return true;
	}

	
}
