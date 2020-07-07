package net.sourceforge.ondex.ovtk2.ui.menu.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop.Position;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptLabel;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogMerging;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogSettings;
import net.sourceforge.ondex.ovtk2.ui.dialog.FontChooserDialog;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.GraphSynchronizer;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;

/**
 * Listens to action events specific to the edit menu.
 * 
 * @author taubertj
 * 
 */
public class EditMenuAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ae) {

		String cmd = ae.getActionCommand();
		final OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();
		final OVTK2Viewer viewer = (OVTK2Viewer) resources.getSelectedViewer();

		// for coping with plug-in Attribute data types
		try {
			Thread.currentThread().setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e);
		} catch (MalformedURLException e) {
			ErrorDialog.show(e);
		}

		// perform undo
		if (cmd.equals("undo")) {
			if (viewer != null) {
				if (viewer.getUndoManager().canUndo()) {
					viewer.getUndoManager().undo();
					desktop.getOVTK2Menu().updateUndoRedo(viewer);
					viewer.getVisualizationViewer().repaint();
				}
			}
		}

		// perform undo all
		else if (cmd.equals("undoall")) {
			if (viewer != null) {
				while (viewer.getUndoManager().canUndo()) {
					viewer.getUndoManager().undo();
				}
				desktop.getOVTK2Menu().updateUndoRedo(viewer);
				viewer.getVisualizationViewer().repaint();
			}
		}

		// perform redo
		else if (cmd.equals("redo")) {
			if (viewer != null) {
				if (viewer.getUndoManager().canRedo()) {
					viewer.getUndoManager().redo();
					desktop.getOVTK2Menu().updateUndoRedo(viewer);
					viewer.getVisualizationViewer().repaint();
				}
			}
		}

		// perform redo all
		else if (cmd.equals("redoall")) {
			if (viewer != null) {
				while (viewer.getUndoManager().canRedo()) {
					viewer.getUndoManager().redo();
				}
				desktop.getOVTK2Menu().updateUndoRedo(viewer);
				viewer.getVisualizationViewer().repaint();
			}
		}

		// reverts visibility to last state
		else if (cmd.equals("revert")) {
			if (viewer != null) {
				viewer.getONDEXJUNGGraph().restoreLastState();
				viewer.getVisualizationViewer().repaint();
			}
		}

		// change node font
		else if (cmd.equals("Nfont")) {
			if (viewer != null)
				viewer.setVertexFont(FontChooserDialog.showDialog(desktop.getMainFrame(), Config.language.getProperty("Menu.Edit.NodeFont"), viewer.getVertexFont()));
		}

		// change edge font
		else if (cmd.equals("Efont")) {
			if (viewer != null)
				viewer.setEdgeFont(FontChooserDialog.showDialog(desktop.getMainFrame(), Config.language.getProperty("Menu.Edit.EdgeFont"), viewer.getEdgeFont()));
		}

		// how to compose concept label
		else if (cmd.equals("ConceptLabel")) {
			if (viewer != null) {
				DialogConceptLabel dialog = new DialogConceptLabel(viewer);
				desktop.display(dialog, Position.centered);
			}
		}

		// sync graph
		else if (cmd.equals("sync")) {
			if (viewer != null) {
				int option = JOptionPane.showInternalConfirmDialog(desktop.getDesktopPane(), Config.language.getProperty("Dialog.Sync.Warning.Text"), Config.language.getProperty("Dialog.Sync.Warning.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					return;
				}

				if (!desktop.getRunningProcess().equals("GraphSynchronization")) {
					// OVTK-202
					String title = viewer.getTitle();
					if (!title.endsWith(" (modified)")) {
						viewer.setTitle(title + " (modified)");
						viewer.setName(viewer.getName() + " (modified)");
					}

					// sync graphs by deleting invisible concepts/relations
					desktop.setRunningProcess("GraphSynchronization");
					final GraphSynchronizer gs = new GraphSynchronizer(viewer);

					java.lang.Thread t = new Thread("graph synchronization") {
						public void run() {
							gs.run();
							desktop.setRunningProcess("none");
							viewer.getMetaGraph().updateMetaData();
						}
					};

					// for coping with plug-in Attribute data types
					try {
						t.setContextClassLoader(OVTK2PluginLoader.getInstance().ucl);
					} catch (FileNotFoundException e) {
						ErrorDialog.show(e);
					} catch (MalformedURLException e) {
						ErrorDialog.show(e);
					}

					// start thread and monitoring
					t.start();
					OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Graph Synchronisation", gs);
				} else {
					JOptionPane.showConfirmDialog(desktop.getDesktopPane(), "Only one Graph Synchronisation can run at anyone time.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		// show settings dialog
		else if (cmd.equals("settings")) {
			DialogSettings settings = new DialogSettings();
			desktop.display(settings, Position.centered);
		}

		// show merge dialog
		else if (cmd.equals("merge")) {
			if (viewer != null) {
				DialogMerging merging = new DialogMerging(viewer, null);
				desktop.display(merging, Position.centered);
			}
		}
	}
}
