package net.sourceforge.ondex.export.xhtml;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

/**
 * A helper class with static methods to ease the writing of xhtml files
 * 
 * 
 * @author hindlem
 *
 */
public class XHTMLHelper {

	//format xml nice a pretty
	public final static boolean PRETTY = true;
	
	private static final String XHTML = "http://www.w3.org/1999/xhtml";

	/**
	 * Writes the starting tags for a xhtml file
	 * @param xmlw the stream to write to
	 * @param title the title of the xhtml document
	 * @throws XMLStreamException on StAX error
	 */
	public static void initializeXHTMLFile(XMLStreamWriter2 xmlw, String title) throws XMLStreamException {
		xmlw.writeStartDocument();

		if (PRETTY) xmlw.writeCharacters("\n");
		xmlw.writeDTD("<!DOCTYPE html " +
				"PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

		if (PRETTY)xmlw.writeCharacters("\n");
		xmlw.writeStartElement("html");
		xmlw.writeDefaultNamespace(XHTML);

		if (PRETTY)xmlw.writeCharacters("\n");
		xmlw.writeStartElement(XHTML, "head");
		
		xmlw.writeRaw("<script type=\"text/javascript\">/* <![CDATA[ */"+
				"function popup(url) { "+
				"newwindow=window.open(url,'name','height=200,width=850, resizable=yes');"+
				"if (window.focus) {"+
				"	newwindow.focus()"+
				"} "+
				"return false;"+
				" }"+
				"/* ]]> */"+

				"window.onload = function() {"+
				"var links = document.getElementsByTagName('a');"+
				"for (var i=0;i &lt; links.length;i++) {"+
				"if (links[i].className == 'new-window') {"+
				"links[i].onclick = function() {"+
				"window.open(this.href);"+
				"return false;"+
				"};"+
				"}"+
				"}"+
				"};"+
		"</script>");
		
		xmlw.writeStartElement("blank");
		xmlw.writeAttribute("target", "_blank");
		xmlw.writeEndElement();
		
		if (PRETTY)xmlw.writeCharacters("\n");
		xmlw.writeStartElement(XHTML, "title");
		xmlw.writeCharacters(title);
		xmlw.writeEndElement();
		
		if (PRETTY)xmlw.writeCharacters("\n");
		xmlw.writeEndElement();

		if (PRETTY) xmlw.writeCharacters("\n");
		xmlw.writeStartElement(XHTML, "body");
	}
	
	/**
	 * Closes a previously opened xhtml document (does not close stream)
	 * @param xmlw the stream to write to
	 * @throws XMLStreamException on StAX error
	 * @see initializeXHTMLFile(XMLStreamWriter2 xmlw, String title)
	 */
	public static void closeXHTMLFile(XMLStreamWriter2 xmlw) throws XMLStreamException {
		xmlw.writeEndElement();
		if (PRETTY) xmlw.writeCharacters("\n");
		
		xmlw.writeEndDocument();
		if (PRETTY) xmlw.writeCharacters("\n");
	}
	
	/**
	 * Writes a anchor that can be linked to in the xhtml document
	 * @param xmlw the stream to write to
	 * @param name the name of the anchor
	 * @throws XMLStreamException on StAX error
	 */
	public static void createAnchor(XMLStreamWriter2 xmlw, String name) throws XMLStreamException {
		xmlw.writeStartElement("a");
		xmlw.writeAttribute("name", name);
		xmlw.writeEndElement();
		if (PRETTY) xmlw.writeCharacters("\n");
		
	}
	
}
