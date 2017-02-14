/*
 * Created on 13-May-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.gene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.FileIndex;
import net.sourceforge.ondex.parser.kegg53.GenomeParser;
import net.sourceforge.ondex.parser.kegg53.MetaData;
import net.sourceforge.ondex.parser.kegg53.Parser;
import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.sink.Concept;
import net.sourceforge.ondex.parser.kegg53.sink.Relation;
import net.sourceforge.ondex.parser.kegg53.sink.Sequence;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

import com.sleepycat.persist.EntityCursor;

 
/**
 * @author taubertj
 */
public class GeneFilesParser {

    private final static boolean DEBUG = false;

    private final FileIndex geneResource;

    private final Map<String, Set<Entry>> gene2Entries;

    private final DPLPersistantSet<Sequence> sequenceCache;
    private final DPLPersistantSet<Relation> relationsCache;
    private final DPLPersistantSet<Pathway> pathwayCache;


    /**
     * @param geneResource
     * @param gene2Entries
     * @param sequenceCache
     * @param relationsCache
     */
    public GeneFilesParser(FileIndex geneResource,
                           Map<String, Set<Entry>> gene2Entries,
                           DPLPersistantSet<Sequence> sequenceCache,
                           DPLPersistantSet<Relation> relationsCache,
                           DPLPersistantSet<Pathway> pathwayCache) {
        this.geneResource = geneResource;
        this.gene2Entries = gene2Entries;
        this.sequenceCache = sequenceCache;
        this.relationsCache = relationsCache;
        this.pathwayCache = pathwayCache;
    }

