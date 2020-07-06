package net.sourceforge.ondex.core.base;

import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationKey;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class defines a Relation between two concepts concept.
 * 
 * @author sierenk, taubertj
 */
public abstract class AbstractRelation extends AbstractONDEXEntity implements
		ONDEXRelation {

	/**
	 * An int key for this AbstractRelation.
	 */
	protected final int id;

	/**
	 * A unique key for this AbstractRelation.
	 */
	protected RelationKey key;

	/**
	 * Start point concept
	 */
	protected ONDEXConcept fromConcept;

	/**
	 * End point concept
	 */
	protected ONDEXConcept toConcept;

	/**
	 * Type of this relation
	 */
	protected RelationType ofType;

	/**
	 * Constructor which fills all fields of this class.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            int id
	 * @param fromConcept
	 *            start point
	 * @param toConcept
	 *            end point
	 * @param ofType
	 *            specifies Relation Type
	 */
	protected AbstractRelation(long sid, int id, ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType) {
		this.sid = sid;
		this.id = id;
		this.fromConcept = fromConcept;
		this.toConcept = toConcept;
		this.ofType = ofType;
		this.key = new RelationKeyImpl(sid, fromConcept.getId(),
				toConcept.getId(), ofType.getId());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#addEvidenceType(net.sourceforge
	 *      .ondex.core.EvidenceType)
	 */
	@Override
	public void addEvidenceType(EvidenceType evidenceType)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.EvidenceTypeNull"));

		saveEvidenceType(evidenceType);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#addTag(net.sourceforge.ondex
	 *      .core.ONDEXConcept)
	 */
	@Override
	public void addTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.ONDEXConceptNull"));

		saveTag(concept);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#createAttribute(net.sourceforge
	 *      .ondex.core.AttributeName, java.lang.Object, boolean)
	 */
	@Override
	public Attribute createAttribute(AttributeName attributeName, Object value,
			boolean doIndex) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.RelationAttributeAttributeNameNull"));

		// null values not allowed
		if (value == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.RelationAttributeValueNull"));

		return storeRelationAttribute(new RelationAttribute(sid, id,
				attributeName, value, doIndex));
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#deleteAttribute(net.sourceforge
	 *      .ondex.core.AttributeName)
	 */
	@Override
	public boolean deleteAttribute(AttributeName attributeName)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.RelationAttributeAttributeNameNull"));

		return removeRelationAttribute(attributeName);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof AbstractRelation))
			return false;
		AbstractRelation r = (AbstractRelation) o;
		return this.key.equals(r.key);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#getAttribute(net.sourceforge
	 *      .ondex.core.AttributeName)
	 */
	@Override
	public Attribute getAttribute(AttributeName attributeName)
			throws NullValueException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.RelationAttributeAttributeNameNull"));

		return retrieveRelationAttribute(attributeName);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXRelation#getAttributes()
	 */
	@Override
	public Set<Attribute> getAttributes() {
		return BitSetFunctions.unmodifiableSet(retrieveRelationAttributeAll());
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXRelation#getEvidence()
	 */
	@Override
	public Set<EvidenceType> getEvidence() {
		return BitSetFunctions.unmodifiableSet(retrieveEvidenceTypeAll());
	}

	/**
	 * Returns the from Concept of this instance of Relation.
	 * 
	 * @return ONDEXConcept
	 */
	@Override
	public ONDEXConcept getFromConcept() {
		return fromConcept;
	}

	@Override
	public int getId() {
		return id;
	}

	/**
	 * Returns the unique Key of this Relation.
	 * 
	 * @return RelationKey
	 */
	@Override
	public RelationKey getKey() {
		return key;
	}

	/**
	 * Returns the Relation type of this instance of Relation.
	 * 
	 * @return RelationType
	 */
	@Override
	public RelationType getOfType() {
		return ofType;
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXRelation#getTags()
	 */
	@Override
	public Set<ONDEXConcept> getTags() {
		return BitSetFunctions.unmodifiableSet(retrieveTagAll());
	}

	/**
	 * Returns the to Concept of this instance of Relation.
	 * 
	 * @return ONDEXConcept
	 */
	@Override
	public ONDEXConcept getToConcept() {
		return toConcept;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	/**
	 * Returns whether this relation is inherited from the given RelationType.
	 * This is the case when its ofType rt' either equals rt or is a transitive
	 * specialisation of rt.
	 * 
	 * @param rt
	 *            the RelationType against which to test.
	 * @return whether the above holds.
	 */
	@Override
	public boolean inheritedFrom(RelationType rt) {
		RelationType my_rt = getOfType();
		while (!my_rt.equals(rt)) {
			my_rt = my_rt.getSpecialisationOf();
			if (my_rt == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#removeEvidenceType(net.sourceforge
	 *      .ondex.core.EvidenceType)
	 */
	@Override
	public boolean removeEvidenceType(EvidenceType evidenceType)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.EvidenceTypeNull"));

		return dropEvidenceType(evidenceType);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXRelation#removeTag(net.sourceforge
	 *      .ondex.core.ONDEXConcept)
	 */
	@Override
	public boolean removeTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractRelation.ONDEXConceptNull"));

		return dropTag(concept);
	}

	/**
	 * Drops a given EvidenceType out of the list of EvidenceTypes.
	 * 
	 * @param evidenceType
	 *            EvidenceType to drop
	 * @return boolean
	 * @throws UnsupportedOperationException
	 */
	protected abstract boolean dropEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException;

	/**
	 * Drops a given ONDEXConcept out of the tag list.
	 * 
	 * @param concept
	 *            ONDEXConcept to drop
	 * @return boolean
	 * @throws UnsupportedOperationException
	 */
	protected abstract boolean dropTag(ONDEXConcept concept)
			throws UnsupportedOperationException;

	/**
	 * Removes a RelationAttribute by a given AttributeName from the repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 */
	protected abstract boolean removeRelationAttribute(
			AttributeName attributeName) throws UnsupportedOperationException;

	/**
	 * Returns the list of all EvidenceTypes.
	 * 
	 * @return Set<EvidenceType>
	 */
	protected abstract Set<EvidenceType> retrieveEvidenceTypeAll();

	/**
	 * Retrieves a RelationAttribute by a given AttributeName from the
	 * repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Attribute
	 */
	protected abstract Attribute retrieveRelationAttribute(
			AttributeName attributeName);

	/**
	 * Retrieves a Set of Attribute from the repository.
	 * 
	 * @return Set<RelationAttribute>
	 */
	protected abstract Set<Attribute> retrieveRelationAttributeAll();

	/**
	 * Returns the tag list.
	 * 
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveTagAll();

	/**
	 * Saves a given EvidenceType to the list of EvidenceTypes.
	 * 
	 * @param evidenceType
	 *            EvidenceType to save
	 * @throws UnsupportedOperationException
	 */
	protected abstract void saveEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException;

	/**
	 * Saves a given ONDEXConcept to the tag list.
	 * 
	 * @param concept
	 *            ONDEXConcept to save
	 * @throws UnsupportedOperationException
	 */
	protected abstract void saveTag(ONDEXConcept concept)
			throws UnsupportedOperationException;

	/**
	 * Stores a given RelationAttribute to the repository.
	 * 
	 * @param attribute
	 *            Attribute to store
	 * @return Attribute
	 * @throws UnsupportedOperationException
	 */
	protected abstract Attribute storeRelationAttribute(Attribute attribute)
			throws UnsupportedOperationException;

}
