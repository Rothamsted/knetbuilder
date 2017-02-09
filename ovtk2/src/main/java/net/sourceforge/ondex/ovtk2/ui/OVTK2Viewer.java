package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.print.Printable;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.undo.StateEdit;
import javax.swing.undo.UndoManager;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
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
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.graph.custom.ONDEXBasicVertexRenderer;
import net.sourceforge.ondex.ovtk2.layout.ConceptClassCircleLayout;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraph;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraphPanel;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2AnnotatingGraphMousePlugin;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2DefaultModalGraphMouse;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.RegisteredFrame;
import net.sourceforge.ondex.ovtk2.util.VisualisationUtils;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Represents the graphical visualisation of an ONDEXGraph.
 * 
 * @author taubertj
 * 
 */
public class OVTK2Viewer extends RegisteredJInternalFrame implements ActionListener, KeyListener, ComponentListener, Printable, RegisteredFrame, OVTK2PropertiesAggregator {

	/**
	 * Calling auto-save function
	 * 
	 * @author taubertj
	 * 
	 */
	class AutoSaveTask extends TimerTask {

		final OVTK2Viewer viewer;

		public AutoSaveTask(OVTK2Viewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void run() {
			if (Config.config.getProperty("Autosave.Set") == null || Boolean.valueOf(Config.config.getProperty("Autosave.Set")) == false)
				return;

			if (Config.config.getProperty("Autosave.Interval") != null) {
				int newvalue = Integer.parseInt(Config.config.getProperty("Autosave.Interval"));
				if (newvalue != minutes) {
					timer.cancel();
					timer = new Timer();
					timer.schedule(new AutoSaveTask(viewer), minutes * 1000 * 60);
				}
			}

			// prevent auto-save name from exploding
			String name = graph.getName();
			if (name.startsWith("autosave")) {
				name = name.substring(9, name.length());
				name = name.substring(0, name.lastIndexOf("_"));
			}

			// save appearance in autosave function
			OVTK2Desktop.getInstance().actionPerformed(new ActionEvent(this, 0, "saveappearance"));

			// construct auto-save filename
			File file = new File(Config.ovtkDir + File.separator + "autosave_" + name + "_" + System.currentTimeMillis() + ".oxl");
			DesktopUtils.saveFile(file, viewer);
		}
	}

	public static final String EDGEFONT = "edgefont";

	// count number of instances
	public static int instances = 0;

	// generated
	private static final long serialVersionUID = -6674411113816397661L;

	public static final String VERTEXFONT = "vertexfont";

	// whether or not viewer is being destroyed without warnings
	private boolean destroy = false;

	// ONDEXEdgeArrows
	private ONDEXEdgeArrows edgearrows = null;

	// ONDEXEdgeColors
	private ONDEXEdgeColors edgecolors = null;

	// ONDEXEdgeLabels
	private ONDEXEdgeLabels edgelabels = null;

	// ONDEXEdgeShapes
	private ONDEXEdgeShapes edgeshapes = null;

	// ONDEXEdgeStrokes
	private ONDEXEdgeStrokes edgestrokes = null;

	// JUNG synchronized ONDEXGraph
	private ONDEXJUNGGraph graph = null;

	// current JUNG mouse
	public AnnotatingModalGraphMouse<ONDEXConcept, ONDEXRelation> graphMouse = null;

	// rendering hints
	private Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();

	// if edge labels are shown
	private boolean isShowEdgeLabels = false;

	// if node labels are shown
	private boolean isShowNodeLabels = false;

	// JUNG synchronized ONDEXMetaGraph
	private ONDEXMetaGraph meta = null;

	// JUNG visualization of ONDEXMetaGraph
	private ONDEXMetaGraphPanel metapanel = null;

	// auto-save interval
	private int minutes = 5;

	// ONDEXNodeColors
	private ONDEXNodeFillPaint nodecolors = null;

	// ONDEXNodeDrawPaint
	private ONDEXNodeDrawPaint nodeDrawPaint = null;

	// ONDEXNodeLabels
	private ONDEXNodeLabels nodelabels = null;

	// ONDEXNodeShapes
	private ONDEXNodeShapes nodeshapes = null;

	Transformer<ONDEXConcept, Stroke> oldStrokes = null;

	// whether to relayout on viewer resize
	protected boolean relayoutOnResize = false;

