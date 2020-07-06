package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * 
 * @author hindlem
 * 
 */
public class ProteinNameBlockParser extends AbstractBlockParser {

	private static final String RECNAME = "recommendedName";
	private static final String ALTNAME = "alternativeName";
	private static final String SUBNAME = "submittedName";
	private static final String FULLNAME = "fullname";
	private static final String SHORTNAME = "shortname";

	public void parseElement(XMLStreamReader staxXmlReader)
			throws XMLStreamException {
		boolean isRecName = false;
		boolean isAltName = false;
		boolean isSubName = false;
		boolean foundPref = false;
		while (staxXmlReader.hasNext()) {
			int next = staxXmlReader.next();

			// START
			if (next == XMLStreamConstants.START_ELEMENT) {
				if (staxXmlReader.getLocalName().equalsIgnoreCase(RECNAME)) {
					isRecName = true;
				}
				if (staxXmlReader.getLocalName().equalsIgnoreCase(ALTNAME)) {
					isAltName = true;
				}
				if (staxXmlReader.getLocalName().equalsIgnoreCase(SUBNAME)) {
					isSubName = true;
				}

				// recommended full name
				if (isRecName
						&& staxXmlReader.getLocalName().equalsIgnoreCase(
								FULLNAME)) {
					// the long recommended name is first choice
					String value = staxXmlReader.getElementText();
					Protein.getInstance().addPreferedName(value);
					foundPref = true;
				} 
				
				// recommended short name
				else if (isRecName
						&& staxXmlReader.getLocalName().equalsIgnoreCase(
								SHORTNAME)) {
					String value = staxXmlReader.getElementText();
					if (!foundPref) {
						// if no long recommended name exists, take short one
						Protein.getInstance().addPreferedName(value);
						foundPref = true;
					} else
						Protein.getInstance().addName(value);
				} 
				
				// alternative full name
				else if (isAltName
						&& staxXmlReader.getLocalName().equalsIgnoreCase(
								FULLNAME)) {
					// if no recommended name exists, use long alternative name
					String value = staxXmlReader.getElementText();
					if (!foundPref) {
						Protein.getInstance().addPreferedName(value);
						foundPref = true;
					} else
						Protein.getInstance().addName(value);
				} 
				
				// alternative short name
				else if (isAltName
						&& staxXmlReader.getLocalName().equalsIgnoreCase(
								SHORTNAME)) {
					// if no long alternative name exists, take short one
					String value = staxXmlReader.getElementText();
					if (!foundPref) {
						Protein.getInstance().addPreferedName(value);
						foundPref = true;
					} else
						Protein.getInstance().addName(value);
				} 
				
				// submitted name short or long
				else if (isSubName
						&& (staxXmlReader.getLocalName().equalsIgnoreCase(
								FULLNAME) || staxXmlReader.getLocalName()
								.equalsIgnoreCase(SHORTNAME))) {
					// otherwise just normal names
					String value = staxXmlReader.getElementText();
					// if preferred name found, then just normal name
					if (foundPref)
						Protein.getInstance().addName(value);
					else {
						// exception as no preferred name existed
						Protein.getInstance().addPreferedName(value);
						foundPref = true;
					}
				}
			}
			// END
			else if (next == XMLStreamConstants.END_ELEMENT) {

				if (staxXmlReader.getLocalName().equalsIgnoreCase(RECNAME)) {
					isRecName = false;
				}
				if (staxXmlReader.getLocalName().equalsIgnoreCase(ALTNAME)) {
					isAltName = false;
				}
				if (staxXmlReader.getLocalName().equalsIgnoreCase(SUBNAME)) {
					isSubName = false;
				}
				if (staxXmlReader.getLocalName().equals("protein")) {
					return;
				}
			}
		}
	}

}
