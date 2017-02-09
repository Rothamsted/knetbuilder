package net.sourceforge.ondex.ovtk2.layout;

/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 9, 2005
 */
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import edu.uci.ics.jung.graph.Graph;

/**
 * TreeLayout adapted from the JUNG2 library.
 * 
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2
 * @author taubertj
 */
public class TreeLayout extends OVTK2Layouter implements Monitorable {

	/**
	 * keep track of already processed nodes of tree
	 */
	private final Set<ONDEXConcept> allreadyDone = new HashSet<ONDEXConcept>();

	/**
	 * where to align the base of nodes
	 */
	private final Map<ONDEXConcept, Integer> basePositions = new HashMap<ONDEXConcept, Integer>();

	/**
	 * distances in X between final node positions
	 */
	private int distX = 50;

	/**
	 * distance in Y between final node positions
	 */
	private int distY = 50;

	/**
	 * current linking point of last node
	 */
	private final Point m_currentPoint = new Point();

	/**
	 * build tree for reversed edge direction
	 */
	private boolean reversed = false;

	/**
	 * all roots of graph
	 */
	private Collection<ONDEXConcept> roots;

	/**
	 * Current progress made for Monitorable
	 */
	private int progress = 0;

	/**
	 * Current state for Monitorable
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * If the process gets cancelled
	 */
	private boolean cancelled = false;

