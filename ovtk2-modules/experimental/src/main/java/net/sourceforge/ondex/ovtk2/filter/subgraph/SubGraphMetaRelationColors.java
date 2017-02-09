package net.sourceforge.ondex.ovtk2.filter.subgraph;

import java.awt.Color;
import java.awt.Paint;

import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Provides a transformation from a given ONDEXMetaRelation to a Colour.
 * 
 * @author taubertj
 * 
 */
public class SubGraphMetaRelationColors implements
		Transformer<ONDEXMetaRelation, Paint> {

	// ####FIELDS####

	// current PickedInfo
	private PickedInfo<ONDEXMetaRelation> pi = null;

	// ####CONSTRUCTOR####

	/**
	 * Initialises the colours for the edges in the graph.
	 * 
	 * @param pi
	 *            PickedInfo<ONDEXMetaRelation> pi
	 */
	public SubGraphMetaRelationColors(PickedInfo<ONDEXMetaRelation> pi) {
		if (pi == null)
			throw new IllegalArgumentException(
					"PickedInfo instance must be non-null");
		this.pi = pi;
	}

	// ####METHODS####
	
	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 * @return Colour
	 */
	public Color transform(ONDEXMetaRelation edge) {
		if (pi.isPicked(edge))
			return Color.YELLOW;
		if (edge.isVisible())
			return Color.BLUE;
		else
			return Color.LIGHT_GRAY;
	}
}
