package net.sourceforge.ondex.ovtk2.ui.popup.items;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Hides selected node and all of the same data source
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class HideNodeDataSourceItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		return true;
	}

	@Override
	protected void doAction() {
		for (ONDEXConcept node : entities) {
			ONDEXConcept c = node;
			DataSource dataSource = c.getElementOf();
			for (ONDEXConcept oc : viewer.getONDEXJUNGGraph().getConceptsOfDataSource(dataSource)) {
				viewer.getONDEXJUNGGraph().setVisibility(oc, false);
			}
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.HIDE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.HideByDataSource";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.HideNodeDataSource";
	}
}
