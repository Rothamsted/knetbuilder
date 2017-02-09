package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Showing table for change of visibility of edges.
 * 
 * @author taubertj
 */
public class DialogEdges extends OVTK2Dialog {

	/**
	 * Wraps the list of edges into a table model.
	 * 
	 * @author taubertj
	 */
	private class EdgesTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -5332617581771162849L;

		// table header
		private String[] columnNames = new String[] { "", Config.language.getProperty("Dialog.Edges.TableLabel"), Config.language.getProperty("Dialog.Edges.RelationTypeLabel"), Config.language.getProperty("Dialog.Edges.TableVisible"), Config.language.getProperty("Dialog.Edges.TableLabelVisible") };

		// contains edge to visible mapping
		private Map<ONDEXRelation, Boolean> edges = null;

		/**
		 * Constructor for a given mapping edge to visible.
		 * 
		 * @param edges
		 *            Map<ONDEXRelation, Boolean>
		 */
		public EdgesTableModel(Map<ONDEXRelation, Boolean> edges) {
			this.edges = edges;
		}

		public Map<ONDEXRelation, Boolean> getData() {
			return edges;
		}

		public int getRowCount() {
			return edges.size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			ONDEXRelation[] keys = edges.keySet().toArray(new ONDEXRelation[0]);
			// return existing data
			if (col == 0) {
				return keys[row].getId();
			} else if (col == 1) {
				return labels.get(keys[row]);
			} else if (col == 2) {
				return rt_labels.get(keys[row]);
			} else if (col == 3) {
				return edges.get(keys[row]);
			} else if (col == 4) {
				return elv.get(keys[row]);
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
				ONDEXRelation[] keys = edges.keySet().toArray(new ONDEXRelation[0]);
				ONDEXRelation key = keys[row];
				edges.put(key, (Boolean) value);
				this.fireTableDataChanged();
			}
			if (col == 4) {
				ONDEXRelation[] keys = edges.keySet().toArray(new ONDEXRelation[0]);
				ONDEXRelation key = keys[row];
				elv.put(key, (Boolean) value);
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
	private static final long serialVersionUID = -4623475722290601497L;

	// calling ovtk2viewer
	private OVTK2Viewer viewer = null;

	// wrapped ONDEXJUNGGraph
	private ONDEXJUNGGraph graph = null;

	// model containing mapping edge to visible
	private EdgesTableModel model = null;

	// table for displaying model
	private JTable table = null;

	// panel for scroll pane
	private JPanel tablePanel = null;

	// buttons to toggle table selection
	private JButton visibleAll, labelsAll;

	// mapping edge id to edge label
	private Map<ONDEXRelation, String> labels = null;
	private Map<ONDEXRelation, String> rt_labels = null;
	private Map<ONDEXRelation, Boolean> elv = null;

	// visible attribute
	private AttributeName an = null;
	private AttributeName anELV = null;

	/**
	 * Constructs user input to edit edges visibility.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public DialogEdges(OVTK2Viewer viewer) {
		super("Dialog.Edges.Title", "Properties16.gif");

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setViewer(viewer);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Edges.Apply", "Dialog.Edges.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Sets viewer to be used in edges view.
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

		// make sure visibleELV attribute name is there
		if (!graph.getMetaData().checkAttributeName("visibleELV")) {
			this.anELV = graph.getMetaData().getFactory().createAttributeName("visibleELV", Boolean.class);
		} else {
			this.anELV = graph.getMetaData().getAttributeName("visibleELV");
		}

		Set<ONDEXRelation> pickedEdges = viewer.getPickedEdges();

		// get edge visibility
		labels = new HashMap<ONDEXRelation, String>();
		rt_labels = new HashMap<ONDEXRelation, String>();
		elv = viewer.getEdgeLabels().getMask();

		// create visibility pattern
		Map<ONDEXRelation, Boolean> visible = new HashMap<ONDEXRelation, Boolean>();
		for (ONDEXRelation edge : graph.getRelations()) {
			ONDEXConcept from = edge.getFromConcept();
			ONDEXConcept to = edge.getToConcept();

			// construct display labels
			String label = viewer.getNodeLabels().getLabel(from);
			label = label + " -> " + viewer.getNodeLabels().getLabel(to);
			labels.put(edge, label);
			rt_labels.put(edge, edge.getOfType().toString());
			visible.put(edge, graph.isVisible(edge));
		}

		// selection according to pick state
		if (pickedEdges.size() > 0) {
			for (ONDEXRelation e : visible.keySet()) {
				visible.put(e, false);
			}
			for (ONDEXRelation e : pickedEdges) {
				visible.put(e, true);
			}
		}

		// setup table
		model = new EdgesTableModel(visible);
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		if (viewer.getONDEXJUNGGraph().getRelations().size() < 2000)
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
	 * Creates the properties panel for edges.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel(new BorderLayout());

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Edges.Edges"));
		properties.setBorder(propertiesBorder);

		TableColumn preferred = table.getColumnModel().getColumn(3);
		int width = Config.language.getProperty("Dialog.Edges.TableVisible").length() * 7;
		preferred.setMaxWidth(width);
		preferred.setMinWidth(width);

		TableColumn preferredELV = table.getColumnModel().getColumn(4);
		int widthELV = Config.language.getProperty("Dialog.Edges.TableLabelVisible").length() * 7;
		preferredELV.setMaxWidth(widthELV);
		preferredELV.setMinWidth(widthELV);

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

		JButton load = new JButton(Config.language.getProperty("Dialog.Edges.Load"));
		load.setActionCommand("load");
		load.addActionListener(this);
		gdsPane.add(load);

		JButton store = new JButton(Config.language.getProperty("Dialog.Edges.Store"));
		store.setActionCommand("store");
		store.addActionListener(this);
		gdsPane.add(store);

		visibleAll = new JButton(Config.language.getProperty("Dialog.Edges.DeSelectAllVisible"));
		visibleAll.setActionCommand("visible");
		visibleAll.addActionListener(this);
		gdsPane.add(visibleAll);

		labelsAll = new JButton(Config.language.getProperty("Dialog.Edges.SelectAllLabels"));
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

			StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());

			// this is for edge visibility
			Map<ONDEXRelation, Boolean> visible = model.getData();
			for (ONDEXRelation relation : visible.keySet()) {
				graph.setVisibility(relation, visible.get(relation));
			}

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
			Map<ONDEXRelation, Boolean> visible = model.getData();
			for (ONDEXRelation key : visible.keySet()) {
				visible.put(key, false);
			}

			// fill from Attribute
			for (ONDEXRelation relation : graph.getRelationsOfAttributeName(an)) {
				Attribute attribute = relation.getAttribute(an);
				visible.put(relation, (Boolean) attribute.getValue());
			}

			// clear existing visibility list
			elv.clear();

			// fill from Attribute
			for (ONDEXRelation relation : graph.getRelationsOfAttributeName(anELV)) {
				Attribute attribute = relation.getAttribute(anELV);
				elv.put(relation, ((Boolean) attribute.getValue()).booleanValue());
			}

			model.fireTableDataChanged();
		}

		// store visible flags to Attribute
		else if (cmd.equals("store")) {
			Map<ONDEXRelation, Boolean> visible = model.getData();
			for (ONDEXRelation relation : visible.keySet()) {
				Attribute attribute = relation.getAttribute(an);
				// set edge visibility
				if (attribute == null) {
					relation.createAttribute(an, visible.get(relation), false);
				} else {
					attribute.setValue(visible.get(relation));
				}
				// set label visibility
				attribute = relation.getAttribute(anELV);
				if (attribute == null) {
					relation.createAttribute(anELV, elv.get(relation), false);
				} else {
					attribute.setValue(elv.get(relation));
				}
			}
		}

		// toggle all visible
		else if (cmd.equals("visible")) {
			if (visibleAll.getText().equals(Config.language.getProperty("Dialog.Edges.SelectAllVisible"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(true, x, table.convertColumnIndexToView(3));
				}
				visibleAll.setText(Config.language.getProperty("Dialog.Edges.DeSelectAllVisible"));
			} else if (visibleAll.getText().equals(Config.language.getProperty("Dialog.Edges.DeSelectAllVisible"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(false, x, table.convertColumnIndexToView(3));
				}
				visibleAll.setText(Config.language.getProperty("Dialog.Edges.SelectAllVisible"));
			}
			visibleAll.repaint();
		}

		// toggle all labels
		else if (cmd.equals("labels")) {
			if (labelsAll.getText().equals(Config.language.getProperty("Dialog.Edges.SelectAllLabels"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(true, x, table.convertColumnIndexToView(4));
				}
				labelsAll.setText(Config.language.getProperty("Dialog.Edges.DeSelectAllLabels"));
			} else if (labelsAll.getText().equals(Config.language.getProperty("Dialog.Edges.DeSelectAllLabels"))) {
				for (int x = 0, y = table.getRowCount(); x < y; x++) {
					table.setValueAt(false, x, table.convertColumnIndexToView(4));
				}
				labelsAll.setText(Config.language.getProperty("Dialog.Edges.SelectAllLabels"));
			}
			labelsAll.repaint();
		}
	}

}
