package net.sourceforge.ondex.ovtk2.annotator.microarray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.ovtk2.annotator.microarray.stats.StatsCalc;


/**
 * An indexing tool for microarray data that constructs backwards and forwards indexes and ratios between treatments
 * @author hindlem
 *
 */
public class MicroarrayDataIndex {

	private HashMap<String, HashMap<String, Double>> probe2TreatmentIndex = new HashMap<String, HashMap<String, Double>>();
	private HashMap<String, HashMap<String, Double>> treatment2ProbeIndex = new HashMap<String, HashMap<String, Double>>();
	
	private HashMap<String, Double> probeToLSD = new HashMap<String, Double>();
	
	private HashMap<String, HashMap<String, HashMap<String, Double>>> probe2RatioIndex = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
	
	private HashSet<String> probes = new HashSet<String>();
	private HashSet<String> treatments = new HashSet<String>();

	private StatsCalc allValues = new StatsCalc();
	
	private boolean areLogValues;

	/**
	 * 
	 * @param areLogValues expression data is logged? (makes a difference in calculating ratios)
	 */
	public MicroarrayDataIndex(boolean areLogValues) {
		this.areLogValues = areLogValues;
	}
	
	/**
	 * Method for setting a lsd on a probe
	 * @param probeName the unique probe name
	 * @param lsd the least significant difference (lsd) for this probe on these treatments 
	 */
	public void setLSD(String probeName, Double lsd) {
		probeToLSD.put(probeName.toUpperCase(), lsd);
	}
	
	/**
	 * Overloading for Method for setting values for a probe
	 * @param probeName the unique probe name
	 * @param treatmentToExpression the treatments and there expressions
	 */
	public void setProbe(String probeName, HashMap<String, Double> treatmentToExpression) {
		this.setProbe(probeName, treatmentToExpression, null);
	}
	
	/**
	 * Method for setting values for a probe
	 * @param probeName the unique probe name
	 * @param treatmentToExpression the treatments and there expressions
	 * @param lsd the least significant difference (lsd) for this probe on these treatments
	 */
	public void setProbe(String probeName, HashMap<String, Double> treatmentToExpression, Double lsd ) {
		
		if (lsd != null) {
			probeToLSD.put(probeName.toUpperCase(), lsd);
		}
		
		probes.add(probeName.toUpperCase());
		probe2TreatmentIndex.put(probeName.toUpperCase(), treatmentToExpression);
		
		for(String treatment: treatmentToExpression.keySet()) {
			treatments.add(treatment);
			HashMap<String, Double> probes = treatment2ProbeIndex.get(treatment);
			if (probes == null) {
				probes = new HashMap<String, Double>();
				treatment2ProbeIndex.put(treatment, probes);
			}
			Double expression = treatmentToExpression.get(treatment);
			probes.put(probeName.toUpperCase(), expression);
			
			allValues.enter(expression); //generate stats while we are here
		}
		
		HashMap<String, HashMap<String, Double>> treatment2TreatmentRatio = new HashMap<String, HashMap<String, Double>>();
		for(String fromTreatment: treatmentToExpression.keySet()) {
			HashMap<String, Double> targetTreats = treatment2TreatmentRatio.get(fromTreatment);
			if (targetTreats == null) {
				targetTreats = new HashMap<String, Double>();
				treatment2TreatmentRatio.put(fromTreatment, targetTreats);
			}
			for(String toTreatment: treatmentToExpression.keySet()) {
				
				Double fromExpression = treatmentToExpression.get(fromTreatment);
				Double toExpression = treatmentToExpression.get(toTreatment);
				
				double ratio;
				if (areLogValues) {
					ratio = fromExpression - toExpression;
				} else {
					ratio = fromExpression/toExpression;
				}
				targetTreats.put(toTreatment, ratio);
			}
		}
		
		probe2RatioIndex.put(probeName.toUpperCase(), treatment2TreatmentRatio);
		
	}

	/**
	 * Gets the ration between the specified treatments for this probe
	 * @param probeName the probe name
	 * @param treatmentA the treatment from
	 * @param treatmentB the treatment to
	 * @return the ratio between these treatments
	 */
	public Double getRatio(String probeName, String treatmentA, String treatmentB) {
		return probe2RatioIndex.get(probeName.toUpperCase()).get(treatmentA).get(treatmentB);
	}
	

	/**
	 * 
	 * @return hash index of probe to treatment
	 */
	public Map<String, HashMap<String, Double>> getProbe2TreatmentIndex() {
		return probe2TreatmentIndex;
	}

	/**
	 * 
	 * @return hash index of treatment to probe
	 */
	public Map<String, HashMap<String, Double>> getTreatment2ProbeIndex() {
		return treatment2ProbeIndex;
	}

	/**
	 * Returns a index of ratios
	 * @return the probe to treatment to treatment to ratio (of treatments) index
	 */
	public Map<String, HashMap<String, HashMap<String, Double>>> getProbe2RatioIndex() {
		return probe2RatioIndex;
	}
	
	/**
	 * 
	 * @return all the treament types
	 */
	public Set<String> getTreatments() {
		return treatments;
	}

	/**
	 * 
	 * @return are expression value logged
	 */
	public boolean isAreLogValues() {
		return areLogValues;
	}
	
	/**
	 * 
	 * @return stats on all values entered
	 */
	public StatsCalc getGeneralStats() {
		return allValues;
	}

	public Map<String, Double> getProbeToLSD() {
		return probeToLSD;
	}

	public HashSet<String> getProbes() {
		return probes;
	}

}
