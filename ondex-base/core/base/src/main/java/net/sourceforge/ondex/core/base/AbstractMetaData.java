package net.sourceforge.ondex.core.base;

import java.io.Serializable;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.api.config.Config;
import net.sourceforge.ondex.core.api.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * Abstract meta-data class. Suitable for deriving your own meta-data trees.
 * 
 * @author Matthew Pocock
 */
public abstract class AbstractMetaData extends AbstractONDEXEntity 
      implements MetaData, Serializable {

	/**
	 * normally short id
	 */
	protected final String id;

	/**
	 * longer name
	 */
	protected String fullname;

	/**
	 * longer description
	 */
	protected String description;

	/**
	 * Constructor for internal use only.
	 * <p/>
	 * Please ensure that the id is already URL-encoding safe. No checks are
	 * done here.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this information entity
	 * @param fullname
	 *            fullname of this information entity
	 * @param description
	 *            description of this information entity
	 */
	protected AbstractMetaData(long sid, String id, String fullname, String description) {
		this.sid = sid;
		this.id = id.intern();
		this.fullname = fullname.intern();
		this.description = description.intern();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFullname() {
		return fullname;
	}

	@Override
	public void setFullname(String fullname) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (fullname == null)
			throw new NullValueException(
					Config.properties.getProperty("MetaData.FullNameNull"));

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.fullname = fullname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (description == null)
			throw new NullValueException(
					Config.properties.getProperty("MetaData.DescriptionNull"));

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		return equals ( this.getClass (), o );
	}
	
	/**
	 * Like {@link #equals(Object)}, but allows you to check that the object is an instance of a specific
	 * class, rather than {@link MetaData}. This is necessary because, for instance, an attribute type and
	 * a concept class can have the same ID.
	 *  
	 */
	protected <MC extends MetaData> boolean equals ( Class<MC> refClass, Object o ) {
		if (o == null) return false;
		if (o == this) return true;
		Class<?> oc = o.getClass ();
		if ( !refClass.isAssignableFrom ( oc ) ) return false;
		return this.id.equals ( ( (MetaData) o).getId() );
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public int compareTo(MetaData o) {
		return this.getId().compareTo(o.getId());
	}

	@Override
	public String toString() {
		if (fullname != null && fullname.length() > 0)
			return fullname;
		else
			return id;
	}
}
