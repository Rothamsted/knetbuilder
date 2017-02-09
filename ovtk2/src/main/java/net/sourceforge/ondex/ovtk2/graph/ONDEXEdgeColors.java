package net.sourceforge.ondex.ovtk2.graph;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.custom.MultiColorEdgePaint;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Provides a transformation from a given ONDEXRelation to a Colour.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXEdgeColors implements Transformer<ONDEXRelation, Paint> {

	public static final String ET = "et";

	public static final String RT = "rt";

	public static final String GRAPH_COLORING_RELATION_STRATEGY = "Graph.ColoringRelationStrategy";

	// different colour selection strategies
	public enum EdgeColorSelection {
		EVIDENCETYPE, MANUAL, RELATIONTYPE;
	}

	// contains mapping id to colour
	private final Map<ONDEXRelation, Paint> colors;

	// current PickedInfo
	private final PickedInfo<ONDEXRelation> pi;

	// current colour selection
	private EdgeColorSelection strategy;

	/**
	 * Initialises the colours for the edges in the graph.
	 * 
	 * @param pi
	 *            PickedInfo<ONDEXEdge>
	 */
	public ONDEXEdgeColors(PickedInfo<ONDEXRelation> pi) {
		if (pi == null)
			throw new IllegalArgumentException("PickedInfo instance must be non-null");
		this.pi = pi;
		this.colors = LazyMap.decorate(new HashMap<ONDEXRelation, Paint>(), new Factory<Paint>() {
			@Override
			public Paint create() {
				return Config.defaultColor;
			}
		});

		// get colouring strategy from visual.xml
		String s = Config.visual.getProperty(GRAPH_COLORING_RELATION_STRATEGY);
		if (s == null)
			// default case
			strategy = EdgeColorSelection.RELATIONTYPE;
		else if (s.equals(RT))
			strategy = EdgeColorSelection.RELATIONTYPE;
		else if (s.equals(ET))
			strategy = EdgeColorSelection.EVIDENCETYPE;
		else
			strategy = EdgeColorSelection.RELATIONTYPE;
	}

	/**
	 * Returning current EdgeColorSelection.
	 * 
	 * @return current EdgeColorSelection
	 */
	public EdgeColorSelection getEdgeColorSelection() {
		return strategy;
	}

	/**
	 * Sets the EdgeColorSelection to use for the nodes in the graph.
	 * 
	 * @param s
	 *            EdgeColorSelection to use
	 */
	public void setEdgeColorSelection(EdgeColorSelection s) {
		this.strategy = s;
		if (s != EdgeColorSelection.MANUAL) {
			// save colouring strategy to visual.xml
			if (strategy == EdgeColorSelection.RELATIONTYPE) {
				Config.visual.setProperty(GRAPH_COLORING_RELATION_STRATEGY, RT);
			} else if (strategy == EdgeColorSelection.EVIDENCETYPE) {
				Config.visual.setProperty(GRAPH_COLORING_RELATION_STRATEGY, ET);
			}
			if (!Config.isApplet)
				Config.saveVisual();
		}
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXRelation
	 * @return Paint
	 */
	public Paint transform(ONDEXRelation edge) {
		if (pi.isPicked(edge)) {
			return Config.edgePickedColor;
		} else {
			if (!colors.containsKey(edge))
				updateColor(edge);
			return colors.get(edge);
		}
	}

	/**
	 * Update all colours from the graph.
	 */
	public void updateAll() {
		if (strategy != EdgeColorSelection.MANUAL)
			colors.clear();
	}

	/**
	 * Update the colour of a given edge.
	 * 
	 * @param edge
	 *            ONDEXRelation
	 */
	public void updateColor(ONDEXRelation edge) {
		switch (strategy) {
		case RELATIONTYPE:
			colors.put(edge, Config.getColorForRelationType(edge.getOfType()));
			break;
		case EVIDENCETYPE:
			Color[] etcolors = new Color[edge.getEvidence().size()];
			int i = 0;
			for (EvidenceType et : edge.getEvidence()) {
				etcolors[i] = Config.getColorForEvidenceType(et);
				i++;
			}
			colors.put(edge, new MultiColorEdgePaint(etcolors));
			break;
		case MANUAL:
			break;
		default:
			colors.put(edge, Config.getDefaultColor());
			break;
		}
	}

	/**
	 * Update the colour of a given edge with a given paint.
	 * 
	 * @param edge
	 *            ONDEXRelation
	 * @param paint
	 *            Paint
	 */
	public void updateColor(ONDEXRelation edge, Paint paint) {
		colors.put(edge, paint);
	}
}
