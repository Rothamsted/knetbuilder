package net.sourceforge.ondex.parser.uniprot.xml;

import net.sourceforge.ondex.InvalidPluginArgumentException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface ComponentParser {

    public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException, InvalidPluginArgumentException;

}
