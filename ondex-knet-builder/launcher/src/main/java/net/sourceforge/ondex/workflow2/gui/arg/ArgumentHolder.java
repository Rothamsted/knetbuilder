package net.sourceforge.ondex.workflow2.gui.arg;

import net.sourceforge.ondex.init.ArgumentDescription;

/**
 * @author lysenkoa
 */
public interface ArgumentHolder {
    public ArgumentDescription getArgumentTemplate();
    public void sendFocus();
    public void setValue(String value);
}
