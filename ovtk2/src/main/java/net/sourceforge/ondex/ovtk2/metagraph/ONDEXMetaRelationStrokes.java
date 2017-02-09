package net.sourceforge.ondex.ovtk2.metagraph;

import java.awt.BasicStroke;
import java.awt.Stroke;

import net.sourceforge.ondex.core.ONDEXGraph;

import org.apache.commons.collections15.Transformer;

/**
 * Provides a transformation from a given ONDEXMetaRelation to a edge stroke.
 * 
 * @author taubertj
 * 
 */
public class ONDEXMetaRelationStrokes implements Transformer<ONDEXMetaRelation, Stroke> {

	// default case
	private Stroke defaultStroke = new BasicStroke(1.0f);

	/**
	 * Initialises the strokes for the edges in the graph.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph
	 */
	public ONDEXMetaRelationStrokes(ONDEXGraph aog) {

	}

	public void setThickness(int thickness) {
		defaultStroke = new BasicStroke(Float.valueOf(thickness));
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXEdge
	 * @return String
	 */
	public Stroke transform(ONDEXMetaRelation edge) {
		return defaultStroke;
	}

}
