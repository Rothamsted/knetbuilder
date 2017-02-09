package net.sourceforge.ondex.ovtk2.filter.relationneighbours;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.filter.relationneighbours.ArgumentNames;
import net.sourceforge.ondex.filter.relationneighbours.Filter;
import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

import org.apache.lucene.queryParser.ParseException;

import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Filter to change the visibility according to relation neighbourhood in graph.
 * 
 * @author taubertj
 * @version 22.02.2012
 */
public class RelationNeighboursFilter extends OVTK2Filter implements ListSelectionListener, ActionListener, ItemListener {

	// generated
	private static final long serialVersionUID = -2354250287552299892L;

	// used to wrap concept list
	private ConceptListModel clm = null;

	// default depth
	private int depth = 1;

	// the important button
	private JButton goButton = null;

	// displays selection list
	private JList list = null;

	// text field for depth
	private JSlider slider = null;

	// multiple selection is enabled
	private ArrayList<ONDEXConcept> targets = null;

	// for interactive filtering
	private JCheckBox interactive = null;

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
	public RelationNeighboursFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		// The filter button
		goButton = new JButton(Config.language.getProperty("Filter.Neighbours.GoButton"));

		clm = new ConceptListModel();

		// configure new selection list for concepts
		list = new JList(clm);
		list.setCellRenderer(new CustomCellRenderer());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addListSelectionListener(this);
		list.setSelectedIndex(0);

		// register with pick state
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
		state.addItemListener(this);

		// check if list is populated
		Collection<ONDEXConcept> set = state.getPicked();
		populateList(set);

		// add to filter GUI
		add(new JLabel(Config.language.getProperty("Filter.Neighbours.Info")));
		add(new JScrollPane(list));

		// select all button
		JButton selALL = new JButton(Config.language.getProperty("Filter.Neighbours.SelectAll"));
		selALL.setActionCommand("select");
		selALL.addActionListener(this);

		// text field input for depth parameter
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
		inputPanel.add(new JLabel(Config.language.getProperty("Filter.Neighbours.Depth")));
		slider = new JSlider(JSlider.HORIZONTAL, 0, 5, 1);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);

		inputPanel.add(slider);
		inputPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		inputPanel.add(selALL);

		add(inputPanel);

		// enable interactive filtering
		interactive = new JCheckBox(Config.language.getProperty("Filter.Neighbours.Interactive"));
		add(interactive);

		goButton.setActionCommand("go");
		goButton.addActionListener(this);
		add(goButton);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}

	/**
	 * Associated with go button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("go")) {
			try {
				callFilter();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			used = true;
		}
		if (e.getActionCommand().equals("select")) {
			int beg = 0;
			int end = list.getModel().getSize() - 1;
			if (end >= 0) {
				list.setSelectionInterval(beg, end);
			}
		}
	}

	/**
	 * Calls backend filter.
	 * 
	 * @throws ParseException
	 */
	private void callFilter() throws Exception {
		if (targets != null && targets.size() > 0) {
			StateEdit edit = new StateEdit(new VisibilityUndo(viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());

			// this will also clear the concept list
			PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
			state.clear();

			// contains results
			Set<ONDEXConcept> concepts = null;
			Set<ONDEXRelation> relations = null;

			// get depth
			depth = slider.getValue();

			// prevent self-loop in interactive mode
			state.removeItemListener(this);
			Set<String> ids = new HashSet<String>();
			// multiple filter calls
			for (ONDEXConcept target : targets) {
				ids.add(String.valueOf(target.getId()));
				// highlight seed concepts
				state.pick(target, true);
			}
			state.addItemListener(this);

			// create new filter
			Filter filter = new Filter();

			// construct filter arguments
			ONDEXPluginArguments fa = new ONDEXPluginArguments(filter.getArgumentDefinitions());
			fa.addOptions(ArgumentNames.SEEDCONCEPT_ARG, ids.toArray(new String[ids.size()]));
			fa.addOption(ArgumentNames.DEPTH_ARG, Integer.valueOf(depth));

			// start filter
			filter.addONDEXListener(new ONDEXLogger());
			filter.setONDEXGraph(graph);
			filter.setArguments(fa);
			filter.start();

			// these are the filter results
			concepts = filter.getVisibleConcepts();
			relations = filter.getVisibleRelations();

			if (concepts != null) {

				// set all relations to invisible
				graph.setVisibility(graph.getRelations(), false);

				// set all concepts to invisible
				graph.setVisibility(graph.getConcepts(), false);

				// first set concepts visible
				graph.setVisibility(concepts, true);

				// second set relations visible
				graph.setVisibility(relations, true);

				// propagate change to viewer
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}

			// notify desktop
			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
		}
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.filter.OVTK2Filter#getName()
	 */
	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.RelationNeighbours");
	}

	@Override
	public boolean hasBeenUsed() {
		viewer.getVisualizationViewer().getPickedVertexState().removeItemListener(this);
		return used;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		populateList(viewer.getPickedNodes());
		// this is for interactive filtering by clicking on graph
		if (viewer.getPickedNodes().size() == 1 && interactive.isSelected()) {
			try {
				callFilter();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			used = true;
		}
	}

	/**
	 * Populates list of selected concepts
	 * 
	 * @param set
	 */
	private void populateList(Collection<ONDEXConcept> set) {

		// clear list
		list.clearSelection();
		clm.clearList();

		// get visible concepts if no selection
		if (set.isEmpty()) {
			set = viewer.getONDEXJUNGGraph().getVertices();
		}

		// fill list
		Iterator<ONDEXConcept> it = set.iterator();
		while (it.hasNext()) {
			clm.addConcept(it.next());
		}

		list.validate();
		list.repaint();

		// enable button only for non-empty list
		if (clm.getSize() == 0) {
			goButton.setEnabled(false);
		} else {
			goButton.setEnabled(true);
		}

		// only one element, select by default
		if (clm.getSize() == 1) {
			list.setSelectedIndex(0);
		}
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
				targets.add(((ConceptListModel) list.getModel()).getConceptAt(i));
			}
		}
	}

}
