package net.sourceforge.ondex.core;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class is the core of the ONDEX back-end and contains all Concepts and
 * Relations as well as additional data like DataSources or ConceptClasses in
 * the ONDEXGraphMetaData.
 * 
 * Concepts and Relations can only be created within the tag of this class.
 * Additional data is added using the appropriate methods.
 * 
 * @author taubertj
 * 
 */
public interface ONDEXGraph extends ONDEXAssociable {

	/**
	 * Creates a new ONDEXConcept with the given pid, annotation, description,
	 * DataSource and ConceptClass. Adds the new ONDEXConcept to the list of
	 * Concepts of this graph and returns it.
	 * 
	 * @param pid
	 *            PARSER ID of the new ONDEXConcept
	 * @param annotation
	 *            relevant annotation of the new ONDEXConcept
	 * @param description
	 *            other descriptions of the new ONDEXConcept
	 * @param elementOf
	 *            DataSource of the new ONDEXConcept
	 * @param ofType
	 *            ConceptClass of the new ONDEXConcept
	 * @param evidence
	 *            evidence types
	 * @return new ONDEXConcept
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXConcept createConcept(String pid, String annotation,
			String description, DataSource elementOf, ConceptClass ofType,
			Collection<EvidenceType> evidence) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Creates a new ONDEXRelation with the given fromConcept, toConcept, ofType
	 * and a collection of EvidenceTypes. Adds the new created ONDEXRelation to
	 * the list of Relations of this graph.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept for the new ONDEXRelation
	 * @param toConcept
	 *            to ONDEXConcept for the new ONDEXRelation
	 * @param ofType
	 *            RelationType for the new ONDEXRelation
	 * @param evidence
	 *            collection of EvidenceTypes for the new ONDEXRelation
	 * @return new ONDEXRelation
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ONDEXRelation createRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType,
			Collection<EvidenceType> evidence) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Removes a ONDEXConcept with the given ID from the list of Concepts of
	 * this graph and returns the removed ONDEXConcept or null if unsuccessful.
	 * 
	 * @param id
	 *            unique ID of ONDEXConcept to be removed
	 * @return true if an entity was deleted, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteConcept(int id) throws UnsupportedOperationException;

	/**
	 * Deletes a ONDEXRelation from the list of Relations of this graph
	 * specified by unique Id. Returns deleted ONDEXRelation or null if
	 * unsuccessful.
	 * 
	 * @param id
	 *            Integer
	 * @return true if an entity was deleted, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteRelation(int id) throws UnsupportedOperationException;

	/**
	 * Deletes a ONDEXRelation from the list of Relations of this graph
	 * specified by fromConcept, toConcept and ofType. Returns deleted Relation
	 * or null if unsuccessful.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept
	 * @param toConcept
	 *            to ONDEXConcept
	 * @param ofType
	 *            RelationType
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws NullValueException, UnsupportedOperationException;

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph that are tags for other Concepts/Relations.
	 * 
	 * @return Set<ONDEXConcept>
	 */
	public Set<ONDEXConcept> getAllTags();

	/**
	 * Returns the ONDEXConcept associated with the given ID or null if no such
	 * Concept exists. Returns found ONDEXConcept or null if unsuccessful.
	 * 
	 * @param id
	 *            unique ID of ONDEXConcept
	 * @return found ONDEXConcept
	 */
	public ONDEXConcept getConcept(int id);

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph.
	 * 
	 * @return Set<ONDEXConcept> an unmodifiable set of ondex concepts
	 */
	public Set<ONDEXConcept> getConcepts();

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph for a given AttributeName.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Set<ONDEXConcept> an unmodifiable set of ondex concepets
	 * @throws NullValueException
	 *             if attributeName parameter is null.
	 */
	public Set<ONDEXConcept> getConceptsOfAttributeName(
			AttributeName attributeName) throws NullValueException;

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph for a given ConceptClass.
	 * 
	 * @param conceptClass
	 *            ConceptClass
	 * @return Set<ONDEXConcept>
	 * @throws NullValueException
	 *             if conceptClass parameter is null.
	 */
	public Set<ONDEXConcept> getConceptsOfConceptClass(ConceptClass conceptClass)
			throws NullValueException;

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph for a given DataSource.
	 * 
	 * @param dataSource
	 *            DataSource
	 * @return Set<ONDEXConcept>
	 * @throws NullValueException
	 *             if dataSource parameter is null.
	 */
	public Set<ONDEXConcept> getConceptsOfDataSource(DataSource dataSource)
			throws NullValueException;

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph for a given EvidenceType.
	 * 
	 * @param evidenceType
	 *            EvidenceType
	 * @return Set<ONDEXConcept>
	 * @throws NullValueException
	 *             if evidenceType parameter is null.
	 */
	public Set<ONDEXConcept> getConceptsOfEvidenceType(EvidenceType evidenceType)
			throws NullValueException;

