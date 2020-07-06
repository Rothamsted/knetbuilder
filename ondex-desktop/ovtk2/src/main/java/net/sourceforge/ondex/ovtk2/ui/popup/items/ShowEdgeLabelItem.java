package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Shows selected edge label
 * 
 * @author taubertj
 * 
 */
public class ShowEdgeLabelItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		for (ONDEXRelation edge : entities) {
			if (viewer.getEdgeLabels().transform(edge).length() == 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			viewer.getEdgeLabels().updateLabel(edge);
			viewer.getEdgeLabels().getMask().put(edge, true);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.EdgeMenu.ShowLabel";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
