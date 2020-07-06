package net.sourceforge.ondex.workflow2.gui.arg;

import net.sourceforge.ondex.workflow.model.BoundArgumentValue;

/**
 * @author lysenkoa
 */
public interface ArgumentContainer extends ArgumentHolder {
    public BoundArgumentValue getContent();
}
