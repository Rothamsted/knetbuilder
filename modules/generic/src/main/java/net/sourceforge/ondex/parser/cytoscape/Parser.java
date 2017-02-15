package net.sourceforge.ondex.parser.cytoscape;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
import net.sourceforge.ondex.exception.type.WrongArgumentException;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.ziptools.ZipEndings;

import org.codehaus.stax2.XMLInputFactory2;

@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Parser extends ONDEXParser {

	public static final String GRAPH_NAME_ARG = "GraphName";

	public static final String GRAPH_NAME_ARG_DESC = "Name of the cytoscape graph to import.";

	public static final String CONCEPT_CLASS_ARG = "ConceptClass";

	public static final String CONCEPT_CLASS_ARG_DESC = "String ID of concept class to be used";

	public static final String RELATION_TYPE_ARG = "RelationType";

	public static final String RELATION_TYPE_ARG_DESC = "String ID of relation type to be used";

	public static final String CV_ARG = "DataSource";

	public static final String CV_ARG_DESC = "String ID of DataSource to be used";

	public static final String EVIDENCE_ARG = "EvidenceType";

	public static final String EVIDENCE_ARG_DESC = "String ID of EvidenceType to be used";

	public static final String INPUT_FILE_DESC = "cytoscape file to import";

	private ArgumentDefinition<?>[] argdefs = new ArgumentDefinition<?>[] {
			new StringArgumentDefinition(GRAPH_NAME_ARG, GRAPH_NAME_ARG_DESC,
					true, "", false),
			new StringArgumentDefinition(CONCEPT_CLASS_ARG,
					CONCEPT_CLASS_ARG_DESC, true, "Thing", false),
			new StringArgumentDefinition(RELATION_TYPE_ARG,
					RELATION_TYPE_ARG_DESC, true, "r", false),
			new StringArgumentDefinition(CV_ARG, CV_ARG_DESC, true, "unknown",
					false),
			new StringArgumentDefinition(EVIDENCE_ARG, EVIDENCE_ARG_DESC, true,
					"IMPD", false),
			new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE,
					INPUT_FILE_DESC, true, true, false, false) };

	private ConceptClass cc;

	private RelationType rt;

	private EvidenceType et;

	private DataSource dataSource;

	private AttributeName graphicalX, graphicalY, visible, color, shape;

	private HashMap<String, ONDEXConcept> nodeID2Concept = new HashMap<String, ONDEXConcept>();

	private XMLStreamReader r;

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return argdefs;
	}

	@Override
	public String getName() {
		return "Cytoscape parser";
	}

	@Override
	public String getVersion() {
		return "17.09.2012";
	}

	@Override
	public String getId() {
		return "cytoscape";
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * Creates an appropriate InputStream from given file.
	 * 
	 * @param filename
	 *            filename to be used
	 * @return InputStream on File
	 * @throws IOException
	 * @throws InvalidPluginArgumentException
	 * @throws WrongArgumentException
	 */
	private InputStream getInStream(String filename) throws IOException,
			InvalidPluginArgumentException, WrongArgumentException {
		InputStream inStream = null;

		int detectedEnding = ZipEndings.getPostfix(filename);

		// do decided where file is coming from
		URL url;
		if (filename.startsWith("http:") || filename.startsWith("file:") || filename.startsWith("https:")) {
			// when loading from a server
			url = new URL(filename);
		} else {
			File file = new File(filename);
			url = file.toURI().toURL();
		}

		// get decompression right
		switch (detectedEnding) {

		case ZipEndings.GZ:
			inStream = new GZIPInputStream((InputStream) url.getContent());
			System.out.println("Detected GZIP file");
			break;
		case ZipEndings.ZIP:
			String targetEntryName = args.getUniqueValue(GRAPH_NAME_ARG)
					+ ".xgmml";
			File file = new File(filename);
			String zipFileName = file.getAbsolutePath();
			ZipFile zipFile = new ZipFile(zipFileName);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			ZipEntry zipEntry = null;
			while (zipEntries.hasMoreElements()) {
				zipEntry = zipEntries.nextElement();
				if (zipEntry.getName().endsWith(targetEntryName)) {
					break;// correct entry found
				}
			}
			if (zipEntry != null) {
				inStream = zipFile.getInputStream(zipEntry);
			} else {
				throw new WrongArgumentException("Graph not contained in file.");
			}
			System.out.println("Detected ZIP file");
			break;
		case ZipEndings.XGMML:
			inStream = (InputStream) url.getContent();
			System.out.println("Detected XGMML file");
			break;
		default:
			System.err.println("Unsupported filetype");
		}

		return inStream;
	}

	/**
	 * Work-around method for reuse in applet.
	 * 
	 * @param url
	 * @throws Exception
	 */
	public void start(String url) throws Exception {
		InputStream inStream = getInStream(url);
		parse(inStream);
	}

	@Override
	public void start() throws Exception {
		InputStream inStream = getInStream((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		parse(inStream);
	}

	/**
	 * Parse XML data from InputStream
	 * 
	 * @param inStream
	 * @throws Exception
	 */
	private void parse(InputStream inStream) throws Exception {
		fetchMetaData();
		XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2
				.newInstance();
		r = factory.createXMLStreamReader(inStream);
		while (r.hasNext()) {
			int event = r.next();
			// find start element with name graph
			if (event == XMLStreamConstants.START_ELEMENT) {
				String elementName = r.getLocalName();
				if (elementName.equals("graph")) {
					parseGraph();
				}
			}
		}
		r.close();
	}

	private void fetchMetaData() throws InvalidPluginArgumentException {
		String id = (String) args.getUniqueValue(CONCEPT_CLASS_ARG);
		cc = graph.getMetaData().getConceptClass(id);
		if (cc == null) {
			cc = graph.getMetaData().getFactory().createConceptClass(id);
		}
		id = (String) args.getUniqueValue(RELATION_TYPE_ARG);
		rt = graph.getMetaData().getRelationType(id);
		if (rt == null) {
			rt = graph.getMetaData().getFactory().createRelationType(id);
		}
		id = (String) args.getUniqueValue(CV_ARG);
		dataSource = graph.getMetaData().getDataSource(id);
		if (dataSource == null) {
			dataSource = graph.getMetaData().getFactory().createDataSource(id);
		}
		id = (String) args.getUniqueValue(EVIDENCE_ARG);
		et = graph.getMetaData().getEvidenceType(id);
		if (et == null) {
			et = graph.getMetaData().getFactory().createEvidenceType(id);
		}

		// always present for importing coordinates from cytoscape
		graphicalX = graph.getMetaData().getFactory()
				.createAttributeName("graphicalX", Double.class);
		graphicalY = graph.getMetaData().getFactory()
				.createAttributeName("graphicalY", Double.class);
		visible = graph.getMetaData().getFactory()
				.createAttributeName("visible", Boolean.class);
		color = graph.getMetaData().getFactory()
				.createAttributeName("color", java.awt.Color.class);
		shape = graph.getMetaData().getFactory()
				.createAttributeName("shape", Integer.class);
	}

	private void parseGraph() throws XMLStreamException, ParsingFailedException {
		boolean more = true;
		for (int event = r.next(); more; event = r.next()) {
			switch (event) {
			case XMLStreamConstants.END_DOCUMENT:
				r.close();
				more = false;
				break;
			case XMLStreamConstants.START_ELEMENT:
				String elementName = r.getLocalName();
				if (elementName.equals("node")) {
					parseNode();
				} else if (elementName.equals("edge")) {
					parseEdge();
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (r.getLocalName().equals("graph")) {
					more = false;
					break;
				}
			}
		}
	}

	private void parseEdge() throws XMLStreamException, ParsingFailedException {
		// <edge label="YGL028C (pp) YML058W-A" source="-15" target="-59">
		Map<String, String> attributes = getAttributes();
		ONDEXConcept fromConcept = nodeID2Concept.get(attributes.get("source"));
		ONDEXConcept toConcept = nodeID2Concept.get(attributes.get("target"));
		ONDEXRelation relation = graph.getFactory().createRelation(fromConcept,
				toConcept, rt, et);

		boolean more = true;
		for (int event = r.next(); more; event = r.next()) {
			switch (event) {
			case XMLStreamConstants.END_DOCUMENT:
				r.close();
				more = false;
				break;
			case XMLStreamConstants.START_ELEMENT:
				String elementName = r.getLocalName();
				if (elementName.equals("att")) {
					parseRelationAttribute(relation);
				} else if (elementName.equals("graphics")) {
					parseRelationGraphics(relation);
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (r.getLocalName().equals("edge")) {
					more = false;
					break;
				}
			}
		}
	}

	private void parseNode() throws XMLStreamException, ParsingFailedException {
		Map<String, String> attributes = getAttributes();
		ONDEXConcept concept = createConcept(attributes.get("label"),
				attributes.get("id"));

		boolean more = true;
		for (int event = r.next(); more; event = r.next()) {
			switch (event) {
			case XMLStreamConstants.END_DOCUMENT:
				r.close();
				more = false;
				break;
			case XMLStreamConstants.START_ELEMENT:
				String elementName = r.getLocalName();
				if (elementName.equals("att")) {
					parseConceptAttribute(concept);
				} else if (elementName.equals("graphics")) {
					parseConceptGraphics(concept);
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (r.getLocalName().equals("node")) {
					more = false;
					break;
				}
			}
		}
	}

	/**
	 * Transform CytoScape graphics to ONDEX from loading appearance.
	 * 
	 * @param relation
	 *            ONDEXRelation to add attributes to
	 * @throws XMLStreamException
	 * @throws ParsingFailedException
	 */
	private void parseRelationGraphics(ONDEXRelation relation)
			throws XMLStreamException, ParsingFailedException {
		// <graphics width="1" fill="#0000ff">
		Map<String, String> attributes = getAttributes();

		if (attributes.get("fill") != null) {
			// decode colour of relation
			relation.createAttribute(color,
					Color.decode(attributes.get("fill")), false);
			// relations always visible when importing
			relation.createAttribute(visible, Boolean.TRUE, false);
		}
	}

	/**
	 * Transform CytoScape graphics to ONDEX static layout compatible Attribute.
	 * 
	 * @param concept
	 *            ONDEXConcept to add attributes to
	 * @throws XMLStreamException
	 * @throws ParsingFailedException
	 */
	private void parseConceptGraphics(ONDEXConcept concept)
			throws XMLStreamException, ParsingFailedException {
		// <graphics width="1" outline="#000000" fill="#ffffff" w="20.0"
		// h="20.0" y="469.1334533691406" x="875.56982421875" type="ellipse">
		Map<String, String> attributes = getAttributes();

		// check if there is x and y position known
		if (attributes.get("x") != null && attributes.get("y") != null
				&& attributes.get("fill") != null
				&& attributes.get("type") != null) {
			concept.createAttribute(graphicalX,
					Double.valueOf(attributes.get("x")), false);
			concept.createAttribute(graphicalY,
					Double.parseDouble(attributes.get("y")), false);
			// always visible
			concept.createAttribute(visible, Boolean.TRUE, false);
			// colour of node
			concept.createAttribute(color,
					Color.decode(attributes.get("fill")), false);
			// parse shape from type name
			String type = attributes.get("type");
			// mapping from CytoScape names to OVTK shapes
			if (type.equals("rectangle"))
				concept.createAttribute(shape, Integer.valueOf(1), false);
			else if (type.equals("ellipse"))
				concept.createAttribute(shape, Integer.valueOf(2), false);
			else if (type.equals("triangle"))
				concept.createAttribute(shape, Integer.valueOf(3), false);
			else if (type.equals("pentagon"))
				concept.createAttribute(shape, Integer.valueOf(4), false);
			else if (type.equals("octagon"))
				concept.createAttribute(shape, Integer.valueOf(5), false);
			else if (type.equals("hexagon"))
				concept.createAttribute(shape, Integer.valueOf(6), false);
			else if (type.equals("rhombus"))
				concept.createAttribute(shape, Integer.valueOf(7), false);
			else if (type.equals("polygon"))
				concept.createAttribute(shape, Integer.valueOf(8), false);
			else if (type.equals("circle"))
				concept.createAttribute(shape, Integer.valueOf(9), false);
			else
				concept.createAttribute(shape, Integer.valueOf(0), false);
		}
	}

	private void parseConceptAttribute(ONDEXConcept concept)
			throws XMLStreamException, ParsingFailedException {
		// <att type="real" name="YPL250C" value="0.432"/>
		// <att type="string" name="Official Gene Symbols" value="SCW11"/>
		// <att type="list" name="cytoscape.alias.list">
		// <att type="string" name="cytoscape.alias.list" value="YGL028C"/>
		// </att>
		Map<String, String> attributes = getAttributes();

		if (attributes.get("type") != null && attributes.get("value") != null
				&& attributes.get("name") != null
				&& !attributes.get("name").trim().equals("")) {
			if (attributes.get("name").equals("cytoscape.alias.list")) {
				concept.createConceptName(attributes.get("value"), false);
			} else {
				createAttributes(concept, attributes);
			}
		}

		boolean more = true;
		for (int event = r.next(); more; event = r.next()) {
			switch (event) {
			case XMLStreamConstants.END_DOCUMENT:
				r.close();
				more = false;
				break;
			case XMLStreamConstants.START_ELEMENT:
				String elementName = r.getLocalName();
				if (elementName.equals("att")) {
					parseConceptAttribute(concept);
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (r.getLocalName().equals("att")) {
					more = false;
					break;
				}
			}
		}
	}

	private void parseRelationAttribute(ONDEXRelation relation)
			throws XMLStreamException, ParsingFailedException {
		// <att type="real" name="YPL250C" value="0.432"/>
		// <att type="string" name="Official Gene Symbols" value="SCW11"/>
		// <att type="list" name="cytoscape.alias.list">
		// <att type="string" name="cytoscape.alias.list" value="YGL028C"/>
		// </att>
		Map<String, String> attributes = getAttributes();

		if (attributes.get("type") != null && attributes.get("value") != null
				&& attributes.get("name") != null
				&& !attributes.get("name").trim().equals("")) {
			createAttributes(relation, attributes);
		}

		boolean more = true;
		for (int event = r.next(); more; event = r.next()) {
			switch (event) {
			case XMLStreamConstants.END_DOCUMENT:
				r.close();
				more = false;
				break;
			case XMLStreamConstants.START_ELEMENT:
				String elementName = r.getLocalName();
				if (elementName.equals("att")) {
					parseRelationAttribute(relation);
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (r.getLocalName().equals("att")) {
					more = false;
					break;
				}
			}
		}
	}

	private void createAttributes(ONDEXEntity entity,
			Map<String, String> attributes) throws ParsingFailedException {
		String type = attributes.get("type");
		Class<?> clazz = null;
		Object value = null;
		if (type.equals("string")) {
			clazz = String.class;
			value = attributes.get("value");
		} else if (type.equals("real")) {
			clazz = Double.class;
			value = Double.parseDouble(attributes.get("value"));
		} else if (type.equals("integer")) {
			clazz = Integer.class;
			value = Integer.parseInt(attributes.get("value"));
		}

		String name = attributes.get("name").replaceAll(" ", "_");
		AttributeName an = graph.getMetaData().getAttributeName(name);
		if (an == null) {
			an = graph.getMetaData().getFactory()
					.createAttributeName(name, clazz);
		} else if (!an.getDataType().equals(clazz)) {
			throw new ParsingFailedException(
					"inconsistent use of attribute names");
		}
		if (entity instanceof ONDEXConcept) {
			((ONDEXConcept) entity).createAttribute(an, value, false);
		} else if (entity instanceof ONDEXRelation) {
			((ONDEXRelation) entity).createAttribute(an, value, false);
		}
	}

	private Map<String, String> getAttributes() {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < r.getAttributeCount(); i++) {
			map.put(r.getAttributeLocalName(i), r.getAttributeValue(i));
		}
		return map;
	}

	private ONDEXConcept createConcept(String nodelabel, String nodeId) {
		ONDEXConcept concept = graph.getFactory().createConcept(nodeId,
				dataSource, cc, et);
		concept.createConceptName(nodelabel, true);
		nodeID2Concept.put(nodeId, concept);
		return concept;
	}

}