	Map<ONDEXConcept, Stroke> strokes = new HashMap<ONDEXConcept, Stroke>();

	// auto-save timer
	private Timer timer;

	// handles visibility undo events
	private UndoManager undoManager = new UndoManager();

	// JUNG visualisation
	private VisualizationViewer<ONDEXConcept, ONDEXRelation> visviewer = null;

	/**
	 * Propagating constructor.
	 * 
	 * @param aog
	 */
	public OVTK2Viewer(ONDEXGraph aog) {
		this(aog, null);
	}

	/**
	 * Initialises an empty ONDEXGraph.
	 * 
	 * @param aog
	 *            ONDEXGraph
	 * @param annotations
	 *            possible XML graph annotations
	 */
	public OVTK2Viewer(ONDEXGraph aog, Map<String, String> annotations) {

		// set title and icon
		super(Config.language.getProperty("Viewer.Title") + " - " + aog.getName(), "Graph", Config.language.getProperty("Viewer.Title") + " - " + aog.getName(), true, true, true, true);
		setName(aog.getName());

		// dispose viewer on close
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		initIcon();

		instances++;

		// wrap ONDEXGraph into JUNG
		graph = new ONDEXJUNGGraph(aog);
		if (annotations != null)
			graph.getAnnotations().putAll(annotations);

		// wrap ONDEXMetaGraph into JUNG
		meta = new ONDEXMetaGraph(graph, this);

		// set default layouter
		ConceptClassCircleLayout layout = new ConceptClassCircleLayout(this);
		visviewer = new VisualizationViewer<ONDEXConcept, ONDEXRelation>(layout, new Dimension(640, 480));
		visviewer.setGraphLayout(layout);
		visviewer.setBackground(Color.white);
		visviewer.setDoubleBuffered(true);

		// apply layout also to invisible nodes
		layout.initialize(true);

		// default label position
		visviewer.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);

		// set custom vertex renderer for shape transparency
		visviewer.getRenderer().setVertexRenderer(new ONDEXBasicVertexRenderer(graph));

		// initialize node labels
		nodelabels = new ONDEXNodeLabels(true);

		// initialize edge labels
		edgelabels = new ONDEXEdgeLabels();

		// initialize node shapes
		nodeshapes = new ONDEXNodeShapes();

		// initialize edge shapes
		edgeshapes = new ONDEXEdgeShapes();

		// initialize node colors
		nodecolors = new ONDEXNodeFillPaint(visviewer.getPickedVertexState());

		// initialize edge colors
		edgecolors = new ONDEXEdgeColors(visviewer.getPickedEdgeState());

		// initialize node draw paint
		nodeDrawPaint = new ONDEXNodeDrawPaint();

		// initialize edge strokes
		edgestrokes = new ONDEXEdgeStrokes();

		// initialize edge arrows
		edgearrows = new ONDEXEdgeArrows();

		// node label transformer
		visviewer.getRenderContext().setVertexLabelTransformer(nodelabels);
		visviewer.getRenderContext().setEdgeLabelTransformer(edgelabels);

		// node shape transformer
		visviewer.getRenderContext().setVertexShapeTransformer(nodeshapes);
		visviewer.getRenderContext().getVertexFontTransformer();

		// edge shape transformer
		visviewer.getRenderContext().setEdgeShapeTransformer(edgeshapes);
		edgeshapes.setEdgeIndexFunction(visviewer.getRenderContext().getParallelEdgeIndexFunction());

		// edge color transformer
		visviewer.getRenderContext().setEdgeDrawPaintTransformer(edgecolors);

		// node color transformer
		visviewer.getRenderContext().setVertexFillPaintTransformer(nodecolors);

		// node draw paint transformer
		visviewer.getRenderContext().setVertexDrawPaintTransformer(nodeDrawPaint);

		// edge stroke transformer
		visviewer.getRenderContext().setEdgeStrokeTransformer(edgestrokes);

		// edge arrow predicate
		visviewer.getRenderContext().setEdgeArrowPredicate(edgearrows);

		// set antialiasing painting off
		Map<RenderingHints.Key, Object> temp = visviewer.getRenderingHints();

		// copying necessary because of type-safety
		for (RenderingHints.Key key : temp.keySet()) {
			hints.put(key, temp.get(key));
		}
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		visviewer.setRenderingHints(hints);

