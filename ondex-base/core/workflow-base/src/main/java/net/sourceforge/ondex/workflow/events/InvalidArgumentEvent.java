package net.sourceforge.ondex.workflow.events;

import net.sourceforge.ondex.event.type.EventType;

public class InvalidArgumentEvent extends EventType{

	public InvalidArgumentEvent(String message) {
		super(message, "");
		super.desc = "Plugin Value is invalid";
	}

}
