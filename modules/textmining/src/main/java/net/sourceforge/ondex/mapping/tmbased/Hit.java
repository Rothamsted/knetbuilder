package net.sourceforge.ondex.mapping.tmbased;

import java.util.HashSet;

/**
 * This Class represents a concept that is related to the publication due to
 * one or many of his names/synonyms occurring in the text of the publication.
 * A score and evidence sentences indicate the strength of the association 
 * between the publication and this concept. 
 * 
 * @author keywan
 *
 */
public class Hit implements Comparable<Hit>{
	
	private Integer hitConID;
	
	private HashSet<String> evidence;
	
	private HashSet<Occurrence> occurrence;
	
	private Double score;

	
	public Hit(Integer conID) {
		this.hitConID = conID;
		this.evidence = new HashSet<String>();
		this.occurrence = new HashSet<Occurrence>();
		this.score = new Double(0);
	}

	public int getNumberOfOccurrence() {
		return this.occurrence.size();
	}
	
	/**
	 * As a new Occurrence is added, the score of Hit is updated if the 
	 * the new Occurrence has a higher score than the current score
	 * 
	 * @param occ Occurrence
	 */
	public void addOccurrence(Occurrence occ) {
		this.occurrence.add(occ);
		//TODO: Decide which formula is the best choice for concept to publication mapping
		//SUM up tfidf scores of all concept synonyms that matched the publication
		setScore(score + occ.getScore());
		
	}
	
	public boolean addEvidence(String evidence){
		return this.evidence.add(evidence);
	}
	
	public boolean addEvidence(HashSet<String> evidence){
		return this.evidence.addAll(evidence);
	}
	
	public int getNumberOfEvidence(){
		return this.evidence.size();
	}
	
	public Double getScore() {
		return this.score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getHitConID() {
		return hitConID;
	}

	public void setHitConID(Integer hitConID) {
		this.hitConID = hitConID;
	}

	public HashSet<String> getEvidence() {
		return this.evidence;
	}

	public HashSet<Occurrence> getOccurrence() {
		return occurrence;
	}

	public int compareTo(Hit hit) {
		if(hit.getScore() < this.getScore())
			return 1;
		if(hit.getScore() > this.getScore())
			return -1;
		else
			return 0;
	}

}
