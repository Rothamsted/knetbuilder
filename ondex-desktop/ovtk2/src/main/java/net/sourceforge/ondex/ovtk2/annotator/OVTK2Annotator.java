package net.sourceforge.ondex.ovtk2.annotator;

import javax.swing.JPanel;

import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

/**
 * Class which all annotators inherit from
 * 
 * @author taubertj
 * 
 */
public abstract class OVTK2Annotator extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Current graph to work on
	 */
	protected ONDEXJUNGGraph graph;

	/**
	 * Current viewer to work on
	 */
	protected OVTK2PropertiesAggregator viewer;

	/**
	 * Abstract Constructor sets viewer and graph Objects
	 * 
	 * @param viewer
	 *            OVTK2Viewer to use
	 */
	public OVTK2Annotator(OVTK2PropertiesAggregator viewer) {
		this.graph = viewer.getONDEXJUNGGraph();
		this.viewer = viewer;
	}

	/**
	 * The name to display for the annotator
	 * 
	 */
	public abstract String getName();

	/**
	 * Determines whether or not this annotator has been used
	 * 
	 * @return
	 */
	public abstract boolean hasBeenUsed();
}