		// standard mouse support
		OVTK2AnnotatingGraphMousePlugin anno = new OVTK2AnnotatingGraphMousePlugin(visviewer.getRenderContext());
		graphMouse = new OVTK2DefaultModalGraphMouse(this, anno);

		visviewer.setGraphMouse(graphMouse);
		visviewer.addKeyListener(this);
		visviewer.addKeyListener(graphMouse.getModeKeyListener());

		// set graph mode to picking
		graphMouse.setMode(Mode.PICKING);

		this.addComponentListener(this);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(visviewer, BorderLayout.CENTER);

		this.pack();

		timer = new Timer();
		if (Config.config.getProperty("Autosave.Interval") != null) {
			minutes = Integer.parseInt(Config.config.getProperty("Autosave.Interval"));
		}
		timer.schedule(new AutoSaveTask(this), minutes * 1000 * 60);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();
		if (arg0.getSource() instanceof ONDEXConcept) {
			if (cmd.equals("create")) {
				ONDEXConcept ac = (ONDEXConcept) arg0.getSource();
				updateViewer(ac);
			} else if (cmd.equals("delete")) {
				updateViewer(null);
			}
		} else if (arg0.getSource() instanceof ONDEXRelation) {
			if (cmd.equals("create")) {
				ONDEXRelation ar = (ONDEXRelation) arg0.getSource();
				updateViewer(ar);
			} else if (cmd.equals("delete")) {
				updateViewer(null);
			}
		} else if (cmd.equals("refresh")) {
			updateViewer(null);
		}
	}

	/**
	 * add a listener for mode changes
	 */
	public void addItemListener(ItemListener listener) {
		graphMouse.addItemListener(listener);
	}

	public void addPickingListener(ActionListener l) {
		if (graphMouse instanceof OVTK2GraphMouse) {
			((OVTK2GraphMouse) graphMouse).getOVTK2PickingMousePlugin().addPickingListener(l);
		}
	}

	/**
	 * Calculates actual bounds of a painted graph.
	 * 
	 * @return Point2D[]
	 */
	private Point2D[] calcBounds() {
		Point2D min = null;
		Point2D max = null;
		Layout<ONDEXConcept, ONDEXRelation> layout = getVisualizationViewer().getGraphLayout();
		for (ONDEXConcept ondexNode : getONDEXJUNGGraph().getVertices()) {
			Point2D point = layout.transform(ondexNode);
			if (min == null) {
				min = new Point2D.Double(0, 0);
				min.setLocation(point);
			}
			if (max == null) {
				max = new Point2D.Double(0, 0);
				max.setLocation(point);
			}
			min.setLocation(Math.min(min.getX(), point.getX()), Math.min(min.getY(), point.getY()));
			max.setLocation(Math.max(max.getX(), point.getX()), Math.max(max.getY(), point.getY()));
		}
		// sanity checks, in case of empty graph
		if (min == null)
			min = new Point2D.Double(0, 0);
		if (max == null)
			max = new Point2D.Double(0, 0);
		// put results together
		Point2D[] result = new Point2D[2];
		result[0] = min;
		result[1] = max;
		// case for just one node, make distinct
		if (min.equals(max)) {
			min.setLocation(min.getX() - 20, min.getY() - 20);
			max.setLocation(max.getX() + 20, max.getY() + 20);
		}
		return result;
	}

	/**
	 * Centres all nodes to the available window
	 */
	@Override
	public void center() {
		VisualizationViewer<ONDEXConcept, ONDEXRelation> vv = this.getVisualizationViewer();
		// reset scaling for predictive behaviour
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();

		// place layout center in center of the view
		Point2D[] calc = calcBounds();
		Point2D min = calc[0];
		Point2D max = calc[1];

		if (min == null || max == null) {
			return; // nothing to center on
		}

		Point2D screen_center = vv.getCenter();
		Point2D layout_bounds = new Point2D.Double(max.getX() - min.getX(), max.getY() - min.getY());
		Point2D layout_center = new Point2D.Double(screen_center.getX() - (layout_bounds.getX() / 2) - min.getX(), screen_center.getY() - (layout_bounds.getY() / 2) - min.getY());
		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).translate(layout_center.getX(), layout_center.getY());

