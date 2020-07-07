package net.sourceforge.ondex.export.xhtml;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

/**
 * Makes writing tables in xhtml considerably easier.
 * These methods assume you are writing in a html 'table' tag but they will deal with 'tr' tags
 * 
 * @author hindlem
 *
 */
public class XHTMLTable {

	//to pretty print or not to pretty print
	public final static boolean PRETTY = true;

	/**
	 * Write  a header pair of 2
	 * @param xmlw the stream to write to
	 * @param name the first value in the pair
	 * @param value the second value in the pair
	 * @throws XMLStreamException
	 */
	public static void writeHeaderPair(XMLStreamWriter2 xmlw, String name, String value) throws XMLStreamException {
		writeHeaders(xmlw, new String[] {name, value});
	}

	/**
	 * Write a n headers
	 * @param xmlw the stream to write to
	 * @param headers
	 * @throws XMLStreamException on StAX error
	 */
	public static void writeHeaders(XMLStreamWriter2 xmlw, String[] headers) throws XMLStreamException {
		xmlw.writeStartElement("tr");

		for(String header: headers) {

			xmlw.writeStartElement("th");
			xmlw.writeCharacters(header);
			xmlw.writeEndElement();
			if (PRETTY) xmlw.writeCharacters("\n");
		}

		xmlw.writeEndElement();
		if (PRETTY) xmlw.writeCharacters("\n");
	}

	/**
	 * 
	 * @param xmlw the stream to write to
	 * @param name the first value in the pair
	 * @param value the second value in the pair
	 * @param html are both elements in the pair in html form (otherwise remove offending tags)
	 * @throws XMLStreamException on StAX error
	 */
	public static void writeNameValuePair(XMLStreamWriter2 xmlw, String name, String value, boolean html) throws XMLStreamException {
		writeRow(xmlw, new String[] {name, value}, html);
	}

	/**
	 * 
	 * @param xmlw the stream to write to
	 * @param rowdata the row to write
	 * @param html are all elements in the row in html form (otherwise remove offending tags)
	 * @throws XMLStreamException on StAX error
	 */
	public static void writeRow(XMLStreamWriter2 xmlw, String[] rowdata, boolean html) throws XMLStreamException {
		xmlw.writeStartElement("tr");

		for(String col: rowdata) {

			xmlw.writeStartElement("td");
			if (!html) xmlw.writeCharacters(col);
			else xmlw.writeRaw(col);
			xmlw.writeEndElement();
			if (PRETTY) xmlw.writeCharacters("\n");
		}

		xmlw.writeEndElement();
		if (PRETTY) xmlw.writeCharacters("\n");
	}
	
	/**
	 * Writes row containing mixed html and non html data
	 * @param xmlw the stream to write to
	 * @param rowdata the row to write
	 * @param html elements in the row in html form (otherwise remove offending tags)
	 * @throws XMLStreamException on StAX error
	 */
	public static void writeMixed(XMLStreamWriter2 xmlw, String[] rowdata, boolean[] html) throws XMLStreamException {
		xmlw.writeStartElement("tr");

		if (rowdata.length != html.length) {
			System.err.println("rowdata - html mismatch");
		}
		
		for (int i = 0; i < rowdata.length; i++) {
			
			xmlw.writeStartElement("td");
			if (!html[i]) xmlw.writeCharacters(rowdata[i]);
			else xmlw.writeRaw(rowdata[i]);
			xmlw.writeEndElement();
			if (PRETTY) xmlw.writeCharacters("\n");
		}

		xmlw.writeEndElement();
		if (PRETTY) xmlw.writeCharacters("\n");
	}

	/**
	 * Writes pair containing mixed html and non html data
	 * @param xmlw the stream to write to
	 * @param valueA the first value in the pair
	 * @param valueB the second value in the pair
	 * @param rawWriteA the first value is html?
	 * @param rawWriteB the second value is html?
	 * @throws XMLStreamException  on StAX error
	 */
	public static void writeMixedPairRow(XMLStreamWriter2 xmlw, String valueA,
			String valueB, boolean rawWriteA, boolean rawWriteB) throws XMLStreamException {
		writeMixed(xmlw, new String[] {valueA, valueB}, new boolean[] {rawWriteA, rawWriteB});
	}
}
