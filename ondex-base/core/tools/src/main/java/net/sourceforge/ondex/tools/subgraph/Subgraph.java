package net.sourceforge.ondex.tools.subgraph;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.AbstractONDEXEntity;
import net.sourceforge.ondex.core.util.ArrayKey;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.functions.CategoryMapBuilder;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

public class Subgraph {
    private Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>();
    private Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
    private final ONDEXGraph graph;

    /**
     * Construct an empty subgraph in the ondex graph supplied
     *
     * @param graph - parent ondex graph
     */
    public Subgraph(ONDEXGraph graph) {
        this.graph = graph;
    }

    /**
     * Construct a new sub graph with the following concepts and relations
     *
     * @param concepts  - ids of concepts
     * @param relations - ids of realtions
     * @param graph     - parent Ondex graph
     */
    public Subgraph(Set<ONDEXConcept> concepts, Set<ONDEXRelation> relations, ONDEXGraph graph) {
        this(graph);
        this.concepts = concepts;
        this.relations = relations;
    }

    /**
     * Return an Ondex view of all the concepts in this subgraph
     *
     * @return
     */
    public Set<ONDEXConcept> getConcepts() {
        return Collections.unmodifiableSet(concepts);
    }

    /**
     * Return an Ondex view of all the relations in this subgraph
     *
     * @return
     */
    public Set<ONDEXRelation> getRelations() {
        return Collections.unmodifiableSet(relations);
    }

    /**
     * Add a concept to this subgraph
     *
     * @param c
     */
    public void addConcept(ONDEXConcept c) {
        concepts.add(c);
    }

    /**
     * Add a relation to this subgraph
     *
     * @param r
     */
    public void addRelation(ONDEXRelation r) {
        relations.add(r);
    }

    /**
     * Remove a concept from this subgraph
     *
     * @param c
     */
    public void removeConcept(ONDEXConcept c) {
        concepts.remove(c);
    }

    /**
     * Remove a relation from this subgraph
     *
     * @param r
     */
    public void removeRelation(ONDEXRelation r) {
        relations.remove(r);
    }

    /**
     * Remove members of other subgraph from this subgraph
     *
     * @param sg
     */
    public void remove(Subgraph sg) {
        concepts.removeAll(sg.concepts);
        relations.removeAll(sg.relations);
    }

    /**
     * Add all members of other subgraph to this subgraph
     *
     * @param sg
     */

    public void add(Subgraph sg) {
        concepts.addAll(sg.concepts);
        relations.addAll(sg.relations);
    }

    /**
     * Remove all members of this subgraph that are not found in the other subgraph
     *
     * @param sg
     */
    public void intersection(Subgraph sg) {
        concepts.retainAll(sg.concepts);
        relations.retainAll(sg.relations);
    }

    /**
     * Only keep a difference of this an other subgraph in this subgraph
     *
     * @param sg
     */
    public void difference(Subgraph sg) {
        Set<ONDEXConcept> tempConcepts = new HashSet<ONDEXConcept>(sg.concepts);
        // this is unique to set b
        tempConcepts.removeAll(concepts);
        // this is unique to set b
        concepts.removeAll(sg.concepts);
        // add both together
        concepts.addAll(tempConcepts);
        
        Set<ONDEXRelation> tempRelations = new HashSet<ONDEXRelation>(sg.relations);
        tempRelations.removeAll(relations);
        relations.removeAll(sg.relations);
        relations.addAll(tempRelations);
    }

    public void filterConcepts(AttributePrototype... aps) throws NullValueException, EmptyStringException, AccessDeniedException {
        ConceptMatcher cf = new ConceptMatcher(graph, aps);
        cf.filter(this.getConcepts());
        cf = null;
    }

    public Set<ONDEXConcept> findConcepts(AttributePrototype... aps) throws NullValueException, EmptyStringException, AccessDeniedException {
        ConceptMatcher cf = new ConceptMatcher(graph, aps);
        Set<ONDEXConcept> result = cf.getMatchingConcpetSet(this.getConcepts());
        cf = null;
        return result;
    }

