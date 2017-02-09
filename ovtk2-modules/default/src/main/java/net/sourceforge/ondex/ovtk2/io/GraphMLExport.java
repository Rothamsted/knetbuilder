package net.sourceforge.ondex.ovtk2.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * This class represents a GraphML export using Stax2 XML writer.
 * 
 * @author taubertj
 * 
 */
public class GraphMLExport implements OVTK2IO {

	private static final String GRAPHML_SCHEMA = "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd";

	private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns/graphml";

	private static final String Y_NS = "http://www.yworks.com/xml/graphml";

	private static final String XML_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	// count up nodes and edges
	private int lastNode, lastEdge;

	// assign nodes a unique id
	private Hashtable<ONDEXConcept, String> nodes = new Hashtable<ONDEXConcept, String>();

	// assign edges a unique id
	private Hashtable<ONDEXRelation, String> edges = new Hashtable<ONDEXRelation, String>();

	// kinds of graphs
	private boolean directed, undirected;

	// current visualization
	private VisualizationViewer<ONDEXConcept, ONDEXRelation> viewer = null;

	public GraphMLExport() {
	}

	/**
	 * Starts the XML document.
	 * 
	 * @param graph
	 *            Graph<V, E>
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeDocument(Graph<ONDEXConcept, ONDEXRelation> graph,
			XMLStreamWriter2 xmlw) throws XMLStreamException {

		xmlw.writeStartDocument("ISO-8859-1", "1.0");
		writeGraphml(graph, xmlw);
		xmlw.writeEndDocument();
	}

	/**
	 * Writes graphml tag and namespaces.
	 * 
	 * @param graph
	 *            Graph<V, E>
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeGraphml(Graph<ONDEXConcept, ONDEXRelation> graph,
			XMLStreamWriter2 xmlw) throws XMLStreamException {

		xmlw.writeStartElement("graphml");
		xmlw.setPrefix("graphml", GRAPHML_NS);
		xmlw.writeNamespace("", GRAPHML_NS);
		xmlw.writeNamespace("xsi", XML_XSI);
		xmlw.writeNamespace("schemaLocation", GRAPHML_SCHEMA);
		xmlw.writeNamespace("y", Y_NS);

		writeKey(xmlw);

		writeGraph(graph, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes key tag defining visual elements of nodes and edges.
	 * 
	 * @param xmlw
	 *            XMLStreamWriter2
	 */
	private void writeKey(XMLStreamWriter2 xmlw) throws XMLStreamException {

		// <key for="node" id="d0" yfiles.type="nodegraphics"/>
		xmlw.writeStartElement("key");
		xmlw.writeAttribute("for", "node");
		xmlw.writeAttribute("id", "d0");
		xmlw.writeAttribute("yfiles.type", "nodegraphics");
		xmlw.writeEndElement();

		// <key attr.name="description" attr.type="string" for="node" id="d1"/>
		xmlw.writeStartElement("key");
		xmlw.writeAttribute("attr.name", "description");
		xmlw.writeAttribute("attr.type", "string");
		xmlw.writeAttribute("for", "node");
		xmlw.writeAttribute("id", "d1");
		xmlw.writeEndElement();

		// <key for="edge" id="d2" yfiles.type="edgegraphics"/>
		xmlw.writeStartElement("key");
		xmlw.writeAttribute("for", "edge");
		xmlw.writeAttribute("id", "d2");
		xmlw.writeAttribute("yfiles.type", "edgegraphics");
		xmlw.writeEndElement();

		// <key attr.name="description" attr.type="string" for="edge" id="d3"/>
		xmlw.writeStartElement("key");
		xmlw.writeAttribute("attr.name", "description");
		xmlw.writeAttribute("attr.type", "string");
		xmlw.writeAttribute("for", "edge");
		xmlw.writeAttribute("id", "d3");
		xmlw.writeEndElement();

		// <key for="graphml" id="d4" yfiles.type="resources"/>
		xmlw.writeStartElement("key");
		xmlw.writeAttribute("for", "graphml");
		xmlw.writeAttribute("id", "d4");
		xmlw.writeAttribute("yfiles.type", "resources");
		xmlw.writeEndElement();

	}

