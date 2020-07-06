package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all of the same concept class
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeConceptClassItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept vertex : entities) {
			ONDEXConcept c = vertex;
			ConceptClass cc = c.getOfType();
			for (ONDEXConcept node : viewer.getONDEXJUNGGraph().getVertices()) {
				if (node.getOfType() == cc) {
					viewer.getONDEXJUNGGraph().setVisibility(node, false);
				}
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideByConceptClass";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNodeConceptClass";
	}
}
