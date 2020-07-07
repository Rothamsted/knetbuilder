package net.sourceforge.ondex.ovtk2.ui.popup;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Used to indicate that this class wishes to be told of a selected vertex along
 * with its visualization component context. Note that the VisualizationViewer
 * has full access to the graph and layout.
 * 
 * @author Dr. Greg M. Bernstein
 */
public interface VertexMenuListener<V, E> {

	/**
	 * Used to set the vertex and visulization component.
	 * 
	 * @param vertex
	 *            V
	 * @param visComp
	 *            VisualizationViewer<V,E>
	 */
	public void setVertexAndView(V vertex, VisualizationViewer<V, E> visComp);
}
