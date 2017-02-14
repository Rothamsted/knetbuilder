/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.ko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.parser.kegg53.MetaData;
import net.sourceforge.ondex.parser.kegg53.Parser;
import net.sourceforge.ondex.parser.kegg53.data.Entry;
import net.sourceforge.ondex.parser.kegg53.data.Pathway;
import net.sourceforge.ondex.parser.kegg53.sink.Concept;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptAcc;
import net.sourceforge.ondex.parser.kegg53.sink.ConceptName;
import net.sourceforge.ondex.parser.kegg53.sink.Relation;
import net.sourceforge.ondex.parser.kegg53.util.DPLPersistantSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;


/**
 * @author taubertj
 */ 
public class KoPathwayMerger {

    private DPLPersistantSet<Pathway> pathways;
    private Map<Concept, Set<String>> koConceptToGenes;

    private Map<String, Set<Entry>> gene2KoEntries;
    private Map<String, Set<String>> ko2Genes;
    private DPLPersistantSet<Relation> relationsCache;
    private HashMap<String, Concept> koNamesToKoConcept;
    private HashMap<String, Concept> koAccessionToKoConcept;

    public KoPathwayMerger(DPLPersistantSet<Pathway> pathways,
                           Map<Concept, Set<String>> koConceptToGenes,
                           HashMap<String, Concept> koNamesToKoConcept,
                           HashMap<String, Concept> koAccessionToKoConcept,
                           DPLPersistantSet<Relation> relationsCache) {
        this.pathways = pathways;
        this.koConceptToGenes = koConceptToGenes;
        this.koNamesToKoConcept = koNamesToKoConcept;
        this.koAccessionToKoConcept = koAccessionToKoConcept;
        this.relationsCache = relationsCache;
    }

