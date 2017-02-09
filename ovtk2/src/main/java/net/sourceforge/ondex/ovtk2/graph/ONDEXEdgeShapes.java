package net.sourceforge.ondex.ovtk2.graph;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.Loop;

/**
 * Provides a transformation from a given ONDEXReklation to a edge shape.
 * 
 * @author taubertj
 * @author Matthew Pocock
 */
public class ONDEXEdgeShapes extends AbstractEdgeShapeTransformer<ONDEXConcept, ONDEXRelation> implements IndexedRendering<ONDEXConcept, ONDEXRelation> {

	public static final String KEYLINE = "line";

	public static final String KEYBENT = "bent";

	public static final String KEYCUBIC = "cubic";

	public static final String KEYQUAD = "quad";

	public static final String GRAPH_EDGE_SHAPES = "Graph.EdgeShapes";

	/**
	 * All different possible edge shapes.
	 * 
	 * @author taubertj
	 * 
	 */
	public enum EdgeShape {
		QUAD, CUBIC, BENT, LINE
	}

	/**
	 * Default edge shape.
	 */
	public EdgeShape shape = EdgeShape.QUAD;

	/**
	 * Initialises the shapes for the edges in the graph.
	 * 
	 */
	public ONDEXEdgeShapes() {
		updateAll();
	}

	/**
	 * Resets the shape scheme to configured one.
	 */
	public void updateAll() {
		String name = Config.visual.getProperty(GRAPH_EDGE_SHAPES);
		if (name.equals(KEYQUAD))
			shape = EdgeShape.QUAD;
		else if (name.equals(KEYCUBIC))
			shape = EdgeShape.CUBIC;
		else if (name.equals(KEYBENT))
			shape = EdgeShape.BENT;
		else if (name.equals(KEYLINE))
			shape = EdgeShape.LINE;
	}

	/**
	 * Sets the edge shape to use.
	 * 
	 * @param shape
	 */
	public void setEdgeShape(EdgeShape shape) {
		this.shape = shape;
		switch (shape) {
		case QUAD:
			Config.visual.setProperty(GRAPH_EDGE_SHAPES, KEYQUAD);
			break;
		case CUBIC:
			Config.visual.setProperty(GRAPH_EDGE_SHAPES, KEYCUBIC);
			break;
		case BENT:
			Config.visual.setProperty(GRAPH_EDGE_SHAPES, KEYBENT);
			break;
		case LINE:
			Config.visual.setProperty(GRAPH_EDGE_SHAPES, KEYLINE);
			break;
		}
		Config.saveVisual();
	}

	/**
	 * Returns current edge shape.
	 * 
	 * @return
	 */
	public EdgeShape getEdgeShape() {
		return this.shape;
	}

	/**
	 * Used for deriving the index of an edge.
	 */
	protected EdgeIndexFunction<ONDEXConcept, ONDEXRelation> parallelEdgeIndexFunction;

	/**
	 * A convenience instance for other edge shapes to use for self-loop edges
	 * where parallel instances will not overlay each other.
	 */
	protected static Loop<ONDEXConcept, ONDEXRelation> loop = new Loop<ONDEXConcept, ONDEXRelation>();

	/**
	 * Singleton instance of the Line2D edge shape
	 */
	private static Line2D line = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);

	/**
	 * singleton instance of the QuadCurve shape
	 */
	private static QuadCurve2D quad = new QuadCurve2D.Float();

	/**
	 * singleton instance of the BentLine shape
	 */
	private static GeneralPath bent = new GeneralPath();

	/**
	 * singleton instance of the CubicCurve edge shape
	 */
	private static CubicCurve2D cubic = new CubicCurve2D.Float();

	public void setEdgeIndexFunction(EdgeIndexFunction<ONDEXConcept, ONDEXRelation> parallelEdgeIndexFunction) {
		this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
		loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
	}

	/**
	 * @return the parallelEdgeIndexFunction
	 */
	public EdgeIndexFunction<ONDEXConcept, ONDEXRelation> getEdgeIndexFunction() {
		return parallelEdgeIndexFunction;
	}

	/**
	 * Get the shape for this edge, returning either the shared instance or, in
	 * the case of self-loop edges, the Loop shared instance.
	 */
	public Shape transform(Context<Graph<ONDEXConcept, ONDEXRelation>, ONDEXRelation> context) {
		Graph<ONDEXConcept, ONDEXRelation> graph = context.graph;
		ONDEXRelation e = context.element;
		Pair<ONDEXConcept> endpoints = graph.getEndpoints(e);
		if (endpoints != null) {
			boolean isLoop = endpoints.getFirst().equals(endpoints.getSecond());
			if (isLoop) {
				return loop.transform(context);
			}
		}

		int index = 1;
		if (parallelEdgeIndexFunction != null) {
			index = parallelEdgeIndexFunction.getIndex(graph, e);
		}

		float controlY = control_offset_increment + control_offset_increment * index;

		switch (shape) {
		case QUAD:
			quad.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
			return quad;
		case CUBIC:
			cubic.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, .66f, -controlY, 1.0f, 0.0f);
			return cubic;
		case BENT:
			bent.reset();
			bent.moveTo(0.0f, 0.0f);
			bent.lineTo(0.5f, controlY);
			bent.lineTo(1.0f, 1.0f);
			return bent;
		default:
			return line;
		}
	}

}
