package net.sourceforge.ondex.ovtk2.filter.shortestpath;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.shortestpath.ArgumentNames;
import net.sourceforge.ondex.filter.shortestpath.Filter;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Filter to change the visibility according to shortest paths present in graph.
 * 
 * @author taubertj
 * @version 26.03.2008
 */
public class ShortestPathFilter extends OVTK2Filter implements
		ListSelectionListener, ActionListener {

	// generated
	private static final long serialVersionUID = -2354250287552299891L;

	// used to wrap concept list
	private ConceptListModel clm = null;

	// displays selection list
	private JList list = null;

	// the important button
	private JButton goButton = null;

	// multiple selection is enabled
	private ArrayList<ONDEXConcept> targets = null;

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
	public ShortestPathFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.setActionCommand("go");
		goButton.addActionListener(this);

		clm = new ConceptListModel();

		list = new JList(clm);
		list.setCellRenderer(new CustomCellRenderer());

		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer()
				.getPickedVertexState();
		Set<ONDEXConcept> set = state.getPicked();

		Iterator<ONDEXConcept> it = set.iterator();
		while (it.hasNext()) {
			ONDEXConcept node = it.next();
			ONDEXConcept ac = node;
			clm.addConcept(ac);
		}

		// check if list is populated
		if (clm.getSize() == 0) {
			add(new JLabel("There are no concepts selected in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select Concept(s) to filter graph with: "));
			add(new JScrollPane(list));
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
					&& !AppearanceSynchronizer.attr.contains(a.getId())) {
				if (graph.getRelationsOfAttributeName(a).size() > 0)
					v.add(a.getId());
			}
		}
		String[] sorted = v.toArray(new String[v.size()]);
		Arrays.sort(sorted);
		return sorted;
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.filter.OVTK2Filter#getName()
	 */
	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.ShortestPath");
	}

	/**
	 * Checks for selections in concept list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);
			targets = new ArrayList<ONDEXConcept>();
			for (int i : indices) {
				targets.add(((ConceptListModel) list.getModel())
						.getConceptAt(i));
			}
		}
	}

	/**
	 * Associated with go button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox)
			ANselection = (String) anBox.getSelectedItem();
		else if (e.getActionCommand().equals("go"))
			try {
				callFilter();
				used = true;
			} catch (InvalidPluginArgumentException e1) {
				ErrorDialog.show(e1);
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

			// contains results
			Set<ONDEXConcept> concepts = null;
			Set<ONDEXRelation> relations = null;

			boolean useweights = !(ANselection == null || ANselection
					.equals(defVal));// no nullpointer danger because of lazy
			// evaluation!

			// multiple filter calls
			for (ONDEXConcept target : targets) {

				// create new filter
				Filter filter = new Filter();

				// construct filter arguments
				ONDEXPluginArguments fa = new ONDEXPluginArguments(
						filter.getArgumentDefinitions());
				fa.addOption(ArgumentNames.SEEDCONCEPT_ARG, target.getId());
				fa.addOption(ArgumentNames.ONLYDIRECTED_ARG,
						dirBox.isSelected());
				fa.addOption(ArgumentNames.USEWEIGHTS_ARG, useweights);
				if (useweights) {
					fa.addOption(ArgumentNames.WEIGHTATTRIBUTENAME_ARG,
							ANselection);
				}
				fa.addOption(ArgumentNames.INVERSE_WEIGHT_ARG,
						invBox.isSelected());

				filter.addONDEXListener(new ONDEXLogger());
				filter.setONDEXGraph(graph);
				filter.setArguments(fa);
				filter.start();

				if (concepts == null) {
					concepts = filter.getVisibleConcepts();
					relations = filter.getVisibleRelations();
				} else {
					concepts = BitSetFunctions.or(concepts,
							filter.getVisibleConcepts());
					relations = BitSetFunctions.or(relations,
							filter.getVisibleRelations());
				}
			}

			if (concepts != null) {

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
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
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
