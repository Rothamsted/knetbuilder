package net.sourceforge.ondex.ovtk2.ui.console;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2ResourceAssesor;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.scripting.ProcessingCheckpoint;

/**
 * This class manages the link between the Ondex graph and visualisation to
 * prevent the sync issues and speed up graph manipulation via the console.
 * 
 * @author lysenkoa
 * 
 */
public class VisualizationHandler implements ProcessingCheckpoint {
	public VisualizationHandler() {
	}

	public static final Map<OVTK2PropertiesAggregator, ONDEXJUNGGraph> graphs = new HashMap<OVTK2PropertiesAggregator, ONDEXJUNGGraph>();

	public synchronized void processingStarted() {
		try {
			OVTK2PropertiesAggregator v = OVTK2Desktop.getDesktopResources().getSelectedViewer();
			graphs.put(v, v.getONDEXJUNGGraph());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.ondex.ovtk2.ui.scripting.ProcessingCheckpoint#
	 * processingFinished()
	 */
	public synchronized void processingFinished() {
		try {

			for (Entry<OVTK2PropertiesAggregator, ONDEXJUNGGraph> ent : graphs.entrySet()) {

				OVTK2ResourceAssesor resources = OVTK2Desktop.getDesktopResources();

				// put graph into viewer
				// OVTK2PropertiesAggregator viewer =
				// DesktopUtils.initViewer(ent.getValue());
				OVTK2PropertiesAggregator viewer = ent.getKey();

				// set viewer as active viewer
				resources.setSelectedViewer(viewer);

				// decide when to display viewer immediately
				if (Math.max(ent.getValue().getConcepts().size(), ent.getValue().getRelations().size()) < 2000) {

					// we can display everything here
					viewer.getONDEXJUNGGraph().setEverythingVisible();

					// make sure layout is set
					// VisualisationUtils.relayout(viewer);
				}

				// update all visual attributes of graph
				viewer.updateViewer(null);
				if (viewer instanceof OVTK2Viewer) {
					((OVTK2Viewer) viewer).getMetaGraph().updateMetaData();
					try {
						((OVTK2Viewer) viewer).setSelected(true);
					} catch (PropertyVetoException e) {
						ErrorDialog.show(e);
					}
				}

				// update UI
				viewer.getVisualizationViewer().fireStateChanged();
				viewer.getVisualizationViewer().repaint();

				// hack to show meta graph
				// JCheckBoxMenuItem item = new JCheckBoxMenuItem();
				// item.setSelected(true);
				// desktop.actionPerformed(new ActionEvent(item,
				// ActionEvent.ACTION_PERFORMED, "metagraph"));
				// ((ActionListener)v).actionPerformed(new
				// ActionEvent(this,0,"refresh"));
				// v.getJUNGGraph().addActionListener((ActionListener)v);
			}
			graphs.clear();
			// OVTK2PropertiesAggregator v =
			// OVTK2Desktop.getInstance().getDesktopResources().getSelectedViewer();
			// v.setMaximizable(true);
			// v.setIconifiable(true);
			// if(state.containsKey(v))
			// v.setIcon(state.remove(v));
			// OVTK-206
			// VisualisationUtils.relayout(v);
			// v.center();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Makes the select subset visible in the graph
	 */
	public void setSelectedSubset(ONDEXGraph graph, Set<ONDEXConcept> cs, Set<ONDEXRelation> rs) {
		Set<OVTK2PropertiesAggregator> vs = new HashSet<OVTK2PropertiesAggregator>();
		for (Entry<OVTK2PropertiesAggregator, ONDEXJUNGGraph> ent : graphs.entrySet()) {
			if (ent.getKey().getONDEXJUNGGraph().equals(graph)) {
				vs.add(ent.getKey());
			}
		}
		for (OVTK2PropertiesAggregator viewer : vs) {
			StateEdit edit = null;
			if (viewer instanceof OVTK2PropertiesAggregator) {
				edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), Config.language.getProperty("Undo.HideSelection"));
				viewer.getUndoManager().addEdit(edit);
				OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(viewer);
			}
			ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();

			// hide nodes next
			for (ONDEXConcept c : cs) {
				jung.setVisibility(c, true);
			}

			// hide edges first
			for (ONDEXRelation r : rs) {
				jung.setVisibility(r, true);
			}

			// update viewer
			viewer.getVisualizationViewer().getModel().fireStateChanged();
			if (edit != null)
				edit.end();
		}
	}
}
