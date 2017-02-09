package net.sourceforge.ondex.ovtk2.layout;

import javax.swing.JPanel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Abstract class for all OVTK2 Layouter.
 * 
 * @author taubertj
 * 
 */
public abstract class OVTK2Layouter extends AbstractLayout<ONDEXConcept, ONDEXRelation> {

	// current VisualizationViewer<ONDEXConcept, ONDEXRelation>
	protected VisualizationViewer<ONDEXConcept, ONDEXRelation> viewer = null;

	/**
	 * Constructor sets internal variables from given OVTK2PropertiesAggregator.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public OVTK2Layouter(OVTK2PropertiesAggregator viewer) {
		super(viewer.getONDEXJUNGGraph());
		this.viewer = viewer.getVisualizationViewer();
	}

	/**
	 * Prevent changing the graph belonging to this layout.
	 * 
	 * @param graph
	 *            Graph<ONDEXConcept, ONDEXRelation>
	 */
	public void setGraph(Graph<ONDEXConcept, ONDEXRelation> graph) {
		throw new IllegalArgumentException("Operation not supported");
	}

	/**
	 * Setting a new viewer is allowed.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public void setViewer(OVTK2PropertiesAggregator viewer) {
		super.setGraph(viewer.getONDEXJUNGGraph());
		this.viewer = viewer.getVisualizationViewer();
	}

	/**
	 * Returns layout option producer for gadget.
	 * 
	 * @return JPanel
	 */
	public abstract JPanel getOptionPanel();

	/**
	 * Clean up of layout resources to avoid memory leaks
	 */
	public void cleanUp() {

	}
}
