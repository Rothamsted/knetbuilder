package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DeleteCellEditor;

/**
 * Showing table for view/edit of concept names.
 * 
 * @author taubertj
 * 
 */
public class DialogConceptName extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	/**
	 * Wraps the hashtable of concept names into a table model.
	 * 
	 * @author taubertj
	 * 
	 */
	private class ConceptNameTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -7275161445290443535L;

		// table header
		private String[] columnNames = new String[] { Config.language.getProperty("Dialog.ConceptName.TableName"), Config.language.getProperty("Dialog.ConceptName.TablePreferred"), "" };

		// contains name to preferred mapping
		private Map<String, Boolean> names = null;

		// show icon in last column
		private ImageIcon icon = null;

		/**
		 * Constructor for a given mapping name to preferred.
		 * 
		 * @param conceptNames
		 *            Map<String,Boolean>
		 */
		public ConceptNameTableModel(Map<String, Boolean> conceptNames) {
			this.names = conceptNames;
			File imgLocation = new File("config/toolbarButtonGraphics/general/delete16.gif");
			URL imageURL = null;

			try {
				imageURL = imgLocation.toURI().toURL();
			} catch (MalformedURLException mue) {
				System.err.println(mue.getMessage());
			}
			icon = new ImageIcon(imageURL);
		}

		public Map<String, Boolean> getData() {
			return names;
		}

		public int getRowCount() {
			return names.size() + 1;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			String[] keys = names.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			// return new row elements
			if (row == names.size()) {
				if (col == 0) {
					return "";
				} else if (col == 1) {
					return Boolean.FALSE;
				} else if (col == 2) {
					return icon;
				}
			}
			// return existing data
			if (col == 0) {
				return keys[row];
			} else if (col == 1) {
				return names.get(keys[row]);
			} else if (col == 2) {
				return icon;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			} else if (col == 1) {
				return Boolean.class;
			} else if (col == 2) {
				return ImageIcon.class;
			}
			return null;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (row < names.size()) {
				// change to name itself
				if (col == 0) {
					String[] keys = names.keySet().toArray(new String[0]);
					Arrays.sort(keys);
					String key = keys[row];
					Boolean preferred = names.get(key);
					names.remove(key);
					names.put((String) value, preferred);
					this.fireTableDataChanged();
				}
				// change to preferred flag
				else if (col == 1) {
					String[] keys = names.keySet().toArray(new String[0]);
					Arrays.sort(keys);
					String key = keys[row];
					names.put(key, (Boolean) value);
					this.fireTableDataChanged();
				}
			} else {
				// add new row
				if (col == 0) {
					names.put((String) value, Boolean.FALSE);
					this.fireTableRowsInserted(row, row);
					this.fireTableDataChanged();
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}
	}

	// generated
	private static final long serialVersionUID = 7118122535811680819L;

	// current concept
	private ONDEXConcept concept = null;

	// calling ovtk2viewer
	private OVTK2Viewer viewer = null;

	// model containing mapping concept name to preferred
	private ConceptNameTableModel model = null;

	/**
	 * Constructs user input to view/edit a ConceptNames.
	 * 
	 * @param concept
	 *            ONDEXConcept to add to
	 */
	public DialogConceptName(ONDEXConcept concept, OVTK2Viewer viewer) {
		super("Dialog.ConceptName.Title", "Properties16.gif");

		this.concept = concept;
		this.viewer = viewer;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.ConceptName.Apply", "Dialog.ConceptName.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for concept names.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		BoxLayout contentLayout = new BoxLayout(properties, BoxLayout.PAGE_AXIS);
		properties.setLayout(contentLayout);
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.ConceptName.ConceptNames"));
		properties.setBorder(propertiesBorder);

		// get concept names from concept
		Map<String, Boolean> names = new HashMap<String, Boolean>();
		for (ConceptName cn : concept.getConceptNames()) {
			names.put(cn.getName(), cn.isPreferred());
		}

		// setup table
		model = new ConceptNameTableModel(names);
		JTable table = new JTable(model);

		TableColumn columnPreferred = table.getColumnModel().getColumn(1);
		int width = Config.language.getProperty("Dialog.ConceptName.TablePreferred").length() * 7;
		columnPreferred.setMaxWidth(width);
		columnPreferred.setMinWidth(width);

		TableColumn columnDelete = table.getColumnModel().getColumn(2);
		columnDelete.setMaxWidth(20);
		columnDelete.setMinWidth(20);
		columnDelete.setCellEditor(new DeleteCellEditor<String, Boolean>(names));

		// add table to properties
		JScrollPane scrollPane = new JScrollPane(table);
		JPanel tablePanel = new JPanel(new GridLayout(1, 1));
		tablePanel.add(scrollPane);
		properties.add(tablePanel);

		return properties;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// sync concept names
		if (cmd.equals(APPLY)) {
			// get existing concept names
			Map<String, Boolean> existing = new HashMap<String, Boolean>();
			for (ConceptName cn : concept.getConceptNames()) {
				existing.put(cn.getName(), cn.isPreferred());
			}
			Map<String, Boolean> names = model.getData();
			// check for positive changes
			for (String name : names.keySet()) {
				if (name.trim().length() > 0) {
					// concept name not exisiting
					if (!existing.containsKey(name)) {
						concept.createConceptName(name, names.get(name));
						existing.put(name, names.get(name));
					}
					// preferred different
					else if (!existing.get(name).equals(names.get(name))) {
						concept.getConceptName(name).setPreferred(names.get(name));
					}
				}
			}
			// check for deletions
			for (String name : existing.keySet()) {
				if (!names.containsKey(name)) {
					concept.deleteConceptName(name);
				}
			}
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}

			// update label of node
			viewer.getNodeLabels().updateLabel(concept);
			viewer.getVisualizationViewer().getModel().fireStateChanged();
		}

		// cancel dialog
		else if (cmd.equals(CANCEL)) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}

	}

}
