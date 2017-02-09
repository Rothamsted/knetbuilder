package net.sourceforge.ondex.ovtk2.filter.tag;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.tag.ArgumentNames;
import net.sourceforge.ondex.filter.tag.Filter;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.ConceptListUtils;
import net.sourceforge.ondex.ovtk2.util.IntegerStringWrapper;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.renderer.HtmlComboBoxRenderer;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

/**
 * Filter on tag
 * 
 * @author keywan, lysenkoa
 */
public class TagFilter extends OVTK2Filter implements ActionListener {

	private static final String NOT = "NOT";

	private static final String OR = "OR";

	private static final String AND = "AND";

	private static final String APPLY = "apply";

	private static final String ADD = "add";

	private static final String TAG_CC_TYPE = "tagCCType";

	private static final String TAG_FIRST_LINE = "Viewer.ToolBar.None";

	private static final String TAG_TYPE_FIRST_LINE = "Viewer.ToolBar.Ignore";

	private static final String TAG_NAME = "Viewer.ToolBar.Context";

	private static final String TAG_TYPE = "Viewer.ToolBar.Context.Type";

	private static final long serialVersionUID = 1375625527203849092L;

	private JTable table;

	private JComboBox typeBox = new JComboBox();

	private JComboBox tagBox = new JComboBox();

	private AutoCompleteSupport<?> support;

	private JComboBox operationBox = new JComboBox();

