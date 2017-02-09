package net.sourceforge.ondex.ovtk2.filter.evidencetype;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
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
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.evidencetype.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.EvidenceTypeListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Filter to change the visibility according to specified evidence type.
 * 
 * @author taubertj (copied from CVFilter by matt)
 * @version 20.11.2008
 */
public class EvidenceTypeFilter extends OVTK2Filter implements
		ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 3743904012085379845L;

	private static final String FILTER = "FILTER";

	// used to wrap EvidenceType list

	// displays selection list
	private JList list = null;

	// the important button
	private JButton goButton = null;

	// multiple selection is enabled
	private ArrayList<EvidenceType> targets = null;

	// change visibility
	private boolean visibility = false;

	private boolean onConcepts = true;
	
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
	public EvidenceTypeFilter(OVTK2Viewer viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		JRadioButton conceptButton = new JRadioButton("concepts", onConcepts);
		conceptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConcepts = true;
			}
		});
		JRadioButton relationButton = new JRadioButton("relations", !onConcepts);
		relationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConcepts = false;
			}
		});

		ButtonGroup bgroupApply = new ButtonGroup();
		bgroupApply.add(conceptButton);
		bgroupApply.add(relationButton);

		JPanel radioPanelApply = new JPanel();
		radioPanelApply.setLayout(new GridLayout(2, 1));
		radioPanelApply.add(conceptButton);
		radioPanelApply.add(relationButton);

		radioPanelApply.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Apply to"));
		this.add(radioPanelApply);

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.setActionCommand(FILTER);
		goButton.addActionListener(this);

		EvidenceTypeListModel etlm = new EvidenceTypeListModel();

		list = new JList(etlm);
		list.setCellRenderer(new CustomCellRenderer());

		// get concept classes from meta data
		for (EvidenceType et : graph.getMetaData().getEvidenceTypes()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfEvidenceType(et);
			Set<ONDEXRelation> relations = graph.getRelationsOfEvidenceType(et);
			if (concepts != null) {
				// check concepts exists on this EvidenceType
				if (concepts.size() > 0 || relations.size() > 0) {
					etlm.addEvidenceType(et);
				}
			}
		}

		// check if list is populated
		if (etlm.getSize() == 0) {
			add(new JLabel("There are no evidence types in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select evidence type to filter graph with:"));
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
		return Config.language.getProperty("Name.Menu.Filter.EvidenceType");
	}

	/**
	 * Checks for selections in EvidenceType list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);
			targets = new ArrayList<EvidenceType>();
			for (int i : indices) {
				targets.add(((EvidenceTypeListModel) list.getModel())
						.getEvidenceTypeAt(i));
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
			fa.addOption(Filter.CONCEPTS_ARG, onConcepts);

			Iterator<EvidenceType> it = targets.iterator();
			while (it.hasNext()) {
				fa.addOption(Filter.ET_ARG, it.next().getId());
			}

			filter.setONDEXGraph(graph);
			filter.setArguments(fa);

			// execute filter
			try {
				filter.start();
			} catch (Exception e) {
				// JOptionPane.showInternalMessageDialog(null, e.getMessage());
				ErrorDialog.show(e);
			}

			// get results from filter
			Set<ONDEXConcept> concepts = BitSetFunctions.andNot(
					graph.getConcepts(), filter.getVisibleConcepts());
			Set<ONDEXRelation> relations = BitSetFunctions.andNot(
					graph.getRelations(), filter.getVisibleRelations());

			// check for visibility selection
			if (visibility) {

				if (onConcepts) {
					// change visibility of concepts
					for (ONDEXConcept c : concepts) {
						graph.setVisibility(c, true);
					}
				} else {
					// change visibility of relations
					for (ONDEXRelation r : relations) {
						graph.setVisibility(r.getFromConcept(), true);
						graph.setVisibility(r.getToConcept(), true);
						graph.setVisibility(r, true);
					}
				}

			} else {

				if (onConcepts) {
					// change visibility of concepts
					for (ONDEXConcept c : concepts) {
						graph.setVisibility(c, false);
					}
				} else {
					// change visibility of relations
					for (ONDEXRelation r : relations) {
						graph.setVisibility(r, false);
					}
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
