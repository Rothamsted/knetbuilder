package net.sourceforge.ondex.ovtk2.metagraph;

import java.awt.Color;
import java.awt.Paint;
import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Provides a transformation from a given ONDEXMetaRelation to a Color.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaRelationColors implements Transformer<ONDEXMetaRelation, Paint> {

	// contains mapping id to colour
	private Map<RelationType, Color> colors = null;

	// current ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// current PickedInfo
	private PickedInfo<ONDEXMetaRelation> pi = null;

	/**
	 * Initialises the colours for the edges in the graph.
	 * 
	 * @param jung
	 *            ONDEXJUNGGraph
	 */
	public ONDEXMetaRelationColors(ONDEXJUNGGraph jung, PickedInfo<ONDEXMetaRelation> pi) {
		if (pi == null)
			throw new IllegalArgumentException("PickedInfo instance must be non-null");
		this.pi = pi;

		this.graph = jung;
		this.colors = new Hashtable<RelationType, Color>();

		// initialise colours
		updateAll();
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 * @return Colour
	 */
	public Color transform(ONDEXMetaRelation edge) {
		if (pi.isPicked(edge)) {
			return Config.nodePickedColor;
		} else {
			updateColor(edge);
			return colors.get(edge.id);
		}
	}

	/**
	 * Update all colours from the graph.
	 * 
	 */
	public void updateAll() {
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {
			// update with a dummy edge
			updateColor(new ONDEXMetaRelation(graph, rt, null));
		}
	}

	/**
	 * Update the colour of a given edge.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 */
	public void updateColor(ONDEXMetaRelation edge) {
		Color c = Config.getColorForRelationType((RelationType) edge.id);
		updateColor(edge, c);
	}

	/**
	 * Update the colour of a given edge with a given colour.
	 * 
	 * @param edge
	 *            ONDEXMetaRelation
	 * @param color
	 *            Color
	 */
	public void updateColor(ONDEXMetaRelation edge, Color c) {
		if (!edge.isVisible()) {
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			c = new Color(r, g, b, 128);
		}
		colors.put(edge.getRelationType(), c);
	}
}
