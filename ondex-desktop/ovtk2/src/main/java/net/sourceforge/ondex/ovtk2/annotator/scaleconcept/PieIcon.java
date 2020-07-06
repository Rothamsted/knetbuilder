package net.sourceforge.ondex.ovtk2.annotator.scaleconcept;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Icon;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Draws the actual pie chart on an icon. Positive values are in red, negative
 * in green.
 * 
 * @author taubertj
 * @version 21.05.2008
 */
public class PieIcon implements Icon {

	// is a real icon?
	private boolean created = false;

	// 360 degrees divided by categories
	private double angle;

	// current concept context
	private ONDEXConcept concept;

	// normalised data (size of arc)
	private Map<Integer, Map<ONDEXConcept, Double>> normalized;

	// original data (plus/minus detection)
	private Map<Integer, Map<ONDEXConcept, Double>> data;

	private int numberOfCats = 0;

	private Map<ONDEXConcept, Double> significanceMap;
	private Map<ONDEXConcept, Double> normalizedSignificanceMap;

	/**
	 * Constructor for a set of original and normalised data in context of a
	 * concept.
	 * 
	 * @param concept
	 *            in context of this concept
	 * @param normalized
	 *            normalised data (size of arc)
	 * @param data
	 *            original data (plus/minus detection)
	 * @param significanceMap
	 */
	public PieIcon(ONDEXConcept concept, Map<Integer, Map<ONDEXConcept, Double>> normalized, Map<Integer, Map<ONDEXConcept, Double>> data, Map<ONDEXConcept, Double> significanceMap, Map<ONDEXConcept, Double> normalizedSignificanceMap) {
		this.concept = concept;
		this.normalized = normalized;
		this.data = data;
		this.significanceMap = significanceMap;
		this.normalizedSignificanceMap = normalizedSignificanceMap;

		for (Integer category : normalized.keySet()) {
			Map<ONDEXConcept, Double> map = normalized.get(category);
			if (map != null && map.containsKey(concept)) {
				created = true;
			}
		}
		numberOfCats = normalized.keySet().size();
		// categories map to angle at circle
		angle = 360d / numberOfCats;
	}

	/**
	 * Whether or not there was something drawn.
	 * 
	 * @return boolean
	 */
	public boolean created() {
		return created;
	}

	@Override
	public int getIconHeight() {
		return 2;
	}

	@Override
	public int getIconWidth() {
		return 2;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2d = (Graphics2D) g;

		// iterate over all categories
		Integer[] keys = normalized.keySet().toArray(new Integer[normalized.keySet().size()]);
		Arrays.sort(keys);

		double maxSize = 0;

		for (Integer category : keys) {
			Map<ONDEXConcept, Double> map = normalized.get(category);
			if (map != null && map.containsKey(concept)) {
				// get arc boundaries
				double size = map.get(concept);
				double half = size / 2;
				if (size > maxSize) {
					maxSize = size;
				}

				double correctedAngle = (((numberOfCats - 1) - category) * angle) + (90 + angle);
				if (correctedAngle >= 360d) {
					correctedAngle = correctedAngle - 360d;
				}

				Arc2D arc = new Arc2D.Double(x - half, y - half, size, size, correctedAngle, -angle, Arc2D.PIE);

				// define colour and fill
				if (data.get(category).get(concept) == 0)
					g2d.setColor(Color.YELLOW);
				else if (data.get(category).get(concept) > 0)
					g2d.setColor(Color.RED);
				else if (data.get(category).get(concept) < 0)
					g2d.setColor(Color.GREEN);
				g2d.fill(arc);

				// draw borders
				g2d.setColor(Color.BLACK);
				g2d.draw(arc);
			}
		}

		if (significanceMap != null && normalizedSignificanceMap != null) {
			// Double value = significanceMap.get(conceptId);
			Double size = normalizedSignificanceMap.get(concept);
			if (size != null) {
				// Arc2D circle = new Arc2D.Double(x - (size/2), y - (size/2),
				// size, size,
				// 0, 360, Arc2D.PIE);
				Ellipse2D circle = new Ellipse2D.Double(x - (size / 2), y - (size / 2), size, size);
				g2d.setColor(Color.BLACK);
				Stroke s = g2d.getStroke();
				g2d.setStroke(new BasicStroke(2.0f, // Width
						BasicStroke.CAP_SQUARE, // End cap
						BasicStroke.JOIN_MITER, // Join style
						10.0f, // Miter limit
						new float[] { 16.0f, 20.0f }, // Dash pattern
						0.0f)); // Dash phase);
				g2d.draw(circle);
				g2d.setStroke(s);
			}

		}

		Line2D line = new Line2D.Double(x, y, x, y - ((maxSize / 3) * 2));
		g2d.draw(line);
	}
}
