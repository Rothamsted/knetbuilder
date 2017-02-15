package net.sourceforge.ondex.transformer.gospecificity;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.tools.SetMapBuilder;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author lysenkoa
 */
@Status(description = "Tested September 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Transformer extends ONDEXTransformer
{
    public final static String RT_HAS_FUNCTION = "has_function";//molecular function "MolFunc"
    public final static String RT_HAS_PARTICIPANT = "has_participant";//biological process "BioProc"
    public final static String RT_LOCATED_IN = "located_in";//cellular component "CelComp"

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new BooleanArgumentDefinition(
                        "ProcessPerDataSource",
                        "Proccess specificity of annotations with each DataSource only",
                        false, false),
                new StringArgumentDefinition(
                        "ConceptClass",
                        "ConceptClass to process annotations on",
                        true, "Protein", false)
        };
    }

    @Override
    public String getId() {
        return "gosepecificity";
    }

    @Override
    public String getName() {
        return "GO specificity";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private final Map<Integer, Set<Integer>> bpIndex = new HashMap<Integer, Set<Integer>>();
    private final Map<Integer, Set<Integer>> mfIndex = new HashMap<Integer, Set<Integer>>();
    private final Map<Integer, Set<Integer>> ccIndex = new HashMap<Integer, Set<Integer>>();


    @Override
    public void start() throws Exception {

        boolean perCV = (Boolean) args.getUniqueValue("ProcessPerCV");

        if (perCV) {
            System.out.println("Processing specificity on a per DataSource basis");
        }

        ConceptClass protein = graph.getMetaData().getConceptClass((String) args.getUniqueValue("ConceptClass"));
        RelationType is_a = graph.getMetaData().getRelationType("is_a");
        ConceptClass bp = graph.getMetaData().getConceptClass("BioProc");
        RelationType bp_r = graph.getMetaData().getRelationType(RT_HAS_PARTICIPANT);
        ConceptClass cc = graph.getMetaData().getConceptClass("CelComp");
        RelationType cc_r = graph.getMetaData().getRelationType(RT_LOCATED_IN);
        ConceptClass mf = graph.getMetaData().getConceptClass("MolFunc");
        RelationType mf_r = graph.getMetaData().getRelationType(RT_HAS_FUNCTION);
        buildIndex(graph, bp, is_a, new SetMapBuilder<Integer, Integer>(bpIndex));
        buildIndex(graph, cc, is_a, new SetMapBuilder<Integer, Integer>(ccIndex));
        buildIndex(graph, mf, is_a, new SetMapBuilder<Integer, Integer>(mfIndex));
        Set<ONDEXConcept> cs = graph.getConceptsOfConceptClass(protein);
        Set<RelationType> set_bp_r = new HashSet<RelationType>();
        set_bp_r.add(bp_r);
        Set<RelationType> set_cc_r = new HashSet<RelationType>();
        set_cc_r.add(cc_r);
        Set<RelationType> set_mf_r = new HashSet<RelationType>();
        set_mf_r.add(mf_r);

        int processdeleted = 0;
        int componantdeleted = 0;
        int functiondeleted = 0;
        for (ONDEXConcept c : cs) {
            if (perCV) functiondeleted = functiondeleted + processOntologyPerCV(graph, c, set_bp_r, bpIndex);
            else functiondeleted = functiondeleted + processOntology(graph, c, set_bp_r, bpIndex);

            if (perCV) componantdeleted = componantdeleted + processOntologyPerCV(graph, c, set_cc_r, ccIndex);
            else componantdeleted = componantdeleted + processOntology(graph, c, set_cc_r, ccIndex);

            if (perCV) processdeleted = processdeleted + processOntologyPerCV(graph, c, set_mf_r, mfIndex);
            else processdeleted = processdeleted + processOntology(graph, c, set_mf_r, mfIndex);
        }
        System.out.println("Removed " + processdeleted + " process, " + componantdeleted + " component, and " + functiondeleted + " function annotations");
    }

    //TODO - hideous, but will do for now...

    public static void buildIndex(final ONDEXGraph graph, ConceptClass cc, RelationType is_a, SetMapBuilder<Integer, Integer> index) {
        Set<RelationType> rts = new HashSet<RelationType>();
        rts.add(is_a);
        for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
            Integer id = c.getId();
            Set<Integer> nextLevel = getIds(relationsToTargets(getOutgoingRelationsInclusive(graph, c, rts)));
            index.addAll(id, nextLevel);
        }
        boolean hasAddition = true;
        while (hasAddition) {
            hasAddition = false;
            for (Entry<Integer, Set<Integer>> ent : index.getSetMap().entrySet()) {
                int size = ent.getValue().size();
                Set<Integer> toAdd = new HashSet<Integer>();
                for (Integer i : ent.getValue()) {
                    toAdd.addAll(index.getSetMap().get(i));
                }
                ent.getValue().addAll(toAdd);
                if (!hasAddition && ent.getValue().size() != size) {
                    hasAddition = true;
                }
            }
        }
    }

    /**
     * @param graph
     * @param c
     * @param ont
     * @param index
     */
    public int processOntologyPerCV(final ONDEXGraph graph, final ONDEXConcept c, final Set<RelationType> ont, Map<Integer, Set<Integer>> index) {
        int removed = 0;
        Map<String, Set<Integer>> cv2OntCs = new HashMap<String, Set<Integer>>();

        Set<ONDEXConcept> targets = relationsToTargets(getOutgoingRelationsInclusive(graph, c, ont));
        for (ONDEXConcept target : targets) {
            for (String cv : target.getElementOf().getId().split(":")) {
                Set<Integer> ontologyConcepts = cv2OntCs.get(cv);
                if (ontologyConcepts == null) {
                    ontologyConcepts = new HashSet<Integer>();
                    cv2OntCs.put(cv, ontologyConcepts);
                }
                ontologyConcepts.add(target.getId());
            }
        }
        for (String cv : cv2OntCs.keySet()) {
            Set<Integer> ontCs = cv2OntCs.get(cv);

            Set<Integer> keep = new HashSet<Integer>(ontCs);
            RelationType rt = ont.iterator().next();
            for (Integer i : ontCs) {
                Set<Integer> set = index.get(i);
                if (set.size() == 0) {
                    keep.remove(i);
                }
                keep.removeAll(set);
            }
            ontCs.removeAll(keep);
            for (Integer id : ontCs) {
                graph.deleteRelation(c, graph.getConcept(id), rt);
                removed++;
            }
        }
        return removed;
    }

    /**
     * @param graph
     * @param c
     * @param ont
     * @param index
     */
    public int processOntology(final ONDEXGraph graph, final ONDEXConcept c, final Set<RelationType> ont, Map<Integer, Set<Integer>> index) {
        int removed = 0;
        Set<Integer> ontCs = getIds(relationsToTargets(getOutgoingRelationsInclusive(graph, c, ont)));
        Set<Integer> keep = new HashSet<Integer>(ontCs);
        RelationType rt = ont.iterator().next();
        for (Integer i : ontCs) {
            Set<Integer> set = index.get(i);
            if (set.size() == 0) {
                keep.remove(i);
            }
            keep.removeAll(set);
        }
        ontCs.removeAll(keep);
        for (Integer id : ontCs) {
            graph.deleteRelation(c, graph.getConcept(id), rt);
            removed++;
        }
        return removed;
    }

    public static Set<Integer> getIds(Set<ONDEXConcept> cs) {
        Set<Integer> result = new HashSet<Integer>();
        for (ONDEXConcept c : cs) {
            result.add(c.getId());
        }
        return result;
    }

    /**
     * Returns all outgoing relations of concept
     *
     * @param graph - graph
     * @param c     - concept
     * @return - set of relations
     */
    public static final Set<ONDEXRelation> getOutgoingRelationsExclusive(ONDEXGraph graph, ONDEXConcept c, Set<RelationType> toExclude) {
        Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            if (r.getFromConcept().equals(c) && !toExclude.contains(r.getOfType()))
                result.add(r);
        }
        return result;
    }

    /**
     * Return all target concepts for a given set of relations.
     * If the concept is bout a source an a target it will be omitted
     *
     * @param rs - set of relations
     * @return -  set of target concepts
     */
    public static final Set<ONDEXConcept> relationsToTargets(Set<ONDEXRelation> rs) {
        Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
        for (ONDEXRelation r : rs) {
            ONDEXConcept from = r.getFromConcept();
            ONDEXConcept to = r.getToConcept();
            if (to != from)
                result.add(to);
        }

        return result;
    }

    /**
     * Returns all outgoing relations of concept
     *
     * @param graph - graph
     * @param c     - concept
     * @return - set of relations
     */
    public static final Set<ONDEXRelation> getOutgoingRelationsInclusive(ONDEXGraph graph, ONDEXConcept c, Set<RelationType> toInclude) {
        Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
        for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
            if (r.getFromConcept().equals(c) && toInclude.contains(r.getOfType()))
                result.add(r);
        }
        return result;
    }

}
