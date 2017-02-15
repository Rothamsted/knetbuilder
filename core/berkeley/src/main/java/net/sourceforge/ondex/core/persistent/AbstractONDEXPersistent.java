package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.EventType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Parent class for all persistent environments.
 *
 * @author taubertj
 */
public abstract class AbstractONDEXPersistent {

    /**
     * Contains all registered listeners, transient = don't store listener
     * persistent
     */
    private transient Set<ONDEXListener> listeners = new HashSet<ONDEXListener>();

    /**
     * Returns the actual instance of AbstractONDEXGraph.
     *
     * @return AbstractONDEXGraph
     */
    public abstract ONDEXGraph getAbstractONDEXGraph();

    /**
     * Commits current open databases to ensure data consistency in-memory and
     * on-disc.
     */
    public abstract void commit();

    /**
     * Closes all open persistent representations and writes all buffered data
     * to disc.
     */
    public abstract void cleanup();

    /**
     * Adds a ONDEX listener to the list.
     *
     * @param l ONDEXListener
     */
    public void addONDEXListener(ONDEXListener l) {
        listeners.add(l);
    }

    /**
     * Removes a ONDEX listener listener from the list.
     *
     * @param l ONDEXListener
     */
    public void removeONDEXListener(ONDEXListener l) {
        listeners.remove(l);
    }

    /**
     * Returns the list of ONDEX listener listeners.
     *
     * @return ONDEXListener[]
     */
    public ONDEXListener[] getONDEXListeners() {
        return listeners.toArray(new ONDEXListener[listeners.size()]);
    }

    /**
     * Notify all listeners that have registered with this class.
     *
     * @param e EventType
     */
    protected void fireEventOccurred(EventType e) {
        if (listeners.size() > 0) {
            // new ondex graph event
            ONDEXEvent oe = new ONDEXEvent(this, e);
            // notify all listeners
            Iterator<ONDEXListener> it = listeners.iterator();
			while (it.hasNext()) {
				it.next().eventOccurred(oe);
			}
		}
	}
}
