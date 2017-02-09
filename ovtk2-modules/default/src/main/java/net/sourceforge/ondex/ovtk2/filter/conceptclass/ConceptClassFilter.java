package net.sourceforge.ondex.ovtk2.filter.conceptclass;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.filter.conceptclass.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptClassListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.DataSourceListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Filter to change the visibility according to specified concept classes.
 * 
 * @author taubertj
 * @version 19.03.2008
 */
public class ConceptClassFilter extends OVTK2Filter implements
		ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 3743904012085379845L;

	private static final String FILTER = "FILTER";

	/**
	 * used to wrap data sources list
	 */
	private DataSourceListModel dataSourceListModel = null;

	/**
	 * used to wrap concept classes list
	 */
	private ConceptClassListModel conceptClassListModel = null;

	/**
	 * displays selection list
	 */
	private JList conceptClassList = null;

	/**
	 * displays selection list
	 */
	private JList dataSourceList = null;

	/**
	 * the important button
	 */
	private JButton goButton = null;

	/**
	 * multiple selection is enabled
	 */
	private List<ConceptClass> conceptClassTargets = null;

	/**
	 * change visibility
	 */
	private boolean visibility = false;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Constructor extracts current concept classes from meta data and checks
	 * for their relevance in the graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer which has current visualisation
	 */
	public ConceptClassFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.setActionCommand(FILTER);
		goButton.addActionListener(this);

		conceptClassListModel = new ConceptClassListModel();

		conceptClassList = new JList(conceptClassListModel);
		conceptClassList.setCellRenderer(new CustomCellRenderer());

		// get concept classes from meta data
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(cc);
			if (concepts != null) {
				// check concepts exists on this ConceptClass
				if (concepts.size() > 0) {
					conceptClassListModel.addConceptClass(cc);
				}
			}
		}

		// check if list is populated
		if (conceptClassListModel.getSize() == 0) {
			add(new JLabel("There are no ConceptClass Objects in the Graph."));
		} else {
			conceptClassList.validate();
			conceptClassList
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			conceptClassList.addListSelectionListener(this);

			add(new JLabel("Select ConceptClass(es) to filter nodes with"));
			add(new JScrollPane(conceptClassList));
		}

		dataSourceListModel = new DataSourceListModel();

		dataSourceList = new JList(dataSourceListModel);
		dataSourceList.setCellRenderer(new CustomCellRenderer());

		// get data sources from meta data
		for (DataSource ds : graph.getMetaData().getDataSources()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfDataSource(ds);
			if (concepts != null) {
				// check concepts exists on this DataSource
				if (concepts.size() > 0) {
					dataSourceListModel.addDataSource(ds);
				}
			}
		}

		// check if list is populated
		if (dataSourceListModel.getSize() == 0) {
			add(new JLabel("There are no DataSource Objects in the Graph."));
		} else {
			dataSourceList.validate();
			dataSourceList
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			dataSourceList.addListSelectionListener(this);

			add(new JLabel("Optional: Select DataSource to filter nodes with"));
			add(new JScrollPane(dataSourceList));
		}

		// to clear optional data source selection
		JButton clear = new JButton("Clear DataSource Selection");
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dataSourceList.clearSelection();
			}
		});
		add(clear);

		JRadioButton yesButton = new JRadioButton("true", true);
		yesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visibility = true;
			}
		});
		JRadioButton noButton = new JRadioButton("false", false);
		noButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visibility = false;
			}
		});

		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(yesButton);
		bgroup.add(noButton);
		noButton.setSelected(true);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(1, 2));
		radioPanel.add(yesButton);
		radioPanel.add(noButton);
		radioPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Change visibility to:"));
		add(radioPanel);

		add(goButton);
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.ConceptClass");
	}

	/**
	 * Checks for selections in ConceptClass list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = conceptClassList.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);
			conceptClassTargets = new ArrayList<ConceptClass>();
			for (int i : indices) {
				conceptClassTargets
						.add(((ConceptClassListModel) conceptClassList
								.getModel()).getConceptClassAt(i));
			}
		}
	}

	/**
	 * Associated with go button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (FILTER.equals(e.getActionCommand())) {
			try {
				callFilter();
			} catch (InvalidPluginArgumentException e1) {
				ErrorDialog.show(e1);
			}

			used = true;
		} else {
			System.err.println("Unknown action command " + e.getActionCommand()
					+ " in " + this.getClass());
		}
	}

	/**
	 * Calls backend filter.
	 */
	private void callFilter() throws InvalidPluginArgumentException {

		// this is the exit condition just to make sure
		if (conceptClassTargets == null || conceptClassTargets.size() == 0)
			return;

		// now start now undo edit
		StateEdit edit = new StateEdit(new VisibilityUndo(
				viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		// new instance of filter and set arguments
		Filter filter = new Filter();

		// construct filter arguments
		ONDEXPluginArguments fa = new ONDEXPluginArguments(
				filter.getArgumentDefinitions());
		fa.addOption(Filter.EXCLUDE_ARG, true);

		// add all concept class restrictions
		Iterator<ConceptClass> it = conceptClassTargets.iterator();
		while (it.hasNext()) {
			fa.addOption(Filter.TARGETCC_ARG, it.next().getId());
		}

		// optional data source restriction
		if (dataSourceList.getSelectedValue() != null) {
			fa.addOption(Filter.FILTER_CV_ARG, dataSourceListModel
					.getDataSourceAt(dataSourceList.getSelectedIndex()).getId());
		}

		// set filter arguments
		filter.setONDEXGraph(graph);
		filter.setArguments(fa);

		// execute filter
		filter.start();

		// get results from filter
		Set<ONDEXConcept> concepts = filter.getInVisibleConcepts();
		Set<ONDEXRelation> relations = filter.getInVisibleRelations();

		// check for visibility selection
		if (visibility) {

			graph.setVisibility(concepts, true);
			graph.setVisibility(relations, true);

		} else {

			graph.setVisibility(relations, false);
			graph.setVisibility(concepts, false);

		}

		// propagate change to viewer
		viewer.getVisualizationViewer().getModel().fireStateChanged();

		edit.end();
		viewer.getUndoManager().addEdit(edit);
		desktop.getOVTK2Menu().updateUndoRedo(viewer);
		desktop.notifyTerminationOfProcess();
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
