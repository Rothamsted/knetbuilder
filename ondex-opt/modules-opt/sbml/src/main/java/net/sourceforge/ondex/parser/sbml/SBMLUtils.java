package net.sourceforge.ondex.parser.sbml;

import java.util.HashMap;

import javax.xml.stream.XMLStreamReader;

/**
 * Collection of static utility methods.
 * 
 * @author taubertj
 * 
 */
public class SBMLUtils {
	
	/**
	 * Converts attributes on an element to an easily read HashMap Does not
	 * store empty or 0 length attribute values
	 * 
	 * @param staxXmlReader
	 * @return String name to String value HashMap
	 */
	public static HashMap<String, String> parseAttributesToHashMap(
			XMLStreamReader staxXmlReader) {
		HashMap<String, String> attribs = new HashMap<String, String>();
		for (int i = 0; i < staxXmlReader.getAttributeCount(); i++) {
			String name = staxXmlReader.getAttributeName(i).getLocalPart()
					.trim();
			String value = staxXmlReader.getAttributeValue(i).trim();
			if (name != null && value != null && name.length() > 0
					&& value.length() > 0) {
				attribs.put(name, value);
			}
		}
		return attribs;
	}
}
