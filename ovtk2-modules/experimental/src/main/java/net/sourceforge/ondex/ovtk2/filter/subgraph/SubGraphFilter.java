package net.sourceforge.ondex.ovtk2.filter.subgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.subgraph.ArgumentNames;
import net.sourceforge.ondex.filter.subgraph.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConceptLabels;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConceptShapes;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaGraph;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelationArrows;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelationLabels;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelationStrokes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Filter to extract specified subgraphs.
 * 
 * @author taubertj
 */
public class SubGraphFilter extends OVTK2Filter implements ComponentListener,
		ActionListener, ListSelectionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -72634806239183449L;

	// preferred size of this gadget
	private Dimension preferredSize = new Dimension(400, 300);

	// display meta graph
	private ONDEXMetaGraph meta = null;

	// used for scaling by scaleToFit
	private ScalingControl scaler = new CrossoverScalingControl();

	// JUNG visualisation
	private VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation> visviewer = null;

	// MetaGraph layout used
	private KKLayout<ONDEXMetaConcept, ONDEXMetaRelation> layout = null;

	// rendering hints
	private Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();

	// triggers processing
	private JButton goButton = null;

	// uses shortest paths then
	private JCheckBox shortest = new JCheckBox(
			"non-redundant meta data selection?");

	// presents list of concepts in graph alphabetically sorted
	private JList nodeSelection = new JList();

	// caching labels
	private Map<ONDEXConcept, String> labels = new HashMap<ONDEXConcept, String>();

	// list index of node
	private Map<ONDEXConcept, Integer> indices = new HashMap<ONDEXConcept, Integer>();
	
	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Wraps an ONDEXNode to provide a cache based toString function and
	 * compareTo.
	 * 
	 * @author taubertj
	 * 
	 */
	private class ONDEXNodeWrapper implements Comparable<ONDEXNodeWrapper> {

		ONDEXConcept node;

		public ONDEXNodeWrapper(ONDEXConcept node) {
			this.node = node;
		}

		public String toString() {
			return labels.get(node);
		}

		@Override
		public int compareTo(ONDEXNodeWrapper o) {
			if (o != null)
				return toString().compareToIgnoreCase(o.toString());
			else
				return -1;
		}

		@Override
		public boolean equals(Object obj) {
			return node.equals(obj);
		}

		@Override
		public int hashCode() {
			return node.hashCode();
		}
	}

	public SubGraphFilter(OVTK2Viewer viewer) {
		super(viewer);

		this.setLayout(new BorderLayout());

		// get current meta graph
		meta = viewer.getMetaGraph();

		initGUI();

		populateNodeList();

		propagateGraphSelection();

		this.revalidate();
	}

	/**
	 * Populates the visible node list sorted alphabetically.
	 */
	private void populateNodeList() {

		// clear previous
		labels.clear();
		indices.clear();

		// calculate labels for visible nodes and sort
		ONDEXNodeWrapper[] sorted = new ONDEXNodeWrapper[graph.getVertices()
				.size()];
		int i = 0;
		for (ONDEXConcept node : graph.getVertices()) {
			String label = viewer.getNodeLabels().getLabel(node);
			labels.put(node, label);
			sorted[i] = new ONDEXNodeWrapper(node);
			i++;
		}
		Arrays.sort(sorted);

		// make new list model and add nodes to it
		DefaultListModel model = new DefaultListModel();
		i = 0;
		for (ONDEXNodeWrapper wrapper : sorted) {
			model.addElement(wrapper);
			indices.put(wrapper.node, i);
			i++;
		}

		// set model to list
		nodeSelection.setModel(model);
		nodeSelection.revalidate();
	}

	/**
	 * Reflect the selection of concepts in the viewer as selection of concept
	 * class here.
	 */
	private void propagateGraphSelection() {

		// get selection from graph
		List<Integer> selected = new ArrayList<Integer>();
		for (ONDEXConcept node : viewer.getPickedNodes()) {
			if (indices.containsKey(node)) {
				ConceptClass cc = node.getOfType();
				for (ONDEXMetaConcept mc : meta.getVertices()) {
					if (mc.getMetaData().equals(cc)) {
						visviewer.getPickedVertexState().pick(mc, true);
						break;
					}
				}
				selected.add(indices.get(node));
			}
		}

		// set list selection to same as graph selection
		int[] array = new int[selected.size()];
		int i = 0;
		for (Integer j : selected) {
			array[i] = j;
			i++;
		}
		if (array.length > 0)
			nodeSelection.setSelectedIndices(array);
	}

	/**
	 * Present user with nice graph selection.
	 */
	private void initGUI() {

		// scroll and border around selection list for concepts
		nodeSelection.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(nodeSelection);
		listScroller.setPreferredSize(new Dimension(400, 100));
		listScroller.setBorder(BorderFactory
				.createTitledBorder("Root concept selection"));

		// new meta graph viewer
		layout = new KKLayout<ONDEXMetaConcept, ONDEXMetaRelation>(meta);
		visviewer = new VisualizationViewer<ONDEXMetaConcept, ONDEXMetaRelation>(
				layout, preferredSize);
		visviewer.setDoubleBuffered(true);
		visviewer.setBackground(Color.white);

		// set label position and label transformer
		visviewer.getRenderer().getVertexLabelRenderer()
				.setPosition(Position.AUTO);
		visviewer.getRenderContext().setVertexLabelTransformer(
				new ONDEXMetaConceptLabels(graph));
		visviewer.getRenderContext().setEdgeLabelTransformer(
				new ONDEXMetaRelationLabels(graph));

		// set visible attribute renderer
		visviewer.getRenderContext().setEdgeDrawPaintTransformer(
				new SubGraphMetaRelationColors(visviewer.getPickedEdgeState()));
		visviewer.getRenderContext().setVertexShapeTransformer(
				new ONDEXMetaConceptShapes(graph));
		visviewer.getRenderContext()
				.setVertexFillPaintTransformer(
						new SubGraphMetaConceptColors(visviewer
								.getPickedVertexState()));
		visviewer.getRenderContext().setEdgeArrowPredicate(
				new ONDEXMetaRelationArrows(graph));
		visviewer.getRenderContext().setEdgeStrokeTransformer(
				new ONDEXMetaRelationStrokes(graph));

		// set anti-aliasing painting on
		Map<?, ?> temp = visviewer.getRenderingHints();

		// copying necessary because of type safety
		Iterator<?> it = temp.keySet().iterator();
		while (it.hasNext()) {
			RenderingHints.Key key = (RenderingHints.Key) it.next();
			hints.put(key, temp.get(key));
		}
		hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		visviewer.setRenderingHints(hints);

		// standard mouse support
		DefaultModalGraphMouse<ONDEXMetaConcept, ONDEXMetaRelation> graphMouse = new DefaultModalGraphMouse<ONDEXMetaConcept, ONDEXMetaRelation>();
		graphMouse
				.add(new PickingGraphMousePlugin<ONDEXMetaConcept, ONDEXMetaRelation>(
						InputEvent.BUTTON1_MASK, InputEvent.BUTTON3_MASK));

		visviewer.setGraphMouse(graphMouse);
		visviewer.addKeyListener(graphMouse.getModeKeyListener());

		// zoom pane and mouse menu in the corner
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visviewer);
		JMenuBar menu = new JMenuBar();
		menu.add(graphMouse.getModeMenu());
		scrollPane.setCorner(menu);
		scrollPane.setBorder(BorderFactory
				.createTitledBorder("Select path in meta-graph"));

		// set graph mode to picking
		graphMouse.setMode(Mode.PICKING);

		goButton = new JButton("Apply");
		goButton.setEnabled(false);
		goButton.addActionListener(this);

		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (ONDEXMetaConcept mc : meta.getVertices())
					visviewer.getPickedVertexState().pick(mc, true);
				for (ONDEXMetaRelation mr : meta.getEdges())
					visviewer.getPickedEdgeState().pick(mr, true);
			}
		});

		JPanel south = new JPanel();
		BoxLayout layout = new BoxLayout(south, BoxLayout.LINE_AXIS);
		south.setLayout(layout);
		south.add(shortest);
		south.add(selectAll);
		south.add(goButton);

		// splits meta-graph and concept selection apart
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				listScroller, scrollPane);
		splitPane.setOneTouchExpandable(true);

		// add to content pane
		this.add(splitPane, BorderLayout.CENTER);
		this.add(south, BorderLayout.SOUTH);
		this.revalidate();
		this.scaleToFit();
	}

	/**
	 * Calculates the bounds of all nodes in a given layout.
	 * 
	 * @return Point2D[] min bounds, max bounds
	 */
	private Point2D[] calcBounds() {
		Point2D[] result = new Point2D[2];
		Point2D min = null;
		Point2D max = null;
		Iterator<ONDEXMetaConcept> it = layout.getGraph().getVertices()
				.iterator();
		while (it.hasNext()) {
			Point2D point = layout.transform(it.next());
			if (min == null) {
				min = new Point2D.Double(0, 0);
				min.setLocation(point);
			}
			if (max == null) {
				max = new Point2D.Double(0, 0);
				max.setLocation(point);
			}
			min.setLocation(Math.min(min.getX(), point.getX()),
					Math.min(min.getY(), point.getY()));
			max.setLocation(Math.max(max.getX(), point.getX()),
					Math.max(max.getY(), point.getY()));
		}
		result[0] = min;
		result[1] = max;
		return result;
	}

	/**
	 * Scale current metagraph view to fit in whole graph.
	 */
	public void scaleToFit() {

		// reset scaling for predictive behaviour
		visviewer.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT).setToIdentity();
		visviewer.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.VIEW).setToIdentity();

		// place layout centre in centre of the view
		Point2D[] calc = calcBounds();
		Point2D min = calc[0];
		Point2D max = calc[1];

		// check for empty graph
		if (min != null && max != null) {
			Point2D layout_bounds = new Point2D.Double(max.getX() - min.getX(),
					max.getY() - min.getY());
			// layouter produced nice bounds
			if (layout_bounds.getX() > 0 && layout_bounds.getY() > 0) {
				Point2D screen_center = visviewer.getCenter();
				Point2D layout_center = new Point2D.Double(screen_center.getX()
						- (layout_bounds.getX() / 2) - min.getX(),
						screen_center.getY() - (layout_bounds.getY() / 2)
								- min.getY());
				visviewer.getRenderContext().getMultiLayerTransformer()
						.getTransformer(Layer.VIEW)
						.translate(layout_center.getX(), layout_center.getY());

				// scale graph
				Point2D scale_bounds = new Point2D.Double(visviewer.getSize()
						.getWidth() / layout_bounds.getX(), visviewer.getSize()
						.getHeight() / layout_bounds.getY());
				float scale = (float) Math.min(scale_bounds.getX(),
						scale_bounds.getY());
				scale = 0.8f * scale;
				scaler.scale(visviewer, scale, visviewer.getCenter());
			}
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.SubGraph");
	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentResized(ComponentEvent e) {
		this.scaleToFit();
	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		try {
			callFilter();
			used = true;
		} catch (InvalidPluginArgumentException e) {
			ErrorDialog.show(e);
		}
	}

	private void callFilter() throws InvalidPluginArgumentException {
		StateEdit edit = new StateEdit(new VisibilityUndo(
				viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		// contains results
		Set<ONDEXConcept> concepts = null;
		Set<ONDEXRelation> relations = null;

		// get user choices
		PickedState<ONDEXMetaConcept> ccPI = visviewer.getPickedVertexState();
		PickedState<ONDEXMetaRelation> rtPI = visviewer.getPickedEdgeState();

		// seed nodes from viewer start subgraph at each of them
		for (Object o : nodeSelection.getSelectedValues()) {
			ONDEXConcept root = ((ONDEXNodeWrapper) o).node;
			ConceptClass rootCC = root.getOfType();

			// execute filter
			Filter filter = new Filter();

			ONDEXPluginArguments fa = new ONDEXPluginArguments(
					filter.getArgumentDefinitions());
			fa.addOption(ArgumentNames.ROOT_ARG, root.getId());

			// graph traversal to identify selected concept classes
			for (ONDEXMetaConcept mc : meta.getVertices()) {
				// found root, make sure its still picked
				if (mc.getMetaData().equals(rootCC) && ccPI.isPicked(mc)) {
					seenConcepts = new HashSet<ONDEXMetaConcept>();
					seenRelations = new HashSet<ONDEXMetaRelation>();
					traverseGraph(fa, ccPI, rtPI, mc);
				}
			}

			filter.setArguments(fa);
			filter.setONDEXGraph(graph);
			try {
				filter.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// concatenate results
			if (concepts == null) {
				concepts = filter.getVisibleConcepts();
				relations = filter.getVisibleRelations();
			} else {
				concepts = BitSetFunctions.or(concepts,
						filter.getVisibleConcepts());
				relations = BitSetFunctions.or(relations,
						filter.getVisibleRelations());
			}
		}

		// show results in graph
		if (concepts != null) {

			// set all relations to invisible
			graph.setVisibility(graph.getRelations(), false);

			// set all concepts to invisible
			graph.setVisibility(graph.getConcepts(), false);

			// first set concepts visible
			graph.setVisibility(concepts, true);

			// second set relations visible
			graph.setVisibility(relations, true);

			// propagate change to viewer
			viewer.getVisualizationViewer().getModel().fireStateChanged();
		}
		edit.end();
		viewer.getUndoManager().addEdit(edit);
		desktop.getOVTK2Menu().updateUndoRedo(viewer);
		desktop.notifyTerminationOfProcess();
	}

	private Set<ONDEXMetaConcept> seenConcepts = null;

	private Set<ONDEXMetaRelation> seenRelations = null;

	/**
	 * Simple Tuple implementation.
	 * 
	 * @author taubertj
	 * @param <A>
	 * @param <B>
	 */
	public class Tuple<A, B> {

		private A first;

		private B second;

		public Tuple(A first, B second) {
			this.first = first;
			this.second = second;
		}

		public A getFirst() {
			return first;
		}

		public B getSecond() {
			return second;
		}
	}

	/**
	 * Performs a breath first search to find first occurrences of relation
	 * types and concept clases.
	 * 
	 * @param fa
	 *            FilterArguments get filled on the way
	 * @param ccPI
	 *            PickedState<ONDEXMetaConcept> for checking user selection
	 * @param rtPI
	 *            PickedState<ONDEXMetaRelation> for checking user selection
	 * @param root
	 *            ONDEXMetaConcept where to start
	 */
	private void traverseGraph(ONDEXPluginArguments fa,
			PickedState<ONDEXMetaConcept> ccPI,
			PickedState<ONDEXMetaRelation> rtPI, ONDEXMetaConcept root)
			throws InvalidPluginArgumentException {

		if (shortest.isSelected())
			seenConcepts.add(root);

		// breath first search (shortest paths
		LinkedList<Tuple<ONDEXMetaConcept, Integer>> queue = new LinkedList<Tuple<ONDEXMetaConcept, Integer>>();
		queue.add(new Tuple<ONDEXMetaConcept, Integer>(root, Integer
				.valueOf(-1)));

		while (!queue.isEmpty()) {

			// get next element from queue and add to arguments
			Tuple<ONDEXMetaConcept, Integer> current = queue.poll();

			// unwrap tuple
			int depth = current.getSecond();
			if (depth < Filter.levelsCC.length) {
				ONDEXMetaConcept mc = current.getFirst();

				// special case for ignoring root
				if (depth > -1)
					fa.addOption(Filter.levelsCC[depth], mc.getMetaData()
							.getId());

				// first outgoing relations
				for (ONDEXMetaRelation outRel : meta.getOutEdges(mc)) {
					if (!seenRelations.contains(outRel)) {
						if (shortest.isSelected())
							seenRelations.add(outRel);
						if (rtPI.isPicked(outRel)
								&& depth + 1 < Filter.levelsCC.length)
							fa.addOption(Filter.levelsRT[depth + 1], outRel
									.getMetaData().getId());

						ONDEXMetaConcept to = meta.getDest(outRel);
						// prevent self loop
						if (!seenConcepts.contains(to) && ccPI.isPicked(to)) {
							if (shortest.isSelected())
								seenConcepts.add(to);
							queue.add(new Tuple<ONDEXMetaConcept, Integer>(to,
									Integer.valueOf(depth + 1)));
						}
					}
				}

				// second incoming relations
				for (ONDEXMetaRelation inRel : meta.getInEdges(mc)) {
					if (!seenRelations.contains(inRel)) {
						if (shortest.isSelected())
							seenRelations.add(inRel);
						if (rtPI.isPicked(inRel)
								&& depth + 1 < Filter.levelsCC.length)
							fa.addOption(Filter.levelsRT[depth + 1], inRel
									.getMetaData().getId());

						ONDEXMetaConcept from = meta.getSource(inRel);
						// prevent self loop
						if (!seenConcepts.contains(from) && ccPI.isPicked(from)) {
							if (shortest.isSelected())
								seenConcepts.add(from);
							queue.add(new Tuple<ONDEXMetaConcept, Integer>(
									from, Integer.valueOf(depth + 1)));
						}
					}
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (nodeSelection.getSelectedIndex() == -1) {
				// No selection, disable fire button.
				goButton.setEnabled(false);

			} else {
				// Selection, enable the fire button.
				goButton.setEnabled(true);

				// propagate list selection to meta-graph
				for (Object value : nodeSelection.getSelectedValues()) {
					ConceptClass cc = ((ONDEXNodeWrapper) value).node
							.getOfType();
					for (ONDEXMetaConcept mc : meta.getVertices()) {
						if (mc.getMetaData().equals(cc)) {
							visviewer.getPickedVertexState().pick(mc, true);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
