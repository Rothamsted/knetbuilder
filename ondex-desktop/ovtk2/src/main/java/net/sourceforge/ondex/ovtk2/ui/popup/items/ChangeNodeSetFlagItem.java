package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;

/**
 * Sets the flag attribute of selected node(s)
 * 
 * @author taubertj
 * 
 */
public class ChangeNodeSetFlagItem extends EntityMenuItem<ONDEXConcept> {

	AttributeName anFlag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem#init(net.sourceforge
	 * .ondex.ovtk2.ui.OVTK2PropertiesAggregator, java.util.Set)
	 */
	@Override
	public void init(OVTK2PropertiesAggregator v, Set<ONDEXConcept> e) {
		super.init(v, e);
		ONDEXJUNGGraph graph = this.viewer.getONDEXJUNGGraph();
		anFlag = graph.getMetaData().getAttributeName(AppearanceSynchronizer.FLAGGED);
		if (anFlag == null)
			anFlag = graph.getMetaData().getFactory().createAttributeName(AppearanceSynchronizer.FLAGGED, "flagged concept", Boolean.class);
	}

	@Override
	public boolean accepts() {
		boolean found = true;
		for (ONDEXConcept c : entities) {
			// at least one concept not flagged
			if (c.getAttribute(anFlag) == null) {
				found = false;
				break;
			} else if (c.getAttribute(anFlag).getValue().equals(Boolean.FALSE)) {
				found = false;
				break;
			}
		}
		return !found;
	}

	@Override
	protected void doAction() {

		// set flagged attribute to true on all concepts
		for (ONDEXConcept c : entities) {
			if (c.getAttribute(anFlag) == null) {
				c.createAttribute(anFlag, Boolean.TRUE, false);
			} else {
				c.getAttribute(anFlag).setValue(Boolean.TRUE);
			}
		}
	}

	@Override
	public net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem.MENUCATEGORY getCategory() {
		return MENUCATEGORY.CHANGE;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ChangeSetFlag";
	}

	@Override
	protected String getUndoPropertyName() {
		return null;
	}

}
