package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

/**
 * Showing table for change of visibility of nodes.
 * 
 * @author taubertj
 */
public class DialogNodes extends OVTK2Dialog {

	/**
	 * Wraps the list of nodes into a table model.
	 * 
	 * @author taubertj
	 */
	private class NodesTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -1136701487783067755L;

		// table header
		private String[] columnNames = new String[] { "", Config.language.getProperty("Dialog.Nodes.TableLabel"), Config.language.getProperty("Dialog.Nodes.ConceptClassLabel"), Config.language.getProperty("Dialog.Nodes.TableVisible"), Config.language.getProperty("Dialog.Nodes.TableLabelVisible") };

		// contains node to visible mapping
		private Map<ONDEXConcept, Boolean> nodes = null;

		/**
		 * Constructor for a given mapping node to visible.
		 * 
		 * @param nodes
		 *            Map<ONDEXConcept, Boolean>
		 */
		public NodesTableModel(Map<ONDEXConcept, Boolean> nodes) {
			this.nodes = nodes;
		}

		public Map<ONDEXConcept, Boolean> getData() {
			return nodes;
		}

		public int getRowCount() {
			return nodes.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			ONDEXConcept[] keys = nodes.keySet().toArray(new ONDEXConcept[0]);
			// return existing data
			if (col == 0) {
				return keys[row].getId();
			} else if (col == 1) {
				return labels.get(keys[row]);
			} else if (col == 2) {
				return cc_labels.get(keys[row]);
			} else if (col == 3) {
				return nodes.get(keys[row]);
			} else if (col == 4) {
				return nlv.get(keys[row]);
			}

			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return Integer.class;
			} else if (col == 1) {
				return String.class;
			} else if (col == 2) {
				return String.class;
			} else if (col == 3) {
				return Boolean.class;
			} else if (col == 4) {
				return Boolean.class;
			}
			return null;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			// change to visible flag
			if (col == 3) {
				ONDEXConcept[] keys = nodes.keySet().toArray(new ONDEXConcept[0]);
				ONDEXConcept key = keys[row];
				nodes.put(key, (Boolean) value);
				this.fireTableDataChanged();
			}
			if (col == 4) {
				ONDEXConcept[] keys = nodes.keySet().toArray(new ONDEXConcept[0]);
				ONDEXConcept key = keys[row];
				nlv.put(key, (Boolean) value);
				this.fireTableDataChanged();
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 3 || col == 4)
				return true;

