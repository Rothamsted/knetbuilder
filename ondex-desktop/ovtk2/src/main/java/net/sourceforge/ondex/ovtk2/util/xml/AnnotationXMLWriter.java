package net.sourceforge.ondex.ovtk2.util.xml;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import edu.uci.ics.jung.visualization.annotations.Annotation;
import edu.uci.ics.jung.visualization.annotations.Annotation.Layer;

/**
 * Turns a JUNG Annotation to XML.
 * 
 * @author taubertj
 * 
 */
public class AnnotationXMLWriter {

	public static final String ANNOTATIONS = "annotations";

	public static final String ANNOTATION = "annotation";

	public static final String TEXT = "text";

	public static final String TYPE = "type";

	public static final String LAYER = "layer";

	public static final String FILL = "fill";

	public static final String PAINT = "paint";

	public static final String SHAPE = "shape";

	public static final String LOCATION = "location";

	public static final String X = "x";

	public static final String Y = "y";

	public static final String COLOR = "color";

	public static final String R = "r";

	public static final String G = "g";

	public static final String B = "b";

	public static final String ALPHA = "alpha";

	public static final String PATH = "path";

	public static final String RULE = "rule";

	public static final String COORDINATES = "coordinates";

	public static final String COORDS = "coords";

	public static final String ARRAY = "array";

	/**
	 * Writes a Set of given annotations to XML
	 * 
	 * @param xmlw
	 * @param annos
	 * @throws XMLStreamException
	 */
	public static void write(XMLStreamWriter2 xmlw, Set<Annotation> annos) throws XMLStreamException {

		xmlw.writeStartElement(ANNOTATIONS);

		for (Annotation anno : annos) {
			write(xmlw, anno);
		}

		xmlw.writeEndElement();
	}

	/**
	 * Writes a given Annotation to an XMLStream
	 * 
	 * @param xmlw
	 * @param anno
	 * @throws XMLStreamException
	 */
	public static void write(XMLStreamWriter2 xmlw, Annotation<?> anno) throws XMLStreamException {

		xmlw.writeStartElement(ANNOTATION);

		// first case is a text annotation
		if (anno.getAnnotation() instanceof String) {
			xmlw.writeAttribute(TYPE, "text");

			// get text from annotation
			xmlw.writeStartElement(TEXT);
			String text = (String) anno.getAnnotation();
			xmlw.writeCharacters(text);
			xmlw.writeEndElement();

			// write annotation layer
			write(xmlw, anno.getLayer());

			// write fill value
			write(xmlw, anno.isFill());

			// write paint
			write(xmlw, anno.getPaint());

			// write location
			write(xmlw, anno.getLocation());
		}

		// second case is a JAVA Shape
		else if (anno.getAnnotation() instanceof Shape) {
			xmlw.writeAttribute(TYPE, "shape");

			// write shape
			Shape s = (Shape) anno.getAnnotation();
			xmlw.writeStartElement(SHAPE);

			// begin path iteration
			PathIterator pi = s.getPathIterator(null);
			xmlw.writeStartElement(PATH);

			// write winding rule
			int rule = pi.getWindingRule();
			xmlw.writeStartElement(RULE);
			xmlw.writeInt(rule);
			xmlw.writeEndElement();

			// iterate over all coordinates
			xmlw.writeStartElement(COORDINATES);
			while (!pi.isDone()) {

				// write each coordinates
				xmlw.writeStartElement(COORDS);
				double[] coords = new double[6];

				// write type of coordinates
				int type = pi.currentSegment(coords);
				xmlw.writeStartElement(TYPE);
				xmlw.writeInt(type);
				xmlw.writeEndElement();

				// write point array
				xmlw.writeStartElement(ARRAY);
				xmlw.writeDoubleArray(coords, 0, 6);
				xmlw.writeEndElement();

				xmlw.writeEndElement();

				// move to next element
				pi.next();
			}

			// close all nested elements
			xmlw.writeEndElement();
			xmlw.writeEndElement();
			xmlw.writeEndElement();

			// write annotation layer
			write(xmlw, anno.getLayer());

			// write fill value
			write(xmlw, anno.isFill());

			// write paint
			write(xmlw, anno.getPaint());

			// write location
			write(xmlw, anno.getLocation());
		}

		xmlw.writeEndElement();
	}

	/**
	 * Writes a Annotation Location to an XMLStream
	 * 
	 * @param xmlw
	 * @param location
	 * @throws XMLStreamException
	 */
	protected static void write(XMLStreamWriter2 xmlw, Point2D location) throws XMLStreamException {

		xmlw.writeStartElement(LOCATION);

		// write X coordinate
		xmlw.writeStartElement(X);
		xmlw.writeDouble(location.getX());
		xmlw.writeEndElement();

		// write Y coordinate
		xmlw.writeStartElement(Y);
		xmlw.writeDouble(location.getY());
		xmlw.writeEndElement();

		xmlw.writeEndElement();
	}

	/**
	 * Writes a Annotation Paint to an XMLStream
	 * 
	 * @param xmlw
	 * @param paint
	 * @throws XMLStreamException
	 */
	protected static void write(XMLStreamWriter2 xmlw, Paint paint) throws XMLStreamException {

		xmlw.writeStartElement(PAINT);

		// only color serialisation support right now
		if (paint instanceof Color) {

			// write RGB of color
			Color c = (Color) paint;
			xmlw.writeStartElement(COLOR);

			// red
			xmlw.writeStartElement(R);
			xmlw.writeInt(c.getRed());
			xmlw.writeEndElement();

			// green
			xmlw.writeStartElement(G);
			xmlw.writeInt(c.getGreen());
			xmlw.writeEndElement();

			// blue
			xmlw.writeStartElement(B);
			xmlw.writeInt(c.getBlue());
			xmlw.writeEndElement();

			// alpha
			xmlw.writeStartElement(ALPHA);
			xmlw.writeInt(c.getAlpha());
			xmlw.writeEndElement();

			xmlw.writeEndElement();
		} else
			throw new XMLStreamException("Paint is not instance of Color. Cannot serialize.");

		xmlw.writeEndElement();
	}

	/**
	 * Writes a Annotation layer to an XMLStream
	 * 
	 * @param xmlw
	 * @param layer
	 * @throws XMLStreamException
	 */
	protected static void write(XMLStreamWriter2 xmlw, Layer layer) throws XMLStreamException {

		xmlw.writeStartElement(LAYER);

		xmlw.writeCharacters(layer.name());

		xmlw.writeEndElement();
	}

	/**
	 * Writes a Annotation fill to an XMLStream
	 * 
	 * @param xmlw
	 * @param fill
	 * @throws XMLStreamException
	 */
	protected static void write(XMLStreamWriter2 xmlw, boolean fill) throws XMLStreamException {

		xmlw.writeStartElement(FILL);

		xmlw.writeBoolean(fill);

		xmlw.writeEndElement();
	}

}
