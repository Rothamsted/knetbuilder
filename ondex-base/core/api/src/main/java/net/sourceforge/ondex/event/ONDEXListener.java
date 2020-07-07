package net.sourceforge.ondex.event;

import java.util.EventListener;

/**
 * Interface defining a ONDEXListener.
 * 
 * @author taubertj
 * 
 */
public interface ONDEXListener extends EventListener {

	/**
	 * Method is called whenever a ONDEXEvent was generated.
	 * 
	 * @param e
	 *            ONDEXEvent
	 */
	public abstract void eventOccurred(ONDEXEvent e);

}