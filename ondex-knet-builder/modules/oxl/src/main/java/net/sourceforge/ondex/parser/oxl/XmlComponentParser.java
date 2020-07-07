package net.sourceforge.ondex.parser.oxl;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.exception.type.InconsistencyException;

/**
 * ComponentParser interface that defines the contract between the main StAX
 * event loop and parsing components.
 * 
 * more about this: http://www.devx.com/Java/Article/30298/1954?pf=true
 * 
 * @author sierenk
 */
public interface XmlComponentParser {

	/**
	 * Name of implementing component.
	 * 
	 * @return String
	 */
	public String getName();

	/**
	 * Method to start parsing.
	 * 
	 * @param staxXmlReader
	 *            XMLStreamReader
	 * @throws XMLStreamException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws InconsistencyException 
	 */
	public void parse(XMLStreamReader staxXmlReader) throws XMLStreamException,
			JAXBException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InconsistencyException;

}
