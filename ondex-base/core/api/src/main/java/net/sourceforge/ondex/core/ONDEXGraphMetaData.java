package net.sourceforge.ondex.core;

import java.util.Set;

import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.exception.type.WrongParameterException;

/**
 * This class defines additional attributes for the ONDEXGraph class. It
 * provides methods for the manipulation of these attributes.
 * 
 * @author taubertj
 */
public interface ONDEXGraphMetaData extends ONDEXAssociable {

	/**
	 * Used in abstract class of ONDEXGraph
	 * 
	 * Not supported in webservce.
	 * 
	 * @param graph
	 *            the ONDEXGraph to associate with
	 * @throws NullValueException
	 *             if graph parameter is null.
	 * @throws UnsupportedOperationException
	 *             if meta graph is already assigned
	 */
	public void associateGraph(ONDEXGraph graph) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Checks if a AttributeName for a given id exists.
	 * 
	 * @param id
	 *            id of AttributeName to be checked
	 * @return true if AttributeName exists
	 */
	public boolean checkAttributeName(String id);

	/**
	 * Checks if a ConceptClass for a given name exists.
	 * 
	 * @param id
	 *            id of ConceptClass to be checked
	 * @return true if ConceptClass exists
	 */
	public boolean checkConceptClass(String id);

	/**
	 * Checks if a DataSource for a given id exists.
	 * 
	 * @param id
	 *            id of DataSource to be checked
	 * @return true if DataSource exists
	 */
	public boolean checkDataSource(String id);

	/**
	 * Checks if a EvidenceType for a given id exists.
	 * 
	 * @param id
	 *            id of EvidenceType to be checked
	 * @return true if EvidenceType exists
	 */
	public boolean checkEvidenceType(String id);

	/**
	 * Checks if a RelationType for a given id exists.
	 * 
	 * @param id
	 *            id of RelationType to be checked
	 * @return true if RelationType exists
	 */
	public boolean checkRelationType(String id);

	/**
	 * Checks if a Unit for a given id exists.
	 * 
	 * @param id
	 *            id of Unit to be checked
	 * @return true if Unit exists
	 */
	public boolean checkUnit(String id);

	/**
	 * Creates a new AttributeName with the given id, fullname, description,
	 * unit and datatype. Adds the new AttributeName to the list of
	 * AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName Empty Sting allowed.
	 * @param description
	 *            description of the new AttributeName Empty Sting allowed.
	 * @param unit
	 *            unit of the new AttributeName null value allowed.
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName null value allowed.
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public AttributeName createAttributeName(String id, String fullname,
			String description, Unit unit, Class<?> datatype,
			AttributeName specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException;

	/**
	 * Creates a new ConceptClass with the given id, description and parent
	 * ConceptClass. Adds the new ConceptClass to the list of ConceptClasses of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param fullname
	 *            fullname of the new ConceptClass Empty Sting allowed.
	 * @param description
	 *            description of the new ConceptClass Empty Sting allowed.
	 * @param specialisationOf
	 *            parent ConceptClass of the new ConceptClass
	 * @return new ConceptClass null value allowed.
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public ConceptClass createConceptClass(String id, String fullname,
			String description, ConceptClass specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException, WrongParameterException;

	/**
	 * Creates a new DataSource with the given id, full name and description.
	 * Adds the new DataSource to the list of DataSources of this graph.
	 * 
	 * @param id
	 *            id of the new DataSource
	 * @param fullname
	 *            full name of the new DataSource Empty Sting allowed.
	 * @param description
	 *            description of the new DataSource Empty Sting allowed.
	 * @return created DataSource
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public DataSource createDataSource(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException;

	/**
	 * Creates a new EvidenceType with the given id, fullname and description.
	 * Adds the new EvidenceType to the list of EvidenceTypes of this graph.
	 * 
	 * @param id
	 *            id of the new EvidenceType
	 * @param fullname
	 *            fullname of the new EvidenceType Empty Sting allowed.
	 * @param description
	 *            description of the new EvidenceType Empty Sting allowed.
	 * @return EvidenceType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public EvidenceType createEvidenceType(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException;

	/**
	 * Creates a new RelationType with the given id, fullname, description,
	 * inverseName and the additional properties. Adds the new RelationType to
	 * the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType Empty Sting allowed.
	 * @param description
	 *            description of the new RelationType Empty Sting allowed.
	 * @param inverseName
	 *            inverse name of the new RelationType Empty Sting allowed.
	 * @param isAntisymmetric
	 *            antisymmetric property Default is false
	 * @param isReflexive
	 *            reflexive property Default is false
	 * @param isSymmetric
	 *            symmetric property Default is false
	 * @param isTransitiv
	 *            transitiv property Default is false
	 * @param specialisationOf
	 *            parent RelationType null value allowed.
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException;

	/**
	 * Creates a new Unit with the given id and description. Adds the new Unit
	 * to the list of Units of this graph.
	 * 
	 * @param id
	 *            id of the new Unit
	 * @param fullname
	 *            fullname of the new Unit Empty Sting allowed.
	 * @param description
	 *            description of the new Unit Empty Sting allowed.
	 * @return Unit
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 * @throws WrongParameterException
	 *             for wrong id parameter
	 */
	public Unit createUnit(String id, String fullname, String description)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException, WrongParameterException;

