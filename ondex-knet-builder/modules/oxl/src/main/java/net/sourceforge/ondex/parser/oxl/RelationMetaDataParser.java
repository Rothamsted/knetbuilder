package net.sourceforge.ondex.parser.oxl;

import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * This class parses XML ONDEX relation meta data and stores them in a ONDEX
 * graph meta data.
 * 
 * @author sierenk
 */
public class RelationMetaDataParser extends RelationParser {

	// type of elements to parse
	private final String type;

	/**
	 * Creates a parser for relation meta data.
	 * 
	 * @param og
	 *            ONDEXGraph for storing parsed relation meta data.
	 * @param type
	 *            type of elements
	 */
	public RelationMetaDataParser(ONDEXGraph og, String type)
			throws JAXBException {
		super(og, new HashMap<Integer, Integer>());
		this.type = type;
	}

	/**
	 * Parses ONDEX Relation Meta Data.
	 * 
	 * @param xmlr
	 *            XMLStramReader
	 */
	@Override
	public void parse(XMLStreamReader xmlr) throws XMLStreamException {

		if (type.equals(XMLTagNames.RELATIONTYPE)) {

			// parse relation type and store it in ondex graph meta data
			parseRelationTypeContent(xmlr);

		}
	}

}
