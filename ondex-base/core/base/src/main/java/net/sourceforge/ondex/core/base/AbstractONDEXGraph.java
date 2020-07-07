package net.sourceforge.ondex.core.base;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EntityFactory;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class is the core of the ONDEX backend and contains all Concepts and
 * Relations as well as additional data like DataSources or ConceptClasses in
 * the ONDEXGraphMetaData.
 * 
 * Concepts and Relations can only be created within the tag of this class.
 * Additional data is added using the appropriate methods.
 * 
 * @author taubertj
 * 
 */
public abstract class AbstractONDEXGraph extends AbstractONDEXEntity implements
		ONDEXGraph {

	/**
	 * For short-cut method signatures
	 */
	private EntityFactory entityFactory = new EntityFactory(this);

	/**
	 * Name for this instance of AbstractONDEXGraph.
	 */
	private String name;

	/**
	 * Graph data for this instance of AbstractONDEXGraph.
	 */
	private ONDEXGraphMetaData data;

	/**
	 * Stores whether or not this ONDEXGraph is read only. For example a LUCENE
	 * ONDEX graph is read only.
	 */
	protected boolean readOnly = false;

	/**
	 * Stores the latest assigned int id to a concept. Every id gets assigned
	 * only once.
	 */
	protected int lastIdForConcept = 0;

	/**
	 * Stores the latest assigned int id to a relation. Every id gets assigned
	 * only once.
	 */
	protected int lastIdForRelation = 0;

	/**
	 * Constructor which fills all private fields of this class and sets a given
	 * unique id to this graph.
	 * 
	 * @param sid
	 *            unique id
	 * @param name
	 *            name of ONDEX graph
	 * @param data
	 *            ONDEX graph meta data
	 */
	protected AbstractONDEXGraph(long sid, String name, ONDEXGraphMetaData data) {
		this.name = name;
		this.data = data;
		this.sid = sid;
		ONDEXGraphRegistry.graphs.put(sid, this);
		data.associateGraph(this);
	}

	/**
	 * Constructor which fills all private fields of this class.
	 * 
	 * @param name
	 *            name of ONDEX graph
	 * @param data
	 *            ONDEX graph meta data
	 */
	protected AbstractONDEXGraph(String name, ONDEXGraphMetaData data) {
		this(System.nanoTime(), name, data);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#createConcept(java.lang.String,
	 *      java.lang.String, java.lang.String,
	 *      net.sourceforge.ondex.core.DataSource,
	 *      net.sourceforge.ondex.core.ConceptClass, java.util.Collection)
	 */
	@Override
	public ONDEXConcept createConcept(String pid, String annotation,
			String description, DataSource elementOf, ConceptClass ofType,
			Collection<EvidenceType> evidence) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (pid == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptIDNull"));

		// null values not allowed
		if (annotation == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptAnnotationNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptDescriptionNull"));

		// null values not allowed
		if (elementOf == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptElementOfNull"));

		// null values not allowed
		if (ofType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptOfTypeNull"));

		// null values not allowed
		if (evidence == null || evidence.size() == 0)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptEvidenceTypeNull"));

		lastIdForConcept++;
		return storeConcept(sid, lastIdForConcept, pid, annotation,
				description, elementOf, ofType, evidence);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#createRelation(net.sourceforge.
	 *      ondex.core.ONDEXConcept, net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType, java.util.Collection)
	 */
	@Override
	public ONDEXRelation createRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType,
			Collection<EvidenceType> evidence) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (fromConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationFromConceptNull"));

		// null values not allowed
		if (toConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationToConceptNull"));

		// null values not allowed
		if (ofType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationOfTypeNull"));

		// null values not allowed
		if (evidence == null || evidence.size() == 0)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationEvidenceTypeNull"));

		lastIdForRelation++;
		return storeRelation(sid, lastIdForRelation, fromConcept, toConcept,
				ofType, evidence);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteConcept(java.lang.Integer)
	 */
	@Override
	public boolean deleteConcept(int id) throws UnsupportedOperationException {

		ONDEXConcept c = this.getConcept(id);

		// check for null concept, happens only for invalid id
		if (c != null) {

			// remove from possible tag, avoid concurrent
			// modification exception by converting to array
			for (ONDEXConcept concept : this.getConceptsOfTag(c).toArray(
					new ONDEXConcept[0])) {
				concept.removeTag(c);
			}
			for (ONDEXRelation relation : this.getRelationsOfTag(c).toArray(
					new ONDEXRelation[0])) {
				relation.removeTag(c);
			}

			// delete associate relations of concept, avoid concurrent
			// modification exception by converting to array
			for (ONDEXRelation relation : this.getRelationsOfConcept(c)
					.toArray(new ONDEXRelation[0])) {
				this.deleteRelation(relation.getId());
			}

			// delete concept itself
			removeConcept(id);
			return true;
		}

		// not found
		return false;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteRelation(java.lang.Integer)
	 */
	@Override
	public boolean deleteRelation(int id) throws UnsupportedOperationException {

		ONDEXRelation r = this.getRelation(id);

		// check for null relation, happens for invalid id
		if (r != null) {

			// delete relation itself
			removeRelation(id);
			return true;
		}

		// not found
		return false;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#deleteRelation(net.sourceforge.
	 *      ondex.core.ONDEXConcept, net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	@Override
	public boolean deleteRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws NullValueException, UnsupportedOperationException {

		// null values not allowed
		if (fromConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationFromConceptNull"));

		// null values not allowed
		if (toConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationToConceptNull"));

		// null values not allowed
		if (ofType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationOfTypeNull"));

		return removeRelation(fromConcept, toConcept, ofType);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getTag()
	 */
	@Override
	public Set<ONDEXConcept> getAllTags() {
		return BitSetFunctions.unmodifiableSet(retrieveTags());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConcept(java.lang.Integer)
	 */
	@Override
	public ONDEXConcept getConcept(int id) {
		return retrieveConcept(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConcepts()
	 */
	@Override
	public Set<ONDEXConcept> getConcepts() {
		return BitSetFunctions.unmodifiableSet(retrieveConceptAll());
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfAttributeName(net.
	 *      sourceforge.ondex.core.AttributeName)
	 */
	@Override
	public Set<ONDEXConcept> getConceptsOfAttributeName(
			AttributeName attributeName) throws NullValueException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AttributeNameNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveConceptAllAttributeName(attributeName));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfConceptClass(net.
	 *      sourceforge.ondex.core.ConceptClass)
	 */
	@Override
	public Set<ONDEXConcept> getConceptsOfConceptClass(ConceptClass conceptClass)
			throws NullValueException {

		// null values not allowed
		if (conceptClass == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ConceptClassNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveConceptAllConceptClass(conceptClass));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfDataSource(net.sourceforge
	 *      .ondex.core.DataSource)
	 */
	@Override
	public Set<ONDEXConcept> getConceptsOfDataSource(DataSource dataSource)
			throws NullValueException {

		// null values not allowed
		if (dataSource == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.DataSourceNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveConceptAllDataSource(dataSource));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfEvidenceType(net.
	 *      sourceforge.ondex.core.EvidenceType)
	 */
	@Override
	public Set<ONDEXConcept> getConceptsOfEvidenceType(EvidenceType evidenceType)
			throws NullValueException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.getConceptsOfEvidenceType"));

		return BitSetFunctions
				.unmodifiableSet(retrieveConceptAllEvidenceType(evidenceType));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getConceptsOfTag(net.sourceforge
	 *      .ondex.core.ONDEXConcept)
	 */
	@Override
	public Set<ONDEXConcept> getConceptsOfTag(ONDEXConcept concept)
			throws NullValueException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptNull"));

		return BitSetFunctions.unmodifiableSet(retrieveConceptAllTag(concept));
	}

	@Override
	public EntityFactory getFactory() {
		return entityFactory;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getMetaData()
	 */
	@Override
	public ONDEXGraphMetaData getMetaData() {
		return data;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelation(java.lang.Integer)
	 */
	@Override
	public ONDEXRelation getRelation(int id) {
		return retrieveRelation(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelation(net.sourceforge.ondex
	 *      .core.ONDEXConcept, net.sourceforge.ondex.core.ONDEXConcept,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	@Override
	public ONDEXRelation getRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws NullValueException {

		// null values not allowed
		if (fromConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationFromConceptNull"));

		// null values not allowed
		if (toConcept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationToConceptNull"));

		// null values not allowed
		if (ofType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AbstractRelationOfTypeNull"));

		return retrieveRelation(fromConcept, toConcept, ofType);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelations()
	 */
	@Override
	public Set<ONDEXRelation> getRelations() {
		return BitSetFunctions.unmodifiableSet(retrieveRelationAll());
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfAttributeName(net
	 *      .sourceforge.ondex.core.AttributeName)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfAttributeName(
			AttributeName attributeName) throws NullValueException {

		// null values not allowed
		if (attributeName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.AttributeNameNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllAttributeName(attributeName));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfConcept(net.sourceforge
	 *      .ondex.core.ONDEXConcept)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfConcept(ONDEXConcept concept)
			throws NullValueException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllConcept(concept));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfConceptClass(net.
	 *      sourceforge.ondex.core.ConceptClass)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfConceptClass(
			ConceptClass conceptClass) throws NullValueException {

		// null values not allowed
		if (conceptClass == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ConceptClassNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllConceptClass(conceptClass));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfDataSource(net.
	 *      sourceforge .ondex.core.DataSource)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfDataSource(DataSource dataSource)
			throws NullValueException {

		// null values not allowed
		if (dataSource == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.DataSourceNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllDataSource(dataSource));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfEvidenceType(net.
	 *      sourceforge.ondex.core.EvidenceType)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfEvidenceType(
			EvidenceType evidenceType) throws NullValueException {

		// null values not allowed
		if (evidenceType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.EvidenceTypeNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllEvidenceType(evidenceType));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfRelationType(net.
	 *      sourceforge.ondex.core.RelationType)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfRelationType(
			RelationType relationType) throws NullValueException {

		// null values not allowed
		if (relationType == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.RelationTypeNull"));

		return BitSetFunctions
				.unmodifiableSet(retrieveRelationAllRelationType(relationType));
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraph#getRelationsOfTag(net.sourceforge
	 *      .ondex.core.ONDEXConcept)
	 */
	@Override
	public Set<ONDEXRelation> getRelationsOfTag(ONDEXConcept concept)
			throws NullValueException {

		// null values not allowed
		if (concept == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraph.ONDEXConceptNull"));

		return BitSetFunctions.unmodifiableSet(retrieveRelationAllTag(concept));
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraph#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Removes a ONDEXConcept for a given ID from the repository.
	 * 
	 * @param id
	 *            unique ID of ONDEXConcept to be removed
	 * @return ONDEXConcept
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	protected abstract ONDEXConcept removeConcept(int id)
			throws UnsupportedOperationException;

	/**
	 * Remove a ONDEXRelation by the given unique Id from the repository.
	 * 
	 * @param id
	 *            Integer
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	protected abstract boolean removeRelation(int id)
			throws UnsupportedOperationException;

	/**
	 * Remove a ONDEXRelation by the given from ONDEXConcept, to ONDEXConcept
	 * and RelationType from the repository.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept
	 * @param toConcept
	 *            to ONDEXConcept
	 * @param ofType
	 *            RelationType
	 * @return ONDEXRelation
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	protected abstract boolean removeRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws UnsupportedOperationException;

	/**
	 * Retrieves a ONDEXConcept for a given ID from the repository.
	 * 
	 * @param id
	 *            unique ID to look up
	 * @return ONDEXConcept
	 */
	protected abstract ONDEXConcept retrieveConcept(int id);

	/**
	 * Retrieves a Set of Concepts from the repository.
	 * 
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAll();

	/**
	 * Retrieves a Set of Concepts for a given AttributeName from the
	 * repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAllAttributeName(
			AttributeName attributeName);

	/**
	 * Retrieves a Set of Concepts for a given ConceptClass from the repository.
	 * 
	 * @param conceptClass
	 *            ConceptClass
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAllConceptClass(
			ConceptClass conceptClass);

	/**
	 * Retrieves a Set of Concepts for a given DataSource from the repository.
	 * 
	 * @param dataSource
	 *            DataSource
	 * @return Set of Concepts
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAllDataSource(
			DataSource dataSource);

	/**
	 * Retrieves a Set of Concepts for a given EvidenceType from the repository.
	 * 
	 * @param evidenceType
	 *            EvidenceType
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAllEvidenceType(
			EvidenceType evidenceType);

	/**
	 * Retrieves a Set of Concepts for a given tag from the repository.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveConceptAllTag(
			ONDEXConcept concept);

	/**
	 * Retrieves a ONDEXRelation by the given unique Id from the repository.
	 * 
	 * @param id
	 *            Integer
	 * @return ONDEXRelation
	 */
	protected abstract ONDEXRelation retrieveRelation(int id);

	/**
	 * Retrieves a ONDEXRelation by the given from ONDEXConcept, to ONDEXConcept
	 * and RelationType from the repository.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept
	 * @param toConcept
	 *            to ONDEXConcept
	 * @param ofType
	 *            RelationType
	 * @return ONDEXRelation
	 * @throws NullValueException
	 *             if from or to concept are null.
	 */
	protected abstract ONDEXRelation retrieveRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType);

	/**
	 * Retrieves a Set of Relations from the repository.
	 * 
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAll();

	/**
	 * Retrieves a Set of Relations with a given AttributeName from the
	 * repository.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllAttributeName(
			AttributeName attributeName);

	/**
	 * Retrieves a Set of Relations that are connected to a given ONDEXConcept
	 * from the repository.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllConcept(
			ONDEXConcept concept);

	/**
	 * Retrieves a Set of Relations with a given ConceptClass from the
	 * repository.
	 * 
	 * @param conceptClass
	 *            ConceptClass
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllConceptClass(
			ConceptClass conceptClass);

	/**
	 * Retrieves a Set of Relations with a given DataSource from the repository.
	 * 
	 * @param dataSource
	 *            DataSource
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllDataSource(
			DataSource dataSource);

	/**
	 * Retrieves a Set of Relations with a given EvidenceType from the
	 * repository.
	 * 
	 * @param evidenceType
	 *            EvidenceType
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllEvidenceType(
			EvidenceType evidenceType);

	/**
	 * Retrieves a Set of Relations with a given RelationType from the
	 * repository.
	 * 
	 * @param relationType
	 *            RelationType
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllRelationType(
			RelationType relationType);

	/**
	 * Retrieves a Set of Relations with a given tag from the repository.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXRelation>
	 */
	protected abstract Set<ONDEXRelation> retrieveRelationAllTag(
			ONDEXConcept concept) throws AccessDeniedException;

	/**
	 * Retrieves a Set of Concepts that are the tag for other Concepts/Relations
	 * from the repository.
	 * 
	 * @return Set<ONDEXConcept>
	 */
	protected abstract Set<ONDEXConcept> retrieveTags();

	/**
	 * This abstract method stores a given ONDEXConcept.
	 * 
	 * @param sid
	 *            unique id for ondex graph
	 * @param id
	 *            unique id
	 * @param pid
	 *            parser id
	 * @param annotation
	 *            annotation of ONDEXConcept
	 * @param description
	 *            description of ONDEXConcept
	 * @param elementOf
	 *            DataSource of ONDEXConcept
	 * @param ofType
	 *            ConceptClass of ONDEXConcept
	 * @param evidence
	 *            Collection<EvidenceType> of ONDEXConcept
	 * @return ONDEXConcept
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	protected abstract ONDEXConcept storeConcept(long sid, int id, String pid,
			String annotation, String description, DataSource elementOf,
			ConceptClass ofType, Collection<EvidenceType> evidence)
			throws UnsupportedOperationException;

	/**
	 * This abstract method stores a given ONDEXRelation.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            Integer
	 * @param fromConcept
	 *            from ONDEXConcept
	 * @param toConcept
	 *            to ONDEXConcept
	 * @param ofType
	 *            RelationType
	 * @param evidence
	 *            Collection<EvidenceType> of ONDEXRelation
	 * @return ONDEXRelation
	 * @throws UnsupportedOperationException
	 *             if this operation is not supported
	 */
	protected abstract ONDEXRelation storeRelation(long sid, int id,
			ONDEXConcept fromConcept, ONDEXConcept toConcept,
			RelationType ofType, Collection<EvidenceType> evidence)
			throws UnsupportedOperationException;

}
