package net.sourceforge.ondex.ovtk2.filter.subgraph;

import java.awt.Color;
import java.awt.Paint;

import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Provides a transformation from a given ONDEXMetaConcept to a Colour.
 * 
 * @author taubertj
 * 
 */
public class SubGraphMetaConceptColors implements
		Transformer<ONDEXMetaConcept, Paint> {

	// ####FIELDS####

	// current PickedInfo
	private PickedInfo<ONDEXMetaConcept> pi = null;

	// ####CONSTRUCTOR####

	/**
	 * Initialises the colours for the nodes in the graph.
	 * 
	 * @param pi
	 *            PickedInfo<ONDEXMetaConcept>
	 */
	public SubGraphMetaConceptColors(PickedInfo<ONDEXMetaConcept> pi) {
		if (pi == null)
			throw new IllegalArgumentException(
					"PickedInfo instance must be non-null");
		this.pi = pi;
	}

	// ####METHODS####

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 * @return Colour
	 */
	public Color transform(ONDEXMetaConcept node) {
		if (pi.isPicked(node))
			return Color.YELLOW;
		if (node.isVisible())
			return Color.BLUE;
		else
			return Color.LIGHT_GRAY;
	}

}