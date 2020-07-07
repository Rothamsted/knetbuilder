package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.Collection;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hide all nodes that are not in the current selection.
 * 
 * @author Matthew Pocock
 */
public class HideOtherNodesItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		Collection<ONDEXConcept> allNodes = viewer.getONDEXJUNGGraph().getVertices();
		for (ONDEXConcept n : allNodes) {
			viewer.getONDEXJUNGGraph().setVisibility(n, entities.contains(n));
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideOtherNodes";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideOtherNodes";
	}
}
