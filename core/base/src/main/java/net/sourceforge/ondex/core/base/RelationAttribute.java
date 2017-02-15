package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * This class represents a generalised data structure for Relations. It requires
 * a attribute name and attribute value.
 * 
 * @author taubertj
 */
public class RelationAttribute extends AbstractAttribute {

	/**
	 * Constructor for internal use only.
	 * 
	 * @param sid
	 *            unique id
	 * @param relationID
	 *            id of owning relation ID
	 * @param attributeName
	 *            AttributeName ID
	 * @param value
	 *            Object for this Attribute
	 * @param doIndex
	 *            index this Attribute
	 */
	protected RelationAttribute(long sid, int relationID,
			AttributeName attributeName, Object value, boolean doIndex) {
		super(sid, relationID, attributeName, value, doIndex);
	}

	@Override
	public Class<? extends ONDEXEntity> getOwnerClass() {
		return AbstractRelation.class;
	}
}