package net.sourceforge.ondex.workflow2.gui.components.highlighting;

import java.util.EventObject;

public class HighlightEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private final boolean currentlyHighlighted;

	public HighlightEvent(Object source, boolean isCurrentlyHighlighted) {
		super(source);
		this.currentlyHighlighted = isCurrentlyHighlighted;
	}

	public boolean isCurrentlyHighlighted() {
		return currentlyHighlighted;
	}
}
