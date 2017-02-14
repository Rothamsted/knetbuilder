/*
 * Created on 13-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.gene;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.parser.kegg52.MetaData;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.sink.Concept;
import net.sourceforge.ondex.parser.kegg52.sink.Relation;
import net.sourceforge.ondex.parser.kegg52.sink.Sequence;
import net.sourceforge.ondex.parser.kegg52.util.DPLPersistantSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;


/**
 * @author taubertj
 */
public class GeneFilesParser {

    private final static boolean DEBUG = false;

    private String pathToGenes;
    private Map<String, Set<Entry>> gene2Entries; //no need to be synchronized
    private Map<String, String> mapping; //no need to be synchronized

    private DPLPersistantSet<Sequence> sequenceCache;
    private DPLPersistantSet<Relation> relationsCache;
    private DPLPersistantSet<Pathway> pathwayCache;

    private Set<String> orgs;

    /**
     * @param orgs           this determines which species all the genes will be parsed from if this is turned on
     * @param pathToGenes
     * @param gene2Entries
     * @param mapping
     * @param sequenceCache
     * @param relationsCache
     */
    public GeneFilesParser(Set<String> orgs,
                           String pathToGenes,
                           Map<String, Set<Entry>> gene2Entries,
                           Map<String, String> mapping,
                           DPLPersistantSet<Sequence> sequenceCache,
                           DPLPersistantSet<Relation> relationsCache,
                           DPLPersistantSet<Pathway> pathwayCache) {
        this.orgs = orgs;
        this.pathToGenes = pathToGenes;
        this.gene2Entries = gene2Entries;
        this.mapping = mapping; //not synchronized
        this.sequenceCache = sequenceCache;
        this.relationsCache = relationsCache;
        this.pathwayCache = pathwayCache;
    }