    /**
     * @param organisms list of kegg organisms to parse
     */
    public void merge(Set<String> organisms) throws MetaDataMissingException {

        final Pattern spaceSplit = Pattern.compile(" ");

        gene2KoEntries = new HashMap<String, Set<Entry>>(500);
        ko2Genes = new HashMap<String, Set<String>>(500);

        ArrayList<String> writtenIds = new ArrayList<String>(10000);

        EntityCursor<Pathway> cursor = pathways.getCursor();
        for (Pathway pathway : cursor) {
            //go through all entries and find ko concepts
            for (Entry entry : pathway.getEntries().values()) {
                if (entry.getType().equalsIgnoreCase("ortholog")) {
                    String[] result = spaceSplit.split(entry.getName());
                    for (String res : result) {

                        res = res.toUpperCase();
                        String koEqu = res;
                        if (res.startsWith("KO:")) {
                            koEqu = res.substring(3, res.length()).toUpperCase();
                        }

                        Concept concept_gene = koAccessionToKoConcept.get(koEqu);
                        if (concept_gene == null) {
                            concept_gene = koNamesToKoConcept.get(koEqu);
                        }

                        //get genes for ko concept
                        Set<String> genes = null;
                        if (concept_gene != null) {
                            genes = koConceptToGenes.get(concept_gene);
                        } else if (koEqu.length() > 0) {
                            //try to search in all ko concept names
                            concept_gene = searchFor(koEqu);
                        }

                        boolean koIsEnzyme = false;
                        if (concept_gene != null) {
                            for (ConceptAcc accession : concept_gene.getConceptAccs()) {
                                if (accession.getElement_of().equalsIgnoreCase("RN")) {
                                    koIsEnzyme = true;
                                    break;
                                }
                            }
                        }

                        //if not found, construct abstract concept
                        if (concept_gene == null && res.length() > 0) {
                            concept_gene = new Concept(res, MetaData.CV_KEGG, MetaData.CC_KEGG_GENE_ORTHOLOG_GROUPS);
                            concept_gene.setDescription("abstract ko concept");
                        } else if (concept_gene == null) {
                            continue;
                        }

                        //if it has an org prefix it is from a gene therfore: add ko in front
                        if (res.indexOf(":") != -1) {
                            String org = res.substring(0, res.indexOf(":"));
                            if (organisms.contains(org)) {
                                concept_gene.setId("KO:" + concept_gene.getId());
                            }
                        }
                        //if there is a link available
                        if (entry.getLink() != null)
                            concept_gene.setUrl(entry.getLink());

                        //concept is a gene
                        String id = concept_gene.getId();
                        if (concept_gene.getId().indexOf("_GE") == -1) {
                            id = concept_gene.getId() + "_GE";
                        }
                        concept_gene = concept_gene.clone(id, MetaData.CV_KEGG, MetaData.CC_KEGG_GENE_ORTHOLOG_GROUPS);

                        //let entry know about its concepts
                        entry.getConceptIDs().add(concept_gene.getId());
                        if (!writtenIds.contains(concept_gene.getId())) {
                            writtenIds.add(concept_gene.getId());
                            Parser.getUtil().writeConcept(concept_gene, false);
                        }

                        Concept concept_protein = makeConceptProtein(concept_gene);
                        entry.getConceptIDs().add(concept_protein.getId());

                        if (!writtenIds.contains(concept_protein.getId())) {
                            writtenIds.add(concept_protein.getId());
                            Parser.getUtil().writeConcept(concept_protein, false);
                        }

                        Relation en_by = new Relation(
                                concept_protein.getId(),
                                concept_gene.getId(),
                                MetaData.RT_ENCODED_BY);
                        en_by.setFrom_element_of(MetaData.CV_KEGG);
                        en_by.setTo_element_of(MetaData.CV_KEGG);

                        if (concept_protein.getId() == null || concept_gene.getId() == null) {
                            throw new NullPointerException("Value is null for en_by relationship " + concept_protein.getId() + " en_by " + concept_gene.getId());
                        }

                        relationsCache.add(en_by);

                        // look for EC numbers indicating an enzyme
                        boolean foundEC = false;
                        if (concept_gene.getConceptAccs() != null) {
                            for (ConceptAcc ca : concept_gene.getConceptAccs()) {
                                if (ca.getElement_of().equalsIgnoreCase(MetaData.CV_EC)) {
                                    foundEC = true;
                                    break;
                                }
                            }
                        }

                        //if theres a reaction it is also an enzyme
                        if (foundEC || entry.getReaction() != null || koIsEnzyme) {
                            Concept concept_enzyme = makeConceptEnzyme(concept_gene);

                            if (!writtenIds.contains(concept_enzyme.getId())) {
                                writtenIds.add(concept_enzyme.getId());
                                Parser.getUtil().writeConcept(concept_enzyme, false);
                            }

                            entry.getConceptIDs().add(concept_enzyme.getId());

                            Relation is_a = new Relation(
                                    concept_enzyme.getId(),
                                    concept_protein.getId(),
                                    MetaData.RT_IS_A);
                            is_a.setFrom_element_of(MetaData.CV_KEGG);
                            is_a.setTo_element_of(MetaData.CV_KEGG);

                            if (concept_protein.getId() == null || concept_gene.getId() == null) {
                                throw new NullPointerException("Value is null for is_a relationship " + concept_protein.getId() + " is_a " + concept_gene.getId());
                            }
                            relationsCache.add(is_a);
                        }

                        //if there are genes for this ko concept
                        if (genes != null) {

                            //put all genes into ko2Genes mapping
                            ko2Genes.put(concept_gene.getId().toUpperCase(), genes);

                            for (String gene : genes) {
                                Set<Entry> entries = gene2KoEntries.get(gene);
                                if (!gene2KoEntries.containsKey(gene)) {
                                    entries = new HashSet<Entry>();
                                    gene2KoEntries.put(gene, entries);
                                }

                                //mapping geneId to set of ko entries
                                entries.add(entry);
                            }
                        }
                    }
                }
            }
            try {
                cursor.update(pathway);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        pathways.closeCursor(cursor);

        Parser.getUtil().writeRelations(relationsCache);
    }

    public Map<String, Set<Entry>> getGene2KoEntries() {
        return gene2KoEntries;
    }

    public Map<String, Set<String>> getKo2Genes() {
        return ko2Genes;
    }

    private final Pattern colonSplit = Pattern.compile(":");

    /**
     * searches for name in all concept names of all concepts
     *
     * @param name
     * @return
     */
    private Concept searchFor(String name) {
        String search = name;
        String[] result = colonSplit.split(name);
        if (result.length > 1) {
            search = result[1];
        }

        //go through all ko concepts
        for (Concept concept : koAccessionToKoConcept.values()) {
            if (concept.getConceptNames() != null) {
                //go through all concept names
                for (ConceptName conceptName : concept.getConceptNames()) {
                    if (conceptName.getName().equals(search)) {
                        //clone ko concept with new id
                        return concept.clone(name + "_GE", MetaData.CV_KEGG, MetaData.CC_KEGG_GENE_ORTHOLOG_GROUPS);
                    }
                }
            }
        }
        return null;
    }

    private static Concept makeConceptProtein(Concept concept_gene) {
        String id = concept_gene.getId();
        id = id.substring(0, id.length() - 3) + "_PR";
        Concept concept_protein = concept_gene.clone(id, MetaData.CV_KEGG, MetaData.CC_KEGG_PROTEIN_ORTHOLOG_GROUPS);
        return concept_protein;
    }

    private static Concept makeConceptEnzyme(Concept concept_gene) {
        String id = concept_gene.getId();
        id = id.substring(0, id.length() - 3) + "_EN";
        Concept concept_enzyme = concept_gene.clone(id, MetaData.CV_KEGG, MetaData.CC_KEGG_ENZYME_ORTHOLOG_GROUPS);
        return concept_enzyme;
    }

}