			return false;
		}
	}

	// generated
	private static final long serialVersionUID = -8204576861373579031L;

	// level
	private JSpinner level = new JSpinner(new SpinnerNumberModel());

	// calling ovtk2viewer
	private OVTK2Viewer viewer = null;

	// wrapped ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// model containing mapping node to visible
	private NodesTableModel model = null;

	// table for displaying model
	private JTable table = null;

	// panel for scroll pane
	private JPanel tablePanel = null;

	// buttons to toggle table selection
	private JButton visibleAll, labelsAll;

	// mapping node id to node label
	private Map<ONDEXConcept, String> labels = null;
	private Map<ONDEXConcept, String> cc_labels = null;
	private Map<ONDEXConcept, Boolean> nlv = null;

	// visible attribute
	private AttributeName an = null;
	private AttributeName anNLV = null;

	/**
	 * Constructs user input to edit nodes visibility.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public DialogNodes(OVTK2Viewer viewer) {
		super("Dialog.Nodes.Title", "Properties16.gif");

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setViewer(viewer);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		JPanel south = new JPanel(new GridLayout(2, 1));
		south.add(new ConceptNeighbourhood());
		south.add(makeButtonsPanel("Dialog.Nodes.Apply", "Dialog.Nodes.Cancel"));
		this.getContentPane().add(south, BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Sets viewer to be used in nodes view.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public void setViewer(OVTK2Viewer viewer) {

		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();

		// make sure visible attribute name is there
		if (!graph.getMetaData().checkAttributeName("visible")) {
			this.an = graph.getMetaData().getFactory().createAttributeName("visible", Boolean.class);
		} else {
			this.an = graph.getMetaData().getAttributeName("visible");
		}

		// make sure visibleNLV attribute name is there
		if (!graph.getMetaData().checkAttributeName("visibleNLV")) {
			this.anNLV = graph.getMetaData().getFactory().createAttributeName("visibleNLV", Boolean.class);
		} else {
			this.anNLV = graph.getMetaData().getAttributeName("visibleNLV");
		}

		Set<ONDEXConcept> pickedNodes = viewer.getPickedNodes();

		// get node visibility
		labels = new HashMap<ONDEXConcept, String>();
		cc_labels = new HashMap<ONDEXConcept, String>();
		nlv = viewer.getNodeLabels().getMask();

		// create visibility pattern
		Map<ONDEXConcept, Boolean> visible = new HashMap<ONDEXConcept, Boolean>();
		for (ONDEXConcept node : graph.getConcepts()) {
			labels.put(node, viewer.getNodeLabels().getLabel(node));
			cc_labels.put(node, node.getOfType().toString());
			visible.put(node, graph.isVisible(node));
		}

		// selection according to pick state
		if (pickedNodes.size() > 0) {
			for (ONDEXConcept n : visible.keySet()) {
				visible.put(n, false);
			}
			for (ONDEXConcept n : pickedNodes) {
				visible.put(n, true);
			}
		}

		// setup table
		model = new NodesTableModel(visible);
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		if (viewer.getONDEXJUNGGraph().getConcepts().size() < 2000)
			table.setAutoCreateRowSorter(true);

		if (tablePanel != null) {
			tablePanel.removeAll();
			tablePanel.add(new JScrollPane(table));
			tablePanel.revalidate();
		}
	}

	/**
	 * Returns current viewer.
	 * 
	 * @return OVTK2Viewer
	 */
	public OVTK2Viewer getViewer() {
		return viewer;
	}

	/**
	 * Creates the properties panel for nodes.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel(new BorderLayout());

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Nodes.Nodes"));
		properties.setBorder(propertiesBorder);

		TableColumn preferred = table.getColumnModel().getColumn(3);
		int width = Config.language.getProperty("Dialog.Nodes.TableVisible").length() * 7;
		preferred.setMaxWidth(width);
		preferred.setMinWidth(width);

		TableColumn preferredNLV = table.getColumnModel().getColumn(4);
		int widthNLV = Config.language.getProperty("Dialog.Nodes.TableLabelVisible").length() * 7;
		preferredNLV.setMaxWidth(widthNLV);
		preferredNLV.setMinWidth(widthNLV);

		TableColumn id = table.getColumnModel().getColumn(0);
		id.setMaxWidth(40);
		id.setMinWidth(40);

		// add table to properties
		JScrollPane scrollPane = new JScrollPane(table);
		tablePanel = new JPanel(new GridLayout(1, 1));
		tablePanel.add(scrollPane);
		properties.add(tablePanel, BorderLayout.CENTER);

		JPanel gdsPane = new JPanel();
		BoxLayout layout = new BoxLayout(gdsPane, BoxLayout.LINE_AXIS);
		gdsPane.setLayout(layout);
		properties.add(gdsPane, BorderLayout.SOUTH);

		JButton load = new JButton(Config.language.getProperty("Dialog.Nodes.Load"));
		load.setActionCommand("load");
		load.addActionListener(this);
		gdsPane.add(load);

		JButton store = new JButton(Config.language.getProperty("Dialog.Nodes.Store"));
		store.setActionCommand("store");
		store.addActionListener(this);
		gdsPane.add(store);

		visibleAll = new JButton(Config.language.getProperty("Dialog.Nodes.DeSelectAllVisible"));
		visibleAll.setActionCommand("visible");
		visibleAll.addActionListener(this);
		gdsPane.add(visibleAll);

		labelsAll = new JButton(Config.language.getProperty("Dialog.Nodes.SelectAllLabels"));
		labelsAll.setActionCommand("labels");
		labelsAll.addActionListener(this);
		gdsPane.add(labelsAll);

		return properties;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// sync visibility
		if (cmd.equals("apply")) {

			StateEdit edit = new StateEdit(new VisibilityUndo(graph), this.getName());

			// this is for node visibility
			Map<ONDEXConcept, Boolean> visible = model.getData();
			for (ONDEXConcept concept : visible.keySet()) {
				graph.setVisibility(concept, visible.get(concept));
			}

			// backward sync just in case
			Set<ONDEXConcept> visibleSet = new HashSet<ONDEXConcept>();
			for (ONDEXConcept concept : graph.getConcepts()) {
				if (graph.isVisible(concept))
					visibleSet.add(concept);
			}

			// include neighbours of given level
			int levelValue = (Integer) level.getValue();
			BitSet expanded = new BitSet();
			if (levelValue > 0) {
				for (ONDEXConcept concept : visibleSet) {
					BitSet[] temp = StandardFunctions.getNeighboursAtLevel(concept, graph, levelValue);
					expanded.or(temp[0]);
				}
				for (int i = expanded.nextSetBit(0); i >= 0; i = expanded.nextSetBit(i + 1))
					visibleSet.add(graph.getConcept(i));
			}

			// set neighbours visible too
			for (ONDEXConcept concept : visibleSet)
				graph.setVisibility(concept, true);

			// update node labels with new mask
			viewer.getVisualizationViewer().getModel().fireStateChanged();

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(viewer);
		}

		// cancel dialog
		else if (cmd.equals("cancel")) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
		}

		// load visible flags from Attribute
		else if (cmd.equals("load")) {

			// clear existing visibility list
			Map<ONDEXConcept, Boolean> visible = model.getData();
			for (ONDEXConcept concept : visible.keySet()) {
				visible.put(concept, false);
			}

			// fill from Attribute
			for (ONDEXConcept concept : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = concept.getAttribute(an);
				visible.put(concept, (Boolean) attribute.getValue());
			}

			// clear existing visibility list
			nlv.clear();

			// fill from Attribute
			for (ONDEXConcept concept : graph.getConceptsOfAttributeName(anNLV)) {
				Attribute attribute = concept.getAttribute(anNLV);
				nlv.put(concept, ((Boolean) attribute.getValue()).booleanValue());
			}

			model.fireTableDataChanged();
		}

		// store visible flags to Attribute
		else if (cmd.equals("store")) {
			Map<ONDEXConcept, Boolean> visible = model.getData();
			for (ONDEXConcept concept : visible.keySet()) {
				Attribute attribute = concept.getAttribute(an);
				// set node visibility
				if (attribute == null) {
					concept.createAttribute(an, visible.get(concept), false);
				} else {
					attribute.setValue(visible.get(concept));
				}
				// set label visibility
				attribute = concept.getAttribute(anNLV);
				if (attribute == null) {
					concept.createAttribute(anNLV, nlv.get(concept), false);
				} else {
					attribute.setValue(nlv.get(concept));
				}
			}
		}

		// toggle all visible
		else if (cmd.equals("visible")) {
			if (visibleAll.getText().equals(Config.language.getProperty("Dialog.Nodes.SelectAllVisible"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(true, x, table.convertColumnIndexToView(3));
				}
				visibleAll.setText(Config.language.getProperty("Dialog.Nodes.DeSelectAllVisible"));
			} else if (visibleAll.getText().equals(Config.language.getProperty("Dialog.Nodes.DeSelectAllVisible"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(false, x, table.convertColumnIndexToView(3));
				}
				visibleAll.setText(Config.language.getProperty("Dialog.Nodes.SelectAllVisible"));
			}
			visibleAll.repaint();
		}

		// toggle all labels
		else if (cmd.equals("labels")) {
			if (labelsAll.getText().equals(Config.language.getProperty("Dialog.Nodes.SelectAllLabels"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(true, x, table.convertColumnIndexToView(4));
				}
				labelsAll.setText(Config.language.getProperty("Dialog.Nodes.DeSelectAllLabels"));
			} else if (labelsAll.getText().equals(Config.language.getProperty("Dialog.Nodes.DeSelectAllLabels"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(false, x, table.convertColumnIndexToView(4));
				}
				labelsAll.setText(Config.language.getProperty("Dialog.Nodes.SelectAllLabels"));
			}
			labelsAll.repaint();
		}
	}

	private class ConceptNeighbourhood extends JPanel {
		/**
		 * generated
		 */
		private static final long serialVersionUID = -3059605352904619803L;

		public ConceptNeighbourhood() {
			super(new FlowLayout(FlowLayout.LEADING));
			this.add(new JLabel("Include neigbours at level:"));
			level.setPreferredSize(new Dimension(80, 20));
			this.add(level);
		}
	}

}
