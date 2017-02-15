package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * An implementation of RelationType based upon AbstractMetaData.
 * 
 * @author Matthew Pocock
 */
public class RelationTypeImpl extends AbstractMetaData implements RelationType {

	/**
	 * The inverse name for this relation type.
	 */
	protected String inverseName;

	/**
	 * The antisymmetric property for this relation type.
	 */
	protected boolean isAntisymmetric;

	/**
	 * The reflexive property for this relation type.
	 */
	protected boolean isReflexive;

	/**
	 * The symmetric property for this relation type.
	 */
	protected boolean isSymmetric;

	/**
	 * The transitive property for this relation type.
	 */
	protected boolean isTransitiv;

	/**
	 * A possible parent relation type for this relation type.
	 */
	protected RelationType specialisationOf;

	/**
	 * Constructor which fills all fields of RelationType
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this RelationType
	 * @param fullname
	 *            fullname of this RelationType
	 * @param description
	 *            description of this RelationType
	 * @param inverseName
	 *            inverse name of this RelationType
	 * @param isAntisymmetric
	 *            antisymetric property of this RelationType
	 * @param isReflexive
	 *            reflexive property of this RelationType
	 * @param isSymmetric
	 *            symmetric property of this RelationType
	 * @param isTransitiv
	 *            transitiv property of this RelationType
	 * @param specialisationOf
	 *            is specialisation of this RelationType
	 */
	protected RelationTypeImpl(long sid, String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) {
		super(sid, id, fullname, description);
		this.inverseName = inverseName.intern();
		this.isAntisymmetric = isAntisymmetric;
		this.isReflexive = isReflexive;
		this.isSymmetric = isSymmetric;
		this.isTransitiv = isTransitiv;
		this.specialisationOf = specialisationOf;
	}

	@Override
	public String getInverseName() {
		return inverseName;
	}

	@Override
	public void setInverseName(String inverseName) throws NullValueException,
			UnsupportedOperationException {

		// null values not allowed
		if (inverseName == null)
			throw new NullValueException(
					Config.properties
							.getProperty("RelationType.AbstractRelationIDNull"));

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.inverseName = inverseName;
	}

	@Override
	public boolean isAntisymmetric() {
		return isAntisymmetric;
	}

	@Override
	public void setAntisymmetric(boolean isAntisymmetric)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.isAntisymmetric = isAntisymmetric;
	}

	@Override
	public boolean isReflexive() {
		return isReflexive;
	}

	@Override
	public void setReflexive(boolean isReflexive)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.isReflexive = isReflexive;
	}

	@Override
	public boolean isSymmetric() {
		return isSymmetric;
	}

	@Override
	public void setSymmetric(boolean isSymmetric)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.isSymmetric = isSymmetric;
	}

	@Override
	public boolean isTransitiv() {
		return isTransitiv;
	}

	@Override
	public void setTransitiv(boolean isTransitiv)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.isTransitiv = isTransitiv;
	}

	@Override
	public RelationType getSpecialisationOf() {
		return specialisationOf;
	}

	@Override
	public void setSpecialisationOf(RelationType specialisationOf)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.specialisationOf = specialisationOf;
	}

	/**
	 * Returns whether this RelationType is a transitive superclass of
	 * <code>rt</code>.
	 */
	@Override
	public boolean isAssignableFrom(RelationType rt) {
		RelationType curr = rt;
		while (!curr.equals(this)) {
			curr = curr.getSpecialisationOf();
			if (curr == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether this RelationType is a transitive subclass of
	 * <code>rt</code>.
	 */
	@Override
	public boolean isAssignableTo(RelationType rt) {
		return rt.isAssignableFrom(this);
	}
}
