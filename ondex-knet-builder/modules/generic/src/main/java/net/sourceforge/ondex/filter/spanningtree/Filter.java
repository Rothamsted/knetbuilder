package net.sourceforge.ondex.filter.spanningtree;

import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Get Subgraph Info Of Minimal Spanning Tree Of Node Indices Using Connected
 * Components Info And Neighbour Indices Lists.
 * 
 * @author taubertj
 * 
 */
public class Filter extends ONDEXFilter {

	// contains list of visible concepts
	private Set<ONDEXConcept> concepts = null;

	// contains list of visible relations
	private Set<ONDEXRelation> relations = null;

	// contains list of invisible concepts
	private Set<ONDEXConcept> invconcepts = null;

	// contains list of invisible relations
	private Set<ONDEXRelation> invrelations = null;

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
		return BitSetFunctions.unmodifiableSet(concepts);
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return BitSetFunctions.unmodifiableSet(relations);
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public String getId() {
		return "spanningtree";
	}

	@Override
	public String getName() {
		return "Minimum Spanning Tree";
	}

	@Override
	public String getVersion() {
		return "06.12.2010";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {
		
		int number_Of_Nodes = graph.getConcepts().size();
		
		
	}
}