    public void filterRelations(AttributePrototype... aps) throws NullValueException, EmptyStringException, AccessDeniedException {
        RelationMatcher cf = new RelationMatcher(graph, aps);
        cf.filter(this.getRelations());
        cf = null;
    }

    public Set<ONDEXRelation> findRelations(AttributePrototype... aps) throws NullValueException, EmptyStringException, AccessDeniedException {
        RelationMatcher cf = new RelationMatcher(graph, aps);
        Set<ONDEXRelation> result = cf.getMatchingRelationSet(this.getRelations());
        cf = null;
        return result;
    }

    public ONDEXGraph getParent() {
        return graph;
    }

    public boolean contains(AbstractONDEXEntity ae) {
        if (ae instanceof ONDEXConcept)
            return concepts.contains(ae);
        else if (ae instanceof ONDEXRelation)
            return relations.contains(ae);
        else
            return false;
    }

    public final void mapOnAttribute() {
        Set<ONDEXConcept> allConcepts = this.getConcepts();
        Map<ArrayKey<Object>, List<Integer>> gdsToConceptIndex = new HashMap<ArrayKey<Object>, List<Integer>>();
        Map<Integer, List<ArrayKey<Object>>> conceptToGdsIndex = new HashMap<Integer, List<ArrayKey<Object>>>();
        Set<ConceptClass> ccs = StandardFunctions.getContainedConceptClasses(allConcepts);

        //Process a subset for each concept class separately
        for (ConceptClass cc : ccs) {
            Set<ONDEXConcept> subsetByCC = BitSetFunctions.and(allConcepts, graph.getConceptsOfConceptClass(cc));
            gdsToConceptIndex.clear();
            conceptToGdsIndex.clear();
            for (ONDEXConcept c : subsetByCC) {

                //Building the index on all gdss
                CategoryMapBuilder<ArrayKey<Object>, Integer> gdsToConceptIndexBuider = new CategoryMapBuilder<ArrayKey<Object>, Integer>(gdsToConceptIndex);
                CategoryMapBuilder<Integer, ArrayKey<Object>> conceptToGdsIndexBuider = new CategoryMapBuilder<Integer, ArrayKey<Object>>(conceptToGdsIndex);
                for (Attribute attribute : c.getAttributes()) {
                    ArrayKey<Object> accessionKey = new ArrayKey<Object>(new Object[]{attribute.getOfType(), attribute.getValue()});
                    gdsToConceptIndexBuider.addEntry(accessionKey, c.getId());
                    conceptToGdsIndexBuider.addEntry(c.getId(), accessionKey);
                }
            }
            //Collapse equivalent entries
            System.err.println("Finished indexing concepts of " + cc.getId() + " concept class and started collapsing " + now("dd.MM.yyyy G 'at' HH:mm:ss z"));

            Entry<ArrayKey<Object>, List<Integer>> seed = null;
            while (gdsToConceptIndex.size() > 0) {
                Set<Integer> cluster = new HashSet<Integer>();
                seed = gdsToConceptIndex.entrySet().iterator().next();
                List<Integer> subCluster = new ArrayList<Integer>();
                subCluster.addAll(seed.getValue());
                while (true) {
                    List<Integer> tempCluster = new ArrayList<Integer>();
                    for (Integer nextSeed : subCluster) {
                        List<ArrayKey<Object>> gdss = conceptToGdsIndex.remove(nextSeed);
                        if (gdss != null) {
                            for (ArrayKey<Object> gds : gdss) {
                                List<Integer> list = gdsToConceptIndex.remove(gds);
                                if (list != null)
                                    tempCluster.addAll(list);
                            }
                        }
                    }
                    cluster.addAll(subCluster);

                    if (tempCluster.size() == 0) {
                        break;
                    }
                    subCluster = tempCluster;
                }
                mergeConcepts(cluster);
            }

            gdsToConceptIndex.clear();
            conceptToGdsIndex.clear();
        }
    }

