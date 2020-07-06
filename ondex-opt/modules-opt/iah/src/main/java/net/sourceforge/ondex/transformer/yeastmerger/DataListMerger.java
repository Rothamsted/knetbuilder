package net.sourceforge.ondex.transformer.yeastmerger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

public abstract class DataListMerger<D> {
	
	protected ONDEXGraph graph;
	
	public DataListMerger(ONDEXGraph g) {
		graph = g;
	}
	
	public abstract Set<D> extractDataList(ONDEXConcept c);
	
	public Collection<D> mergeData(List<Integer> cids) {
		Set<D> set = new HashSet<D>();
		for (int cid : cids) {
			ONDEXConcept c = graph.getConcept(cid);
			for (D data : extractDataList(c)) {
				set.add(data);
			}
		}
		return set;
	}
}