	/**
	 * Returns a Set of all Concepts contained in the list of Concepts of this
	 * graph for a given tag.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXConcept>
	 * @throws NullValueException
	 *             if concept parameter is null.
	 */
	public Set<ONDEXConcept> getConceptsOfTag(ONDEXConcept concept)
			throws NullValueException;

	/**
	 * Creates a factory for ONDEXEntity
	 * 
	 * Not supported in webservice.
	 * 
	 * @return EntityFactory
	 */
	public EntityFactory getFactory();

	/**
	 * Returns the ONDEX graph data associated with this instance of ONDEXGraph.
	 * 
	 * Not supported in webservice.
	 * 
	 * @return current ONDEXGraphMetaData
	 */
	public ONDEXGraphMetaData getMetaData();

	/**
	 * Returns the name of this instance of ONDEXGraph.
	 * 
	 * @return name of graph
	 */
	public String getName();

	/**
	 * Returns a ONDEXRelation from the list of Relations of this graph
	 * specified by unique Id or null if unsuccessful.
	 * 
	 * @param id
	 *            Integer
	 * @return existing ONDEXRelation
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public ONDEXRelation getRelation(int id);

	/**
	 * Returns a ONDEXRelation from the list of Relations of this graph
	 * specified by fromConcept, toConcept and ofType or null if unsuccessful.
	 * 
	 * @param fromConcept
	 *            from ONDEXConcept
	 * @param toConcept
	 *            to ONDEXConcept
	 * @param ofType
	 *            RelationType
	 * @return existing AbstractRelaiton
	 * @throws NullValueException
	 *             if any parameter is null.
	 */
	public ONDEXRelation getRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType)
			throws NullValueException;

	/**
	 * Returns all Relations contained in this graph.
	 * 
	 * @return all AbstractRelations as Set<ONDEXRelation>
	 */
	public Set<ONDEXRelation> getRelations();

	/**
	 * Returns all Relations of a given AttributeName contained in this graph.
	 * 
	 * @param attributeName
	 *            AttributeName
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if attributeName parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfAttributeName(
			AttributeName attributeName) throws NullValueException;

	/**
	 * Returns all Relations connected to a given ONDEXConcept contained in this
	 * graph. This includes all relations where the given concept is the "from"
	 * or "to" within the relation.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if concept parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfConcept(ONDEXConcept concept)
			throws NullValueException;

	/**
	 * Returns all Relations connected (from, to, qualified by) to all Concepts
	 * of a given ConceptClass contained in this graph.
	 * 
	 * @param conceptClass
	 *            ConceptClass
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if conceptClass parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfConceptClass(
			ConceptClass conceptClass) throws NullValueException;

	/**
	 * Returns all Relations connected (from, to, qualified by) to all Concepts
	 * of a given DataSource contained in this graph.
	 * 
	 * @param dataSource
	 *            DataSource
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if dataSource parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfDataSource(DataSource dataSource)
			throws NullValueException;

	/**
	 * Returns all Relations of a given EvidenceType contained in this graph.
	 * 
	 * @param evidenceType
	 *            EvidenceType
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if evidenceType parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfEvidenceType(
			EvidenceType evidenceType) throws NullValueException;

	/**
	 * Returns all Relations of a given RelationType contained in this graph. It
	 * only represents the state of RelationTypes in a AbstractRelationTypeSet
	 * at the time the ONDEXRelation was created.
	 * 
	 * @param relationType
	 *            RelationType
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if relationType parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfRelationType(
			RelationType relationType) throws NullValueException;

	/**
	 * Returns all Relations of a given tag contained in this graph.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @return Set<ONDEXRelation>
	 * @throws NullValueException
	 *             if concept parameter is null.
	 */
	public Set<ONDEXRelation> getRelationsOfTag(ONDEXConcept concept)
			throws NullValueException;

	/**
	 * Returns whether or not this ONDEXGraph is read only.
	 * 
	 * @return true if read only
	 */
	public boolean isReadOnly();

}