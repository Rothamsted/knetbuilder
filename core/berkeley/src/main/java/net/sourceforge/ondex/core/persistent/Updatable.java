package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.util.UpdateListener;

public interface Updatable {
	void setUpdateListener(UpdateListener l);
	void fireUpdateEvent();
}
