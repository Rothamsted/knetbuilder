package net.sourceforge.ondex.ovtk2.metagraph;

import java.util.Set;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

/**
 * Represents concepts of a ConceptClass in the ONDEXJUNGGraph.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaConcept extends ONDEXMetaEntity {

	// number of visible concepts
	private int visibleConcepts = 0;

	/**
	 * Initialises ONDEXMetaConcept with given ONDEXJUNGGraph and ConceptClass.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 * @param cc
	 *            ConceptClass
	 */
	public ONDEXMetaConcept(ONDEXJUNGGraph graph, ConceptClass cc) {
		super(graph, cc);
	}

	/**
	 * Returns the ConceptClass represented by this ONDEXMetaConcept.
	 * 
	 * @return ConceptClass
	 */
	public ConceptClass getConceptClass() {
		return (ConceptClass) id;
	}

	/**
	 * Returns an Set on all concepts that belong to the ConceptClass
	 * represented by this MetaConcept.
	 * 
	 * @return Set<ONDEXConcept>
	 */
	public Set<ONDEXConcept> getConcepts() {
		return graph.getConceptsOfConceptClass((ConceptClass) id);
	}

	/**
	 * Returns number of concepts of this concept class.
	 * 
	 * @return # concepts of concept class
	 */
	public int getNumberOfConcepts() {
		return getConcepts().size();
	}

	/**
	 * Returns number of visible concepts
	 * 
	 * @return # visible concepts
	 */
	public int getNumberOfVisibleConcepts() {
		return visibleConcepts;
	}

	@Override
	public boolean isVisible() {
		visibleConcepts = 0;
		boolean visible = false;
		for (ONDEXConcept c : getConcepts()) {
			visible = visible || graph.isVisible(c);
			if (graph.isVisible(c))
				visibleConcepts++;
		}
		return visible;
	}

}
