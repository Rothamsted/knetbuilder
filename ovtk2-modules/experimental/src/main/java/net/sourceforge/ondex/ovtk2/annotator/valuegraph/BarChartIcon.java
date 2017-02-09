package net.sourceforge.ondex.ovtk2.annotator.valuegraph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Icon;

import net.sourceforge.ondex.core.ONDEXConcept;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Draws the actual bar chart on an icon. Positive values are in red, negative
 * in green.
 * 
 * @author taubertj
 * @version 01.03.2012
 */
public class BarChartIcon implements Icon {

	// is a real icon?
	private boolean created = false;

	// rendered bar chart
	private BufferedImage image = null;

	/**
	 * Constructor for a set of original and normalised data in context of a
	 * concept.
	 * 
	 * @param concept
	 *            in context of this concept
	 * @param data
	 *            raw data
	 */
	public BarChartIcon(ONDEXConcept concept,
			Map<Integer, Map<Integer, Map<ONDEXConcept, Double>>> data,
			Map<Integer, Color> colours, int targMax, boolean use3D,
			boolean globalRange) {

		// capture global range of values
		double lower = Double.POSITIVE_INFINITY;
		double upper = Double.NEGATIVE_INFINITY;

		// check concept is contained in data
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Integer series : data.keySet()) {
			for (Integer category : data.get(series).keySet()) {
				Map<ONDEXConcept, Double> map = data.get(series).get(category);
				if (map != null && map.containsKey(concept)) {
					created = true;
					dataset.addValue(map.get(concept), series, category);
				} else {
					dataset.addValue(0, series, category);
				}
				// get range over all values
				if (globalRange && map != null) {
					for (Double v : map.values()) {
						if (v < lower)
							lower = v;
						if (v > upper)
							upper = v;
					}
				}
			}
		}

		if (created) {

			// categories are not explicitly labelled
			CategoryAxis domainAxis = new CategoryAxis();
			domainAxis.setTickLabelsVisible(false);
			domainAxis.setTickMarksVisible(false);
			domainAxis.setAxisLineVisible(true);
			domainAxis.setVisible(true);

			// the actual raw value
			ValueAxis rangeAxis = new NumberAxis();
			rangeAxis.setTickLabelsVisible(true);
			rangeAxis.setTickMarksVisible(false);
			rangeAxis.setAxisLineVisible(true);
			rangeAxis.setNegativeArrowVisible(false);
			rangeAxis.setPositiveArrowVisible(false);
			rangeAxis.setVisible(true);
			if (globalRange) {
				rangeAxis.setRangeWithMargins(lower, upper);
			}

			// renderer to set colour for categories
			CategoryItemRenderer renderer;
			if (use3D)
				renderer = new BarRenderer3D();
			else {
				renderer = new BarRenderer();
				((BarRenderer) renderer)
						.setBarPainter(new StandardBarPainter());
				((BarRenderer) renderer).setShadowVisible(false);
			}
			Integer[] sorted = data.keySet().toArray(new Integer[0]);
			Arrays.sort(sorted);
			for (int i = 0; i < sorted.length; i++) {
				renderer.setSeriesPaint(i, colours.get(sorted[i]));
			}

			// plot combines data with axis and renderer
			CategoryPlot plot = new CategoryPlot();
			plot.setDataset(dataset);
			plot.setDomainAxis(domainAxis);
			plot.setDomainGridlinesVisible(false);
			plot.setDomainCrosshairVisible(false);
			plot.setRangeAxis(rangeAxis);
			plot.setRangeGridlinesVisible(false);
			plot.setRangeCrosshairVisible(false);
			plot.setRenderer(renderer);

			// chart without title and legend
			JFreeChart chart = new JFreeChart(null,
					JFreeChart.DEFAULT_TITLE_FONT, plot, false);
			chart.setBorderVisible(true);

			// approximate heuristic for size determination
			int width = 20 + dataset.getColumnCount() * dataset.getRowCount()
					* 10;
			if (width < targMax)
				width = targMax;
			if (width > 2 * targMax)
				width = 2 * targMax;

			// create image to be drawn
			image = chart.createBufferedImage(width, targMax);
		}
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
		if (image != null)
			return image.getHeight();
		return 2;
	}

	@Override
	public int getIconWidth() {
		if (image != null)
			return image.getWidth();
		return 2;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (image != null) {
			g.drawImage(image, x, y, null);
		} else {
			g.setColor(Color.BLACK);
			g.fillRect(x, y, 2, 2);
		}
	}
}
