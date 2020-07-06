package net.sourceforge.ondex.workflow.engine;

import org.jdom.Element;

public interface OutputConverter {
    public Element convert(Object o);
}