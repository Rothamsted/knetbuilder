package net.sourceforge.ondex.ovtk2.reusable_functions;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeDrawPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Useful reusable functions for annotation of graphs in OVTK.
 * 
 * @author lysenkoa
 * 
 */

public class Annotation {

	private static final Color[] rainbow = new Color[] { new Color(160, 32, 240), Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED };

	/**
	 * Creates a colour scale from the colours specified and annotates the graph
	 * with it If no colours are specified , rainbow scale is used by default,
	 * if one colour specified white starting colour is used.
	 * 
	 * @param viewer
	 *            - ovtk viewer
	 * @param map
	 *            - concept to value map
	 * @param colors
	 *            - colour that will be included in the colour scale.
	 */
	public static final void annotateOnColorScaleFill(OVTK2PropertiesAggregator viewer, Map<ONDEXConcept, Double> map, double[] minmax, Color... colors) {
		if (minmax == null)
			minmax = findMinMaxInSet(map.values());
		double range = minmax[1] - minmax[0];
		range = Math.abs(range);
		if (colors == null || colors.length == 0)
			colors = rainbow;

		// transform colour of nodes
		ONDEXNodeFillPaint colorManager = viewer.getNodeColors();
		colorManager.setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.MANUAL);

		double subrange = range / colors.length;
		for (Entry<ONDEXConcept, Double> ent : map.entrySet()) {
			int pos = (int) (Math.abs(ent.getValue()) / subrange);
			Color newColor = null;
			if (pos < colors.length - 1)
				newColor = getColorRatio(colors[pos], colors[pos + 1], (Math.abs(ent.getValue()) - pos * subrange) / subrange);
			else
				newColor = colors[colors.length - 1];
			colorManager.updateColor(ent.getKey(), newColor);
		}
	}

	/**
	 * Creates a colour scale from the colours specified and annotates the graph
	 * with it If no colours are specified , rainbow scale is used by default,
	 * if one colour specified white starting colour is used.
	 * 
	 * @param viewer
	 *            - ovtk viewer
	 * @param map
	 *            - concept to value map
	 * @param colors
	 *            - colour that will be included in the colour scale.
	 */
	public static final void annotateOnColorScaleDraw(OVTK2PropertiesAggregator viewer, Map<ONDEXConcept, Double> map, double[] minmax, Color... colors) {
		if (minmax == null)
			minmax = findMinMaxInSet(map.values());
		double range = minmax[1] - minmax[0];
		if (colors == null || colors.length == 0)
			colors = rainbow;

		// transform colour of nodes
		ONDEXNodeDrawPaint colorManager = viewer.getNodeDrawPaint();
		colorManager.setDrawPaintSelection(ONDEXNodeDrawPaint.NodeDrawPaintSelection.MANUAL);

		double subrange = range / colors.length;
		for (Entry<ONDEXConcept, Double> ent : map.entrySet()) {
			int pos = (int) (ent.getValue() / subrange);
			Color newColor = null;
			if (pos < colors.length - 1)
				newColor = colors[pos];
			else
				newColor = colors[colors.length - 1];
			colorManager.updateColor(ent.getKey(), newColor);
		}
	}

	/**
	 * Set s collection of nodes to particular fill colour
	 * 
	 * @param viewer
	 * @param cs
	 * @param col
	 */
	public static final void setNodeFillColors(OVTK2PropertiesAggregator viewer, Collection<ONDEXConcept> cs, Color col) {
		ONDEXNodeFillPaint colorManager = viewer.getNodeColors();
		colorManager.setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.MANUAL);
		for (ONDEXConcept c : cs) {
			colorManager.updateColor(c, col);
		}
	}

	/**
	 * Set s collection of nodes to particular draw colour
	 * 
	 * @param viewer
	 * @param cs
	 * @param col
	 */
	public static final void setNodeDrawColors(OVTK2PropertiesAggregator viewer, Collection<ONDEXConcept> cs, Color col) {
		ONDEXNodeDrawPaint colorManager = viewer.getNodeDrawPaint();
		colorManager.setDrawPaintSelection(ONDEXNodeDrawPaint.NodeDrawPaintSelection.MANUAL);
		for (ONDEXConcept c : cs) {
			colorManager.updateColor(c, col);
		}
	}

	/**
	 * Sets the colour of a concept
	 * 
	 * @param viewer
	 * @param c
	 * @param color
	 */
	public static final void setColor(OVTK2PropertiesAggregator viewer, ONDEXConcept c, Color color) {
		ONDEXNodeFillPaint colorManager = viewer.getNodeColors();
		colorManager.setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.MANUAL);
		colorManager.updateColor(c, color);
	}

	/**
	 * Sets the colour of the relation
	 * 
	 * @param viewer
	 * @param r
	 * @param color
	 */
	public static final void setColor(OVTK2PropertiesAggregator viewer, ONDEXRelation r, Color color) {
		ONDEXEdgeColors colorManager = viewer.getEdgeColors();
		colorManager.setEdgeColorSelection(ONDEXEdgeColors.EdgeColorSelection.MANUAL);
		colorManager.updateColor(r, color);
	}

	/**
	 * Creates size scale between the min and max specified and annotates the
	 * graph with it
	 * 
	 * @param viewer
	 *            - OVTK2PropertiesAggregator
	 * @param map
	 *            - concept to value map
	 * @param minSize
	 *            - minimum size of nodes
	 * @param maxSize
	 *            - maximum size of nodes
	 */
	public static final void annotateOnSizeScale(OVTK2PropertiesAggregator viewer, Map<ONDEXConcept, Double> map, int minSize, int maxSize, boolean inverse) {

		// update graphs node shapes
		ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
		double[] minmax = findMinMaxInSet(map.values());
		double range = minmax[1] - minmax[0];
		int sizeRange = maxSize - minSize;

		// processed values for node sizes
		final Map<ONDEXConcept, Integer> amplification = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return Config.defaultNodeSize;
			}
		});

		// scale node size according to percentage
		for (ONDEXConcept concept : map.keySet()) {
			double percentBase = map.get(concept) / range;
			// this is the base size
			if (percentBase == 0) {
				amplification.put(concept, (int) Math.floor(minSize + (sizeRange / 2)));
			}
			// this is the case when there is only one value and range is zero
			if (percentBase > 1) {
				percentBase = 1;
			}
			if (inverse)
				percentBase = 1 - percentBase;
			// actual new size
			double width = minSize + (percentBase * sizeRange);
			if (width <= 0)
				width = 1;
			amplification.put(concept, (int) Math.floor(width));
		}

		// set transformer for node sizes
		nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				return amplification.get(input);
			}
		});
		nodeShapes.updateAll();
	}

	/**
	 * Adjusts the colour on the graph in the direction of the colour specified
	 */
	public static final void BlendOnColorScale() {

	}

	/**
	 * Computes the intermediate colour between the two colours specified,
	 * blended using the ratio
	 * 
	 * @param c1
	 * @param c2
	 * @param ratio
	 * @return colour
	 */
	public static final Color getColorRatio(Color c1, Color c2, double ratio) {
		if (ratio > 1)
			ratio = 1d;
		if (ratio < 0)
			ratio = 0d;
		int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
		int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
		int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
		return new Color(red, green, blue);
	}

	/**
	 * Produce a colour on a rainbow scale given the ratio.
	 * 
	 * @param ratio
	 *            number from 0-1
	 * @return colour from a seven-colour range (from purple to red)
	 */
	public static final Color getRainbowColor(double ratio) {
		if (ratio > 1)
			ratio = 1d;
		if (ratio < 0)
			ratio = 0d;
		double colorRange = 1d / ((double) (rainbow.length - 1));
		double subrange = ratio % colorRange;
		int colorSelection = (int) (ratio / colorRange);
		if (subrange == 0d || colorSelection == rainbow.length - 1)
			return rainbow[colorSelection];
		return getColorRatio(rainbow[colorSelection], rainbow[colorSelection + 1], subrange / colorRange);
	}

	/**
	 * Finds min and max in set of doubles
	 * 
	 * @param set
	 *            to process
	 * @return array[min][max] of the set
	 */
	public static final double[] findMinMaxInSet(Collection<Double> set) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (Double value : set) {
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}
		return new double[] { min, max };
	}

}