    /**
     * scans pathways and constructs a genes to gene references map
     *
     * @return genes to references map
     */
    private Map<String, Set<String>> geneReferences() {
        final Pattern colonSplit = Pattern.compile(":");
        final Pattern spaceSplit = Pattern.compile(" ");

        //map of existing organisms and genes mentioned in pathways
        Map<String, Set<String>> genesToReferencesMap = new HashMap<String, Set<String>>();

        System.out.println("scanning " + pathwayCache.size() + " pathways for references: ");
        EntityCursor<Pathway> cursor = pathwayCache.getCursor();
        for (Pathway pathway : cursor) {
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equalsIgnoreCase("gene")) {
                    String[] results = spaceSplit.split(entry.getName().toUpperCase());
                    String lastOrg = null;
                    for (String result : results) {
                        String[] parts = colonSplit.split(result.trim());

                        String id = result.trim().toUpperCase();
                        if (parts.length == 2) {
                            lastOrg = parts[0].trim().toUpperCase();
                        } else {
                            id = (lastOrg + ":" + result.trim()).toUpperCase();
                        }

                        Set<String> references = genesToReferencesMap.get(id);
                        if (references == null) {
                            references = new HashSet<String>();
                            genesToReferencesMap.put(id, references);
                        }
                        genesToReferencesMap.get(id).add(pathway.getId());
                    }
                }
            }
        }
        cursor.close();
        return genesToReferencesMap;
    }


    /**
     * @param organisms    list of kegg organism ids to parse
     * @param orthologOrgs species to parser orthologs for
     */
    public void parseAndWrite(
            Set<GenomeParser.Taxonomony> organisms,
            Set<GenomeParser.Taxonomony> orthologOrgs,
            GenomeParser genomeParser) throws MetaDataMissingException, IOException {

        Map<GenomeParser.Taxonomony, Map<String, Boolean>> genesFromTargetOrganisms
                = new HashMap<GenomeParser.Taxonomony, Map<String, Boolean>>(organisms.size());
        Map<GenomeParser.Taxonomony, Map<String, Boolean>> genesFromOtherOrganisms
                = new HashMap<GenomeParser.Taxonomony, Map<String, Boolean>>();

        Pattern colonSplit = Pattern.compile(":");

        //catagorize gene entries as target org or other_org and if enzymes
        for (String gene : gene2Entries.keySet()) {
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
            if (parts.length != 2) {
                System.err.println("Warning unusual gene name format \"" + gene + "\"");
            }

            String org = parts[0].toLowerCase();
            String newGene = parts[1].trim().toUpperCase();

            GenomeParser.Taxonomony taxonomony = genomeParser.getTaxonomony(org);
            if (taxonomony == null) {
                throw new RuntimeException("Unknown species id " + org + " in gene " + gene);
            }

            if (!(organisms.contains(taxonomony) || orthologOrgs.contains(taxonomony))) {
                if (DEBUG) System.out.println(org + ":" + newGene);
                Map<String, Boolean> genes = genesFromOtherOrganisms.get(taxonomony);
                if (genes == null) {
                    genes = new HashMap<String, Boolean>(10000);
                    genesFromOtherOrganisms.put(taxonomony, genes);
                }
                genes.put(newGene, isEnzyme);
            } else {
                Map<String, Boolean> genes = genesFromTargetOrganisms.get(taxonomony);
                if (genes == null) {
                    genes = new HashMap<String, Boolean>(10000);
                    genesFromTargetOrganisms.put(taxonomony, genes);
                }
                genes.put(newGene, isEnzyme);
            }
        }
        parseGeneFiles(genesFromTargetOrganisms, genesFromOtherOrganisms, organisms, orthologOrgs);
    }


    /**
     * @param genesFromTargetOrganisms
     * @param genesFromOtherOrganisms
     * @param organisms                list of kegg organism ids to parse
     * @param orthologOrgs             list of otholog species to parse
     */
    public void parseGeneFiles(Map<GenomeParser.Taxonomony, Map<String, Boolean>> genesFromTargetOrganisms,
                               Map<GenomeParser.Taxonomony, Map<String, Boolean>> genesFromOtherOrganisms,
                               Set<GenomeParser.Taxonomony> organisms,
                               Set<GenomeParser.Taxonomony> orthologOrgs) throws MetaDataMissingException, IOException {

        Map<String, Set<String>> references = geneReferences();

        int geneCount = 0;
        for (Map<String, Boolean> value : genesFromTargetOrganisms.values()) {
            geneCount = geneCount + value.size();
        }

        System.out.println("Target organisms to Parse = " + genesFromTargetOrganisms.size());
        System.out.println("Total Genes to Parse in target organism = " + geneCount);

        ArrayList<ParseGeneFile> parsers = new ArrayList<ParseGeneFile>();
        boolean allSequences4Species = Parser.isImportAllSequences4Species();

        List<GenomeParser.Taxonomony> orgsWithGenes
                = new ArrayList<GenomeParser.Taxonomony>(genesFromTargetOrganisms.keySet());
        Collections.sort(orgsWithGenes);

        for (GenomeParser.Taxonomony org : orgsWithGenes) {

            Map<String, Boolean> genes = genesFromTargetOrganisms.get(org);

            Set<String> nonEnzymeGene = new HashSet<String>();
            Set<String> enzymeGenes = new HashSet<String>();

            for (String gene : genes.keySet()) {
                if (genes.get(gene))
                    enzymeGenes.add(gene.toUpperCase());
                else
                    nonEnzymeGene.add(gene.toUpperCase());
            }

            System.out.println("Parsing genes for " + org.getScientificName() + " (" + org.getKeggId() + "/" + org.getTaxNumber() + ")" +
                    " ENZYMES:" + enzymeGenes.size() + " UNKNOWN TYPE GENES:" + nonEnzymeGene.size() + " Parse All?:" + (allSequences4Species && organisms.contains(org)));

            ParseGeneFile fileParser = new ParseGeneFile(
                    nonEnzymeGene,
                    enzymeGenes,
                    org,
                    geneResource.getFile(org.getScientificName().toLowerCase()),
                    (allSequences4Species && organisms.contains(org)),
                    sequenceCache,
                    relationsCache,
                    references);
            parsers.add(fileParser);
            fileParser.call();
        }

        //now get results!! determine written genes
        for (ParseGeneFile geneParser : parsers) {
            if (genesFromOtherOrganisms.get(geneParser.getOrganism()) == null) {
                genesFromOtherOrganisms.put(geneParser.getOrganism(), new HashMap<String, Boolean>());
            }
            genesFromOtherOrganisms.get(geneParser.getOrganism()).putAll(geneParser.getMissingGenes());
        }

        int othologGeneCount = 0;
        for (Map<String, Boolean> value : genesFromOtherOrganisms.values()) {
            othologGeneCount = othologGeneCount + value.size();
        }

        if (Parser.DEBUG) System.out.println("Finished Gene Parse waiting on Concepts to write");

        if (Parser.DEBUG)
            System.out.println("Finished processing gene files proceding to create enyzymes only for referenced genes from " + othologGeneCount + " ortholog genes from " + genesFromOtherOrganisms.size() + " other organisms");

        //insert abstract concepts for clashed genes
        for (GenomeParser.Taxonomony organism : genesFromOtherOrganisms.keySet()) {
            Map<String, Boolean> genes = genesFromOtherOrganisms.get(organism);
            for (String gene : genes.keySet()) {
                //reconstruct full gene id
                String id = (organism.getKeggId() + ':' + gene).toUpperCase();

                //either an explicit mention in the KGML or a request to parse that species is enough
                if (references.containsKey(id)) {

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
                            //orthologs no longer referenced in kgml
                            //System.out.println("External organism gene "+id + " not found in referenced list");
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
                            //orthologs no longer referenced in kgml
                            //System.out.println("External organism gene "+id + " not found in referenced list");
                        }
                        relationsCache.add(is_a);
                    }
                }
            }
        }
        Parser.getUtil().writeRelations(relationsCache);
        Parser.getUtil().writeSequences(sequenceCache);
    }
}
