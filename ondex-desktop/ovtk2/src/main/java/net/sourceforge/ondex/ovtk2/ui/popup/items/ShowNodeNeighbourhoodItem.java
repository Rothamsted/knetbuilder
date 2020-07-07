package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;

/**
 * Shows everything in the neighbourhood of this node
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ShowNodeNeighbourhoodItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept c : entities) {
			if (!graph.getRelationsOfConcept(c).isEmpty())
				return true;
		}
		return false;
	}

	@Override
	protected void doAction() {
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept c : entities) {

			// which are invisible neighbours for later arrangement
			Set<ONDEXConcept> neighbours = new HashSet<ONDEXConcept>();

			// get all relations of concept
			for (ONDEXRelation r : viewer.getONDEXJUNGGraph().getRelationsOfConcept(c)) {

				// get the other concept
				ONDEXConcept other = r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();

				// if concept invisible add to re-layout
				if (!jung.isVisible(other))
					neighbours.add(other);

				// make concept visible
				jung.setVisibility(other, true);

				// make relation visible
				jung.setVisibility(r, true);
			}

			// arrange newly visible neighbours
			LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), c, neighbours);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.SHOW;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.ShowByNeighbourhood";
	}

	@Override
	protected String getUndoPropertyName() {
		return "Undo.ShowNodeNeighbourhood";
	}
}
