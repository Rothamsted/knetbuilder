package net.sourceforge.ondex.ovtk2.annotator.degreespecificity;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.specificity.ArgumentNames;
import net.sourceforge.ondex.export.specificity.Export;
import net.sourceforge.ondex.export.specificity.ThresholdArgumentDefinition;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

/**
 * @author hindlem
 */
public class DegreeSpecificityAnnotator extends OVTK2Annotator implements
		ActionListener, ListSelectionListener {

	private static String CHECK_ACTION = "CHECK";
	private static String FINISH_ACTION = "FINISH";

	// displays specificity type
	private JList listFields;
	private JFormattedTextField value;
	private JTextField specificityAttributeName;
	private JTextField degreeAttributeName;
	private JList source;
	private JList target;
	private JComboBox relationType;
	private JButton goButton;

	private JCheckBox addToConcepts;
	private JCheckBox addToRelations;
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * @param viewer
	 */
	public DegreeSpecificityAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		setUpGUI();
		setMinimumSize(new Dimension(400, 550));
		this.setPreferredSize(new Dimension(400, 600));
	}

	private void setUpGUI() {
		setLayout(new SpringLayout());

		JLabel sourceLabel = new JLabel("ConceptClass source");
		JPanel sourcePanel = new JPanel();
		sourcePanel.setToolTipText(ArgumentNames.SOURCE_CC_ARG_DESC);
		source = new JList(getConceptClassNames());
		source.setSelectedIndex(-1);
		source.addListSelectionListener(this);
		source.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// row1
		sourcePanel.add(sourceLabel);
		sourcePanel.add(new JScrollPane(source));
		add(sourcePanel);

		JPanel targetPanel = new JPanel();
		targetPanel.setToolTipText(ArgumentNames.TARGET_CC_ARG_DESC);
		JLabel targetLabel = new JLabel("ConceptClass target");
		target = new JList(getConceptClassNames());
		target.setSelectedIndex(-1);
		target.addListSelectionListener(this);
		source.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// row2
		targetPanel.add(targetLabel);
		targetPanel.add(new JScrollPane(target));
		add(targetPanel);

		JPanel rtPanel = new JPanel();
		JLabel rtLabel = new JLabel("RelationType target");
		rtPanel.setToolTipText(ArgumentNames.RELATION_TYPE_ARG_DESC);
		relationType = new JComboBox(getRelationTypeNames());
		relationType.setSelectedIndex(-1);
		relationType.setActionCommand(CHECK_ACTION);
		relationType.addActionListener(this);

		JPanel attPanel = new JPanel();
		specificityAttributeName = new JTextField();
		specificityAttributeName
				.setToolTipText(ArgumentNames.SPEC_ATT_TYPE_ARG_DESC);
		specificityAttributeName.setColumns(6);
		specificityAttributeName.setActionCommand(CHECK_ACTION);
		specificityAttributeName.addActionListener(this);
		specificityAttributeName.setText("Specificity");

		// row3
		attPanel.add(new JLabel("Specificity AttributeName"));
		attPanel.add(specificityAttributeName);
		add(attPanel);

		JPanel att2Panel = new JPanel();
		degreeAttributeName = new JTextField();
		degreeAttributeName
				.setToolTipText(ArgumentNames.DEGREE_ATT_TYPE_ARG_DESC);
		degreeAttributeName.setColumns(6);
		degreeAttributeName.setActionCommand(CHECK_ACTION);
		degreeAttributeName.addActionListener(this);
		degreeAttributeName.setText("Degree");

		// row4
		att2Panel.add(new JLabel("Degree AttributeName"));
		att2Panel.add(degreeAttributeName);
		add(att2Panel);

		// row5
		rtPanel.add(rtLabel);
		rtPanel.add(relationType);
		add(rtPanel);

		String[] types = ThresholdArgumentDefinition.getTypes();
		Arrays.sort(types);
		JPanel fieldsPanel = new JPanel(new SpringLayout());
		listFields = new JList(types);
		listFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFields.addListSelectionListener(this);

		JTextArea description = new JTextArea();
		description
				.setText("Select the type of specificity measure for which specificity maximum degree range for normalization is calculated. For example \"count\" with a value of \"3\" will make all relations that are part of a degree >=3 non specific i.e. 0 and the specificity measure will be normalized across 1-0 on the range 1-3; ");
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.setEditable(false);

		// row 6
		fieldsPanel.add(new JScrollPane(description));
		fieldsPanel.add(new JScrollPane(listFields));
		SpringUtilities.makeCompactGrid(fieldsPanel, 1, 2, 0, 0, 0, 0);
		add(fieldsPanel);

		JPanel valuePanel = new JPanel();
		JLabel valueLabel = new JLabel("Value for specificity measure");
		value = new JFormattedTextField(valueLabel);
		value.setActionCommand(CHECK_ACTION);
		value.addActionListener(this);
		value.setValue(new Double(0));
		value.setColumns(6);

		// row 7
		valuePanel.add(valueLabel);
		valuePanel.add(value);
		add(valuePanel);

		TitledBorder titled = BorderFactory.createTitledBorder("Annotation");
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.setBorder(titled);

		JPanel checkBoxPanel1 = new JPanel();
		addToConcepts = new JCheckBox("Add measures to Concept");
		addToConcepts.setSelected(true);
		checkBoxPanel1.add(addToConcepts);

		JPanel checkBoxPanel2 = new JPanel();
		addToRelations = new JCheckBox("Add measures to Relations");
		addToRelations.setSelected(false);
		checkBoxPanel2.add(addToRelations);

		checkBoxPanel.add(Box.createHorizontalGlue());
		checkBoxPanel.add(checkBoxPanel1);
		checkBoxPanel.add(Box.createHorizontalGlue());
		checkBoxPanel.add(checkBoxPanel2);

		// row 8
		add(checkBoxPanel);

		JPanel buttonPan = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		goButton = new JButton("Apply");
		goButton.setActionCommand(FINISH_ACTION);
		goButton.addActionListener(this);
		goButton.setEnabled(false);
		buttonPan.add(goButton);

		// row 8
		add(buttonPan);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	private String[] getConceptClassNames() {

		// only add non-empty concept classes
		Set<ConceptClass> ccs = new HashSet<ConceptClass>();
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			if (graph.getConceptsOfConceptClass(cc).size() > 0) {
				ccs.add(cc);
			}
		}
		String[] names = new String[ccs.size()];
		int i = 0;
		for (ConceptClass cc : ccs) {
			names[i] = cc.getId();
			i++;
		}
		Arrays.sort(names);
		return names;
	}

	private String[] getRelationTypeNames() {
		
		// only add non-empty relation types
		Set<RelationType> rts = new HashSet<RelationType>();
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {
			if (graph.getRelationsOfRelationType(rt).size() > 0) {
				rts.add(rt);
			}
		}
		String[] names = new String[rts.size()];
		int i = 0;
		for (RelationType rt : rts) {
			names[i] = rt.getId();
			i++;
		}
		Arrays.sort(names);
		return names;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.DegreeSpecificity");
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (CHECK_ACTION.equals(e.getActionCommand())) {
			check();
		} else if (FINISH_ACTION.equals(e.getActionCommand())) {

			Object[] sourceCC = source.getSelectedValues();
			Object[] targetCC = target.getSelectedValues();
			String rt = (String) relationType.getSelectedItem();
			String att = specificityAttributeName.getText().trim();
			String deg_att = degreeAttributeName.getText().trim();
			String valueType = (String) listFields.getSelectedValue();
			Number valueNum = (Number) value.getValue();
			Boolean addToConcept = addToConcepts.isSelected();
			Boolean addToRelation = addToRelations.isSelected();

			Export statistics = new Export();
			try {
				ONDEXPluginArguments sa = new ONDEXPluginArguments(
						statistics.getArgumentDefinitions());
				for (Object cc : targetCC) {
					sa.addOption(ArgumentNames.TARGET_CC_ARG, cc);
				}
				for (Object cc : sourceCC) {
					sa.setOption(ArgumentNames.SOURCE_CC_ARG, cc);
				}
				sa.setOption(ArgumentNames.RELATION_TYPE_ARG, rt);
				sa.setOption(ArgumentNames.SPEC_ATT_TYPE_ARG, att);
				sa.setOption(ArgumentNames.THRESHOLD_ARG, valueNum);
				sa.setOption(ArgumentNames.THRESHOLD_TYPE_ARG, valueType);
				sa.setOption(ArgumentNames.DEGREE_ATT_TYPE_ARG, deg_att);
				sa.setOption(ArgumentNames.ADD_2_CONCEPT_ARG, addToConcept);
				sa.setOption(ArgumentNames.ADD_2_RELATION_ARG, addToRelation);

				statistics.setArguments(sa);
				statistics.setONDEXGraph(viewer.getONDEXJUNGGraph());

				statistics.start();
			} catch (Exception e1) {
				ErrorDialog.show(e1);
			}
		}

		used = true;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		check();
	}

	private void check() {
		Object[] sourceCC = source.getSelectedValues();
		Object[] targetCC = target.getSelectedValues();

		String rt = (String) relationType.getSelectedItem();
		String spec_att = specificityAttributeName.getText().trim();
		String deg_att = degreeAttributeName.getText().trim();
		String valueType = (String) listFields.getSelectedValue();
		Number valueNum = (Number) value.getValue();

		if (valueType != null && valueType.equalsIgnoreCase("all")) {
			valueNum = 0d;
			value.setEnabled(false);
		} else {
			value.setEnabled(true);
		}

		if (sourceCC != null && sourceCC.length > 0 && targetCC != null
				&& targetCC.length > 0 && rt != null && valueType != null
				&& valueNum != null && spec_att != null
				&& spec_att.length() > 0 && deg_att != null
				&& deg_att.length() > 0) {
			goButton.setEnabled(true);
		} else {
			goButton.setEnabled(false);
		}

	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
