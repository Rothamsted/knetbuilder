package net.sourceforge.ondex.mapping.inparanoid.clustering;

import java.util.HashSet;

/**
 * Represents an ortholog pair.
 * 
 * @author taubertj
 * 
 */
public class Ortholog implements Comparable<Ortholog> {

	// main ortholog A
	private Inparalog mainA = null;

	private String taxidA = "0";

	// main ortholog B
	private Inparalog mainB = null;

	private String taxidB = "0";

	// hit score
	private double score = 0;

	// inparalogs for A
	public HashSet<Inparalog> inA = new HashSet<Inparalog>();

	// inparalogs for B
	public HashSet<Inparalog> inB = new HashSet<Inparalog>();

	/**
	 * Method for order on Ortholog.
	 * 
	 * @param ortho
	 *            Ortholog
	 * @return int
	 */
	public int compareTo(Ortholog ortho) {
		return (int) (ortho.score - this.score);
	}

	/**
	 * @return the mainA
	 */
	public Inparalog getMainA() {
		return mainA;
	}

	/**
	 * @return the mainB
	 */
	public Inparalog getMainB() {
		return mainB;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the taxidA
	 */
	public String getTaxidA() {
		return taxidA;
	}

	/**
	 * @return the taxidB
	 */
	public String getTaxidB() {
		return taxidB;
	}

	/**
	 * @param mainA the mainA to set
	 */
	public void setMainA(Inparalog mainA) {
		this.mainA = mainA;
	}

	/**
	 * @param mainB the mainB to set
	 */
	public void setMainB(Inparalog mainB) {
		this.mainB = mainB;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param taxidA the taxidA to set
	 */
	public void setTaxidA(String taxidA) {
		this.taxidA = taxidA;
	}

	/**
	 * @param taxidB the taxidB to set
	 */
	public void setTaxidB(String taxidB) {
		this.taxidB = taxidB;
	}
}
