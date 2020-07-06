package net.sourceforge.ondex.parser.transpath;


/**
 * Class representing a Gene object in TP.
 * 
 * @author taubertj
 *
 */
class Gene extends AbstractTPObject {
	
	// which species this Gene belongs to
	private String species; // OS
	
	/**
	 * Constructor for a unique TP accession.
	 * 
	 * @param accession - String
	 */
	protected Gene(String accession) {
		super(accession);
	}
	
	/**
	 * Returns associated species.
	 * 
	 * @return String
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * Sets species for this Gene.
	 * 
	 * @param species - String
	 */
	protected void setSpecies(String species) {
		this.species = species;
	}
}
