package net.sourceforge.ondex.core.base;

import java.io.Serializable;

/**
 * Common functionality for security management.
 * 
 * @author taubertj
 */
public abstract class AbstractONDEXEntity
    implements Serializable {

	protected long sid = -1;

	/**
	 * Returns the unique id associated with the parent AbstractONDEXGraph, or, for the case of 
	 * a {@link AbstractONDEXGraph graph} itself, returns a unique ID for the graph. 
	 * 
	 * @return unique id of parent AbstractONDEXGraph
	 */
	public long getSID() {
		return sid;
	}

}
