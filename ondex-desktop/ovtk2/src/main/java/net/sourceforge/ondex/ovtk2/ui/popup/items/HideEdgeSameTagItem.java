package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected edge and all which have same tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideEdgeSameTagItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		for (ONDEXRelation e : entities) {
			ONDEXRelation r = e;
			if (r.getTags().size() > 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			ONDEXRelation r = edge;
			for (ONDEXConcept concept : r.getTags()) {
				// first set relations invisible
				for (ONDEXRelation re : viewer.getONDEXJUNGGraph().getRelationsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(re, false);
				}
				// second set concepts invisible
				for (ONDEXConcept oc : viewer.getONDEXJUNGGraph().getConceptsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(oc, false);
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
		return "Viewer.EdgeMenu.HideBySameTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideEdgeSameTag";
	}
}
