package net.sourceforge.ondex.statistics.leells;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * Created by IntelliJ IDEA.
 * User: nmrp3
 * Date: 27-Oct-2009
 * Time: 15:21:06
 * To change this template use File | Settings | File Templates.
 */
@Status(description = "Changed to DISCONTINUED (by Christian) as there is no export file or directory.", status = StatusType.DISCONTINUED)
public class Statistics
        extends ONDEXExport
{
    private static final String GOLD_STANDARD_RELATION = "goldStandardRelation";
    private static final String DATASET_RELATION = "datasetRelation";
    private static final String GOLD_STANDARD_CONCEPT_CLASS = "goldStandardConceptClass";
    private static final String DATASET_CONCEPT_CLASS = "datasetConceptClass";

    public String getName() {
        return "leells";
    }

    public String getVersion() {
        return "alpha";
    }

    @Override
    public String getId() {
        return "leells";
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(GOLD_STANDARD_CONCEPT_CLASS, "type of entities in the gold standard to consider",
                        true, null, false),
                new StringArgumentDefinition(GOLD_STANDARD_RELATION, "relations that are gold standard positives",
                        true, null, false),
                new StringArgumentDefinition(DATASET_CONCEPT_CLASS, "type of entities to compare to the gold standard",
                        true, null, false),
                new StringArgumentDefinition(DATASET_RELATION, "relations to compare to the gold standard",
                        true, null, false)
        };
    }

    public void start()
            throws Exception {
        RelationType eqRelation = graph.getMetaData().getRelationType("equ");

        ConceptClass gsCClass = graph.getMetaData().getConceptClass(
                (String) getArguments().getUniqueValue(GOLD_STANDARD_CONCEPT_CLASS));
        RelationType gsRelType = graph.getMetaData().getRelationType(
                (String) getArguments().getUniqueValue(GOLD_STANDARD_RELATION));
        ConceptClass dsCClass = graph.getMetaData().getConceptClass(
                (String) getArguments().getUniqueValue(DATASET_CONCEPT_CLASS));
        RelationType dsRelType = graph.getMetaData().getRelationType(
                (String) getArguments().getUniqueValue(DATASET_RELATION));

        Collection<ONDEXRelation> eqRelations = graph.getRelationsOfRelationType(eqRelation);

        Collection<ONDEXConcept> gsConcepts = graph.getConceptsOfConceptClass(gsCClass);

        Collection<ONDEXRelation> gsRR = graph.getRelationsOfRelationType(gsRelType);
        Set<ONDEXRelation> gsCR = new HashSet<ONDEXRelation>(graph.getRelationsOfConceptClass(gsCClass));
        Set<ONDEXRelation> gsRelations = new HashSet<ONDEXRelation>(gsRR);
        gsRelations.retainAll(gsCR);

        Collection<ONDEXConcept> dsConcepts = graph.getConceptsOfConceptClass(dsCClass);

        Collection<ONDEXRelation> dsRR = graph.getRelationsOfRelationType(dsRelType);
        Set<ONDEXRelation> dsCR = new HashSet<ONDEXRelation>(graph.getRelationsOfConceptClass(dsCClass));
        Set<ONDEXRelation> dsRelations = new HashSet<ONDEXRelation>(dsRR);
        dsRelations.retainAll(dsCR);


        System.out.println("Relevant gs concepts: " + gsConcepts.size());
        System.out.println("Relevant gs relations: " + gsRelations.size());
        System.out.println("Relevant ds concepts: " + dsConcepts.size());
        System.out.println("Relevant ds relations: " + dsRelations.size());

        Set<ONDEXRelation> eqRel = new HashSet<ONDEXRelation>();
        eqRel.addAll(eqRelations);
        eqRel.retainAll(gsCR);
        eqRel.retainAll(dsCR);

        System.out.println("Equality assertions: " + eqRel.size());

        int commonEdges = 0;
        int missingEdges = 0;
        for (ONDEXRelation gsr : gsRelations) {
            ONDEXConcept gsrf = gsr.getFromConcept();
            ONDEXConcept gsrt = gsr.getToConcept();
            ONDEXConcept dsrf = findEq(gsrf, eqRel);
            ONDEXConcept dsrt = findEq(gsrt, eqRel);

            if (dsrf != null && dsrt != null &&
                    (containsEdge(dsrf, dsrt, dsRelations) || containsEdge(dsrt, dsrf, dsRelations))) {
                commonEdges++;
            } else // todo: check what we should be doing on the case of no equality mapping (null case above)
            {
                missingEdges++;
            }
        }

        // not corrected for symmetry
        int edgesInGS = gsRelations.size();
        int nonEdgesInGS = gsConcepts.size() * gsConcepts.size() - edgesInGS;

        System.out.println("Common edges: " + commonEdges);
        System.out.println("Missing edges: " + missingEdges);
        System.out.println("Gold standard edges: " + edgesInGS);
        System.out.println("Gold standard missing edges: " + nonEdgesInGS);

        double cmRat = ((double) commonEdges) / ((double) missingEdges);
        double enRat = ((double) edgesInGS) / ((double) nonEdgesInGS);
        double lls = Math.log(cmRat / enRat);

        System.out.println("LLS: " + lls);
    }

    private ONDEXConcept findEq(ONDEXConcept c, Set<ONDEXRelation> rels) {
        for (ONDEXRelation r : rels) {
            if (r.getFromConcept().equals(c)) {
                return r.getToConcept();
            } else if (r.getToConcept().equals(c)) {
                return r.getFromConcept();
            }
        }

        return null;
    }

    private boolean containsEdge(ONDEXConcept f, ONDEXConcept t, Set<ONDEXRelation> rels) {
        for (ONDEXRelation r : rels) {
            if (r.getFromConcept().equals(f) && r.getToConcept().equals(t)) {
                return true;
            }
        }

        return false;
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}
