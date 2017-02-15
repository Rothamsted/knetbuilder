package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.Unit;

/**
 * Convenience implementation of Unit.
 */
public class UnitImpl extends AbstractMetaData implements Unit {

	/**
	 * Construtor which calls the constructor of the parent class.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this Unit
	 * @param fullname
	 *            fullname of this Unit
	 * @param description
	 *            description of this Unit
	 */
	protected UnitImpl(long sid, String id, String fullname, String description) {
		super(sid, id, fullname, description);
	}
}
