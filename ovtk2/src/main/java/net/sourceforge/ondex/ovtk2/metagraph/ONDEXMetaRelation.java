package net.sourceforge.ondex.ovtk2.metagraph;

import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Represents relations of a RelationType in the AbstractONDEXGraph.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaRelation extends ONDEXMetaEntity {

	// number of visible relations
	private int visibleRelations = 0;

	// between which concept classes is this relation type
	private Pair<ConceptClass> pair;

	/**
	 * Initialises ONDEXMetaRelation with given ONDEXJUNGGraph and RelationType.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 * @param rt
	 *            RelationType
	 * @param pair
	 *            Pair<ConceptClass>, optional, if null return all relations of
	 *            type
	 */
	public ONDEXMetaRelation(ONDEXJUNGGraph graph, RelationType rt, Pair<ConceptClass> pair) {
		super(graph, rt);
		this.pair = pair;
	}

	/**
	 * Returns number of relation of this relation type.
	 * 
	 * @return # relations of relation type
	 */
	public int getNumberOfRelations() {
		return getRelations().size();
	}

	/**
	 * Returns number of visible relations
	 * 
	 * @return # visible relations
	 */
	public int getNumberOfVisibleRelations() {
		return visibleRelations;
	}

	/**
	 * Returns an Set on all relations that belong to the RelationType
	 * represented by this MetaRelation.
	 * 
	 * @return Set<ONDEXRelation>
	 */
	public Set<ONDEXRelation> getRelations() {
		// no pair has been specified, so return all relations
		if (pair == null)
			return graph.getRelationsOfRelationType((RelationType) id);

		// get all relations of relation type
		Set<ONDEXRelation> all = BitSetFunctions.copy(graph.getRelationsOfRelationType((RelationType) id));

		// get relations of first concept class
		all.retainAll(graph.getRelationsOfConceptClass(pair.getFirst()));

		// get relations of second concept class
		all.retainAll(graph.getRelationsOfConceptClass(pair.getSecond()));

		// sanity check to preserve directionality of relations
		Iterator<ONDEXRelation> it = all.iterator();
		while (it.hasNext()) {
			ONDEXRelation r = it.next();
			if (!r.getFromConcept().getOfType().equals(pair.getFirst()))
				it.remove();
		}
		return all;
	}

	/**
	 * Returns the RelationType represented by this ONDEXMetaRelation.
	 * 
	 * @return RelationType
	 */
	public RelationType getRelationType() {
		return (RelationType) id;
	}

	@Override
	public boolean isVisible() {
		visibleRelations = 0;
		boolean visible = false;
		for (ONDEXRelation r : getRelations()) {
			visible = visible || graph.isVisible(r);
			if (graph.isVisible(r))
				visibleRelations++;
		}
		return visible;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof ONDEXMetaRelation)
			return super.equals(arg0) && this.pair.equals(((ONDEXMetaRelation) arg0).pair);
		else
			return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 31 * pair.hashCode();
	}

}
