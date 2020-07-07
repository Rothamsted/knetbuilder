package net.sourceforge.ondex.ovtk2.util.xml;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;

import edu.uci.ics.jung.visualization.annotations.Annotation;
import edu.uci.ics.jung.visualization.annotations.Annotation.Layer;

/**
 * Turn XML into JUNG Annotations.
 * 
 * @author taubertj
 * 
 */
public class AnnotationXMLReader {

	/**
	 * Reads the XML stream and de-serialises annotations into the given set.
	 * 
	 * @param xmlr
	 * @param annos
	 *            result set of annotations
	 * @throws XMLStreamException
	 */
	public static void read(XMLStreamReader2 xmlr, Set<Annotation> annos) throws XMLStreamException {

		// iterate over XML
		while (xmlr.hasNext()) {

			// get next event
			int event = xmlr.next();

			// check for start of new element
			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = xmlr.getLocalName();

				// find next annotation
				if (element.equals(AnnotationXMLWriter.ANNOTATION)) {

					// decide on type
					String type = xmlr.getAttributeValue(0);

					// parse text
					if (type.equals(AnnotationXMLWriter.TEXT)) {
						readText(xmlr, annos);
					}

					// parse shape
					else if (type.equals(AnnotationXMLWriter.SHAPE)) {
						readShape(xmlr, annos);
					}

					else
						throw new XMLStreamException("Unknown annotation type found: " + type);
				}
			}
		}
	};

	/**
	 * Read fill from XML.
	 * 
	 * @param xmlr
	 * @return
	 * @throws XMLStreamException
	 */
	private static boolean readFill(XMLStreamReader2 xmlr) throws XMLStreamException {

		// fill tag
		xmlr.nextTag();
		boolean fill = xmlr.getElementAsBoolean();

		return fill;
	}

	/**
	 * Read layer from XML.
	 * 
	 * @param xmlr
	 * @return
	 * @throws XMLStreamException
	 */
	private static Layer readLayer(XMLStreamReader2 xmlr) throws XMLStreamException {

		// layer tag
		xmlr.nextTag();
		Layer layer = Annotation.Layer.valueOf(xmlr.getElementText());

		return layer;
	}

	/**
	 * Read paint from XML. Only color supported currently.
	 * 
	 * @param xmlr
	 * @return
	 * @throws XMLStreamException
	 */
	private static Paint readPaint(XMLStreamReader2 xmlr) throws XMLStreamException {

		// paint and color tag
		xmlr.nextTag();
		xmlr.nextTag();

		// red tag
		xmlr.nextTag();
		int r = xmlr.getElementAsInt();

		// green tag
		xmlr.nextTag();
		int g = xmlr.getElementAsInt();

		// blue tag
		xmlr.nextTag();
		int b = xmlr.getElementAsInt();

		// alpha tag
		xmlr.nextTag();
		int alpha = xmlr.getElementAsInt();

		// compose new Color
		Color paint = new Color(r, g, b, alpha);
		xmlr.nextTag(); // close color
		xmlr.nextTag(); // close paint

		return paint;
	}

	/**
	 * Read location as a Point2D from XML.
	 * 
	 * @param xmlr
	 * @return
	 * @throws XMLStreamException
	 */
	private static Point2D readLocation(XMLStreamReader2 xmlr) throws XMLStreamException {
		// location tag
		xmlr.nextTag();

		// x coordinate
		xmlr.nextTag();
		double x = xmlr.getElementAsDouble();

		// y coordinate
		xmlr.nextTag();
		double y = xmlr.getElementAsDouble();

		// compose new Point2D
		Point2D location = new Point2D.Double(x, y);
		xmlr.nextTag();

		return location;
	}

	/**
	 * Read text annotation from XML.
	 * 
	 * @param xmlr
	 * @param annos
	 * @throws XMLStreamException
	 */
	protected static void readText(XMLStreamReader2 xmlr, Set<Annotation> annos) throws XMLStreamException {

		// text tag
		xmlr.nextTag();
		String text = xmlr.getElementText();

		// get layer
		Layer layer = readLayer(xmlr);

		// get fill
		boolean fill = readFill(xmlr);

		// get paint
		Paint paint = readPaint(xmlr);

		// get location
		Point2D location = readLocation(xmlr);

		// compose new annotation and add to result list
		Annotation<String> anno = new Annotation<String>(text, layer, paint, fill, location);
		annos.add(anno);
	}

	/**
	 * Read shape annotation from XML.
	 * 
	 * @param xmlr
	 * @param annos
	 * @throws XMLStreamException
	 */
	protected static void readShape(XMLStreamReader2 xmlr, Set<Annotation> annos) throws XMLStreamException {

		xmlr.nextTag(); // shape tag
		xmlr.nextTag(); // path tag

		// rule tag
		xmlr.nextTag();
		int rule = Integer.parseInt(xmlr.getElementText());

		// construct a new shape from XML
		Path2D shape = new Path2D.Double(rule);

		xmlr.nextTag(); // coordinates

		// iterate over coordinates
		while (xmlr.hasNext()) {

			// get next event
			int event = xmlr.next();

			// check for start of new element
			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = xmlr.getLocalName();

				// find next coords
				if (element.equals(AnnotationXMLWriter.COORDS)) {

					// type tag
					xmlr.nextTag();
					int type = xmlr.getElementAsInt();

					// array tag
					xmlr.nextTag();
					double[] array = new double[6];

					// convert element text to double array
					String[] split = xmlr.getElementText().trim().split(" ");
					for (int i = 0; i < split.length; i++) {
						array[i] = Double.parseDouble(split[i]);
					}

					// add result to current path
					switch (type) {
					case PathIterator.SEG_CLOSE:
						shape.closePath();
						break;
					case PathIterator.SEG_CUBICTO:
						shape.curveTo(array[0], array[1], array[2], array[3], array[4], array[5]);
						break;
					case PathIterator.SEG_LINETO:
						shape.lineTo(array[0], array[1]);
						break;
					case PathIterator.SEG_MOVETO:
						shape.moveTo(array[0], array[1]);
						break;
					case PathIterator.SEG_QUADTO:
						shape.quadTo(array[0], array[1], array[2], array[3]);
						break;
					default:
						throw new XMLStreamException("Unknown PathIterator type.");
					}
				}
			}

			else if (event == XMLStreamConstants.END_ELEMENT) {
				String element = xmlr.getLocalName();

				// find end coordinates
				if (element.equals(AnnotationXMLWriter.COORDINATES)) {
					xmlr.next();
					xmlr.next();
					break;
				}
			}
		}

		// get layer
		Layer layer = readLayer(xmlr);

		// get fill
		boolean fill = readFill(xmlr);

		// get paint
		Paint paint = readPaint(xmlr);

		// get location
		Point2D location = readLocation(xmlr);

		// compose new annotation and add to result list
		Annotation<Shape> anno = new Annotation<Shape>(shape, layer, paint, fill, location);
		annos.add(anno);

	}

}
