package net.sourceforge.ondex.core.util;

import java.util.EventListener;

import net.sourceforge.ondex.core.ONDEXAssociable;

/**
 * Listener to catch update events in the core data structure.
 * 
 * @author taubertj
 * 
 */
public interface UpdateListener extends EventListener {

	/**
	 * Method is called, whenever persistent data has to be updated in the
	 * persistent layer to represent current in-memory representation.
	 * 
	 * @param o
	 *            Object in need of updating
	 */
	public abstract void performUpdate(ONDEXAssociable o);

}
