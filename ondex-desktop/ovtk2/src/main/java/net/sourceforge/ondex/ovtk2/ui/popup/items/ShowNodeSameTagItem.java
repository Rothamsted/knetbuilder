package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Shows selected node and all which have same tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeSameTagItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		for (ONDEXConcept n : entities) {
			if (!n.getTags().isEmpty())
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept c : entities) {
			for (ONDEXConcept concept : c.getTags()) {
				// first set concepts visible
				for (ONDEXConcept ctxt : viewer.getONDEXJUNGGraph().getConceptsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(ctxt, true);
				}
				// second set relations visible
				for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(r, true);
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
		return "Viewer.VertexMenu.ShowBySameTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.ShowNodeSameTag";
	}
}
