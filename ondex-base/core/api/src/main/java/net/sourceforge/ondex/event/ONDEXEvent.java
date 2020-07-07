package net.sourceforge.ondex.event;

import java.util.EventObject;

import net.sourceforge.ondex.event.type.EventType;

/**
 * Implements an ONDEX event.
 * 
 * @author taubertj
 * 
 */
public class ONDEXEvent extends EventObject {

	// stores associated event type
	protected EventType eventType;

	/**
	 * Default serialisation id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a given Object and corresponding event type.
	 * 
	 * @param o
	 *            Object
	 * @param e
	 *            EventType
	 */
	public ONDEXEvent(Object o, EventType e) {
		super(o);
		this.eventType = e;
	}

	/**
	 * Returns the event type associated with this ONDEX event.
	 * 
	 * @return EventType
	 */
	public EventType getEventType() {
		return this.eventType;
	}

}
