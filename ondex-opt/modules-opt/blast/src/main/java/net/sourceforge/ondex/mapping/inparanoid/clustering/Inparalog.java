package net.sourceforge.ondex.mapping.inparanoid.clustering;

import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Represents a Inparalog, which is the smallest unit in INPARANOID for a
 * concept.
 * 
 * @author taubertj
 * 
 */
public class Inparalog implements Comparable<Inparalog> {

	// contained concept and taxid
	private ONDEXConcept concept = null;

	// taxid for this inparalog
	private String taxid = "0";

	// confidence for this inparalog in ortholog group
	private double confidence = 0;

	/**
	 * Constructor sets internal variables.
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @param taxid
	 *            String
	 */
	public Inparalog(ONDEXConcept c, String taxid) {
		this.concept = c;
		this.taxid = taxid;
	}

	/**
	 * Method for order of Inparalogs.
	 * 
	 * @param inpara
	 *            Inparalog
	 * @return int
	 */
	public int compareTo(Inparalog inpara) {
		// sort by 2 digits after .
		return (int) (inpara.confidence * 100 - confidence * 100);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Inparalog) {
			Inparalog inpara = (Inparalog) o;
			return inpara.concept.equals(this.concept);
		} else
			return false;
	}

	/**
	 * @return the concept
	 */
	public ONDEXConcept getConcept() {
		return concept;
	}

	/**
	 * @return the confidence
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * @return the taxid
	 */
	public String getTaxid() {
		return taxid;
	}

	@Override
	public int hashCode() {
		return concept.hashCode();
	}

	/**
	 * @param concept
	 *            the concept to set
	 */
	public void setConcept(ONDEXConcept concept) {
		this.concept = concept;
	}

	/**
	 * @param confidence
	 *            the confidence to set
	 */
	public void setConfidence(double confidence) {
		if (confidence < 0)
			this.confidence = 0;
		else
			this.confidence = confidence;
	}

	/**
	 * @param taxid
	 *            the taxid to set
	 */
	public void setTaxid(String taxid) {
		this.taxid = taxid;
	}

	@Override
	public String toString() {
		return concept.getPID() + "=" + (int) confidence;
	}
}
