package net.sourceforge.ondex.workflow2.gui.arg;

import net.sourceforge.ondex.workflow.model.BoundArgumentValue;

import java.util.List;

/**
 * @author lysenkoa
 */
public interface ArgumentCollectionContainer extends ArgumentHolder {
    public List<BoundArgumentValue> getContentList();

    public String[] getValuesAsArray();

    public void setValuesAsArray(String[] values);
}