    public final void mapOnNames() {
        Set<ONDEXConcept> allConcepts = this.getConcepts();
        Map<String, List<Integer>> nameToConceptIndex = new HashMap<String, List<Integer>>();
        Map<Integer, List<String>> conceptToNameIndex = new HashMap<Integer, List<String>>();
        Set<ConceptClass> ccs = StandardFunctions.getContainedConceptClasses(allConcepts);

        //Process a subset for each concept class separately
        for (ConceptClass cc : ccs) {
            Set<ONDEXConcept> subsetByCC = BitSetFunctions.and(allConcepts, graph.getConceptsOfConceptClass(cc));
            nameToConceptIndex.clear();
            conceptToNameIndex.clear();
            for (ONDEXConcept c : subsetByCC) {

                //Building the index on all accessions
                CategoryMapBuilder<String, Integer> nameToConceptIndexBuider = new CategoryMapBuilder<String, Integer>(nameToConceptIndex);
                CategoryMapBuilder<Integer, String> conceptToNameIndexBuider = new CategoryMapBuilder<Integer, String>(conceptToNameIndex);
                for (ConceptName n : c.getConceptNames()) {
                    nameToConceptIndexBuider.addEntry(n.getName(), c.getId());
                    conceptToNameIndexBuider.addEntry(c.getId(), n.getName());
                }
            }
            //Collapse equivalent entries
            System.err.println("Finished indexing concepts of " + cc.getId() + " concept class and started collapsing " + now("dd.MM.yyyy G 'at' HH:mm:ss z"));

            Entry<String, List<Integer>> seed = null;
            while (nameToConceptIndex.size() > 0) {
                Set<Integer> cluster = new HashSet<Integer>();
                seed = nameToConceptIndex.entrySet().iterator().next();
                List<Integer> subCluster = new ArrayList<Integer>();
                subCluster.addAll(seed.getValue());
                while (true) {
                    List<Integer> tempCluster = new ArrayList<Integer>();
                    for (Integer nextSeed : subCluster) {
                        List<String> names = conceptToNameIndex.remove(nextSeed);
                        if (names != null) {
                            for (String name : names) {
                                List<Integer> list = nameToConceptIndex.remove(name);
                                if (list != null)
                                    tempCluster.addAll(list);
                            }
                        }
                    }
                    cluster.addAll(subCluster);

                    if (tempCluster.size() == 0) {
                        break;
                    }
                    subCluster = tempCluster;
                }
                mergeConcepts(cluster);
            }

            nameToConceptIndex.clear();
            conceptToNameIndex.clear();
        }
    }

    /**
     * This method will perform cv and ambiguity flag agnostic, concept class constrained accession-based mapping on
     * all concepts within the subgraph.
     */
    public final void mapOnAccessions() {
        Set<ONDEXConcept> allConcepts = this.getConcepts();
        Map<ArrayKey<Object>, List<Integer>> accessionToConceptIndex = new HashMap<ArrayKey<Object>, List<Integer>>();
        Map<Integer, List<ArrayKey<Object>>> conceptToAccessionIndex = new HashMap<Integer, List<ArrayKey<Object>>>();
        Set<ConceptClass> ccs = StandardFunctions.getContainedConceptClasses(allConcepts);

        //Process a subset for each concept class separately
        for (ConceptClass cc : ccs) {
            Set<ONDEXConcept> subsetByCC = BitSetFunctions.and(allConcepts, graph.getConceptsOfConceptClass(cc));
            accessionToConceptIndex.clear();
            conceptToAccessionIndex.clear();
            for (ONDEXConcept c : subsetByCC) {

                //Building the index on all accessions
                CategoryMapBuilder<ArrayKey<Object>, Integer> accessionToConceptIndexBuider = new CategoryMapBuilder<ArrayKey<Object>, Integer>(accessionToConceptIndex);
                CategoryMapBuilder<Integer, ArrayKey<Object>> conceptToAccessionIndexBuider = new CategoryMapBuilder<Integer, ArrayKey<Object>>(conceptToAccessionIndex);
                for (ConceptAccession ac : c.getConceptAccessions()) {
                    ArrayKey<Object> accessionKey = new ArrayKey<Object>(new Object[]{ac.getElementOf(), ac.getAccession()});
                    accessionToConceptIndexBuider.addEntry(accessionKey, c.getId());
                    conceptToAccessionIndexBuider.addEntry(c.getId(), accessionKey);
                }
            }
            //Collapse equivalent entries
            System.err.println("Finished indexing concepts of " + cc.getId() + " concept class and started collapsing " + now("dd.MM.yyyy G 'at' HH:mm:ss z"));

            Entry<ArrayKey<Object>, List<Integer>> seed = null;
            while (accessionToConceptIndex.size() > 0) {
                Set<Integer> cluster = new HashSet<Integer>();
                seed = accessionToConceptIndex.entrySet().iterator().next();
                List<Integer> subCluster = new ArrayList<Integer>();
                subCluster.addAll(seed.getValue());
                while (true) {
                    List<Integer> tempCluster = new ArrayList<Integer>();
                    for (Integer nextSeed : subCluster) {
                        List<ArrayKey<Object>> accessions = conceptToAccessionIndex.remove(nextSeed);
                        if (accessions != null) {
                            for (ArrayKey<Object> accession : accessions) {
                                List<Integer> list = accessionToConceptIndex.remove(accession);
                                if (list != null)
                                    tempCluster.addAll(list);
                            }
                        }
                    }
                    cluster.addAll(subCluster);

                    if (tempCluster.size() == 0) {
                        break;
                    }
                    subCluster = tempCluster;
                }
                mergeConcepts(cluster);
            }

            accessionToConceptIndex.clear();
            conceptToAccessionIndex.clear();
        }
    }

