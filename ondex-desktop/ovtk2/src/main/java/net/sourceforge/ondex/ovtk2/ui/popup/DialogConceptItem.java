package net.sourceforge.ondex.ovtk2.ui.popup;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConcept;

/**
 * Menu item to display dialog of concept properties.
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class DialogConceptItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept n : entities) {
			DialogConcept dialog = new DialogConcept((OVTK2Viewer) viewer, n);
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
