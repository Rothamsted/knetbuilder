package net.sourceforge.ondex.ovtk2.filter;

import javax.swing.JPanel;

import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;

/**
 * Class to wrap interface to filter functionality.
 * 
 * @author taubertj
 * @version 18.03.2008
 */
public abstract class OVTK2Filter extends JPanel {

	private static final long serialVersionUID = 1L;

	// current active viewer
	protected OVTK2Viewer viewer;

	// JUNG ONDEX graph
	protected ONDEXJUNGGraph graph;

	/**
	 * Constructor sets viewer and graph protected Objects.
	 * 
	 * @param viewer
	 *            active OVTK2Viewer to use
	 */
	public OVTK2Filter(OVTK2Viewer viewer) {
		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();
	}

	/**
	 * Returns the name of this filter.
	 * 
	 * @return name of filter
	 */
	public abstract String getName();

	/**
	 * Determines whether or not this filter has been used
	 * 
	 * @return
	 */
	public abstract boolean hasBeenUsed();

}
