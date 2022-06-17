package net.sourceforge.ondex.workflow.engine;

import org.jdom2.Element;

public interface OutputConverter {
    public Element convert(Object o);
}