package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected edge label
 * 
 * @author taubertj
 * 
 */
public class HideEdgeLabelItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		for (ONDEXRelation edge : entities) {
			if (viewer.getEdgeLabels().transform(edge).length() > 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			viewer.getEdgeLabels().getMask().put(edge, false);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.EdgeMenu.HideLabel";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
