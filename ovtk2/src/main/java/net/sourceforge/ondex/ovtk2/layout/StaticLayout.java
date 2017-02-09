package net.sourceforge.ondex.ovtk2.layout;

import java.awt.geom.Point2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Layouter which places the nodes according to their Attribute graphicalX/Y.
 * 
 * @author taubertj
 */
public class StaticLayout extends OVTK2Layouter implements Monitorable {

	/**
	 * Current progress made for Monitorable
	 */
	private int progress = 0;

	/**
	 * Current state for Monitorable
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * If the process gets cancelled
	 */
	private boolean cancelled = false;

	/**
	 * old layouter.
	 */
	private Layout<ONDEXConcept, ONDEXRelation> oldLayouter;

	/**
	 * debug flag.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Constructor sets OVTK2PropertiesAggregator.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public StaticLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		oldLayouter = viewer.getVisualizationViewer().getGraphLayout();
	}

	public void reset() {
		initialize();
	}

	public void initialize() {

		cancelled = false;
		progress = 0;
		state = Monitorable.STATE_IDLE;

		ONDEXJUNGGraph graph = (ONDEXJUNGGraph) this.getGraph();
		if (graph != null) {

			// check for attribute names
			state = "getting meta data";
			ONDEXGraphMetaData meta = graph.getMetaData();
			AttributeName attrGraphicalX, attrGraphicalY, attrVisible;
			if ((attrGraphicalX = meta.getAttributeName("graphicalX")) == null)
				attrGraphicalX = meta.getFactory().createAttributeName("graphicalX", Double.class);
			if ((attrGraphicalY = meta.getAttributeName("graphicalY")) == null)
				attrGraphicalY = meta.getFactory().createAttributeName("graphicalY", Double.class);
			if ((attrVisible = meta.getAttributeName("visible")) == null)
				attrVisible = meta.getFactory().createAttributeName("visible", Boolean.class);

			// if meta data incomplete return
			if (attrVisible == null || attrGraphicalX == null || attrGraphicalY == null)
				return;
			progress++;
			if (cancelled)
				return;

			state = "calculating node positions";
			for (ONDEXConcept c : graph.getVertices()) {

				// check visibility of node, default is true
				Attribute visible = c.getAttribute(attrVisible);
				if (visible == null || visible.getValue().equals(Boolean.TRUE)) {

					// get possible preset coordinates
					Point2D coord = oldLayouter.transform(c);
					double x = coord.getX();
					double y = coord.getY();
					if (DEBUG)
						System.err.println("old coordinate:\t" + x + "\t" + y);

					// get X from Attribute
					Attribute graphicalX = c.getAttribute(attrGraphicalX);
					if (graphicalX != null)
						x = (Double) graphicalX.getValue();

					// get Y from Attribute
					Attribute graphicalY = c.getAttribute(attrGraphicalY);
					if (graphicalY != null)
						y = (Double) graphicalY.getValue();

					// set coordinates
					this.transform(c).setLocation(x, y);
				} else {
					// hide node
					graph.setVisibility(c, false);
				}
				progress++;
				if (cancelled)
					return;
			}
		}
		state = Monitorable.STATE_TERMINAL;
	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("No options available."));
		return panel;
	}

	@Override
	public int getMaxProgress() {
		return getGraph().getVertexCount() + 1;
	}

	@Override
	public int getMinProgress() {
		return 0;
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
	public Throwable getUncaughtException() {
		return null;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

	@Override
	public void cleanUp() {
		oldLayouter = null;
	}
}
