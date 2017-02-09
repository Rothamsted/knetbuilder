package net.sourceforge.ondex.ovtk2.filter.scatterplot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Filter to select a region in a scatter plot of two attribute values against
 * each other.
 * 
 * @author taubertj
 * 
 */
public class ScatterPlotFilter extends OVTK2Filter implements ActionListener,
		ChangeListener {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * First axis attribute
	 */
	JComboBox attributes1 = new JComboBox();

	/**
	 * Second axis attribute
	 */
	JComboBox attributes2 = new JComboBox();

	/**
	 * the go button
	 */
	JButton button;

	/**
	 * Panel for future chart
	 */
	ChartPanel chartPanel;

	/**
	 * current marker values
	 */
	double currentXMax, currentXMin, currentYMax, currentYMin;

	/**
	 * filter has been used
	 */
	private boolean used = false;

	/**
	 * Marker on the chart
	 */
	Marker xMaxMarker, xMinMarker, yMaxMarker, yMinMarker;

	/**
	 * x max slider
	 */
	JSlider xMaxSlider = new JSlider(0, 1000, 1000);

	/**
	 * min and max dat values
	 */
	double xMaxValue, xMinValue, yMaxValue, yMinValue;

	/**
	 * x min slider
	 */
	JSlider xMinSlider = new JSlider(0, 1000, 0);

	/**
	 * y max slider
	 */
	JSlider yMaxSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 1000);

	/**
	 * y min slider
	 */
	JSlider yMinSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);

	/**
	 * Default constructor
	 * 
	 * @param viewer
	 */
	public ScatterPlotFilter(OVTK2Viewer viewer) {
		super(viewer);
		initGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// update to combo boxes
		if (e.getSource() == attributes1 || e.getSource() == attributes2) {
			// check that attribute is selected and not NONE
			if (!(attributes1.getSelectedItem() instanceof String)
					&& !(attributes2.getSelectedItem() instanceof String)) {
				populateData();
				button.setEnabled(true);
				xMaxSlider.setEnabled(true);
				xMinSlider.setEnabled(true);
				yMaxSlider.setEnabled(true);
				yMinSlider.setEnabled(true);
			} else {
				// empty attribute selected
				chartPanel.setChart(null);
				button.setEnabled(false);
				xMaxSlider.setEnabled(false);
				xMinSlider.setEnabled(false);
				yMaxSlider.setEnabled(false);
				yMinSlider.setEnabled(false);
			}
		}

		// filter button
		else {

			// iterate over all concepts
			for (ONDEXConcept c : graph.getConcepts()) {
				Attribute attr1 = c.getAttribute((AttributeName) attributes1
						.getSelectedItem());
				Attribute attr2 = c.getAttribute((AttributeName) attributes2
						.getSelectedItem());

				// only concepts with both attributes remain visible
				if (attr1 != null && attr2 != null) {
					double x = ((Number) attr1.getValue()).doubleValue();
					double y = ((Number) attr2.getValue()).doubleValue();

					// find hit concept and make visible
					if (x < currentXMax && x > currentXMin && y < currentYMax
							&& y > currentYMin) {
						graph.setVisibility(c, true);
					} else {
						graph.setVisibility(c, false);
					}
				} else {
					graph.setVisibility(c, false);
				}
			}

			used = true;
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.ScatterPlot");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

	/**
	 * Construct GUI
	 */
	private void initGUI() {

		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);

		// attribute name selection panel
		JPanel selectionPanel = new JPanel();
		BoxLayout layout = new BoxLayout(selectionPanel, BoxLayout.PAGE_AXIS);
		selectionPanel.setLayout(layout);
		this.add(selectionPanel, BorderLayout.NORTH);

		// first axis panel
		JPanel panel1 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel1.add(new JLabel("x-axis attribute:"));
		panel1.add(attributes1);
		selectionPanel.add(panel1);

		// second axis panel
		JPanel panel2 = new JPanel();
		panel2.setBackground(Color.WHITE);
		panel2.add(new JLabel("y-axis attribute:"));
		panel2.add(attributes2);
		selectionPanel.add(panel2);

		// populate combo boxes
		attributes1.addItem("NONE");
		attributes2.addItem("NONE");
		for (AttributeName an : graph.getMetaData().getAttributeNames()) {
			if (graph.getConceptsOfAttributeName(an).size() > 0
					&& Number.class.isAssignableFrom(an.getDataType())
					&& !AppearanceSynchronizer.attr.contains(an.getId())) {
				attributes1.addItem(an);
				attributes2.addItem(an);
			}
		}

		// listen for events
		attributes1.addActionListener(this);
		attributes2.addActionListener(this);

		// configure chart drawing
		chartPanel = new ChartPanel(null, true);
		chartPanel.setBackground(Color.WHITE);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMinimumDrawHeight(10);
		chartPanel.setMaximumDrawHeight(2000);
		chartPanel.setMinimumDrawWidth(20);
		chartPanel.setMaximumDrawWidth(2000);

		// wrap slider controls too
		JPanel center = new JPanel(new BorderLayout());
		center.add(chartPanel, BorderLayout.CENTER);

		// configure sliders
		xMinSlider.addChangeListener(this);
		xMaxSlider.addChangeListener(this);
		yMinSlider.addChangeListener(this);
		yMaxSlider.addChangeListener(this);
		xMinSlider.setBackground(Color.WHITE);
		xMaxSlider.setBackground(Color.WHITE);
		yMinSlider.setBackground(Color.WHITE);
		yMaxSlider.setBackground(Color.WHITE);
		xMaxSlider.setEnabled(false);
		xMinSlider.setEnabled(false);
		yMaxSlider.setEnabled(false);
		yMinSlider.setEnabled(false);

		// left slider panel
		JPanel left = new JPanel(new GridLayout(1, 2));
		left.add(yMinSlider);
		left.add(yMaxSlider);
		center.add(left, BorderLayout.WEST);

		// bottom slider panel
		JPanel bottom = new JPanel(new GridLayout(2, 1));
		bottom.add(xMinSlider);
		bottom.add(xMaxSlider);
		center.add(bottom, BorderLayout.SOUTH);

		this.add(center, BorderLayout.CENTER);

		// the go button
		button = new JButton("Filter");
		button.setEnabled(false);
		button.addActionListener(this);
		this.add(button, BorderLayout.SOUTH);
	}

	/**
	 * Populates the data array with attributes.
	 */
	private void populateData() {
		// get user selection
		AttributeName an1 = (AttributeName) attributes1.getSelectedItem();
		AttributeName an2 = (AttributeName) attributes2.getSelectedItem();

		// pre-set min and max correctly
		xMaxValue = Double.NEGATIVE_INFINITY;
		xMinValue = Double.POSITIVE_INFINITY;
		yMaxValue = Double.NEGATIVE_INFINITY;
		yMinValue = Double.POSITIVE_INFINITY;

		// create a dataset...
		final XYSeries series = new XYSeries(an1.getId() + " & " + an2.getId());
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute attr1 = c.getAttribute(an1);
			Attribute attr2 = c.getAttribute(an2);
			if (attr1 != null && attr2 != null) {
				Number x = (Number) attr1.getValue();
				Number y = (Number) attr2.getValue();
				series.add(x, y);
				// track min and max values
				if (x.doubleValue() < xMinValue)
					xMinValue = x.doubleValue();
				if (x.doubleValue() > xMaxValue)
					xMaxValue = x.doubleValue();
				if (y.doubleValue() < yMinValue)
					yMinValue = y.doubleValue();
				if (y.doubleValue() > yMaxValue)
					yMaxValue = y.doubleValue();
			}
		}

		final XYDataset data = new XYSeriesCollection(series);

		// create a scatter chart...
		final boolean withLegend = false;
		final JFreeChart chart = ChartFactory.createScatterPlot("Scatter plot",
				an1.getId(), an2.getId(), data, PlotOrientation.VERTICAL,
				withLegend, true, false);

		// make bigger dots
		final XYPlot plot = chart.getXYPlot();
		XYDotRenderer render = new XYDotRenderer();
		render.setDotWidth(2);
		render.setDotHeight(2);
		plot.setRenderer(render);

		// add xMax
		xMaxMarker = new ValueMarker(xMaxValue);
		xMaxMarker.setPaint(Color.green);
		plot.addDomainMarker(xMaxMarker);
		currentXMax = xMaxValue;

		// add xMin
		xMinMarker = new ValueMarker(xMinValue);
		xMinMarker.setPaint(Color.red);
		plot.addDomainMarker(xMinMarker);
		currentXMin = xMinValue;

		// add yMax
		yMaxMarker = new ValueMarker(yMaxValue);
		yMaxMarker.setPaint(Color.green);
		plot.addRangeMarker(yMaxMarker);
		currentYMax = yMaxValue;

		// add yMin
		yMinMarker = new ValueMarker(yMinValue);
		yMinMarker.setPaint(Color.red);
		plot.addRangeMarker(yMinMarker);
		currentYMin = yMinValue;

		// set chart to panel
		chartPanel.setChart(chart);
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		// check if chart is present
		JFreeChart chart = chartPanel.getChart();
		if (chart == null)
			return;

		XYPlot plot = chart.getXYPlot();

		// calculate range
		double xRange = xMaxValue - xMinValue;
		double yRange = yMaxValue - yMinValue;

		if (e.getSource() == xMaxSlider) {
			// calculate new values
			double xMaxSet = xMinValue + (double) xMaxSlider.getValue()
					/ 1000.0 * xRange;

			// add xMax
			plot.removeDomainMarker(xMaxMarker);
			xMaxMarker = new ValueMarker(xMaxSet);
			xMaxMarker.setPaint(Color.green);
			plot.addDomainMarker(xMaxMarker);
			currentXMax = xMaxSet;
		}

		else if (e.getSource() == xMinSlider) {
			// calculate new values
			double xMinSet = xMinValue + (double) xMinSlider.getValue()
					/ 1000.0 * xRange;

			// add xMin
			plot.removeDomainMarker(xMinMarker);
			xMinMarker = new ValueMarker(xMinSet);
			xMinMarker.setPaint(Color.red);
			plot.addDomainMarker(xMinMarker);
			currentXMin = xMinSet;
		}

		else if (e.getSource() == yMaxSlider) {
			// calculate new values
			double yMaxSet = yMinValue + (double) yMaxSlider.getValue()
					/ 1000.0 * yRange;

			// add yMax
			plot.removeRangeMarker(yMaxMarker);
			yMaxMarker = new ValueMarker(yMaxSet);
			yMaxMarker.setPaint(Color.green);
			plot.addRangeMarker(yMaxMarker);
			currentYMax = yMaxSet;
		}

		else if (e.getSource() == yMinSlider) {
			// calculate new values
			double yMinSet = yMinValue + (double) yMinSlider.getValue()
					/ 1000.0 * yRange;

			// add yMin
			plot.removeRangeMarker(yMinMarker);
			yMinMarker = new ValueMarker(yMinSet);
			yMinMarker.setPaint(Color.red);
			plot.addRangeMarker(yMinMarker);
			currentYMin = yMinSet;
		}
	}
}
