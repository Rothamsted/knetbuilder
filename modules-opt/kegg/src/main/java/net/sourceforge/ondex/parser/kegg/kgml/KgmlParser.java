package net.sourceforge.ondex.parser.kegg.kgml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.event.type.ParsingErrorEvent;
import net.sourceforge.ondex.parser.kegg.Parser;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * This parser parses KGML files according to http://www.genome.jp/kegg/xml/
 * 
 * @author taubertj
 * 
 */
public class KgmlParser {

	// back reference to Parser
	private Parser parser;

	// list of delegate parsers
	private Map<String, KgmlComponentParser> delegates;

	/**
	 * Constructor initialises internal data.
	 * 
	 */
	public KgmlParser(Parser parser) {
		this.parser = parser;
		delegates = new HashMap<String, KgmlComponentParser>();
	}

	/**
	 * Parser a given KGML file from the InputStream.
	 * 
	 * @param input
	 *            InputStream
	 */
	public void parse(InputStream input) {

		// configure XMLInputFactory2
		XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory2
				.newInstance();
		inputFactory.configureForSpeed();

		// stop parser from looking up of DTD
		inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
		inputFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);

		try {

			// reader xml content from InputStream
			XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) inputFactory
					.createXMLStreamReader(input, "UTF-8");

			while (xmlStreamReader.hasNext()) {

				int event = xmlStreamReader.next();

				// return on END_DOCUMENT
				if (event == XMLStreamConstants.END_DOCUMENT) {
					return;
				}

				// start of a new element
				if (event == XMLStreamConstants.START_ELEMENT) {

					String element = xmlStreamReader.getLocalName();

					// delegate to registered element parsers
					if (delegates.containsKey(element)) {
						KgmlComponentParser delegate = delegates.get(element);
						delegate.parse(xmlStreamReader);
					}
				}
			}

		} catch (XMLStreamException xse) {
			parser.fireEventOccurred(new ParsingErrorEvent(xse.getMessage(),
					"[KgmlParser - parse]"));
		}
	}

	/**
	 * Registers a KgmlComponentParser with the list of delegates.
	 * 
	 * @param parser
	 *            KgmlComponentParser
	 */
	public void registerComponentParser(KgmlComponentParser parser) {
		this.delegates.put(parser.getName(), parser);
	}
}
