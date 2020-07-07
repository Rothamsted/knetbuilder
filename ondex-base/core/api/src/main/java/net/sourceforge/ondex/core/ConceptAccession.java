package net.sourceforge.ondex.core;

/**
 * This class stores ConceptAccession which belong to one instance of Concept. A
 * ConceptAccession belongs to a DataSource. It is identified by accession and
 * DataSource.
 * 
 * @author taubertj
 * 
 */
public interface ConceptAccession extends ONDEXAssociable {

	/**
	 * Returns the store accession name.
	 * 
	 * @return String
	 */
	public String getAccession();

	/**
	 * Returns the DataSource this accession belongs to.
	 * 
	 * @return DataSource
	 */
	public DataSource getElementOf();

	/**
	 * Returns the id of the ONDEXConcept that this Accession is associated to.
	 */
	public int getOwnerId();

	/**
	 * Returns whether or not this accession is ambiguous.
	 * 
	 * @return boolean
	 */
	public boolean isAmbiguous();

	/**
	 * Changes whether or not this accession is ambiguous.
	 * 
	 * @param ambiguous
	 *            set ambiguous
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setAmbiguous(boolean ambiguous)
			throws UnsupportedOperationException;
}
