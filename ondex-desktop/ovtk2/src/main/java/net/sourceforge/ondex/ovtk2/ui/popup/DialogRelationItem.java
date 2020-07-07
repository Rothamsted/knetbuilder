package net.sourceforge.ondex.ovtk2.ui.popup;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogRelation;

/**
 * Menu item to display dialog of relation properties.
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class DialogRelationItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			DialogRelation dialog = new DialogRelation((OVTK2Viewer) viewer, edge);
			dialog.setLocation(getPoint());
			((OVTK2Viewer) viewer).getDesktopPane().add(dialog);
			dialog.setVisible(true);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return null;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.Edit";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}
}
