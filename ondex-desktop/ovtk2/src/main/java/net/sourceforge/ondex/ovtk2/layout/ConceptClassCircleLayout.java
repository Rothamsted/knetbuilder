package net.sourceforge.ondex.ovtk2.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * Layouter which places circles for each concept class on one big circle.
 * 
 * @author taubertj
 * 
 */
public class ConceptClassCircleLayout extends OVTK2Layouter implements Monitorable {

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
	 * Constructor sets OVTK2PropertiesAggregator.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public ConceptClassCircleLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
	}

	@Override
	public void reset() {
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uci.ics.jung.visualization.layout.AbstractLayout#setSize(java.awt
	 * .Dimension)
	 */
	@Override
	public void setSize(Dimension d) {
		super.setSize(d);
		initialize();
	}

	@Override
	public void initialize() {
		initialize(false);
	}

	/**
	 * Layouts also invisible nodes
	 * 
	 * @param all
	 */
	public void initialize(boolean all) {
		cancelled = false;
		progress = 0;
		state = Monitorable.STATE_IDLE;

		Dimension d = getSize();
		ONDEXJUNGGraph graph = (ONDEXJUNGGraph) this.getGraph();
		if (graph != null && d != null) {

			double height = d.getHeight();
			double width = d.getWidth();

			// number of small circles, one for each concept class
			int count_circles = 0;

			Map<ConceptClass, List<ONDEXConcept>> cc2nodes = new HashMap<ConceptClass, List<ONDEXConcept>>();

			// decide whether to layout all or only visible vertices
			Collection<ONDEXConcept> vertices;
			if (all) {
				vertices = graph.getConcepts();
			} else {
				vertices = graph.getVertices();
			}

			// get nodes per concept classes
			state = "sorting nodes";
			for (ONDEXConcept ac : vertices) {
				ConceptClass cc = ac.getOfType();
				if (!cc2nodes.containsKey(cc)) {
					count_circles++;
					cc2nodes.put(cc, new ArrayList<ONDEXConcept>());
				}
				cc2nodes.get(cc).add(ac);
				progress++;
				if (cancelled)
					return;
			}

			state = "calculating node positions";
			if (count_circles > 1) {

				// maximum allowed space
				double total = 0.45 * (height < width ? height : width);

				// calculate big radius
				double radius = total / (2.0 / count_circles + 1.0);

				// subtract some bit for node size overlap
				double r = 0.90 * (total - radius);

				// positioning angle of small circles
				double bigangle = (2.0 * Math.PI) / count_circles;

				int currentCircle = -1;
				for (ConceptClass cc : cc2nodes.keySet()) {

					// get next concepts for concept class
					List<ONDEXConcept> nodes = cc2nodes.get(cc);

					// start new circle
					currentCircle++;

					// get position on outer circle, center outer circle
					double currentX = width / 2 + radius * Math.cos(currentCircle * bigangle);
					double currentY = height / 2 + radius * Math.sin(currentCircle * bigangle);

					// angle on small circle
					double angle = (2 * Math.PI) / nodes.size();

					// place all concepts of concept class
					Iterator<ONDEXConcept> it_c = nodes.iterator();
					for (int i = 0; it_c.hasNext(); i++) {
						ONDEXConcept node = it_c.next();
						Point2D coord = transform(node);
						coord.setLocation(currentX + r * Math.cos(i * angle), currentY + r * Math.sin(i * angle));
						progress++;
						if (cancelled)
							return;
					}
				}
			} else {
				// calculate big radius
				double radius = 0.45 * (height < width ? height : width);

				// layout all concepts on one big circle
				ONDEXConcept[] nodes = vertices.toArray(new ONDEXConcept[0]);

				for (int i = 0; i < nodes.length; i++) {
					Point2D coord = transform(nodes[i]);

					double angle = (2 * Math.PI * i) / nodes.length;

					coord.setLocation(Math.cos(angle) * radius + width / 2, Math.sin(angle) * radius + height / 2);
					progress++;
					if (cancelled)
						return;
				}
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
		return 2 * getGraph().getVertexCount();
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
}
