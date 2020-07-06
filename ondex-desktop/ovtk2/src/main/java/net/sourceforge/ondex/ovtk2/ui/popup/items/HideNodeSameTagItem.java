package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all which have same tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeSameTagItem extends EntityMenuItem<ONDEXConcept> {

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
		for (ONDEXConcept vertex : entities) {
			ONDEXConcept c = vertex;
			for (ONDEXConcept concept : c.getTags()) {
				// first set relations invisible
				for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(r, false);
				}
				// second set concepts invisible
				for (ONDEXConcept c2 : viewer.getONDEXJUNGGraph().getConceptsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(c2, false);
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
		return "Viewer.VertexMenu.HideBySameTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNodeSameTag";
	}
}
