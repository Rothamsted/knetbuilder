package net.sourceforge.ondex.transformer.buildhierarchy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Records the delta statistic for hierarchy buildup run.
 * 
 * @author Jochen Weile, B.Sc.
 *
 */
public class StatsRecorder {

	//####FIELDS####
	
	/**
	 * positive and negative value storages.
	 */
	private int[] plus, minus;
	
	/**
	 * number of calls.
	 */
	private int calls;
	
	/**
	 * the variance.
	 */
	private double variance = -1.0;
	
	/**
	 * instance of this class.
	 */
	private static StatsRecorder instance;

	//####CONSTRUCTOR####
	
	/**
	 * constructor.
	 */
	private StatsRecorder(int size) {
		plus = new int[size];
		minus = new int[size];
		calls = 0;
		for (int i = 0; i < size; i++)
			plus[i] = minus[i] = 0;
	}

	//####METHODS####
	
	/**
	 * gets an instance.
	 */
	public static StatsRecorder getInstance(int size) {
		if (instance == null)
			instance = new StatsRecorder(size);
		return instance;
	}
	
	/**
	 * resets the recorder.
	 */
	public static void reset() {
		instance = null;
	}
	
	/**
	 * records a value.
	 * @param d
	 */
	public void record(double d) {
		if (d != Double.NEGATIVE_INFINITY && d != Double.POSITIVE_INFINITY && d != Double.NaN) {
			int di = ((int)Math.floor(Math.abs(d)));
			if (d < 0.0)
				minus[di]++;
			else
				plus[di]++;
			calls++;
			
			if (variance == -1.0)
				variance = Math.pow(d,2.0);
			else {
				double n = (double)calls;
				variance = ((n-1)/n) * variance + (1/n)*Math.pow(d,2.0);
			}
		}
	}
	
	/**
	 * gets the standard deviation.
	 * @return
	 */
	public double stdev() {
		if (calls < 100)
			return 1.0;
		else
			return Math.sqrt(variance);
	}
	
	/**
	 * checks whether a change of likelihood is acceptable or not.
	 * @param old old value.
	 * @param nu new value
	 * @return acceptability
	 */
	public boolean acceptable(double old, double nu) {
		if (nu >= old)
			return true;
		else {
			double ratio = Math.exp(nu - old);
			double tau;
			if (calls < 1000)
				tau = normalQuantile(1.0 - ratio) * stdev();
			else
				tau = searchThreshold(ratio);
//			System.err.println("ratio = "+ratio+"\ttau = "+tau);
			return (nu - old) > tau;
		}
	}
	
	/**
	 * finds the threshold with numerical integration.
	 * @param pvalue the pvalue to achieve.
	 * @return threshold tau
	 */
	private double searchThreshold(double pvalue) {
		double sum = 0.0;
		for (int i = 10 ; i >= 0; i--) {
			sum += ((double)plus[i])/((double)calls);
			if (sum >= pvalue)
				return (double)i;
		}
		for (int i = 0; i < minus.length; i++) {
			sum += ((double)minus[i])/((double)calls);
			if (sum >= pvalue)
				return (double)(-i - 1);
		}
		return 0.0;
	}
	
	/**
	 * write the histogram to a file in the ondex directory
	 * called delta_statsTIMESTAMP.txt .
	 */
	public void write() {
		int min = minus.length;
		int max = 0;
		for (int i = minus.length-1; i >= 0; i--) {
			if (minus[i] != 0) {
				min = i;
				break;
			}
		}
		for (int i = 0; i < plus.length; i++) {
			if (plus[i] != 0)
				max = i;
		}
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(net.sourceforge.ondex.config.Config.ondexDir+System.getProperty("file.separator")+"delta_stats"+System.currentTimeMillis()+".txt"));
			double dummy;
			double calls_d = (double)calls;
			for (int i = min; i >= 0; i--){
				dummy = ((double)minus[i])/calls_d;
				w.write((-i)+"\t"+dummy+"\n");
			}
			for (int i = 0; i <= max; i++) {
				dummy = ((double)plus[i])/calls_d;
				w.write(i+"\t"+dummy+"\n");
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Lower tail quantile for standard normal distribution function.
	 * 
	 * This function returns an approximation of the inverse cumulative
	 * standard normal distribution function.  I.e., given P, it returns
	 * an approximation to the X satisfying P = Pr{Z <= X} where Z is a
	 * random variable from the standard normal distribution.
	 * 
	 * The algorithm uses a minimax approximation by rational functions
	 * and the result has a relative error whose absolute value is less
	 * than 1.15e-9.
	 * 
	 * @author   Peter J. Acklam, hacked by J. Weile
	 * 
	 * An algorithm with a relative error less than 1.15*10-9 in the entire region.
	 */
	public static double normalQuantile(double p) {
	    // Coefficients in rational approximations
	    double[] a = new double[]{-3.969683028665376e+01,  2.209460984245205e+02,
	                      -2.759285104469687e+02,  1.383577518672690e+02,
	                      -3.066479806614716e+01,  2.506628277459239e+00};

	    double[] b = new double[]{-5.447609879822406e+01,  1.615858368580409e+02,
	                      -1.556989798598866e+02,  6.680131188771972e+01,
	                      -1.328068155288572e+01 };

	    double[] c = new double[]{-7.784894002430293e-03, -3.223964580411365e-01,
	                      -2.400758277161838e+00, -2.549732539343734e+00,
	                      4.374664141464968e+00,  2.938163982698783e+00};

	    double[] d = new double[]{7.784695709041462e-03, 3.224671290700398e-01,
	                       2.445134137142996e+00,  3.754408661907416e+00};

	    // Define break-points.
	    double plow  = 0.02425;
	    double phigh = 1 - plow;

	    // Rational approximation for lower region:
	    if ( p < plow ) {
	             double q  = Math.sqrt(-2*Math.log(p));
	             return (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                             ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for upper region:
	    if ( phigh < p ) {
	             double q  = Math.sqrt(-2*Math.log(1-p));
	             return -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                                    ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for central region:
	    double q = p - 0.5;
	    double r = q*q;
	    return (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q /
	                             (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1);
	}
	
}
