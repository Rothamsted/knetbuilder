package net.sourceforge.ondex.ovtk2.layout;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Vertically flips the selected nodes.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class FlipLayout extends OVTK2Layouter {

	// ####FIELDS####

	/**
	 * debug flag.
	 */
	private static final boolean DEBUG = false;

	/**
	 * old layouter.
	 */
	private Layout<ONDEXConcept, ONDEXRelation> oldLayouter;

	private OVTK2PropertiesAggregator aViewer;

	// ####CONSTRUCTOR####

	/**
	 * constructor
	 */
	public FlipLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		aViewer = viewer;
		oldLayouter = viewer.getVisualizationViewer().getGraphLayout();
	}

	// ####METHODS####

	/**
	 * gives the options panel.
	 */
	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("No options available."));
		return panel;
	}

	/**
	 * runs layouter
	 * 
	 * @see edu.uci.ics.jung.algorithms.layout.Layout#initialize()
	 */
	@Override
	public void initialize() {
		Collection<ONDEXConcept> nodes = aViewer.getPickedNodes();
		if (nodes == null || nodes.size() == 0)
			nodes = this.getGraph().getVertices();
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (ONDEXConcept node : nodes) {
			Point2D coord = oldLayouter.transform(node);
			if (coord.getY() < min)
				min = coord.getY();
			if (coord.getY() > max)
				max = coord.getY();
		}
		if (DEBUG)
			System.out.println("min = " + min + "\tmax = " + max);

		if (nodes.size() < getGraph().getVertices().size()) {
			Collection<ONDEXConcept> rest = new HashSet<ONDEXConcept>();
			rest.addAll(getGraph().getVertices());
			rest.removeAll(nodes);
			for (ONDEXConcept node : rest) {
				Point2D coord = oldLayouter.transform(node);
				double y = coord.getY();
				double x = coord.getX();
				Point2D coordNew = transform(node);
				coordNew.setLocation(x, y);
			}
		}

		for (ONDEXConcept node : nodes) {
			Point2D coord = oldLayouter.transform(node);
			double y = coord.getY();
			double x = coord.getX();
			if (DEBUG)
				System.out.println("x = " + x + "\ty = " + y);
			y = max - (y - min);
			Point2D coordNew = transform(node);
			coordNew.setLocation(x, y);
		}
	}

	/**
	 * reruns layouter
	 * 
	 * @see edu.uci.ics.jung.algorithms.layout.Layout#reset()
	 */
	@Override
	public void reset() {
		initialize();
	}
	
	@Override 
	public void cleanUp(){
		oldLayouter = null;
	}

}
