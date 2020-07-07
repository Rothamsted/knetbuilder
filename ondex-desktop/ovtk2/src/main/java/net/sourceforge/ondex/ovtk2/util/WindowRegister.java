package net.sourceforge.ondex.ovtk2.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton to register all window (JInternalFrame) and open and close event
 * changes
 * 
 * @author hindlem
 */
public class WindowRegister {

	// eagerly created singleton
	private static WindowRegister register = new WindowRegister();

	private WindowRegister() {

	}

	/**
	 * @return register of all window (JInternalFrame) and event changes within
	 *         the internal desktop
	 */
	public static WindowRegister getInstance() {
		return register;
	}

	private List<RegisteredFrame> internalFrames = Collections.synchronizedList(new ArrayList<RegisteredFrame>());

	/**
	 * @return returns all internal frames registered
	 */
	public List<RegisteredFrame> getInternalFrames() {
		return internalFrames;
	}

	/**
	 * Adds the frame to the register
	 * 
	 * @param frame
	 *            the frame to register
	 */
	public void registerInternalFrame(RegisteredFrame frame) {
		internalFrames.add(frame);
		synchronized (listeners) {
			for (WindowRegisteryChangeListener listener : listeners) {
				listener.registeryChangedEvent();
			}
		}
	}

	/**
	 * @param frame
	 *            the frame to deregister
	 * @return if the frame was successfuly deregistered
	 */
	public boolean deregisterInternalFrame(RegisteredFrame frame) {
		boolean ok = internalFrames.remove(frame);

		if (ok)
			synchronized (listeners) {
				for (WindowRegisteryChangeListener listener : listeners)
					listener.registeryChangedEvent();
			}
		return ok;
	}

	/**
	 * Trigger an update event to construct new windows menu
	 * 
	 */
	public void update() {
		synchronized (listeners) {
			for (WindowRegisteryChangeListener listener : listeners)
				listener.registeryChangedEvent();
		}
	}

	private Set<WindowRegisteryChangeListener> listeners = new HashSet<WindowRegisteryChangeListener>();

	/**
	 * @param listener
	 *            the listener to signal event changes to
	 */
	public void registerListener(WindowRegisteryChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * @param listener
	 *            the listener to stop signaling event changes to
	 */
	public void removeListener(WindowRegisteryChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

}
