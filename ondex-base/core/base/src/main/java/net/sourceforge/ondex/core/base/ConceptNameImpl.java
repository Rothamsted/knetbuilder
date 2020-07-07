package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.config.ONDEXGraphRegistry;
import net.sourceforge.ondex.core.ConceptName;

/**
 * Simple implementation of ConceptName.
 * 
 * @author Matthew Pocock
 */
public class ConceptNameImpl extends AbstractONDEXEntity implements ConceptName {

	/**
	 * owning concept's id.
	 */
	protected final int conceptId;

	/**
	 * the concept name
	 */
	protected String name;

	/**
	 * is the concept name preferred?
	 */
	protected boolean isPreferred;

	/**
	 * Constructor for internal use only.
	 * 
	 * @param sid
	 *            unique id.
	 * @param cid
	 *            owning concept's id.
	 * @param name
	 *            Concept Name
	 * @param isPreferred
	 *            Is the concept name preferred?
	 */
	protected ConceptNameImpl(long sid, int cid, String name,
			boolean isPreferred) {
		this.sid = sid;
		this.conceptId = cid;
		this.name = name;
		this.isPreferred = isPreferred;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof ConceptName) {
			ConceptName cn = (ConceptName) o;
			return this.name.equals(cn.getName());
		}
		return false;
	}

	/**
	 * Returns the stored Concept Name.
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getOwnerId() {
		return conceptId;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	/**
	 * Returns if the ConceptName is preferred.
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isPreferred() {
		return isPreferred;
	}

	/**
	 * Sets the ConceptName to preferred.
	 * 
	 * @param isPreferred
	 *            new is preferred value
	 * @throws UnsupportedOperationException
	 *             for read-only graphs
	 */
	@Override
	public void setPreferred(boolean isPreferred)
			throws UnsupportedOperationException {

		// check for read-only graphs
		if (ONDEXGraphRegistry.graphs.get(getSID()).isReadOnly())
			throw new UnsupportedOperationException();

		this.isPreferred = isPreferred;
	}

}
