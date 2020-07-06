package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.sink.DbLink;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;
import net.sourceforge.ondex.parser.uniprot.sink.Publication;

/**
 * 
 * @author peschr
 *
 */
public class PublicationBlockParser extends AbstractBlockParser {
	
	private static final String TYPE = "type";
	private static final String JARTICLE = "journal article";
	private static final String NAME = "name";
	private static final String VOLUME = "volume";
	private static final String FIRST = "first";
	private static final String LAST = "last";
	private static final String DATE = "date";
	private static final String TITLE = "title";
	private static final String OJA = "online journal article";
	private static final String ID = "id";
	private static final String REFERENCE = "reference";

	public void parseElement(XMLStreamReader staxXmlReader) throws XMLStreamException {
		boolean inReference = true;
		Publication publication = null;
		staxXmlReader.nextTag();
		String cit_type = null;
		for (int i=0;i<staxXmlReader.getAttributeCount();i++) {
			String name = staxXmlReader.getAttributeLocalName(i);
			if (name.equalsIgnoreCase(TYPE)) {
				cit_type = staxXmlReader.getAttributeValue(i);
				break;
			} 
		}
		
		if ( cit_type.equalsIgnoreCase(JARTICLE) ){
			
			String volume = null;
			String journal = null;
			String first = null;
			String last = null;
			String year = null;
			
			for (int i=0;i<staxXmlReader.getAttributeCount();i++) {
				String name = staxXmlReader.getAttributeLocalName(i);
				if (name.equalsIgnoreCase(NAME)) {
					journal = staxXmlReader.getAttributeValue(i);
				} else if (name.equalsIgnoreCase(VOLUME)) {
					volume = staxXmlReader.getAttributeValue(i);
				} else if (name.equalsIgnoreCase(FIRST)) {
					first = staxXmlReader.getAttributeValue(i);
				} else if (name.equalsIgnoreCase(LAST)) {
					last = staxXmlReader.getAttributeValue(i);
				} else if (name.equalsIgnoreCase(DATE)) {
					year = staxXmlReader.getAttributeValue(i);
				}
			}
			
			publication = new Publication();
			if (volume != null)
				publication.setVolume(volume);
			if (journal != null)
				publication.setJournalName(journal);
			if (last != null)
				publication.setLastPage(last);
			if (first != null)
				publication.setFirstPage(first);
			if (year != null)
				publication.setYear(Integer.parseInt(year));
				

			staxXmlReader.nextTag();
			if ( staxXmlReader.getLocalName().equalsIgnoreCase(TITLE)){
				publication.setTitle(staxXmlReader.getElementText());
			}	
			Protein.getInstance().addPublication(publication);
		} else if(cit_type.equalsIgnoreCase(OJA)){
			publication = new Publication();
			staxXmlReader.nextTag();
			if ( staxXmlReader.getLocalName().equalsIgnoreCase(TITLE))
				publication.setTitle(staxXmlReader.getElementText());
		}
		
		//parse dbReferences (PubMed, DOI) for publications
		while(staxXmlReader.hasNext() && inReference){
			int event = staxXmlReader.next();
			
			if (event == XMLStreamConstants.START_ELEMENT && 
					staxXmlReader.getLocalName().equals("dbReference")){
				DbLink dbLink = new DbLink();
				
						String type = null;
				String id = null;
				for (int i=0;i<staxXmlReader.getAttributeCount();i++) {
					String name = staxXmlReader.getAttributeLocalName(i);
					if (name.equalsIgnoreCase(TYPE)) {
						type = staxXmlReader.getAttributeValue(i);
					} else if (name.equalsIgnoreCase(ID)) {
						id = staxXmlReader.getAttributeValue(i);
					} 
				}
				dbLink.setDbName(type);
				dbLink.setAccession(id);
				
				if ( publication != null ){
					publication.addReference(dbLink);
				}else{
					Protein.getInstance().addDbReference(dbLink);
				}
			}
			// parse RP line: http://www.uniprot.org/docs/userman.htm#RP_line
			if (event == XMLStreamConstants.START_ELEMENT && 
					staxXmlReader.getLocalName().equals("scope")){
				
				if(publication != null){
					String pubType = staxXmlReader.getElementText();
					publication.addScope(pubType);
					if(pubType.toUpperCase().contains("LARGE SCALE")){
						//this is a large scale / review publication
						publication.setLargeScalePaper(true);
					}
				}	
					
			}
			
			// quit the reference block
			if(event == XMLStreamConstants.END_ELEMENT && staxXmlReader.getLocalName().equals(REFERENCE)){
				inReference = false;
			}
		}
	}
}
