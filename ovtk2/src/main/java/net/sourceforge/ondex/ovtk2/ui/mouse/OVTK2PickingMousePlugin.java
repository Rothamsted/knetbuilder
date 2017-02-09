package net.sourceforge.ondex.ovtk2.ui.mouse;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.gds.AttributePanel;
import net.sourceforge.ondex.ovtk2.ui.popup.VertexMenu;
import net.sourceforge.ondex.tools.data.ChemicalStructure;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;

/**
 * Realises the change in cursor while in picking mode.
 * 
 * @author taubertj
 * @version 27.05.2008
 */
public class OVTK2PickingMousePlugin extends PickingGraphMousePlugin<ONDEXConcept, ONDEXRelation> {
	// TODO the timer thread used here is the cause of random graph
	// visualisation corruption and should be removed
	/**
	 * Waits a second and compares if still at some node.
	 * 
	 * @author taubertj
	 * 
	 */
	class CountDown {
		public CountDown(final ONDEXConcept n) {

			timer.schedule(new TimerTask() {
				public void run() {
					// just being paranoid
					cleanPopups();
					if (showMouseOver && vv.isShowing() && onNode && n.equals(currentNode) && !activePopups.containsKey(n)) {
						ConceptClass cc = n.getOfType();
						if (cc.getId().equals("Comp") || (cc.getSpecialisationOf() != null && cc.getSpecialisationOf().getId().equals("Comp"))) {
							for (Attribute attr : n.getAttributes()) {
								Object o = attr.getValue();
								if (o instanceof ChemicalStructure) {
									// use Attribute Editor as a hack
									JComponent c = AttributePanel.findEditor(attr);

									if (c != null) {
										c.setBorder(BorderFactory.createEtchedBorder());
										c.setPreferredSize(new Dimension(150, 150));

										Point location = calculatePopupLocation(c);

										// show popup
										PopupFactory popupFactory = PopupFactory.getSharedInstance();
										Popup popup = popupFactory.getPopup(vv, c, location.x, location.y);

										// just being paranoid
										if (!activePopups.containsKey(n)) {
											activePopups.put(n, popup);
											popup.show();
										}
									}
								}
							}
						}
					}
				}
			}, 500);
		}
	}

	/**
	 * Track all popups, in case something hangs around, needs to be
	 * synchronized because of time tasks
	 */
	Map<ONDEXConcept, Popup> activePopups = Collections.synchronizedMap(new HashMap<ONDEXConcept, Popup>());

	/**
	 * last found node
	 */
	ONDEXConcept currentNode = null;

	/**
	 * graph is dragged
	 */
	boolean dragged = false;

	/**
	 * Copied from ToolTipManager
	 */
	PopupFactory factory = PopupFactory.getSharedInstance();

	/**
	 * for dragging, the anti aliased setting
	 */
	boolean oldAntiAliased = false;

	/**
	 * for dragging, the old edge labels
	 */
	Transformer<ONDEXRelation, String> oldEdgeLabels = null;

	/**
	 * for dragging, the old node labels
	 */
	Transformer<ONDEXConcept, String> oldNodeLabels = null;

	/**
	 * hover over a node
	 */
	boolean onNode = false;

	/**
	 * mouse move where
	 */
	Point2D p = null;

	/**
	 * Should be a set to reduce redundant listeners
	 */
	private Set<ActionListener> pickingListeners = new HashSet<ActionListener>();

	/**
	 * Whether or not to show mouse over drawings
	 */
	private boolean showMouseOver = false;

	/**
	 * count down to popup shows
	 */
	Timer timer = new Timer();

	/**
	 * Reference back to VisualizationViewer to find nodes and edges
	 */
	VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = null;

	/**
	 * Calls super constructor and sets VisualizationViewer.
	 * 
	 * @param vv
	 *            VisualizationViewer<ONDEXNode, ONDEXEdge>
	 */
	public OVTK2PickingMousePlugin(VisualizationViewer<ONDEXConcept, ONDEXRelation> vv) {
		super();
		this.vv = vv;
	}

	/**
	 * For other tools registering interest
	 * 
	 * @param l
	 */
	public void addPickingListener(ActionListener l) {
		pickingListeners.add(l);
	}

	/**
	 * Adapted from JAVA ToolTipManager to calculate
	 * 
	 * @param c
	 * @return
	 */
	private Point calculatePopupLocation(JComponent c) {

		// return value
		Point location = new Point();

		// size of content
		Dimension size = c.getPreferredSize();

		// locate parent
		Point screenLocation = vv.getLocationOnScreen();
		GraphicsConfiguration gc = vv.getGraphicsConfiguration();
		Rectangle sBounds = gc.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

		// Take into account screen insets, decrease viewport
		sBounds.x += screenInsets.left;
		sBounds.y += screenInsets.top;
		sBounds.width -= (screenInsets.left + screenInsets.right);
		sBounds.height -= (screenInsets.top + screenInsets.bottom);

		// define offset from mouse pointer
		location.x = screenLocation.x + (int) p.getX();
		location.y = screenLocation.y + (int) p.getY() + 20;

		// Fit as much of the tooltip on screen as possible
		if (location.x < sBounds.x) {
			location.x = sBounds.x;
		} else if (location.x - sBounds.x + size.width > sBounds.width) {
			location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
		}
		if (location.y < sBounds.y) {
			location.y = sBounds.y;
		} else if (location.y - sBounds.y + size.height > sBounds.height) {
			location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
		}

		return location;
	}

