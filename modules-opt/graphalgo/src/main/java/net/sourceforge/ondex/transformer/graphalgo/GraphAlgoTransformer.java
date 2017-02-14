package net.sourceforge.ondex.transformer.graphalgo;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.Set;

/**
 * Abstract class all GraphAlgo transformers belong to. Handles common argument
 * names etc.
 *
 * @author taubertj
 */
public abstract class GraphAlgoTransformer extends
        ONDEXTransformer implements ArgumentNames {

    /**
     * Array index 1,2,...,m to relation id
     */
    private int[] relationMapping;

    /**
     * Array index 1,2,...,n to concept id
     */
    private int[] conceptMapping;

    /**
     * Concept id to array index 1,2,...,n
     */
    private int[] reverseConceptMapping;

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        StringArgumentDefinition attr = new StringArgumentDefinition(
                IDENTIFIER_ARG, IDENTIFIER_ARG_DESC, true, null, false);
        return new ArgumentDefinition[]{attr};
    }

    /**
     * Returns identifier for results.
     *
     * @return identifier
     */
    protected String getIdentifier() throws InvalidPluginArgumentException {
        return (String) args.getUniqueValue(IDENTIFIER_ARG);
    }

    /**
     * Searches for last concept id in whole graph.
     *
     * @return id of last concept
     */
    private int getLastIdForConcepts() {
        int last = 0;
        for (ONDEXConcept c : graph.getConcepts()) {
            int id = c.getId();
            if (id > last)
                last = id;
        }
        return last;
    }

    /**
     * Returns the ONDEX graph as an array representation of node lists. The
     * indices are the actual relation and concept IDs. The graph is not
     * necessarily connected.
     *
     * @return
     */
    protected int[][] graphAsArray() {

        // create concept mapping and reverse
        int n = numberOfNodes();
        conceptMapping = new int[n + 1];
        reverseConceptMapping = new int[getLastIdForConcepts() + 1];

        // project concepts to 1,2,...,n
        int i = 1;
        for (ONDEXConcept c : graph.getConcepts()) {
            int id = c.getId();
            conceptMapping[i] = id;
            reverseConceptMapping[id] = i;
            i++;
        }

        // create relation mapping
        int m = numberOfEdges();
        relationMapping = new int[m + 1];

        // create return structure
        int[][] r = new int[2][];
        int[] nodei = new int[m + 1];
        int[] nodej = new int[m + 1];

        // project relations to 1,2,...,m
        i = 1;
        for (ONDEXRelation rel : graph.getRelations()) {
            ONDEXConcept from = rel.getFromConcept();
            ONDEXConcept to = rel.getToConcept();
            nodei[i] = reverseConceptMapping[from.getId()];
            nodej[i] = reverseConceptMapping[to.getId()];
            relationMapping[i] = rel.getId();
            i++;
        }

        r[0] = nodei;
        r[1] = nodej;

        return r;
    }

    /**
     * Returns ONDEXConcept for a given array index.
     *
     * @param index array index of projection
     * @return corresponding ONDEXConcept
     */
    public ONDEXConcept getConceptForIndex(int index) {
        return graph.getConcept(Integer.valueOf(conceptMapping[index]));
    }

    /**
     * Returns ONDEXRelation for a given array index.
     *
     * @param index array index of projection
     * @return corresponding ONDEXRelation
     */
    public ONDEXRelation getRelationForIndex(int index) {
        return graph.getRelation(Integer.valueOf(relationMapping[index]));
    }

    /**
     * Returns the number of edges (relations) in the graph.
     *
     * @return number of edges
     */
    protected int numberOfEdges() {
        Set<ONDEXRelation> it = graph.getRelations();
        int r = it.size();
        return r;
    }

    /**
     * Returns the number of nodes (concepts) in the graph.
     *
     * @return number of nodes
     */
    protected int numberOfNodes() {
        Set<ONDEXConcept> it = graph.getConcepts();
        int r = it.size();
        return r;
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
