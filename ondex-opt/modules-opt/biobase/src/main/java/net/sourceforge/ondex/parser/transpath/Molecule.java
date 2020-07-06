package net.sourceforge.ondex.parser.transpath;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Representing a Molecule object in TP.
 * 
 * @author taubertj
 *
 */
class Molecule extends AbstractTPObject {

	// which gene encodes this molecule
	private String encodingGene; // GE
	
	// which species it is
	private String species; // OS
	
	// what kind of molecule
	private String type; // TY: family = ProtFam, complex = Protcmplx, other = Comp, der ganze Rest = Protein
	
	// associated sequence data
	private String sequence; // SQ
	
	// possible subunits
	private HashSet<String> subunits = new HashSet<String>(); // (of Strings) ST
	
	/**
	 * Constructor for a unique TP accession.
	 * 
	 * @param accession - String
	 */
	protected Molecule(String accession) {
		super(accession);
	}

	/**
	 * Return the encoding gene for this molecule.
	 * 
	 * @return String
	 */
	public String getEncodingGene() {
		return encodingGene;
	}
	
	/**
	 * Sets the encoding gene for this molecule.
	 * 
	 * @param encodingGene - String
	 */
	protected void setEncodingGene(String encodingGene) {
		this.encodingGene = encodingGene;
	}

	/**
	 * Returns sequence data for this molecule.
	 * 
	 * @return String
	 */
	public String getSequence() {
		return sequence;
	}

	/**
	 * Sets sequence data for this molecule.
	 * 
	 * @param sequence - String
	 */
	protected void setSequence(String sequence) {
		this.sequence = sequence;
	}

	/**
	 * Returns species of this molecule.
	 * 
	 * @return String
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * Sets the species of this molecule.
	 * 
	 * @param species - String
	 */
	protected void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * Returns an Iterator over associated subunits.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getSubunits() {
		return subunits.iterator();
	}
	
	/**
	 * Adds a subunit to the current list.
	 * 
	 * @param sub - String
	 */
	protected void addSubunit(String sub) {
		this.subunits.add(sub);
	}

	/**
	 * Returns type of this molecule.
	 * 
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets type of this molecule.
	 * 
	 * @param type - String
	 */
	protected void setType(String type) {
		this.type = type;
	}
}
