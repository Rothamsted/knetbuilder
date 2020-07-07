package net.sourceforge.ondex.algorithm.annotationquality;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class TermEnrichment {
	
	private GOTreeParser goterms;
	
	private GoaIndexer goas;
	
	public TermEnrichment(String goFile, String goaFile, int taxID, String datasource) throws Exception {
		try {
			goterms = new GOTreeParser(goFile);
			goterms.parseOboFile();
		} catch (IOException e) {
			throw new Exception(goFile+" is not valid.", e);
		}
		try {
			goas = new GoaIndexer(goterms);
			goas.parseFileByTaxon(goaFile, taxID, datasource);
		} catch (IOException e) {
			throw new Exception(goaFile+" is not valid", e);
		}
	}
	
	public TermScore[] getEnrichedTerms(String[] genes, int countThreshold, double icThreshold) {
		HashMap<Integer,Integer> termCounter = new HashMap<Integer, Integer>();
		for (String gene : genes) {
			Collection<Integer> terms = goas.getNonRedundantGoaForProtID(gene);
			Set<Integer> superTerms = fetchSuperTerms(terms);
			for (Integer term : superTerms) {
				Integer count = termCounter.get(term);
				if (count == null) {
					count = 1;
				} else {
//					count = new Integer(count.intValue() + 1);
					count++;
				}
				termCounter.put(term, count);
			}
		}
		
		TreeSet<TermScore> result = new TreeSet<TermScore>();
		for (int term : termCounter.keySet()) {
			int count = termCounter.get(term);
			double ic = goas.getInformationContent(term);
			if (count >= countThreshold && ic >= icThreshold) {
				result.add(new TermScore(term, count, ic));
			}
		}
		return result.toArray(new TermScore[result.size()]);
	}

	private Set<Integer> fetchSuperTerms(Collection<Integer> terms) {
		Set<Integer> set = new TreeSet<Integer>();
		for (int term : terms) {
			GoTerm goterm = goterms.getEntry(term);
			if (goterm == null) {
				System.out.println("Ignoring unknown GO term: "+ term);
				continue;
			}
			trace(goterm, set);
		}
		return set;
	}
	
	private void trace(GoTerm goterm, Set<Integer> set) {
		if (goterm.getParents() != null) {
			for (GoTerm parent : goterm.getParents()) {
				set.add(parent.getId());
				trace(parent, set);
			}
		}
	}
	
	public class TermScore implements Comparable<TermScore> {
		private int term;
		private int count;
		private double ic;
		private TermScore(int term, int count, double ic) {
			this.term = term;
			this.count = count;
			this.ic = ic;
		}
		public int getTerm() {
			return term;
		}
		public int getCount() {
			return count;
		}
		public double getIc() {
			return ic;
		}
		public double getScore() {
			return ic * (double)count;
		}
		@Override
		public int compareTo(TermScore o) {
			if (term == o.getTerm()) { 
				return 0;
			}
			
			if (count > o.getCount()) {
				return 1;
			} else if (count < o.getCount()) {
				return -1;
			}
			
			if (ic > o.getIc()) {
				return 1;
			} else if (ic < o.getIc()) {
				return -1;
			} 
			
			if (term > o.getTerm()) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
