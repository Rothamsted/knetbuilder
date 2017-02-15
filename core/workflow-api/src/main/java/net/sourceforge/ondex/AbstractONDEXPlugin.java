package net.sourceforge.ondex;

import java.util.HashSet;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.event.type.EventType;

/**
 * Parent class for export functionality implements ONDEXPlugin.
 *
 * @author hindelm
 */
public abstract class AbstractONDEXPlugin implements ONDEXPlugin, RequiresGraph
{

    // arguments for this exporter
    protected ONDEXPluginArguments args;

    // contains all registered listeners
    private HashSet<ONDEXListener> listeners = new HashSet<ONDEXListener>();

    // current ONDEX graph
    protected ONDEXGraph graph;

    /**
     * Sets the arguments the plug-in should use.
     *
     * @param args ExportArguments
     */
    @Override
    public void setArguments(ONDEXPluginArguments args) {
        this.args = args;
    }

    /**
     * Returns the actual arguments.
     *
     * @return ExportArguments
     */
    @Override
    public ONDEXPluginArguments getArguments() {
        return args;
    }

    /**
     * Adds a export listener to the list.
     *
     * @param l ONDEXListener
     */
    public void addONDEXListener(ONDEXListener l) {
        listeners.add(l);
    }

    /**
     * Removes a export listener from the list.
     *
     * @param l ONDEXListener
     */
    public void removeONDEXListener(ONDEXListener l) {
        listeners.remove(l);
    }

    /**
     * Returns the list of export listeners.
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
    public void fireEventOccurred(EventType e) {
        if (listeners.size() > 0) {
            // new export event
            ONDEXEvent ee = new ONDEXEvent(this, e);
            // notify all listeners
            for (ONDEXListener listener : listeners)
                listener.eventOccurred(ee);
        }
    }

    /**
     * Sets the current AbstractONDEXGraph.
     *
     * @param inGraph current AbstractONDEXGraph
     */
    @Override
    public void setONDEXGraph(ONDEXGraph inGraph) {
        this.graph = inGraph;
    }

    /**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}
 
}
