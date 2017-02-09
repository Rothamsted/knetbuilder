package net.sourceforge.ondex.ovtk2.metagraph;

import java.awt.Shape;
import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;

/**
 * Provides a transformation from a given ONDEXMetaConcept to a Shape.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaConceptShapes implements Transformer<ONDEXMetaConcept, Shape> {

	// current ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// produces the shapes
	private VertexShapeFactory<ONDEXMetaConcept> shapeFactory = null;

	// contains mapping concept class to shape
	private Map<ConceptClass, Shape> shapes = null;

	/**
	 * Initialises the shapes for the nodes in the graph.
	 * 
	 * @param graph
	 *            ONDEXJUNGGraph
	 */
	public ONDEXMetaConceptShapes(ONDEXJUNGGraph graph) {

		this.graph = graph;
		this.shapes = new Hashtable<ConceptClass, Shape>();
		this.shapeFactory = new VertexShapeFactory<ONDEXMetaConcept>(new Transformer<ONDEXMetaConcept, Integer>() {

			@Override
			public Integer transform(ONDEXMetaConcept input) {
				return 20;
			}

		}, new Transformer<ONDEXMetaConcept, Float>() {

			@Override
			public Float transform(ONDEXMetaConcept input) {
				return 1.0f;
			}
		});

		// initialise shapes
		updateAll();
	}

	/**
	 * Sets a new node size used to generate all shapes.
	 * 
	 * @param nodeSize
	 *            new node size
	 */
	public void setSize(final int nodeSize) {
		this.shapeFactory = new VertexShapeFactory<ONDEXMetaConcept>(new Transformer<ONDEXMetaConcept, Integer>() {

			@Override
			public Integer transform(ONDEXMetaConcept input) {
				return nodeSize;
			}

		}, new Transformer<ONDEXMetaConcept, Float>() {

			@Override
			public Float transform(ONDEXMetaConcept input) {
				return 1.0f;
			}
		});
		updateAll();
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 * @return Shape
	 */
	public Shape transform(ONDEXMetaConcept node) {
		updateShape(node);
		return shapes.get(node.id);
	}

	/**
	 * Update all shapes from the graph.
	 * 
	 */
	public void updateAll() {
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			// update with dummy node
			updateShape(new ONDEXMetaConcept(graph, cc));
		}
	}

	/**
	 * Update the shape of a given node.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 */
	public void updateShape(ONDEXMetaConcept node) {
		int shape = Config.getShapeForConceptClass((ConceptClass) node.id);

		switch (shape) {
		case 0:
			updateShape(node, shapeFactory.getEllipse(node));
			break;
		case 1:
			updateShape(node, shapeFactory.getRectangle(node));
			break;
		case 2:
			updateShape(node, shapeFactory.getRoundRectangle(node));
			break;
		case 3:
			updateShape(node, shapeFactory.getRegularPolygon(node, 3));
			break;
		case 4:
			updateShape(node, shapeFactory.getRegularPolygon(node, 5));
			break;
		case 5:
			updateShape(node, shapeFactory.getRegularPolygon(node, 8));
			break;
		case 6:
			updateShape(node, shapeFactory.getRegularStar(node, 5));
			break;
		case 7:
			updateShape(node, shapeFactory.getRegularStar(node, 7));
			break;
		case 8:
			updateShape(node, shapeFactory.getRegularStar(node, 9));
			break;
		default:
			updateShape(node, shapeFactory.getEllipse(node));
		}
	}

	/**
	 * Update the shape of a given node with a given shape.
	 * 
	 * @param node
	 *            ONDEXMetaConcept
	 * @param shape
	 *            Shape
	 */
	public void updateShape(ONDEXMetaConcept node, Shape shape) {
		shapes.put((ConceptClass) node.id, shape);
	}
}
