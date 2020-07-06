package net.sourceforge.ondex.core;

import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This super class represents a generalised data structure for Concepts and
 * Relations. It requires a attribute name and attribute value.
 * 
 * @author taubertj
 */
public interface Attribute extends ONDEXAssociable,
		Instantiation<AttributeName>, Comparable<Attribute> {

	/**
	 * Returns the id of the ONDEXConcept or ONDEXRelation that this Attribute
	 * is associated to.
	 * 
	 * @return int id
	 */
	public int getOwnerId();

	/**
	 * Returns the class of the owning entity. Could be either ONDEXConcept or
	 * ONDEXRelation or a relevant sub-class.
	 * 
	 * @return Class<? extends ONDEXEntity>
	 */
	public Class<? extends ONDEXEntity> getOwnerClass();

	/**
	 * Returns the value stored with this instance of Attribute
	 * 
	 * @return Object
	 */
	public Object getValue();

	/**
	 * Returns whether or not this Attribute will be part of the index.
	 * 
	 * @return boolean
	 */
	public boolean isDoIndex();

	/**
	 * Sets whether or not this Attribute will be indexed.
	 * 
	 * @param doIndex
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setDoIndex(boolean doIndex)
			throws UnsupportedOperationException;

	/**
	 * Sets the value stored with this instance of Attribute
	 * 
	 * @param value
	 *            new Object to be stored
	 * @throws NullValueException
	 *             if value parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setValue(Object value) throws NullValueException,
			UnsupportedOperationException;
}