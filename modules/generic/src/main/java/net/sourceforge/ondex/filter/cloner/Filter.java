package net.sourceforge.ondex.filter.cloner;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import java.util.Set;

/**
 * Returns the graph exactly
 *
 * @author hindlem
 */
@Authors(authors = {"Matthew Hindle"}, emails = {"matthew_hindle at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {" \tjweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter
{

    public Filter() {
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[0];
    }

    @Override
    public String getName() {
        return "Graph Cloner";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public String getId() {
        return "cloner";
    }


    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return graph.getRelations();
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return graph.getConcepts();
    }

    /**
     * no idea ...
     */
    public void start() {

    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : graph.getConcepts()) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : graph.getRelations()) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }
}
