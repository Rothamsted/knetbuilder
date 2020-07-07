package net.sourceforge.ondex.ovtk2.io;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.IntegerEditor;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;


/**
 * Provides a GUI to export data from the graph as tab separated files.
 * 
 * @author Jan
 * 
 */
public class TabularExporter extends JInternalFrame implements ActionListener,
		OVTK2IO, TableModelListener {

	/**
	 * Extension class to return the right column class.
	 * 
	 * @author Jan
	 * 
	 */
	private class MyDefaultTableModel extends DefaultTableModel {

		/**
		 * generated
		 */
		private static final long serialVersionUID = -8255899540278254151L;

		/*
		 * Contains editable switches
		 */
		private Map<Integer, Map<Integer, Boolean>> editable = new HashMap<Integer, Map<Integer, Boolean>>();

		/**
		 * Only constructor from super class used
		 * 
		 * @param columnNames
		 * @param rowCount
		 */
		public MyDefaultTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {

			// decide which data type to return
			switch (columnIndex) {
			case 0:
				return Integer.class;
			case 4:
				return Boolean.class;
			default:
				return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return editable.get(row) != null
					&& editable.get(row).get(column) != null
					&& editable.get(row).get(column).booleanValue();
		}

		/**
		 * Sets editable flag for each cell.
		 * 
		 * @param row
		 * @param column
		 * @param isEditable
		 */
		public void setCellEditable(int row, int column, boolean isEditable) {
			if (!editable.containsKey(row))
				editable.put(row, new HashMap<Integer, Boolean>());
			editable.get(row).put(column, isEditable);
		}
	}

	/*
	 * generated
	 */
	private static final long serialVersionUID = 8816112859346084493L;

	/*
	 * Action command for adding another row
	 */
	private static final String ADD = "ADD";

	/*
	 * Action command for removing last row
	 */
	private static final String REMOVE = "REMOVE";

	/*
	 * Action command for export button
	 */
	private static final String GO = "GO";

	/*
	 * Names for field selection choices for concepts
	 */
	private static final String PID = "PID";

	private static final String DESC = "Description";

	private static final String ANNO = "Annotation";

	private static final String NAMES = "Names";

	private static final String ACCS = "Accessions";

	/*
	 * Header for configuration table to allow user to define path through graph
	 */
	private static String[] columnNames = new String[] { "Column",
			"Entity type", "Value selection", "RegEx (optional)",
			"Use in output?" };

	/*
	 * Choices for fields to include into output for a concept
	 */
	private static JComboBox conceptChoices = new JComboBox(new String[] { PID,
			DESC, ANNO, NAMES, ACCS });

	/*
	 * ONDEXGraph to export data from, set by caller
	 */
	private ONDEXGraph graph;

	/*
	 * File to write to, set by caller
	 */
	private File file;

	/*
	 * here comes the GUI part, table to contain all configuration settings
	 */
	private JTable configurationTable = null;

	/*
	 * the default table model can be modified, which is required for a dynamic
	 * configuration table, e.g. adding new rows
	 */
	private MyDefaultTableModel configurationTableModel = null;

	/*
	 * processing of lines is cancelled
	 */
	private boolean cancelled = false;

	/**
	 * Make sure the GUI is flexible etc.
	 */
	public TabularExporter() {
		super("Tabular Exporter", true, true, true, true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ADD)) {
			addNewTableRow();
		}

		else if (e.getActionCommand().equals(REMOVE)) {
			if (configurationTable.getRowCount() > 1) {
				configurationTableModel.removeRow(configurationTable
						.getRowCount() - 1);
			}
		}

		else if (e.getActionCommand().equals(GO)) {

			if (file != null) {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							file));

					// defines how to traverse the graph in terms of MetaData
					List<MetaData> lineMetaData = new ArrayList<MetaData>();

					// which attributes to write to the output file
					List<String> lineAttributes = new ArrayList<String>();

					// which line to write out to file
					List<Boolean> lineInclude = new ArrayList<Boolean>();

					// regular expression for post processing
					List<String> lineRegEx = new ArrayList<String>();

					// iterate over all rows
					for (int row = 0; row < configurationTableModel
							.getRowCount(); row++) {

						MetaData metaData = (MetaData) configurationTableModel
								.getValueAt(row, 1);

						// only consider lines which have a actual meta data
						// selection
						if (metaData != null) {
							lineMetaData.add(metaData);
							lineAttributes.add((String) configurationTableModel
									.getValueAt(row, 2));
							String regex = (String) configurationTableModel
									.getValueAt(row, 3);
							if (regex != null)
								lineRegEx.add(regex);
							else
								lineRegEx.add("");
							lineInclude.add((Boolean) configurationTableModel
									.getValueAt(row, 4));
						}
					}

					// extract neighborhoods as per meta data definitions
					List<Map<Integer, Set<ONDEXConcept>>> neighbourHoods = processGraph(lineMetaData);

					// for each neighborhood write output
					cancelled = false;
					for (Map<Integer, Set<ONDEXConcept>> nh : neighbourHoods) {
						if (!cancelled)
							processNeighbourHood(writer, nh, lineAttributes,
									lineInclude, lineRegEx);
					}

					writer.close();
				} catch (IOException e1) {
					ErrorDialog.show(e1);
				}
			}
		}
	}

	/**
	 * Writes lines for each neighborhood previously found.
	 * 
	 * @param writer
	 * @param neighbourHoodAtDepth
	 * @param lineAttributes
	 * @param lineInclude
	 * @param lineRegEx
	 * @throws IOException
	 */
	private void processNeighbourHood(BufferedWriter writer,
			Map<Integer, Set<ONDEXConcept>> neighbourHoodAtDepth,
			List<String> lineAttributes, List<Boolean> lineInclude,
			List<String> lineRegEx) throws IOException {

		// flatten hierarchical data structure by extracting all possible paths
		List<List<ONDEXConcept>> flatPaths = recurseFlatten(0,
				neighbourHoodAtDepth, new ArrayList<List<ONDEXConcept>>());

		// output each path
		for (List<ONDEXConcept> path : flatPaths) {

			// fill each column with one concept
			int index = 0;
			for (ONDEXConcept c : path) {

				// check for current column description should be included
				if (lineInclude.get(index)) {

					// output concept PID
					String output = "";
					if (lineAttributes.get(index) == PID) {
						if (c.getPID() != null)
							output = c.getPID();
					}

					// output concept description
					else if (lineAttributes.get(index) == DESC) {
						if (c.getDescription() != null)
							output = c.getDescription();
					}

					// output concept annotation
					else if (lineAttributes.get(index) == ANNO) {
						if (c.getAnnotation() != null)
							output = c.getAnnotation();
					}

					// output names as comma separated
					else if (lineAttributes.get(index) == NAMES) {
						StringBuffer names = new StringBuffer();
						for (ConceptName name : c.getConceptNames()) {
							names.append(name.getName());
							names.append(", ");
						}
						// remove last comma
						output = names.toString();
						if (output.length() > 2)
							output = output.substring(0, output.length() - 2);
					}

					// output accessions as comma separated
					else if (lineAttributes.get(index) == ACCS) {
						StringBuffer accs = new StringBuffer();
						for (ConceptAccession acc : c.getConceptAccessions()) {
							accs.append(acc.getAccession());
							accs.append(" [");
							accs.append(acc.getElementOf().getId());
							accs.append("], ");
						}
						// remove last comma
						output = accs.toString();
						if (output.length() > 2)
							output = output.substring(0, output.length() - 2);
					}

					// post processing regular expression
					if (lineRegEx.get(index).trim().length() > 0) {
						Pattern p = Pattern
								.compile(lineRegEx.get(index).trim());
						Matcher m = p.matcher(output);
						if (m.matches()) {
							if (m.groupCount() != 1) {
								JOptionPane
										.showMessageDialog(this,
												"Exactly one capturing group has to be defined in your regular expression.");
								cancelled = true;
								return;
							} else
								output = m.group(1);

						} else {
							output = "no match";
						}
					}

					writer.write(output);
					writer.write("\t");
				}
				index++;
			}

			writer.write("\n");
		}
	}

	/**
	 * Flattens the hierarchical data structure to return each path as a
	 * separate list.
	 * 
	 * @param depth
	 * @param neighbourHoodAtDepth
	 * @param paths
	 * @return
	 */
	private List<List<ONDEXConcept>> recurseFlatten(int depth,
			Map<Integer, Set<ONDEXConcept>> neighbourHoodAtDepth,
			List<List<ONDEXConcept>> paths) {

		// base case for initialization
		if (depth == 0) {
			paths.add(new ArrayList<ONDEXConcept>());
			paths.get(0).addAll(neighbourHoodAtDepth.get(0));
			return recurseFlatten(1, neighbourHoodAtDepth, paths);
		}

		// recursion end condition
		else if (depth == neighbourHoodAtDepth.keySet().size()) {
			return paths;
		}

		// recursion case
		else {

			// construct a new list of more complete paths
			List<List<ONDEXConcept>> newpaths = new ArrayList<List<ONDEXConcept>>();
			for (List<ONDEXConcept> unfinished : paths) {
				for (ONDEXConcept c : neighbourHoodAtDepth.get(depth)) {
					// preserve old path information
					List<ONDEXConcept> extended = new ArrayList<ONDEXConcept>(
							unfinished);
					// add additional concept
					extended.add(c);
					// add to list of new paths
					newpaths.add(extended);
				}
			}

			// do recursion
			return recurseFlatten(++depth, neighbourHoodAtDepth, newpaths);
		}
	}

	/**
	 * Traverses the graph in the given way the meta data is defined.
	 * 
	 * @param lineMetaData
	 */
	private List<Map<Integer, Set<ONDEXConcept>>> processGraph(
			List<MetaData> lineMetaData) {

		MetaData firstMetaData = lineMetaData.get(0);
		List<Map<Integer, Set<ONDEXConcept>>> neighbourHoods = new ArrayList<Map<Integer, Set<ONDEXConcept>>>();
		if (firstMetaData instanceof ConceptClass) {
			ConceptClass cc = (ConceptClass) firstMetaData;

			// start at concepts of root concept class
			Set<ONDEXConcept> rootConcepts = graph
					.getConceptsOfConceptClass(cc);

			// each root concept forms a line
			for (ONDEXConcept c : rootConcepts) {

				// collect further neighborhood from graph
				Map<Integer, Set<ONDEXConcept>> neighbourHoodAtDepth = new HashMap<Integer, Set<ONDEXConcept>>();
				neighbourHoodAtDepth.put(0, new HashSet<ONDEXConcept>());
				neighbourHoodAtDepth.get(0).add(c);

				// perform a breadth first search for other concepts
				for (int i = 1; i < lineMetaData.size(); i++) {
					neighbourHoodAtDepth.put(i, new HashSet<ONDEXConcept>());

					/*
					 * in this case the relation types between concept classes
					 * doesn't matter
					 */
					if (lineMetaData.get(i) instanceof ConceptClass) {
						ConceptClass neighborConceptClass = (ConceptClass) lineMetaData
								.get(i);
						for (ONDEXConcept previous : neighbourHoodAtDepth
								.get(i - 1)) {
							for (ONDEXRelation r : graph
									.getRelationsOfConcept(previous)) {
								ONDEXConcept neighbor = null;

								// a forward relation
								if (previous.equals(r.getFromConcept()))
									neighbor = r.getToConcept();

								// a backward relation
								else if (previous.equals(r.getToConcept()))
									neighbor = r.getFromConcept();

								// Note: ternary relations are ignored here

								// check specified concept class condition
								if (neighbor != null
										&& neighbor.getOfType().equals(
												neighborConceptClass))
									neighbourHoodAtDepth.get(i).add(neighbor);
							}

						}
					}

					/*
					 * pay attention to the specified relation type between
					 * concepts, the target concept class doesn't matter
					 */
					else if (lineMetaData.get(i) instanceof RelationType) {
						RelationType connectingRelationType = (RelationType) lineMetaData
								.get(i);
						for (ONDEXConcept previous : neighbourHoodAtDepth
								.get(i - 1)) {
							for (ONDEXRelation r : graph
									.getRelationsOfConcept(previous)) {

								// check specified relation type condition
								if (r.getOfType()
										.equals(connectingRelationType)) {

									// a forward relation
									if (previous.equals(r.getFromConcept()))
										neighbourHoodAtDepth.get(i).add(
												r.getToConcept());

									// a backward relation
									if (previous.equals(r.getToConcept()))
										neighbourHoodAtDepth.get(i).add(
												r.getFromConcept());

									// Note: ternary relations are ignored here
								}
							}
						}
					}

					// add to complete list
					neighbourHoods.add(neighbourHoodAtDepth);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this,
					"You need to select a ConceptClass as the first element.");
		}

		return neighbourHoods;
	}

	/**
	 * Adds a new empty row to the table model.
	 * 
	 */
	private void addNewTableRow() {

		// one data row
		Object[] row = new Object[5];
		row[0] = Integer.valueOf(configurationTableModel.getRowCount() + 1);
		row[1] = null;
		row[2] = null;
		row[3] = null;
		row[4] = Boolean.TRUE;

		// add new default row to table model
		configurationTableModel.addRow(row);

		int lastRow = configurationTableModel.getRowCount() - 1;
		configurationTableModel.setCellEditable(lastRow, 1, true);
		configurationTableModel.setCellEditable(lastRow, 4, true);
	}

	public String assembleLine(ONDEXConcept c1, Set<ONDEXConcept> additional,
			ONDEXConcept c2, RelationType rt, Set<ONDEXConcept> other) {
		StringBuffer buf = new StringBuffer();
		buf.append(c1.getPID());
		buf.append("\t");
		buf.append(additional);
		buf.append("\t");

		if (c1.getAnnotation().contains("irreversible")) {
			buf.append("irreversible");
		} else if (c1.getAnnotation().contains("reversible")) {
			buf.append("reversible");
		} else {
			buf.append("unknown");
		}

		buf.append("\t");
		buf.append(c2.getPID());
		buf.append("\t");
		buf.append(rt.getId());
		buf.append("\t");
		buf.append(other);

		buf.append("\n");
		return buf.toString();
	}

	/**
	 * Creates a ComboBox which contains both concept classes and relation
	 * types.
	 * 
	 * @return ONDEX entity types ComboBox
	 */
	private JComboBox getEntityTypeComboBox() {
		JComboBox cb = new JComboBox();

		// first add concept classes, sorted by ID
		Set<ConceptClass> ccs = graph.getMetaData().getConceptClasses();
		ConceptClass[] arrayCC = ccs.toArray(new ConceptClass[0]);
		Arrays.sort(arrayCC);
		for (ConceptClass cc : arrayCC) {
			// only add meta data if used
			if (graph.getConceptsOfConceptClass(cc).size() > 0)
				cb.addItem(cc);
		}

		// second add relation types, sorted by ID
		Set<RelationType> rts = graph.getMetaData().getRelationTypes();
		RelationType[] arrayRT = rts.toArray(new RelationType[0]);
		Arrays.sort(arrayRT);
		for (RelationType rt : arrayRT) {
			// only add meta data if used
			if (graph.getRelationsOfRelationType(rt).size() > 0)
				cb.addItem(rt);
		}

		return cb;
	}

	@Override
	public String getExt() {
		return "tab";
	}

	/**
	 * Puts mainly the configuration table together
	 */
	private void initGUI() {

		// dispose viewer on close
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel(new SpringLayout());

		// empty table to start with (one empty row)
		configurationTableModel = new MyDefaultTableModel(columnNames, 0);

		// table to define export configuration
		configurationTable = new JTable(configurationTableModel);

		// add input checking to Integer values
		configurationTable.getColumnModel().getColumn(0)
				.setCellEditor(new IntegerEditor(1, 100));

		// add ComboBox for ONDEX entity types
		TableColumn entityColumn = configurationTable.getColumnModel()
				.getColumn(1);
		JComboBox entityTypes = getEntityTypeComboBox();
		entityColumn.setCellEditor(new DefaultCellEditor(entityTypes));

		// set concept choices ComboBox as editor for value selection
		TableColumn valueColumn = configurationTable.getColumnModel()
				.getColumn(2);
		valueColumn.setCellEditor(new DefaultCellEditor(conceptChoices));

		// add first line to table
		addNewTableRow();

		// add listener for user selections
		configurationTableModel.addTableModelListener(this);

		// display configuration table in a scroll pane
		mainPanel.add(new JScrollPane(configurationTable));

		// button to add new row to table
		JButton addRow = new JButton("Add new row");
		addRow.addActionListener(this);
		addRow.setActionCommand(ADD);
		mainPanel.add(addRow);

		// button to add remove last row
		JButton removeRow = new JButton("Remove last row");
		removeRow.addActionListener(this);
		removeRow.setActionCommand(REMOVE);
		mainPanel.add(removeRow);

		// GO button to start processing
		JButton exportGo = new JButton("Export");
		exportGo.addActionListener(this);
		exportGo.setActionCommand(GO);
		mainPanel.add(exportGo);

		SpringUtilities.makeCompactGrid(mainPanel,
				mainPanel.getComponentCount(), 1, 5, 5, 5, 5);

		// set layout
		this.getContentPane().setLayout(new GridLayout(1, 1));
		this.getContentPane().add(mainPanel);
		this.pack();

		// add to desktop
		OVTK2Desktop.getInstance().getDesktopPane().add(this);
		this.setVisible(true);
	}

	@Override
	public boolean isImport() {
		return false;
	}

	@Override
	public void setGraph(ONDEXGraph graph) {
		this.graph = graph;
	}

	@Override
	public void start(File file) {
		this.file = file;
		initGUI();
	}

	@Override
	public void tableChanged(TableModelEvent e) {

		if (e.getFirstRow() > -1 && e.getColumn() > -1) {

			Object value = configurationTableModel.getValueAt(e.getFirstRow(),
					e.getColumn());

			/*
			 * For concept classes enable value selection field and RegEx field
			 */
			if (value instanceof ConceptClass) {
				configurationTableModel.setCellEditable(e.getFirstRow(), 2,
						true);
				configurationTableModel.setCellEditable(e.getFirstRow(), 3,
						true);
				configurationTableModel.setValueAt(conceptChoices.getItemAt(0),
						e.getFirstRow(), 2);
			}

			/*
			 * For relation types the rest of the fields stay disabled as per
			 * default
			 */
			else if (value instanceof RelationType) {
				configurationTableModel.setValueAt("n/a", e.getFirstRow(), 2);
			}
		}

	}

}
