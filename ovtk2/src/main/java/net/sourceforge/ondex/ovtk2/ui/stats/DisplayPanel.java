package net.sourceforge.ondex.ovtk2.ui.stats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.ui.RectangleInsets;

/**
 * this is the panel of the statistics module that displays the results of the
 * analysis, i.e. the number of relevant concepts the number of relevant
 * relations, the average value, the standard deviation and the histogram for
 * the dataset. It also provides a Slider that allows for the adjustment of the
 * quantification interval length (i.e. the resolution).
 * 
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class DisplayPanel extends JPanel implements ChangeListener {

	// ####FIELDS####
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -6781103355192706590L;

	/**
	 * the chart object for the histogram.
	 */
	private JFreeChart chart;

	/**
	 * labels for the number of concepts, number of relations, average and std
	 * deviation.
	 */
	private JLabel num_c, num_r, aver, deviat;

	/**
	 * the data extractor object.
	 */
	private DataExtractor extractor;

	/**
	 * the resolution slider.
	 */
	private JSlider resolution;

	/**
	 * the panel that holds the chart object.
	 */
	private ChartPanel chartPanel;

	// ####CONSTRUCTOR####

	/**
	 * constructor. sets up the standard layout and adds all components.
	 */
	public DisplayPanel(DataExtractor extractor) {
		this.extractor = extractor;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(engl("Statistic")));

		num_c = new JLabel();
		num_r = new JLabel();
		aver = new JLabel();
		deviat = new JLabel();

	}

	// ####METHODS####

	private String engl(String s) {
		return Config.language.getProperty("Statistics." + s);
	}

	/**
	 * updates the complete analysis. all values are recalculated and the
	 * results displayed. the analysis runs in a separate thread that is
	 * monitored by a progressbar, which is displayed instead of the chart until
	 * the computation is done.
	 */
	public void updateWholeStatistics() {
		removeAll();
		setLayout(new BorderLayout());
		final JProgressBar progressbar = new JProgressBar();
		int h = (this.getHeight() - 50) / 2;
		add(Box.createVerticalStrut(h), BorderLayout.NORTH);
		add(Box.createVerticalStrut(h), BorderLayout.SOUTH);
		add(Box.createHorizontalStrut(20), BorderLayout.WEST);
		add(Box.createHorizontalStrut(20), BorderLayout.EAST);
		add(progressbar, BorderLayout.CENTER);

		revalidate();
		repaint();

		Thread worker = new Thread("stats calculator - Timestamp " + System.currentTimeMillis()) {
			public void run() {
				// try {Thread.sleep(500);} catch (InterruptedException e) {}
				extractor.extract();
				chart = createChart(createDataset());
				displayResult();
			}
		};
		worker.start();

		Thread feeder = new Thread("progressbar feeder - Timestamp " + System.currentTimeMillis()) {
			public void run() {
				while (!Monitorable.STATE_TERMINAL.equals(extractor.getState())) {
					progressbar.setMaximum(extractor.getMaxProgress());
					progressbar.setMinimum(extractor.getMinProgress());
					progressbar.setValue(extractor.getProgress());
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		feeder.start();

	}

	/**
	 * displays the results of the analysis.
	 */
	private void displayResult() {
		if (Monitorable.STATE_TERMINAL.equals(extractor.getState())) {
			removeAll();
			JPanel propertiesPanel = new JPanel(new GridLayout(2, 2, 20, 5));
			propertiesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			num_c.setHorizontalAlignment(JLabel.CENTER);
			propertiesPanel.add(num_c);
			num_r.setHorizontalAlignment(JLabel.CENTER);
			propertiesPanel.add(num_r);
			aver.setHorizontalAlignment(JLabel.CENTER);
			propertiesPanel.add(aver);
			deviat.setHorizontalAlignment(JLabel.CENTER);
			propertiesPanel.add(deviat);

			add(propertiesPanel, BorderLayout.NORTH);

			chartPanel = new ChartPanel(chart);
			chartPanel.setMouseZoomable(true, false);
			chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			add(chartPanel, BorderLayout.CENTER);

			resolution = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
			resolution.setToolTipText("length of quantification interval.");
			resolution.addChangeListener(this);
			resolution.setMajorTickSpacing(25);
			resolution.setMinorTickSpacing(5);
			resolution.setPaintTicks(true);
			resolution.setPaintLabels(false);
			JPanel sliderpanel = new JPanel(new BorderLayout());
			sliderpanel.add(new JLabel(engl("Resolution")), BorderLayout.WEST);
			sliderpanel.add(resolution, BorderLayout.CENTER);
			add(sliderpanel, BorderLayout.SOUTH);

			revalidate();
			repaint();
		}
	}

	/**
	 * creates a new chart object and replaces the old one.
	 */
	private void updateChart() {
		chart = createChart(createDataset());

		chartPanel.setChart(chart);

		repaint();
	}

	/**
	 * called whenever the slider was moved. it then reads out the desired
	 * resolution and repeats the analysis.
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			updateChart();
		}
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            a dataset.
	 * 
	 * @return A chart.
	 */
	private JFreeChart createChart(SimpleHistogramDataset dataset) {
		chart = ChartFactory.createHistogram(null, engl("Value"), engl("Histogram"), dataset, PlotOrientation.VERTICAL, false, true, false);

		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.getRenderer().setSeriesPaint(0, new Color(0x7f9f51));
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		return chart;
	}

	/**
	 * performs the data extraction and quantification.
	 * 
	 * @return a histogram dataset.
	 */
	private SimpleHistogramDataset createDataset() {
		Statistic statistic = new Statistic(extractor.getDataSet(), getResolution());
		setAverage(statistic.getAverage());
		setDeviation(statistic.getStandardDeviation());
		setNumConcepts(extractor.getNumConcepts());
		setNumRelations(extractor.getNumRelations());

		SimpleHistogramDataset set = new SimpleHistogramDataset(extractor.getVariableName());
		SimpleHistogramBin bin;
		double[][] his = statistic.getHistogramValues();
		double x_start, x_end, y;
		for (int i = 0; i < his.length; i++) {
			x_start = his[i][0];
			x_end = his[i][0] + getResolution();
			y = his[i][1];
			bin = new SimpleHistogramBin(x_start, x_end, true, false);
			bin.setItemCount((int) y);
			set.addBin(bin);
		}

		return set;
	}

	/**
	 * @return the length of the quantification interval as chosen by the user.
	 */
	private double getResolution() {
		double selectedVal;
		if (resolution != null)
			selectedVal = (double) resolution.getValue();
		else
			selectedVal = 25.0;
		double max = extractor.getMaximalValue().doubleValue();
		double min = extractor.getMinimalValue().doubleValue();
		double range = max - min;
		double pot = 2.0 * (selectedVal / 100.0);
		double perc = Math.pow(10.0, pot) - 1;
		return (perc / 100.0) * range;
	}

	/**
	 * sets the average label to the given value.
	 * 
	 * @param a
	 *            the average.
	 */
	private void setAverage(double a) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		aver.setText("<html> Mean (&mu;) = " + nf.format(a) + "</html>");
	}

	/**
	 * sets the deviation label to the given value.
	 * 
	 * @param d
	 *            the deviation.
	 */
	private void setDeviation(double d) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		deviat.setText("<html>Standard Deviation (&sigma;) = " + nf.format(d) + "</html>");
	}

	/**
	 * sets the concept number label to the given value
	 * 
	 * @param c
	 *            number of concepts.
	 */
	private void setNumConcepts(int c) {
		num_c.setText("Number of Concepts (|C|) = " + c);
	}

	/**
	 * sets the number of relations label to the given value.
	 * 
	 * @param r
	 *            the number of relations.
	 */
	private void setNumRelations(int r) {
		num_r.setText("Number of Relations (|R|) = " + r);
	}

}
