package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Listens to action events specific to the selecting menu.
 * 
 * @author taubertj
 * 
 */
public class SelectingMenuAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// select all visible nodes
		if (cmd.equals("allnodes")) {
			if (viewer != null) {
				PickedState<ONDEXConcept> pickState = viewer.getVisualizationViewer().getPickedVertexState();
				for (ONDEXConcept n : viewer.getONDEXJUNGGraph().getVertices())
					pickState.pick(n, true);
			}
		}

		// select all visible edges
		else if (cmd.equals("alledges")) {
			if (viewer != null) {
				PickedState<ONDEXRelation> pickState = viewer.getVisualizationViewer().getPickedEdgeState();
				for (ONDEXRelation e : viewer.getONDEXJUNGGraph().getEdges())
					pickState.pick(e, true);
			}
		}

		// inverse node selection
		else if (cmd.equals("inversenodes")) {
			if (viewer != null) {
				PickedState<ONDEXConcept> pickState = viewer.getVisualizationViewer().getPickedVertexState();
				for (ONDEXConcept n : viewer.getONDEXJUNGGraph().getVertices())
					pickState.pick(n, !pickState.isPicked(n));
			}
		}

		// inverse edge selection
		else if (cmd.equals("inverseedges")) {
			if (viewer != null) {
				PickedState<ONDEXRelation> pickState = viewer.getVisualizationViewer().getPickedEdgeState();
				for (ONDEXRelation e : viewer.getONDEXJUNGGraph().getEdges())
					pickState.pick(e, !pickState.isPicked(e));
			}
		}

		// highlight selected tag concept
		else if (cmd.startsWith("showtag")) {
			if (viewer != null) {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();

				// set tag concept visible
				int id = Integer.parseInt(cmd.substring(7, cmd.length()));
				ONDEXConcept c = graph.getConcept(id);
				graph.setVisibility(c, true);

				// pick tag concept only
				PickedState<ONDEXConcept> pickState = viewer.getVisualizationViewer().getPickedVertexState();
				pickState.clear();
				pickState.pick(c, true);

				// perform zoom in to picked concept
				desktop.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OVTK2ToolBar.ZOOMIN));
			}
		}
	}
}
