package net.sourceforge.ondex.mapping.transitive;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import static net.sourceforge.ondex.mapping.ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG;
import static net.sourceforge.ondex.mapping.ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG_DESC;

/**
 * Implements a transitive mapping for equ relationtypes.
 *
 * @author taubertj
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Mapping extends ONDEXMapping implements ArgumentNames,
        MetaData {

    /**
     * Constructor
     */
    public Mapping() {
    }

    /**
     * Returns the name of this mapping.
     *
     * @return String
     */
    public String getName() {
        return new String("Transitive mapping");
    }

    /**
     * Returns the version of this mapping.
     *
     * @return String
     */
    public String getVersion() {
        return new String("15.04.2008");
    }

    @Override
    public String getId() {
        return "transitive";
    }

    /**
     * Target relation type set can be specified. The default target relation
     * type is equ.
     *
     * @return ArgumentDefinition<?>[]
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition relationTypeSets = new StringArgumentDefinition(
                RELTYPES_ARG, RELTYPES_ARG_DESC, false, "equ", true);

        StringMappingPairArgumentDefinition ccRestriction = new StringMappingPairArgumentDefinition(
                CONCEPTCLASS_RESTRICTION_ARG,
                CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true);

        return new ArgumentDefinition<?>[]{relationTypeSets, ccRestriction};
    }

    @Override
    public void start() throws InvalidPluginArgumentException {

        // get restrictions on ConceptClasses
        Map<ConceptClass, ConceptClass> ccMapping = getAllowedCCs(graph);

        // global list of already processed concepts
        HashSet<ONDEXConcept> visited = new HashSet<ONDEXConcept>();

        // get the relationtypeset and evidencetype for the mapping
        HashSet<RelationType> targetRelationTypes = new HashSet<RelationType>();
        for (Object targetRelationType : args.getObjectValueArray(RELTYPES_ARG)) {
            RelationType rtSet = graph.getMetaData()
                    .getRelationType(targetRelationType.toString());
            if (rtSet == null) {
                fireEventOccurred(new GeneralOutputEvent(
                        "RelationType " + targetRelationType.toString()
                                + " could not be found", this.getClass()
                                .toString()));
            } else
                targetRelationTypes.add(rtSet);
        }

        RelationType rtSet = graph.getMetaData()
                .getRelationType(relType);

        EvidenceType eviType = graph.getMetaData().getEvidenceType(
                evidence);

        // iterate over all concepts
        for (ONDEXConcept root : graph.getConcepts()) {

            // concept was not yet been found
            if (!visited.contains(root)) {

                // add root to set of visited concepts
                visited.add(root);

                // contains all reached concepts
                HashSet<ONDEXConcept> seen = new HashSet<ONDEXConcept>();

                // contains concepts to be visited
                LinkedList<ONDEXConcept> que = new LinkedList<ONDEXConcept>();
                que.add(root);

                // start breadth first search
                while (!que.isEmpty()) {

                    // dequeue next concept
                    ONDEXConcept concept = que.poll();

                    // prevent from loops
                    if (!seen.contains(concept)) {

                        // add to seen concept IDs
                        seen.add(concept);

                        // iterate over all relations
                        for (ONDEXRelation r : graph.getRelationsOfConcept(concept)) {
                            // check for equ relation type set
                            if (targetRelationTypes.contains(r.getOfType())) {
                                // get concepts of relation
                                ONDEXConcept from = r.getFromConcept();
                                ONDEXConcept to = r.getToConcept();

                                // enqueue from concept, which has to be visited
                                // next
                                if (!from.equals(concept)) {
                                    que.add(from);
                                }

                                // enqueue to concept, which has to be visited
                                // next
                                if (!to.equals(concept)) {
                                    que.add(to);
                                }
                            }
                        }
                    }
                }

                // iterate over all found concepts that were reachable from the
                // root
                for (ONDEXConcept fromConcept : seen) {
                    visited.add(fromConcept);

                    // iterate again over all found concepts
                    for (ONDEXConcept toConcept : seen) {

                        // no self equ relations
                        if (!fromConcept.equals(toConcept)) {

                            // check ConceptClass conditions
                            ConceptClass fromCC = fromConcept.getOfType();
                            ConceptClass toCC = toConcept.getOfType();
                            if (ccMapping.size() > 0 && !toCC.equals(ccMapping.get(fromCC))) {
                                continue;
                            }

                            // get existing relation
                            ONDEXRelation relation = graph.getRelation(
                                    fromConcept, toConcept, rtSet);

                            // if there is no relation yet
                            if (relation == null) {
                                relation = graph.getFactory().createRelation(fromConcept,
                                        toConcept, rtSet, eviType);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Does not require a Lucene index.
     *
     * @return false
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }
}