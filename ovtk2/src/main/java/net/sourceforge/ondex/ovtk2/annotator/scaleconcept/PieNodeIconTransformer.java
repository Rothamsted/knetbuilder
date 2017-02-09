package net.sourceforge.ondex.ovtk2.annotator.scaleconcept;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import net.sourceforge.ondex.core.ONDEXConcept;

import org.apache.commons.collections15.Transformer;

/**
 * A node shape which displays data as a pie chart. This is the 2D data
 * implementation, i.e. time points to expression values.
 * 
 * @author taubertj
 * @version 21.05.2008
 */
public class PieNodeIconTransformer implements Transformer<ONDEXConcept, Icon> {

	/**
	 * original data
	 */
	private Map<Integer, Map<ONDEXConcept, Double>> data = null;

	/**
	 * normalised data 0..1
	 */
	private Map<Integer, Map<ONDEXConcept, Double>> normalized = null;

	/**
	 * icon storage
	 */
	protected Map<ONDEXConcept, Icon> iconMap = new HashMap<ONDEXConcept, Icon>();

	/**
	 * calculate significance
	 */
	private Map<ONDEXConcept, Double> significanceMap;

	/**
	 * normalise significance
	 */
	private Map<ONDEXConcept, Double> normalizedSignificanceMap;

	/**
	 * Constructor to use with 2D data.
	 * 
	 * @param data
	 *            2D data of numerical values, category first
	 * @param significanceMap
	 * @param targMin
	 *            min size range
	 * @param targMax
	 *            max size range
	 * @param absoluteValues
	 *            treat more negative values as greater
	 */
	public PieNodeIconTransformer(Map<Integer, Map<ONDEXConcept, Double>> data, Map<ONDEXConcept, Double> significanceMap, int targMin, int targMax, boolean absoluteValues) {
		this.data = data;
		this.significanceMap = significanceMap;
		if (significanceMap != null) {
			normalizedSignificanceMap = new HashMap<ONDEXConcept, Double>();
		}
		normalize(targMin, targMax, absoluteValues);
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

	/**
	 * Normalise data.
	 * 
	 * @param targMin
	 *            min size range
	 * @param targMax
	 *            max size range
	 * @param absoluteValues
	 *            treat more negative values as greater
	 */
	private void normalize(int targMin, int targMax, boolean absoluteValues) {

		/*
		 * Should be normalised across all categories - otherwise it is
		 * impossible to compare the values!!!!!!! And thats what the pie chart
		 * represenatation implies.
		 * 
		 * //TODO maybe allow the user to select the normalisation through the
		 * menu?
		 * 
		 * // iterate over categories Iterator<Integer> categories =
		 * data.keySet().iterator(); while (categories.hasNext()) { Integer
		 * category = categories.next(); Map<Integer, Double>
		 * normalizedValuesForCategory = new HashMap<Integer, Double>();
		 * 
		 * // get min/max of data double min = Double.MAX_VALUE; double max =
		 * Double.MIN_VALUE;
		 * 
		 * Map<Integer, Double> valuesForCategory = data.get(category);
		 * Iterator<Integer> conceptIds = valuesForCategory.keySet().iterator();
		 * while (conceptIds.hasNext()) { double value =
		 * valuesForCategory.get(conceptIds.next()).doubleValue(); if (value <
		 * min) min = value; if (value > max) max = value; }
		 * 
		 * // process data values conceptIds =
		 * valuesForCategory.keySet().iterator(); while (conceptIds.hasNext()) {
		 * Integer conceptId = conceptIds.next(); // calculate normalised value
		 * double percentBase = (valuesForCategory.get(conceptId).doubleValue()
		 * - min) / max; // make sure they are within the given range
		 * normalizedValuesForCategory.put(conceptId, targMin + (percentBase *
		 * sizeRange)); }
		 * 
		 * normalized.put(category, normalizedValuesForCategory); }
		 */
		int sizeRange = targMax - targMin;

		normalized = new HashMap<Integer, Map<ONDEXConcept, Double>>();

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		double extream = 0;

		// categories correspond to e.g. AttributeNames selected
		for (Integer category : data.keySet()) {
			// get min/max of data
			Map<ONDEXConcept, Double> valuesForCategory = data.get(category);
			for (ONDEXConcept c : valuesForCategory.keySet()) {
				double value = valuesForCategory.get(c);
				if (value < min)
					min = value;
				if (value > max)
					max = value;
			}
		}

		if (Math.abs(min) > extream) {
			extream = Math.abs(min);
		}

		if (max > extream) {
			extream = max;
		}

		for (Integer category : data.keySet()) {
			Map<ONDEXConcept, Double> normalizedValuesForCategory = new HashMap<ONDEXConcept, Double>();
			Map<ONDEXConcept, Double> valuesForCategory = data.get(category);
			for (ONDEXConcept c : valuesForCategory.keySet()) {

				// calculate normalised value
				double percentBase = 0;
				if (absoluteValues) {
					percentBase = Math.abs(valuesForCategory.get(c)) / extream;
				} else {
					percentBase = (valuesForCategory.get(c) - min) / max;
				}
				normalizedValuesForCategory.put(c, targMin + (percentBase * sizeRange));
			}
			normalized.put(category, normalizedValuesForCategory);
		}

		for (ONDEXConcept c : significanceMap.keySet()) {
			Double value = significanceMap.get(c);
			if (value != null) {
				double percentBase;
				if (absoluteValues) {
					percentBase = value / extream;
				} else {
					percentBase = (value - min) / max;
				}
				normalizedSignificanceMap.put(c, targMin + (percentBase * sizeRange));
			}
		}
	}

	@Override
	public Icon transform(ONDEXConcept input) {
		// lazy initialisation of icon map
		if (!iconMap.containsKey(input)) {
			PieIcon icon = new PieIcon(input, normalized, data, significanceMap, normalizedSignificanceMap);
			if (icon.created())
				iconMap.put(input, icon);
			else
				iconMap.put(input, null);
		}
		return iconMap.get(input);
	}
}
