package net.sourceforge.ondex.ovtk2.metagraph;

import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

import org.apache.commons.collections15.Transformer;

/**
 * Provides a transformation from a given ONDEXMetaConcept to a String as label.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaConceptLabels implements Transformer<ONDEXMetaConcept, String> {

	// current ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// contains mapping concept class to label
	private Map<ConceptClass, String> labels = null;

	/**
	 * Initialises the labels for the nodes in the graph.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 */
	public ONDEXMetaConceptLabels(ONDEXJUNGGraph graph) {

		this.graph = graph;
		this.labels = new Hashtable<ConceptClass, String>();

		// initialise labels
		updateAll();
	}

	/**
	 * Extracts the label from a given ConceptClass.
	 * 
	 * @param cc
	 *            ConceptClass
	 * @return String
	 */
	private String getLabel(ConceptClass cc) {
		String label = null;
		// first try fullname of cc
		label = cc.getFullname();
		// else take id of cc
		if (label.trim().length() == 0) {
			label = cc.getId();
		}
		return label;
	}

	/**
	 * Update the label of a given node.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 */
	public void updateLabel(ONDEXMetaConcept node) {
		labels.put((ConceptClass) node.id, getLabel(node.getConceptClass()));
	}

	/**
	 * Update all labels from the graph.
	 * 
	 */
	public void updateAll() {
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			labels.put(cc, getLabel(cc));
		}
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 * @return String
	 */
	public String transform(ONDEXMetaConcept node) {
		return labels.get(node.id);
	}

}