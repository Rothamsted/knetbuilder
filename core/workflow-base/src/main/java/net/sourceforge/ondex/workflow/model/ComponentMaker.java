package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.workflow.engine.Processor;
import org.jdom.Element;

/**
 * @author lysenkoa
 */
public interface ComponentMaker {
    public Processor makeComponent(Element e);
}