package uk.ac.ncl.cs.ondex.merger;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

public abstract class SetMerger<D> {
	
	protected ONDEXGraph graph;
	
	public SetMerger(ONDEXGraph g) {
		graph = g;
	}
	
	public abstract Set<D> extractSet(ONDEXConcept c);
	
	public Collection<D> mergeData(Collection<ONDEXConcept> cs) {
		Set<D> set = new HashSet<D>();
		for (ONDEXConcept c : cs) {
			for (D data : extractSet(c)) {
				set.add(data);
			}
		}
		return set;
	}
}