    private Map<String, Set<String>> geneReferences() throws DatabaseException {
        final Pattern colonSplit = Pattern.compile(":");
        final Pattern spaceSplit = Pattern.compile(" ");

        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        EntityCursor<Pathway> cursor = pathwayCache.getCursor();
        Iterator<Pathway> it = cursor.iterator();
        while (it.hasNext()) {
            Pathway pathway = it.next();
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equals("gene")) {
                    String[] results = spaceSplit.split(entry.getName().toUpperCase());
                    String lastOrg = null;
                    for (String result : results) {
                        String[] parts = colonSplit.split(result);
                        if (parts.length == 2) {
                            String id = result;
                            lastOrg = parts[0];
                            if (!map.containsKey(id.toUpperCase()))
                                map.put(id.toUpperCase(), new HashSet<String>());
                            map.get(id.toUpperCase()).add(pathway.getId());
                        } else {
                            String id = lastOrg + ":" + result;
                            if (!map.containsKey(id.toUpperCase()))
                                map.put(id.toUpperCase(), new HashSet<String>());
                            map.get(id.toUpperCase()).add(pathway.getId());
                        }
                    }
                }
            }
        }
        cursor.close();
        return map;
    }


    /**
     * checks consitancy of each given gene name
     */
    public void parseAndWrite() {

        Map<String, Map<String, Boolean>> genesToParse = new HashMap<String, Map<String, Boolean>>();
        Map<String, Map<String, Boolean>> missingGenes = new HashMap<String, Map<String, Boolean>>();

        Pattern colonSplit = Pattern.compile(":");

        Iterator<String> itGs = gene2Entries.keySet().iterator();
        while (itGs.hasNext()) {
            String gene = itGs.next();
            Set<Entry> entries = gene2Entries.get(gene);
            //go through all entries and check for enzymes probability

            Iterator<Entry> itEs = entries.iterator();
            boolean isEnzyme = false;
            while (itEs.hasNext()) {
                Entry entry = itEs.next();
                if (entry.getReaction() != null) {
                    isEnzyme = true;
                    break;
                }
            }

            //check if there is a mapping for org to filename
            String[] parts = colonSplit.split(gene);

            String org = parts[0].toLowerCase();
            String newGene = parts[1];
            if (mapping.get(org) == null) {
                if (DEBUG) System.out.println(org + ":" + newGene);
                Map<String, Boolean> genes = missingGenes.get(org);
                if (genes == null) {
                    genes = new HashMap<String, Boolean>(10000);
                    missingGenes.put(org, genes);
                }
                genes.put(newGene.toUpperCase(), isEnzyme);
            } else {
                Map<String, Boolean> genes = genesToParse.get(org);
                if (genes == null) {
                    genes = new HashMap<String, Boolean>(10000);
                    genesToParse.put(org, genes);
                }
                genes.put(newGene.toUpperCase(), isEnzyme);
            }
        }
        gene2Entries = null;
        parseGeneFiles(genesToParse, missingGenes);
    }


    /**
     * parses the gene flat files
     *
     * @param sequenceCache
     */
    public void parseGeneFiles(Map<String, Map<String, Boolean>> genesToParse,
                               Map<String, Map<String, Boolean>> missingGenes
    ) {

        Map<String, Set<String>> references = new HashMap<String, Set<String>>();
        try {
            references = geneReferences();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        int geneCount = 0;

        Iterator<Map<String, Boolean>> values = genesToParse.values().iterator();
        while (values.hasNext()) {
            Map<String, Boolean> value = values.next();
            geneCount = geneCount + value.size();
        }

        System.out.println("Orgs to Parse = " + genesToParse.size());
        System.out.println("Total Genes to Parse = " + geneCount);

        ArrayList<ParseGeneFile> parsers = new ArrayList<ParseGeneFile>();
        boolean allSequences = Parser.isImportAllSequences();

        ArrayList<String> orgsWithGenes = new ArrayList<String>(genesToParse.keySet());
        Collections.sort(orgsWithGenes);

        Iterator<String> itGenes = orgsWithGenes.iterator();
        while (itGenes.hasNext()) {
            String org = itGenes.next();

            if (!Parser.parseOrthologfillers && !Parser.orgs.contains(org.toLowerCase())) {
                continue; //skip this its an ortholog filler
            }

            Map<String, Boolean> genes = genesToParse.get(org);

            Set<String> nonEnzymeGene = new HashSet<String>();
            Set<String> enzymeGenes = new HashSet<String>();

            Iterator<String> geneIt = genes.keySet().iterator();
            while (geneIt.hasNext()) {
                String gene = geneIt.next();
                if (genes.get(gene)) enzymeGenes.add(gene.toUpperCase());
                else nonEnzymeGene.add(gene.toUpperCase());
            }

            System.out.println("Parsing genes for " + org + " EZ:" + enzymeGenes.size() + " NE:" + nonEnzymeGene.size() + " Parse All:" + (allSequences && orgs.contains(org.toLowerCase())));

            //used unziped genes where possible but use original .tar.gz if not
            File file = new File(pathToGenes + File.separator + mapping.get(org.toLowerCase()));
            if (!file.exists()) {
                file = new File(new File(pathToGenes).getParent() + File.separator + "genes.tar.gz");
            }

            ParseGeneFile fileParser = new ParseGeneFile(
                    nonEnzymeGene,
                    enzymeGenes,
                    org,
                    mapping.get(org.toLowerCase()),
                    file.getAbsolutePath(),
                    (allSequences && orgs.contains(org.toLowerCase())),
                    sequenceCache,
                    relationsCache,
                    references);
            parsers.add(fileParser);
            fileParser.run();
        }

        //now get results!! determin written genes
        Iterator<ParseGeneFile> parserJobsIt = parsers.iterator();
        while (parserJobsIt.hasNext()) {
            ParseGeneFile geneParser = parserJobsIt.next();

            if (missingGenes.get(geneParser.getOrg().toLowerCase()) == null) {
                missingGenes.put(geneParser.getOrg().toLowerCase(), new HashMap<String, Boolean>());
            }

            missingGenes.get(geneParser.getOrg().toLowerCase()).putAll(geneParser.getMissingGenes());
            parserJobsIt.remove();
        }

        if (Parser.DEBUG) System.out.println("Finished Gene Parse waiting on Concepts to write");

        if (Parser.DEBUG)
            System.out.println("finished processing gene files proceding to write " + missingGenes.size() + " missing genes");

        //insert abstract concepts for clashed genes
        Iterator<String> itClashGenes = missingGenes.keySet().iterator();
        while (itClashGenes.hasNext()) {
            String org = itClashGenes.next();
            Map<String, Boolean> genes = missingGenes.get(org);
            Iterator<String> itGenesForId = genes.keySet().iterator();
            while (itGenesForId.hasNext()) {
                //reconstruct full gene id
                String gene = itGenesForId.next();
                String id = (org + ":" + gene).toUpperCase();
                if (DEBUG) System.out.println(id);

                //create abstract concept
                Concept concept_gene = new Concept(id + "_GE", MetaData.CV_KEGG, MetaData.CC_GENE);
                concept_gene.setDescription("abstract gene concept for missing organism");
                Parser.getUtil().writeConcept(concept_gene);

                //build chain up towards enzyme
                if (genes.get(gene)) {

                    //Protein derive from gene
                    Concept concept_protein = new Concept(id + "_PR", MetaData.CV_KEGG, MetaData.CC_PROTEIN);
                    concept_protein.setDescription("derived protein");
                    Parser.getUtil().writeConcept(concept_protein);

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
                    relationsCache.add(en_by);

                    //Enzyme derive from protein
                    Concept concept_enzyme = new Concept(id + "_EN", MetaData.CV_KEGG, MetaData.CC_ENZYME);
                    concept_enzyme.setDescription("derived enzyme");
                    Parser.getUtil().writeConcept(concept_enzyme);

                    //Relation between Enzyme and Protein
                    Relation is_a = new Relation(concept_enzyme.getId(), concept_protein.getId(), MetaData.RT_IS_A);
                    is_a.setFrom_element_of(MetaData.CV_KEGG);
                    is_a.setTo_element_of(MetaData.CV_KEGG);
                    if (references.containsKey(id)) {
                        for (String context : references.get(id))
                            is_a.addContext(context);
                    } else {
                        System.out.println(id + " not found in referenced list.");
                    }
                    relationsCache.add(is_a);
                }
            }
        }
        Parser.getUtil().writeRelations(relationsCache);
        Parser.getUtil().writeSequences(sequenceCache);
	}
}
