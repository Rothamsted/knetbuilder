package net.sourceforge.ondex.ovtk2.ui.stats;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class is initialized with a dataset, which is basically a collection of
 * numbers, and a resolution parameter which determines the length of the
 * quantification interval. It uses these inputs to compute:
 * <ul>
 * <li>A histogram over the given dataset with the given resolution.</li>
 * <li>The statistical average (mean) of the dataset.</li>
 * <li>The statistical standard deviation of the dataset.</li>
 * <li>The maximal value in the dataset.</li>
 * <li>The minimal value in the dataset.</li>
 * </ul>
 * 
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class Statistic {

	// ####FIELDS####

	/**
	 * debug flag.
	 */
	private final boolean DEBUG = false;

	/**
	 * the histogram.
	 */
	private double[][] histo;

	/**
	 * the computed average.
	 */
	private double average;

	/**
	 * the computed std deviation.
	 */
	private double deviation;

	/**
	 * the computed minimal value.
	 */
	private double min;

	/**
	 * the computed maximal value.
	 */
	private double max;

	/**
	 * the chosen resolution (length of quantification interval).
	 */
	private double resolution;

	// ####CONSTRUCTOR####

	/**
	 * the constructor. all calculations are already triggered from within the
	 * constructor.
	 */
	public Statistic(Collection<Number> c, double resolution) {
		this.resolution = resolution;
		findMinMax(c);
		setupHisto(c);
		calcAverageAndDeviation(c);
		if (DEBUG)
			printHisto();
	}

	// ####METHODS####

	/**
	 * finds the minimal and maximal value in the dataset.
	 */
	private void findMinMax(Collection<Number> c) {
		if (c.size() == 0) {
			min = 0.0;
			max = 1.0;
		} else {
			Iterator<? extends Number> it = c.iterator();
			double n = it.next().doubleValue();
			min = n;
			max = n;
			while (it.hasNext()) {
				n = it.next().doubleValue();
				if (n < min)
					min = n;
				else if (n > max)
					max = n;
			}
		}
	}

	/**
	 * constructs the histogram.
	 * 
	 * @param c
	 */
	private void setupHisto(Collection<Number> c) {
		double a = min / resolution;
		if (DEBUG)
			System.out.println("resolution: " + resolution);
		if (DEBUG)
			System.out.println("min: " + min);
		double his_min = Math.floor(a) * resolution;
		if (DEBUG)
			System.out.println("hismin: " + his_min);

		a = max / resolution;
		if (DEBUG)
			System.out.println("max: " + max);
		double his_max = Math.ceil(a) * resolution;
		if (DEBUG)
			System.out.println("hismax: " + his_max);

		int l = ((int) ((his_max - his_min) / resolution));
		if (DEBUG)
			System.out.println("arraylength: " + l);
		histo = new double[l + 1][2];

		double last_x = his_min;
		if (DEBUG)
			System.out.println("X-Values: ");
		for (int i = 0; i < l + 1; i++) {
			histo[i][0] = last_x;
			if (DEBUG)
				System.out.print(last_x + "  ");
			last_x += resolution;
			histo[i][1] = 0;
		}
		if (DEBUG)
			System.out.println();

		Iterator<Number> it = c.iterator();
		double v;
		int index;
		while (it.hasNext()) {
			v = it.next().doubleValue();
			index = (int) Math.floor(((v - his_min) / (his_max - his_min)) * l);
			histo[index][1]++;
		}
	}

	/**
	 * debug method.
	 */
	private void printHisto() {
		int total = 0;
		for (int i = 0; i < histo.length; i++) {
			System.out.println(histo[i][0] + "\t" + histo[i][1]);
			total += histo[i][1];
		}
		System.out.println("average: " + average + "\tdeviation: " + deviation + "\ttotal values: " + total);
	}

	/**
	 * calculates the average and standard deviation.
	 * 
	 * @param c
	 *            the dataset.
	 */
	private void calcAverageAndDeviation(Collection<Number> c) {
		if (c.size() == 0) {
			average = 0.0;
			deviation = 0.0;
		} else {
			Iterator<Number> it = c.iterator();
			double val;
			average = it.next().doubleValue();
			double i = 1.0;
			while (it.hasNext()) {
				val = it.next().doubleValue();
				average = ((average * i) + val) / (++i);
			}

			it = c.iterator();
			val = it.next().doubleValue();
			deviation = Math.abs(average - val);
			i = 1.0;
			while (it.hasNext()) {
				val = it.next().doubleValue();
				val = Math.abs(average - val);
				deviation = ((deviation * i) + val) / (++i);
			}
			// correction for missing degree of freedom:
			deviation = (deviation * i) / (i - 1.0);
		}
	}

	/**
	 * @return the histogram in the shape of a 2D double[][] array. where
	 *         array[i][0] contains the lower boundary of the interval on the
	 *         x-axis (to get the upper boundary just add the value of the
	 *         resolution) and array[i][1] contains the value on the y-axis ( =
	 *         number of occurrences).
	 */
	public double[][] getHistogramValues() {
		return histo;
	}

	/**
	 * 
	 * @return the computed average.
	 */
	public double getAverage() {
		return average;
	}

	/**
	 * the computed standard deviation.
	 * 
	 * @return
	 */
	public double getStandardDeviation() {
		return deviation;
	}
}
