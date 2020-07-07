package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Shows selected edge and all which have same tag
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowEdgeSameTagItem extends EntityMenuItem<ONDEXRelation> {

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
				// first set concepts visible
				for (ONDEXConcept ctxt : viewer.getONDEXJUNGGraph().getConceptsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(ctxt, true);
				}
				// second set relations visible
				for (ONDEXRelation re : viewer.getONDEXJUNGGraph().getRelationsOfTag(concept)) {
					viewer.getONDEXJUNGGraph().setVisibility(re, true);
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
		return "Viewer.EdgeMenu.ShowBySameTag";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.ShowEdgeSameTag";
	}
}
