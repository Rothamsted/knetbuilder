package net.sourceforge.ondex.workflow2.gui.arg;

import java.util.List;

import net.sourceforge.ondex.workflow.model.BoundArgumentValue;

/**
 * @author lysenkoa
 */
public interface ArgumentCollectionContainer extends ArgumentHolder {
    public List<BoundArgumentValue> getContentList();

    public String[] getValuesAsArray();

    public void setValuesAsArray(String[] values);
}
