package net.sourceforge.ondex.ovtk2.annotator.valuegraph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import net.sourceforge.ondex.core.ONDEXConcept;

import org.apache.commons.collections15.Transformer;

/**
 * A node shape which displays data as a line chart. This is the 2D data
 * implementation, i.e. time points to expression values.
 * 
 * @author taubertj
 * @version 01.03.2012
 */
public class LineChartNodeIconTransformer implements
		Transformer<ONDEXConcept, Icon> {

	/**
	 * colour for each series
	 */
	private Map<Integer, Color> colours;

	/**
	 * raw data
	 */
	private Map<Integer, Map<Integer, Map<ONDEXConcept, Double>>> data;

	/**
	 * calculate value range global
	 */
	private boolean globalRange = false;

	/**
	 * icon storage
	 */
	protected Map<ONDEXConcept, Icon> iconMap = new HashMap<ONDEXConcept, Icon>();

	/**
	 * diameter of chart
	 */
	private int targMax = 0;

	/**
	 * enable 3D effect for charts
	 */
	private boolean use3D = false;

	/**
	 * Constructor to use with 2D data.
	 * 
	 * @param data
	 *            2D data of numerical values, category first
	 * @param targMax
	 *            max size range
	 */
	public LineChartNodeIconTransformer(
			Map<Integer, Map<Integer, Map<ONDEXConcept, Double>>> data,
			Map<Integer, Color> colours, int targMax, boolean use3D,
			boolean globalRange) {
		this.data = data;
		this.colours = colours;
		this.targMax = targMax;
		this.use3D = use3D;
		this.globalRange = globalRange;
	}

	/**
	 * Returns the icon storage as a <code>Map</code>.
	 */
	public Map<ONDEXConcept, Icon> getIconMap() {
		return iconMap;
	}

	/**
	 * Sets the icon storage to the specified <code>Map</code>.
	 */
	public void setIconMap(Map<ONDEXConcept, Icon> iconMap) {
		this.iconMap = iconMap;
	}

	@Override
	public Icon transform(ONDEXConcept input) {
		// lazy initialisation of icon map
		if (!iconMap.containsKey(input)) {
			LineChartIcon icon = new LineChartIcon(input, data, colours,
					targMax, use3D, globalRange);
			if (icon.created())
				iconMap.put(input, icon);
			else
				iconMap.put(input, null);
		}
		return iconMap.get(input);
	}
}
