package net.sourceforge.ondex.algorithm.entropy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In physics, the word entropy has important physical implications as the
 * amount of "disorder" of a system.
 * 
 * @author taubertj
 * 
 */
public class ShannonEntropy {

	public static Double calculateShannonEntropy(List<String> values) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		// count the occurrences of each value
		for (String sequence : values) {
			if (!map.containsKey(sequence)) {
				map.put(sequence, 0);
			}
			map.put(sequence, map.get(sequence) + 1);
		}

		// calculate the entropy
		Double result = 0.0;
		for (String sequence : map.keySet()) {
			Double frequency = (double) map.get(sequence) / values.size();
			result -= frequency * (Math.log(frequency) / Math.log(2));
		}

		return result;
	}

}
