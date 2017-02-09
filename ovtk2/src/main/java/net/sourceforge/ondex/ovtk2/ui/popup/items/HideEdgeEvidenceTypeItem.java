package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected edge and all which share evidence types
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideEdgeEvidenceTypeItem extends EntityMenuItem<ONDEXRelation> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXRelation edge : entities) {
			ONDEXRelation r = edge;
			for (EvidenceType et : r.getEvidence()) {
				for (ONDEXRelation or : viewer.getONDEXJUNGGraph().getRelationsOfEvidenceType(et)) {
					viewer.getONDEXJUNGGraph().setVisibility(or, false);
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
		return "Viewer.EdgeMenu.HideByEvidenceType";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideEdgeEvidenceType";
	}
}
