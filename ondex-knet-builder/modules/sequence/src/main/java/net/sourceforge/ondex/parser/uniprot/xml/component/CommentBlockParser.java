package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * Parses UniProt comment elements, so far looks only for disruption phenotype
 *
 *
 * @author keywan
 *
 * Now also parses OMIM fields for Name & Description
 *
 * @author Joseph
 *
 */
public class CommentBlockParser extends AbstractBlockParser {

    @Override
    public void parseElement(XMLStreamReader staxXmlReader)
            throws XMLStreamException, InvalidPluginArgumentException {

        String type = staxXmlReader.getAttributeValue(0);
        if (type.equals("disruption phenotype")) {
            int event = staxXmlReader.nextTag();
            if (event == XMLStreamConstants.START_ELEMENT
                    && staxXmlReader.getLocalName().equals("text")) {

                String phenotype = staxXmlReader.getElementText();
                Protein.getInstance().setDisruptionPhenotype(phenotype);
            }
        }
        if (type.equals("disease")) {

            /**Ensure that the first local tag is disease and not 'text' so we skip these entries 
             Skip the first two tags to get the Name **/
            
            int eventOne = staxXmlReader.nextTag();
            if (eventOne == XMLStreamConstants.START_ELEMENT
                    && staxXmlReader.getLocalName().equals("disease") && !"text".equals(staxXmlReader.getLocalName()) ) {
                int eventTwo = staxXmlReader.nextTag();

                // Add name description to Name attribute
                if (eventTwo == XMLStreamConstants.START_ELEMENT
                        && staxXmlReader.getLocalName().equals("name") && staxXmlReader.isStartElement() == true) {
                    String name = staxXmlReader.getElementText();
                    Protein.getInstance().addOmimName(name);
                    //System.out.println("Name is :" + name);
                    staxXmlReader.nextTag(); // Skip to the description
                    staxXmlReader.getElementText(); // read the element text to continue parsing

                }
                //System.out.println("localName:  "  + staxXmlReader.getLocalName());
                int eventThree = staxXmlReader.nextTag(); // Description tag
                // Add description to Description setter
                if (eventThree == XMLStreamConstants.START_ELEMENT
                        && staxXmlReader.getLocalName() == "description" && staxXmlReader.isStartElement() == true) {
                    String description = staxXmlReader.getElementText();
                    Protein.getInstance().addDescription(description);
                }

                int eventFour = staxXmlReader.nextTag();
                if (eventFour == XMLStreamConstants.START_ELEMENT
                        && staxXmlReader.getLocalName() == "dbReference") {
                    //System.out.println("DBref: " + staxXmlReader.getLocalName());
                    String Id = staxXmlReader.getAttributeValue(1);
                    Protein.getInstance().addID(Id);
                }

            }
        }

    }

}
