package net.sourceforge.ondex.parser.transpath;

import java.util.HashSet;
import java.util.Iterator;


/**
 * Representing a Reaction object in TP.
 * 
 * @author taubertj
 *
 */
class Reaction extends AbstractTPObject{
	
	// incoming relations to molecules
	private HashSet<String> inMolecules = new HashSet<String>(); // MB
	
	// outgoing relations to molecules
	private HashSet<String> outMolecules = new HashSet<String>(); // MA
	
	// catalysed by molecules
	private HashSet<String> catalysationMolecules = new HashSet<String>(); // MC
	
	// inhibited by molecules
	private HashSet<String> inhibitionMolecules = new HashSet<String>(); // MI

	/**
	 * Constructor for a unique TP accession.
	 * 
	 * @param accession - String
	 */
	protected Reaction(String accession) {
		super(accession);
	}
	
	/**
	 * Returns an Iterator over catalysing molecules.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getCatalysationMolecules() {
		return catalysationMolecules.iterator();
	}

	/**
	 * Adds a molecule to the list of catalysing molecules.
	 * 
	 * @param mol - String
	 */
	protected void addCatalysationMolecule(String mol) {
		this.catalysationMolecules.add(mol);
	}
	
	/**
	 * Returns an Iterator over incoming molecules.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getInMolecules() {
		return inMolecules.iterator();
	}

	/**
	 * Adds a molecule to the list of incoming molecules.
	 * 
	 * @param mol - String
	 */
	protected void addInMolecule(String mol) {
		this.inMolecules.add(mol);
	}
	
	/**
	 * Returns an Iterator over inhibiting molecules.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getInhibitionMolecules() {
		return inhibitionMolecules.iterator();
	}

	/**
	 * Adds a molecule to the list of inhibiting molecules.
	 * 
	 * @param mol - String
	 */
	protected void addInhibitionMolecule(String mol) {
		this.inhibitionMolecules.add(mol);
	}
	
	/**
	 * Returns an Iterator over outgoing molecules.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getOutMolecules() {
		return outMolecules.iterator();
	}
	
	/**
	 * Adds a molecule to the list of outgoing molecules.
	 * 
	 * @param mol - String
	 */
	protected void addOutMolecule(String mol) {
		this.outMolecules.add(mol);
	}	
}
