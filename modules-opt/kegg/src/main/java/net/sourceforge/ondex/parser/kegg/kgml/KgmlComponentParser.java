package net.sourceforge.ondex.parser.kegg.kgml;

import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.parser.kegg.Parser;

import org.codehaus.stax2.XMLStreamReader2;

/**
 * Abstract parent class for component parser for KGML.
 * 
 * @author taubertj
 * 
 */
public abstract class KgmlComponentParser {

	// back reference to Parser
	protected Parser parser;

	// name of component
	protected String name;

	/**
	 * Constructor for KEGG parser and name of component.
	 * 
	 * @param parser
	 *            Parser
	 * @param name
	 *            String
	 */
	public KgmlComponentParser(Parser parser, String name) {
		this.parser = parser;
		this.name = name;
	}

	/**
	 * Name of implementing component.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method to start parsing.
	 * 
	 * @param xmlStreamReader
	 *            XMLStreamReader2
	 * @throws XMLStreamException
	 */
	public abstract void parse(XMLStreamReader2 xmlStreamReader)
			throws XMLStreamException;
}
