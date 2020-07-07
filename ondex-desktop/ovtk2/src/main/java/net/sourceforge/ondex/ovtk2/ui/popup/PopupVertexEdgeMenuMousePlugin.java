package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

/**
 * A GraphMousePlugin that brings up distinct popup menus when an edge or vertex
 * is appropriately clicked in a graph. If these menus contain components that
 * implement either the EdgeMenuListener or VertexMenuListener then the
 * corresponding interface methods will be called prior to the display of the
 * menus (so that they can display context sensitive information for the edge or
 * vertex).
 * 
 * @author Dr. Greg M. Bernstein
 */
public class PopupVertexEdgeMenuMousePlugin<V, E> extends AbstractPopupGraphMousePlugin {

	// both popups
	private JPopupMenu edgePopup, vertexPopup;

	/**
	 * Creates a new instance of PopupVertexEdgeMenuMousePlugin
	 * 
	 */
	public PopupVertexEdgeMenuMousePlugin() {
		this(MouseEvent.BUTTON3_MASK);
	}

	/**
	 * Creates a new instance of PopupVertexEdgeMenuMousePlugin
	 * 
	 * @param modifiers
	 *            mouse event modifiers see the jung visualization Event class.
	 */
	public PopupVertexEdgeMenuMousePlugin(int modifiers) {
		super(modifiers);
	}

	/**
	 * Getter for the edge popup.
	 * 
	 * @return
	 */
	public JPopupMenu getEdgePopup() {
		return edgePopup;
	}

	/**
	 * Getter for the vertex popup.
	 * 
	 * @return
	 */
	public JPopupMenu getVertexPopup() {
		return vertexPopup;
	}

	/**
	 * Implementation of the AbstractPopupGraphMousePlugin method. This is where
	 * the work gets done. You shouldn't have to modify unless you really want
	 * to...
	 * 
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	protected void handlePopup(MouseEvent e) {
		VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		Point p = e.getPoint();

		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
		if (pickSupport != null) {
			final V v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
			if (v != null) {
				// System.out.println("Vertex " + v + " was right clicked");
				updateVertexMenu(v, vv, p);
				vertexPopup.show(vv, e.getX(), e.getY());
			} else {
				final E edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
				if (edge != null) {
					// System.out.println("Edge " + edge + " was right
					// clicked");
					updateEdgeMenu(edge, vv, p);
					edgePopup.show(vv, e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * Setter for the Edge popup.
	 * 
	 * @param edgePopup
	 */
	public void setEdgePopup(JPopupMenu edgePopup) {
		this.edgePopup = edgePopup;
	}

	/**
	 * Setter for the vertex popup.
	 * 
	 * @param vertexPopup
	 */
	public void setVertexPopup(JPopupMenu vertexPopup) {
		this.vertexPopup = vertexPopup;
	}

	/**
	 * Keep edge menu updated.
	 * 
	 * @param edge
	 *            E
	 * @param vv
	 *            VisualizationViewer<V,E>
	 * @param point
	 *            Point2D
	 */
	@SuppressWarnings("unchecked")
	private void updateEdgeMenu(E edge, VisualizationViewer<V, E> vv, Point point) {
		if (edgePopup == null)
			return;
		if (edgePopup instanceof EdgeMenuListener) {
			((EdgeMenuListener<V, E>) edgePopup).setEdgeAndView(edge, vv);
		}
		if (edgePopup instanceof MenuPointListener) {
			((MenuPointListener) edgePopup).setPoint(point);
		}
		Component[] menuComps = edgePopup.getComponents();
		for (Component comp : menuComps) {
			if (comp instanceof EdgeMenuListener) {
				((EdgeMenuListener<V, E>) comp).setEdgeAndView(edge, vv);
			}
			if (comp instanceof MenuPointListener) {
				((MenuPointListener) comp).setPoint(point);
			}
		}
	}

	/**
	 * Keep vertex menu updated.
	 * 
	 * @param v
	 *            V
	 * @param vv
	 *            VisualizationViewer<V,E>
	 * @param point
	 *            Point2D
	 */
	@SuppressWarnings("unchecked")
	private void updateVertexMenu(V v, VisualizationViewer<V, E> vv, Point point) {
		if (vertexPopup == null)
			return;
		if (vertexPopup instanceof VertexMenuListener) {
			((VertexMenuListener<V, E>) vertexPopup).setVertexAndView(v, vv);
		}
		if (vertexPopup instanceof MenuPointListener) {
			((MenuPointListener) vertexPopup).setPoint(point);
		}
		Component[] menuComps = vertexPopup.getComponents();
		for (Component comp : menuComps) {
			if (comp instanceof VertexMenuListener) {
				((VertexMenuListener<V, E>) comp).setVertexAndView(v, vv);
			}
			if (comp instanceof MenuPointListener) {
				((MenuPointListener) comp).setPoint(point);
			}
		}
	}
}
