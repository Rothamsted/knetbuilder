package net.sourceforge.ondex.ovtk2.layout;

//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.linlog.Edge;
import net.sourceforge.ondex.ovtk2.util.linlog.MinimizerBarnesHut;
import net.sourceforge.ondex.ovtk2.util.linlog.Node;
import net.sourceforge.ondex.ovtk2.util.linlog.OptimizerModularity;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

/**
 * LinLogLayout is a simple program for computing graph layouts (positions of
 * the nodes of a graph in two- or three-dimensional space) and graph
 * clusterings for visualization and knowledge discovery. It reads a graph from
 * a file, computes a layout and a clustering, writes the layout and the
 * clustering to a file, and displays them in a dialog. LinLogLayout can be used
 * to identify groups of densely connected nodes in graphs, like groups of
 * friends or collaborators in social networks, related documents in hyperlink
 * structures (e.g. web graphs), cohesive subsystems in software models, etc.
 * With a change of a parameter in the <code>main</code> method, it can also
 * compute classical "nice" (i.e. readable) force-directed layouts. The program
 * is primarily intended as a demo for the use of its core layouter and
 * clusterer classes <code>MinimizerBarnesHut</code>,
 * <code>MinimizerClassic</code>, and <code>OptimizerModularity</code>. While
 * <code>MinimizerBarnesHut</code> is faster, <code>MinimizerClassic</code> is
 * simpler and not limited to a maximum of three dimensions.
 * 
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @author hacked by taubertj for Ondex
 * @version 13.11.2008
 */
