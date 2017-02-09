package net.sourceforge.ondex.ovtk2.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Layouter which places each concept class on a separate hierarchical layer.
 * 
 * @author taubertj
 */
public class ConceptClassHierarchicalLayout extends OVTK2Layouter implements
		ActionListener, Monitorable {

	private Random generator = new Random();

	private JComboBox box = new JComboBox(new String[] { "None" });

	private JCheckBox arrangeValues = new JCheckBox(
			"Arrange according to values");

	private JCheckBox drawLines = new JCheckBox(
			"Draw category horizontal lines");

	private Paintable grid = null;

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
	 * Constructor sets OVTK2PropertiesAggregator.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public ConceptClassHierarchicalLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		box.addActionListener(this);
	}

	public void reset() {
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uci.ics.jung.visualization.layout.AbstractLayout#setSize(java.awt
	 * .Dimension)
	 */

	@Override
	public void setSize(Dimension d) {
		super.setSize(d);
		initialize();
	}

	public void initialize() {

		cancelled = false;
		progress = 0;
		state = Monitorable.STATE_IDLE;

		// just to make sure we are not carrying it over to another layout
		if (grid != null)
			viewer.removePreRenderPaintable(grid);
		grid = null;

		Dimension d = getSize();
		Graph<ONDEXConcept, ONDEXRelation> graph = this.getGraph();
		if (graph != null && d != null) {

			double height = d.getHeight();
			double width = d.getWidth();

			// number of layers, one for each concept class
			int count_layers = 0;

			state = "sorting nodes";
			Map<Comparable<?>, Map<ONDEXConcept, Integer>> comparable2nodes = new Hashtable<Comparable<?>, Map<ONDEXConcept, Integer>>();
			Map<ONDEXConcept, Comparable<?>> node2comparable = new Hashtable<ONDEXConcept, Comparable<?>>();

			if (!box.getSelectedItem().equals("None")) {
				// get nodes per attribute name value
				AttributeName an = (AttributeName) box.getSelectedItem();
				ONDEXConcept[] nodes = graph.getVertices().toArray(
						new ONDEXConcept[0]);
				for (int i = 0; i < nodes.length; i++) {
					ONDEXConcept n = nodes[i];
					Attribute attribute = n.getAttribute(an);
					if (attribute != null) {
						Comparable<?> value = (Comparable<?>) attribute
								.getValue();
						if (!comparable2nodes.containsKey(value)) {
							count_layers++;
							comparable2nodes.put(value,
									new Hashtable<ONDEXConcept, Integer>());
						}
						comparable2nodes.get(value).put(n, Integer.valueOf(i));
						node2comparable.put(n, value);
					} else {
						if (!comparable2nodes.containsValue("none")) {
							count_layers++;
							comparable2nodes.put("none",
									new Hashtable<ONDEXConcept, Integer>());
						}
						comparable2nodes.get("none").put(n, Integer.valueOf(i));
						node2comparable.put(n, "none");
					}

					progress++;
					if (cancelled)
						return;
				}
			} else {
				// get nodes per concept classes
				ONDEXConcept[] nodes = graph.getVertices().toArray(
						new ONDEXConcept[0]);
				for (int i = 0; i < nodes.length; i++) {
					ONDEXConcept n = nodes[i];
					ConceptClass cc = n.getOfType();
					if (!comparable2nodes.containsKey(cc)) {
						count_layers++;
						comparable2nodes.put(cc,
								new Hashtable<ONDEXConcept, Integer>());
					}
					comparable2nodes.get(cc).put(n, Integer.valueOf(i));
					node2comparable.put(n, cc);

					progress++;
					if (cancelled)
						return;
				}
			}

			state = "calculating node positions";
			if (count_layers > 1) {

				// contains ordering of layers vertical
				Comparable<?>[] comparableLayers = new Comparable[count_layers];

				// randomly fill layers
				Iterator<Comparable<?>> itcc = comparable2nodes.keySet()
						.iterator();
				for (int i = 0; itcc.hasNext(); i++) {
					comparableLayers[i] = itcc.next();
				}

				// arrange layers
				if (!arrangeValues.isSelected()) {
					orderLayers(comparableLayers, comparable2nodes,
							node2comparable, 0);
				} else {
					// simply sort by values of each layer
					Arrays.sort(comparableLayers);
				}

				// arrange nodes in layer
				orderNodes(comparableLayers, comparable2nodes, node2comparable,
						0, 0);

				// vertical spacing
				double verticalSpacing = 0.8 * height / count_layers;

				// vertical offset for first layer
				double verticalOffset = verticalSpacing / 2;

				// layout each layer
				int j = 0;
				for (Comparable<?> cc : comparableLayers) {

					// nodes of current layer
					ONDEXConcept[] list = comparable2nodes.get(cc).keySet()
							.toArray(new ONDEXConcept[0]);

					// calculate equal spacing
					double spacing = 0.8 * width / list.length;

					// first node gets an offset
					double offset = spacing / 2;

					for (int l = 0; l < list.length; l++) {
						Point2D coord = transform(list[l]);
						coord.setLocation(offset + l * spacing, verticalOffset
								+ j * verticalSpacing);

						progress++;
						if (cancelled)
							return;
					}

					j++;
				}

				if (drawLines.isSelected()) {
					grid = new DataGrid<ONDEXConcept, ONDEXRelation>(viewer,
							comparable2nodes);
					viewer.addPreRenderPaintable(grid);
					viewer.repaint();
				}
			} else {

				// layout all concepts on one big line
				ONDEXConcept[] vertices = getGraph().getVertices().toArray(
						new ONDEXConcept[0]);

				// calculate equal spacing
				double spacing = 0.8 * width / vertices.length;

				// first node gets an offset
				double offset = spacing / 2;

				for (int i = 0; i < vertices.length; i++) {
					Point2D coord = transform(vertices[i]);
					coord.setLocation(offset + i * spacing, height / 2);

					progress++;
					if (cancelled)
						return;
				}
			}
		}

		state = Monitorable.STATE_TERMINAL;
	}

	/**
	 * Draws the category lines first on the graph.
	 */
	static class DataGrid<V, E> implements Paintable {

		Layout<V, E> layout;
		VisualizationViewer<V, E> vv;
		Map<Comparable<?>, Map<V, Integer>> comparable2nodes;

		public DataGrid(VisualizationViewer<V, E> vv,
				Map<Comparable<?>, Map<V, Integer>> comparable2nodes) {
			this.layout = vv.getGraphLayout();
			this.vv = vv;
			this.comparable2nodes = comparable2nodes;
		}

		@Override
		public void paint(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;

			// getting the transforming right
			AffineTransform oldXform = g2d.getTransform();
			AffineTransform lat = vv.getRenderContext()
					.getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
					.getTransform();
			AffineTransform vat = vv.getRenderContext()
					.getMultiLayerTransformer().getTransformer(Layer.VIEW)
					.getTransform();
			AffineTransform at = new AffineTransform();
			at.concatenate(g2d.getTransform());
			at.concatenate(vat);
			at.concatenate(lat);
			g2d.setTransform(at);

			// adapting line colours
			Color old = g.getColor();
			Color lineColor = vv.getBackground();
			g.setColor(lineColor);
			g.setColor(Color.gray);

			// get minimum and maximum coordinates
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			for (Comparable<?> key : comparable2nodes.keySet()) {
				for (V n : comparable2nodes.get(key).keySet()) {
					Point2D p = layout.transform(n);
					if (p.getX() < minX)
						minX = p.getX();
					if (p.getX() > maxX)
						maxX = p.getX();
				}
			}

			// just in case there is really only one node per category
			if (minX == maxX) {
				minX -= 10;
				maxX += 10;
			}

			// add 5% of additional space to each side as layout is limited to
			// 80% of total size of visualisation window
			double width = maxX - minX;
			minX = minX - 0.05 * width;
			maxX = maxX + 0.05 * width;

			// draw one line per category
			for (Comparable<?> key : comparable2nodes.keySet()) {
				Point2D p = layout.transform(comparable2nodes.get(key).keySet()
						.iterator().next());
				Shape line = new Line2D.Double(minX, p.getY(), maxX, p.getY());
				g2d.draw(line);
				g2d.drawString(key.toString(), (float) minX,
						(float) (p.getY() - 3));

			}

			// reset transform and colour
			g2d.setTransform(oldXform);
			g.setColor(old);
		}

		@Override
		public boolean useTransform() {
			return false;
		}

	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		if (graph != null) {

			// panel for attribute name selection
			JPanel select = new JPanel();
			BoxLayout selectLayout = new BoxLayout(select, BoxLayout.PAGE_AXIS);
			select.setLayout(selectLayout);
			select.setBorder(new TitledBorder(
					"Select Value corresponding to a layer:"));
			panel.add(select);

			// combo box to select attribute name
			populateComboBox(box);
			select.add(box);

			// re-populate combo box
			JButton refresh = new JButton("Refresh list");
			refresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					populateComboBox(box);
				}
			});
			select.add(refresh);

			// panel for layer arrangement to sort values in
			JPanel arrangeLayers = new JPanel(new GridLayout(1, 1));
			arrangeLayers.setBorder(new TitledBorder(
					"How to arrange layers vertically:"));
			panel.add(arrangeLayers);
			arrangeLayers.add(arrangeValues);

			// panel for drawing additional information on the graph
			JPanel drawPanel = new JPanel(new GridLayout(1, 1));
			drawPanel.setBorder(new TitledBorder(
					"Draw additional information on graph:"));
			panel.add(drawPanel);
			drawPanel.add(drawLines);

			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
					layout.createParallelGroup().addComponent(select)
							.addComponent(arrangeLayers)
							.addComponent(drawPanel)));
			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addComponent(select, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(arrangeLayers, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(drawPanel, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE));

		} else {
			panel.add(new JLabel("No options available."));
		}

		return panel;
	}

	/**
	 * Populates given combo box with list of AttributeNames for visible
	 * concepts.
	 * 
	 * @param box
	 *            JComboBox to fill
	 */
	private void populateComboBox(JComboBox box) {
		Set<AttributeName> ans = new HashSet<AttributeName>();

		for (ONDEXConcept n : graph.getVertices()) {
			for (Attribute g : n.getAttributes()) {
				if (!AppearanceSynchronizer.attr
						.contains(g.getOfType().getId()))
					ans.add(g.getOfType());
			}
		}

		box.removeAllItems();
		box.addItem("None");

		if (ans.size() > 0) {
			AttributeName[] array = ans.toArray(new AttributeName[0]);
			Arrays.sort(array);
			for (AttributeName an : array) {
				box.addItem(an);
			}
			box.revalidate();
		}
	}

	/**
	 * Recursive function for arranging specific layers.
	 * 
	 * @param comparableLayers
	 *            Comparable<?>[]
	 * @param comparable2nodes
	 *            Map<Comparable<?>, Map<ONDEXConcept, Integer>>
	 * @param node2comparable
	 *            Map<ONDEXConcept, Comparable<?>>
	 * @param nonemove
	 *            int
	 * @return Comparable<?>[]
	 */
	private Comparable<?>[] orderLayers(Comparable<?>[] comparableLayers,
			Map<Comparable<?>, Map<ONDEXConcept, Integer>> comparable2nodes,
			Map<ONDEXConcept, Comparable<?>> node2comparable, int nonemove) {

		if (nonemove == comparableLayers.length) {
			return comparableLayers;
		} else {

			// reverse map Comparable<?> to level
			Map<Comparable<?>, Integer> reverse = new Hashtable<Comparable<?>, Integer>();
			for (int i = 0; i < comparableLayers.length; i++) {
				reverse.put(comparableLayers[i], i);
			}

			// randomly select pair of Comparable<?> under consideration
			int step = generator.nextInt(comparableLayers.length);
			Comparable<?> first = comparableLayers[step];
			Comparable<?> second = null;
			do {
				step = generator.nextInt(comparableLayers.length);
				second = comparableLayers[step];
			} while (first.equals(second));

			// get current total relation lengths
			int total = 0;
			for (Comparable<?> cc : comparableLayers) {
				for (ONDEXConcept node : comparable2nodes.get(cc).keySet()) {
					for (ONDEXConcept neighbor : this.getGraph().getNeighbors(
							node)) {
						Comparable<?> otherCC = node2comparable.get(neighbor);
						total += Math.abs(reverse.get(otherCC)
								- reverse.get(cc));
					}
				}
			}

			// now do the swap
			Integer oldLayer = reverse.get(first);
			reverse.put(first, reverse.get(second));
			reverse.put(second, oldLayer);

			// get new total relation lengths
			int newtotal = 0;
			for (Comparable<?> cc : comparableLayers) {
				for (ONDEXConcept node : comparable2nodes.get(cc).keySet()) {
					for (ONDEXConcept neighbor : this.getGraph().getNeighbors(
							node)) {
						Comparable<?> otherCC = node2comparable.get(neighbor);
						newtotal += Math.abs(reverse.get(otherCC)
								- reverse.get(cc));
					}
				}
			}

			// successful swap
			if (newtotal < total) {
				nonemove = 0;
				// reorder layers array
				for (Comparable<?> cc : reverse.keySet()) {
					comparableLayers[reverse.get(cc)] = cc;
				}
			} else {
				nonemove++;
			}

			return orderLayers(comparableLayers, comparable2nodes,
					node2comparable, nonemove);
		}
	}

	/**
	 * Recursive creation of successive layer ordering.
	 * 
	 * @param comparableLayers
	 *            Comparable<?>[]
	 * @param comparable2nodes
	 *            Map<Comparable<?>, Map<ONDEXConcept, Integer>>
	 * @param node2comparable
	 *            Map<ONDEXConcept, Comparable<?>>
	 * @param step
	 *            int
	 * @param nonemove
	 *            int
	 * @return Map<Comparable<?>, Map<ONDEXConcept, Integer>>
	 */
	private Map<Comparable<?>, Map<ONDEXConcept, Integer>> orderNodes(
			Comparable<?>[] comparableLayers,
			Map<Comparable<?>, Map<ONDEXConcept, Integer>> comparable2nodes,
			Map<ONDEXConcept, Comparable<?>> node2comparable, int step,
			int nonemove) {

		if (nonemove == comparableLayers.length) {
			return comparable2nodes;
		} else {

			// get Comparable<?> under consideration
			Comparable<?> cc = comparableLayers[step];
			if (step == comparableLayers.length - 1) {
				step = 0;
			} else {
				step++;
			}

			// get current layer
			ONDEXConcept[] nodes = comparable2nodes.get(cc).keySet()
					.toArray(new ONDEXConcept[0]);
			boolean swap = false;
			for (int i = 0; i < nodes.length - 1; i++) {

				// proceed pairwise
				ONDEXConcept first = nodes[i];
				ONDEXConcept second = nodes[i + 1];

				int firstPos = comparable2nodes.get(cc).get(first);
				int secondPos = comparable2nodes.get(cc).get(second);

				// look at neighbours
				int firstTotal = 0;
				int secondTotal = 0;
				for (ONDEXConcept neighbor : this.getGraph()
						.getNeighbors(first)) {
					Comparable<?> neighborCC = node2comparable.get(neighbor);
					int neighborPos = comparable2nodes.get(neighborCC).get(
							neighbor);
					firstTotal += Math.abs(firstPos - neighborPos);
					secondTotal += Math.abs(secondPos - neighborPos);
				}

				if (firstTotal < secondTotal) {
					comparable2nodes.get(cc).put(first,
							Integer.valueOf(secondPos));
					comparable2nodes.get(cc).put(second,
							Integer.valueOf(firstPos));
					swap = true;
				}
			}

			// track swaps
			if (swap) {
				nonemove = 0;
			} else {
				nonemove++;
			}

			return orderNodes(comparableLayers, comparable2nodes,
					node2comparable, step, nonemove);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (box.getSelectedItem() instanceof AttributeName) {
			AttributeName an = (AttributeName) box.getSelectedItem();
			Class<?> clazz = an.getDataType();
			if (!Comparable.class.isAssignableFrom(clazz)) {
				JOptionPane
						.showMessageDialog(
								OVTK2Desktop.getInstance().getMainFrame(),
								"Selected values are not comparable. Please make a different selection.",
								"Error in value selection",
								JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public int getMaxProgress() {
		return 2 * getGraph().getVertexCount();
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
