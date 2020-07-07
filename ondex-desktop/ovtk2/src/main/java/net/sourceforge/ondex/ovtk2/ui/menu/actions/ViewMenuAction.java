package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Legend;
import net.sourceforge.ondex.ovtk2.ui.OVTK2MetaGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Satellite;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.contentsdisplay.ContentsDisplay;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogEdges;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogNodes;
import net.sourceforge.ondex.ovtk2.ui.editor.GraphTableEditor;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Listens to action events specific to the view menu.
 * 
 * @author taubertj
 * @author Matthew Pocock
 * 
 */
public class ViewMenuAction implements ActionListener, InternalFrameListener {

	// contents display
	private static ContentsDisplay contentsdisplay = null;

	// edges visibility dialog
	private static DialogEdges dialogEdges = null;

	// node visibility dialog
	private static DialogNodes dialogNodes = null;

	// map of table graph editors to viewer windows
	private static Map<OVTK2Viewer, GraphTableEditor> editors = new HashMap<OVTK2Viewer, GraphTableEditor>();

	// legend of meta data of active graph
	private static OVTK2Legend legend = null;

	// meta graph view of active graph
	private static OVTK2MetaGraph metagraph = null;

	// satellite view of active graph
	private static OVTK2Satellite satellite = null;

	/**
	 * Returns current legend or null is none is visible.
	 * 
	 * @return OVTK2Legend
	 */
	public static OVTK2Legend getLegend() {
		return legend;
	}

	/**
	 * Returns the currently set meta graph.
	 * 
	 * @return current OVTK2MetaGraph
	 */
	public static OVTK2MetaGraph getMetaGraph() {
		return metagraph;
	}

	/**
	 * Returns whether or not the contents display is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isContentsDisplayShown() {
		return contentsdisplay != null && contentsdisplay.isVisible();
	}

	/**
	 * Returns whether or not the edges list is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isDialogEdgesShown() {
		return dialogEdges != null && dialogEdges.isVisible();
	}

	/**
	 * Returns whether or not the nodes list is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isDialogNodesShown() {
		return dialogNodes != null && dialogNodes.isVisible();
	}

	/**
	 * Returns whether or not the legend is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isLegendShown() {
		return legend != null && legend.isVisible();
	}

	/**
	 * Returns whether or not the meta graph is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isMetaGraphShown() {
		return metagraph != null && metagraph.isVisible();
	}

	/**
	 * Returns whether or not the satellite view is currently visible.
	 * 
	 * @return is visible?
	 */
	public static boolean isSatelliteShown() {
		return satellite != null && satellite.isVisible();
	}

