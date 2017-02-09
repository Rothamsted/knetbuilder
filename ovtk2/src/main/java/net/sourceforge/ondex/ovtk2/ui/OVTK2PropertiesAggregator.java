package net.sourceforge.ondex.ovtk2.ui;

import java.awt.Font;
import java.util.Set;

import javax.swing.undo.UndoManager;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeArrows;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeColors;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeLabels;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeDrawPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeLabels;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Defines contract for accessing graph and visual properties central to
 * operation.
 * 
 * @author taubertj
 * 
 */
public interface OVTK2PropertiesAggregator {

	/**
	 * Centre the associates graph
	 */
	public void center();

	/**
	 * Returns transformer for edge arrows.
	 * 
	 * @return ONDEXEdgeArrows
	 */
	public ONDEXEdgeArrows getEdgeArrows();

	/**
	 * Returns transformer for edge colours.
	 * 
	 * @return ONDEXEdgeColors
	 */
	public ONDEXEdgeColors getEdgeColors();

	/**
	 * Return current edge label font.
	 * 
	 * @return the edge font
	 */
	public Font getEdgeFont();

	/**
	 * Returns transformer for edge labels.
	 * 
	 * @return ONDEXEdgeLabels
	 */
	public ONDEXEdgeLabels getEdgeLabels();

	/**
	 * Returns transformer for edge shapes.
	 * 
	 * @return ONDEXEdgeShapes
	 */
	public ONDEXEdgeShapes getEdgeShapes();

	/**
	 * Returns transformer for edge strokes.
	 * 
	 * @return ONDEXEdgeStrokes
	 */
	public ONDEXEdgeStrokes getEdgeStrokes();

	/**
	 * Returns ONDEXMetaGraph, which is a JUNG graph implementation.
	 * 
	 * @return OMDEXMetaGraph
	 */
	public ONDEXMetaGraph getMetaGraph();

	/**
	 * Returns transformer for node colours.
	 * 
	 * @return ONDEXNodeColors
	 */
	public ONDEXNodeFillPaint getNodeColors();

	/**
	 * Returns transformer for node draw colours.
	 * 
	 * @return ONDEXNodeDrawPaint
	 */
	public ONDEXNodeDrawPaint getNodeDrawPaint();

	/**
	 * Returns transformer for node labels.
	 * 
	 * @return ONDEXNodeLabels
	 */
	public ONDEXNodeLabels getNodeLabels();

	/**
	 * Returns transformer for node shapes.
	 * 
	 * @return ONDEXNodeShapes
	 */
	public ONDEXNodeShapes getNodeShapes();

	/**
	 * Returns ONDEXJUNGGraph, which is a JUNG graph implementation.
	 * 
	 * @return ONDEXJUNGGraph
	 */
	public ONDEXJUNGGraph getONDEXJUNGGraph();

	/**
	 * Returns a set with picked ONDEXEdges.
	 * 
	 * @return Set<ONDEXEdge>
	 */
	public Set<ONDEXRelation> getPickedEdges();

	/**
	 * Returns a set with picked ONDEXNodes.
	 * 
	 * @return Set<ONDEXNode>
	 */
	public Set<ONDEXConcept> getPickedNodes();

	/**
	 * Returns title of this viewer
	 * 
	 * @return title
	 */
	public String getTitle();

	/**
	 * Returns the UndoManager for this viewer.
	 * 
	 * @return UndoManager
	 */
	public UndoManager getUndoManager();

	/**
	 * Return current vertex label font.
	 * 
	 * @return the node/vertex font
	 */
	public Font getVertexFont();

	/**
	 * Needs to be reorganized, to something more generic. Because
	 * edu.uci.ics.jung.visualization.VisualizationViewer and
	 * edu.uci.ics.jung3d.visualization.VisualizationViewer are incompatible.
	 * 
	 * @return VisualizationViewer<ONDEXNode, ONDEXEdge>
	 */
	public VisualizationViewer<ONDEXConcept, ONDEXRelation> getVisualizationViewer();

	/**
	 * Returns whether or not antialiased painting is used.
	 * 
	 * @return antialiased painting enabled
	 */
	public boolean isAntiAliased();

	/**
	 * @return the destroy
	 */
	public boolean isDestroy();

	/**
	 * Returns whether or not relayout on resize is used.
	 * 
	 * @return relayout enabled
	 */
	public boolean isRelayoutOnResize();

	/**
	 * Returns whether or not edge labels are shown.
	 * 
	 * @return edge labels shown
	 */
	public boolean isShowEdgeLabels();

	/**
	 * Returns whether or not node labels are shown.
	 * 
	 * @return node labels shown
	 */
	public boolean isShowNodeLabels();

	/**
	 * Whether or not this viewer is currently visible
	 * 
	 * @return visible?
	 */
	public boolean isVisible();

	/**
	 * Sets whether or not antialiased painting sould be used.
	 * 
	 * @param antialiased
	 *            antialiased painting enabled
	 */
	public void setAntiAliased(boolean antialiased);

	/**
	 * Method to override current status when using LoadAppearance.
	 * 
	 * @param show
	 *            labels shown
	 */
	public void setShowEdgeLabels(boolean show);

	/**
	 * Method to override current status when using LoadAppearance.
	 * 
	 * @param show
	 *            labels shown
	 */
	public void setShowNodeLabels(boolean show);

	/**
	 * Sets the title of this viewer
	 * 
	 * @param title
	 *            title to use
	 */
	public void setTitle(String title);

	/**
	 * Update with current changes.
	 * 
	 * @param entity
	 *            ONDEXEntity or null
	 */
	public void updateViewer(ONDEXEntity entity);

}
