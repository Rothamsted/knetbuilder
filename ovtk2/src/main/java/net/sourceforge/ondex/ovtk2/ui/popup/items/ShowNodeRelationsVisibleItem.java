package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Shows all relations from this node to visible nodes
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeRelationsVisibleItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept n : entities) {
			if (!graph.getRelationsOfConcept(n).isEmpty())
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept c : entities) {
			for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfConcept(c)) {
				// check if this is an invisible edge
				if (!jung.isVisible(r)) {
					if (r.getFromConcept().equals(c)) {
						// check if to is visible
						if (jung.isVisible(r.getToConcept()))
							jung.setVisibility(r, true);
					} else {
						// check if from is visible
						if (jung.isVisible(r.getFromConcept()))
							jung.setVisibility(r, true);
					}
				}
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ShowByRelationsVisible";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.ShowNodeRelationsVisible";
	}
}
