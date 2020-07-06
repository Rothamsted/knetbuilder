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
public class HideNodeLabelItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		for (ONDEXConcept node : entities) {
			if (viewer.getNodeLabels().transform(node).length() > 0)
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept n : entities) {
			viewer.getNodeLabels().getMask().put(n, false);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideLabel";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
