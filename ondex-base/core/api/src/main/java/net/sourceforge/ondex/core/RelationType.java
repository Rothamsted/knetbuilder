package net.sourceforge.ondex.core;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This class is part of a RelationTypeSet and specifies one RelationType of a
 * Relation. In addition to the mandatory field id and the optional field
 * description, an inverse name and several properties can be defined. A
 * RelationType can be a specialisation of another RelationType.
 * 
 * @author taubertj
 * 
 */
@XmlJavaTypeAdapter(net.sourceforge.ondex.webservice.Adapters.AnyTypeAdapter.class)
public interface RelationType extends MetaData, Hierarchy<RelationType> {

	/**
	 * Returns the inverse name of this instance of RelationType.
	 * 
	 * @return String
	 */
	public String getInverseName();

	/**
	 * Returns whether or not this RelationType is antisymmetric.
	 * 
	 * @return boolean
	 */
	public boolean isAntisymmetric();

	/**
	 * Returns whether or not this RelationType is reflexive.
	 * 
	 * @return boolean
	 */
	public boolean isReflexive();

	/**
	 * Returns whether or not this RelationType is symmetric.
	 * 
	 * @return boolean
	 */
	public boolean isSymmetric();

	/**
	 * Returns whether or not this RelationType is transitive.
	 * 
	 * @return boolean
	 */
	public boolean isTransitiv();

	/**
	 * Sets whether or not this RelationType is antisymmetric.
	 * 
	 * @param isAntisymmetric
	 *            new antisymmetric property
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setAntisymmetric(boolean isAntisymmetric)
			throws UnsupportedOperationException;

	/**
	 * Sets the inverse name of this instance of RelationType.
	 * 
	 * @param inverseName
	 *            new inverse name
	 * @throws NullValueException
	 *             if inverseName parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setInverseName(String inverseName) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Sets whether or not this RelationType is reflexive.
	 * 
	 * @param isReflexive
	 *            new reflexive property
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setReflexive(boolean isReflexive)
			throws UnsupportedOperationException;

	/**
	 * Sets whether or not this RelationType is symmetric.
	 * 
	 * @param isSymmetric
	 *            new symmetric property
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setSymmetric(boolean isSymmetric)
			throws UnsupportedOperationException;

	/**
	 * Sets whether or not this RelationType is transitive.
	 * 
	 * @param isTransitiv
	 *            new transitive property
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setTransitiv(boolean isTransitiv)
			throws UnsupportedOperationException;

}
