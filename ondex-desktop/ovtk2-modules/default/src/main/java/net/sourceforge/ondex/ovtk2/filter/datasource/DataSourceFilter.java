package net.sourceforge.ondex.ovtk2.filter.datasource;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.datasource.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.DataSourceListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Filter to change the visibility according to specified data source.
 * 
 * @author taubertj (copied from ConceptClass Filter by matt)
 * @version 19.03.2008
 */
public class DataSourceFilter extends OVTK2Filter implements ListSelectionListener,
		ActionListener {

	private static final long serialVersionUID = 3743904012085379845L;

	private static final String FILTER = "FILTER";

	/**
	 * used to wrap data sources list
	 */
	private DataSourceListModel dataSourceListModel = null;

	/**
	 * displays selection list
	 */
	private JList list = null;

	/**
	 * the important button
	 */
	private JButton goButton = null;

	/**
	 * multiple selection is enabled
	 */
	private List<DataSource> targets = null;

	/**
	 * change visibility
	 */
	private boolean visibility = false;
	
	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Constructor extracts current data sources from meta data and checks
	 * for their relevance in the graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer which has current visualisation
	 */
	public DataSourceFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.setActionCommand(FILTER);
		goButton.addActionListener(this);

		dataSourceListModel = new DataSourceListModel();

		list = new JList(dataSourceListModel);
		list.setCellRenderer(new CustomCellRenderer());

		// get data sources from meta data
		for (DataSource dataSource : graph.getMetaData().getDataSources()) {
			Set<ONDEXConcept> concepts = graph
					.getConceptsOfDataSource(dataSource);
			if (concepts != null) {
				// check concepts exists on this DataSource
				if (concepts.size() > 0) {
					dataSourceListModel.addDataSource(dataSource);
				}
			}
		}

		// check if list is populated
		if (dataSourceListModel.getSize() == 0) {
			add(new JLabel(
					"There are no data source objects in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select data source to filter nodes with"));
			add(new JScrollPane(list));
		}

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
		return Config.language.getProperty("Name.Menu.Filter.DataSource");
	}

	/**
	 * Checks for selections in DataSource list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);
			targets = new ArrayList<DataSource>();
			for (int i : indices) {
				targets.add(((DataSourceListModel) list.getModel()).getDataSourceAt(i));
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
		if (targets != null && targets.size() > 0) {
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

			for (DataSource target : targets) {
				fa.addOption(Filter.DATASOURCE_ARG, target.getId());
			}

			filter.setONDEXGraph(graph);
			filter.setArguments(fa);

			// execute filter
			filter.start();

			// get results from filter
			Set<ONDEXConcept> concepts = BitSetFunctions.andNot(
					graph.getConcepts(), filter.getVisibleConcepts());
			Set<ONDEXRelation> relations = BitSetFunctions.andNot(
					graph.getRelations(), filter.getVisibleRelations());

			// check for visibility selection
			if (visibility) {

				// first set concepts visible
				for (ONDEXConcept c : concepts) {
					graph.setVisibility(c, true);
				}

				// second set relations visible
				for (ONDEXRelation r : relations) {
					graph.setVisibility(r, true);
				}

			} else {

				// change visibility of relations
				for (ONDEXRelation r : relations) {
					graph.setVisibility(r, false);
				}

				// change visibility of concepts
				for (ONDEXConcept c : concepts) {
					graph.setVisibility(c, false);
				}
			}

			// propagate change to viewer
			viewer.getVisualizationViewer().getModel().fireStateChanged();

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
