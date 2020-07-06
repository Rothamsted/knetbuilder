package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected edge and all edges with same relationtype
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideEdgeRelationTypeItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			ONDEXRelation r = edge;
			for (ONDEXRelation or : viewer.getONDEXJUNGGraph().getRelationsOfRelationType(r.getOfType())) {
				viewer.getONDEXJUNGGraph().setVisibility(or, false);
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.EdgeMenu.HideByRelationType";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideEdgeRelationType";
	}
}
