package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * 
 * @author peschr
 *
 */
public class SequenceBlockParser extends AbstractBlockParser {

	public void parseElement(XMLStreamReader staxXmlReader)
			throws XMLStreamException {
		Protein.getInstance().setSequence(staxXmlReader.getElementText());
	}
}
