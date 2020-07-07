package net.sourceforge.ondex.core.base;

import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaDataFactory;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.exception.type.WrongParameterException;

/**
 * This class defines additional attributes for the AbstractONDEXGraph class. It
 * provides methods for the manipulation of these attributes.
 * 
 * @author taubertj
 * @version 09.11.2000
 */
public abstract class AbstractONDEXGraphMetaData extends AbstractONDEXEntity
		implements ONDEXGraphMetaData {

	/**
	 * Pattern to process metadata IDs
	 */
	public static Pattern WHITESPACE;

	/**
	 * compile patterns
	 */
	static {
		WHITESPACE = Pattern.compile("\\s");
	}

	@Override
	public void associateGraph(ONDEXGraph g)
			throws UnsupportedOperationException {

		// check for duplicated assignment
		if (sid == -1L || sid == g.getSID()) {
			sid = g.getSID();
		} else {
			throw new UnsupportedOperationException(
					"MetaData is already associated to another graph!");
		}
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkAttributeName(java
	 *      .lang.String)
	 */
	@Override
	public boolean checkAttributeName(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsAttributeName(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkConceptClass(java.
	 *      lang.String)
	 */
	@Override
	public boolean checkConceptClass(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsConceptClass(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkDataSource(java.lang
	 *      .String)
	 */
	@Override
	public boolean checkDataSource(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsDataSource(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkEvidenceType(java.
	 *      lang.String)
	 */
	@Override
	public boolean checkEvidenceType(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsEvidenceType(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkRelationType(java.
	 *      lang.String)
	 */
	@Override
	public boolean checkRelationType(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsRelationType(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#checkUnit(java.lang.String)
	 */
	@Override
	public boolean checkUnit(String id) {

		// null values not allowed
		if (id == null)
			return false;

		// empty strings not allowed
		if (id.trim().length() == 0)
			return false;

		return existsUnit(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createAttributeName(java
	 *      .lang.String, java.lang.String, java.lang.String,
	 *      net.sourceforge.ondex.core.Unit, java.lang.Class,
	 *      net.sourceforge.ondex.core.AttributeName)
	 */
	@Override
	public AttributeName createAttributeName(String id, String fullname,
			String description, Unit unit, Class<?> datatype,
			AttributeName specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameDescriptionNull"));

		// null values not allowed
		if (datatype == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameDataTypeNull"));

		AttributeName an = new AttributeNameImpl(sid, id, fullname,
				description, unit, datatype.getName(), specialisationOf);
		return storeAttributeName(an);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createConceptClass(java
	 *      .lang.String, java.lang.String, java.lang.String,
	 *      net.sourceforge.ondex.core.ConceptClass)
	 */
	@Override
	public ConceptClass createConceptClass(String id, String fullname,
			String description, ConceptClass specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException, WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassDescriptionNull"));

		ConceptClass cc = new ConceptClassImpl(sid, id, fullname, description,
				specialisationOf);
		return storeConceptClass(cc);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createDataSource(java.lang
	 *      .String, java.lang.String, java.lang.String)
	 */
	@Override
	public DataSource createDataSource(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceDescriptionNull"));

		DataSource dataSource = new DataSourceImpl(sid, id, fullname,
				description);
		return storeDataSource(dataSource);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createEvidenceType(java
	 *      .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public EvidenceType createEvidenceType(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeDescriptionNull"));

		EvidenceType evitype = new EvidenceTypeImpl(sid, id, fullname,
				description);
		return storeEvidenceType(evitype);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createRelationType(java
	 *      .lang.String, java.lang.String, java.lang.String, java.lang.String,
	 *      boolean, boolean, boolean, boolean,
	 *      net.sourceforge.ondex.core.RelationType)
	 */
	@Override
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException,
			WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeDescriptionNull"));

		// null values not allowed
		if (inverseName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeInverseNameNull"));

		RelationType rt = new RelationTypeImpl(sid, id, fullname, description,
				inverseName, isAntisymmetric, isReflexive, isSymmetric,
				isTransitiv, specialisationOf);
		return storeRelationType(rt);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#createUnit(java.lang.String
	 *      , java.lang.String, java.lang.String)
	 */
	@Override
	public Unit createUnit(String id, String fullname, String description)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException, WrongParameterException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameEmpty"));

		// whitespace in ID not allows
		if (WHITESPACE.matcher(id.trim()).find())
			throw new WrongParameterException("ID contained white-space: '"
					+ id + "'");

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitFullNameNull"));

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitDescriptionNull"));

		Unit unit = new UnitImpl(sid, id, fullname, description);
		return storeUnit(unit);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteAttributeName(java
	 *      .lang.String)
	 */
	@Override
	public boolean deleteAttributeName(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameEmpty"));

		return removeAttributeName(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteConceptClass(java
	 *      .lang.String)
	 */
	@Override
	public boolean deleteConceptClass(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameEmpty"));

		return removeConceptClass(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteDataSource(java.lang
	 *      .String)
	 */
	@Override
	public boolean deleteDataSource(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDEmpty"));

		return removeDataSource(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteEvidenceType(java
	 *      .lang.String)
	 */
	@Override
	public boolean deleteEvidenceType(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameEmpty"));

		return removeEvidenceType(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteRelationType(java
	 *      .lang.String)
	 */
	@Override
	public boolean deleteRelationType(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameEmpty"));

		return removeRelationType(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#deleteUnit(java.lang.String
	 *      )
	 */
	@Override
	public boolean deleteUnit(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameEmpty"));

		return removeUnit(id);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getAttributeName(java.lang
	 *      .String)
	 */
	@Override
	public AttributeName getAttributeName(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.AttributeNameNameEmpty"));

		return retrieveAttributeName(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getAttributeNames()
	 */
	@Override
	public Set<AttributeName> getAttributeNames() {
		return BitSetFunctions.unmodifiableSet(retrieveAttributeNameAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getConceptClass(java.lang
	 *      .String)
	 */
	@Override
	public ConceptClass getConceptClass(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.ConceptClassNameEmpty"));

		return retrieveConceptClass(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getConceptClasses()
	 */
	@Override
	public Set<ConceptClass> getConceptClasses() {
		return BitSetFunctions.unmodifiableSet(retrieveConceptClassAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getDataSource(java.lang
	 *      .String)
	 */
	@Override
	public DataSource getDataSource(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.DataSourceIDEmpty"));

		return retrieveDataSource(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getDataSources()
	 */
	@Override
	public Set<DataSource> getDataSources() {
		return BitSetFunctions.unmodifiableSet(retrieveDataSourceAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getEvidenceType(java.lang
	 *      .String)
	 */
	@Override
	public EvidenceType getEvidenceType(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.EvidenceTypeNameEmpty"));

		return retrieveEvidenceType(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getEvidenceTypes()
	 */
	@Override
	public Set<EvidenceType> getEvidenceTypes() {
		return BitSetFunctions.unmodifiableSet(retrieveEvidenceTypeAll());
	}

	@Override
	public MetaDataFactory getFactory() {
		return new MetaDataFactory(this);
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getRelationType(java.lang
	 *      .String)
	 */
	@Override
	public RelationType getRelationType(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.RelationTypeNameEmpty"));

		return retrieveRelationType(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getRelationTypes()
	 */
	@Override
	public Set<RelationType> getRelationTypes() {
		return BitSetFunctions.unmodifiableSet(retrieveRelationTypeAll());
	}

	/**
	 * 
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getUnit(java.lang.String)
	 */
	@Override
	public Unit getUnit(String id) throws NullValueException,
			EmptyStringException {

		// null values not allowed
		if (id == null)
			throw new NullValueException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameNull"));

		// empty strings not allowed
		if (id.trim().length() == 0)
			throw new EmptyStringException(
					Config.properties
							.getProperty("AbstractONDEXGraphMetaData.UnitNameEmpty"));

		return retrieveUnit(id);
	}

	/**
	 * @return UnmodifiableSet
	 * @see net.sourceforge.ondex.core.ONDEXGraphMetaData#getUnits()
	 */
	@Override
	public Set<Unit> getUnits() {
		return BitSetFunctions.unmodifiableSet(retrieveUnitAll());
	}

	/**
	 * Checks if a AttributeName by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of AttributeName to be checked
	 * @return boolean
	 */
	protected abstract boolean existsAttributeName(String id);

	/**
	 * Checks if a ConceptClass by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of ConceptClass to be checked
	 * @return boolean
	 */
	protected abstract boolean existsConceptClass(String id);

	/**
	 * Checks if a DataSource by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of DataSource to be checked
	 * @return boolean
	 */
	protected abstract boolean existsDataSource(String id);

	/**
	 * Check if a EvidenceType by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of EvidenceType to be checked
	 * @return boolean
	 */
	protected abstract boolean existsEvidenceType(String id);

	/**
	 * Checks if a RelationType by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of RelationType to be checked
	 * @return boolean
	 */
	protected abstract boolean existsRelationType(String id);

	/**
	 * Checks if a Unit by a given id exists in the repository.
	 * 
	 * @param id
	 *            id of Unit to be checked
	 * @return boolean
	 */
	protected abstract boolean existsUnit(String id);

	/**
	 * Removes a AttributeName by a given id from the repository.
	 * 
	 * @param id
	 *            id of AttributeName to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeAttributeName(String id)
			throws UnsupportedOperationException;

	/**
	 * Removes a ConceptClass by a given id from the repository.
	 * 
	 * @param id
	 *            id of ConceptClass to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeConceptClass(String id)
			throws UnsupportedOperationException;

	/**
	 * Removes a DataSource by a given id from the repository.
	 * 
	 * @param id
	 *            id of DataSource to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeDataSource(String id)
			throws UnsupportedOperationException;

	/**
	 * Removes a EvidenceType by a given id from the repository.
	 * 
	 * @param id
	 *            id of EvidenceType to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeEvidenceType(String id)
			throws UnsupportedOperationException;

	/**
	 * Removes a RelationType by a given id from the repository.
	 * 
	 * @param id
	 *            id of RelationType to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeRelationType(String id)
			throws UnsupportedOperationException;

	/**
	 * Removes a Unit by a given id from the repository.
	 * 
	 * @param id
	 *            id of Unit to be removed
	 * @return true if the entity was removed, false otherwise
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract boolean removeUnit(String id)
			throws UnsupportedOperationException;

	/**
	 * Retrieves a AttributeName by a given id from the repository.
	 * 
	 * @param id
	 *            id of AttributeName to be retrieved
	 * @return AttributeName
	 */
	protected abstract AttributeName retrieveAttributeName(String id);

	/**
	 * Returns all AttributeNames contained in the repository.
	 * 
	 * @return Set<AttributeName>
	 */
	protected abstract Set<AttributeName> retrieveAttributeNameAll();

	/**
	 * Retrieves a ConceptClass by a given id from the repository.
	 * 
	 * @param id
	 *            id of ConceptClass to be retrieved
	 * @return ConceptClass
	 */
	protected abstract ConceptClass retrieveConceptClass(String id);

	/**
	 * Returns all ConceptClasses contained in the repository.
	 * 
	 * @return Set<ConceptClass>
	 */
	protected abstract Set<ConceptClass> retrieveConceptClassAll();

	/**
	 * Retrieves a DataSource by a given id from the repository.
	 * 
	 * @param id
	 *            id of DataSource to be retrieved
	 * @return DataSource
	 */
	protected abstract DataSource retrieveDataSource(String id);

	/**
	 * Returns all DataSources contained in the repository.
	 * 
	 * @return Set<DataSource>
	 */
	protected abstract Set<DataSource> retrieveDataSourceAll();

	/**
	 * Retrieves a EvidenceType by a given id from the repository.
	 * 
	 * @param id
	 *            id of EvidenceType to be retrieved
	 * @return EvidenceType
	 */
	protected abstract EvidenceType retrieveEvidenceType(String id);

	/**
	 * Returns all EvidenceTypes contained in the repository.
	 * 
	 * @return Set<EvidenceType>
	 */
	protected abstract Set<EvidenceType> retrieveEvidenceTypeAll();

	/**
	 * Retrieves a RelationType by a given id from the repository.
	 * 
	 * @param id
	 *            id of RelationType to be retrieved
	 * @return RelationType
	 */
	protected abstract RelationType retrieveRelationType(String id);

	/**
	 * Returns all RelationTypes contained in the repository.
	 * 
	 * @return Set<RelationType>
	 */
	protected abstract Set<RelationType> retrieveRelationTypeAll();

	/**
	 * Retrieves a Unit by a given id from the repository.
	 * 
	 * @param id
	 *            id of Unit to be retrieved
	 * @return Unit
	 */
	protected abstract Unit retrieveUnit(String id);

	/**
	 * Returns all Units contained in the repository.
	 * 
	 * @return Set<Unit>
	 */
	protected abstract Set<Unit> retrieveUnitAll();

	/**
	 * Stores the given AttributeName in the repository.
	 * 
	 * @param an
	 *            AttributeName to store
	 * @return AttributeName
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract AttributeName storeAttributeName(AttributeName an)
			throws UnsupportedOperationException;

	/**
	 * Stores the given ConceptClass in the repository.
	 * 
	 * @param cc
	 *            ConceptClass to store
	 * @return ConceptClass
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract ConceptClass storeConceptClass(ConceptClass cc)
			throws UnsupportedOperationException;

	/**
	 * Stores the given DataSource in the repository.
	 * 
	 * @param dataSource
	 *            DataSource to store
	 * @return DataSource
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract DataSource storeDataSource(DataSource dataSource)
			throws UnsupportedOperationException;

	/**
	 * Stores the given EvidenceType in the repository.
	 * 
	 * @param evitype
	 *            EvidenceType to store
	 * @return EvidenceType
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract EvidenceType storeEvidenceType(EvidenceType evitype)
			throws UnsupportedOperationException;

	/**
	 * Stores the given RelationType in the repository.
	 * 
	 * @param rt
	 *            RelationType to store
	 * @return RelationType
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract RelationType storeRelationType(RelationType rt)
			throws UnsupportedOperationException;

	/**
	 * Stores the given Unit in the repository.
	 * 
	 * @param unit
	 *            Unit to store
	 * @return Unit
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	protected abstract Unit storeUnit(Unit unit)
			throws UnsupportedOperationException;

}
