package net.sourceforge.ondex.parser.biocycold.objects;

import java.util.HashSet;

/**
 * Represents a pathway
 * @author peschr
 * 
 */
public class Pathway extends AbstractNode {
	private String commonName;

	private String comment;

	private boolean isSuperPathWay;

	private boolean belongsToSuperPathway;
	
	private Pathway superPathWay;

	private HashSet<Pathway> pathwayLinks = new HashSet<Pathway>();

	private HashSet<Pathway> subPathway = new HashSet<Pathway>();

	private HashSet<ECNumber> ecNumbers = new HashSet<ECNumber>();

	/**
	 * Adds a link, if the pathway is not already set.
	 * 
	 * @param way
	 * @throws Exception
	 */
	public void addPathwayLink(Pathway way) throws Exception {
		if (way.equals(superPathWay)) {
			throw new Exception("Already a super pathway");
		}
		this.pathwayLinks.add(way);
	}

	public void addSubPathWay(Pathway way) {
		this.subPathway.add(way);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public HashSet<Pathway> getPathwayLinks() {
		return pathwayLinks;
	}

	public HashSet<Pathway> getSubPathway() {
		return subPathway;
	}

	public boolean isSuperPathWay() {
		return isSuperPathWay;
	}

	public void setSuperPathWay(boolean isSuperPathWay) {
		this.isSuperPathWay = isSuperPathWay;
	}

	public Pathway getSuperPathWay() {
		return superPathWay;
	}

	/**
	 * Sets a pathway as super pathway. if the pathway is already set as
	 * pathway, it will be deletead
	 * 
	 * @param superPathWay
	 * @throws Exception
	 */
	public void setSuperPathWay(Pathway superPathWay) throws Exception {
		if (pathwayLinks.contains(superPathWay)) {
			pathwayLinks.remove(superPathWay);
		}
		this.superPathWay = superPathWay;
	}

	public boolean isBelongsToSuperPathway() {
		return belongsToSuperPathway;
	}

	public void setBelongsToSuperPathway(boolean belongsToSuperPathway) {
		this.belongsToSuperPathway = belongsToSuperPathway;
	}

	public void addECNumber(ECNumber ec) {
		ecNumbers.add(ec);
	}

	public HashSet<ECNumber> getEcNumbers() {
		return ecNumbers;
	}
}
