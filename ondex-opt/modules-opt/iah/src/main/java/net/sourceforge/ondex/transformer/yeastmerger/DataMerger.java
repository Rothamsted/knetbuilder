package net.sourceforge.ondex.transformer.yeastmerger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
	
	public String mergeStrings(List<Integer> cids) {
		Set<String> set = new TreeSet<String>();
		for (int cid : cids) {
			ONDEXConcept c = graph.getConcept(cid);
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
	
	public Collection<D> mergeData(List<Integer> cids) {
		Set<D> set = new HashSet<D>();
		for (int cid : cids) {
			ONDEXConcept c = graph.getConcept(cid);
			D data = extractData(c);
			set.add(data);
		}
		return set;
	}
		
}
