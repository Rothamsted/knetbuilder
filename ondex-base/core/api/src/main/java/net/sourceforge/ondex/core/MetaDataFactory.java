package net.sourceforge.ondex.core;

import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * Factory class to provide method stubs for meta data creation.
 * 
 * @author sckuo
 */
public final class MetaDataFactory {

	/**
	 * contained meta data
	 */
	private final ONDEXGraphMetaData md;

	/**
	 * Constructor for actual implementation of ONDEXGraphMetaData.
	 * 
	 * @param metadata
	 */
	public MetaDataFactory(ONDEXGraphMetaData metadata) {
		this.md = metadata;
	}

	/**
	 * Creates a new AttributeName with the given id and datatype. Adds the new
	 * AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, Class<?> datatype)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, "", datatype);
	}

	/**
	 * Creates a new AttributeName with the given id and datatype. Adds the new
	 * AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, Class<?> datatype,
			AttributeName specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createAttributeName(id, "", datatype, specialisationOf);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname and datatype.
	 * Adds the new AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			Class<?> datatype) throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, fullname, "", datatype);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname and datatype.
	 * Adds the new AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			Class<?> datatype, AttributeName specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, fullname, "", datatype, specialisationOf);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, description and
	 * datatype. Adds the new AttributeName to the list of AttributeNames of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param description
	 *            description of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			String description, Class<?> datatype) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createAttributeName(id, fullname, description, datatype, null);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, description and
	 * datatype. Adds the new AttributeName to the list of AttributeNames of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param description
	 *            description of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			String description, Class<?> datatype,
			AttributeName specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createAttributeName(id, fullname, description, null,
				datatype, specialisationOf);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, description,
	 * unit and datatype. Adds the new AttributeName to the list of
	 * AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param description
	 *            description of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			String description, Unit unit, Class<?> datatype)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createAttributeName(id, fullname, description, unit,
				datatype, null);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, description,
	 * unit and datatype. Adds the new AttributeName to the list of
	 * AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param description
	 *            description of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			String description, Unit unit, Class<?> datatype,
			AttributeName specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createAttributeName(id, fullname, description, unit,
				datatype, specialisationOf);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, unit and
	 * datatype. Adds the new AttributeName to the list of AttributeNames of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			Unit unit, Class<?> datatype) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createAttributeName(id, fullname, "", unit, datatype);
	}

	/**
	 * Creates a new AttributeName with the given id, fullname, unit and
	 * datatype. Adds the new AttributeName to the list of AttributeNames of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param fullname
	 *            fullname of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, String fullname,
			Unit unit, Class<?> datatype, AttributeName specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, fullname, "", unit, datatype,
				specialisationOf);
	}

	/**
	 * Creates a new AttributeName with the given id, unit and datatype. Adds
	 * the new AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, Unit unit,
			Class<?> datatype) throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, "", unit, datatype);
	}

	/**
	 * Creates a new AttributeName with the given id, unit and datatype. Adds
	 * the new AttributeName to the list of AttributeNames of this graph.
	 * 
	 * @param id
	 *            id of the new AttributeName
	 * @param unit
	 *            unit of the new AttributeName
	 * @param datatype
	 *            datatype of the new AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of the new AttributeName
	 * @return AttributeName
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public AttributeName createAttributeName(String id, Unit unit,
			Class<?> datatype, AttributeName specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createAttributeName(id, "", unit, datatype, specialisationOf);
	}

	/**
	 * Creates a new ConceptClass with the given id and empty description. Adds
	 * the new ConceptClass to the list of ConceptClasses of this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createConceptClass(id, "");
	}

	/**
	 * Creates a new ConceptClass with the given id and parent ConceptClass.
	 * Adds the new ConceptClass to the list of ConceptClasses of this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param specialisationOf
	 *            parent ConceptClass of the new ConceptClass
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id,
			ConceptClass specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createConceptClass(id, "", specialisationOf);
	}

	/**
	 * Creates a new ConceptClass with the given id, fullname and empty
	 * description. Adds the new ConceptClass to the list of ConceptClasses of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param fullname
	 *            fullname of the new ConceptClass
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id, String fullname)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createConceptClass(id, fullname, "");
	}

	/**
	 * Creates a new ConceptClass with the given id, fullname and parent
	 * ConceptClass. Adds the new ConceptClass to the list of ConceptClasses of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param fullname
	 *            fullname of the new ConceptClass
	 * @param specialisationOf
	 *            parent ConceptClass of the new ConceptClass
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id, String fullname,
			ConceptClass specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createConceptClass(id, fullname, "", specialisationOf);
	}

	/**
	 * Creates a new ConceptClass with the given id, fullname and description.
	 * Adds the new ConceptClass to the list of ConceptClasses of this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param fullname
	 *            fullname of the new ConceptClass
	 * @param description
	 *            description of the new ConceptClass
	 * @return ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createConceptClass(id, fullname, description, null);
	}

	/**
	 * Creates a new ConceptClass with the given id, description and parent
	 * ConceptClass. Adds the new ConceptClass to the list of ConceptClasses of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new ConceptClass
	 * @param fullname
	 *            fullname of the new ConceptClass
	 * @param description
	 *            description of the new ConceptClass
	 * @param specialisationOf
	 *            parent ConceptClass of the new ConceptClass
	 * @return new ConceptClass
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public ConceptClass createConceptClass(String id, String fullname,
			String description, ConceptClass specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createConceptClass(id, fullname, description,
				specialisationOf);
	}

	/**
	 * Creates a new DataSource with the given id and empty description. Adds
	 * the new DataSource to the list of DataSources of this graph.
	 * 
	 * @param id
	 *            id of the new DataSource
	 * @return created DataSource
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public DataSource createDataSource(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createDataSource(id, "");
	}

	/**
	 * Creates a new DataSource with the given id, full name and empty
	 * description. Adds the new DataSource to the list of DataSources of this
	 * graph.
	 * 
	 * @param id
	 *            id of the new DataSource
	 * @param fullname
	 *            full name of the new DataSource
	 * @return created DataSource
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public DataSource createDataSource(String id, String fullname)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createDataSource(id, fullname, "");
	}

	/**
	 * Creates a new DataSource with the given id, full name and description.
	 * Adds the new DataSource to the list of DataSources of this graph.
	 * 
	 * @param id
	 *            id of the new DataSource
	 * @param fullname
	 *            full name of the new DataSource
	 * @param description
	 *            description of the new DataSource
	 * @return created DataSource
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public DataSource createDataSource(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createDataSource(id, fullname, description);
	}

	/**
	 * Creates a new EvidenceType with the given id and empty description. Adds
	 * the new EvidenceType to the list of EvidenceTypes of this graph.
	 * 
	 * @param id
	 *            id of the new EvidenceType
	 * @return EvidenceType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public EvidenceType createEvidenceType(String id)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createEvidenceType(id, "");
	}

	/**
	 * Creates a new EvidenceType with the given id, fullname and empty
	 * description. Adds the new EvidenceType to the list of EvidenceTypes of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new EvidenceType
	 * @param fullname
	 *            fullname of the new EvidenceType
	 * @return EvidenceType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public EvidenceType createEvidenceType(String id, String fullname)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createEvidenceType(id, fullname, "");
	}

	/**
	 * Creates a new EvidenceType with the given id, fullname and description.
	 * Adds the new EvidenceType to the list of EvidenceTypes of this graph.
	 * 
	 * @param id
	 *            id of the new EvidenceType
	 * @param fullname
	 *            fullname of the new EvidenceType
	 * @param description
	 *            description of the new EvidenceType
	 * @return EvidenceType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public EvidenceType createEvidenceType(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createEvidenceType(id, fullname, description);
	}

	/**
	 * Creates a new RelationType with the given id. Adds the new RelationType
	 * to the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, "");
	}

	/**
	 * Creates a new RelationType with the given id and the additional
	 * properties. Adds the new RelationType to the list of RelationTypes of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, "", isAntisymmetric, isReflexive,
				isSymmetric, isTransitiv);
	}

	/**
	 * Creates a new RelationType with the given id, the additional properties
	 * and parent RelationType. Adds the new RelationType to the list of
	 * RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, "", isAntisymmetric, isReflexive,
				isSymmetric, isTransitiv, specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id and parent RelationType.
	 * Adds the new RelationType to the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, "", specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id and fullname. Adds the new
	 * RelationType to the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, fullname, "");
	}

	/**
	 * Creates a new RelationType with the given id, fullname and the additional
	 * properties. Adds the new RelationType to the list of RelationTypes of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric,
			boolean isTransitiv) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, fullname, "", isAntisymmetric,
				isReflexive, isSymmetric, isTransitiv);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, the additional
	 * properties and parent RelationType. Adds the new RelationType to the list
	 * of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric,
			boolean isTransitiv, RelationType specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, fullname, "", isAntisymmetric,
				isReflexive, isSymmetric, isTransitiv, specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id, fullname and parent
	 * RelationType. Adds the new RelationType to the list of RelationTypes of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, fullname, "", specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id, fullname and description.
	 * Adds the new RelationType to the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, fullname, description, "");
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description and
	 * the additional properties. Adds the new RelationType to the list of
	 * RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, boolean isAntisymmetric, boolean isReflexive,
			boolean isSymmetric, boolean isTransitiv)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, fullname, description, "",
				isAntisymmetric, isReflexive, isSymmetric, isTransitiv);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description, the
	 * additional properties and parent RelationType. Adds the new RelationType
	 * to the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, boolean isAntisymmetric, boolean isReflexive,
			boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createRelationType(id, fullname, description, "",
				isAntisymmetric, isReflexive, isSymmetric, isTransitiv,
				specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description and
	 * parent RelationType. Adds the new RelationType to the list of
	 * RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, RelationType specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return createRelationType(id, fullname, description, "",
				specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description and
	 * inverseName. Adds the new RelationType to the list of RelationTypes of
	 * this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param inverseName
	 *            inverse name of the new RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createRelationType(id, fullname, description, inverseName,
				false, false, false, false);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description,
	 * inverseName and the additional properties. Adds the new RelationType to
	 * the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param inverseName
	 *            inverse name of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createRelationType(id, fullname, description, inverseName,
				isAntisymmetric, isReflexive, isSymmetric, isTransitiv, null);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description,
	 * inverseName and the additional properties. Adds the new RelationType to
	 * the list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param inverseName
	 *            inverse name of the new RelationType
	 * @param isAntisymmetric
	 *            antisymmetric property
	 * @param isReflexive
	 *            reflexive property
	 * @param isSymmetric
	 *            symmetric property
	 * @param isTransitiv
	 *            transitive property
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return md.createRelationType(id, fullname, description, inverseName,
				isAntisymmetric, isReflexive, isSymmetric, isTransitiv,
				specialisationOf);
	}

	/**
	 * Creates a new RelationType with the given id, fullname, description,
	 * inverseName and parent RelationType. Adds the new RelationType to the
	 * list of RelationTypes of this graph.
	 * 
	 * @param id
	 *            id of the new RelationType
	 * @param fullname
	 *            fullname of the new RelationType
	 * @param description
	 *            description of the new RelationType
	 * @param inverseName
	 *            inverse name of the new RelationType
	 * @param specialisationOf
	 *            parent RelationType
	 * @return RelationType
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public RelationType createRelationType(String id, String description,
			String fullname, String inverseName, RelationType specialisationOf)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createRelationType(id, fullname, description, inverseName,
				false, false, false, false, specialisationOf);
	}

	/**
	 * Creates a new Unit with the given id and empty description. Adds the new
	 * Unit to the list of Units of this graph.
	 * 
	 * @param id
	 *            id of the new Unit
	 * @return Unit
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public Unit createUnit(String id) throws NullValueException,
			EmptyStringException, UnsupportedOperationException {
		return createUnit(id, "");
	}

	/**
	 * Creates a new Unit with the given id, fullname and empty description.
	 * Adds the new Unit to the list of Units of this graph.
	 * 
	 * @param id
	 *            id of the new Unit
	 * @param fullname
	 *            fullname of the new Unit
	 * @return Unit
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public Unit createUnit(String id, String fullname)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createUnit(id, fullname, "");
	}

	/**
	 * Creates a new Unit with the given id and description. Adds the new Unit
	 * to the list of Units of this graph.
	 * 
	 * @param id
	 *            id of the new Unit
	 * @param fullname
	 *            fullname of the new Unit
	 * @param description
	 *            description of the new Unit
	 * @return Unit
	 * @throws NullValueException
	 *             if any parameter is null.
	 * @throws EmptyStringException
	 *             if id parameter is an empty string.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public Unit createUnit(String id, String fullname, String description)
			throws NullValueException, EmptyStringException,
			UnsupportedOperationException {
		return md.createUnit(id, fullname, description);
	}
}
