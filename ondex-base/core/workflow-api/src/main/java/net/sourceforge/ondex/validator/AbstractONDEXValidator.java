package net.sourceforge.ondex.validator;

import java.util.HashSet;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.EventType;

/**
 * Abstract implementation of an ONDEX validator, manages listener handling.
 *
 * @author taubertj
 */
public abstract class AbstractONDEXValidator {

    // validator arguments for this validator
    protected ONDEXPluginArguments va;

    // contains all registered listeners
    private HashSet<ONDEXListener> listeners = new HashSet<ONDEXListener>();

    /**
     * Returns arguments valid for this producer
     *
     * @return ArgumentDefinition<?>[]
     */
    public abstract ArgumentDefinition<?>[] getArgumentDefinitions();

    /**
     * Sets the arguments the validator should use.
     *
     * @param va ValidatorArguments
     */
    public void setArguments(ONDEXPluginArguments va)
            throws InvalidPluginArgumentException {
        this.va = va;
    }

    /**
     * Returns the actual validator arguments.
     *
     * @return ValidatorArguments
     */
    public ONDEXPluginArguments getArguments() {
        return this.va;
    }

    /**
     * This is here until somone presents a good case for a validator that needs
     * an indexed graph.
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * Use validate(Object) instead.
     */
    public void start() {
        // do nothing
    }

    /**
     * Validates a given object and returns the results.
     *
     * @param o Object to validate
     * @return results
     */
    public abstract Object validate(Object o);

    /**
     * Cleans all temporary files associated with the validator.
     */
    public abstract void cleanup();

    /**
     * Adds a validator listener to the list.
     *
     * @param l ONDEXListener
     */
    public void addONDEXListener(ONDEXListener l) {
        listeners.add(l);
    }

    /**
     * Removes a validator listener from the list.
     *
     * @param l ONDEXListener
     */
    public void removeONDEXListener(ONDEXListener l) {
        listeners.remove(l);
    }

    /**
     * Returns the list of validator listeners.
     *
     * @return ONDEXListener[]
     */
    public ONDEXListener[] getONDEXListeners() {
        return listeners.toArray(new ONDEXListener[listeners.size()]);
    }

    /**
     * Notify all listeners that have registered with this class.
     *
     * @param e name of event
     */
    protected void fireEventOccurred(EventType e) {
        if (listeners.size() > 0) {
            // new validator event
            ONDEXEvent ve = new ONDEXEvent(this, e);
            // notify all listeners
            // notify all listeners
            for (ONDEXListener listener : listeners)
                listener.eventOccurred(ve);
        }
    }

    /**
     * Returns the human readable name of the ONDEX validator.
     *
     * @return String
     */
    public abstract String getName();

    /**
     * Returns the internal string identifier of the ONDEX validator - must be
     * unique - which is also used in the name of the XML attribute in workflow
     * files.
     *
     * @return String
     */
	public abstract String getId();
}
