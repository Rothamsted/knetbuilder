package net.sourceforge.ondex.workflow.model;

import org.jdom.Element;

/**
 * @author lysenkoa
 */
public interface InputConverter {
    public Object convert(Element e, String cls);
}