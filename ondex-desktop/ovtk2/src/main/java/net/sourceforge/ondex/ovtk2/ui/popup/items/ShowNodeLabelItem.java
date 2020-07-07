package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeLabelItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		for (ONDEXConcept node : entities) {
			if (viewer.getNodeLabels().transform(node).length() == 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept n : entities) {
			viewer.getNodeLabels().updateLabel(n);
			viewer.getNodeLabels().getMask().put(n, true);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ShowLabel";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
