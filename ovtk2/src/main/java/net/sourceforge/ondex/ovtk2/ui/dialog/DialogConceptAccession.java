package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Showing table for view/edit of concept accessions.
 * 
 * @author taubertj
 * 
 */
public class DialogConceptAccession extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	/**
	 * Compares two Pair containing String.
	 * 
	 * @author taubertj
	 * 
	 */
	private class PairComparator<T> implements Comparator<T> {

		public int compare(T arg0, T arg1) {
			if (arg0 instanceof Pair && arg1 instanceof Pair) {
				Pair<?> a = (Pair<?>) arg0;
				Pair<?> b = (Pair<?>) arg1;
				String a1 = (String) a.getFirst();
				String a2 = (String) a.getSecond();
				String b1 = (String) b.getFirst();
				String b2 = (String) b.getSecond();
				if (a1.compareTo(b1) == 0) {
					return a2.compareTo(b2);
				} else {
					return a1.compareTo(b1);
				}
			}
			return 0;
		}

	}

	/**
	 * Wraps the hashtable of concept accessions into a table model.
	 * 
	 * @author taubertj
	 * 
	 */
	private class ConceptAccessionTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -7469086391255891387L;

		// table header
		private String[] columnNames = new String[] { Config.language.getProperty("Dialog.ConceptAccession.TableAccession"), Config.language.getProperty("Dialog.ConceptAccession.TableDataSource"), Config.language.getProperty("Dialog.ConceptAccession.TableAmbiguous"), "" };

		// contains accession+data source to ambiguous mapping
		private Map<Pair<?>, Boolean> accessions = null;

		// show icon in last column
		private ImageIcon icon = null;

		// comparator for Arrays.sort
		private PairComparator<Pair<?>> comparator = new PairComparator<Pair<?>>();

		/**
		 * Constructor for a given mapping accession+data source to ambiguous.
		 * 
		 * @param conceptAccessions
		 *            Map<Pair,Boolean>
		 */
		public ConceptAccessionTableModel(Map<Pair<?>, Boolean> conceptAccessions) {
			this.accessions = conceptAccessions;
			File imgLocation = new File("config/toolbarButtonGraphics/general/delete16.gif");
			URL imageURL = null;

			try {
				imageURL = imgLocation.toURI().toURL();
			} catch (MalformedURLException mue) {
				System.err.println(mue.getMessage());
			}
			icon = new ImageIcon(imageURL);
		}

		public Map<Pair<?>, Boolean> getData() {
			return accessions;
		}

		public int getRowCount() {
			return accessions.size() + 1;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public Object getValueAt(int row, int col) {
			Pair<?>[] keys = accessions.keySet().toArray(new Pair[0]);
			Arrays.sort(keys, comparator);
			// return new row elements
			if (row == accessions.size()) {
				if (col == 0) {
					return "";
				} else if (col == 1) {
					return "";
				} else if (col == 2) {
					return Boolean.FALSE;
				} else if (col == 3) {
					return icon;
				}
			}
			// return existing data
			if (col == 0) {
				return keys[row].getFirst();
			} else if (col == 1) {
				return keys[row].getSecond();
			} else if (col == 2) {
				return accessions.get(keys[row]);
			} else if (col == 3) {
				return icon;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0 || col == 1) {
				return String.class;
			} else if (col == 2) {
				return Boolean.class;
			} else if (col == 3) {
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
			if (row < accessions.size()) {
				// change to accession itself
				if (col == 0) {
					Pair<?>[] keys = accessions.keySet().toArray(new Pair[0]);
					Arrays.sort(keys, comparator);
					Pair<?> key = keys[row];
					Boolean ambiguous = accessions.get(key);
					accessions.remove(key);
					// OVTK-349 remove trailing whitespace
					String accession = (String) value;
					accession = accession.trim();
					Pair<String> pair = new Pair<String>(accession, (String) key.getSecond());
					accessions.put(pair, ambiguous);
					this.fireTableDataChanged();
				}
				// change to data source
				else if (col == 1) {
					Pair<?>[] keys = accessions.keySet().toArray(new Pair[0]);
					Arrays.sort(keys, comparator);
					Pair<?> key = keys[row];
					Boolean ambiguous = accessions.get(key);
					accessions.remove(key);
					Pair<String> pair = new Pair<String>((String) key.getFirst(), (String) value);
					accessions.put(pair, ambiguous);
					this.fireTableDataChanged();
				}
				// change to ambiguous flag
				else if (col == 2) {
					Pair<?>[] keys = accessions.keySet().toArray(new Pair[0]);
					Arrays.sort(keys, comparator);
					Pair<String> pair = new Pair<String>((String) keys[row].getFirst(), (String) keys[row].getSecond());
					accessions.put(pair, (Boolean) value);
					this.fireTableDataChanged();
				}
			} else {
				// add new row
				if (col == 0) {
					Pair<String> pair = new Pair<String>((String) value, "");
					accessions.put(pair, Boolean.FALSE);
					this.fireTableRowsInserted(row, row);
					this.fireTableDataChanged();
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (row == accessions.size() && (col == 1 || col == 2 || col == 3)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Implements the table delete button.
	 * 
	 * @author taubertj
	 * 
	 */
	private class DeleteCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		// generated
		private static final long serialVersionUID = 2277895696342068127L;

		// contains accession+data source to ambiguous mapping
		private Map<Pair<?>, Boolean> accessions = null;

		// current table
		private JTable table;

		// button with icon on
		private JButton button;

		// comparator for Arrays.sort
		private PairComparator<Pair<?>> comparator = new PairComparator<Pair<?>>();

		/**
		 * Returns a delete button as editor.
		 * 
		 * @param conceptAccessions
		 *            Map<Pair, Boolean>
		 */
		public DeleteCellEditor(Map<Pair<?>, Boolean> conceptAccessions) {
			this.accessions = conceptAccessions;
			File imgLocation = new File("config/toolbarButtonGraphics/general/delete16.gif");
			URL imageURL = null;

			try {
				imageURL = imgLocation.toURI().toURL();
			} catch (MalformedURLException mue) {
				System.err.println(mue.getMessage());
			}

			button = new JButton(new ImageIcon(imageURL));
			button.addActionListener(this);
			button.setBorderPainted(false);
		}

		public Object getCellEditorValue() {
			return button.getIcon();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();

			// remove concept accession
			Pair<?>[] keys = accessions.keySet().toArray(new Pair[0]);
			Arrays.sort(keys, comparator);
			int row = Integer.parseInt(cmd);
			if (row < keys.length) {
				accessions.remove(keys[row]);
				((ConceptAccessionTableModel) table.getModel()).fireTableRowsDeleted(row, row);
				((ConceptAccessionTableModel) table.getModel()).fireTableDataChanged();
			}
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.table = table;
			button.setActionCommand("" + row);
			return button;
		}

	}

	// generated
	private static final long serialVersionUID = -4629075612818276763L;

	// calling ovtk2viewer
	private OVTK2Viewer viewer = null;

	// current concept
	private ONDEXConcept concept = null;

	// model containing mapping concept accession+data source to ambiguous
	private ConceptAccessionTableModel model = null;

	/**
	 * Constructs user input to view/edit a ConceptAccessions.
	 * 
	 * @param aog
	 *            ONDEXGraph to use for meta data
	 * @param concept
	 *            ONDEXConcept to add to
	 */
	public DialogConceptAccession(ONDEXConcept concept, OVTK2Viewer viewer) {
		super("Dialog.ConceptAccession.Title", "Properties16.gif");

		this.viewer = viewer;
		this.concept = concept;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.ConceptAccession.Apply", "Dialog.ConceptAccession.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for concept accessions.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		BoxLayout contentLayout = new BoxLayout(properties, BoxLayout.PAGE_AXIS);
		properties.setLayout(contentLayout);
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.ConceptAccession.ConceptAccessions"));
		properties.setBorder(propertiesBorder);

		// get concept accessions from concept
		Map<Pair<?>, Boolean> accessions = new HashMap<Pair<?>, Boolean>();
		for (ConceptAccession ca : concept.getConceptAccessions()) {
			accessions.put(new Pair<String>(ca.getAccession(), ca.getElementOf().getId()), ca.isAmbiguous());
		}

		// setup table
		model = new ConceptAccessionTableModel(accessions);
		JTable table = new JTable(model);

		TableColumn columnDataSource = table.getColumnModel().getColumn(1);
		DataSource[] sorted = viewer.getONDEXJUNGGraph().getMetaData().getDataSources().toArray(new DataSource[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		JComboBox cb = new JComboBox();
		for (DataSource dataSource : sorted) {
			cb.addItem(dataSource.getId());
		}
		columnDataSource.setCellEditor(new DefaultCellEditor(cb));

		TableColumn columnAmbiguous = table.getColumnModel().getColumn(2);
		int width = Config.language.getProperty("Dialog.ConceptAccession.TableAmbiguous").length() * 8;
		columnAmbiguous.setMaxWidth(width);
		columnAmbiguous.setMinWidth(width);

		TableColumn columnDelete = table.getColumnModel().getColumn(3);
		columnDelete.setMaxWidth(20);
		columnDelete.setMinWidth(20);
		columnDelete.setCellEditor(new DeleteCellEditor(accessions));

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

		// sync accessions
		if (cmd.equals(APPLY)) {
			// get existing concept accessions
			Map<Pair<?>, Boolean> existing = new HashMap<Pair<?>, Boolean>();
			for (ConceptAccession ca : concept.getConceptAccessions()) {
				existing.put(new Pair<String>(ca.getAccession(), ca.getElementOf().getId()), ca.isAmbiguous());
			}
			Map<Pair<?>, Boolean> accessions = model.getData();
			// check for positive changes
			for (Pair<?> pair : accessions.keySet()) {
				if (((String) pair.getFirst()).trim().length() > 0 && ((String) pair.getSecond()).trim().length() > 0) {
					// concept accessions not existing
					if (!existing.containsKey(pair)) {
						DataSource dataSource = viewer.getONDEXJUNGGraph().getMetaData().getDataSource((String) pair.getSecond());
						concept.createConceptAccession((String) pair.getFirst(), dataSource, accessions.get(pair));
						existing.put(pair, accessions.get(pair));
					}
					// ambiguous different
					else if (!existing.get(pair).equals(accessions.get(pair))) {
						DataSource dataSource = viewer.getONDEXJUNGGraph().getMetaData().getDataSource((String) pair.getSecond());
						concept.getConceptAccession((String) pair.getFirst(), dataSource).setAmbiguous(accessions.get(pair));
					}
				}
			}
			// check for deletions
			for (Pair<?> pair : existing.keySet()) {
				if (!accessions.containsKey(pair)) {
					DataSource dataSource = viewer.getONDEXJUNGGraph().getMetaData().getDataSource((String) pair.getSecond());
					concept.deleteConceptAccession((String) pair.getFirst(), dataSource);
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
