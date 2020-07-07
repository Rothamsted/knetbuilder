package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected edge
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideEdgeItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities)
			viewer.getONDEXJUNGGraph().setVisibility(edge, false);
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.EdgeMenu.Hide";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideEdge";
	}
}
