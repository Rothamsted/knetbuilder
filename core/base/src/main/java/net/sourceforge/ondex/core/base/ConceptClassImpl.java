package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.ConceptClass;

/**
 * Simple implementation of ConceptClass.
 * 
 * @author Matthew Pocock
 */
public class ConceptClassImpl extends AbstractMetaData implements ConceptClass {

	/**
	 * parent ConceptClass of this ConceptClass
	 */
	protected ConceptClass specialisationOf;

	/**
	 * Constructor which fills all fields of ConceptClass.
	 * 
	 * @param sid
	 *            unique id
	 * @param data
	 *            ONDEXGraphMetaData
	 * @param id
	 *            id of this ConceptClass
	 * @param fullname
	 *            fullname of this ConceptClass
	 * @param description
	 *            description of this ConceptClass
	 * @param specialisationOf
	 *            parent ConceptClass of this ConceptClass
	 */
	protected ConceptClassImpl(long sid, String id, String fullname,
			String description, ConceptClass specialisationOf) {
		super(sid, id, fullname, description);
		this.specialisationOf = specialisationOf;
	}

	/**
	 * Returns the parent ConceptClass of this instance of ConceptClass.
	 * 
	 * @return ConceptClass
	 */
	@Override
	public ConceptClass getSpecialisationOf() {
		return specialisationOf;
	}

	/**
	 * Returns whether this concept class is a transitive superclass of
	 * <code>cc</code>.
	 */
	@Override
	public boolean isAssignableFrom(ConceptClass cc) {
		ConceptClass curr = cc;
		while (!curr.equals(this)) {
			curr = curr.getSpecialisationOf();
			if (curr == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether this concept class is a transitive subclass of
	 * <code>cc</code>.
	 */
	@Override
	public boolean isAssignableTo(ConceptClass cc) {
		return cc.isAssignableFrom(this);
	}

	/**
	 * Sets the parent ConceptClass of this instance of ConceptClass.
	 * 
	 * @param specialisationOf
	 *            new parent ConceptClass
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	@Override
	public void setSpecialisationOf(ConceptClass specialisationOf)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.specialisationOf = specialisationOf;
	}

}
