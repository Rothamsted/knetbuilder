package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;

/**
 * 
 * @author peschr
 *
 */
public class TaxonomieBlockParser extends AbstractBlockParser {
	
	private static final String DBREF = "dbReference";
	private static final String ID = "id";

	public TaxonomieBlockParser(ValueFilter filter){
		this.filter = filter;
	}

	public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException {
		
		while ( staxXmlReader.next() != XMLStreamConstants.START_ELEMENT ||
				!staxXmlReader.getName().getLocalPart().equalsIgnoreCase(DBREF) );
		
		
		String taxId = null;
		for (int i=0;i<staxXmlReader.getAttributeCount();i++) {
			String name = staxXmlReader.getAttributeLocalName(i);
			if (name.equalsIgnoreCase(ID)) {
				taxId = staxXmlReader.getAttributeValue(i);
				break;
			}
		}
		if ( filter != null) {
			filter.check(taxId);
		}
		Protein.getInstance().setTaxId(taxId);
	}
}
