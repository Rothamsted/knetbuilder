package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Shows selected node and all which have this node as tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeTagItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept n : entities) {
			if (!graph.getConceptsOfTag(n).isEmpty())
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept vertex : entities) {
			ONDEXConcept c = vertex;
			// first set concepts visible
			for (ONDEXConcept ctxt : viewer.getONDEXJUNGGraph().getConceptsOfTag(c)) {
				viewer.getONDEXJUNGGraph().setVisibility(ctxt, true);
			}
			// second set relations visible
			for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfTag(c)) {
				viewer.getONDEXJUNGGraph().setVisibility(r, true);
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ShowByTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.ShowNodeTag";
	}
}
