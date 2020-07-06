package net.sourceforge.ondex.ovtk2.filter.degree;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptClassListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.RelationTypeListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Implements a degree specific filter with concept class restriction. Sets
 * visibility of nodes according to their in- and/or out-degree.
 * 
 * @author Jan
 * 
 */
public class DegreeFilter extends OVTK2Filter implements ActionListener,
		ListSelectionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7893723653797469975L;

	// displays selection list
	private JList cclist = null;

	// selection list for relation types
	private JList inList = null;

	// selection list for relation types
	private JList outList = null;

	// multiple selection is enabled
	private ArrayList<ConceptClass> ccs = null;

	// multiple selection is enabled
	private Set<RelationType> rtsIn = null;

	// multiple selection is enabled
	private Set<RelationType> rtsOut = null;

	// the important button
	private JButton goButton = null;

	// change visibility
	private boolean visibility = false;

	// contains number for out degree
	private JSpinner outdegree = null;

	// contains number for in degree
	private JSpinner indegree = null;

	// check box for out degree
	private JCheckBox outcheck = new JCheckBox();

	// check box for in degree
	private JCheckBox incheck = new JCheckBox();

	// operation for out degree
	private JComboBox outop = new JComboBox(new String[] { "<", "=", ">" });

	// operation for in degree
	private JComboBox inop = new JComboBox(new String[] { "<", "=", ">" });

	// logical combination between in and out degree
	private JComboBox logical = new JComboBox(new String[] { "OR", "AND" });
	
	/**
	 * Filter has been used
	 */
	private boolean used = false;

	public DegreeFilter(OVTK2Viewer viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.addActionListener(this);

		ConceptClassListModel cclm = new ConceptClassListModel();

		cclist = new JList(cclm);
		cclist.setCellRenderer(new CustomCellRenderer());

		// get concept classes from meta data
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(cc);
			if (concepts != null) {
				// check concepts exists on this ConceptClass
				if (concepts.size() > 0) {
					cclm.addConceptClass(cc);
				}
			}
		}

		// check if list is populated
		if (cclm.getSize() == 0) {
			add(new JLabel("There are no ConceptClass Objects in the Graph."));
		} else {
			cclist.validate();
			cclist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			cclist.addListSelectionListener(this);

			add(new JLabel("Select ConceptClass to filter nodes with"));
			add(new JScrollPane(cclist));
		}

		// degree depth spinner
		SpinnerModel outmodel = new SpinnerNumberModel(1, // initial value
				0, // min
				1000, // max
				1); // step

		SpinnerModel inmodel = new SpinnerNumberModel(1, // initial value
				0, // min
				1000, // max
				1); // step

		// init fields for degrees, empty means default
		outdegree = new JSpinner(outmodel);
		indegree = new JSpinner(inmodel);

		// put a label for the out degree text field
		JPanel outpanel = new JPanel(new FlowLayout());
		outpanel.add(new JLabel("Out-degree: "));
		outpanel.add(outop);
		outpanel.add(outdegree);
		outcheck.setSelected(true);
		outpanel.add(outcheck);
		add(outpanel);

		RelationTypeListModel rtslm = new RelationTypeListModel();

		outList = new JList(rtslm);
		outList.setCellRenderer(new CustomCellRenderer());

		// get relation types from meta data
		for (RelationType rts : graph.getMetaData().getRelationTypes()) {
			Set<ONDEXRelation> relations = graph.getRelationsOfRelationType(rts);
			if (relations != null) {
				// check relations exists for this RelationType
				if (relations.size() > 0) {
					rtslm.addRelationType(rts);
				}
			}
		}

		// check if list is populated
		if (rtslm.getSize() == 0) {
			add(new JLabel("There are no RelationType Objects in the Graph."));
		} else {
			outList.validate();
			outList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			outList.addListSelectionListener(this);

			add(new JLabel("Select RelationTypes for out-degree:"));
			add(new JScrollPane(outList));
		}

		add(logical);

		// put a label for the in degree text field
		JPanel inpanel = new JPanel(new FlowLayout());
		inpanel.add(new JLabel("In-degree: "));
		inpanel.add(inop);
		inpanel.add(indegree);
		incheck.setSelected(true);
		inpanel.add(incheck);
		add(inpanel);

		rtslm = new RelationTypeListModel();

		inList = new JList(rtslm);
		inList.setCellRenderer(new CustomCellRenderer());

		// get relation types from meta data
		for (RelationType rts : graph.getMetaData().getRelationTypes()) {
			Set<ONDEXRelation> relations = graph.getRelationsOfRelationType(rts);
			if (relations != null) {
				// check relations exists for this RelationType
				if (relations.size() > 0) {
					rtslm.addRelationType(rts);
				}
			}
		}

		// check if list is populated
		if (rtslm.getSize() == 0) {
			add(new JLabel("There are no RelationType Objects in the Graph."));
		} else {
			inList.validate();
			inList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			inList.addListSelectionListener(this);

			add(new JLabel("Select RelationTypes for in-degree:"));
			add(new JScrollPane(inList));
		}

		// visibility settings
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
		return Config.language.getProperty("Name.Menu.Filter.Degree");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (ccs != null && ccs.size() > 0) {
			StateEdit edit = new StateEdit(new VisibilityUndo(
					viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());

			// get user selection
			int in = (Integer) indegree.getValue();
			int out = (Integer) outdegree.getValue();

			// contains concepts to be removed
			Set<ONDEXConcept> change = new HashSet<ONDEXConcept>();

			// filter only on concepts of selected concept class
			Iterator<ConceptClass> it = ccs.iterator();
			while (it.hasNext()) {

				ConceptClass cc = it.next();
				Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(cc);
				for (ONDEXConcept concept : concepts) {
					// looking at all relations of concept
					Set<ONDEXRelation> relations = graph
							.getRelationsOfConcept(concept);
					int inTotal = 0;
					int outTotal = 0;
					// count all incoming or outgoing relations
					for (ONDEXRelation relation : relations) {
						if (relation.getFromConcept().equals(concept)
								&& (rtsOut == null || rtsOut.contains(relation
										.getOfType()))) {
							outTotal++;
						} else if (relation.getToConcept().equals(concept)
								&& (rtsIn == null || rtsIn.contains(relation
										.getOfType()))) {
							inTotal++;
						}
					}

					// default OR combination
					if (logical.getSelectedItem().equals("OR")) {
						if (outcheck.isSelected()) {
							String op = outop.getSelectedItem().toString();
							if (op.equals("<") && outTotal < out) {
								change.add(concept);
							} else if (op.equals("=") && outTotal == out) {
								change.add(concept);
							} else if (op.equals(">") && outTotal > out) {
								change.add(concept);
							}
						}

						if (incheck.isSelected()) {
							String op = inop.getSelectedItem().toString();
							if (op.equals("<") && inTotal < in) {
								change.add(concept);
							} else if (op.equals("=") && inTotal == in) {
								change.add(concept);
							} else if (op.equals(">") && inTotal > in) {
								change.add(concept);
							}
						}
					}

					// for AND both degrees need to be set
					else if (logical.getSelectedItem().equals("AND")) {
						if (!outcheck.isSelected() || !incheck.isSelected()) {
							JOptionPane
									.showInternalMessageDialog(
											OVTK2Desktop.getInstance()
													.getDesktopPane(),
											"You need to specify both (in- and out-degree) to use the AND operation.",
											"Wrong settings",
											JOptionPane.WARNING_MESSAGE);
							return;
						} else {

							boolean changeout = false;
							String op = outop.getSelectedItem().toString();
							if (op.equals("<") && outTotal < out) {
								changeout = true;
							} else if (op.equals("=") && outTotal == out) {
								changeout = true;
							} else if (op.equals(">") && outTotal > out) {
								changeout = true;
							}

							boolean changein = false;
							op = inop.getSelectedItem().toString();
							if (op.equals("<") && inTotal < in) {
								changein = true;
							} else if (op.equals("=") && inTotal == in) {
								changein = true;
							} else if (op.equals(">") && inTotal > in) {
								changein = true;
							}

							if (changeout && changein) {
								change.add(concept);
							}
						}
					}
				}
			}

			// set concepts visibility
			for (ONDEXConcept c : change) {
				graph.setVisibility(c, visibility);
			}

			// set relations between nodes to visible
			if (visibility) {
				for (ONDEXConcept c : change) {
					Set<ONDEXRelation> relations = graph.getRelationsOfConcept(c);
					for (ONDEXRelation relation : relations) {
						if (relation.getFromConcept().equals(c)
								&& (rtsOut == null || rtsOut.contains(relation
										.getOfType()))) {
							graph.setVisibility(relation.getToConcept(),
									visibility);
							graph.setVisibility(relation, visibility);
						} else if (relation.getToConcept().equals(c)
								&& (rtsIn == null || rtsIn.contains(relation
										.getOfType()))) {
							graph.setVisibility(relation.getFromConcept(),
									visibility);
							graph.setVisibility(relation, visibility);
						}
					}
				}
			}

			// propagate change to viewer
			viewer.getVisualizationViewer().getModel().fireStateChanged();

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
			used = true;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(cclist)) {
			int[] indices = cclist.getSelectedIndices();
			if (indices.length > 0) {
				goButton.setEnabled(true);
				ccs = new ArrayList<ConceptClass>();
				for (int i : indices) {
					ccs.add(((ConceptClassListModel) cclist.getModel())
							.getConceptClassAt(i));
				}
			}
		} else if (e.getSource().equals(inList)) {
			int[] indices = inList.getSelectedIndices();
			if (indices.length > 0) {
				rtsIn = new HashSet<RelationType>();
				for (int i : indices) {
					rtsIn.add(((RelationTypeListModel) inList.getModel())
							.getRelationTypeAt(i));
				}
			}
		} else if (e.getSource().equals(outList)) {
			int[] indices = outList.getSelectedIndices();
			if (indices.length > 0) {
				rtsOut = new HashSet<RelationType>();
				for (int i : indices) {
					rtsOut.add(((RelationTypeListModel) outList.getModel())
							.getRelationTypeAt(i));
				}
			}
		}

	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
