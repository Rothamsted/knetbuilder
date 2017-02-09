package net.sourceforge.ondex.ovtk2.util;

import java.util.EventListener;

/**
 * Interface to register window registery changes
 * 
 * @author hindlem
 * 
 */
public interface WindowRegisteryChangeListener extends EventListener {

	/**
	 * Signals the Windows registered frames has changed
	 */
	public void registeryChangedEvent();

}