		// scale graph
		Point2D scale_bounds = new Point2D.Double(vv.getWidth() / layout_bounds.getX(), vv.getHeight() / layout_bounds.getY());
		float scale = (float) Math.min(scale_bounds.getX(), scale_bounds.getY());
		scale = 0.95f * scale;
		OVTK2GraphMouse mouse = (OVTK2GraphMouse) vv.getGraphMouse();
		mouse.getScaler().scale(vv, scale, vv.getCenter());
	}

	/**
	 * Changes the stroke of nodes according to whether or not all edges are
	 * shown.
	 * 
	 */
	public void changeStroke() {
		if (oldStrokes == null) {
			oldStrokes = visviewer.getRenderContext().getVertexStrokeTransformer();

			Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] { 5f }, 0f);

			for (ONDEXConcept ondexNode : graph.getVertices()) {
				boolean allVisible = true;
				for (ONDEXRelation r : graph.getRelationsOfConcept(ondexNode)) {
					allVisible = allVisible && graph.isVisible(r);
				}
				if (!allVisible) {
					strokes.put(ondexNode, stroke);
				} else {
					strokes.put(ondexNode, oldStrokes.transform(ondexNode));
				}
			}

			visviewer.getRenderContext().setVertexStrokeTransformer(new Transformer<ONDEXConcept, Stroke>() {

				@Override
				public Stroke transform(ONDEXConcept arg0) {
					return strokes.get(arg0);
				}
			});
		} else {
			visviewer.getRenderContext().setVertexStrokeTransformer(oldStrokes);
			oldStrokes = null;
		}
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	/**
	 * Propagate resize events to layouter.
	 * 
	 * @param arg0
	 *            ComponentEvent
	 */
	@Override
	public void componentResized(ComponentEvent arg0) {
		if (relayoutOnResize)
			visviewer.getGraphLayout().setSize(visviewer.getSize());
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	@Override
	public void dispose() {
		super.dispose();
		// stop layouter
		Relaxer relaxer = visviewer.getModel().getRelaxer();
		if (relaxer != null) {
			relaxer.stop();
		}
	}

	/**
	 * Returns transformer for edge arrows.
	 * 
	 * @return ONDEXEdgeArrows
	 */
	@Override
	public ONDEXEdgeArrows getEdgeArrows() {
		return edgearrows;
	}

	/**
	 * Returns transformer for edge colors.
	 * 
	 * @return ONDEXEdgeColors
	 */
	@Override
	public ONDEXEdgeColors getEdgeColors() {
		return edgecolors;
	}

	/**
	 * Return current edge label font.
	 * 
	 * @return the edge font
	 */
	@Override
	public Font getEdgeFont() {
		if (graph.getAnnotations().containsKey(EDGEFONT)) {
			ByteArrayInputStream bis = new ByteArrayInputStream(graph.getAnnotations().get(EDGEFONT).getBytes());
			XMLDecoder decoder = new XMLDecoder(bis);
			return (Font) decoder.readObject();
		}
		return null;
	}

	/**
	 * Returns transformer for edge labels.
	 * 
	 * @return ONDEXEdgeLabels
	 */
	@Override
	public ONDEXEdgeLabels getEdgeLabels() {
		return edgelabels;
	}

	/**
	 * Returns transformer for edge shapes.
	 * 
	 * @return ONDEXEdgeShapes
	 */
	@Override
	public ONDEXEdgeShapes getEdgeShapes() {
		return edgeshapes;
	}

	/**
	 * Returns transformer for edge strokes.
	 * 
	 * @return ONDEXEdgeStrokes
	 */
	@Override
	public ONDEXEdgeStrokes getEdgeStrokes() {
		return edgestrokes;
	}

	@Override
	public String getGroup() {
		return "OVTK Graph";
	}

	/**
	 * Returns an array of all the <code>ItemListener</code>s added to this
	 * JComboBox with addItemListener().
	 * 
	 * @return all of the <code>ItemListener</code>s added or an empty array if
	 *         no listeners have been added
	 * @since 1.4
	 */
	public ItemListener[] getItemListeners() {
		return graphMouse.getItemListeners();
	}

	/**
	 * Returns ONDEXMetaGraph, which is a JUNG graph implementation.
	 * 
	 * @return OMDEXMetaGraph
	 */
	@Override
	public ONDEXMetaGraph getMetaGraph() {
		return meta;
	}

	/**
	 * Lazy initialisation for meta graph visualisation.
	 * 
	 * @return ONDEXMetaGraphPanel
	 */
	public ONDEXMetaGraphPanel getMetaGraphPanel() {
		if (metapanel == null) {
			metapanel = new ONDEXMetaGraphPanel(this);
			meta.addChangeListener(metapanel);
		}
		return metapanel;
	}

	/**
	 * Returns transformer for node colours.
	 * 
	 * @return ONDEXNodeColors
	 */
	@Override
	public ONDEXNodeFillPaint getNodeColors() {
		return nodecolors;
	}

	/**
	 * Returns transformer for node draw colors.
	 * 
	 * @return ONDEXNodeDrawPaint
	 */
	@Override
	public ONDEXNodeDrawPaint getNodeDrawPaint() {
		return nodeDrawPaint;
	}

	/**
	 * Returns transformer for node labels.
	 * 
	 * @return ONDEXNodeLabels
	 */
	@Override
	public ONDEXNodeLabels getNodeLabels() {
		return nodelabels;
	}

	/**
	 * Returns transformer for node shapes.
	 * 
	 * @return ONDEXNodeShapes
	 */
	@Override
	public ONDEXNodeShapes getNodeShapes() {
		return nodeshapes;
	}

	/**
	 * Returns ONDEXJUNGGraph, which is a JUNG graph implementation.
	 * 
	 * @return ONDEXJUNGGraph
	 */
	@Override
	public ONDEXJUNGGraph getONDEXJUNGGraph() {
		return graph;
	}

	/**
	 * Returns a set with picked ONDEXEdges.
	 * 
	 * @return Set<ONDEXEdge>
	 */
	@Override
	public Set<ONDEXRelation> getPickedEdges() {
		return visviewer.getPickedEdgeState().getPicked();
	}

	/**
	 * Returns a set with picked ONDEXNodes.
	 * 
	 * @return Set<ONDEXNode>
	 */
	@Override
	public Set<ONDEXConcept> getPickedNodes() {
		return visviewer.getPickedVertexState().getPicked();
	}

	@Override
	public UndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * Return current vertex label font.
	 * 
	 * @return the node/vertex font
	 */
	@Override
	public Font getVertexFont() {
		if (graph.getAnnotations().containsKey(VERTEXFONT)) {
			ByteArrayInputStream bis = new ByteArrayInputStream(graph.getAnnotations().get(VERTEXFONT).getBytes());
			XMLDecoder decoder = new XMLDecoder(bis);
			return (Font) decoder.readObject();
		}
		return null;
	}

	/**
	 * Needs to be reorganized, to something more generic. Because
	 * edu.uci.ics.jung.visualization.VisualizationViewer and
	 * edu.uci.ics.jung3d.visualization.VisualizationViewer are incompatible.
	 * 
	 * @return VisualizationViewer<ONDEXNode, ONDEXEdge>
	 */
	@Override
	public VisualizationViewer<ONDEXConcept, ONDEXRelation> getVisualizationViewer() {
		return visviewer;
	}

	/**
	 * Hides any selection for nodes or edges in the graph.
	 * 
	 */
	public void hideSelection() {

		StateEdit edit = new StateEdit(new VisibilityUndo(this.getONDEXJUNGGraph()), Config.language.getProperty("Undo.HideSelection"));
		undoManager.addEdit(edit);
		OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(this);

		// hide edges first
		for (ONDEXRelation ondexEdge : getPickedEdges()) {
			getONDEXJUNGGraph().setVisibility(ondexEdge, false);
		}

		// hide nodes next
		for (ONDEXConcept ondexNode : getPickedNodes()) {
			getONDEXJUNGGraph().setVisibility(ondexNode, false);
		}

		// update viewer
		getVisualizationViewer().getModel().fireStateChanged();
		edit.end();
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon() {

		try {
			File imgLocation = new File("config/toolbarButtonGraphics/development/Application16.gif");
			URL imageURL = imgLocation.toURI().toURL();
			this.setFrameIcon(new ImageIcon(imageURL));
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

	}

	/**
	 * Returns whether or not antialiased painting is used.
	 * 
	 * @return antialiased painting enabled
	 */
	@Override
	public boolean isAntiAliased() {
		return hints.get(RenderingHints.KEY_ANTIALIASING).equals(RenderingHints.VALUE_ANTIALIAS_ON);
	}

	/**
	 * @return the destroy
	 */
	@Override
	public boolean isDestroy() {
		return destroy;
	}

	/**
	 * Returns whether or not relayout on resize is used.
	 * 
	 * @return relayout enabled
	 */
	@Override
	public boolean isRelayoutOnResize() {
		return relayoutOnResize;
	}

	/**
	 * Returns whether or not edge labels are shown.
	 * 
	 * @return edge labels shown
	 */
	@Override
	public boolean isShowEdgeLabels() {
		return isShowEdgeLabels;
	}

	/**
	 * Returns whether or not node labels are shown.
	 * 
	 * @return node labels shown
	 */
	@Override
	public boolean isShowNodeLabels() {
		return isShowNodeLabels;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
			if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("h")) {
				hideSelection();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("c")) {
				center();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("g")) {

				StateEdit edit = new StateEdit(new VisibilityUndo(this.getONDEXJUNGGraph()), Config.language.getProperty("Undo.RemoveComplement"));
				undoManager.addEdit(edit);
				OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(this);

				Set<ONDEXConcept> allnodes = new HashSet<ONDEXConcept>(graph.getVertices());
				allnodes.removeAll(this.getPickedNodes());
				for (ONDEXConcept allnode : allnodes) {
					graph.setVisibility(allnode, false);
				}
				edit.end();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("n")) {
				selectNeighboursOfSelection();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("w")) {
				showAllRelations();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("d")) {
				changeStroke();
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("z")) {
				if (undoManager.canUndo()) {
					undoManager.undo();
					OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(this);
					visviewer.repaint();
				}
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("y")) {
				if (undoManager.canRedo()) {
					undoManager.redo();
					OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(this);
					visviewer.repaint();
				}
			} else if (KeyEvent.getKeyText(arg0.getKeyCode()).equalsIgnoreCase("a")) {
				PickedState<ONDEXConcept> pickState = visviewer.getPickedVertexState();
				for (ONDEXConcept n : graph.getVertices())
					pickState.pick(n, true);
			}

			this.getVisualizationViewer().getModel().fireStateChanged();
		}

		else if (arg0.getKeyCode() == KeyEvent.VK_F5) {
			VisualisationUtils.relayout(this, OVTK2Desktop.getInstance().getMainFrame());
		}

		else if (arg0.getKeyCode() == KeyEvent.VK_F11) {
			try {
				this.setMaximum(!this.isMaximum);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	/**
	 * Print rendering content of OVTK2Viewer
	 * 
	 * @param graphics
	 *            Graphis
	 * @param pageFormat
	 *            PageFormat
	 * @param pageIndex
	 *            int
	 * @return int
	 */
	@Override
	public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return (Printable.NO_SUCH_PAGE);
		}
		java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
		visviewer.setDoubleBuffered(false);
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		visviewer.paint(g2d);
		visviewer.setDoubleBuffered(true);

		return (Printable.PAGE_EXISTS);
	}

	/**
	 * remove a listener for mode changes
	 */
	public void removeItemListener(ItemListener listener) {
		graphMouse.removeItemListener(listener);
	}

	public void removePickingListener(ActionListener l) {
		if (graphMouse instanceof OVTK2GraphMouse) {
			((OVTK2GraphMouse) graphMouse).getOVTK2PickingMousePlugin().removePickingListener(l);
		}
	}

	public void selectNeighboursOfSelection() {
		PickedState<ONDEXConcept> ps = visviewer.getPickedVertexState();
		Set<ONDEXConcept> selected = ps.getPicked();
		if (selected.size() == 1) {
			ONDEXConcept node = selected.iterator().next();
			ps.clear();
			for (ONDEXConcept neighbour : graph.getNeighbors(node)) {
				ps.pick(neighbour, true);
			}
			getVisualizationViewer().getModel().fireStateChanged();
		}
	}

	/**
	 * Sets whether or not antialiased painting sould be used.
	 * 
	 * @param antialiased
	 *            antialiased painting enabled
	 */
	@Override
	public void setAntiAliased(boolean antialiased) {
		if (antialiased) {
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			visviewer.setRenderingHints(hints);
		} else {
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			visviewer.setRenderingHints(hints);
		}
		visviewer.getModel().fireStateChanged();
	}

	/**
	 * @param destroy
	 *            the destroy to set
	 */
	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	/**
	 * Change edge label font.
	 * 
	 * @param font
	 *            the edge font
	 */
	public void setEdgeFont(final Font font) {
		visviewer.getRenderContext().setEdgeFontTransformer(new Transformer<ONDEXRelation, Font>() {

			@Override
			public Font transform(ONDEXRelation input) {
				return font;
			}
		});
		visviewer.getModel().fireStateChanged();
		// serialise font
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(bos);
		encoder.writeObject(font);
		encoder.close();
		graph.getAnnotations().put(EDGEFONT, bos.toString());
	}

	/**
	 * Change from picking to transforming mouse mode.
	 * 
	 * @param picking
	 *            is in picking mode
	 */
	public void setMouseMode(boolean picking) {
		ModalGraphMouse graphMouse = (ModalGraphMouse) visviewer.getGraphMouse();
		graphMouse.setMode(picking ? Mode.PICKING : Mode.TRANSFORMING);
	}

	/**
	 * Sets whether or not to relayout on resize of viewer.
	 * 
	 * @param relayout
	 *            relayout enabled
	 */
	public void setRelayoutOnResize(boolean relayout) {
		this.relayoutOnResize = relayout;
	}

	/**
	 * Method to override current status when using LoadAppearance.
	 * 
	 * @param show
	 *            labels shown
	 */
	@Override
	public void setShowEdgeLabels(boolean show) {
		isShowEdgeLabels = show;
		edgelabels.fillMask(show);
		visviewer.getModel().fireStateChanged();
	}

	/**
	 * Method to override current status when using LoadAppearance.
	 * 
	 * @param show
	 *            labels shown
	 */
	@Override
	public void setShowNodeLabels(boolean show) {
		isShowNodeLabels = show;
		nodelabels.fillMask(show);
		visviewer.getModel().fireStateChanged();
	}

	/**
	 * Change vertex label font.
	 * 
	 * @param font
	 *            the node/vertex font
	 */
	public void setVertexFont(final Font font) {
		visviewer.getRenderContext().setVertexFontTransformer(new Transformer<ONDEXConcept, Font>() {

			@Override
			public Font transform(ONDEXConcept input) {
				return font;
			}
		});
		visviewer.getModel().fireStateChanged();
		// serialise font
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(bos);
		encoder.writeObject(font);
		encoder.close();
		graph.getAnnotations().put(VERTEXFONT, bos.toString());
	}

	/**
	 * Shows all relations between the currently visible nodes.
	 * 
	 */
	public void showAllRelations() {
		StateEdit edit = new StateEdit(new VisibilityUndo(this.getONDEXJUNGGraph()), Config.language.getProperty("Undo.ShowAllRelations"));
		undoManager.addEdit(edit);
		OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(this);

		for (ONDEXConcept ondexNode : graph.getVertices()) {
			for (ONDEXRelation r : graph.getRelationsOfConcept(ondexNode)) {
				graph.setVisibility(r, true);
			}
		}
		edit.end();
	}

	/**
	 * Update with current changes.
	 * 
	 * @param entity
	 *            ONDEXEntity or null
	 */
	@Override
	public synchronized void updateViewer(ONDEXEntity entity) {

		if (entity == null) {
			// ensure that none of these pull the entire underlying graph into
			// the GUI
			this.getNodeLabels().updateAll();
			this.getNodeColors().updateAll();
			this.getNodeDrawPaint().updateAll();
			this.getNodeShapes().updateAll();
			this.getEdgeLabels().updateAll();
			this.getEdgeColors().updateAll();
			this.getEdgeShapes().updateAll();
		} else if (entity instanceof ONDEXConcept) {
			ONDEXConcept node = (ONDEXConcept) entity;
			this.getNodeLabels().updateLabel(node);
			this.getNodeColors().updateColor(node);
			this.getNodeDrawPaint().updateColor(node);
			this.getNodeShapes().updateShape(node);
		} else if (entity instanceof ONDEXRelation) {
			ONDEXRelation edge = (ONDEXRelation) entity;
			this.getEdgeLabels().updateLabel(edge);
			this.getEdgeColors().updateColor(edge);
		}

		visviewer.getModel().fireStateChanged();
		visviewer.repaint();
	}
}
