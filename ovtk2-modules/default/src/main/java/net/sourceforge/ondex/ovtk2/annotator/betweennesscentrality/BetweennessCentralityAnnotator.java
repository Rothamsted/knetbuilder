package net.sourceforge.ondex.ovtk2.annotator.betweennesscentrality;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Implements a node ranking according to betweenness centrality. See Ulrik
 * Brandes: A Faster Algorithm for Betweenness Centrality.
 * 
 * @author taubertj
 * @version 26.03.2008
 */
public class BetweennessCentralityAnnotator extends OVTK2Annotator implements
		ActionListener, ListSelectionListener {

	// generated
	private static final long serialVersionUID = 1L;

	// contains calculated values
	private JTable table;

	// node sizes
	private JFormattedTextField minField, maxField;

	// the betweenness scores
	private Map<ONDEXConcept, Double> map;
	
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Constructor runs calculation and displays results.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public BetweennessCentralityAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// get results for graph
		map = calculate();

		// init table data
		String[] columnNames = { "id", "concept name", "score" };
		Object[][] data = new Object[map.size()][3];

		// fill table data
		ONDEXConcept[] keys = map.keySet().toArray(
				new ONDEXConcept[map.keySet().size()]);
		for (int i = 0; i < keys.length; i++) {
			Double value = map.get(keys[i]);
			data[i][0] = keys[i].getId();
			if (keys[i].getConceptName() != null)
				data[i][1] = keys[i].getConceptName().getName();
			else
				data[i][1] = "";
			data[i][2] = value;
		}

		// setup table
		table = new JTable(new CBTableModel(data, columnNames));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		// add sorting of table
		table.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				table.getModel());
		sorter.setComparator(2, new Comparator<Double>() {
			public int compare(Double o1, Double o2) {
				if (o1.equals(o2))
					return 0;
				else if (o1 < o2)
					return -1;
				else
					return 1;
			}
		});
		table.setRowSorter(sorter);

		// put into scroll pane
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		add(scrollPane);

		// input fields for node sizes
		JPanel sizeConstraints = new JPanel(new SpringLayout());
		sizeConstraints.add(new JLabel("Min Node Size"));
		minField = new JFormattedTextField(10);
		minField.setColumns(5);
		sizeConstraints.add(minField);
		sizeConstraints.add(new JLabel("Max Node Size"));
		maxField = new JFormattedTextField(30);
		maxField.setColumns(5);
		sizeConstraints.add(maxField);
		SpringUtilities.makeCompactGrid(sizeConstraints,
				sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);
		add(sizeConstraints);

		// export button
		JButton exportButton = new JButton("Export results");
		exportButton.addActionListener(this);
		exportButton.setActionCommand("export");
		add(exportButton);

		// go button
		JButton goButton = new JButton("Annotate Graph");
		goButton.addActionListener(this);
		goButton.setActionCommand("go");
		add(goButton);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.BetweennessCentrality");
	}

	/**
	 * Propagate table selection to selection of nodes in the graph.
	 */
	public void valueChanged(ListSelectionEvent e) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer()
				.getPickedVertexState();
		state.clear();

		int[] selection = table.getSelectedRows();
		for (int i = 0; i < selection.length; i++) {
			int index = table.convertRowIndexToModel(selection[i]);
			Integer id = (Integer) table.getModel().getValueAt(index, 0);
			ONDEXConcept node = graph.getConcept(id);
			state.pick(node, true);
		}
	}

	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();

		if (cmd.equals("go")) {
			Integer min = (Integer) minField.getValue();
			Integer max = (Integer) maxField.getValue();
			if (min < 5) {
				min = 5;
				minField.setValue(min);
			}
			if (min > 45) {
				min = 45;
				minField.setValue(min);
			}
			if (max < 5) {
				max = 5;
				maxField.setValue(max);
			}
			if (max > 45) {
				max = 45;
				maxField.setValue(max);
			}

			if (min > max) {
				Integer temp = min;
				min = max;
				max = temp;
				minField.setValue(min);
				maxField.setValue(max);
			}

			// update graphs node shapes
			final Map<ONDEXConcept, Integer> amplification = resizeNodes(min,
					max, map);
			ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
			nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
				@Override
				public Integer transform(ONDEXConcept input) {
					return amplification.get(input);
				}
			});
			nodeShapes.updateAll();

			viewer.getVisualizationViewer().getModel().fireStateChanged();
			viewer.getVisualizationViewer().repaint();

			// add value as Attribute
			annotateNodes(map);
		}

		// here excel spreadsheet export
		else if (cmd.equals("export")) {
			try {
				String excelFileName = null;

				// filechooser for excel files
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						String ext = f.getAbsolutePath();
						if (ext.lastIndexOf('.') > -1) {
							ext = ext.substring(ext.lastIndexOf('.') + 1,
									ext.length());
							if (ext.equals("xls"))
								return true;
						}
						return false;
					}

					@Override
					public String getDescription() {
						return ".xls Excel Files";
					}
				});

				// get file chooser filename
				int returnVal = chooser.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					excelFileName = chooser.getSelectedFile().getAbsolutePath();
				}

				if (excelFileName != null) {

					// get database connection
					Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
					String excelDatabase = "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ=";
					excelDatabase += excelFileName.trim()
							+ ";DriverID=790;READONLY=false}";
					Connection excelCon = DriverManager.getConnection(
							excelDatabase, "", "");
					if (!excelCon.isClosed())
						System.out
								.println("Successfully Connected To Excel for "
										+ excelFileName);

					// create insert statements
					Statement excelStat;
					excelStat = excelCon.createStatement();

					// "id", "concept name", "score"
					excelStat
							.executeUpdate("CREATE TABLE betweenness (id NUMBER, concept_name TEXT, score NUMBER)");
					for (int rowI = 0; rowI < table.getRowCount(); rowI++) {
						String sqlInsert = "INSERT INTO [betweenness$] (id, concept_name, score) VALUES ("
								+ table.getValueAt(rowI, 0)
								+ ", '"
								+ table.getValueAt(rowI, 1)
								+ "',"
								+ table.getValueAt(rowI, 2) + ")";
						// System.out.println(sqlInsert);
						excelStat.executeUpdate(sqlInsert);
					}
					excelStat.close();
					excelCon.commit();
					excelCon.close();
				}
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}

	/**
	 * Annotates the nodes according to specified values.
	 * 
	 * @param map
	 *            Map<ONDEXConcept, Double>
	 */
	private void annotateNodes(Map<ONDEXConcept, Double> map) {

		AttributeName an = graph.getMetaData().getFactory()
				.createAttributeName("Betweenness_centrality", Double.class);

		// scale node size according to percentage
		for (ONDEXConcept c : map.keySet()) {
			Double percentBase = map.get(c);
			if (c.getAttribute(an) == null)
				c.createAttribute(an, percentBase, false);
			else
				c.getAttribute(an).setValue(percentBase);
		}
	}

	/**
	 * Calculates amplification of the nodes according to specified values.
	 * 
	 * @param targMin
	 *            int
	 * @param targMax
	 *            int
	 * @param map
	 *            Map<ONDEXConcept, Double>
	 * @return Map<ONDEXConcept, Integer>
	 */
	private Map<ONDEXConcept, Integer> resizeNodes(int targMin, int targMax,
			Map<ONDEXConcept, Double> map) {

		int sizeRange = targMax - targMin;

		// processed values for node sizes
		Map<ONDEXConcept, Integer> amplification = LazyMap.decorate(
				new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultNodeSize;
					}
				});

		// scale node size according to percentage
		for (ONDEXConcept c : map.keySet()) {
			double percentBase = map.get(c);
			// this is the base size
			if (percentBase == 0) {
				amplification.put(c,
						(int) Math.floor(targMin + (sizeRange / 2)));
			}
			// actual new size
			double width = targMin + (percentBase * sizeRange);
			amplification.put(c, (int) Math.floor(width));
		}

		return amplification;
	}

	/**
	 * Calculates betweenness centrality on the ONDEX graph.
	 * 
	 * @return Map<ONDEXConcept, Double>
	 */
	private Map<ONDEXConcept, Double> calculate() {

		// CB[v] <- 0, v e V
		Map<ONDEXConcept, Double> CB = LazyMap.decorate(
				new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
					@Override
					public Double create() {
						return 0.0;
					}
				});

		// iterate over all concepts
		for (ONDEXConcept s : graph.getConcepts()) {

			// S <- empty stack
			Stack<ONDEXConcept> S = new Stack<ONDEXConcept>();

			// P[w] <- empty list, w e V
			Map<ONDEXConcept, List<ONDEXConcept>> P = new Hashtable<ONDEXConcept, List<ONDEXConcept>>();

			// rho[t] <- 0, t e V
			Map<ONDEXConcept, Integer> rho = LazyMap.decorate(
					new HashMap<ONDEXConcept, Integer>(),
					new Factory<Integer>() {
						@Override
						public Integer create() {
							return 0;
						}
					});

			// rho[s] <- 1
			rho.put(s, 1);

			// d[t] <- -1, t e V
			Map<ONDEXConcept, Integer> d = LazyMap.decorate(
					new HashMap<ONDEXConcept, Integer>(),
					new Factory<Integer>() {
						@Override
						public Integer create() {
							return -1;
						}
					});

			// d[s] <- 0
			d.put(s, 0);

			// Q <- empty queue
			LinkedBlockingQueue<ONDEXConcept> Q = new LinkedBlockingQueue<ONDEXConcept>();

			// enqueue s -> Q
			Q.offer(s);

			// while Q not empty do
			while (!Q.isEmpty()) {

				// dequeue v <- Q
				ONDEXConcept v = Q.poll();

				// push v -> S
				S.push(v);

				// foreach neighbor w of v do
				for (ONDEXRelation r : graph.getRelationsOfConcept(v)) {
					ONDEXConcept from = r.getFromConcept();
					ONDEXConcept to = r.getToConcept();
					ONDEXConcept w = null;
					if (!from.equals(v))
						w = from;
					else if (!to.equals(v))
						w = to;
					else
						continue; // or w = v;
					// w found for the first time?
					// if d[w] < 0 then
					if (d.get(w) < 0) {

						// enqueue w -> Q
						Q.offer(w);

						// d[w] <- d[v] + 1
						d.put(w, d.get(v) + 1);
					}
					// shortest path to w via v?
					// if d[w] = d[v] + 1
					if (d.get(w) == d.get(v) + 1) {

						// rho[w] <- rho[w] + rho[v]
						rho.put(w, rho.get(w) + rho.get(v));

						// append v -> P[w]
						if (!P.containsKey(w))
							P.put(w, new ArrayList<ONDEXConcept>());
						P.get(w).add(v);
					}
				}
			}

			// delta[v] <- 0, v e V
			Map<ONDEXConcept, Double> delta = LazyMap.decorate(
					new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
						@Override
						public Double create() {
							return 0.0;
						}
					});

			// S returns vertices in order of non-increasing distance from s
			// while S not empty do
			while (!S.isEmpty()) {

				// pop w <- S
				ONDEXConcept w = S.pop();

				// for v e P[w] do
				if (P.containsKey(w)) {
					for (ONDEXConcept v : P.get(w)) {

						// delta[v] <- delta[v] + rho[v] / rho[w] * (1 +
						// delta[w])
						delta.put(v, delta.get(v)
								+ ((double) rho.get(v) / (double) rho.get(w))
								* (1.0 + delta.get(w)));
					}
				}

				// if w != s
				if (!w.equals(s)) {

					// CB[w] <- CB[w] + delta[w]
					CB.put(w, CB.get(w) + delta.get(w));
				}
			}
		}

		// normalise results to [0,1]
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		ONDEXConcept[] keys = CB.keySet().toArray(new ONDEXConcept[0]);
		for (int i = 0; i < keys.length; i++) {
			double value = CB.get(keys[i]);
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}

		double diff = max - min;
		for (int i = 0; i < keys.length; i++) {
			CB.put(keys[i], (CB.get(keys[i]) - min) / diff);
		}

		return CB;
	}

	/**
	 * Simple model, that is not edit able.
	 * 
	 * @author taubertj
	 * @version 26.03.2008
	 */
	private class CBTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = 1L;

		private String[] columnNames = null;

		private Object[][] data = null;

		public CBTableModel(Object[][] data, String[] columnNames) {
			this.columnNames = columnNames;
			this.data = data;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		// not edit able

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
