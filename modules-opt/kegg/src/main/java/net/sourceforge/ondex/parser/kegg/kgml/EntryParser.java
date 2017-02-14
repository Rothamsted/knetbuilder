package net.sourceforge.ondex.parser.kegg.kgml;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.parser.kegg.Parser;

import org.codehaus.stax2.XMLStreamReader2;

public class EntryParser extends KgmlComponentParser {

	// stop parser from progressing
	private boolean ready = true;

	// CVs
	private DataSource dataSourceMap, dataSourceKEGG, dataSourceEC;

	// EvidenceType = IMPD
	private EvidenceType et;

	// RelationType = member_of
	private RelationType ofType;

	// hitting a EC number
	private Pattern ecPattern = Pattern
			.compile("\\d+\\.[\\d-]+\\.[\\d-]+\\.[\\d-]+");

	// Attribute for link URL
	private AttributeName anURL;

	// graphical attributes
	private AttributeName attrGraphicalX, attrGraphicalY, attrColor, attrShape,
			attrVisible, attrWidth, attrHeight, attrDrawColor, attrLine,
			attrAlpha, attrDrawAlpha;

	// current ONDEX graph
	private ONDEXGraph aog;

	// non matching types
	public static Set<String> notfound = new HashSet<String>();

	// map of KEGG id to ONDEXConcept
	private Map<String, ONDEXConcept> id2Concepts = new HashMap<String, ONDEXConcept>();

	// parser for top pathway concept
	private PathwayParser pathway;