    private void mergeConcepts(final Set<Integer> cluster) {
        Iterator<Integer> it = cluster.iterator();
        ONDEXConcept toKeep = graph.getConcept(it.next());
        while (it.hasNext()) {
            ONDEXConcept toMerge = graph.getConcept(it.next());
            for (ONDEXRelation r : graph.getRelationsOfConcept(toMerge)) {
                boolean retainId = false;
                if (this.relations.contains(r)) {
                    relations.remove(r);
                    retainId = true;
                }
                if (retainId) {
                    relations.add(GraphElementManipulation.changeRelationVertex(graph, toMerge, toKeep, r));
                } else {
                    GraphElementManipulation.changeRelationVertex(graph, toMerge, toKeep, r);
                }
            }
            GraphElementManipulation.copyConceptAttributes(toMerge, toKeep);
            int cId = toMerge.getId();
            concepts.add(toMerge);
            if (graph.getConcept(cId) != null)
                graph.deleteConcept(cId);
        }
    }

    private static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    /**
     * Adds all of the concepts contained in a view to this subgraph.
     * The view is used up and closed.
     *
     * @param view - ondex view of concepts
     */
    public void addConcepts(Set<ONDEXConcept> view) {
        for (ONDEXConcept c : view) {
            this.concepts.add(c);
        }
    }

    /**
     * Adds all of the relations contained in a view to this subgraph.
     * The view is used up and closed.
     *
     * @param view - ondex view of relations
     */
    public void addRelations(Set<ONDEXRelation> view) {
        for (ONDEXRelation r : view) {
            this.relations.add(r);
        }
    }

    /**
     * Will add a specified tag concept to all members of this subgraph
     *
     * @param tag - a tag concept
     */
    public void addTag(ONDEXConcept tag) {
        int conceptCounter = 0;
        for(ONDEXConcept c : concepts) {
            if (graph.getConcept(c.getId()) != null) {
                graph.getConcept(c.getId()).addTag(tag);
                conceptCounter++;
            }

        }
        int relationCounter = 0;
        for(ONDEXRelation r : relations) {
            if (graph.getRelation(r.getId()) != null) {
                graph.getRelation(r.getId()).addTag(tag);
                relationCounter++;
            }
        }
        System.err.println("Assigned tag to " + conceptCounter + " concepts and " + relationCounter + " relations.");
    }

    public void createTag(String conceptClass, String name) {
        ONDEXConcept c = graph.getFactory().createConcept(name, createDataSource(graph, "UC"), createCC(graph, conceptClass), createEvidence(graph, "M"));
        c.createConceptName(name, true);
        this.addTag(c);
    }
}
