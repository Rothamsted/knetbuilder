package net.sourceforge.ondex.ovtk2.filter.allpairs;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.filter.allpairs.ArgumentNames;
import net.sourceforge.ondex.filter.allpairs.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;

/**
 * This filter removes all inefficient edges from the graph. Only those who are
 * part of a shortest path between any two nodes remain.
 * 
 * @author Jochen Weile, B.Sc.
 */
public class AllPairsShortestPathFilter extends OVTK2Filter implements
		ActionListener {

	// ####FIELDS####

	// generated
	private static final long serialVersionUID = -2586544291608372163L;

	/**
	 * The empty entry for the attribute name selector.
	 */
	private String defVal = "";

	/**
	 * a check box for switching directed/undirected mode.
	 */
	private JCheckBox dirBox;

	/**
	 * a checkbox for switching weight inversion.
	 */
	private JCheckBox invBox;

	/**
	 * A combo box for choosing the attribute name that represents the edge
	 * weights.
	 */
	private JComboBox anBox;

	/**
	 * The start button.
	 */
	private JButton goButton;

	/**
	 * The currently selected attribute name.
	 */
	private String selection;

	/**
	 * The actual filter.
	 */
	private Filter filter;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	// private Semaphore semaphore;

	// ####CONSTRUCTOR####

	/**
	 * The standard constructor.
	 */
	public AllPairsShortestPathFilter(OVTK2Viewer viewer) {
		super(viewer);
		setupGUI();
	}

	// ####METHODS####

	/**
	 * Builds the UI for the filter.
	 */
	private void setupGUI() {
		String[] anames = getAttributeNames();
		Arrays.sort(anames);

		goButton = new JButton("Filter graph");
		goButton.setActionCommand("go");
		goButton.addActionListener(this);
		goButton.setEnabled(false);

		if (anames.length > 1) {
			setLayout(new GridLayout(5, 1, 3, 3));
			anBox = new JComboBox(anames);
			anBox.setSelectedItem(defVal);
			anBox.addActionListener(this);

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

		} else {
			setLayout(new GridLayout(2, 1, 3, 3));
			add(new JLabel(
					"<html>No attribute names associated with <code>Double</code> found!</html>"));
		}

		add(goButton);
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
			if (Number.class.isAssignableFrom(a.getDataType()))
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
				.getProperty("Name.Menu.Filter.AllPairsShortestPath");
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			selection = (String) anBox.getSelectedItem();
			if (!selection.equals(defVal))
				goButton.setEnabled(true);
			else
				goButton.setEnabled(false);
		} else if (e.getActionCommand().equals("go")) {
			filter = new Filter();
			Thread filterthread = new Thread("filter") {
				public void run() {
					OVTK2Desktop.getInstance().setRunningProcess(
							"AllPairsShortestPathFilter");
					try {
						filter(selection, dirBox.isSelected(),
								invBox.isSelected());
					} catch (InvalidPluginArgumentException e1) {
						ErrorDialog.show(e1);
					}
					OVTK2Desktop.getInstance().setRunningProcess("none");
				}
			};
			filterthread.start();

			OVTKProgressMonitor.waitForInitialization(filter);
			OVTKProgressMonitor.start(
					OVTK2Desktop.getInstance().getMainFrame(),
					"All pairs shortest path filter", filter);

			used = true;
		}
	}

	/**
	 * starts the actual filter.
	 * 
	 * @param attributeName
	 *            the ID of the attribute name that contains the edge weight.
	 * @param directed
	 *            to switch between directed/undirected mode.
	 */
	private void filter(String attributeName, boolean directed, boolean inverse)
			throws InvalidPluginArgumentException {

		StateEdit edit = new StateEdit(new VisibilityUndo(
				viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		ArgumentDefinition<?>[] args = filter.getArgumentDefinitions();
		ONDEXPluginArguments fa = new ONDEXPluginArguments(
				filter.getArgumentDefinitions());

		String name = null;
		Object value = null;
		for (ArgumentDefinition<?> arg : args) {
			name = arg.getName();
			if (name.equals(ArgumentNames.WEIGHTATTRIBUTENAME_ARG))
				value = attributeName;
			else if (name.equals(ArgumentNames.ONLYDIRECTED_ARG))
				value = new Boolean(directed);
			fa.addOption(name, value);
			if (name.equals(ArgumentNames.INVERSE_WEIGHT_ARG))
				value = new Boolean(inverse);
		}

		filter.setONDEXGraph(graph);
		filter.setArguments(fa);

		OVTKProgressMonitor.notifyInitializationDone(filter);
		filter.start();

		// set all concepts to visible
		for (ONDEXConcept c : graph.getConcepts()) {
			graph.setVisibility(c, true);
		}

		// set all relations to invisible
		for (ONDEXRelation r : graph.getRelations()) {
			graph.setVisibility(r, false);
		}

		// set filter results to visible
		for (ONDEXRelation r : filter.getVisibleRelations()) {
			graph.setVisibility(r, true);
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
