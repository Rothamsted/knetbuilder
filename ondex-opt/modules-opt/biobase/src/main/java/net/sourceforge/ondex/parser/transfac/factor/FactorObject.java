package net.sourceforge.ondex.parser.transfac.factor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.parser.transfac.AbstractTFObject;
import net.sourceforge.ondex.parser.transfac.Parser;

/**
 * Class representing a Factor object in TF.
 * 
 * @author taubertj
 *
 */
public class FactorObject extends AbstractTFObject {
	
	private static boolean DEBUG = false;
	
	// protein sequence for this transcription factor
	private String sequence = null;
	
	// the encoding gene
	private String encodingGene = null;
	
	// list of interacting factors	
	private HashSet<String> interactingFactors = new HashSet<String>();
	
	// list of regulating genes
	private HashSet<String> regulatedGenes = new HashSet<String>();
	
	// list of associated matrices
	private HashSet<String> matrices = new HashSet<String>();

	// list of associated binding sites
	private HashSet<String> bindingSites = new HashSet<String>();

	/**
	 * Constructor for a unique TF accession.
	 * 
	 * @param accession - String
	 */
	protected FactorObject(String accession) {
		super(accession);
	}

	/**
	 * Returns the associated protein sequence.
	 * 
	 * @return String
	 */
	public String getSequence() {
		return sequence;
	}
	
	private static final Pattern nonValidChars = Pattern.compile("[^a-z|A-Z]");
	
	/**
	 * Sets the protein sequence for this factor.
	 * 
	 * @param proteinSequence - String
	 */
	protected void setSequence(String sequence) {
		this.sequence = nonValidChars.matcher(sequence).replaceAll("");
	}
	
	/**
	 * Returns encoding gene identifier.
	 * 
	 * @return String
	 */
	public String getEncodingGene() {
		return encodingGene;
	}
	
	/**
	 * Sets the encoding gene identifier.
	 * 
	 * @param encodingGene - String
	 */
	protected void setEncodingGene(String encodingGene) {
		this.encodingGene = encodingGene;
	}

	/**
	 * Returns an Iterator over interacting factors.
	 * 
	 * @return Iterator<String>
	 */
	public HashSet<String> getInteractingFactors() {
		return interactingFactors;
	}
		
	/**
	 * Adds an interacting factor to current list.
	 * 
	 * @param interactingFactor - String
	 */
	protected void addInteractingFactor(String interactingFactor) {
		if (DEBUG && interactingFactors.contains(interactingFactor)) {
			Parser.propagateEventOccurred(new DataFileErrorEvent(
					"InteractionFactor id already exists: " 
					+interactingFactor+" in "+getAccession(), ""));
		}
		this.interactingFactors.add(interactingFactor);
	}

	/**
	 * Returns an Iterator over regulated genes.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getRegulatedGenes() {
		return regulatedGenes.iterator();
	}
	
	/**
	 * Adds a regulated gene to current list.
	 * 
	 * @param regulated_gene - String
	 */
	protected void addRegulatedGene(String regulatedGene) {
		this.regulatedGenes.add(regulatedGene);
	}

	/**
	 * Returns an Iterator over matrices.
	 * 
	 * @return Iterator<String>
	 */
	public Iterator<String> getMatrices() {
		return matrices.iterator();
	}
	
	/**
	 * Adds a matrix to current list.
	 * 
	 * @param matrix - String
	 */
	protected void addMatrix(String matrix) {
		if (DEBUG && matrices.contains(matrix)) {
			Parser.propagateEventOccurred(new DataFileErrorEvent(
					"Matrix id already exists: " 
					+matrix+" in "+getAccession(), ""));
		}
		this.matrices.add(matrix);
	}

	public void addBindingSite(String bindingSiteAcc) {
		this.bindingSites.add(bindingSiteAcc);
	}

	public HashSet<String> getBindingSites() {
		return bindingSites;
	}
}
