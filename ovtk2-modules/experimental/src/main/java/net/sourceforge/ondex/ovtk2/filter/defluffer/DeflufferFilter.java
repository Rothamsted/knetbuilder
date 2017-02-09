package net.sourceforge.ondex.ovtk2.filter.defluffer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;

/**
 * Hide all single-degree nodes, and those nodes which become single-degree once
 * these are hidden (recursively).
 * 
 * @author Matthew Pocock
 */
public class DeflufferFilter extends OVTK2Filter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3525454670514138524L;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	public DeflufferFilter(final OVTK2Viewer viewer) {
		super(viewer);

		setLayout(new BorderLayout());

		add(new JLabel(
				"<html>Hide all single-degree nodes, and those nodes<br>"
						+ "which become single-degree once these are hidden (recursively).</html>"),
				BorderLayout.CENTER);

		add(new JButton(new AbstractAction("Filter graph") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -126019741444216641L;

			@Override
			public void actionPerformed(ActionEvent e) {
				StateEdit edit = new StateEdit(new VisibilityUndo(viewer
						.getONDEXJUNGGraph()), Config.language
						.getProperty("Name.Menu.Filter.Defluff"));
				OVTK2Desktop desktop = OVTK2Desktop.getInstance();
				desktop.setRunningProcess(Config.language
						.getProperty("Name.Menu.Filter.Defluff"));

				Set<ONDEXConcept> visBS = new HashSet<ONDEXConcept>();
				ONDEXJUNGGraph jungGraph = viewer.getONDEXJUNGGraph();
				for (ONDEXConcept on : jungGraph.getVertices()) {
					visBS.add(on);
				}
				visBS.retainAll(graph.getConcepts());

				for (boolean tryAgain = true; tryAgain;) {
					tryAgain = false;
					for (Iterator<ONDEXConcept> iterator = visBS.iterator(); iterator
							.hasNext();) {
						ONDEXConcept c = iterator.next();
						int degree = jungGraph.getIncidentEdges(c).size();
						if (degree <= 1) {
							jungGraph.setVisibility(c, false);
							iterator.remove();
							tryAgain = true;
						}
					}
				}

				// propagate change to viewer
				viewer.getVisualizationViewer().getModel().fireStateChanged();

				edit.end();
				viewer.getUndoManager().addEdit(edit);
				desktop.getOVTK2Menu().updateUndoRedo(viewer);
				desktop.notifyTerminationOfProcess();

				used = true;
			}
		}), BorderLayout.SOUTH);
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.Defluff");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