	/**
	 * make a thread safe call to clean all popups
	 */
	private void cleanPopups() {
		synchronized (activePopups) {

			// hide possible popups
			Iterator<ONDEXConcept> it = activePopups.keySet().iterator();
			while (it.hasNext()) {
				activePopups.get(it.next()).hide();
				it.remove();
			}
			activePopups.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		// just being paranoid
		cleanPopups();
	}

	/**
	 * Propagate ActionEvent, removes null ActionListeners.
	 * 
	 * @param e
	 *            ActionEvent
	 */
	private void firePickingEvent(ActionEvent e) {
		for (ActionListener l : pickingListeners) {
			if (l != null)
				l.actionPerformed(e);
			else
				pickingListeners.remove(l);
		}
	}

	/**
	 * How has interest in picking events, like the information panel
	 * 
	 * @return
	 */
	public Set<ActionListener> getPickingListeners() {
		return pickingListeners;
	}

	/**
	 * @return the showMouseOver
	 */
	public boolean isShowMouseOver() {
		return showMouseOver;
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		super.mouseClicked(me);

		// hide possible popups
		cleanPopups();

		// prevent popup from showing when right click
		currentNode = null;

		// mouse move where
		Point2D p = me.getPoint();

		// is pick support available
		GraphElementAccessor<ONDEXConcept, ONDEXRelation> pickSupport = vv.getPickSupport();

		// layout important to find node or edge
		Layout<ONDEXConcept, ONDEXRelation> layout = vv.getGraphLayout();

		if (pickSupport != null) {
			// first check if its a node
			ONDEXConcept n = pickSupport.getVertex(layout, p.getX(), p.getY());
			ONDEXRelation e = pickSupport.getEdge(layout, p.getX(), p.getY());
			if (n != null) {
				firePickingEvent(new ActionEvent(n, 0, "putative node pick"));
			} else if (e != null) {
				firePickingEvent(new ActionEvent(e, 1, "putative edge pick"));
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		// hide possible popups
		cleanPopups();

		if (!dragged && onNode) {
			oldNodeLabels = vv.getRenderContext().getVertexLabelTransformer();
			oldEdgeLabels = vv.getRenderContext().getEdgeLabelTransformer();
			oldAntiAliased = vv.getRenderingHints().get(RenderingHints.KEY_ANTIALIASING).equals(RenderingHints.VALUE_ANTIALIAS_ON);
			vv.getRenderContext().setVertexLabelTransformer(new ConstantTransformer(null));
			vv.getRenderContext().setEdgeLabelTransformer(new ConstantTransformer(null));
			vv.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			dragged = true;
		}
		super.mouseDragged(e);
	}

	/**
	 * Necessary to change the mouse cursor. Could be a bit computing intensive
	 * as pick support is calculated every time mouse is moved.
	 * 
	 */
	@Override
	public void mouseMoved(MouseEvent me) {
		super.mouseMoved(me);

		// mouse move where
		p = me.getPoint();

		// is pick support available
		GraphElementAccessor<ONDEXConcept, ONDEXRelation> pickSupport = vv.getPickSupport();

		// layout important to find node or edge
		Layout<ONDEXConcept, ONDEXRelation> layout = vv.getGraphLayout();

		if (pickSupport != null && layout != null) {

			// prevent popups from showing when vertex menu is active
			if (VertexMenu.INSTANCE != null && VertexMenu.INSTANCE.isShowing()) {
				cleanPopups();
			} else {
				if (layout.getGraph().getVertexCount() < 5000 && layout.getGraph().getEdgeCount() < 7000) {
					// first check if its a node
					ONDEXConcept n = pickSupport.getVertex(layout, p.getX(), p.getY());
					if (n != null) {
						JComponent c = (JComponent) me.getSource();
						c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						// did node change?
						if (!n.equals(currentNode)) {
							cleanPopups();
							new CountDown(n);
						}
						currentNode = n;
						onNode = true;
					} else {
						// hide possible popups
						cleanPopups();
						onNode = false;
						currentNode = null;
						timer.cancel();
						timer = new Timer();
						// maybe its an edge
						ONDEXRelation e = pickSupport.getEdge(layout, p.getX(), p.getY());
						if (e != null) {
							JComponent c = (JComponent) me.getSource();
							c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						} else {
							// OK back to normal
							JComponent c = (JComponent) me.getSource();
							c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		super.mousePressed(me);

		// hide possible popups
		cleanPopups();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		// hide possible popups
		cleanPopups();
		JComponent c = (JComponent) e.getSource();
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if (dragged) {
			vv.getRenderContext().setVertexLabelTransformer(oldNodeLabels);
			vv.getRenderContext().setEdgeLabelTransformer(oldEdgeLabels);
			if (oldAntiAliased)
				vv.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			dragged = false;
		}
	}

	/**
	 * Other tools lost interest, de-register
	 * 
	 * @param l
	 */
	public void removePickingListener(ActionListener l) {
		pickingListeners.remove(l);
	}

	/**
	 * @param showMouseOver
	 *            the showMouseOver to set
	 */
	public void setShowMouseOver(boolean showMouseOver) {
		this.showMouseOver = showMouseOver;
	}

}
