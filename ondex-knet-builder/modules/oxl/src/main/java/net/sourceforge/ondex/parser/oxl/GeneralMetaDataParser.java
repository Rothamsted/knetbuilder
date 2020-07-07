package net.sourceforge.ondex.parser.oxl;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * This class parses XML ONDEX general meta data and stores them the ONDEX graph
 * meta data.
 * 
 * @author sierenk
 */
public class GeneralMetaDataParser extends AbstractEntityParser {

	// type of meta elements to parse
	private String type;

	/**
	 * Creates a parser for Concept elements.
	 * 
	 * @param og
	 *            ONDEXGraph for storing parsed general meta data
	 * @param type
	 *            name of meta element
	 */
	public GeneralMetaDataParser(ONDEXGraph og, String type)
			throws JAXBException {
		super(og);
		this.type = type;
	}

	/**
	 * Parses ONDEX general meta data.
	 * 
	 * @param xmlr
	 *            XMLStreamReader
	 * @throws ClassNotFoundException
	 * @throws UnsupportedOperationException
	 * @throws EmptyStringException
	 * @throws NullValueException
	 */
	public void parse(XMLStreamReader xmlr) throws XMLStreamException,
			JAXBException, NullValueException, EmptyStringException,
			UnsupportedOperationException, ClassNotFoundException {
		if (type.equals(XMLTagNames.UNIT)) {

			parseUnit(xmlr);

		} else if (type.equals(XMLTagNames.ATTRIBUTENAME)) {

			// parse AttributeName and save it in ondex graph metadata
			parseAttributeName(xmlr);

		} else if (type.equals(XMLTagNames.EVIDENCES)) {

			// parse all evidencetypes and save them in ondex graph metadata
			parseEvidences(xmlr);

		}
	}

	/**
	 * Returns the name of this XMLComponentParser.
	 * 
	 * @return String
	 */
	public String getName() {
		return "GeneralMetaDataParser";
	}
}
