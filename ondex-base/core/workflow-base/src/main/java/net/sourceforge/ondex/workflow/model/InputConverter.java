package net.sourceforge.ondex.workflow.model;

import org.jdom2.Element;

/**
 * @author lysenkoa
 */
public interface InputConverter {
    public Object convert(Element e, String cls);
}