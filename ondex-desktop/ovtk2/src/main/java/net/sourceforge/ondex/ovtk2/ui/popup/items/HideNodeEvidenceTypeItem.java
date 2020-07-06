package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all which share evidence types
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeEvidenceTypeItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept vertex : entities) {
			ONDEXConcept c = vertex;
			for (EvidenceType et : c.getEvidence()) {
				for (ONDEXConcept oc : viewer.getONDEXJUNGGraph().getConceptsOfEvidenceType(et)) {
					viewer.getONDEXJUNGGraph().setVisibility(oc, false);
				}
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideByEvidenceType";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNodeEvidenceType";
	}
}
