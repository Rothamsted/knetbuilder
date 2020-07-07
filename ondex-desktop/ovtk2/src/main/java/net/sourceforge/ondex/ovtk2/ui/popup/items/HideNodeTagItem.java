package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all which have this node as tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeTagItem extends EntityMenuItem<ONDEXConcept> {

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
			// first set relations invisible
			for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfTag(c)) {
				viewer.getONDEXJUNGGraph().setVisibility(r, false);
			}
			// second set concepts invisible
			for (ONDEXConcept co : viewer.getONDEXJUNGGraph().getConceptsOfTag(c)) {
				viewer.getONDEXJUNGGraph().setVisibility(co, false);
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideByTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNodeTag";
	}
}
