package net.sourceforge.ondex.ovtk2.graph;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.custom.MultiColorNodePaint;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Provides a transformation from a given ONDEXConcept to a Color.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXNodeFillPaint implements Transformer<ONDEXConcept, Paint> {

	public static final String ET = "et";

	public static final String DS = "ds";

	public static final String CC = "cc";

	public static final String GRAPH_COLORING_CONCEPT_STRATEGY = "Graph.ColoringConceptStrategy";

	/**
	 * Different colour selection strategies
	 * 
	 * @author taubertj
	 * 
	 */
	public enum NodeFillPaintSelection {
		CONCEPTCLASS, DATASOURCE, EVIDENCETYPE, MANUAL
	}

	/**
	 * contains mapping to paint
	 */
	private final Map<ONDEXConcept, Paint> colors;

	/**
	 * contains mapping to alpha values
	 */
	private final Map<ONDEXConcept, Integer> alphas;

	/**
	 * current PickedInfo
	 */
	private final PickedInfo<ONDEXConcept> pi;

	/**
	 * current colour selection
	 */
	private NodeFillPaintSelection strategy;

	/**
	 * Initialises the colours for the nodes in the graph.
	 * 
	 * @param pi
	 *            PickedInfo<ONDEXNode>
	 */
	public ONDEXNodeFillPaint(PickedInfo<ONDEXConcept> pi) {
		if (pi == null)
			throw new IllegalArgumentException("PickedInfo instance must be non-null");
		this.pi = pi;
		this.colors = LazyMap.decorate(new HashMap<ONDEXConcept, Paint>(), new Factory<Paint>() {
			@Override
			public Paint create() {
				return Config.defaultColor;
			}
		});
		this.alphas = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 255;
			}
		});

		// get colouring strategy from visual.xml
		String s = Config.visual.getProperty(GRAPH_COLORING_CONCEPT_STRATEGY);
		if (s == null)
			strategy = NodeFillPaintSelection.CONCEPTCLASS;
		else if (s.equals(CC))
			strategy = NodeFillPaintSelection.CONCEPTCLASS;
		else if (s.equals(DS))
			strategy = NodeFillPaintSelection.DATASOURCE;
		else if (s.equals(ET))
			strategy = NodeFillPaintSelection.EVIDENCETYPE;
		else
			strategy = NodeFillPaintSelection.CONCEPTCLASS;
	}

	/**
	 * Returning current FillPaintSelection.
	 * 
	 * @return current FillPaintSelection
	 */
	public NodeFillPaintSelection getFillPaintSelection() {
		return strategy;
	}

	/**
	 * Sets the FillPaintSelection to use for the nodes in the graph.
	 * 
	 * @param s
	 *            NodeFillPaintSelection to use
	 */
	public void setFillPaintSelection(NodeFillPaintSelection s) {
		this.strategy = s;
		if (s != NodeFillPaintSelection.MANUAL) {
			if (strategy == NodeFillPaintSelection.CONCEPTCLASS) {
				Config.visual.setProperty(GRAPH_COLORING_CONCEPT_STRATEGY, CC);
			} else if (strategy == NodeFillPaintSelection.DATASOURCE) {
				Config.visual.setProperty(GRAPH_COLORING_CONCEPT_STRATEGY, DS);
			} else if (strategy == NodeFillPaintSelection.EVIDENCETYPE) {
				Config.visual.setProperty(GRAPH_COLORING_CONCEPT_STRATEGY, ET);
			}
			if (!Config.isApplet)
				Config.saveVisual();
		}
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @return Color
	 */
	public Paint transform(ONDEXConcept node) {
		if (pi.isPicked(node)) {
			return Config.nodePickedColor;
		} else {
			if (!colors.containsKey(node))
				updateColor(node);
			return colors.get(node);
		}
	}

	/**
	 * Returns alpha value for node or null.
	 * 
	 * @param node
	 * @return
	 */
	public Integer transformAlpha(ONDEXConcept node) {
		if (alphas.containsKey(node))
			return alphas.get(node);
		else
			return null;
	}

	/**
	 * Update all colours from the graph.
	 */
	public void updateAll() {
		if (strategy != NodeFillPaintSelection.MANUAL)
			colors.clear();
	}

	/**
	 * Update the colour of a given node.
	 * 
	 * @param node
	 *            ONDEXConcept
	 */
	public void updateColor(ONDEXConcept node) {
		Paint paint = null;
		switch (strategy) {
		case DATASOURCE:
			paint = Config.getColorForDataSource(node.getElementOf());
			break;
		case EVIDENCETYPE:
			Color[] etcolors = new Color[node.getEvidence().size()];
			int i = 0;
			for (EvidenceType et : node.getEvidence()) {
				etcolors[i] = Config.getColorForEvidenceType(et);
				i++;
			}
			paint = new MultiColorNodePaint(etcolors);
			break;
		case CONCEPTCLASS:
			paint = Config.getColorForConceptClass(node.getOfType());
			break;
		case MANUAL:
			break;
		default:
			paint = Config.getDefaultColor();
			break;
		}

		// Explicitly set alpha channel of colour
		if (paint instanceof Color && alphas.containsKey(node)) {
			Color color = (Color) paint;
			paint = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphas.get(node));
		}
		updateColor(node, paint);
	}

	/**
	 * Update the Paint of a given node with a given Paint.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @param paint
	 *            Paint
	 */
	public void updateColor(ONDEXConcept node, Paint paint) {
		colors.put(node, paint);
	}

	/**
	 * Update the alpha value of a given node.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @param alpha
	 *            Integer
	 */
	public void updateAlpha(ONDEXConcept node, Integer alpha) {
		alphas.put(node, alpha);
	}
}
