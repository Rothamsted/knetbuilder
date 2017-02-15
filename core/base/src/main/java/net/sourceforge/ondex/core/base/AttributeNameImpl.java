package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Hierarchy;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.exception.type.RetrievalException;

/**
 * Implementation of AttributeName.
 * 
 * @author Matthew Pocock
 */
public class AttributeNameImpl extends AbstractMetaData implements
		AttributeName {

	/**
	 * unit for this attribute name
	 */
	protected Unit unit;

	/**
	 * data type for this attribute name
	 */
	protected String datatypeName;

	/**
	 * parent attribute name for this attribute name
	 */
	protected AttributeName specialisationOf;

	/**
	 * Constructor which fills all fields of AttributeName
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this AttributeName
	 * @param fullname
	 *            fullname of this AttributeName
	 * @param description
	 *            description of this AttributeName
	 * @param unit
	 *            unit for this AttributeName
	 * @param datatypeName
	 *            Class name for data type for this AttributeName
	 * @param specialisationOf
	 *            parent AttributeName of this AttributeName
	 */
	protected AttributeNameImpl(long sid, String id, String fullname,
			String description, Unit unit, String datatypeName,
			AttributeName specialisationOf) {
		super(sid, id, fullname, description);
		this.unit = unit;
		this.datatypeName = datatypeName;
		this.specialisationOf = specialisationOf;
	}

	/**
	 * Returns the unit of this instance of AttributeName.
	 * 
	 * @return Unit
	 */
	@Override
	public Unit getUnit() {
		return unit;
	}

	/**
	 * Sets the unit of this instance of AttributeName.
	 * 
	 * @param unit
	 *            new Unit for this AttributeName
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	@Override
	public void setUnit(Unit unit) throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.unit = unit;
	}

	/**
	 * Returns the data type of this instance of AttributeName.
	 * 
	 * @return Class<?>
	 */
	@Override
	public Class<?> getDataType() {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl == null)
				cl = ClassLoader.getSystemClassLoader();
			return cl.loadClass(datatypeName);
		} catch (ClassNotFoundException cnfe) {
			throw new RetrievalException(cnfe.getMessage());
		}
	}

	/**
	 * Returns the data type of this instance of AttributeName as String.
	 * 
	 * @return String
	 */
	@Override
	public String getDataTypeAsString() {
		return datatypeName;
	}

	/**
	 * Returns to parent attribute name of this instance of AttributeName.
	 * 
	 * @return AttributeName
	 */
	@Override
	public AttributeName getSpecialisationOf() {
		return specialisationOf;
	}

	/**
	 * Sets the parent attribute name of this instance of AttributeName.
	 * 
	 * @param specialisationOf
	 *            new parent AttributeName
	 */
	@Override
	public void setSpecialisationOf(AttributeName specialisationOf)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.specialisationOf = specialisationOf;
	}

	@Override
	public boolean isAssignableTo(AttributeName possibleAncestor) {
		return Hierarchy.Helper.transitiveParent(possibleAncestor, this);
	}

	@Override
	public boolean isAssignableFrom(AttributeName possibleDescendant) {
		return Hierarchy.Helper.transitiveParent(this, possibleDescendant);
	}
}
