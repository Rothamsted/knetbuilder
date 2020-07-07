package net.sourceforge.ondex.ovtk2.graph;

import java.util.Hashtable;

import javax.swing.undo.StateEditable;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Records the visibility of nodes and edges before an operation to be able to
 * restore it during undo.
 * 
 * @author taubertj
 * 
 */
public class VisibilityUndo implements StateEditable {

	private ONDEXJUNGGraph jung = null;

	public VisibilityUndo(ONDEXJUNGGraph jung) {
		this.jung = jung;
	}

	@Override
	public void restoreState(Hashtable<?, ?> state) {
		for (Object key : state.keySet()) {
			if (key instanceof ONDEXConcept) {
				jung.setVisibility((ONDEXConcept) key, (Boolean) state.get(key));
			} else if (key instanceof ONDEXRelation) {
				jung.setVisibility((ONDEXRelation) key, (Boolean) state.get(key));
			}
		}
		// System.out.println("restoreState: " + state.size());
	}

	@Override
	public void storeState(Hashtable<Object, Object> state) {
		for (ONDEXConcept node : jung.getConcepts()) {
			state.put(node, jung.isVisible(node));
		}
		for (ONDEXRelation edge : jung.getRelations()) {
			state.put(edge, jung.isVisible(edge));
		}
		// System.out.println("storeState: " + state.size());
	}
}
