package net.sourceforge.ondex.ovtk2.annotator.attributeweight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.transformer.attributeweight.ArgumentNames;
import net.sourceforge.ondex.transformer.attributeweight.Transformer;

/**
 * Annotator to calculate new Attribute value from numerical averages.
 * 
 * @author taubertj
 * @version 22.10.2008
 */
public class AttributeWeightAnnotator extends OVTK2Annotator implements
		ActionListener {

	private static final long serialVersionUID = 3743904012085379846L;

	private static final String ANNOTATE = "ANNOTATE";

	private static final String SELECTION = "SELECTION";

	// stores weights of each attribute name
	private Map<String, Double> weights = new Hashtable<String, Double>();

	// stores inverse flag of each attribute name
	private Map<String, Boolean> inverse = new Hashtable<String, Boolean>();

	// the selection box
	private JComboBox choices = null;

	// the important button
	private JButton goButton = null;

	// which relation type to use
	private RelationType relationType = null;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Constructor extracts current attribute names from meta data and checks
	 * for their relevance in the graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer which has current visualisation
	 */
	public AttributeWeightAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		updateRelationTypes();
	}

	private void updateRelationTypes() {

		this.removeAll();

		int selection = 0;
		if (choices != null) {
			selection = choices.getSelectedIndex();
		}

		// present user with selection of relation types
		Set<RelationType> possibilites = new HashSet<RelationType>();
		for (RelationType rt : graph.getMetaData().getRelationTypes()) {
			if (graph.getRelationsOfRelationType(rt).size() > 0) {
				possibilites.add(rt);
			}
		}

		RelationType[] sorted = possibilites.toArray(new RelationType[0]);
		Arrays.sort(sorted);

		setLayout(new SpringLayout());

		add(new JLabel("Select a RelationType:"));

		choices = new JComboBox(sorted);
		choices.setSelectedIndex(selection);
		choices.addActionListener(this);
		choices.setActionCommand(SELECTION);
		add(choices);

		if (selection > -1 && selection < sorted.length)
			relationType = sorted[selection];
		else {
			add(new JLabel("No valid RelationType available."));
			relationType = null;
		}

		weights.clear();
		inverse.clear();

		if (relationType != null) {
			// The magic button
			goButton = new JButton("Annotate Graph");
			goButton.setActionCommand(ANNOTATE);
			goButton.addActionListener(this);

			// get attribute names from graph
			for (AttributeName an : graph.getMetaData().getAttributeNames()) {
				if (an.getId().equals("weighted")
						|| AppearanceSynchronizer.attr.contains(an.getId()))
					continue;

				Set<ONDEXRelation> relationsOfRT = graph
						.getRelationsOfRelationType(relationType);
				Set<ONDEXRelation> relationsOfAN = graph
						.getRelationsOfAttributeName(an);
				Set<ONDEXRelation> relations = BitSetFunctions.and(
						relationsOfRT, relationsOfAN);
				if (relations != null) {
					// check relations exists on this AttributeName
					Class<?> cl = an.getDataType();
					if (relations.size() > 0
							&& cl.getSuperclass().equals(Number.class)) {
						weights.put(an.getId(), Double.valueOf(0.0));
						inverse.put(an.getId(), Boolean.FALSE);
					}
				}
			}

			// check if list is populated
			if (weights.size() == 0) {
				add(new JLabel("No Number Attributes found."));
			} else {
				add(new JLabel("Enter values for each AttributeName"));
				for (String an : weights.keySet()) {
					JPanel row = new JPanel(new BorderLayout());
					// name of attribute
					row.add(BorderLayout.CENTER, new JLabel(an));

					JPanel panel = new JPanel(new FlowLayout());

					// field that automatically updates weights
					JTextField field = new JTextField(10);
					field.setText(weights.get(an).toString());
					field.setName(an);
					field.addCaretListener(new CaretListener() {

						@Override
						public void caretUpdate(CaretEvent arg0) {
							JTextField t = (JTextField) arg0.getSource();
							String s = t.getText();
							try {
								double v = Double.parseDouble(s);
								weights.put(t.getName(), Double.valueOf(v));
								t.setBackground(Color.WHITE);
							} catch (NumberFormatException nfe) {
								t.setBackground(Color.RED);
							}
						}
					});

					JCheckBox box = new JCheckBox();
					box.setSelected(inverse.get(an));
					box.setName(an);
					box.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBox b = (JCheckBox) e.getSource();
							boolean s = b.isSelected();
							inverse.put(b.getName(), Boolean.valueOf(s));
						}
					});

					panel.add(field);
					panel.add(box);

					// add to annotator panel
					row.add(BorderLayout.EAST, panel);
					add(row);
				}
				add(goButton);
			}
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);

		this.revalidate();
	}

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.AttributeWeight");
	}

	/**
	 * Associated with go button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (ANNOTATE.equals(e.getActionCommand())) {
			try {
				callAnnotator();
			} catch (InvalidPluginArgumentException e1) {
				ErrorDialog.show(e1);
			}
			used = true;
		} else if (SELECTION.equals(e.getActionCommand())) {
			updateRelationTypes();
		} else {
			System.err.println("Unknown action command " + e.getActionCommand()
					+ " in " + this.getClass());
		}
	}

	/**
	 * Calls backend annotator.
	 */
	private void callAnnotator() throws InvalidPluginArgumentException {

		// new instance of transformer and set arguments
		Transformer trans = new Transformer();

		// construct transformer arguments
		ONDEXPluginArguments ta = new ONDEXPluginArguments(
				trans.getArgumentDefinitions());
		StringBuffer gds = new StringBuffer();
		StringBuffer w = new StringBuffer();
		StringBuffer i = new StringBuffer();
		for (String s : weights.keySet()) {
			gds.append(s);
			gds.append(",");
			w.append(weights.get(s));
			w.append(",");
			i.append(inverse.get(s));
			i.append(",");
		}
		gds.deleteCharAt(gds.length() - 1);
		w.deleteCharAt(w.length() - 1);
		i.deleteCharAt(i.length() - 1);
		ta.addOption(ArgumentNames.RELATION_TYPE_ARG, relationType.getId());
		ta.addOption(ArgumentNames.ATTRIBUTE_ARG, gds.toString());
		ta.addOption(ArgumentNames.WEIGHTS_ARG, w.toString());
		ta.addOption(ArgumentNames.INVERSE_ARG, i.toString());

		trans.setONDEXGraph(graph);
		trans.setArguments(ta);

		// execute transformer
		trans.start();

	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
