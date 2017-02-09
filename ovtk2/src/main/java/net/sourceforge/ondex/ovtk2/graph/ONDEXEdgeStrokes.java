package net.sourceforge.ondex.ovtk2.graph;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;

import org.apache.commons.collections15.Transformer;

/**
 * Provides a transformation from a given ONDEXRelation to a edge stroke.
 * 
 * @author taubertj
 */
public class ONDEXEdgeStrokes implements Transformer<ONDEXRelation, Stroke> {

	// edge stroke size function
	private Transformer<ONDEXRelation, Integer> esf = null;

	// caches stroke to relation type mapping
	private Map<RelationType, Stroke> strokes = new HashMap<RelationType, Stroke>();

	/**
	 * Returns the edge size function.
	 * 
	 * @return Transformer<ONDEXRelation, Integer>
	 */
	public Transformer<ONDEXRelation, Integer> getEdgeSizeTransformer() {
		return esf;
	}

	/**
	 * Sets edge stroke size transformer.
	 * 
	 * @param esf
	 *            edge stroke size transformer
	 */
	public void setEdgeSizes(Transformer<ONDEXRelation, Integer> esf) {
		this.esf = esf;
	}

	/**
	 * Returns result of transformation.
	 * 
	 * @param edge
	 *            ONDEXRelation
	 * @return String
	 */
	public Stroke transform(ONDEXRelation edge) {
		if (esf != null) {
			// transform edge thickness
			return new BasicStroke(esf.transform(edge));
		} else {
			// without a size transformer
			if (!strokes.containsKey(edge.getOfType())) {
				Stroke stroke = new BasicStroke(Config.getSizeForRelationType(edge.getOfType()));
				strokes.put(edge.getOfType(), stroke);
			}
			return strokes.get(edge.getOfType());
		}
	}

	/**
	 * Clears the stroke cache, so sizes get re-assigned
	 */
	public void updateAll() {
		esf = null;
		strokes.clear();
	}

}