	/**
	 * Writes graph tag and attributes.
	 * 
	 * @param graph
	 *            Graph<V, E>
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeGraph(Graph<ONDEXConcept, ONDEXRelation> graph,
			XMLStreamWriter2 xmlw) throws XMLStreamException {

		xmlw.writeStartElement("graph");

		directed = graph instanceof DirectedGraph;
		undirected = graph instanceof UndirectedGraph;

		if (directed)
			xmlw.writeAttribute("edgedefault", "directed");
		else if (undirected)
			xmlw.writeAttribute("edgedefault", "undirected");
		else // default for mixed graphs
		{
			directed = true;
			xmlw.writeAttribute("edgedefault", "directed");
		}

		xmlw.writeAttribute("id", "G");
		xmlw.writeAttribute("parse.edges", "1");
		xmlw.writeAttribute("parse.nodes", "1");
		xmlw.writeAttribute("parse.order", "free");

		// write all vertices
		Iterator<ONDEXConcept> it_v = graph.getVertices().iterator();
		while (it_v.hasNext()) {
			writeNode(it_v.next(), xmlw);
		}

		// write all edges
		Iterator<ONDEXRelation> it_e = graph.getEdges().iterator();
		while (it_e.hasNext()) {
			writeEdge(graph, it_e.next(), xmlw);
		}

		xmlw.writeEndElement();

		// <data key="d4"><y:Resources/>
		xmlw.writeStartElement("data");
		xmlw.writeAttribute("key", "d4");
		xmlw.writeStartElement("y:Resources");
		xmlw.writeEndElement();
		xmlw.writeEndElement();
	}

	/**
	 * Writes a node and attributes.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeNode(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		nodes.put(vertex, "n" + lastNode);
		lastNode++;

		xmlw.writeStartElement("node");
		xmlw.writeAttribute("id", nodes.get(vertex).toString());

		// writes graphical attributes of node
		writeNodeData(vertex, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes an edge and attributes.
	 * 
	 * @param graph
	 *            Graph<V, E>
	 * @param edges
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeEdge(Graph<ONDEXConcept, ONDEXRelation> graph, ONDEXRelation edge,
			XMLStreamWriter2 xmlw) throws XMLStreamException {

		edges.put(edge, "e" + lastEdge);
		lastEdge++;

		// get the two endpoints
		ONDEXConcept source = graph.getSource(edge);
		ONDEXConcept dest = graph.getDest(edge);

		xmlw.writeStartElement("edge");
		xmlw.writeAttribute("id", edges.get(edge).toString());

		// compare real edge type with default, needed for mixed graphs
		EdgeType type = graph.getEdgeType(edge);
		if (type.equals(EdgeType.DIRECTED) && !directed)
			xmlw.writeAttribute("directed", "true");
		else if (type.equals(EdgeType.UNDIRECTED) && !undirected)
			xmlw.writeAttribute("directed", "false");

		xmlw.writeAttribute("source", nodes.get(source).toString());
		xmlw.writeAttribute("target", nodes.get(dest).toString());

		// writes graphical attributes of edge
		writeEdgeData(graph, edge, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes data tag including graphical attributes for a given node.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 */
	private void writeNodeData(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		xmlw.writeStartElement("data");
		xmlw.writeAttribute("key", "d0");

		// start shape node
		writeShapeNode(vertex, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes data tag including graphical attributes for a given edge.
	 * 
	 * @param graph
	 *            Graph<V,E>
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 */
	private void writeEdgeData(Graph<ONDEXConcept, ONDEXRelation> graph,
			ONDEXRelation edge, XMLStreamWriter2 xmlw) throws XMLStreamException {

		xmlw.writeStartElement("data");
		xmlw.writeAttribute("key", "d2");

		// start polyline edge
		writePolyLineEdge(graph, edge, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes y:ShapeNode tag and proceeds further.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeShapeNode(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		xmlw.writeStartElement("y:ShapeNode");

		// write node position and size
		writeGeometry(vertex, xmlw);

		// write node color
		writeFill(vertex, xmlw);

		// write node border color
		writeBorderStyle(vertex, xmlw);

		// write node label information
		writeNodeLabel(vertex, xmlw);

		// write node shape
		writeShape(vertex, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Write y:PolyLineEdge tag and proceeds further.
	 * 
	 * @param graph
	 *            Graph<V,E>
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writePolyLineEdge(Graph<ONDEXConcept, ONDEXRelation> graph,
			ONDEXRelation edge, XMLStreamWriter2 xmlw) throws XMLStreamException {

		xmlw.writeStartElement("y:PolyLineEdge");

		// write path of edge
		writePath(graph, edge, xmlw);

		// write line style and color
		writeLineStyle(edge, xmlw);

		// write arrow configuration
		writeArrows(graph, edge, xmlw);

		// write edge label information
		writeEdgeLabel(edge, xmlw);

		// write bend style
		writeBendStyle(edge, xmlw);

		xmlw.writeEndElement();
	}

	/**
	 * Writes y:Geometry tag with node positions.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeGeometry(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// get coordinates of node
		Point2D coords = viewer.getGraphLayout().transform(vertex);

		// get shape of node
		Shape shape = viewer.getRenderContext().getVertexShapeTransformer()
				.transform(vertex);
		Rectangle bounds = shape.getBounds();

		xmlw.writeStartElement("y:Geometry");
		xmlw.writeAttribute("x", String.valueOf(coords.getX()));
		xmlw.writeAttribute("y", String.valueOf(coords.getY()));
		xmlw.writeAttribute("width", String.valueOf(bounds.getWidth()));
		xmlw.writeAttribute("height", String.valueOf(bounds.getHeight()));
		xmlw.writeEndElement();
	}

	/**
	 * Writes y:Fill tag with node fill color.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeFill(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// translate color to hex
		Paint p = viewer.getRenderContext().getVertexFillPaintTransformer()
				.transform(vertex);
		Color c = Color.BLACK;
		if (p instanceof Color)
			c = (Color) p;
		String ccode = (new Formatter()).format("#%1$02X%2$02X%3$02X",
				c.getRed(), c.getGreen(), c.getBlue()).toString();

		xmlw.writeStartElement("y:Fill");
		xmlw.writeAttribute("color", ccode);
		xmlw.writeAttribute("transparent", "false");
		xmlw.writeEndElement();
	}

	/**
	 * Writes y:BorderStyle tag with node draw color.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeBorderStyle(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// translate color to hex
		Color c = (Color) viewer.getRenderContext()
				.getVertexDrawPaintTransformer().transform(vertex);
		String ccode = (new Formatter()).format("#%1$02X%2$02X%3$02X",
				c.getRed(), c.getGreen(), c.getBlue()).toString();

		xmlw.writeStartElement("y:BorderStyle");
		xmlw.writeAttribute("type", "line");
		xmlw.writeAttribute("width", "1.0");
		xmlw.writeAttribute("color", ccode);
		xmlw.writeEndElement();
	}

	/**
	 * Writes y:NodeLabel tag with node label information.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeNodeLabel(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// current label text and font
		String text = viewer.getRenderContext().getVertexLabelTransformer()
				.transform(vertex);
		Font font = viewer.getRenderContext().getVertexFontTransformer()
				.transform(vertex);

		// get position of label
		Graphics2D graphics = (Graphics2D) viewer.getGraphics();
		Rectangle2D bounds = null;
		if (text != null) {
			bounds = font
					.getStringBounds(text, graphics.getFontRenderContext());
		} else {
			bounds = new Rectangle2D.Float();
		}

		// convert font style to string
		String style = null;
		switch (font.getStyle()) {
		case 0:
			style = "plain";
			break;
		case 1:
			style = "bold";
			break;
		case 2:
			style = "italic";
			break;
		}

		// text color
		Color c = (Color) viewer.getRenderContext()
				.getVertexDrawPaintTransformer().transform(vertex);
		String ccode = (new Formatter()).format("#%1$02X%2$02X%3$02X",
				c.getRed(), c.getGreen(), c.getBlue()).toString();

		xmlw.writeStartElement("y:NodeLabel");
		xmlw.writeAttribute("x", String.valueOf(bounds.getWidth() / -2));
		xmlw.writeAttribute("y", String.valueOf(bounds.getHeight() * -1.1));
		xmlw.writeAttribute("width", String.valueOf(bounds.getWidth()));
		xmlw.writeAttribute("height", String.valueOf(bounds.getHeight()));
		xmlw.writeAttribute("visible", "true");
		xmlw.writeAttribute("alignment", "center");
		xmlw.writeAttribute("fontFamily", font.getFamily());
		xmlw.writeAttribute("fontSize", String.valueOf(font.getSize()));
		xmlw.writeAttribute("fontStyle", style);
		xmlw.writeAttribute("textColor", ccode);
		// modelName="corners" modelPosition="n" autoSizePolicy="content"
		xmlw.writeAttribute("modelName", "corners");
		xmlw.writeAttribute("modelPosition", "n");
		xmlw.writeAttribute("autoSizePolicy", "content");
		if (text != null)
			xmlw.writeCharacters(text);
		xmlw.writeEndElement();
	}

	/**
	 * Writes y:Shape tag with node shape.
	 * 
	 * @param vertex
	 *            V
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeShape(ONDEXConcept vertex, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// convert shape to yFiles shape name
		String name = "rectangle";
		Shape shape = viewer.getRenderContext().getVertexShapeTransformer()
				.transform(vertex);
		if (shape instanceof Rectangle) {
			name = "rectangle";
		} else if (shape instanceof RoundRectangle2D) {
			name = "roundrectangle";
		} else if (shape instanceof Ellipse2D) {
			name = "ellipse";
		} else {
			PathIterator it = shape.getPathIterator(new AffineTransform());
			int count = 0;
			while (!it.isDone()) {
				it.next();
				count++;
			}
			if (count == 6) {
				name = "rectangle";
			} else if (count == 8) {
				name = "hexagon";
			} else {
				name = "octagon";
			}
		}

		xmlw.writeStartElement("y:Shape");
		xmlw.writeAttribute("type", name);
		xmlw.writeEndElement();
	}

	/**
	 * Write y:Path tag with attributes.
	 * 
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writePath(Graph<ONDEXConcept, ONDEXRelation> graph, ONDEXRelation edge,
			XMLStreamWriter2 xmlw) throws XMLStreamException {
		// <y:Path sx="0.0" sy="0.0" tx="0.0" ty="0.0">
		xmlw.writeStartElement("y:Path");
		xmlw.writeAttribute("sx", "0.0");
		xmlw.writeAttribute("sy", "0.0");
		xmlw.writeAttribute("tx", "0.0");
		xmlw.writeAttribute("ty", "0.0");

		// get two endpoints
		Point2D source = viewer.getGraphLayout().transform(
				graph.getSource(edge));
		Point2D dest = viewer.getGraphLayout().transform(graph.getDest(edge));
		xmlw.writeStartElement("y:Point");
		xmlw.writeAttribute("x", String.valueOf(source.getX()));
		xmlw.writeAttribute("y", String.valueOf(source.getY()));
		xmlw.writeEndElement();
		xmlw.writeStartElement("y:Point");
		xmlw.writeAttribute("x", String.valueOf(dest.getX()));
		xmlw.writeAttribute("y", String.valueOf(dest.getY()));
		xmlw.writeEndElement();

		xmlw.writeEndElement();
	}

	/**
	 * Write y:LineStyle tag with attributes.
	 * 
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeLineStyle(ONDEXRelation edge, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		Paint p = viewer.getRenderContext().getEdgeDrawPaintTransformer().transform(edge);
		Color c = Color.BLACK;
		if (p instanceof Color)
			c = (Color) p;
		String ccode = (new Formatter()).format("#%1$02X%2$02X%3$02X",
				c.getRed(), c.getGreen(), c.getBlue()).toString();

		xmlw.writeStartElement("y:LineStyle");
		xmlw.writeAttribute("type", "line");
		xmlw.writeAttribute("width", "1.0");
		xmlw.writeAttribute("color", ccode);
		xmlw.writeEndElement();
	}

	/**
	 * Write y:Arrows tag with attributes.
	 * 
	 * @param graph
	 *            Graph<V,E>
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeArrows(Graph<ONDEXConcept, ONDEXRelation> graph, ONDEXRelation edge,
			XMLStreamWriter2 xmlw) throws XMLStreamException {

		boolean arrow = viewer.getRenderContext().getEdgeArrowPredicate()
				.evaluate(Context.getInstance(graph, edge));

		xmlw.writeStartElement("y:Arrows");
		xmlw.writeAttribute("source", "none");
		if (arrow)
			xmlw.writeAttribute("target", "standard");
		else
			xmlw.writeAttribute("target", "none");
		xmlw.writeEndElement();
	}

	/**
	 * Writes y:EdgeLabel tag with edge label information.
	 * 
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeEdgeLabel(ONDEXRelation edge, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		// current label text and font
		String text = viewer.getRenderContext().getEdgeLabelTransformer()
				.transform(edge);
		Font font = viewer.getRenderContext().getEdgeFontTransformer()
				.transform(edge);

		// get position of label
		Graphics2D graphics = (Graphics2D) viewer.getGraphics();
		Rectangle2D bounds = null;
		if (text != null) {
			bounds = font
					.getStringBounds(text, graphics.getFontRenderContext());
		} else {
			bounds = new Rectangle2D.Float();
		}

		// convert font style to string
		String style = null;
		switch (font.getStyle()) {
		case 0:
			style = "plain";
			break;
		case 1:
			style = "bold";
			break;
		case 2:
			style = "italic";
			break;
		}

		// text color
		Paint p = viewer.getRenderContext().getEdgeDrawPaintTransformer().transform(edge);
		Color c = Color.BLACK;
		if (p instanceof Color)
			c = (Color) p;
		String ccode = (new Formatter()).format("#%1$02X%2$02X%3$02X",
				c.getRed(), c.getGreen(), c.getBlue()).toString();

		xmlw.writeStartElement("y:EdgeLabel");
		xmlw.writeAttribute("x", String.valueOf(bounds.getWidth() / 2));
		xmlw.writeAttribute("y", String.valueOf(bounds.getHeight()));
		xmlw.writeAttribute("width", String.valueOf(bounds.getWidth()));
		xmlw.writeAttribute("height", String.valueOf(bounds.getHeight()));
		xmlw.writeAttribute("visible", "true");
		xmlw.writeAttribute("alignment", "center");
		xmlw.writeAttribute("fontFamily", font.getFamily());
		xmlw.writeAttribute("fontSize", String.valueOf(font.getSize()));
		xmlw.writeAttribute("fontStyle", style);
		xmlw.writeAttribute("textColor", ccode);
		// modelName="six_pos" modelPosition="tail"
		// preferredPlacement="anywhere" distance="2.0" ratio="0.5"
		xmlw.writeAttribute("modelName", "six_pos");
		xmlw.writeAttribute("modelPosition", "tail");
		xmlw.writeAttribute("preferredPlacement", "anywhere");
		xmlw.writeAttribute("distance", "2.0");
		xmlw.writeAttribute("ratio", "0.5");
		if (text != null)
			xmlw.writeCharacters(text);
		xmlw.writeEndElement();
	}

	/**
	 * Write y:BendStyle tag with attributes.
	 * 
	 * @param edge
	 *            E
	 * @param xmlw
	 *            XMLStreamWriter2
	 * @throws XMLStreamException
	 */
	private void writeBendStyle(ONDEXRelation edge, XMLStreamWriter2 xmlw)
			throws XMLStreamException {

		xmlw.writeStartElement("y:BendStyle");
		xmlw.writeAttribute("smoothed", "false");
		xmlw.writeEndElement();
	}

	@Override
	public void start(File file) {
		// xml output
		XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2
				.newInstance();
		xmlOutput.configureForSpeed();
		xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

		try {

			// output file writer
			OutputStream outStream = new FileOutputStream(file);

			if (outStream != null) {

				XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput
						.createXMLStreamWriter(outStream, "ISO-8859-1");

				writeDocument(viewer.getGraphLayout().getGraph(),
						xmlWriteStream);

				xmlWriteStream.flush();
				xmlWriteStream.close();

				outStream.flush();
				outStream.close();
			}

		} catch (Exception e) {
			ErrorDialog.show(e);
		}
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		// hack to get to the visualisation viewer
		this.viewer = desktop.getDesktopResources().getSelectedViewer()
				.getVisualizationViewer();
	}

	@Override
	public String getExt() {
		return "graphml";
	}

	@Override
	public boolean isImport() {
		return false;
	}

}
