package net.sourceforge.ondex.ovtk2.filter.attributevalue;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
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

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.ONDEXListener;
import net.sourceforge.ondex.filter.attributevalue.ArgumentNames;
import net.sourceforge.ondex.filter.attributevalue.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.ValueListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Compares Attribute values with a given one to change visibility of nodes and
 * edges.
 * 
 * @author taubertj
 */
public class AttributeValueFilter extends OVTK2Filter implements
		ActionListener, ListSelectionListener, ONDEXListener {

	private static final long serialVersionUID = 5263791831446835975L;

	private static final String FILTER = "FILTER";

	private boolean onConcepts = true;

	// change visibility
	private boolean visibility = false;

	// used to wrap attribute name list
	private AttributeNameListModel anlm = null;

	// used to wrap value list
	private ValueListModel vlm = null;

	// displays selection list
	private JList list = null;

	// displays all values for selection
	private JList values = null;

	// the important button
	private JButton goButton = null;

	// which attribute name to use
	private AttributeName target = null;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	public AttributeValueFilter(OVTK2Viewer viewer) {
		super(viewer);

		initGUI();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		StateEdit edit = new StateEdit(new VisibilityUndo(
				viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		Filter filter = new Filter();
		filter.addONDEXListener(this);
		try {
			ONDEXPluginArguments fa = new ONDEXPluginArguments(
					filter.getArgumentDefinitions());
			fa.addOption(ArgumentNames.ATTRNAME_ARG, target.getId());
			for (int index : values.getSelectedIndices()) {
				fa.addOption(ArgumentNames.VALUE_ARG, vlm.getObjectAt(index)
						.toString());
			}
			fa.addOption(ArgumentNames.OPERATOR_ARG, "=");
			fa.addOption(ArgumentNames.INCLUDING_ARG, Boolean.FALSE);
			fa.addOption(ArgumentNames.MODULUS_ARG, Boolean.FALSE);

			filter.setArguments(fa);
			filter.setONDEXGraph(graph);

			filter.start();

		} catch (Exception e) {
			ErrorDialog.show(e);
		}

		for (ONDEXRelation r : filter.getInVisibleRelations()) {
			graph.setVisibility(r, visibility);
		}
		for (ONDEXConcept c : filter.getInVisibleConcepts()) {
			graph.setVisibility(c, visibility);
		}

		viewer.getVisualizationViewer().getModel().fireStateChanged();

		edit.end();
		viewer.getUndoManager().addEdit(edit);
		desktop.getOVTK2Menu().updateUndoRedo(viewer);
		desktop.notifyTerminationOfProcess();

		used = true;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.AttributeValue");
	}

	private void initGUI() {
		setLayout(new SpringLayout());

		JRadioButton conceptButton = new JRadioButton("concepts", onConcepts);
		conceptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConcepts = true;
				populateList();
				list.clearSelection();
			}
		});
		JRadioButton relationButton = new JRadioButton("relations", !onConcepts);
		relationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConcepts = false;
				populateList();
				list.clearSelection();
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

		anlm = new AttributeNameListModel();

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		// get attribute names from meta data
		populateList();

		list.validate();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);

		add(new JLabel("Select attribute to filter with"));
		add(new JScrollPane(list));

		vlm = new ValueListModel();

		values = new JList(vlm);
		values.setCellRenderer(new CustomCellRenderer());
		values.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		add(new JLabel("Select a value to filter for"));
		add(new JScrollPane(values));

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

	private void populateList() {
		anlm.clearList();
		for (AttributeName an : graph.getMetaData().getAttributeNames()) {
			if (!onConcepts) {
				Set<ONDEXRelation> relations = graph
						.getRelationsOfAttributeName(an);
				if (relations != null) {
					// check relations exists on this AttributeName
					if (relations.size() > 0
							&& !AppearanceSynchronizer.attr
									.contains(an.getId())) {
						anlm.addAttributeName(an);
					}
				}
			} else {
				Set<ONDEXConcept> concepts = graph
						.getConceptsOfAttributeName(an);
				if (concepts != null) {
					// check concepts exists on this AttributeName
					if (concepts.size() > 0
							&& !AppearanceSynchronizer.attr
									.contains(an.getId())) {
						anlm.addAttributeName(an);
					}
				}
			}
		}
		anlm.refresh();
		list.setEnabled(!anlm.isEmpty());
		list.repaint();
		this.repaint();
	}

	private void populateValues() {
		vlm.clearList();
		Set<Object> objects = new HashSet<Object>();
		if (target != null) {
			if (onConcepts) {
				for (ONDEXConcept c : graph.getConceptsOfAttributeName(target)) {
					Attribute attribute = c.getAttribute(target);
					objects.add(attribute.getValue());
				}
			} else {
				for (ONDEXRelation r : graph
						.getRelationsOfAttributeName(target)) {
					Attribute attribute = r.getAttribute(target);
					objects.add(attribute.getValue());
				}
			}
		}
		// sort array if possible
		Object[] array = objects.toArray();
		Arrays.sort(array);
		for (Object o : array) {
			vlm.addObject(o);
		}
		vlm.refresh();
		values.setEnabled(!vlm.isEmpty());
		values.repaint();
		this.repaint();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = list.getSelectedIndex();
		if (index > -1 && !anlm.isEmpty()) {
			goButton.setEnabled(true);
			target = anlm.getAttributeNameAt(index);
			populateValues();
		}
	}

	@Override
	public void eventOccurred(ONDEXEvent e) {
		System.out.println(e.getEventType().getCompleteMessage());
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
