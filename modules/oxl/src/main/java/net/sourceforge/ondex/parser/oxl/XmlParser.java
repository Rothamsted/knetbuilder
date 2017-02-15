package net.sourceforge.ondex.parser.oxl;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.export.oxl.Export;
import net.sourceforge.ondex.export.oxl.XMLTagNames;

/**
 * Parses XML Documents.
 * 
 * @author sierenk, taubertj
 * @author Matthew Pocock
 *         <p/>
 *         Uses XML parsing pattern, more about this:
 *         http://www.devx.com/Java/Article/30298/1954?pf=true
 */
public class XmlParser implements XmlComponentParser {

	// Saves parsing components
	private HashMap<String, XmlComponentParser> delegates;

	// back reference to calling parser for logging purpose
	private Parser oxlparser;

	// nicer format for numbers
	private static final NumberFormat ELEMENT_NUMBER_FORMAT = NumberFormat
			.getInstance();

	// track counts of elements
	private Map<String, Integer> count = new HashMap<String, Integer>();

	// when to stop parsing
	private boolean cancelled = false;

	/**
	 * Default constructor.
	 */
	public XmlParser() {
		// for parsing components
		delegates = new HashMap<String, XmlComponentParser>();
	}

	/**
	 * Default constructor.
	 */
	public XmlParser(Parser oxlparser) {
		this();
		this.oxlparser = oxlparser;
	}

	/**
	 * Returns the name of this XmlComponentParser.
	 * 
	 * @return String
	 */
	public String getName() {
		return "XMLParser";
	}

	/**
	 * Parses a XmlStreamReader, which contains a XML Document.
	 * <p/>
	 * In the main event loop, farms out parsing work to ComponentParsers based
	 * on the XML element name. That keeps the main event loop code simple and
	 * faciliates understanding of the XML format.
	 * 
	 * @param staxXmlReader
	 *            XMLStreamReader
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws InconsistencyException
	 */
	public void parse(XMLStreamReader staxXmlReader) throws XMLStreamException,
			JAXBException, InconsistencyException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		// main loop
		while (staxXmlReader.hasNext() && !cancelled) {
			int event = staxXmlReader.next();

			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = staxXmlReader.getLocalName();

				// check versions, complain only
				if (element.equals(XMLTagNames.VERSION)) {
					String version = staxXmlReader.getElementText();
					if (!version.equals(Export.version)) {
						// fixme: log?
						System.err.println("Different OXL versions found.");
					}
				}

				// If a Component Parser is registered that can handle
				// this element delegate
				if (delegates.containsKey(element)) {

					XmlComponentParser parser = delegates.get(element);

					if (count.containsKey(element)) {
						int current = count.get(element);

						if (current % 5000 == 0) {
							// System.out.println(element + " parsed " +
							// ELEMENT_NUMBER_FORMAT.format(current) +
							// " elements");
							if (oxlparser != null) {
								oxlparser
										.fireEventOccurred(new GeneralOutputEvent(
												element
														+ " parsed "
														+ ELEMENT_NUMBER_FORMAT
																.format(current)
														+ " elements", ""));
							}
						}

						count.put(element, ++current);
					} else {
						count.put(element, 1);
					}
					parser.parse(staxXmlReader);
				}
			}
		} // eof
	}

	/**
	 * Method to register a parsing component.
	 * 
	 * @param elementName
	 *            XML Element
	 * @param elementParser
	 *            associated parsing component
	 */
	public void registerParser(String elementName,
			XmlComponentParser elementParser) {
		delegates.put(elementName, elementParser);
	}

	/**
	 * Sets the state of this parser to cancelled.
	 * 
	 * @param c
	 *            process is cancelled
	 */
	public void setCancelled(boolean c) {
		this.cancelled = c;
	}

	/**
	 * Returns the current element count.
	 * 
	 * @param element
	 *            XML element name
	 * @return appearance count
	 */
	public int getCount(String element) {
		return count.get(element);
	}
}
