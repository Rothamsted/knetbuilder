package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * Parses UniProt comment elements, so far looks only for disruption phenotype
 * 
 * @author keywan
 *
 */
public class CommentBlockParser extends AbstractBlockParser {

	@Override
	public void parseElement(XMLStreamReader staxXmlReader)
			throws XMLStreamException, InvalidPluginArgumentException {
		// TODO Auto-generated method stub
		String type = staxXmlReader.getAttributeValue(0);
		if(type.equals("disruption phenotype")){
			int event = staxXmlReader.nextTag();
			if (event == XMLStreamConstants.START_ELEMENT && 
					staxXmlReader.getLocalName().equals("text")){
			
				String phenotype = staxXmlReader.getElementText();
				Protein.getInstance().setDisruptionPhenotype(phenotype);
			
			}
            
		}

	}

}