	/**
	 * Closes a internal frame if it is not already null.
	 * 
	 * @param frame
	 *            frame to close
	 */
	private static void close(RegisteredJInternalFrame frame) {
		if (frame != null) {
			try {
				frame.setClosed(true);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
		}
	}

	/**
	 * Shows the contents display frame.
	 * 
	 * @param viewer
	 *            what viewer to show contents display for
	 */
	private static void showContentsDisplay(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a contents display
		if (contentsdisplay == null) {
			contentsdisplay = new ContentsDisplay(viewer.getONDEXJUNGGraph());
			contentsdisplay.addInternalFrameListener(desktop);
			desktop.display(contentsdisplay, Position.leftTop);
		}
		updateContentsDisplay(viewer);
		contentsdisplay.setVisible(true);
		try {
			contentsdisplay.setIcon(false);
		} catch (PropertyVetoException e) {
			ErrorDialog.show(e);
		}
		contentsdisplay.toFront();
	}

	/**
	 * Shows the edges list frame.
	 * 
	 * @param viewer
	 *            what viewer to show edges list for
	 */
	private static void showDialogEdges(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a edges list
		if (dialogEdges == null) {
			dialogEdges = new DialogEdges(viewer);
			dialogEdges.addInternalFrameListener(desktop);
			desktop.display(dialogEdges, Position.leftTop);
		} else {
			dialogEdges.setViewer(viewer);
			dialogEdges.setVisible(true);
			try {
				dialogEdges.setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			dialogEdges.toFront();
		}
	}

	/**
	 * Shows the nodes list frame.
	 * 
	 * @param viewer
	 *            what viewer to show nodes list for
	 */
	private static void showDialogNodes(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a nodes list
		if (dialogNodes == null) {
			dialogNodes = new DialogNodes(viewer);
			dialogNodes.addInternalFrameListener(desktop);
			desktop.display(dialogNodes, Position.leftTop);
		} else {
			dialogNodes.setViewer(viewer);
			dialogNodes.setVisible(true);
			try {
				dialogNodes.setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			dialogNodes.toFront();
		}
	}

	/**
	 * Shows the meta data legend.
	 * 
	 * @param viewer
	 *            what viewer to show meta data for
	 */
	private static void showLegend(final OVTK2Viewer viewer) {
		final OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a legend
		if (legend == null) {
			legend = new OVTK2Legend(viewer);
			legend.addInternalFrameListener(desktop);
			desktop.display(legend, Position.leftBottom);
		} else {
			legend.setViewer(viewer);
			legend.setVisible(true);
			try {
				legend.setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			legend.toFront();
		}
	}

	/**
	 * Shows the meta graph frame.
	 * 
	 * @param viewer
	 *            what viewer to show meta graph for
	 */
	private static void showMetaGraph(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a meta graph
		if (metagraph == null) {
			metagraph = new OVTK2MetaGraph(viewer);
			metagraph.addInternalFrameListener(desktop);
			desktop.display(metagraph, Position.centered);
		} else {
			metagraph.setViewer(viewer);
			metagraph.setVisible(true);
			try {
				metagraph.setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			metagraph.toFront();
		}
	}

	/**
	 * Shows the satellite frame.
	 * 
	 * @param viewer
	 *            what viewer to show satellite view for
	 */
	private static void showSatellite(OVTK2Viewer viewer) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();

		// check if there is already a nodes list
		if (satellite == null) {
			satellite = new OVTK2Satellite(viewer);
			satellite.addInternalFrameListener(desktop);
			desktop.display(satellite, Position.right);
		} else {
			satellite.setViewer(viewer);
			satellite.setVisible(true);
			try {
				satellite.setIcon(false);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
			satellite.toFront();
		}
	}

	/**
	 * Refreshes the contents display with a different viewer.
	 * 
	 * @param viewer
	 *            viewer to set to contents display
	 */
	private static void updateContentsDisplay(OVTK2Viewer viewer) {

		// just in case we do not want to add it twice
		viewer.removePickingListener(contentsdisplay);
		viewer.addPickingListener(contentsdisplay);

		// get currently picked node
		Set<ONDEXConcept> pickedNodes = viewer.getPickedNodes();
		if (pickedNodes.size() > 0)
			contentsdisplay.showInfoFor(pickedNodes.iterator().next());
		else {
			// if no node selected, try edges
			Set<ONDEXRelation> pickedEdges = viewer.getPickedEdges();
			if (pickedEdges.size() > 0)
				contentsdisplay.showInfoFor(pickedEdges.iterator().next());
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		final OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e);
		} catch (MalformedURLException e) {
			ErrorDialog.show(e);
		}

		// toggle meta graph view
		if (cmd.equals("metagraph")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showMetaGraph(viewer);
				} else {
					close(metagraph);
				}
			}
		}

		// toggle legend view
		else if (cmd.equals("legend")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showLegend(viewer);
				} else {
					close(legend);
				}
			}
		}

		// show tabular graph editor
		else if (cmd.equals("editor")) {
			if (viewer != null) {

				// check for size of graph before displaying editor
				ONDEXJUNGGraph g = viewer.getONDEXJUNGGraph();
				if (g.getConcepts().size() > 1000 || g.getRelations().size() > 1000) {
					int result = JOptionPane.showInternalConfirmDialog(desktop.getDesktopPane(), "The current graph contains more than 1000 nodes or edges.\n" + "It is not recommend to use the Tabular Graph Editor for such a large graph.\n" + "Do you still want to proceed?", "Large graph warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.NO_OPTION)
						return;
				}

				// only one editor per viewer
				if (!editors.containsKey(viewer)) {
					GraphTableEditor editor = new GraphTableEditor(viewer.getONDEXJUNGGraph());

					// handle closing of all editor windows
					editor.addInternalFrameListener(this);
					editors.put(viewer, editor);
					desktop.display(editor, Position.centered);
				} else {
					GraphTableEditor editor = editors.get(viewer);
					editor.setVisible(true);
					try {
						editor.setIcon(false);
					} catch (PropertyVetoException e) {
						ErrorDialog.show(e);
					}
					editor.toFront();
				}
			}
		}

		// toggle contents display view
		else if (cmd.equals("contentsdisplay")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showContentsDisplay(viewer);
				} else {
					close(contentsdisplay);
				}
			}
		}

