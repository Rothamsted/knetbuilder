package net.sourceforge.ondex.ovtk2.ui.toolbars;

import static net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar.REFRESH;
import static net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar.ZOOMIN;
import static net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar.ZOOMOUT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConcept;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogRelation;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.VisualisationUtils;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

/**
 * Handles tool-bar related action events.
 * 
 * @author taubertj
 * 
 */
public class ToolBarAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// add a concept or relation to the active viewer
		if (cmd.equals("add")) {
			if (viewer != null) {
				Object[] options = { Config.language.getProperty("Dialog.Add.ChoiceConcept"), Config.language.getProperty("Dialog.Add.ChoiceRelation") };
				int n = JOptionPane.showInternalOptionDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.Add.Text"), Config.language.getProperty("Dialog.Add.Title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon("config/toolbarButtonGraphics/general/Add24.gif"), options, options[0]);
				if (n == 0) {
					DialogConcept dialog = new DialogConcept(viewer, null);
					desktop.display(dialog, Position.centered);
				} else if (n == 1) {
					DialogRelation dialog = new DialogRelation(viewer, null);
					desktop.display(dialog, Position.centered);
				}
			}
		}

		// edit select node or edge
		else if (cmd.equals("edit")) {
			if (viewer != null) {
				if (viewer.getPickedNodes().size() == 1 && viewer.getPickedEdges().size() == 0) {
					ONDEXConcept node = viewer.getPickedNodes().iterator().next();
					DialogConcept dialog = new DialogConcept(viewer, node);
					desktop.display(dialog, Position.centered);
				} else if (viewer.getPickedNodes().size() == 0 && viewer.getPickedEdges().size() == 1) {
					ONDEXRelation edge = viewer.getPickedEdges().iterator().next();
					DialogRelation dialog = new DialogRelation(viewer, edge);
					desktop.display(dialog, Position.centered);
				}
			}
		}

		// delete selection
		else if (cmd.equals("delete")) {
			if (viewer != null) {
				// ask user if he really wants to delete them
				int answer = JOptionPane.showInternalConfirmDialog(viewer, Config.language.getProperty("ToolBar.DeleteWarning"), "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (answer == JOptionPane.YES_OPTION) {

					// first remove relations
					for (ONDEXRelation r : viewer.getPickedEdges()) {
						viewer.getONDEXJUNGGraph().deleteRelation(r.getId());
					}

					// then delete concepts
					for (ONDEXConcept c : viewer.getPickedNodes()) {
						viewer.getONDEXJUNGGraph().deleteConcept(c.getId());
					}

					// notify model of change
					viewer.getVisualizationViewer().getModel().fireStateChanged();
					((ActionListener) viewer).actionPerformed(new ActionEvent(this, 0, REFRESH));

					viewer.getMetaGraph().updateMetaData();

					// OVTK-202
					String title = viewer.getTitle();
					if (!title.endsWith(" (modified)")) {
						viewer.setTitle(title + " (modified)");
						viewer.setName(viewer.getName() + " (modified)");
					}
				}
			}
		}

		// copy whole graph
		else if (cmd.equals("copy")) {
			if (viewer != null) {
				// get attributes from original graph
				OVTK2Viewer current = viewer;
				ONDEXGraph old = current.getONDEXJUNGGraph();

				int option = JOptionPane.showInternalConfirmDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.Copy.Text"), Config.language.getProperty("Dialog.Copy.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					// initialise new OVTK2Viewer
					resources.setSelectedViewer(DesktopUtils.initViewer(old));
					viewer = (OVTK2Viewer) resources.getSelectedViewer();

					Map<Integer, Boolean> node_visibility = current.getONDEXJUNGGraph().getVertices_visibility();
					Map<Integer, Boolean> edge_visibility = current.getONDEXJUNGGraph().getEdges_visibility();

					// synchronise visibility
					Iterator<Integer> it = node_visibility.keySet().iterator();
					while (it.hasNext()) {
						int internal = it.next();
						viewer.getONDEXJUNGGraph().getVertices_visibility().put(internal, node_visibility.get(internal));
					}
					it = edge_visibility.keySet().iterator();
					while (it.hasNext()) {
						int internal = it.next();
						viewer.getONDEXJUNGGraph().getEdges_visibility().put(internal, edge_visibility.get(internal));
					}

					// synchronise layout
					viewer.getVisualizationViewer().setGraphLayout(current.getVisualizationViewer().getGraphLayout());
				} else {
					// initialise new OVTK2Viewer only
					resources.setSelectedViewer(DesktopUtils.initViewer(old));
					viewer = (OVTK2Viewer) resources.getSelectedViewer();
				}

				// display and update cloned graph
				desktop.display(viewer, Position.centered);
				viewer.updateViewer(null);

				// OVTK-202
				String title = viewer.getTitle();
				if (!title.endsWith(" (copied)")) {
					viewer.setTitle(title + " (copied)");
					viewer.setName(viewer.getName() + " (copied)");
				}
			}
		}

		// refresh layout of graph
		else if (REFRESH.equals(cmd)) {
			if (viewer != null) {
				VisualisationUtils.relayout(viewer, OVTK2Desktop.getInstance().getMainFrame());
			}
		}

		// zoom in
		else if (ZOOMIN.equals(cmd)) {
			if (viewer != null) {
				VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = viewer.getVisualizationViewer();

				// check for node selection
				Set<ONDEXConcept> picked = viewer.getPickedNodes();
				if (picked.size() > 0) {
					VisualisationUtils.zoomIn(viewer);
				} else {
					OVTK2GraphMouse mouse = (OVTK2GraphMouse) vv.getGraphMouse();
					mouse.getScaler().scale(vv, 1.1f, vv.getCenter());
				}
			}
		}

		// zoom out
		else if (ZOOMOUT.equals(cmd)) {
			if (viewer != null) {
				VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = viewer.getVisualizationViewer();
				OVTK2GraphMouse mouse = (OVTK2GraphMouse) vv.getGraphMouse();
				mouse.getScaler().scale(vv, 0.9f, vv.getCenter());
			}
		}

		// switch to picking mode
		else if (cmd.equals(OVTK2ToolBar.PICKING_MODE)) {
			if (viewer != null) {
				viewer.setMouseMode(true);
				if (viewer.getMetaGraphPanel() != null)
					viewer.getMetaGraphPanel().setMouseMode(true);
			}
		}

		// switch to transforming mode
		else if (cmd.equals(OVTK2ToolBar.TRANSFORMING_MODE)) {
			if (viewer != null) {
				viewer.setMouseMode(false);
				if (viewer.getMetaGraphPanel() != null)
					viewer.getMetaGraphPanel().setMouseMode(false);
			}
		}

		// switch to annotation mode
		else if (cmd.equals(OVTK2ToolBar.ANNOTATION_MODE)) {
			if (viewer != null) {
				VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = viewer.getVisualizationViewer();
				ModalGraphMouse graphMouse = (ModalGraphMouse) vv.getGraphMouse();
				graphMouse.setMode(Mode.ANNOTATING);
			}
		}

	}
}
