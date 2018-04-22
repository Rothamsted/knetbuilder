package net.sourceforge.ondex.logging;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.EventType;

/**
 * 
 * Class for logging core events.
 * 
 * @author taubertj
 * 
 */
public class ONDEXLogger implements ONDEXListener {

	// #####FIELDS#####
	/**
	 * List for EventListeners.
	 */
	private EventListenerList eventListenerList = new EventListenerList();
	
	/**
	 * Captures all events and writes them to Log4j.
	 * 
	 * @param e
	 *            ONDEXEvent
	 */
	@Override
	public void eventOccurred(ONDEXEvent e) {

		Logger logger = Logger.getLogger(e.getSource().getClass());

		EventType et = e.getEventType ();
		
		StringBuilder msg = new StringBuilder ( et.getDescription () );
		if ( msg.length () > 0 ) msg.append ( ' ' );
		msg.append ( et.getCompleteMessage() );
		

		logger.log ( et.getLog4jLevel(), msg.toString () );
	}

	/**
	 * Adds a ONDEX graph listener to the list.
	 * 
	 * @param l
	 *            ONDEXONDEXListener to add
	 */
	public void addONDEXONDEXListener(ONDEXListener l) {
		eventListenerList.add(ONDEXListener.class, l);
	}

	/**
	 * Removes a ONDEX graph listener listener from the list.
	 * 
	 * @param l
	 *            ONDEXONDEXListener
	 */
	public void removeONDEXONDEXListener(ONDEXListener l) {
		eventListenerList.remove(ONDEXListener.class, l);
	}

	/**
	 * Returns the list of ONDEX graph listener listeners.
	 * 
	 * @return list of ONDEXONDEXListeners
	 */
	public ONDEXListener[] getONDEXONDEXListeners() {
		return eventListenerList.getListeners(ONDEXListener.class);
	}

}