	/**
	 * Constructor to set ONDEXJUNGGraph.
	 * 
	 * @param viewer
	 *            current OVTK2PropertiesAggregator
	 */
	public TreeLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
	}

	/**
	 * Build the tree on the whole graph.
	 * 
	 */
	void buildTree() {

		// starting point of first root node
		this.m_currentPoint.setLocation(0, 20);
		if (roots.size() > 0 && graph != null) {
			// get total X size
			calculateDimensionX(roots);
			// layout every contained tree
			for (ONDEXConcept v : roots) {
				state = "building tree " + progress;
				calculateDimensionX(v);
				m_currentPoint.x += this.basePositions.get(v) / 2 + 50;
				buildTree(v, this.m_currentPoint.x);
				progress++;
				if (cancelled)
					return;
			}
		}
	}

	/**
	 * Builds a tree for root v at X position x.
	 * 
	 * @param v
	 *            ONDEXConcept root for tree
	 * @param x
	 *            current starting position
	 */
	void buildTree(ONDEXConcept v, int x) {

		// check for potential loops
		if (!allreadyDone.contains(v)) {
			allreadyDone.add(v);

			// go one level further down
			this.m_currentPoint.y += this.distY;
			this.m_currentPoint.x = x;

			this.setCurrentPositionFor(v);

			int sizeXofCurrent = basePositions.get(v);

			int lastX = x - sizeXofCurrent / 2;

			int sizeXofChild;
			int startXofChild;

			// check selection of edge direction
			Collection<ONDEXConcept> children = null;
			if (reversed)
				children = graph.getPredecessors(v);
			else
				children = graph.getSuccessors(v);

			// get new x position from children
			for (ONDEXConcept element : children) {
				sizeXofChild = this.basePositions.get(element);
				startXofChild = lastX + sizeXofChild / 2;

				// recursion over all children
				buildTree(element, startXofChild);
				lastX = lastX + sizeXofChild + distX;
			}

			// simply align them in y dimension, this is edge direction
			// dependent, otherwise it gets upside down
			this.m_currentPoint.y -= this.distY;
		}
	}

	/**
	 * Calculate the overall X dimension for a number of roots.
	 * 
	 * @param roots
	 *            Collection<ONDEXConcept> containing roots
	 * @return total X dimension
	 */
	private int calculateDimensionX(Collection<ONDEXConcept> roots) {

		int size = 0;
		for (ONDEXConcept v : roots) {

			// check selection of edge direction
			Collection<ONDEXConcept> children = null;
			if (reversed)
				children = graph.getPredecessors(v);
			else
				children = graph.getSuccessors(v);

			// check dimension for children
			int childrenNum = children.size();
			if (childrenNum != 0) {
				for (ONDEXConcept element : children) {
					// recursion step
					size += calculateDimensionX(element) + distX;
				}
			}

			// base step
			size = Math.max(0, size - distX);
			basePositions.put(v, size);
		}

		return size;
	}

	/**
	 * Recursively calculate the X dimension of the whole tree, starting at
	 * node. v.
	 * 
	 * @param v
	 *            ONDEXConcept root to calculate X dimension
	 * @return X dimension of tree starting at v
	 */
	private int calculateDimensionX(ONDEXConcept v) {

		int size = 0;

		// stack for performing a breadth first search
		LinkedList<ONDEXConcept> stack = new LinkedList<ONDEXConcept>();
		stack.add(v);

		Set<ONDEXConcept> seen = new HashSet<ONDEXConcept>();
		while (!stack.isEmpty()) {
			ONDEXConcept current = stack.pop();
			if (!seen.contains(current)) {
				// check selection of edge direction
				if (reversed)
					stack.addAll(graph.getPredecessors(current));
				else
					stack.addAll(graph.getSuccessors(current));
				// check dimension for children
				size = distX;
				size = Math.max(0, size - distX);
				basePositions.put(current, size);
				seen.add(current);
			}
		}

		return size;
	}

	/**
	 * Get all leaf nodes siblings of p.
	 * 
	 * @param p
	 *            ONDEXConcept to start at
	 * @return List<ONDEXConcept> with siblings
	 */
	public List<ONDEXConcept> getAtomics(ONDEXConcept p) {
		List<ONDEXConcept> v = new ArrayList<ONDEXConcept>();
		getAtomics(p, v);
		return v;
	}

	/**
	 * Returns the list of siblings, which are leaf nodes of the graph.
	 * 
	 * @param p
	 *            current ONDEXConcept to start with
	 * @param v
	 *            List<ONDEXConcept> contains siblings of p
	 */
	private void getAtomics(ONDEXConcept p, List<ONDEXConcept> v) {

		// check selection of edge direction
		Collection<ONDEXConcept> parents = null;
		if (reversed)
			parents = graph.getPredecessors(p);
		else
			parents = graph.getSuccessors(p);
		for (ONDEXConcept c : parents) {

			// check selection of edge direction
			Collection<ONDEXConcept> children = null;
			if (reversed)
				children = graph.getPredecessors(c);
			else
				children = graph.getSuccessors(c);

			if (children.size() == 0) {
				v.add(c);
			} else {
				getAtomics(c, v);
			}
		}
	}

	/**
	 * Get the depth of a path starting at node v recursively.
	 * 
	 * @param v
	 *            ONDEXConcept as root for this path
	 * @return length of this path
	 */
	public int getDepth(ONDEXConcept v) {
		int depth = 0;

		// check selection of edge direction
		Collection<ONDEXConcept> parents = null;
		if (reversed)
			parents = graph.getPredecessors(v);
		else
			parents = graph.getSuccessors(v);
		for (ONDEXConcept c : parents) {

			// check selection of edge direction
			Collection<ONDEXConcept> children = null;
			if (reversed)
				children = graph.getPredecessors(c);
			else
				children = graph.getSuccessors(c);

			// base: at leaf node, no more children
			if (children.size() == 0) {
				depth = 0;
			} else {
				// recursion get maximum path depth
				depth = Math.max(depth, getDepth(c));
			}
		}

		return depth + 1;
	}

	@Override
	public JPanel getOptionPanel() {

		// new option panel and layout
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// check box for reversed edge direction
		JCheckBox checkBox = new JCheckBox("reversed edge direction?");
		checkBox.setSelected(reversed);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				reversed = ((JCheckBox) e.getSource()).isSelected();
			}
		});

		// text field for X distance
		JTextField distanceX = new JTextField(String.valueOf(distX));
		distanceX.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				String text = ((JTextField) e.getSource()).getText();
				try {
					distX = Integer.parseInt(text);
				} catch (NumberFormatException nfe) {
					// result default
					distX = 50;
				}
			}
		});
		JPanel panelX = new JPanel(new BorderLayout());
		panelX.add(BorderLayout.WEST, new JLabel("Distance X: "));
		panelX.add(BorderLayout.CENTER, distanceX);

		// text field for Y distance
		JTextField distanceY = new JTextField(String.valueOf(distY));
		distanceY.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				String text = ((JTextField) e.getSource()).getText();
				try {
					distY = Integer.parseInt(text);
				} catch (NumberFormatException nfe) {
					// result default
					distY = 50;
				}
			}
		});
		JPanel panelY = new JPanel(new BorderLayout());
		panelY.add(BorderLayout.WEST, new JLabel("Distance Y: "));
		panelY.add(BorderLayout.CENTER, distanceY);

		// horizontal arrangement
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(checkBox).addComponent(panelX)
				.addComponent(panelY));

		// vertical arrangement
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(checkBox).addComponent(panelX)
				.addComponent(panelY));

		// for spacing issues
		JPanel result = new JPanel();
		result.add(panel);
		return result;
	}

	/**
	 * A root is defined as having no ancestors.
	 * 
	 * @param g
	 *            ONDEXJUNGGraph to get roots from
	 * @return Collection<ONDEXConcept> containing all root nodes
	 */
	private Collection<ONDEXConcept> getRoots(
			Graph<ONDEXConcept, ONDEXRelation> g) {
		Set<ONDEXConcept> roots = new HashSet<ONDEXConcept>();
		for (ONDEXConcept n : g.getVertices()) {
			// check selection of edge direction
			if (reversed && g.getSuccessorCount(n) == 0) {
				roots.add(n);
			}
			if (!reversed && g.getPredecessorCount(n) == 0) {
				roots.add(n);
			}
		}
		return roots;
	}

	/**
	 * This is not an incremental layout.
	 * 
	 * @see edu.uci.ics.jung.visualization.Layout#incrementsAreDone()
	 */
	public boolean incrementsAreDone() {
		return true;
	}

	@Override
	public void initialize() {

		cancelled = false;
		progress = 0;
		state = Monitorable.STATE_IDLE;

		// clear potential left overs from previous runs
		allreadyDone.clear();
		basePositions.clear();
		distX = distY = 50;
		locations.clear();

		// get roots in graph
		roots = getRoots(graph);
		if (roots.size() == 0)
			throw new IllegalStateException("Graph has no roots.");
		// else
		// System.out.println(roots);

		// construct tree
		buildTree();
	}

	@Override
	public void reset() {
		initialize();
	}

	/**
	 * Transform the current vertex position into the layout location map.
	 * 
	 * @param vertex
	 *            ONDEXConcept to set location for
	 */
	private void setCurrentPositionFor(ONDEXConcept vertex) {
		Point2D coord = transform(vertex);
		coord.setLocation(m_currentPoint);
	}

	@Override
	public int getMaxProgress() {
		if (roots != null)
			return roots.size();
		return 1;
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		return null;
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}
}