	/**
	 * Deletes a AttributeName from the list of AttributeNames of this graph.
	 * The deleted AttributeName is returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of AttributeName to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteAttributeName(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Deletes a ConceptClass from the list of ConceptClasses of this graph. The
	 * deleted ConceptClass is returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of ConceptClass to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteConceptClass(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Deletes a DataSource from the list of DataSources of this graph. The
	 * deleted DataSource is returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of DataSource to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteDataSource(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Deletes a EvidenceType from the list of EvidenceTypes of this graph. The
	 * deleted EvidenceType is returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of EvidenceType to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteEvidenceType(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Deletes a RelationType from the list of RelationTypes of this graph. The
	 * deleted RelationType is returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of RelationType to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteRelationType(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Deletes a Unit from the list of Units of this graph. The deleted Unit is
	 * returned or null if unsuccessful.
	 * 
	 * @param id
	 *            id of Unit to be deleted
	 * @return true if an entity was deleted, false otherwise
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public boolean deleteUnit(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException;

	/**
	 * Returns a AttributeName for a given id or null if unsuccessful.
	 * 
	 * @param id
	 *            id of AttributeName to be returned
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @return AttributeName
	 */
	public AttributeName getAttributeName(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all AttributeNames contained in the list of AttributeNames.
	 * 
	 * @return Set<AttributeName>
	 */
	public Set<AttributeName> getAttributeNames();

	/**
	 * Returns a ConceptClass for a given name or null if unsuccessful.
	 * 
	 * @param id
	 *            id of ConceptClass to be returned
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 */
	public ConceptClass getConceptClass(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all ConceptClasses contained in the list of ConceptClasses.
	 * 
	 * @return Set<ConceptClass>
	 */
	public Set<ConceptClass> getConceptClasses();

	/**
	 * Returns a DataSource for a given id or null if unsuccessful.
	 * 
	 * @param id
	 *            id of DataSource to be returned
	 * @return DataSource for given id
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 */
	public DataSource getDataSource(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all DataSources contained in the list of DataSources.
	 * 
	 * @return Set<DataSource> of all DataSources
	 */
	public Set<DataSource> getDataSources();

	/**
	 * Returns a EvidenceType for a given id or null if unsuccessful.
	 * 
	 * @param id
	 *            id of EvidenceType to be returned
	 * @return EvidenceType
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 */
	public EvidenceType getEvidenceType(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all EvidenceTypes contained in the list of EvidenceTypes.
	 * 
	 * @return Set<EvidenceType>
	 */
	public Set<EvidenceType> getEvidenceTypes();

	/**
	 * Creates a factory for GraphMetaData
	 * 
	 * Not supported in webservice.
	 * 
	 * @return MetaDataFactory
	 */
	public MetaDataFactory getFactory();

	/**
	 * Returns a RelationType for a given id or null if unsuccessful.
	 * 
	 * @param id
	 *            id of RelationType to be returned
	 * @return RelationType
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 */
	public RelationType getRelationType(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all RelationTypes contained in the list of RelationTypes.
	 * 
	 * @return Set<RelationType>
	 */
	public Set<RelationType> getRelationTypes();

	/**
	 * Returns a Unit for a given id or null if unsuccessful.
	 * 
	 * @param id
	 *            id of Unit to be returned
	 * @return Unit
	 * @throws NullValueException
	 *             if id parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 */
	public Unit getUnit(String id) throws NullValueException,
			EmptyStringException;

	/**
	 * Returns all Units contained in the list of Units.
	 * 
	 * @return Set<Unit>
	 */
	public Set<Unit> getUnits();

}