package net.sourceforge.ondex.export.sbml;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;

import com.ctc.wstx.stax.WstxInputFactory;

/**
 * Config file reader for the sbml mappings
 * 
 * @author lysenkoa
 * @date 13.06.07
 */
public class ConfigReader {

	private WstxInputFactory factory;

	private Map<String, Map<String, String>> masterMapKey = new HashMap<String, Map<String, String>>();

	private Map<String, Set<String>> masterSetKey = new HashMap<String, Set<String>>();

	private XMLStreamReader2 staxXmlReader;

	public ConfigReader(String inputFile) {
		System.setProperty("javax.xml.stream.XMLInputFactory",
				"com.ctc.wstx.stax.WstxInputFactory");
		factory = (WstxInputFactory) WstxInputFactory.newInstance();
		factory.setProperty(WstxInputFactory.IS_NAMESPACE_AWARE, false);

		// this is for set configuration parameters
		masterSetKey.put("ConceptClassToAnnotation", new HashSet<String>());
		masterSetKey.put("ConceptClassToReaction", new HashSet<String>());
		masterSetKey.put("RelationTypeToAnnotation", new HashSet<String>());
		masterSetKey.put("RelationTypeToRegArc", new HashSet<String>());
		masterSetKey.put("RelationTypeReverseLogic", new HashSet<String>());

		// this is for map configuration parameters
		masterMapKey.put("ConceptClassToSBO", new HashMap<String, String>());

		Set<String> activeSet = new HashSet<String>();
		Map<String, String> activeMap = new HashMap<String, String>();
		try {
			staxXmlReader = factory.createXMLStreamReader(new File(inputFile));
			while (staxXmlReader.hasNext()) {

				String element;
				int event = staxXmlReader.next();
				switch (event) {
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.START_ELEMENT:
					element = staxXmlReader.getLocalName();

					// activate corresponding set
					if (masterSetKey.containsKey(element))
						activeSet = masterSetKey.get(element);

					// activate corresponding map
					else if (masterMapKey.containsKey(element))
						activeMap = masterMapKey.get(element);

					// add list item to set
					else if (element.equals("list_item"))
						activeSet.add(staxXmlReader.getElementText());

					// split map item and put into map
					else if (element.equals("map_item")) {
						String[] split = staxXmlReader.getElementText().split(
								",");
						activeMap.put(split[0], split[1]);
					}

					break;
				case XMLStreamConstants.END_ELEMENT:
					break;
				case XMLStreamConstants.END_DOCUMENT:
					staxXmlReader.close();
					break;
				default:
					break;
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getConceptClassToAnnotation() {
		return masterSetKey.get("ConceptClassToAnnotation");
	}

	public Set<String> getConceptClassToReaction() {
		return masterSetKey.get("ConceptClassToReaction");
	}

	public Map<String, String> getConceptClassToSBO() {
		return masterMapKey.get("ConceptClassToSBO");
	}

	public Set<String> getRelationTypeReverseLogic() {
		return masterSetKey.get("RelationTypeReverseLogic");
	}

	public Set<String> getRelationTypeToAnnotation() {
		return masterSetKey.get("RelationTypeToAnnotation");
	}

	public Set<String> getRelationTypeToRegArc() {
		return masterSetKey.get("RelationTypeToRegArc");
	}
}
