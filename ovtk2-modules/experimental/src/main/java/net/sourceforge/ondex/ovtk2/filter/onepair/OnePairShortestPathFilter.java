package net.sourceforge.ondex.ovtk2.filter.onepair;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.onepairshortestpath.ArgumentNames;
import net.sourceforge.ondex.filter.onepairshortestpath.Filter;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;
import net.sourceforge.ondex.tools.threading.monitoring.MonitoringToolKit;
import net.sourceforge.ondex.tools.threading.monitoring.SimpleMonitor;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Filter to change the visibility according to shortest paths present in graph.
 * 
 * @author weilej
 */
public class OnePairShortestPathFilter extends OVTK2Filter implements
		ListSelectionListener, ActionListener {

	// generated
	private static final long serialVersionUID = -2354250287552299891L;

	// used to wrap concept list
	private ConceptListModel clm_start, clm_stop = null;

	// displays selection list
	private JList list_start = null, list_stop = null;

	// the important button
	private JButton goButton = null;

	// multiple selection is enabled
	private ArrayList<ONDEXConcept> startSet = null, endSet = null;

	// the empty entry for the attribute name selection box
	private String defVal = "";

	// a checkbox for switching between directed/undirected mode
	private JCheckBox dirBox;

	// inverse weight mode
	private JCheckBox invBox;

	// attribute name selection combo box
	private JComboBox anBox;

	// the currently selected attribute name's ID
	private String ANselection;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Constructor extracts current selection of concepts in graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer which has current visualisation
	 */
	public OnePairShortestPathFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// // The magic button
		// goButton = new JButton("Filter Graph");
		// goButton.setEnabled(false);
		// goButton.setActionCommand("go");
		// goButton.addActionListener(this);

		clm_start = new ConceptListModel();
		clm_stop = new ConceptListModel();

		list_start = new JList(clm_start);
		list_start.setCellRenderer(new CustomCellRenderer());

		list_stop = new JList(clm_stop);
		list_stop.setCellRenderer(new CustomCellRenderer());

		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer()
				.getPickedVertexState();
		Set<ONDEXConcept> set = state.getPicked();

		Iterator<ONDEXConcept> it = set.iterator();
		while (it.hasNext()) {
			ONDEXConcept node = it.next();
			ONDEXConcept ac = node;
			clm_start.addConcept(ac);
			clm_stop.addConcept(ac);
		}

		// check if list is populated
		if (clm_start.getSize() == 0) {
			add(new JLabel("There are no nodes selected in the graph."));
		} else {
			list_start.validate();
			list_start
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list_start.addListSelectionListener(this);

			add(new JLabel("Select start concept: "));
			add(new JScrollPane(list_start));

			list_stop.validate();
			list_stop
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list_stop.addListSelectionListener(this);

			add(new JLabel("Select end concept: "));
			add(new JScrollPane(list_stop));
		}

		String[] anames = getAttributeNames();

		anBox = new JComboBox(anames);
		anBox.setSelectedItem(defVal);
		anBox.addActionListener(this);
		if (anames.length == 1)
			anBox.setEnabled(false);

		dirBox = new JCheckBox("directed");
		invBox = new JCheckBox("inverse weight");

		goButton = new JButton("Filter graph");
		goButton.setActionCommand("go");
		goButton.addActionListener(this);
		goButton.setEnabled(false);

		add(new JLabel(" Choose edge weight attribute: "));
		add(anBox);
		add(dirBox);
		add(invBox);

		add(goButton);
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	/**
	 * Extracts all attribute names corresponding to GDSs with
	 * <code>Double</code> values, that can be used as edge weights.
	 * 
	 * @return an array of attribute name IDs.
	 */
	private String[] getAttributeNames() {
		Vector<String> v = new Vector<String>();
		v.add(defVal);
		for (AttributeName a : graph.getMetaData().getAttributeNames()) {
			if (Number.class.isAssignableFrom(a.getDataType())
					&& !AppearanceSynchronizer.attr.contains(a.getId()))
				v.add(a.getId());
		}
		return v.toArray(new String[v.size()]);
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.filter.OVTK2Filter#getName()
	 */
	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Filter.OnePairShortestPath");
	}

	/**
	 * Checks for selections in concept list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		JList list = (JList) e.getSource();
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			if (list.equals(list_start)) {
				startSet = new ArrayList<ONDEXConcept>();
				for (int i : indices) {
					startSet.add(((ConceptListModel) list.getModel())
							.getConceptAt(i));
				}
			} else {
				endSet = new ArrayList<ONDEXConcept>();
				for (int i : indices) {
					endSet.add(((ConceptListModel) list.getModel())
							.getConceptAt(i));
				}
			}
		}
		if (startSet != null && startSet.size() > 0 && endSet != null
				&& endSet.size() > 0)
			goButton.setEnabled(true);
	}

	/**
	 * Associated with go button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox)
			ANselection = (String) anBox.getSelectedItem();
		else if (e.getActionCommand().equals("go")) {
			callFilter();
			used = true;
		}
	}

	/**
	 * Calls backend filter.
	 */
	private void callFilter() {
		if (startSet != null && startSet.size() > 0 && endSet != null
				&& endSet.size() > 0) {

			final SimpleMonitor monitor = new SimpleMonitor("preparing...",
					(startSet.size() * endSet.size()) + 1);

			Thread filterThread = new Thread("shortest path filter thread") {
				public void run() {
					// contains results
					Set<ONDEXConcept> concepts = null;
					Set<ONDEXRelation> relations = null;

					boolean useweights = !(ANselection == null || ANselection
							.equals(defVal));// no nullpointer danger because of
					// lazy
					// evaluation!

					boolean aborted = false;

					StringBuilder messages = new StringBuilder(
							"Errors occurred during operation:\n");

					long before = System.currentTimeMillis();

					// multiple filter calls
					Iterator<ONDEXConcept> it = startSet.iterator();
					while (it.hasNext()) {
						ONDEXConcept startConcept = it.next();

						Iterator<ONDEXConcept> it2 = endSet.iterator();
						while (it2.hasNext()) {
							ONDEXConcept endConcept = it2.next();

							if (startConcept.equals(endConcept))
								continue;
							String message = null;
							// create new filter
							Filter filter = new Filter();
							try {
								// construct filter arguments
								ONDEXPluginArguments fa = new ONDEXPluginArguments(
										filter.getArgumentDefinitions());
								fa.addOption(ArgumentNames.STARTCONCEPT_ARG,
										startConcept.getId());
								fa.addOption(ArgumentNames.STOPCONCEPT_ARG,
										endConcept.getId());
								fa.addOption(ArgumentNames.ONLYDIRECTED_ARG,
										dirBox.isSelected());
								fa.addOption(ArgumentNames.USEWEIGHTS_ARG,
										useweights);
								if (useweights)
									fa.addOption(
											ArgumentNames.WEIGHTATTRIBUTENAME_ARG,
											ANselection);
								fa.addOption(ArgumentNames.INVERSE_WEIGHT_ARG,
										invBox.isSelected());

								filter.addONDEXListener(new ONDEXLogger());
								filter.setONDEXGraph(graph);
								filter.setArguments(fa);

								filter.start();
							} catch (Exception e) {
								if (e.getMessage() != null)
									message = e.getMessage();
								else
									message = e.toString();
							}

							if (message == null) {
								if (concepts == null) {
									concepts = filter.getVisibleConcepts();
									relations = filter.getVisibleRelations();
								} else {
									concepts = BitSetFunctions.or(concepts,
											filter.getVisibleConcepts());
									relations = BitSetFunctions.or(relations,
											filter.getVisibleRelations());
								}
							} else {
								messages.append(message + "\n");
							}
							aborted = !monitor.next(MonitoringToolKit
									.calculateWaitingTime(before, monitor));
							if (aborted) {
								break;
							}
						}
						if (aborted) {
							break;
						}
					}
					if (!aborted) {
						if (concepts != null) {
							StateEdit edit = new StateEdit(new VisibilityUndo(
									viewer.getONDEXJUNGGraph()), this.getName());
							OVTK2Desktop desktop = OVTK2Desktop.getInstance();
							desktop.setRunningProcess(this.getName());

							// set all relations to invisible
							for (ONDEXRelation r : graph.getRelations()) {
								graph.setVisibility(r, false);
							}

							// set all concepts to invisible
							for (ONDEXConcept c : graph.getConcepts()) {
								graph.setVisibility(c, false);
							}

							// first set concepts visible
							for (ONDEXConcept c : concepts) {
								graph.setVisibility(c, true);
							}

							// second set relations visible
							for (ONDEXRelation r : relations) {
								graph.setVisibility(r, true);
							}

							// propagate change to viewer
							viewer.getVisualizationViewer().getModel()
									.fireStateChanged();
							edit.end();
							viewer.getUndoManager().addEdit(edit);
							desktop.getOVTK2Menu().updateUndoRedo(viewer);
							desktop.notifyTerminationOfProcess();

							monitor.complete();
						} else {

							monitor.complete();
							JOptionPane.showMessageDialog(OVTK2Desktop
									.getInstance().getMainFrame(), messages
									.toString(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			};

			OVTKProgressMonitor.start(
					OVTK2Desktop.getInstance().getMainFrame(),
					"Shortest path filter", monitor);
			filterThread.start();

		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
