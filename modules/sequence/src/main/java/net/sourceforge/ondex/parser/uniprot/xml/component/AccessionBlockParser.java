package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.MetaData;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.xml.filter.ValueFilter;

/**
 * 
 * @author peschr
 *
 */
public class AccessionBlockParser extends AbstractBlockParser {

//	private static final String NAME = "name";
	private static final String ACCESSION = "accession";

	public AccessionBlockParser(ValueFilter filter){
		this.filter = filter;
	}
	
	public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException {
		String value = staxXmlReader.getElementText();
		if (filter != null) 
			filter.check(value);
		
		// just add the first (primary) accession
		if(!Protein.getInstance().getAccessions().containsKey(MetaData.CV_UniProt)){
			Protein.getInstance().addAccession(MetaData.CV_UniProt, value);
		}
		
		// add secondary accessions
//		while ( staxXmlReader.nextTag()  == XMLStreamConstants.START_ELEMENT 
//				&& staxXmlReader.getLocalName().equalsIgnoreCase(ACCESSION)){
//			value =	staxXmlReader.getElementText();
//			
//			if (filter != null) 
//				filter.check(value);
//			Protein.getInstance().addAccession(MetaData.CV_UniProt, value);
//		}
//		if ( staxXmlReader.getLocalName().equalsIgnoreCase(NAME) ){
//			value = staxXmlReader.getElementText();
//			Protein.getInstance().addName(value);
//		}
	}

}
