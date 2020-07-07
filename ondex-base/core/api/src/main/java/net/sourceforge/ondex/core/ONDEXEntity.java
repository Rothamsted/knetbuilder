package net.sourceforge.ondex.core;

import java.util.Set;

import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * Common functionality for security management and update tracking.
 * 
 * @author taubertj
 * 
 */
public interface ONDEXEntity {

	/**
	 * Adds a given EvidenceType to the ONDEXConcept.
	 * 
	 * @param evidencetype
	 *            EvidenceType to be added
	 * @throws NullValueException
	 *             if evidencetype parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void addEvidenceType(EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException;

	/**
	 * Adds an ONDEXConcept to the tag of this ONDEXConcept.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @throws NullValueException
	 *             if concept parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void addTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Creates a new Attribute with the given AttributeName and value. Adds the
	 * new Attribute to the list of Attribute of this instance.
	 * 
	 * @param attrname
	 *            the AttributeName of the new Attribute
	 * @param value
	 *            the Object value of the new Attribute
	 * @param doIndex
	 *            whether or not to index the new Attribute
	 * @return new Attribute
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public Attribute createAttribute(AttributeName attrname, Object value,
			boolean doIndex) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Removes a Attribute or null if unsuccessful from the list of Attribute of
	 * this instance and returns the removed Attribute.
	 * 
	 * @param attrname
	 *            AttributeName of Attribute to be removed
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if attrname parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteAttribute(AttributeName attrname)
			throws NullValueException, UnsupportedOperationException;

	/**
	 * Returns a Attribute or null if unsuccessful for a given AttributeName.
	 * 
	 * @param attrname
	 *            AttributeName to search
	 * @return existing Attribute
	 * @throws NullValueException
	 *             if attrname parameter is null.
	 */
	public Attribute getAttribute(AttributeName attrname)
			throws NullValueException;

	/**
	 * Returns all Attribute contained in the list of Attribute of this
	 * instance.
	 * 
	 * @return all ConceptAttribute as Set<ConceptAttribute>
	 */
	public Set<Attribute> getAttributes();

	/**
	 * Returns a list of all evidences for this ONDEXConcept.
	 * 
	 * @return list of evidences as Set<EvidenceType>
	 */
	public Set<EvidenceType> getEvidence();

	/**
	 * Returns a unique integer id for this entity.
	 * 
	 * @return Integer
	 */
	public int getId();

	/**
	 * Returns an Set on ONDEXConcepts representing the tags of this
	 * ONDEXConcept.
	 * 
	 * @return tags as Set<ONDEXConcept>
	 */
	public Set<ONDEXConcept> getTags();

	/**
	 * Removes a given EvidenceType for the set of EvidenceType of this
	 * ONDEXConcept.
	 * 
	 * @param evidencetype
	 *            EvidenceType to be removed
	 * @return true if successful
	 * @throws NullValueException
	 *             if evidencetype parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean removeEvidenceType(EvidenceType evidencetype)
			throws NullValueException, UnsupportedOperationException;

	/**
	 * Removes an ONDEXConcept from the tag of this ONDEXConcept.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return true if successful
	 * @throws NullValueException
	 *             if evidencetype parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean removeTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException;
}
