package net.sourceforge.ondex.ovtk2.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2MetaGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import net.sourceforge.ondex.tools.threading.monitoring.MonitoringToolKit;

/**
 * This class is a synchronizes the ONDEXGraph with the Jung graph. Its run()
 * method deletes all elements from the ONDEX Graph that are hidden in the Jung
 * graph. It is also monitorable by the OVTKProgressMonitor.
 * 
 * @author Jochen Weile, B.Sc., taubertj
 * 
 */
public class GraphSynchronizer implements Monitorable {

	// ####FIELDS####

	/**
	 * the maximal progress ( = number of concepts + number of relations).
	 */
	private int max_progress;

	/**
	 * The minimal progress ( = 0).
	 */
	private int min_progress = 0;

	/**
	 * The current progress, increases whenever an element was scanned.
	 */
	private int progress = 0;

	/**
	 * The process state.
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * the ONDEX graph.
	 */
	private ONDEXJUNGGraph graph;

	/**
	 * Concepts to be deleted.
	 */
	private Set<ONDEXConcept> concepts;

	/**
	 * Relations to be deleted.
	 */
	private Set<ONDEXRelation> relations;

	/**
	 * are there any exceptions?
	 */
	private Throwable caught = null;

	/**
	 * viewer to work with
	 */
	private OVTK2PropertiesAggregator v;

	// ####CONSTRUCTOR####

	/**
	 * The constructor just initialises all fields and then minimises the graph
	 * window to prevent flickering.
	 */
	public GraphSynchronizer(OVTK2PropertiesAggregator v) {
		this.v = v;
		graph = v.getONDEXJUNGGraph();

		concepts = new HashSet<ONDEXConcept>();
		relations = new HashSet<ONDEXRelation>();
		determineMax();

		// minimise graph first
		try {
			if (v instanceof OVTK2Viewer)
				((OVTK2Viewer) v).setIcon(true);

			// also hide meta graph away
			OVTK2MetaGraph meta = OVTK2Desktop.getDesktopResources().getSelectedMetagraph();
			if (meta != null)
				meta.setIcon(true);
		} catch (PropertyVetoException e) {
		}
	}

	// ####METHODS####

	/**
	 * performs the actual process while reporting continuously to the monitor
	 * by updating the readable fields "progress" and "state".
	 */
	public void run() {
		try {
			state = "running";
			long before = System.currentTimeMillis();

			// first remove invisible relations
			for (ONDEXRelation r : relations) {
				graph.deleteRelation(r.getId());
				progress++;
				if (progress % 5 == 4)
					state = MonitoringToolKit.calculateWaitingTime(before, this);
			}

			// second remove invisible concepts
			for (ONDEXConcept c : concepts) {
				graph.deleteConcept(c.getId());
				progress++;
				if (progress % 5 == 4)
					state = MonitoringToolKit.calculateWaitingTime(before, this);
			}

			// output total time it took
			System.out.println("Graph synchronisation took: " + (System.currentTimeMillis() - before) + "msec.");

			state = Monitorable.STATE_TERMINAL;

			// refresh visualisation
			if (v instanceof OVTK2Viewer)
				((ActionListener) v).actionPerformed(new ActionEvent(this, 0, "refresh"));

			// show meta graph again
			OVTK2MetaGraph meta = OVTK2Desktop.getDesktopResources().getSelectedMetagraph();
			if (meta != null)
				meta.setIcon(false);
		} catch (Throwable t) {
			caught = t;
		}
	}

	/**
	 * Get the number of invisible concepts and relations.
	 */
	private void determineMax() {
		state = "running";
		for (ONDEXRelation r : graph.getRelations()) {
			if (!graph.isVisible(r)) {
				relations.add(r);
			}
		}
		for (ONDEXConcept c : graph.getConcepts()) {
			if (!graph.isVisible(c)) {
				concepts.add(c);
			}
		}
		state = Monitorable.STATE_IDLE;
		max_progress = relations.size() + concepts.size();
	}

	@Override
	public int getMaxProgress() {
		return max_progress;
	}

	@Override
	public int getMinProgress() {
		return min_progress;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public void setCancelled(boolean c) {
		// this process is not abort-able
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	@Override
	public Throwable getUncaughtException() {
		return caught;
	}

	@Override
	public boolean isAbortable() {
		return false;
	}
}
