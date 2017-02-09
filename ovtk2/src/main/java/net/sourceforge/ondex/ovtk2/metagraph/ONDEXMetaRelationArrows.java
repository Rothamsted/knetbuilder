package net.sourceforge.ondex.ovtk2.metagraph;

import net.sourceforge.ondex.core.ONDEXGraph;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * Provides a prediate whether or not to draw arrows at edges.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaRelationArrows implements Predicate<Context<Graph<ONDEXMetaConcept, ONDEXMetaRelation>, ONDEXMetaRelation>> {

	/**
	 * Initializes the arrow predicate for the edges in the graph.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 */
	public ONDEXMetaRelationArrows(ONDEXGraph aog) {

	}

	/**
	 * Return whether or not to draw arrows for a given Context.
	 * 
	 * @param object
	 *            Context<Graph<ONDEXMetaConcept, ONDEXMetaRelation>,
	 *            ONDEXMetaRelation>
	 * @return boolean
	 */
	public boolean evaluate(Context<Graph<ONDEXMetaConcept, ONDEXMetaRelation>, ONDEXMetaRelation> object) {
		return true;
	}

}
