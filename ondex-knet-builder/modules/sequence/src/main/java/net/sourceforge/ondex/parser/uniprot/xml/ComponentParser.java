package net.sourceforge.ondex.parser.uniprot.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;

public interface ComponentParser {

    public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException, InvalidPluginArgumentException;

}
