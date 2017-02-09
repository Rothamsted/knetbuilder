package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2GraphMouse;
import net.sourceforge.ondex.ovtk2.ui.mouse.OVTK2PickingMousePlugin;
import net.sourceforge.ondex.ovtk2.ui.toolbars.ChemicalSearch;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.IdLabel;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Showing table for showing search results.
 * 
 * @author taubertj
 * @version 14.07.2008
 */
public class DialogChemicalSearch extends OVTK2Dialog implements ListSelectionListener, MouseListener {

	private class FilterJob {
		Set<ONDEXConcept> concepts;
		Set<ONDEXRelation> relations;

		/**
		 * Execute neighbourhood filter
		 */
		public void callFilter() throws Exception {

			StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), "Search Result Filter");
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess("Search Result Filter");

			ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
			PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
			state.clear();

			// contains results
			concepts = BitSetFunctions.create(jung, ONDEXConcept.class, new BitSet());
			relations = BitSetFunctions.create(jung, ONDEXRelation.class, new BitSet());

			// get depth
			Integer depth = (Integer) spinner.getValue();
			if (depth > 5) {
				int option = JOptionPane.showInternalConfirmDialog(DialogChemicalSearch.this, Config.language.getProperty("Dialog.SearchResult.DepthWarning"), Config.language.getProperty("Dialog.SearchResult.DepthWarningTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.CANCEL_OPTION)
					return;
			}

			// multiple filter calls
			boolean deleted = false;
			List<ONDEXConcept> targets = new ArrayList<ONDEXConcept>();
			for (int row : table.getSelectedRows()) {
				int index = table.convertRowIndexToModel(row);
				Integer selection = ((IdLabel) model.getValueAt(index, 0)).getId();
				ONDEXConcept c = jung.getConcept(selection);
				if (c == null)
					deleted = true;
				else
					targets.add(c);
			}

			// notify user of deleted concepts
			if (deleted)
				JOptionPane.showInternalMessageDialog(DialogChemicalSearch.this, Config.language.getProperty("Dialog.SearchResult.DeletedWarning"), Config.language.getProperty("Dialog.SearchResult.DeletedWarningTitle"), JOptionPane.ERROR_MESSAGE);

			// notify user of empty search
			if (targets.size() == 0) {
				JOptionPane.showInternalMessageDialog(DialogChemicalSearch.this, Config.language.getProperty("Dialog.SearchResult.SelectWarning"), Config.language.getProperty("Dialog.SearchResult.SelectWarningTitle"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			LOG.info("Applying neighbourhood on: " + targets);

			for (ONDEXConcept root : targets) {
				// highlight seed concepts
				state.pick(root, true);
				recurse(jung, root, depth);
			}

			LOG.info("Found " + concepts.size() + " concepts and " + relations.size() + " relations");
			if (concepts.size() > 0) {

				// set all concepts to invisible unless they are in the
				// neighbourhood
				for (ONDEXConcept node : jung.getVertices()) {
					jung.setVisibility(node, concepts.contains(node) || (jung.isVisible(node) && !hideAllOthers.isSelected()));
				}

				if (restrictRelations.isSelected()) {
					// set all relations to invisible iff the restrict checkbox
					// was checked set neighbourhood relations visible
					jung.setVisibility(jung.getEdges(), false);
				}

				// set concepts and relations visible
				jung.setVisibility(concepts, true);
				jung.setVisibility(relations, true);

				// propagate change to viewer
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
		}

		/**
		 * Recursively performing a DFS on the graph to retrieve neighbours.
		 */
		private void recurse(ONDEXGraph aog, ONDEXConcept root, int depth) {

			// add root as visible neighbour
			concepts.add(root);

			if (depth > 0) {
				// add all relations to found list
				Set<ONDEXRelation> neighbourRels = aog.getRelationsOfConcept(root);
				relations.addAll(neighbourRels);

				// next recursion step
				depth--;
				for (ONDEXRelation r : neighbourRels) {
					ONDEXConcept from = r.getFromConcept();
					ONDEXConcept to = r.getToConcept();

					// prevent self loops
					if (!from.equals(to)) {
						// outgoing relations
						if (root.equals(from)) {
							recurse(aog, to, depth);
						}
						// incoming relations
						else if (root.equals(to)) {
							recurse(aog, from, depth);
						}
						// special case of qualifier was root concept
						else {
							recurse(aog, to, depth);
							recurse(aog, from, depth);
						}
					}
				}
			}
		}
	}

	/**
	 * Wraps the results into a table model.
	 * 
	 * @author taubertj
	 */
	private class ResultTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -6992756349799812531L;

		// table header
		private String[] columnNames = new String[] { Config.language.getProperty("Dialog.SearchResult.TableLabel"), Config.language.getProperty("Dialog.SearchResult.TableMatch"), Config.language.getProperty("Dialog.SearchResult.TableInfo") };

		// contains results
		private Vector<Vector<Object>> results = null;

		/**
		 * Constructor for a given results.
		 * 
		 * @param results
		 *            Vector<Vector<Object>>
		 */
		public ResultTableModel(Vector<Vector<Object>> results) {
			this.results = results;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return IdLabel.class;
			return String.class;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getRowCount() {
			return results.size();
		}

		public Object getValueAt(int row, int col) {
			// return existing data
			return results.get(row).get(col);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	private static final Logger LOG = Logger.getLogger(DialogChemicalSearch.class);

	// generated
	private static final long serialVersionUID = 2208353118937547502L;

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	// current table model for results
	private ResultTableModel model = null;

	// table displaying results
	private JTable table = null;

	// spinner for neighbourhood depth
	private JSpinner spinner = null;

	// hides not found relations
	private JCheckBox restrictRelations;

	// hides rest of the graph
	private JCheckBox hideAllOthers;

	// performs the search
	private ChemicalSearch search;

	/**
	 * Constructs user input to view search results.
	 * 
	 * @param viewer
	 *            current OVTK2Viewer to search in
	 * @param s
	 *            search stringe
	 * @param conceptClass
	 *            possible concept class restriction
	 * @param dataSource
	 *            possible DataSource restriction
	 * @param context
	 *            possible context concept restriction
	 * @param searchMode
	 *            InChI or SMILES
	 * @param percentSimilarity
	 *            tanimoto similarity
	 * @param useChEMBL
	 *            query ChEMBL
	 */
	public DialogChemicalSearch(OVTK2Viewer viewer, String s, ConceptClass conceptClass, DataSource dataSource, ONDEXConcept context, String searchMode, int percentSimilarity, boolean useChEMBL) {
		super("Dialog.SearchResult.Title", "Properties16.gif");
		// set dialog behaviour and closing operation
		this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		this.setTitle(super.getTitle() + " (" + s + ")");

		// set internal variables
		this.viewer = viewer;

		search = new ChemicalSearch(viewer, s, conceptClass, dataSource, context, searchMode, percentSimilarity, useChEMBL);

		JPanel south = new JPanel(new GridLayout(3, 1));

		JPanel restrictPanel = new JPanel();
		BoxLayout restrictLayout = new BoxLayout(restrictPanel, BoxLayout.LINE_AXIS);
		restrictPanel.setLayout(restrictLayout);
		south.add(restrictPanel);

		JLabel restrictLabel = new JLabel("Hide relations");
		restrictPanel.add(restrictLabel);
		restrictRelations = new JCheckBox("", false);
		restrictRelations.setToolTipText("Only show the relations for the neighbourhood filter" + " that were traversed while building the neighbourhood");
		restrictPanel.add(restrictRelations);

		JLabel hideAllOthersLabel = new JLabel("Hide all others");
		restrictPanel.add(hideAllOthersLabel);
		hideAllOthers = new JCheckBox("", true);
		hideAllOthers.setToolTipText("Hide everything in the graph that is not matched by running that filter. " + "If option is not selected, then it adds whatever is hit by the search to the existing graph");
		restrictPanel.add(hideAllOthers);

		JPanel spinnerPanel = new JPanel();
		BoxLayout spinLayout = new BoxLayout(spinnerPanel, BoxLayout.LINE_AXIS);
		spinnerPanel.setLayout(spinLayout);
		south.add(spinnerPanel);

		JLabel label = new JLabel("Neighbourhood depth ");
		spinnerPanel.add(label);

		// neighbourhood depth spinner
		SpinnerModel model = new SpinnerNumberModel(1, // initial value
				0, // min
				1000, // max
				1); // step
		spinner = new JSpinner(model);
		spinnerPanel.add(spinner);

		JButton go = new JButton("Filter Graph");
		go.setActionCommand("filter");
		go.addActionListener(this);
		spinnerPanel.add(go);

		// close button
		JButton close = new JButton(Config.language.getProperty("Dialog.SearchResult.Close"));
		close.setActionCommand("close");
		close.addActionListener(this);
		south.add(close);

		// layout content
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeResultTable(), BorderLayout.CENTER);
		this.getContentPane().add(south, BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// filter neighbourhood
		if (cmd.equals("filter")) {
			try {
				new FilterJob().callFilter();
			} catch (Exception e) {
				e.printStackTrace();
				ErrorDialog.show(e);
			}
		}

		// close dialog
		if (cmd.equals("close")) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}
	}

	/**
	 * Creates the properties panel for search results.
	 * 
	 * @return JPanel
	 */
	private JPanel makeResultTable() {

		// init properties layout
		final JPanel properties = new JPanel();
		final DialogChemicalSearch instance = this;
		BoxLayout contentLayout = new BoxLayout(properties, BoxLayout.PAGE_AXIS);
		properties.setLayout(contentLayout);
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.SearchResult.Result"));
		properties.setBorder(propertiesBorder);
		properties.add(new JLabel("Searching..."));

		// run in own process
		IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
			public void task() {

				// perform search
				Vector<Vector<Object>> data = new Vector<Vector<Object>>();
				try {
					data = search.search();
				} catch (Exception e) {
					e.printStackTrace();
					properties.removeAll();
					properties.add(new JLabel("CDK Error while searching."));
					search.setCancelled(true);
					this.setCancelled(true);
					return;
				}
				model = new ResultTableModel(data);

				// setup table
				table = new JTable(model);
				table.setAutoCreateRowSorter(true);

				table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				table.getSelectionModel().addListSelectionListener(instance);
				table.addMouseListener(instance);

				// add table to properties
				JScrollPane scrollPane = new JScrollPane(table);
				JPanel tablePanel = new JPanel(new GridLayout(1, 1));
				tablePanel.add(scrollPane);
				properties.removeAll();
				properties.add(tablePanel);
				instance.pack();
			}
		};

		// start processing and monitoring
		p.start();
		OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Searching graph", search);

		return properties;
	}

	/**
	 * This is a hack to trigger a propagation of selection even if there is
	 * only one element showing.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		ListSelectionModel lsm = table.getSelectionModel();
		propagateSelection(lsm);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	private void propagateSelection(ListSelectionModel lsm) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		OVTK2GraphMouse mouse = (OVTK2GraphMouse) viewer.getVisualizationViewer().getGraphMouse();
		OVTK2PickingMousePlugin picking = mouse.getOVTK2PickingMousePlugin();

		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
		state.clear();

		if (!lsm.isSelectionEmpty()) {
			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					int index = table.convertRowIndexToModel(i);
					Integer selection = ((IdLabel) model.getValueAt(index, 0)).getId();
					ONDEXConcept node = graph.getConcept(selection);
					state.pick(node, true);

					// propagate selection in search results to content info
					if (picking != null) {
						for (ActionListener l : picking.getPickingListeners()) {
							if (l != null)
								l.actionPerformed(new ActionEvent(node, 0, "putative node pick"));
						}
					}
				}
			}

			if (state.getPicked().size() > 1)
				// fire for zooming into search results
				OVTK2Desktop.getInstance().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "zoomin"));
		}
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		ListSelectionModel lsm = (ListSelectionModel) arg0.getSource();
		propagateSelection(lsm);
	}
}
