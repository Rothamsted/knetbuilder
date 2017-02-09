package net.sourceforge.ondex.ovtk2.metagraph;

import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

import org.apache.commons.collections15.Transformer;

/**
 * Provides a transformation from a given ONDEXMetaRelation to a String as
 * label.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaRelationLabels implements Transformer<ONDEXMetaRelation, String> {

	// current ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// contains mapping id to label
	private Map<RelationType, String> labels = null;

	/**
	 * Initialises the labels for the edges in the graph.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 */
	public ONDEXMetaRelationLabels(ONDEXJUNGGraph graph) {

		this.graph = graph;
		this.labels = new Hashtable<RelationType, String>();

		// initialise labels
		updateAll();
	}

	/**
	 * Makes maximum length texts.
	 * 
	 * @param text
	 *            String to shorten
	 * @return shorten String
	 */
	private String makeMaxLength(String text) {
		if (text.length() > 15) {
			return text.substring(0, 14) + "...";
		} else
			return text;
	}

	/**
	 * Extracts the label from a given RelationType.
	 * 
	 * @param rt
	 *            ARelationType
	 * @return String
	 */
	private String getLabel(RelationType rt) {
		String label = null;
		// first try fullname of rt
		label = rt.getFullname();
		// else take id of rtset
		if (label.trim().length() == 0) {
			label = rt.getId();
		}
		label = makeMaxLength(label);
		return label;
	}

	/**
	 * Update the label of a given edge.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 */
	public void updateLabel(ONDEXMetaRelation edge) {
		labels.put((RelationType) edge.id, getLabel(edge.getRelationType()));
	}

	/**
	 * Update all labels from the graph.
	 * 
	 */
	public void updateAll() {
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {
			labels.put(rt, getLabel(rt));
		}
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 * @return String
	 */
	public String transform(ONDEXMetaRelation edge) {
		return labels.get(edge.id);
	}

}
