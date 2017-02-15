package net.sourceforge.ondex.parser.uniprot.xml.component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.parser.uniprot.MetaData;
import net.sourceforge.ondex.parser.uniprot.sink.Protein;

/**
 * 
 * @author peschr
 * 
 */
public class GeneBlockParser extends AbstractBlockParser {

	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String SYNONYM = "synonym";
	private static final String PRIMARY = "primary";
	private static final String ORD_LOC = "ordered locus";
	private static final String ORF = "ORF";
	private static final String GENE = "gene";

	public void parseElement(XMLStreamReader staxXmlReader)
			throws XMLStreamException {

		while (staxXmlReader.hasNext()) {
			int next = staxXmlReader.next();
			if (next == XMLStreamConstants.START_ELEMENT
					&& staxXmlReader.getLocalName().equalsIgnoreCase(NAME)) {
				String type = null;
				for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
					String name = staxXmlReader.getAttributeLocalName(i);
					if (name.equalsIgnoreCase(TYPE)) {
						type = staxXmlReader.getAttributeValue(i);
						break;
					}
				}
				String value = staxXmlReader.getElementText();

				if (type.equalsIgnoreCase(PRIMARY)
						|| type.equalsIgnoreCase(SYNONYM)) {
					Protein.getInstance().addName(value);
				} else if (type.equalsIgnoreCase(ORD_LOC)) {
					// TODO: this is a hack, might change in future
				/*	if (value.startsWith("Os")) {
						Protein.getInstance().addAccession(MetaData.CV_TIGR,
								value);
						Protein.getInstance().addAccession(MetaData.CV_TIGR,
								"LOC_" + value);
					} else if (value.startsWith("At")) {
						Protein.getInstance().addAccession(MetaData.CV_TAIR,
								value);
					} else if (value.startsWith("Y") && !value.contains("_")) {
						Protein.getInstance().addAccession(MetaData.CV_SGD,
								value);
					}*/
					Protein.getInstance().addName(value);
				} else if (type.equalsIgnoreCase(ORF)) {
					// ORF are somewhat equivalent to accessions but only in the
					// same genome
				//	Protein.getInstance().addAccession("LOCUSTAG", value);
				} else {
					Protein.getInstance().addName(value);
				}
			} else if (next == XMLStreamConstants.END_ELEMENT
					&& staxXmlReader.getLocalName().equalsIgnoreCase(GENE)) {
				return;
			}
		}
	}

}
