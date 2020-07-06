package net.sourceforge.ondex.filter.relationtypemissing;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Removes specified concept class with missing relation type from the graph.
 *
 * @author taubertj
 * @version 05.03.2009
 */
@Authors(authors = {"Jan Taubert"}, emails = {"jantaubert at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {

    // contains list of visible concepts
    private Set<ONDEXConcept> concepts = null;

    // contains list of visible relations
    private Set<ONDEXRelation> relations = null;

    private Set<ONDEXConcept> inverseConcepts = null;

    private Set<ONDEXRelation> inverseRelations = null;

    /**
     * Constructor
     */
    public Filter() {
    }

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : concepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : relations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return concepts;
    }

    public Set<ONDEXConcept> getInVisibleConcepts() {
        return inverseConcepts;
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return relations;
    }

    public Set<ONDEXRelation> getInVisibleRelations() {
        return inverseRelations;
    }

    /**
     * Returns the name of this filter.
     *
     * @return name
     */
    public String getName() {
        return "MissingRelationType Filter";
    }

    /**
     * Returns the version of this filter.
     *
     * @return version
     */
    public String getVersion() {
        return "05.03.2009";
    }

    @Override
    public String getId() {
        return "relationtypemissing";
    }


    /**
     * ArgumentDefinitions for RelationType, ConceptClass restrictions.
     *
     * @return two argument definition
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition rt_arg = new StringArgumentDefinition(
                TARGETRT_ARG, TARGETRT_ARG_DESC, true, "", false);
        StringArgumentDefinition cc_arg = new StringArgumentDefinition(
                TARGETCC_ARG, TARGETCC_ARG_DESC, true, "", false);
        return new ArgumentDefinition<?>[]{rt_arg, cc_arg};
    }

    /**
     * Filters the graph and constructs the lists for visible concepts and
     * relations.
     */
    public void start() throws InvalidPluginArgumentException {

        // get relation type
        RelationType filterOnRt = graph.getMetaData().getRelationType(
                args.getUniqueValue(TARGETRT_ARG).toString());
        if (filterOnRt == null) {
            fireEventOccurred(new WrongParameterEvent(args
                    .getUniqueValue(TARGETRT_ARG)
                    + " is not a valid RelationType.", "[Filter - start]"));
            return;
        }

        // get concept class
        ConceptClass filterOnCc = graph.getMetaData().getConceptClass(
                args.getUniqueValue(TARGETCC_ARG).toString());
        if (filterOnCc == null) {
            fireEventOccurred(new WrongParameterEvent(args
                    .getUniqueValue(TARGETCC_ARG)
                    + " is not a valid ConceptClass.", "[Filter - start]"));
            return;
        }

        // identified concepts and relations to be removed
        Set<Integer> conceptsToFilter = new HashSet<Integer>();
        Set<Integer> relationsToFilter = new HashSet<Integer>();

        // iterate overall concepts of concept class
        concepts = graph.getConceptsOfConceptClass(filterOnCc);
        for (ONDEXConcept c : concepts) {
            // look if there is the wished relation contained
            relations = graph.getRelationsOfConcept(c);
            if (relations.size() == 0) {
                // no relations means filter
                conceptsToFilter.add(c.getId());
            } else {
                boolean keep = false;
                for (ONDEXRelation r : relations) {
                    // this one has the right relation, so keep it
                    if (r.getOfType().equals(filterOnRt)) {
                        keep = true;
                        break;
                    }
                }
                if (!keep) {
                    conceptsToFilter.add(c.getId());
                    // add all relations of removed concept
                    for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                        relationsToFilter.add(r.getId());
                    }
                }
            }
        }

        // this is filtered out
        inverseConcepts = BitSetFunctions.create(graph,
                ONDEXConcept.class, conceptsToFilter);
        inverseRelations = BitSetFunctions.create(graph,
                ONDEXRelation.class, relationsToFilter);

        // the rest is kept
        concepts = BitSetFunctions.copy(graph.getConcepts());
        concepts.removeAll(inverseConcepts);
        relations = BitSetFunctions.copy(graph.getRelations());
        relations.removeAll(inverseRelations);
    }

    /**
     * An indexed graph is not required.
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
