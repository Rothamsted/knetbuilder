package net.sourceforge.ondex.core;

import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * This is the parent class for information classes like DataSource or
 * ConceptClass. It provides getter and setter methodes for the mandatory field
 * id and the optional fields fullname and description.
 * 
 * @author taubertj
 * 
 */
public interface MetaData extends ONDEXAssociable, Comparable<MetaData> {
	// fixme: It should be the direct sub-interfaces of MetaData that extend
	// Comparable, as it makes no sense to compare
	// one type of MetaData with another.

	/**
	 * Comparable is here used for sorting for display purposes. Canonical
	 * definition is that sorting is on the basis of the ids. i.e. the
	 * corresponding overridden compareTo should be passed to String compareTo.
	 * 
	 */

	/**
	 * Returns the description of this instance of Information.
	 * 
	 * @return String
	 */
	public String getDescription();

	/**
	 * Returns the fullname of this instance of Information.
	 * 
	 * @return String
	 */
	public String getFullname();

	/**
	 * Returns the id of this instance of Information.
	 * <p/>
	 * This ID will never contain characters which are illegal as part of a URI.
	 * 
	 * @return String
	 */
	public String getId();

	/**
	 * Sets the description of this instance of Information.
	 * 
	 * @param description
	 *            new description of this Information
	 * @throws NullValueException
	 *             if description parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setDescription(String description) throws NullValueException,
			UnsupportedOperationException;

	/**
	 * Sets the fullname of this instance of Information.
	 * 
	 * @param fullname
	 *            new fullname of this Information
	 * @throws NullValueException
	 *             if fullname parameter is null.
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	public void setFullname(String fullname) throws NullValueException,
			UnsupportedOperationException;
}
