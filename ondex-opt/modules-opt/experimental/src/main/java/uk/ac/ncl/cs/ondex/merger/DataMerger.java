package uk.ac.ncl.cs.ondex.merger;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

public abstract class DataMerger<D> {
	
	private ONDEXGraph graph;
	
	public DataMerger(ONDEXGraph g) {
		graph = g;
	}
	
	public abstract D extractData(ONDEXConcept c);
	
	public String mergeStrings(Collection<ONDEXConcept> cs) {
		Set<String> set = new TreeSet<String>();
		for (ONDEXConcept c : cs) {
			String data = (String) extractData(c);
			set.add(data);
		}
		if (set.size() == 0) {
			return null;
		} else if (set.size() == 1) {
			return set.iterator().next();
		} else {
			return set.toString();
		}
	}
	
	public Collection<D> mergeData(Collection<ONDEXConcept> cs) {
		Set<D> set = new HashSet<D>();
		for (ONDEXConcept c : cs) {
			D data = extractData(c);
			set.add(data);
		}
		return set;
	}
		
}
