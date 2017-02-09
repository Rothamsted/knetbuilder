package net.sourceforge.ondex.ovtk2.filter;

import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.GraphSynchronizer;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;

/**
 * Provides clean-up method for filter displayed in an internal frame.
 * 
 * @author hindlem
 * 
 */
public class OVTK2FilterInternalFrameListener implements InternalFrameListener {

	private OVTK2Filter filter;

	/**
	 * Constructor.
	 * 
	 * @param filter
	 *            the OVTK2Filter associated with this frame
	 */
	public OVTK2FilterInternalFrameListener(OVTK2Filter filter) {
		this.filter = filter;
	}

	public void internalFrameActivated(InternalFrameEvent e) {
	}

	public void internalFrameClosed(InternalFrameEvent e) {
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		// if the graph frame still exists
		if (filter.viewer != null && filter.viewer.isVisible()) {

			OVTK2Viewer viewer = filter.viewer;

			// only ask user when filter has been used
			if (!filter.hasBeenUsed())
				return;

			// dont ask when Close All
			if (viewer.isDestroy())
				return;

			int option;

			if (Boolean.valueOf(Config.config.getProperty("FilterClose.Set"))) {

				// use pre-defined close behaviour
				option = Integer.parseInt(Config.config.getProperty("FilterClose.Option"));

			} else {

				// ask user what to do
				Object[] options = { Config.language.getProperty("Filter.Save.Changes.Keep"), Config.language.getProperty("Filter.Save.Changes.KeepApply"), Config.language.getProperty("Filter.Save.Changes.Discard") };
				option = JOptionPane.showOptionDialog(viewer.getDesktopPane(), Config.language.getProperty("Filter.Save.Changes.Text"), Config.language.getProperty("Filter.Save.Changes.Title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			}

			switch (option) {
			case JOptionPane.NO_OPTION:
				// sync graphs by deleting invisible concepts/relations
				final GraphSynchronizer gs = new GraphSynchronizer(viewer);

				java.lang.Thread t = new Thread("graph synchronization") {
					public void run() {
						OVTK2Desktop.getInstance().setRunningProcess("GraphSynchronization");
						gs.run();
						OVTK2Desktop.getInstance().setRunningProcess("none");
					}
				};
				t.start();

				OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Graph Synchronisation", gs);
				break;
			case JOptionPane.YES_OPTION:
				// do nothing
				break;
			case JOptionPane.CANCEL_OPTION:
				// get current JUNG graph
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();

				// set all concepts from ONDEX graph visible in JUNG graph
				for (ONDEXConcept c : graph.getConcepts()) {
					graph.setVisibility(c, true);
				}

				// set all relations from ONDEX graph visible in JUNG graph
				for (ONDEXRelation r : graph.getRelations()) {
					graph.setVisibility(r, true);
				}
				break;
			}
		}
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	public void internalFrameIconified(InternalFrameEvent e) {
	}

	public void internalFrameOpened(InternalFrameEvent e) {
	}

}