		// show node visibility dialog
		else if (cmd.equals("nodevisible")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showDialogNodes(viewer);
				} else {
					close(dialogNodes);
				}
			}
		}

		// show edge visibility dialog
		else if (cmd.equals("edgevisible")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showDialogEdges(viewer);
				} else {
					close(dialogEdges);
				}
			}
		}

		// toggle satellite view
		else if (cmd.equals("satellite")) {
			if (viewer != null) {
				boolean selected = ((JCheckBoxMenuItem) ae.getSource()).isSelected();
				if (selected) {
					showSatellite(viewer);
				} else {
					close(satellite);
				}
			}
		}
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		if (e.getInternalFrame() instanceof OVTK2Viewer) {
			OVTK2Viewer viewer = (OVTK2Viewer) e.getInternalFrame();
			if (dialogNodes != null && !dialogNodes.getViewer().equals(viewer)) {
				dialogNodes.setViewer(viewer);
			}
			if (dialogEdges != null && !dialogEdges.getViewer().equals(viewer)) {
				dialogEdges.setViewer(viewer);
			}
			if (satellite != null && !satellite.getViewer().equals(viewer)) {
				satellite.setViewer(viewer);
			}
			if (metagraph != null && !metagraph.getViewer().equals(viewer)) {
				metagraph.setViewer(viewer);
			}
			if (legend != null && !legend.getViewer().equals(viewer)) {
				legend.setViewer(viewer);
			}
			if (contentsdisplay != null) {
				updateContentsDisplay(viewer);
			}
		}
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		if (e.getInternalFrame() instanceof OVTK2Viewer) {

			// make all table graph editors disappear
			if (editors.containsKey(e.getInternalFrame())) {
				close(editors.remove(e.getInternalFrame()));
			}

			int count = 0;
			for (JInternalFrame jif : desktop.getDesktopPane().getAllFrames()) {
				if (jif instanceof OVTK2Viewer)
					count++;
			}
			// last OVTK2Viewer is closing
			if (count == 1) {
				close(dialogNodes);
				close(dialogEdges);
				close(satellite);
				close(metagraph);
				close(contentsdisplay);
				close(legend);
				legend = null;
				dialogNodes = null;
				dialogEdges = null;
				satellite = null;
				metagraph = null;
				contentsdisplay = null;
			}
		} else if (e.getInternalFrame() instanceof DialogNodes) {
			dialogNodes = null;
		} else if (e.getInternalFrame() instanceof DialogEdges) {
			dialogEdges = null;
		} else if (e.getInternalFrame() instanceof OVTK2Satellite) {
			satellite = null;
		} else if (e.getInternalFrame() instanceof OVTK2MetaGraph) {
			metagraph = null;
		} else if (e.getInternalFrame() instanceof ContentsDisplay) {
			contentsdisplay = null;
		} else if (e.getInternalFrame() instanceof OVTK2Legend) {
			legend = null;
		} else if (e.getInternalFrame() instanceof GraphTableEditor) {
			// remove editors from list associated to viewer
			Iterator<GraphTableEditor> it = editors.values().iterator();
			while (it.hasNext()) {
				if (it.next() == e.getInternalFrame())
					it.remove();
			}
		}
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {

	}
}
