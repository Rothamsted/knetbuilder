package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.Collection;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		Collection<ONDEXConcept> allNodes = viewer.getONDEXJUNGGraph().getVertices();
		for (ONDEXConcept n : allNodes) {
			viewer.getONDEXJUNGGraph().setVisibility(n, !entities.contains(n));
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.Hide";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNode";
	}
}
