package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;

public class DialogRelation extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	private static final String NODE = "node";

	private static final String RELATIONTYPE = "relationtype";

	private static final String EVIDENCETYPE = "evidencetype";

	private static final String ATTRIBUTE = "attribute";

	private static final String TAG = "tag";

	// generated
	private static final long serialVersionUID = -3303876325356985944L;

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	// current AbstractONDEXGraph
	private ONDEXJUNGGraph graph = null;

	// current ONDEXRelation
	private ONDEXRelation ar = null;

	// integer id field
	private JTextField id = null;

	// fromConcept input box
	private JComboBox from = new JComboBox(new String[] { "" });

	// toConcept input box
	private JComboBox to = new JComboBox(new String[] { "" });

	// relationType input box
	private JComboBox relationType = new JComboBox(new String[] { "" });

	// evidence input box
	private JComboBox evidences = new JComboBox(new String[] { "" });

	// context input box
	private JComboBox tags = new JComboBox(new String[] { "" });

	/**
	 * Constructs user input dialog to add or modify a Relation to a given
	 * graph.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 * @param ar
	 *            optional ONDEXRelation
	 */
	public DialogRelation(OVTK2Viewer viewer, ONDEXRelation ar) {
		super("Dialog.Relation.Title", "Properties16.gif");

		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();
		this.ar = ar;

		initProperties();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Relation.Apply", "Dialog.Relation.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Initialises properties fields.
	 * 
	 */
	private void initProperties() {
		if (ar != null) {
			id = new JTextField(String.valueOf(ar.getId()));
			id.setEnabled(false);

			from.addItem(ar.getFromConcept().getId());
			from.setSelectedIndex(1);
			from.setEnabled(false);

			to.addItem(ar.getToConcept().getId());
			to.setSelectedIndex(1);
			to.setEnabled(false);

			relationType.addItem(ar.getOfType().getId());
			relationType.setSelectedIndex(1);
			relationType.setEnabled(false);

			// get list of present evidence types
			String currentEvidenceTypes = null;
			for (EvidenceType et : ar.getEvidence()) {
				if (currentEvidenceTypes == null) {
					currentEvidenceTypes = et.getId();
				} else {
					currentEvidenceTypes = currentEvidenceTypes + "," + et.getId();
				}
			}

			initEvidenceTypes(null);
			Vector<String> evidenceTypes = new Vector<String>();
			evidenceTypes.add(currentEvidenceTypes);
			for (int i = 1; i < evidences.getItemCount(); i++) {
				evidenceTypes.add((String) evidences.getItemAt(i));
			}
			evidences.removeAllItems();
			for (String et : evidenceTypes) {
				evidences.addItem(et);
			}
			evidences.revalidate();
			evidences.setActionCommand(EVIDENCETYPE);
			evidences.addActionListener(this);

			initTags();

			String currentTagSet = null;
			Vector<String> currentContext = new Vector<String>();
			for (ONDEXConcept c : ar.getTags()) {
				String id = String.valueOf(c.getId());
				currentContext.add(id);
				if (currentTagSet == null) {
					currentTagSet = id;
				} else {
					currentTagSet = currentTagSet + "," + id;
				}
			}

			Vector<String> allTags = new Vector<String>();
			allTags.add(currentTagSet);
			allTags.addAll(currentContext);
			for (int i = 1; i < tags.getItemCount(); i++) {
				allTags.add((String) tags.getItemAt(i));
			}
			tags.removeAllItems();
			for (String c : allTags) {
				tags.addItem(c);
			}
			tags.revalidate();
			tags.setActionCommand(TAG);
			tags.addActionListener(this);

		} else {
			id = new JTextField(Config.language.getProperty("Dialog.Relation.AutoGenerated"));
			id.setEnabled(false);

			initNodes();

			from.setActionCommand(NODE);
			from.addActionListener(this);

			to.setActionCommand(NODE);
			to.addActionListener(this);

			initRelationType(null);
			relationType.setActionCommand(RELATIONTYPE);
			relationType.addActionListener(this);

			initEvidenceTypes(null);
			evidences.setActionCommand(EVIDENCETYPE);
			evidences.addActionListener(this);

			initTags();
			tags.setActionCommand(TAG);
			tags.addActionListener(this);
		}
	}

	/**
	 * Get node selection from graph.
	 * 
	 */
	private void initNodes() {
		from.removeAllItems();
		to.removeAllItems();

		from.addItem("");
		to.addItem("");

		from.addItem(Config.language.getProperty("Dialog.Relation.PickNew"));
		to.addItem(Config.language.getProperty("Dialog.Relation.PickNew"));

		for (ONDEXConcept c : viewer.getPickedNodes()) {
			from.addItem(String.valueOf(c.getId()));
			to.addItem(String.valueOf(c.getId()));
		}

		from.setSelectedIndex(0);
		to.setSelectedIndex(0);

		from.revalidate();
		to.revalidate();
	}

	/**
	 * Get node selection from graph for context.
	 * 
	 */
	private void initTags() {
		tags.removeAllItems();
		tags.addItem("");
		tags.addItem(Config.language.getProperty("Dialog.Relation.PickNew"));

		for (ONDEXConcept c : viewer.getPickedNodes()) {
			tags.addItem(String.valueOf(c.getId()));
		}

		tags.setSelectedIndex(0);
		tags.revalidate();
	}

	/**
	 * Loads available RelationTypes into ComboBox.
	 * 
	 */
	protected void initRelationType(String selected) {
		// get list of available relation types
		Vector<String> relationTypes = new Vector<String>();
		relationTypes.add("");
		relationTypes.add(Config.language.getProperty("Dialog.Relation.CreateRelationType"));
		RelationType[] sorted = graph.getMetaData().getRelationTypes().toArray(new RelationType[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (RelationType rt : sorted) {
			relationTypes.add(rt.getId());
		}
		relationType.removeAllItems();
		for (String rt : relationTypes) {
			relationType.addItem(rt);
		}
		if (selected != null)
			relationType.setSelectedItem(selected);
		relationType.revalidate();
	}

	/**
	 * Loads available EvidenceTypes into ComboBox.
	 * 
	 */
	protected void initEvidenceTypes(String selected) {
		// get list of available evidence types
		Vector<String> evidenceTypes = new Vector<String>();
		evidenceTypes.add("");
		evidenceTypes.add(Config.language.getProperty("Dialog.Relation.CreateEvidenceType"));
		EvidenceType[] sorted = graph.getMetaData().getEvidenceTypes().toArray(new EvidenceType[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (EvidenceType et : sorted) {
			evidenceTypes.add(et.getId());
		}
		evidences.removeAllItems();
		for (String et : evidenceTypes) {
			evidences.addItem(et);
		}
		if (selected != null)
			evidences.setSelectedItem(selected);
		evidences.revalidate();
	}

	/**
	 * Creates the properties panel for relations.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Relation.Relation"));
		properties.setBorder(propertiesBorder);

		// relation id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.Relation.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(id);

		// fromConcept
		JLabel fromLabel = new JLabel(Config.language.getProperty("Dialog.Relation.FromConcept"));
		properties.add(fromLabel);
		from.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		from.setBackground(this.getRequiredColor());
		properties.add(from);

		// toConcept
		JLabel toLabel = new JLabel(Config.language.getProperty("Dialog.Relation.ToConcept"));
		properties.add(toLabel);
		to.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		to.setBackground(this.getRequiredColor());
		properties.add(to);

		// ofTypeSet
		JLabel rtLabel = new JLabel(Config.language.getProperty("Dialog.Relation.OfType"));
		properties.add(rtLabel);
		relationType.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		relationType.setBackground(this.getRequiredColor());
		properties.add(relationType);

		// relation evidence types
		JLabel etLabel = new JLabel(Config.language.getProperty("Dialog.Relation.Evidences"));
		properties.add(etLabel);
		evidences.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		evidences.setBackground(this.getRequiredColor());
		properties.add(evidences);

		// context list
		JLabel contextLabel = new JLabel(Config.language.getProperty("Dialog.Relation.Tag"));
		properties.add(contextLabel);
		tags.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(tags);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(fromLabel).addComponent(toLabel).addComponent(rtLabel).addComponent(etLabel).addComponent(contextLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(from, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(to, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(relationType, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(evidences, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(tags, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fromLabel).addComponent(from)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(toLabel).addComponent(to)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(rtLabel).addComponent(relationType)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(etLabel).addComponent(evidences)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(contextLabel).addComponent(tags)));

		// show relation attribute
		JButton gdsButton = new JButton(Config.language.getProperty("Dialog.Relation.RelationAttribute"));
		gdsButton.setActionCommand(ATTRIBUTE);
		gdsButton.addActionListener(this);
		if (ar == null)
			gdsButton.setEnabled(false);

		// brining it all together
		JPanel upper = new JPanel(new BorderLayout());
		upper.add(properties, BorderLayout.CENTER);
		upper.add(gdsButton, BorderLayout.SOUTH);

		return upper;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (from.getSelectedItem().equals("") || from.getSelectedItem().equals(Config.language.getProperty("Dialog.Relation.PickNew"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Relation.InvalidFrom"), Config.language.getProperty("Dialog.Relation.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (to.getSelectedItem().equals("") || to.getSelectedItem().equals(Config.language.getProperty("Dialog.Relation.PickNew"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Relation.InvalidTo"), Config.language.getProperty("Dialog.Relation.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (relationType.getSelectedItem().equals("") || relationType.getSelectedItem().equals(Config.language.getProperty("Dialog.Relation.CreateRelationType"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Relation.InvalidRelationType"), Config.language.getProperty("Dialog.Relation.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (evidences.getSelectedItem().equals("") || evidences.getSelectedItem().equals(Config.language.getProperty("Dialog.Relation.CreateEvidenceType"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Relation.InvalidEvidenceType"), Config.language.getProperty("Dialog.Relation.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new relation
		if (cmd.equals(APPLY)) {
			if (ar == null && validateEntry()) {
				ONDEXConcept fromConcept = graph.getConcept(Integer.parseInt(from.getSelectedItem().toString()));
				ONDEXConcept toConcept = graph.getConcept(Integer.parseInt(to.getSelectedItem().toString()));
				RelationType ofType = graph.getMetaData().getRelationType((String) relationType.getSelectedItem());
				// get Evidences as Collection
				String[] ets = ((String) evidences.getSelectedItem()).split(",");
				Vector<EvidenceType> relationETS = new Vector<EvidenceType>();
				for (int i = 0; i < ets.length; i++) {
					relationETS.add(graph.getMetaData().getEvidenceType(ets[i]));
				}
				ar = graph.createRelation(fromConcept, toConcept, ofType, relationETS);

				if (!tags.getSelectedItem().equals("")) {
					String[] ids = ((String) tags.getSelectedItem()).split(",");
					for (String id : ids) {
						ONDEXConcept co = graph.getConcept(Integer.parseInt(id));
						ar.addTag(co);
					}
				}
				graph.setVisibility(ar, true);

				// hide dialog
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}

				// update label and colour and notify model of change
				viewer.updateViewer(ar);
				viewer.getMetaGraph().updateMetaData();

				// OVTK-202
				String title = viewer.getTitle();
				if (!title.endsWith(" (modified)")) {
					viewer.setTitle(title + " (modified)");
					viewer.setName(viewer.getName() + " (modified)");
				}
			}

			else if (ar != null) {
				// modified evidences
				Set<String> original = new HashSet<String>();
				for (EvidenceType et : ar.getEvidence()) {
					original.add(et.getId());
				}
				Set<String> modified = new HashSet<String>(Arrays.asList(((String) evidences.getSelectedItem()).split(",")));
				Set<String> delete = new HashSet<String>(original);
				delete.removeAll(modified);
				Set<String> add = new HashSet<String>(modified);
				add.removeAll(original);
				for (String aDelete : delete) {
					ar.removeEvidenceType(graph.getMetaData().getEvidenceType(aDelete));
				}
				for (String anAdd : add) {
					ar.addEvidenceType(graph.getMetaData().getEvidenceType(anAdd));
				}
				// modified tags
				Set<Integer> originalTags = new HashSet<Integer>();
				for (ONDEXConcept c : ar.getTags()) {
					originalTags.add(c.getId());
				}
				Set<Integer> modifiedTags = new HashSet<Integer>();
				if (tags.getItemAt(0) != null && !tags.getItemAt(0).equals("")) {
					for (String id : ((String) tags.getSelectedItem()).split(",")) {
						modifiedTags.add(Integer.parseInt(id));
					}
				}
				Set<Integer> deleteTags = new HashSet<Integer>(originalTags);
				deleteTags.removeAll(modifiedTags);
				Set<Integer> addTags = new HashSet<Integer>(modifiedTags);
				addTags.removeAll(originalTags);
				for (Integer aDeleteTag : deleteTags) {
					ar.removeTag(graph.getConcept(aDeleteTag));
				}
				for (Integer anAddTag : addTags) {
					ar.addTag(graph.getConcept(anAddTag));
				}

				// hide dialog
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}

				// update color and notify model of change
				viewer.updateViewer(ar);
			}
		}

		// cancel dialog
		else if (cmd.equals(CANCEL)) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}

		// concept node selection
		else if (cmd.equals(NODE)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Relation.PickNew"))) {
				try {
					this.setIcon(true);
					if (viewer.isIcon()) {
						viewer.setIcon(false);
					}
					viewer.toFront();
				} catch (PropertyVetoException pve) {
					System.err.println(pve.getMessage());
				}
			}
		}

		// new relation type set create
		else if (cmd.equals(RELATIONTYPE)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Relation.CreateRelationType"))) {
				DialogRelationType dialog = new DialogRelationType(graph, this);
				displayCentered(dialog);
			}
		}

		// add new EvidenceType or modify list of Evidences
		else if (cmd.equals(EVIDENCETYPE)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Relation.CreateEvidenceType"))) {
				DialogEvidenceType dialog = new DialogEvidenceType(graph, this);
				displayCentered(dialog);
			} else if (selection != null) {
				String[] content = new String[cb.getItemCount()];
				for (int i = 0; i < content.length; i++) {
					content[i] = (String) cb.getItemAt(i);
				}
				int sel = cb.getSelectedIndex();
				if (content[0].indexOf(content[sel]) > -1) {
					content[0] = content[0].replaceAll(content[sel] + ",", "");
					content[0] = content[0].replaceAll("," + content[sel], "");
					content[0] = content[0].replaceAll(content[sel], "");
				} else {
					if (content[0].equalsIgnoreCase(""))
						content[0] = content[sel];
					else
						content[0] = content[0] + "," + content[sel];
				}
				cb.removeAllItems();
				for (int i = 0; i < content.length; i++) {
					cb.addItem(content[i]);
				}
				cb.setSelectedIndex(0);
				cb.revalidate();
			}
		}

		// add new context or modify list of context
		else if (cmd.equals(TAG)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Relation.PickNew"))) {
				try {
					this.setIcon(true);
					if (viewer.isIcon()) {
						viewer.setIcon(false);
					}
					viewer.toFront();
				} catch (PropertyVetoException pve) {
					System.err.println(pve.getMessage());
				}
			} else if (selection != null) {
				String[] content = new String[cb.getItemCount()];
				for (int i = 0; i < content.length; i++) {
					content[i] = (String) cb.getItemAt(i);
				}
				int sel = cb.getSelectedIndex();
				if (content[0] != null && content[0].indexOf(content[sel]) > -1) {
					content[0] = content[0].replaceAll(content[sel] + ",", "");
					content[0] = content[0].replaceAll("," + content[sel], "");
					content[0] = content[0].replaceAll(content[sel], "");
				} else {
					if (content[0] == null || content[0].equalsIgnoreCase(""))
						content[0] = content[sel];
					else
						content[0] = content[0] + "," + content[sel];
				}
				cb.removeAllItems();
				for (int i = 0; i < content.length; i++) {
					cb.addItem(content[i]);
				}
				cb.setSelectedIndex(0);
				cb.revalidate();
			}
		}

		// view/edit relation Attribute
		else if (cmd.equals(ATTRIBUTE)) {
			DialogAttribute dialog = new DialogAttribute(graph, ar);
			displayCentered(dialog);
		}
	}

	/**
	 * Init new node selection on deiconify.
	 */
	@Override
	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		super.internalFrameDeiconified(arg0);
		initNodes();
		initTags();
	}

}
