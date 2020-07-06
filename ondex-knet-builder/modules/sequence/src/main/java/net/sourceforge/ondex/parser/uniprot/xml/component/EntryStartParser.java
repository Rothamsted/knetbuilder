package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * Parser for entry section
 *
 * @author keywan
 */
public class EntryStartParser extends AbstractBlockParser {

	private String DATASET = "dataset";
 	private String CREATED = "created";
 	private String MODIFIED = "modified";
 	private String VERSION = "version"; 
 	
    public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException {

            String dataset = null;
            String created = null;
            String modified = null;
            String version = null;
            
            for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
                String name = staxXmlReader.getAttributeLocalName(i);
                if (name.equalsIgnoreCase(DATASET)) {
                	// "Swiss-Prot" or "TrEMBL"
                    dataset = staxXmlReader.getAttributeValue(i);
                } else if (name.equalsIgnoreCase(CREATED)) {
                    created = staxXmlReader.getAttributeValue(i);
                } else if (name.equalsIgnoreCase(MODIFIED)) {
                    modified = staxXmlReader.getAttributeValue(i);
                } else if (name.equalsIgnoreCase(VERSION)) {
                    version = staxXmlReader.getAttributeValue(i);
                }
            }
            Protein.getInstance().setDataset(dataset);
            Protein.getInstance().setEntryStats("Created: "+created+"; Modified: "+modified+"; Version: "+version);
    }
}