public class LinLogLayout extends OVTK2Layouter implements ActionListener,
		ChangeListener, CaretListener, Monitorable {

	// attribute name of conf value
	private AttributeName an = null;

	// setting for attrExp
	private double attrExp = 1.0;

	// input number for attrExp
	private JTextField attrExpField = null;

	private boolean cancelled = false;

	private Color defaultBackground = null;

	// setting for gravFactor
	private double gravFactor = 0.05;

	// input number for gravFactor
	private JTextField gravFactorField = null;

	// scale edge length
	private boolean ignoreLoops = false;

	// checkbox to normalise length of edges
	private JCheckBox ignoreLoopsBox = null;

	// number of maximum iterations
	private int maxIterations = 100;

	MinimizerBarnesHut mbh = null;

	OptimizerModularity om = null;

	private int progress = 0;

	// setting for repuExp
	private double repuExp = 0.0;

	// input number for repuExp
	private JTextField repuExpField = null;

	// slider for maxIter
	private JSlider sliderMaxIter = null;

	// layout status messages
	public String status = Monitorable.STATE_IDLE;

	/**
	 * Reads a graph from a specified input file, computes a layout and a
	 * clustering, writes the layout and the clustering into a specified output
	 * file, and displays them in a dialog.
	 * 
	 * @param args
	 *            number of dimensions, name of the input file and of the output
	 *            file. If <code>args.length != 3</code>, the method outputs a
	 *            help message.
	 */
	public LinLogLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
	}

	/**
	 * Check for selection of an AttributeName.
	 */
	public void actionPerformed(ActionEvent arg0) {
		ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;
		JComboBox box = (JComboBox) arg0.getSource();
		String name = (String) box.getSelectedItem();
		an = aog.getMetaData().getAttributeName(name);
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		if (e.getSource().equals(repuExpField)) {
			String text = repuExpField.getText();
			try {
				repuExp = Double.parseDouble(text);
				repuExpField.setBackground(defaultBackground);
			} catch (NumberFormatException nfe) {
				repuExpField.setBackground(Color.RED);
			}
		} else if (e.getSource().equals(attrExpField)) {
			String text = attrExpField.getText();
			try {
				attrExp = Double.parseDouble(text);
				attrExpField.setBackground(defaultBackground);
			} catch (NumberFormatException nfe) {
				attrExpField.setBackground(Color.RED);
			}
		} else if (e.getSource().equals(gravFactorField)) {
			String text = gravFactorField.getText();
			try {
				gravFactor = Double.parseDouble(text);
				gravFactorField.setBackground(defaultBackground);
			} catch (NumberFormatException nfe) {
				gravFactorField.setBackground(Color.RED);
			}
		}
	}

	@Override
	public int getMaxProgress() {
		return 4;
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public JPanel getOptionPanel() {
		ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;

		// new option panel
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// combobox for attributenames
		JComboBox box = new JComboBox();
		box.addActionListener(this);
		box.addItem("None");
		box.setSelectedIndex(0);
		for (AttributeName an : aog.getMetaData().getAttributeNames()) {
			Class<?> cl = an.getDataType();
			if (cl != null && Number.class.isAssignableFrom(cl)) {
				Set<ONDEXRelation> relations = aog
						.getRelationsOfAttributeName(an);
				if (relations.size() > 0
						&& !AppearanceSynchronizer.attr.contains(an.getId()))
					box.addItem(an.getId());
			}
		}

		panel.add(new JLabel("Select AttributeName:"));
		panel.add(box);

		sliderMaxIter = new JSlider();
		sliderMaxIter
				.setToolTipText("<html>Choose appropriate values by observing the convergence of energy.<br>"
						+ "A typical value is 100.</html>");
		sliderMaxIter.setBorder(BorderFactory
				.createTitledBorder("max iterations"));
		sliderMaxIter.setMinimum(0);
		sliderMaxIter.setMaximum(5000);
		sliderMaxIter.setValue(maxIterations);
		sliderMaxIter.setMajorTickSpacing(1000);
		sliderMaxIter.setMinorTickSpacing(100);
		sliderMaxIter.setPaintTicks(true);
		sliderMaxIter.setPaintLabels(true);
		sliderMaxIter.addChangeListener(this);
		panel.add(sliderMaxIter);

		repuExpField = new JTextField();
		repuExpField.setBorder(BorderFactory
				.createTitledBorder("repulsion exponent"));
		repuExpField
				.setToolTipText("<html>Exponent of the distance in the repulsion energy. Exception:<br>"
						+ "The value 0.0 corresponds to logarithmic repulsion.<br>"
						+ "Is 0.0 in both the LinLog and the Fruchterman-Reingold energy model.<br>"
						+ "Negative values are permitted.</html>");
		defaultBackground = repuExpField.getBackground();
		repuExpField.setText(repuExp + "");
		repuExpField.addCaretListener(this);
		panel.add(repuExpField);

		attrExpField = new JTextField();
		attrExpField.setBorder(BorderFactory
				.createTitledBorder("attraction exponent"));
		attrExpField
				.setToolTipText("<html>Exponent of the distance in the attraction energy.<br>"
						+ "Is 1.0 in the LinLog model (which is used for computing clusters, i.e. dense subgraphs),<br>"
						+ "and 3.0 in standard energy model of Fruchterman and Reingold.<br>"
						+ "Must be greater than repuExponent.</html>");
		attrExpField.setText(attrExp + "");
		attrExpField.addCaretListener(this);
		panel.add(attrExpField);

		gravFactorField = new JTextField();
		gravFactorField.setBorder(BorderFactory
				.createTitledBorder("gravitation factor"));
		gravFactorField
				.setToolTipText("<html>Factor for the gravitation energy.<br>"
						+ "Gravitation attracts each node to the barycenter of all nodes,<br>"
						+ "to prevent distances between unconnected graph components from approaching infinity.<br>"
						+ "Typical values are 0.0 if the graph is guaranteed to be connected,<br>"
						+ "and positive values significantly smaller 1.0 (e.g. 0.05) otherwise.</html>");
		gravFactorField.setText(gravFactor + "");
		gravFactorField.addCaretListener(this);
		panel.add(gravFactorField);

		ignoreLoopsBox = new JCheckBox("ignore loops");
		ignoreLoopsBox
				.setToolTipText("<html>Set to true to use an adapted version of Modularity for graphs without loops<br>"
						+ "(edges whose start node equals the end node).</html>");
		ignoreLoopsBox.setSelected(ignoreLoops);
		ignoreLoopsBox.addChangeListener(this);
		panel.add(ignoreLoopsBox);

		return panel;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		if (mbh != null && mbh.getStatus() != null)
			return mbh.getStatus();
		if (om != null && om.getStatus() != null)
			return om.getStatus();
		return status;
	}

	@Override
	public Throwable getUncaughtException() {
		return null;
	}

	@Override
	public void initialize() {
		progress = 0;
		cancelled = false;
		status = "Transforming graph into internal representation...";
		Map<ONDEXConcept, Map<ONDEXConcept, Double>> graph = readGraph();

		if (cancelled)
			return;
		graph = makeSymmetricGraph(graph);

		if (cancelled)
			return;
		Map<ONDEXConcept, Node> nameToNode = makeNodes(graph);

		if (cancelled)
			return;
		List<Node> nodes = new ArrayList<Node>(nameToNode.values());

		if (cancelled)
			return;
		List<Edge> edges = makeEdges(graph, nameToNode);

		if (cancelled)
			return;
		// only use 2 dimensions, so last argument is false
		Map<Node, double[]> nodeToPosition = makeInitialPositions(nodes, false);
		progress++;

		// see class MinimizerBarnesHut for a description of the parameters;
		// for classical "nice" layout (uniformly distributed nodes), use
		// new MinimizerBarnesHut(nodes, edges, -1.0, 2.0,
		// 0.05).minimizeEnergy(nodeToPosition, 100);
		// new MinimizerBarnesHut(nodes, edges, 0.0, 1.0, 0.05)
		if (cancelled)
			return;
		mbh = new MinimizerBarnesHut(nodes, edges, repuExp, attrExp, gravFactor);
		mbh.minimizeEnergy(nodeToPosition, maxIterations);
		mbh = null;
		progress++;

		// see class OptimizerModularity for a description of the parameters
		if (cancelled)
			return;
		om = new OptimizerModularity();
		Map<Node, Integer> nodeToCluster = om
				.execute(nodes, edges, ignoreLoops);
		om = null;

		if (cancelled)
			return;
		writePositions(nodeToPosition, nodeToCluster);
		progress++;
		status = Monitorable.STATE_TERMINAL;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	/**
	 * Converts a given graph into a list of edges.
	 * 
	 * @param graph
	 *            the graph.
	 * @param nameToNode
	 *            map from node names to nodes.
	 * @return the given graph as list of edges.
	 */
	private List<Edge> makeEdges(
			Map<ONDEXConcept, Map<ONDEXConcept, Double>> graph,
			Map<ONDEXConcept, Node> nameToNode) {
		List<Edge> result = new ArrayList<Edge>();
		for (ONDEXConcept sourceName : graph.keySet()) {
			for (ONDEXConcept targetName : graph.get(sourceName).keySet()) {
				Node sourceNode = nameToNode.get(sourceName);
				Node targetNode = nameToNode.get(targetName);
				double weight = graph.get(sourceName).get(targetName);
				result.add(new Edge(sourceNode, targetNode, weight));
			}
		}
		return result;
	}

	/**
	 * Returns, for each node in a given list, a random initial position in two-
	 * or three-dimensional space.
	 * 
	 * @param nodes
	 *            node list.
	 * @param is3d
	 *            initialize 3 (instead of 2) dimension with random numbers.
	 * @return map from each node to a random initial positions.
	 */
	private Map<Node, double[]> makeInitialPositions(List<Node> nodes,
			boolean is3d) {
		Map<Node, double[]> result = new HashMap<Node, double[]>();
		for (Node node : nodes) {
			double[] position = { Math.random() - 0.5, Math.random() - 0.5,
					is3d ? Math.random() - 0.5 : 0.0 };
			result.put(node, position);
		}
		return result;
	}

	/**
	 * Construct a map from node names to nodes for a given graph, where the
	 * weight of each node is set to its degree, i.e. the total weight of its
	 * edges.
	 * 
	 * @param graph
	 *            the graph.
	 * @return map from each node names to nodes.
	 */
	private Map<ONDEXConcept, Node> makeNodes(
			Map<ONDEXConcept, Map<ONDEXConcept, Double>> graph) {
		Map<ONDEXConcept, Node> result = new HashMap<ONDEXConcept, Node>();
		for (ONDEXConcept nodeName : graph.keySet()) {
			double nodeWeight = 0.0;
			for (double edgeWeight : graph.get(nodeName).values()) {
				nodeWeight += edgeWeight;
			}
			result.put(nodeName, new Node(nodeName, nodeWeight));
		}
		return result;
	}

	/**
	 * Returns a symmetric version of the given graph. A graph is symmetric if
	 * and only if for each pair of nodes, the weight of the edge from the first
	 * to the second node equals the weight of the edge from the second to the
	 * first node. Here the symmetric version is obtained by adding to each edge
	 * weight the weight of the inverse edge.
	 * 
	 * @param graph
	 *            possibly unsymmetric graph.
	 * @return symmetric version of the given graph.
	 */
	private Map<ONDEXConcept, Map<ONDEXConcept, Double>> makeSymmetricGraph(
			Map<ONDEXConcept, Map<ONDEXConcept, Double>> graph) {
		Map<ONDEXConcept, Map<ONDEXConcept, Double>> result = new HashMap<ONDEXConcept, Map<ONDEXConcept, Double>>();
		for (ONDEXConcept source : graph.keySet()) {
			for (ONDEXConcept target : graph.get(source).keySet()) {
				double weight = graph.get(source).get(target);
				double revWeight = 0.0f;
				if (graph.get(target) != null
						&& graph.get(target).get(source) != null) {
					revWeight = graph.get(target).get(source);
				}
				if (result.get(source) == null)
					result.put(source, new HashMap<ONDEXConcept, Double>());
				result.get(source).put(target, weight + revWeight);
				if (result.get(target) == null)
					result.put(target, new HashMap<ONDEXConcept, Double>());
				result.get(target).put(source, weight + revWeight);
			}
		}
		return result;
	}

	/**
	 * Reads and returns a graph from the specified file. The graph is returned
	 * as a nested map: Each source node of an edge is mapped to a map
	 * representing its outgoing edges. This map maps each target node of the
	 * outgoing edges to the edge weight (the weight of the edge from the source
	 * node to the target node). Schematically, source -> target -> edge weight.
	 * 
	 * @param filename
	 *            name of the file to read from.
	 * @return read graph.
	 */
	private Map<ONDEXConcept, Map<ONDEXConcept, Double>> readGraph() {
		Map<ONDEXConcept, Map<ONDEXConcept, Double>> result = new HashMap<ONDEXConcept, Map<ONDEXConcept, Double>>();

		// calculate minimum for weight
		double weight = 1.0f;
		if (an != null) {
			weight = Double.POSITIVE_INFINITY;
			for (ONDEXRelation r : graph.getEdges()) {
				Attribute attr = r.getAttribute(an);
				if (attr != null) {
					double w = ((Number) attr.getValue()).doubleValue();
					if (w < weight)
						weight = w;
				}
			}
		}

		for (ONDEXRelation r : graph.getEdges()) {
			ONDEXConcept source = r.getFromConcept();
			ONDEXConcept target = r.getToConcept();
			// parse weight from attribute

			if (an != null) {
				Attribute attr = r.getAttribute(an);
				if (attr != null) {
					weight = ((Number) attr.getValue()).doubleValue();
				}
			}
			if (result.get(source) == null)
				result.put(source, new HashMap<ONDEXConcept, Double>());
			result.get(source).put(target, weight);
		}

		return result;
	}

	@Override
	public void reset() {
		initialize();
	}

	@Override
	public void setCancelled(boolean c) {
		this.cancelled = c;
		status = Monitorable.STATE_TERMINAL;
	}

	/**
	 * Performs updates of layout parameters.
	 */
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(sliderMaxIter)) {
			maxIterations = sliderMaxIter.getValue();
		} else if (arg0.getSource().equals(ignoreLoopsBox)) {
			ignoreLoops = ignoreLoopsBox.isSelected();
		}
	}

	/**
	 * Writes a given layout and clustering into layout positions.
	 * 
	 * @param nodeToPosition
	 *            map from each node to its layout position.
	 * @param nodeToPosition
	 *            map from each node to its cluster.
	 */
	private void writePositions(Map<Node, double[]> nodeToPosition,
			Map<Node, Integer> nodeToCluster) {
		for (Node node : nodeToPosition.keySet()) {
			double[] position = nodeToPosition.get(node);
			// int cluster = nodeToCluster.get(node);
			this.setLocation(node.concept, position[0], position[1]);
		}
	}

}
