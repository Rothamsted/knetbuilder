package net.sourceforge.ondex.core;

/**
 * This class describes the attributes used in a Attribute class. It has a
 * mandatory name and datatype field, an optional description and an optional
 * unit field.
 * 
 * @author taubertj
 * 
 */
public interface AttributeName extends MetaData, Hierarchy<AttributeName> {

	/**
	 * Returns the data type of this instance of AttributeName.
	 * 
	 * @return Class<?>
	 */
	public Class<?> getDataType();

	/**
	 * Returns the data type of this instance of AttributeName as String.
	 * 
	 * @return String
	 */
	public String getDataTypeAsString();

	/**
	 * Returns the unit of this instance of AttributeName.
	 * 
	 * @return Unit
	 */
	public Unit getUnit();

	/**
	 * Sets the unit of this instance of AttributeName.
	 * 
	 * @param unit
	 *            new Unit for this AttributeName
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setUnit(Unit unit) throws UnsupportedOperationException;
}
