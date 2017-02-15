package net.sourceforge.ondex.event;

import net.sourceforge.ondex.event.type.EventType;

import javax.swing.event.EventListenerList;
import java.util.HashMap;

/**
 * Stores the EventHandler instances for each graph, accessible through
 * the static method getEventHandlerForSID(long sid)
 * 
 * The returned instance provides methods for adding, removing and getting
 * all listeners as well as firing events to them.
 * 
 * 
 * @author Jochen Weile, M.Sc.
 *
 */
public class ONDEXEventHandler {
	
	//####STATIC FIELDS####
	/**
	 * static map storing the event handler instances for all graphs.
	 */
	private static HashMap<Long,ONDEXEventHandler> instances = new HashMap<Long,ONDEXEventHandler>();
	
	//#####FIELDS#####
	/**
	 * List for EventListeners.
	 */
	private EventListenerList eventListenerList = new EventListenerList();
	

	//####CONSTRUCTOR####
	
	/**
	 * private constructor. only to be called by getEventHandlerForSID()
	 */
	protected ONDEXEventHandler() {
		
	}
	
	
	//#####METHODS####
	/**
	 * Notify all listeners that have registered with this class.
	 * 
	 * @param e  the EventType to fire
	 */
	public void fireEventOccurred(EventType e) {
		// new ondex graph event
		ONDEXEvent oe = new ONDEXEvent(this, e);
		// notify all listeners
		for (ONDEXListener l : eventListenerList.getListeners(ONDEXListener.class)) {
			l.eventOccurred(oe);
		}
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
	
	//####STATIC METHODS####
	/**
	 * @return the event handler instance for all graph with the given SID.
	 */
	public static ONDEXEventHandler getEventHandlerForSID(long sid) {
		ONDEXEventHandler h = instances.get(sid);
		if (h == null) {
			h = new ONDEXEventHandler();
			instances.put(sid,h);
		}
		return h;
	}
}
