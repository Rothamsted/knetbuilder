package net.sourceforge.ondex.mapping.tmbased;

import java.util.HashSet;

/**
 * This Class represents a single search query that has been found in a publication.
 * It saves a score for the hit and all sentences of the publication
 * in which the query occurs (evidence).
 * 
 * @author keywan
 *
 */
public class Occurrence {

	private String query;
	
	private HashSet<String> evidence;
	
	private Double score;
	
	
	public Occurrence (String query, Double score) {
		this.query = query;
		this.score = score;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public HashSet<String> getEvidence() {
		return evidence;
	}

	public void setEvidence(HashSet<String> evidence) {
		this.evidence = evidence;
	}



}
