package net.sourceforge.ondex.core.base;

import java.io.Serializable;

/**
 * Common functionality for security management.
 * 
 * @author taubertj
 */
public abstract class AbstractONDEXEntity
    implements Serializable {

	/**
	 * A unique session id for each graph for security. Currently derived from
	 * nano system time at construction of AbstractONDEXGraph.
	 */
	protected long sid = -1;

	/**
	 * Returns the unique id associated with the parent AbstractONDEXGraph.
	 * 
	 * @return unique id of parent AbstractONDEXGraph
	 */
	public long getSID() {
		return sid;
	}

}
