package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * This class represents a generalised data structure for Concepts. It requires
 * a attribute name and attribute value.
 * 
 * @author sierenk, taubertj
 * @author Matthew Pocock
 */
public class ConceptAttribute extends AbstractAttribute {

	/**
	 * Constructor for internal use only.
	 * 
	 * @param sid
	 *            unique id
	 * @param conceptId
	 *            id of owning concept
	 * @param attributeName
	 *            AttributeName
	 * @param value
	 *            Object for the Attribute
	 * @param doIndex
	 *            index this Attribute
	 */
	protected ConceptAttribute(long sid, int conceptId,
			AttributeName attributeName, Object value, boolean doIndex) {
		super(sid, conceptId, attributeName, value, doIndex);
	}

	@Override
	public Class<? extends ONDEXEntity> getOwnerClass() {
		return AbstractConcept.class;
	}
}