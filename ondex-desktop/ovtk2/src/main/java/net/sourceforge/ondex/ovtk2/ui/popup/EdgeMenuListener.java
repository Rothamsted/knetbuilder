package net.sourceforge.ondex.ovtk2.ui.popup;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * An interface for menu items that are interested in knowning the currently
 * selected edge and its visualization component context. Used with
 * PopupVertexEdgeMenuMousePlugin.
 * 
 * @author Dr. Greg M. Bernstein
 */
public interface EdgeMenuListener<V, E> {

	/**
	 * Used to set the edge and visulization component.
	 * 
	 * @param egde
	 *            E
	 * @param visComp
	 *            VisualizationViewer<V, E>
	 */
	public void setEdgeAndView(E egde, VisualizationViewer<V, E> visComp);

}
