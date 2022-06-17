package net.sourceforge.ondex.workflow.model;

import org.jdom2.Element;

import net.sourceforge.ondex.workflow.engine.Processor;

/**
 * @author lysenkoa
 */
public interface ComponentMaker {
    public Processor makeComponent(Element e);
}