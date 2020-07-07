package net.sourceforge.ondex.core;

/**
 * This class stores one Concept Name belonging to one instance of Concept. If
 * this is the preferred name for the corresponding Concept, isPreferred is set.
 * 
 * @author taubertj
 * 
 */
public interface ConceptName extends ONDEXAssociable {

	/**
	 * Returns the stored Concept Name.
	 * 
	 * @return String
	 */
	public String getName();

	/**
	 * Returns the id of the ONDEXConcept that this Name is associated to.
	 */
	public int getOwnerId();

	/**
	 * Returns if the ConceptName is preferred.
	 * 
	 * @return boolean
	 */
	public boolean isPreferred();

	/**
	 * Sets the ConceptName to preferred.
	 * 
	 * @param isPreferred
	 *            new is preferred value
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setPreferred(boolean isPreferred)
			throws UnsupportedOperationException;
}
