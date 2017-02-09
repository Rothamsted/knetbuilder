package net.sourceforge.ondex.ovtk2.metagraph;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

/**
 * Parent class for ONDEXMetaConcept and ONDEXMetaRelation. Contains shared
 * functionality.
 * 
 * @author taubertj
 * 
 */
public abstract class ONDEXMetaEntity {

	// ConceptClass or RelationType
	protected MetaData id = null;

	// wrapped ONDEXJUNGGraph
	protected ONDEXJUNGGraph graph = null;

	/**
	 * Constructor for wrapped ONDEXGraph.
	 * 
	 * @param graph
	 *            wrapped ONDEXJUNGGraph
	 * @param id
	 *            ConceptClass or RelationType
	 */
	protected ONDEXMetaEntity(ONDEXJUNGGraph graph, MetaData id) {
		this.graph = graph;
		this.id = id;
	}

	/**
	 * Returns ConceptClass or RelationType.
	 * 
	 * @return MetaData
	 */
	public MetaData getMetaData() {
		return id;
	}

	/**
	 * Determines whether or not elements of this entity are visible.
	 * 
	 * @return has visible elements
	 */
	public abstract boolean isVisible();

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof ONDEXMetaEntity)
			return this.id.equals(((ONDEXMetaEntity) arg0).id);
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id.toString();
	}

}
