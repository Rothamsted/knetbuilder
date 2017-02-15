package net.sourceforge.ondex.core.base;

import java.io.Serializable;
import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class represents a ONDEX Concept. It can have 0..* ConceptNames, 0..*
 * ConceptAccessions and 0..* Attribute. ConceptNames, ConceptAccessions and
 * Attribute can be added with the create*-methods.
 * 
 * @author sierenk, taubertj
 */
public abstract class AbstractConcept extends AbstractONDEXEntity 
    implements ONDEXConcept, Serializable {

	/**
	 * Primary unique id for this ONDEXConcept within the current ONDEXGraph.
	 * This id is assigned at creation time by the ONDEXGraph by simply counting
	 * up. An id doesn't get reassigned at any time, so the maximum number of
	 * concepts in a graph is Integer.MAX_VALUE.
	 */
	protected final int id;

	/**
	 * A arbitrary id for this ONDEXConcept, which could be assigned by a parser
	 * or any other means. This is not required and can be null. The intension
	 * of this id is for debugging or associating an id from a data source with
	 * this AbstractConcept.
	 */
	protected String pid;

	/**
	 * Relevant annotation for this ONDEXConcept, which is a short significant
	 * human readable description.
	 */
	protected String annotation;

	/**
	 * Any longer description String for this AbstractConcept. Should be
	 * distinct from annotation.
	 */
	protected String description;

	/**
	 * DataSource (data source) to which this ONDEXConcept belongs to. The
	 * actual instance for DataSource.
	 */
	protected DataSource elementOf;

	/**
	 * ConceptClass of which this ONDEXConcept belongs to. The actual instance
	 * for ConceptClass.
	 */
	protected ConceptClass ofType;

	/**
	 * Constructor which fills all fields of this ONDEXConcept and initialises
	 * internal data structures.
	 * 
	 * @param sid
	 *            unique id associated with parent ONDEXGraph
	 * @param id
	 *            unique ID of this ONDEXConcept
	 * @param pid
	 *            parser assigned ID of this ONDEXConcept
	 * @param annotation
	 *            relevant annotations of this ONDEXConcept
	 * @param description
	 *            every associated description of this ONDEXConcept
	 * @param elementOf
	 *            DataSource (data source) to which this ONDEXConcept belongs to
	 * @param ofType
	 *            ConceptClass of this AbtractConcept
	 */
	protected AbstractConcept(long sid, int id, String pid, String annotation,
			String description, DataSource elementOf, ConceptClass ofType) {
		this.sid = sid;
		this.id = id;
		this.pid = pid;
		this.annotation = annotation;
		this.description = description;
		this.elementOf = elementOf;
		this.ofType = ofType;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#addEvidenceType(net.sourceforge
	 *      .ondex.core.EvidenceType)
	 */
	@Override
	public void addEvidenceType(EvidenceType evidenceType)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.EvidenceTypeNull"));

		saveEvidenceType(evidenceType);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#addTag(net.sourceforge.ondex
	 *      .core.ONDEXConcept)
	 */
	@Override
	public void addTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ONDEXConceptNull"));

		saveTag(concept);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#createAttribute(net.sourceforge
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
							.getProperty("AbstractConcept.ConceptAttributeAttributeNameNull"));

		// null values not allowed
		if (value == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAttributeValueNull"));

		return storeConceptAttribute(new ConceptAttribute(sid, id,
				attributeName, value, doIndex));
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#createConceptAccession(java.lang
	 *      .String, net.sourceforge.ondex.core.DataSource, boolean)
	 */
	@Override
	public ConceptAccession createConceptAccession(String accession,
			DataSource elementOf, boolean ambiguous) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (accession == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionAccessionNull"));

		// empty strings not allowed
		if (accession.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionAccessionEmpty"));

		// null values not allowed
		if (elementOf == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionElementOfNull"));

		return storeConceptAccession(new ConceptAccessionImpl(sid, id,
				accession, elementOf, ambiguous));
	}

	/**
	 * 
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#createConceptName(java.lang.String
	 *      , boolean)
	 */
	@Override
	public ConceptName createConceptName(String name, boolean isPreferred)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {

		// null values not allowed
		if (name == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameNull"));

		// empty strings not allowed
		if (name.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameEmpty"));

		return storeConceptName(new ConceptNameImpl(sid, id, name, isPreferred));
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#deleteAttribute(net.sourceforge
	 *      .ondex.core.AttributeName)
	 */
	@Override
	public boolean deleteAttribute(AttributeName attributeName)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAttributeAttributeNameNull"));

		return removeConceptAttribute(attributeName);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#deleteConceptAccession(java.lang
	 *      .String, net.sourceforge.ondex.core.DataSource)
	 */
	@Override
	public boolean deleteConceptAccession(String accession, DataSource elementOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {

		// null values not allowed
		if (accession == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionAccessionNull"));

		// empty strings not allowed
		if (accession.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionAccessionEmpty"));

		// null values not allowed
		if (elementOf == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionElementOfNull"));

		return removeConceptAccession(accession, elementOf);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#deleteConceptName(java.lang.String
	 *      )
	 */
	@Override
	public boolean deleteConceptName(String name) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (name == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameNull"));

		// empty strings not allowed
		if (name.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameEmpty"));

		return removeConceptName(name);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof AbstractConcept))
			return false;
		AbstractConcept c = (AbstractConcept) o;
		return this.id == c.id;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getAnnotation()
	 */
	@Override
	public String getAnnotation() {
		return annotation;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getAttribute(net.sourceforge
	 *      .ondex.core.AttributeName)
	 */
	@Override
	public Attribute getAttribute(AttributeName attributeName)
			throws NullValueException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAttributeAttributeNameNull"));

		return retrieveConceptAttribute(attributeName);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getAttributes()
	 */
	@Override
	public Set<Attribute> getAttributes() {
		return BitSetFunctions.unmodifiableSet(retrieveConceptAttributeAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getConceptAccession(java.lang
	 *      .String, net.sourceforge.ondex.core.DataSource)
	 */
	@Override
	public ConceptAccession getConceptAccession(String accession,
			DataSource elementOf) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (accession == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameNull"));

		// empty strings not allowed
		if (accession.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionAccessionEmpty"));

		// null values not allowed
		if (elementOf == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptAccessionElementOfNull"));

		return retrieveConceptAccession(accession, elementOf);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getConceptAccessions()
	 */
	@Override
	public Set<ConceptAccession> getConceptAccessions() {
		return BitSetFunctions.unmodifiableSet(retrieveConceptAccessionAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getConceptName()
	 */
	@Override
	public ConceptName getConceptName() {
		return retrievePreferredConceptName();
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getConceptName(java.lang.String)
	 */
	@Override
	public ConceptName getConceptName(String name) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (name == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameNull"));

		// empty strings not allowed
		if (name.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractConcept.ConceptNameNameEmpty"));

		return retrieveConceptName(name);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getConceptNames()
	 */
	@Override
	public Set<ConceptName> getConceptNames() {
		return BitSetFunctions.unmodifiableSet(retrieveConceptNameAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getElementOf()
	 */
	@Override
	public DataSource getElementOf() {
		return elementOf;
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getEvidence()
	 */
	@Override
	public Set<EvidenceType> getEvidence() {
		return BitSetFunctions.unmodifiableSet(retrieveEvidenceTypeAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getOfType()
	 */
	@Override
	public ConceptClass getOfType() {
		return ofType;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getPID()
	 */
	@Override
	public String getPID() {
		return pid;
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXConcept#getTags()
	 */
	@Override
	public Set<ONDEXConcept> getTags() {
		return BitSetFunctions.unmodifiableSet(retrieveTagAll());
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	/**
	 * Returns whether this concept is inherited from the given ConceptClass.
	 * This is the case when its ofType cc' either equals cc or is a transitive
	 * specialisation of cc.
	 * 
	 * @param conceptClass
	 *            the ConceptClass against which to test.
	 * @return whether the above holds.
	 */
	@Override
	public boolean inheritedFrom(ConceptClass conceptClass) {
		ConceptClass my_cc = getOfType();
		while (!my_cc.equals(conceptClass)) {
			my_cc = my_cc.getSpecialisationOf();
			if (my_cc == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#removeEvidenceType(net.sourceforge
	 *      .ondex.core.EvidenceType)
	 */
	@Override
	public boolean removeEvidenceType(EvidenceType evidenceType)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.EvidenceTypeNull"));

		return dropEvidenceType(evidenceType);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#removeTag(net.sourceforge
	 *      .ondex.core.ONDEXConcept)
	 */
	@Override
	public boolean removeTag(ONDEXConcept concept) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.ONDEXConceptNull"));

		return dropTag(concept);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#setAnnotation(java.lang.String)
	 */
	@Override
	public void setAnnotation(String annotation) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (annotation == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.AnnotationNull"));

		// read-only graph
		if (ONDEXGraphRegistry.graphs.get(sid).isReadOnly())
			throw new UnsupportedOperationException();

		this.annotation = annotation;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractConcept.DescriptionNull"));

		// read-only graph
		if (ONDEXGraphRegistry.graphs.get(sid).isReadOnly())
			throw new UnsupportedOperationException();

		this.description = description;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXConcept#setPID(java.lang.String)
	 */
	@Override
	public void setPID(String pid) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (pid == null)
			throw new NullValueException(
					Config.properties.getProperty("AbstractConcept.PidNull"));

		// read-only graph
		if (ONDEXGraphRegistry.graphs.get(sid).isReadOnly())
			throw new UnsupportedOperationException();

		this.pid = pid;
	}

	@Override
	public String toString() {
		return this.pid;
	}

	/**
	 * Drops a given EvidenceType out of the list of EvidenceTypes.
	 * 
	 * @param evidenceType
	 *            EvidenceType to drop
	 * @return boolean
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean dropEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException;

	/**
	 * Drops a given ONDEXConcept out of the tag list.
	 * 
	 * @param concept
	 *            AbstracConcept to drop
	 * @return boolean
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean dropTag(ONDEXConcept concept)
			throws UnsupportedOperationException;

	/**
	 * Removes a ConceptAccession by a given name from the repository.
	 * 
	 * @param accession
	 *            accession to be removed
	 * @param elementOf
	 *            DataSource of ConceptAccession
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeConceptAccession(String accession,
			DataSource elementOf) throws UnsupportedOperationException;

	/**
	 * Removes a Attribute by a given AttributeName from the repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeConceptAttribute(
			AttributeName attributeName) throws UnsupportedOperationException;

	/**
	 * Removes a ConceptName by a given name from the repository.
	 * 
	 * @param name
	 *            name of ConceptName to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeConceptName(String name)
			throws UnsupportedOperationException;

	/**
	 * Retrieves a ConceptAccession by the given accession and elementOf from
	 * the repository.
	 * 
	 * @param accession
	 *            accession to be retrieved
	 * @param elementOf
	 *            DataSource of ConceptAccession
	 * @return ConceptAccession
	 */
	protected abstract ConceptAccession retrieveConceptAccession(
			String accession, DataSource elementOf);

	/**
	 * Returns all ConceptAccessions contained in the repository.
	 * 
	 * @return Set<ConceptAccession>
	 */
	protected abstract Set<ConceptAccession> retrieveConceptAccessionAll();

	/**
	 * Retrieves a Attribute by a given AttributeName from the repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Attribute
	 */
	protected abstract Attribute retrieveConceptAttribute(
			AttributeName attributeName);

	/**
	 * Retrieves a Set of Attribute from the repository.
	 * 
	 * @return Set<Attribute>
	 */
	protected abstract Set<Attribute> retrieveConceptAttributeAll();

	/**
	 * Retrieves a ConceptName by a given name from the repository.
	 * 
	 * @param name
	 *            name of ConceptName to be retrieved
	 * @return ConceptName
	 */
	protected abstract ConceptName retrieveConceptName(String name);

	/**
	 * Returns all ConceptNames contained in the repository.
	 * 
	 * @return Set<ConceptName>
	 */
	protected abstract Set<ConceptName> retrieveConceptNameAll();

	/**
	 * Returns the list of all EvidenceTypes.
	 * 
	 * @return Set<EvidenceType>
	 */
	protected abstract Set<EvidenceType> retrieveEvidenceTypeAll();

	/**
	 * Returns the preferred ConceptName from the repository.
	 * 
	 * @return ConceptName
	 */
	protected abstract ConceptName retrievePreferredConceptName();

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
	 */
	protected abstract void saveEvidenceType(EvidenceType evidenceType)
			throws UnsupportedOperationException;

	/**
	 * Saves a given ONDEXConcept to the tag list.
	 * 
	 * @param concept
	 *            ONDEXConcept to save
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract void saveTag(ONDEXConcept concept)
			throws UnsupportedOperationException;

	/**
	 * Stores the given ConceptAccession in the repository.
	 * 
	 * @param conceptAccession
	 *            ConceptAccession to store
	 * @return ConceptAccession
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract ConceptAccession storeConceptAccession(
			ConceptAccession conceptAccession)
			throws UnsupportedOperationException;

	/**
	 * Stores a given Attribute to the repository.
	 * 
	 * @param attribute
	 *            Attribute to store
	 * @return Attribute
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract Attribute storeConceptAttribute(Attribute attribute)
			throws UnsupportedOperationException;

	/**
	 * Stores the given ConceptName in the repository.
	 * 
	 * @param conceptName
	 *            ConceptName to store
	 * @return ConceptName
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract ConceptName storeConceptName(ConceptName conceptName)
			throws UnsupportedOperationException;
}