	public EntryParser(ONDEXGraph aog, Parser parser, PathwayParser pathway) {
		super(parser, "entry");
		this.aog = aog;
		this.pathway = pathway;

		// check meta data
		dataSourceMap = aog.getMetaData().getDataSource("KEGGMAP");
		if (dataSourceMap == null)
			dataSourceMap = aog.getMetaData().getFactory()
					.createDataSource("KEGGMAP");
		dataSourceKEGG = aog.getMetaData().getDataSource("KEGG");
		dataSourceEC = aog.getMetaData().getDataSource("EC");
		et = aog.getMetaData().getEvidenceType("IMPD");
		ofType = aog.getMetaData().getRelationType("member_of");
		anURL = aog.getMetaData().getAttributeName("URL");

		// sanity checks
		if (dataSourceKEGG == null) {
			parser.fireEventOccurred(new DataSourceMissingEvent("KEGG",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (dataSourceEC == null) {
			parser.fireEventOccurred(new DataSourceMissingEvent("EC",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (et == null) {
			parser.fireEventOccurred(new EvidenceTypeMissingEvent("IMPD",
					"[EntryParser - constructor]"));
			ready = false;
		}
		if (anURL == null) {
			parser.fireEventOccurred(new AttributeNameMissingEvent("URL",
					"[EntryParser - constructor]"));
			ready = false;
		}

		// get meta data
		ONDEXGraphMetaData meta = aog.getMetaData();

		// check for attribute names
		if ((attrGraphicalX = meta.getAttributeName("graphicalX")) == null)
			attrGraphicalX = meta.getFactory().createAttributeName(
					"graphicalX", Double.class);
		if ((attrGraphicalY = meta.getAttributeName("graphicalY")) == null)
			attrGraphicalY = meta.getFactory().createAttributeName(
					"graphicalY", Double.class);
		if ((attrColor = meta.getAttributeName("color")) == null)
			attrColor = meta.getFactory().createAttributeName("color",
					java.awt.Color.class);
		if ((attrDrawColor = meta.getAttributeName("drawcolor")) == null)
			attrDrawColor = meta.getFactory().createAttributeName("drawcolor",
					java.awt.Color.class);
		if ((attrShape = meta.getAttributeName("shape")) == null)
			attrShape = meta.getFactory().createAttributeName("shape",
					java.lang.Integer.class);
		if ((attrLine = meta.getAttributeName("line")) == null)
			attrLine = meta.getFactory().createAttributeName("line",
					java.lang.String.class);
		if ((attrVisible = meta.getAttributeName("visible")) == null)
			attrVisible = meta.getFactory().createAttributeName("visible",
					java.lang.Boolean.class);
		if ((attrWidth = meta.getAttributeName("width")) == null)
			attrWidth = meta.getFactory().createAttributeName("width",
					java.lang.Integer.class);
		if ((attrHeight = meta.getAttributeName("height")) == null)
			attrHeight = meta.getFactory().createAttributeName("height",
					java.lang.Integer.class);
		if ((attrAlpha = meta.getAttributeName("alpha")) == null)
			attrAlpha = meta.getFactory().createAttributeName("alpha",
					java.lang.Integer.class);
		if ((attrDrawAlpha = meta.getAttributeName("drawalpha")) == null)
			attrDrawAlpha = meta.getFactory().createAttributeName("drawalpha",
					java.lang.Integer.class);
	}

	@Override
	public void parse(XMLStreamReader2 xmlStreamReader)
			throws XMLStreamException {

		if (ready) {

			// get all attributes for entry element
			String id = xmlStreamReader.getAttributeValue(null, "id");
			String name = xmlStreamReader.getAttributeValue(null, "name");
			String type = xmlStreamReader.getAttributeValue(null, "type");
			String link = xmlStreamReader.getAttributeValue(null, "link");
			String reaction = xmlStreamReader.getAttributeValue(null,
					"reaction");

			// get entry type mapping for concepts
			String ccID = EntityMapping.getConceptClassMapping(type);
			if (ccID != null) {
				ConceptClass cc = aog.getMetaData().getConceptClass(ccID);
				if (cc == null) {
					parser.fireEventOccurred(new ConceptClassMissingEvent(ccID,
							"[EntryParser - parse]"));
					return;
				}

				// create new concept for entry
				String pid = name;
				if (checkNotEmpty(reaction)) {
					pid = reaction;
				}
				ONDEXConcept concept = aog.getFactory().createConcept(pid,
						dataSourceMap, cc, et);
				concept.setDescription(pathway.getConcept().getDescription());
				concept.addTag(pathway.getConcept());
				id2Concepts.put(id, concept);

				// for any other than compound create member_of
				if (!ccID.equals("Comp")) {
					// member is part relation
					ONDEXRelation relation = aog.getFactory().createRelation(
							concept, pathway.getConcept(), ofType, et);
					relation.addTag(pathway.getConcept());

					// default set to visible
					relation.createAttribute(attrVisible, Boolean.FALSE, false);
				}

				// add possible name
				if (checkNotEmpty(name)) {
					concept.createConceptName(name, false);
					extractEC(concept, name);

					// transform into KEGG accessions
					String[] split = name.split(" ");
					for (String s : split) {
						concept.createConceptAccession(s.toUpperCase(),
								dataSourceKEGG, true);
						if (s.contains(":")) {
							s = s.substring(s.indexOf(":") + 1);
							concept.createConceptAccession(s.toUpperCase(),
									dataSourceKEGG, true);
						}
					}
				}

				// add possible URL
				if (checkNotEmpty(link)) {
					concept.createAttribute(anURL, link, false);
				}

				parseConceptGraphics(xmlStreamReader, concept);
			} else {
				notfound.add(type);
			}
		}
	}

	/**
	 * Returns list of concepts indexed by KEGG ID.
	 * 
	 * @return Map<String, ONDEXConcept>
	 */
	public Map<String, ONDEXConcept> getId2Concepts() {
		return id2Concepts;
	}

	/**
	 * Extracts possible EC number from name and adds it as accession.
	 * 
	 * @param concept
	 *            ONDEXConcept
	 * @param name
	 *            String
	 */
	private void extractEC(ONDEXConcept concept, String name) {
		// add EC number from name
		Matcher m = ecPattern.matcher(name);
		while (m.find()) {
			concept.createConceptAccession(m.group(), dataSourceEC, false);
		}
	}

	/**
	 * Adds graphical attributes to a concept.
	 * 
	 * @param xmlStreamReader
	 * @param concept
	 * @throws XMLStreamException
	 */
	private void parseConceptGraphics(XMLStreamReader2 xmlStreamReader,
			ONDEXConcept concept) throws XMLStreamException {

		// make sure we are hitting the right graphics elements
		xmlStreamReader.nextTag();
		int eventType = xmlStreamReader.getEventType();
		if (eventType == XMLStreamConstants.START_ELEMENT
				&& xmlStreamReader.getLocalName().equals("graphics")) {

			// get all attributes of graphics element
			String graphicsName = xmlStreamReader.getAttributeValue(null,
					"name");
			String graphicsFgcolor = xmlStreamReader.getAttributeValue(null,
					"fgcolor");
			String graphicsBgcolor = xmlStreamReader.getAttributeValue(null,
					"bgcolor");
			String graphicsType = xmlStreamReader.getAttributeValue(null,
					"type");
			String graphicsX = xmlStreamReader.getAttributeValue(null, "x");
			String graphicsY = xmlStreamReader.getAttributeValue(null, "y");
			String graphicsWidth = xmlStreamReader.getAttributeValue(null,
					"width");
			String graphicsHeight = xmlStreamReader.getAttributeValue(null,
					"height");
			String graphicsCoords = xmlStreamReader.getAttributeValue(null,
					"coords");

			// default set to visible
			concept.createAttribute(attrVisible, Boolean.TRUE, false);

			// add graphics name
			if (checkNotEmpty(graphicsName)) {
				concept.createConceptName(graphicsName, true);
				extractEC(concept, name);
			}

			// resolve shape
			if (checkNotEmpty(graphicsType)) {
				Integer shapeID = EntityMapping
						.getGraphicsTypeMapping(graphicsType);
				// can be empty for line
				if (shapeID != null && shapeID > -1) {
					concept.createAttribute(attrShape, shapeID, false);
				} else if (shapeID != null && shapeID == -1
						&& checkNotEmpty(graphicsCoords)) {
					// this is a line shape
					concept.createAttribute(attrLine, graphicsCoords, false);
					graphicsBgcolor = "none";
				} else {
					notfound.add(graphicsType);
				}
			}

			// set X coordinate
			if (checkNotEmpty(graphicsX)) {
				Double x = Double.valueOf(graphicsX);
				concept.createAttribute(attrGraphicalX, x, false);
			} else {
				concept.createAttribute(attrGraphicalX, Double.valueOf(0),
						false);
			}

			// set Y coordinate
			if (checkNotEmpty(graphicsY)) {
				Double y = Double.valueOf(graphicsY);
				concept.createAttribute(attrGraphicalY, y, false);
			} else {
				concept.createAttribute(attrGraphicalY, Double.valueOf(0),
						false);
			}

			// set colour
			if (checkNotEmpty(graphicsBgcolor)) {
				if (!graphicsBgcolor.equalsIgnoreCase("none")) {
					Color color = Color.decode(graphicsBgcolor);
					concept.createAttribute(attrColor, color, false);
				} else {
					Color color = new Color(255, 255, 255);
					concept.createAttribute(attrColor, color, false);
					concept.createAttribute(attrAlpha, Integer.valueOf(0),
							false);
				}
			}

			// set draw colour
			if (checkNotEmpty(graphicsFgcolor)) {
				if (!graphicsFgcolor.equalsIgnoreCase("none")) {
					Color color = Color.decode(graphicsFgcolor);
					concept.createAttribute(attrDrawColor, color, false);
				} else {
					Color color = new Color(255, 255, 255);
					concept.createAttribute(attrDrawColor, color, false);
					concept.createAttribute(attrDrawAlpha, Integer.valueOf(0),
							false);
				}
			}

			// set width
			if (checkNotEmpty(graphicsWidth) && !graphicsType.equals("line")) {
				Integer width = Integer.valueOf(graphicsWidth);
				concept.createAttribute(attrWidth, width, false);
			}

			// set height
			if (checkNotEmpty(graphicsHeight) && !graphicsType.equals("line")) {
				Integer height = Integer.valueOf(graphicsHeight);
				concept.createAttribute(attrHeight, height, false);
			}

			// process multi line coordinates
			xmlStreamReader.nextTag();
			xmlStreamReader.nextTag();
			eventType = xmlStreamReader.getEventType();
			if (eventType == XMLStreamConstants.START_ELEMENT
					&& xmlStreamReader.getLocalName().equals("graphics")) {
				graphicsCoords = xmlStreamReader.getAttributeValue(null,
						"coords");
				if (checkNotEmpty(graphicsCoords)) {
					// this is a line shape
					Attribute oldAttribute = concept.getAttribute(attrLine);
					String oldCoords = (String) oldAttribute.getValue();
					oldAttribute.setValue(oldCoords + ";" + graphicsCoords);
				}
			}
		}
	}

	/**
	 * Simply check for null or empty Strings.
	 * 
	 * @param s
	 *            String to check
	 * @return true if not empty
	 */
	private boolean checkNotEmpty(String s) {
		return s != null && s.trim().length() > 0;
	}
}
