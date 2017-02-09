package net.sourceforge.ondex.ovtk2.graph;

import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.util.VertexShapeFactory;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Provides a transformation from a given ONDEXConcept to a Shape.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXNodeShapes implements Transformer<ONDEXConcept, Shape> {

	/**
	 * Different shape selection strategies
	 * 
	 * @author taubertj
	 * 
	 */
	public enum NodeShapeSelection {
		MANUAL, NONE
	}

	// mapping tables for shapes
	private final static Map<Integer, Shape> id2shape;

	private final static Map<Shape, Integer> shape2id;

	static {
		// vertex shape factory with default sizes/aspect
		VertexShapeFactory<ONDEXConcept> staticShapeFactory = new VertexShapeFactory<ONDEXConcept>(new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				if (input != null)
					return Config.getSizeForConceptClass(input.getOfType());
				return Config.defaultNodeSize;
			}
		}, new Transformer<ONDEXConcept, Float>() {
			@Override
			public Float transform(ONDEXConcept input) {
				return 1.0f;
			}
		});
		// default cases
		final Shape defaultShape = staticShapeFactory.getEllipse(null);
		id2shape = LazyMap.decorate(new HashMap<Integer, Shape>(), new Factory<Shape>() {
			@Override
			public Shape create() {
				return defaultShape;
			}
		});
		shape2id = LazyMap.decorate(new HashMap<Shape, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return 0;
			}
		});
		id2shape.put(0, defaultShape);
		shape2id.put(defaultShape, 0);
		Shape shape = staticShapeFactory.getRectangle(null);
		id2shape.put(1, shape);
		shape2id.put(shape, 1);
		shape = staticShapeFactory.getRoundRectangle(null);
		id2shape.put(2, shape);
		shape2id.put(shape, 2);
		shape = staticShapeFactory.getRegularPolygon(null, 3);
		id2shape.put(3, shape);
		shape2id.put(shape, 3);
		shape = staticShapeFactory.getRegularPolygon(null, 5);
		id2shape.put(4, shape);
		shape2id.put(shape, 4);
		shape = staticShapeFactory.getRegularPolygon(null, 8);
		id2shape.put(5, shape);
		shape2id.put(shape, 5);
		shape = staticShapeFactory.getRegularStar(null, 5);
		id2shape.put(6, shape);
		shape2id.put(shape, 6);
		shape = staticShapeFactory.getRegularStar(null, 7);
		id2shape.put(7, shape);
		shape2id.put(shape, 7);
		shape = staticShapeFactory.getRegularStar(null, 9);
		id2shape.put(8, shape);
		shape2id.put(shape, 8);
		shape = staticShapeFactory.getEllipse(null);
		id2shape.put(9, shape);
		shape2id.put(shape, 9);
	}

	/**
	 * Gets all associated IDs
	 * 
	 * @return all IDs of shapes
	 */
	public static Integer[] getAvailableIds() {
		return id2shape.keySet().toArray(new Integer[0]);
	}

	/**
	 * Gets all possible shapes
	 * 
	 * @return all possible shapes
	 */
	public static Shape[] getAvailableShapes() {
		return shape2id.keySet().toArray(new Shape[0]);
	}

	/**
	 * Returns a id for a given shape.
	 * 
	 * @param shape
	 *            Shape
	 * @return Integer
	 */
	public static Integer getId(Shape shape) {
		return shape2id.get(shape);
	}

	/**
	 * Returns a Shape for a given shape index.
	 * 
	 * @param shape
	 *            Integer
	 * @return Shape
	 */
	public static Shape getShape(Integer shape) {
		return id2shape.get(shape);
	}

	/**
	 * produces the shapes
	 */
	private VertexShapeFactory<ONDEXConcept> shapeFactory;

	/**
	 * contains mapping to shape
	 */
	private final Map<ONDEXConcept, Shape> shapes;

	/**
	 * contains mapping to shape ID
	 */
	private final Map<ONDEXConcept, Integer> shapeIDs;

	/**
	 * selection strategy
	 */
	private NodeShapeSelection strategy = NodeShapeSelection.NONE;

	/**
	 * vertex aspect ratio
	 */
	private Transformer<ONDEXConcept, Float> varf;

	/**
	 * vertex size function
	 */
	private Transformer<ONDEXConcept, Integer> vsf;

	/**
	 * Initialises the shapes for the nodes in the graph.
	 * 
	 */
	public ONDEXNodeShapes() {
		this.shapes = new HashMap<ONDEXConcept, Shape>();
		this.shapeIDs = new HashMap<ONDEXConcept, Integer>();
		this.varf = new Transformer<ONDEXConcept, Float>() {
			@Override
			public Float transform(ONDEXConcept input) {
				return 1.0f;
			}
		};
		this.vsf = new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				return Config.getSizeForConceptClass(input.getOfType());
			}
		};
		this.shapeFactory = new VertexShapeFactory<ONDEXConcept>(vsf, varf);
	}

	/**
	 * Returns current shape selection strategy.
	 * 
	 * @return NodeShapeSelection
	 */
	public NodeShapeSelection getNodeShapeSelection() {
		return this.strategy;
	}

	/**
	 * Returns current aspect ratio transformer
	 * 
	 * @return
	 */
	public Transformer<ONDEXConcept, Float> getNodeAspectRatios() {
		return varf;
	}

	/**
	 * Returns current vertex size transformer
	 * 
	 * @return
	 */
	public Transformer<ONDEXConcept, Integer> getNodeSizes() {
		return vsf;
	}

	/**
	 * Sets node aspect ratio transformer.
	 * 
	 * @param varf
	 *            node aspect ratio transformer
	 */
	public void setNodeAspectRatios(Transformer<ONDEXConcept, Float> varf) {
		this.varf = varf;
		this.shapeFactory = new VertexShapeFactory<ONDEXConcept>(vsf, varf);
	}

	/**
	 * Sets node size transformer.
	 * 
	 * @param vsf
	 *            node size transformer
	 */
	public void setNodeSizes(Transformer<ONDEXConcept, Integer> vsf) {
		this.vsf = vsf;
		this.shapeFactory = new VertexShapeFactory<ONDEXConcept>(vsf, varf);
	}

	/**
	 * Sets NodeShapeSelection strategy to use.
	 * 
	 * @param s
	 *            NodeShapeSelection
	 */
	public void setNodeShapeSelection(NodeShapeSelection s) {
		this.strategy = s;
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @return Shape
	 */
	public Shape transform(ONDEXConcept node) {
		if (!shapes.containsKey(node))
			updateShape(node);
		// sanity check in case loading hasn't finished
		if (shapes.get(node) == null)
			return id2shape.get(0);
		return shapes.get(node);
	}

	/**
	 * Return id of corresponding shape of node.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @return Integer
	 */
	public Integer transformId(ONDEXConcept node) {
		if (!shapeIDs.containsKey(node))
			updateShape(node);
		// sanity check in case loading hasn't finished
		if (shapeIDs.get(node) == null)
			return 0;
		return shapeIDs.get(node);
	}

	/**
	 * Update all shapes from the graph.
	 */
	public void updateAll() {
		if (strategy != NodeShapeSelection.MANUAL)
			shapes.clear();
	}

	/**
	 * Update the shape of a given node.
	 * 
	 * @param node
	 *            ONDEXConcept
	 */
	public void updateShape(ONDEXConcept node) {
		if (strategy == NodeShapeSelection.MANUAL)
			return;

		int shape = Config.getShapeForConceptClass(node.getOfType());
		shapeIDs.put(node, shape);

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
	 *            ONDEXConcept
	 * @param shape
	 *            Shape
	 */
	public void updateShape(ONDEXConcept node, Shape shape) {
		shapes.put(node, shape);
	}

	/**
	 * Update the shape of a given node with a given shape.
	 * 
	 * @param node
	 *            ONDEXConcept
	 * @param shape
	 *            Shape
	 * @param shapeId
	 *            Id of associated shape
	 */
	public void updateShape(ONDEXConcept node, Shape shape, int shapeId) {
		shapes.put(node, shape);
		shapeIDs.put(node, shapeId);
	}
}