	private JCheckBox showRelations = new JCheckBox("Show relations between concepts of selected tags.");

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Constructor
	 * 
	 * @param viewer
	 */
	public TagFilter(OVTK2Viewer viewer) {
		super(viewer);
		showRelations.setSelected(true);
		setLayout(new SpringLayout());

		// table header
		String[] columnNames = { Config.language.getProperty(TAG_NAME), "Operation:" };

		// new set of combo boxes
		operationBox.addItem(Config.language.getProperty(TAG_FIRST_LINE));
		operationBox.addItem(AND);
		operationBox.addItem(OR);
		operationBox.addItem(NOT);
		tagBox.setRenderer(new HtmlComboBoxRenderer());
		addContextToComboBox(typeBox, tagBox);

		// set selected index default
		typeBox.setSelectedIndex(0);
		tagBox.setSelectedIndex(0);
		operationBox.setSelectedIndex(0);

		// update tag drop down box
		typeBox.setActionCommand(TAG_CC_TYPE);
		typeBox.addActionListener(this);

		// table data
		Object[][] data = { { tagBox.getSelectedItem(), operationBox.getSelectedItem() } };

		// create table and set editors
		table = new JTable(new DefaultTableModel(data, columnNames));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");

		// tool tip enabled renderer
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		// combo box cell editor
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(tagBox));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(operationBox));

		table.getModel().addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent arg0) {
				if (table.getModel().getValueAt(table.getModel().getRowCount() - 1, 0) != Config.language.getProperty(TAG_FIRST_LINE)) {
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.addRow(new Object[] { Config.language.getProperty(TAG_FIRST_LINE), operationBox.getItemAt(0) });
				}
			}
		});

		// class selection to limit drop down box of tag
		JPanel typePanel = new JPanel(new FlowLayout());
		typePanel.add(new JLabel(Config.language.getProperty(TAG_TYPE)));
		typePanel.add(typeBox);
		this.add(typePanel);

		// single selection only
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.add(new JScrollPane(table));
		this.add(showRelations, SpringLayout.EAST);
		JPanel sub = new JPanel(new BorderLayout());
		// add a new row
		JButton add = new JButton("Add");
		forceSize(add);
		add.setActionCommand(ADD);
		add.addActionListener(this);
		sub.add(add, BorderLayout.WEST);

		// apply filter
		JButton combine = new JButton("Apply");
		forceSize(combine);
		combine.setActionCommand(APPLY);
		combine.addActionListener(this);
		sub.add(combine, BorderLayout.EAST);
		this.add(sub);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}

	private static void forceSize(JButton button) {
		button.setMaximumSize(new Dimension(70, 24));
		button.setMinimumSize(new Dimension(70, 24));
		button.setSize(new Dimension(70, 24));
		button.setPreferredSize(new Dimension(70, 24));
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.Tag");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.contains(TAG_CC_TYPE)) {
			JComboBox typeBox = (JComboBox) e.getSource();
			if (typeBox.getSelectedItem() != null) {
				table.getCellEditor(table.getModel().getRowCount() - 1, 0).stopCellEditing();
				String type = typeBox.getSelectedItem().toString();
				updateContext(type, tagBox);
			}
		} else if (cmd.equals(ADD)) {
			// add a new default row to the table
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[] { Config.language.getProperty(TAG_FIRST_LINE), operationBox.getItemAt(0) });

		} else if (cmd.equals(APPLY)) {

			StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());

			Set<ONDEXConcept> concepts = null;
			Set<ONDEXRelation> relations = null;

			for (int row = 0; row < table.getModel().getRowCount(); row++) {

				// check if tag was selected
				if (!table.getModel().getValueAt(row, 0).equals(Config.language.getProperty(TAG_FIRST_LINE))) {
					// get value from table
					IntegerStringWrapper tag = (IntegerStringWrapper) table.getModel().getValueAt(row, 0);

					ONDEXConcept concept = graph.getConcept(tag.getValue());

					Filter filter = new Filter();

					try {
						ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
						fa.addOption(ArgumentNames.TAG_ARG, concept.getId());

						filter.addONDEXListener(new ONDEXLogger());
						filter.setONDEXGraph(graph);
						filter.setArguments(fa);
						filter.start();
					} catch (InvalidPluginArgumentException e1) {
						e1.printStackTrace();
					}

					// store visible relations
					if (row == 0) {
						concepts = BitSetFunctions.copy(filter.getVisibleConcepts());
						relations = BitSetFunctions.copy(filter.getVisibleRelations());
					} else {
						// operator from previous row more logical
						String operation = (String) table.getModel().getValueAt(row - 1, 1);

						// and operator
						if (operation.equals(AND)) {
							concepts.retainAll(filter.getVisibleConcepts());
							relations.retainAll(filter.getVisibleRelations());
						}

						// or operator
						if (operation.equals(OR)) {
							concepts.addAll(filter.getVisibleConcepts());
							relations.addAll(filter.getVisibleRelations());
						}

						// not operator
						if (operation.equals(NOT)) {
							concepts.removeAll(filter.getVisibleConcepts());
							relations.removeAll(filter.getVisibleRelations());
						}
					}
				} else {
					// first row means everything reset
					if (row == 0) {
						// set entire graph to visible if filter == none
						setGraphVisible(true);

						// propagate change to viewer
						viewer.getVisualizationViewer().getModel().fireStateChanged();
					}
				}
			}

			if (concepts != null) {

				// set first entire graph to invisible
				setGraphVisible(false);

				// set only graph returned by filter to visible
				setGraphVisible(concepts, relations);

				if (showRelations.isSelected()) {
					Collection<ONDEXConcept> visible = graph.getVertices();
					for (ONDEXRelation r : graph.getRelations()) {
						if (visible.contains(r.getToConcept()) && visible.contains(r.getFromConcept())) {
							graph.setVisibility(r, true);
						}
					}
				}

				// propagate change to viewer
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();

			used = true;
		}
	}

	/**
	 * adds the context items to the drop-down list and sorts them
	 * alpha-numerically
	 * 
	 * @param typeBox
	 * @param contextBox
	 */
	private void addContextToComboBox(JComboBox typeBox, JComboBox contextBox) {
		// add context items to combo box
		typeBox.removeAllItems();
		contextBox.removeAllItems();
		typeBox.addItem(Config.language.getProperty(TAG_TYPE_FIRST_LINE));
		contextBox.addItem(Config.language.getProperty(TAG_FIRST_LINE));
		List<Object> list = new ArrayList<Object>();
		list.add(Config.language.getProperty(TAG_FIRST_LINE));
		HashSet<String> contextCCTypeList = new HashSet<String>();
		for (ONDEXConcept c : graph.getAllTags()) {

			// check concept class exists in tagCCType
			if (!contextCCTypeList.contains(c.getOfType().getId())) {
				contextCCTypeList.add(c.getOfType().getId());
				typeBox.addItem(c.getOfType().getId());
			}
			IntegerStringWrapper wrapper = new IntegerStringWrapper(c.getId(), ConceptListUtils.getDefaultNameForConcept(c));
			contextBox.addItem(wrapper);
			list.add(wrapper);
		}

		addAutoCompleteSupport(list, contextBox);
	}

	/**
	 * Adds auto complete support to the drop down box.
	 * 
	 * @param list
	 *            list representation of content
	 * @param contextBox
	 *            JComboBox to wrap
	 */
	private void addAutoCompleteSupport(List<Object> list, final JComboBox contextBox) {
		final Object[] elements = list.toArray();

		// request the event-dispatching thread to run certain code
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				SortedList<Object> sorted = new SortedList<Object>(GlazedLists.eventListOf(elements));
				// auto completion support for selection list
				support = AutoCompleteSupport.install(contextBox, sorted);
				support.setStrict(true);
				support.setFilterMode(TextMatcherEditor.CONTAINS);
			}
		};
		SwingUtilities.invokeLater(doWorkRunnable);

		contextBox.revalidate();
	}

	/**
	 * Updates the contexts list after the CC type has changed.
	 * 
	 * @param cc
	 *            the cc to restrict to
	 * @param contextBox
	 *            JComboBox to be updated
	 */
	private void updateContext(String cc, JComboBox contextBox) {
		if (contextBox.getItemCount() == 0)
			return;

		// have to uninstall auto complete support first
		support.uninstall();

		// empty tag box
		contextBox.removeAllItems();

		List<Object> list = new ArrayList<Object>();
		list.add(Config.language.getProperty(TAG_FIRST_LINE));
		contextBox.addItem(Config.language.getProperty(TAG_FIRST_LINE));
		for (ONDEXConcept c : graph.getAllTags()) {
			if (cc.equals(Config.language.getProperty(TAG_TYPE_FIRST_LINE))) {
				IntegerStringWrapper wrapper = new IntegerStringWrapper(c.getId(), ConceptListUtils.getDefaultNameForConcept(c));
				contextBox.addItem(wrapper);
				list.add(wrapper);
				continue;
			}

			if (cc != null && !c.getOfType().getId().equals(cc)) {
				continue;
			}
			IntegerStringWrapper wrapper = new IntegerStringWrapper(c.getId(), ConceptListUtils.getDefaultNameForConcept(c));
			contextBox.addItem(wrapper);
			list.add(wrapper);
		}

		addAutoCompleteSupport(list, contextBox);
	}

	/**
	 * set visibility for entire graph
	 * 
	 * @param isVisible
	 *            if true, entire graph is visible
	 */
	private void setGraphVisible(boolean isVisible) {

		// make all nodes visible
		for (ONDEXConcept ac : graph.getConcepts()) {
			graph.setVisibility(ac, isVisible);
		}

		// make all edges visible
		for (ONDEXRelation ar : graph.getRelations()) {
			graph.setVisibility(ar, isVisible);
		}
	}

	/**
	 * set visibility of graph using a filter
	 * 
	 * @param viewC
	 * @param viewR
	 */
	private void setGraphVisible(Set<ONDEXConcept> viewC, Set<ONDEXRelation> viewR) {

		// show concepts of filter
		for (ONDEXConcept ac : viewC) {
			graph.setVisibility(ac, true);
		}

		// show relations of filter
		for (ONDEXRelation ar : viewR) {
			graph.setVisibility(ar, true);
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
