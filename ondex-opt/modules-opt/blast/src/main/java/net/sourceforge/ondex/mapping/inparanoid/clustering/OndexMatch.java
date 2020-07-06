package net.sourceforge.ondex.mapping.inparanoid.clustering;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * This is a match between two concepts from ONDEX.
 * 
 * @author taubertj
 * 
 */
public class OndexMatch implements Comparable<OndexMatch> {

	// query concept
	private ONDEXConcept query;

	private String queryTaxId = "0";

	// target concept
	private ONDEXConcept target;

	private String targetTaxId = "0";

	// score between the two concepts
	private double score;

	@Override
	public int compareTo(OndexMatch m) {
		return (int) (m.score - score);
	}

	/**
	 * @return the query
	 */
	public ONDEXConcept getQuery() {
		return query;
	}

	/**
	 * @return the queryTaxId
	 */
	public String getQueryTaxId() {
		return queryTaxId;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the target
	 */
	public ONDEXConcept getTarget() {
		return target;
	}

	/**
	 * @return the targetTaxId
	 */
	public String getTargetTaxId() {
		return targetTaxId;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(ONDEXConcept query) {
		this.query = query;
	}

	/**
	 * @param queryTaxId
	 *            the queryTaxId to set
	 */
	public void setQueryTaxId(String queryTaxId) {
		this.queryTaxId = queryTaxId;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(ONDEXConcept target) {
		this.target = target;
	}

	/**
	 * @param targetTaxId
	 *            the targetTaxId to set
	 */
	public void setTargetTaxId(String targetTaxId) {
		this.targetTaxId = targetTaxId;
	}

}
