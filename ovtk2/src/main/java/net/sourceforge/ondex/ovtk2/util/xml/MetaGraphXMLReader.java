package net.sourceforge.ondex.ovtk2.util.xml;

import java.awt.geom.Point2D;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2MetaGraph;

import org.codehaus.stax2.XMLStreamReader2;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Translates XML settings for MetaGraph appearance.
 * 
 * @author taubertj
 * 
 */
public class MetaGraphXMLReader {

	/**
	 * Reads the XML stream, de-serialises settings and sets appearance.
	 * 
	 * @param xmlr
	 * @param meta
	 *            MetaGraph to get modified
	 * @throws XMLStreamException
	 */
	public static void read(XMLStreamReader2 xmlr, OVTK2MetaGraph meta) throws XMLStreamException {

		// layout to modify
		Layout<ONDEXMetaConcept, ONDEXMetaRelation> layout = meta.getViewer().getMetaGraphPanel().getVisualizationViewer().getGraphLayout();

		// iterate over XML
		while (xmlr.hasNext()) {

			// get next event
			int event = xmlr.next();

			// check for start of new element
			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = xmlr.getLocalName();

				// layout position
				if (element.equals(MetaGraphXMLWriter.POSITION)) {
					String id = xmlr.getAttributeValue(0);
					xmlr.nextTag();
					double x = xmlr.getElementAsDouble();
					xmlr.nextTag();
					double y = xmlr.getElementAsDouble();
					Point2D p = new Point2D.Double(x, y);
					for (ONDEXMetaConcept mc : layout.getGraph().getVertices()) {
						if (mc.getConceptClass().getId().equals(id)) {
							layout.setLocation(mc, p);
							break;
						}
					}
				}

				// node labels visible
				else if (element.equals(MetaGraphXMLWriter.NODELABELS)) {
					meta.showNodeLabels(xmlr.getElementAsBoolean());
				}

				// edge labels visible
				else if (element.equals(MetaGraphXMLWriter.EDGELABELS)) {
					meta.showEdgeLabels(xmlr.getElementAsBoolean());
				}

				// node size
				else if (element.equals(MetaGraphXMLWriter.NODESIZE)) {
					meta.setNodeSize(xmlr.getElementAsInt());
				}

				// edge size
				else if (element.equals(MetaGraphXMLWriter.EDGESIZE)) {
					meta.setEdgeSize(xmlr.getElementAsInt());
				}

				// font size
				else if (element.equals(MetaGraphXMLWriter.FONTSIZE)) {
					meta.setFontSize(xmlr.getElementAsInt());
				}
			}
		}
	};

}
