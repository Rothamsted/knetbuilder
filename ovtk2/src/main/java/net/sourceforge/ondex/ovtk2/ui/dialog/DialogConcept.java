package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
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

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Concept properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogConcept extends OVTK2Dialog {

	/**
	 * Action commands for user inputs
	 */
	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	private static final String CONCEPTCLASS = "conceptclass";

	private static final String DATASOURCE = "datasource";

	private static final String EVIDENCETYPE = "evidencetype";

	private static final String TAG = "tag";

	private static final String CONCEPTNAME = "conceptname";

	private static final String CONCEPTACCESSION = "conceptaccession";

	private static final String ATTRIBUTE = "attribute";

	// generated
	private static final long serialVersionUID = -4144320642041988307L;

	// current OVTK2Viewer
	private OVTK2Viewer viewer = null;

	// current AbstractONDEXGraph
	private ONDEXJUNGGraph graph = null;

	// current ONDEXConcept
	private ONDEXConcept concept = null;

	// integer id field
	private JTextField id = null;

	// pid input field
	private JTextField pid = null;

	// annotation input field
	private JTextField annotation = null;

	// description input field
	private JTextField description = null;

	// elementOf input box
	private JComboBox dataSource = new JComboBox(new String[] { "" });

	// ofType input box
	private JComboBox conceptClass = new JComboBox(new String[] { "" });

	// evidence input box
	private JComboBox evidences = new JComboBox(new String[] { "" });

	// context input box
	private JComboBox tags = new JComboBox(new String[] { "" });

	/**
	 * Constructs user input dialog to add or modify a Concept to a given graph.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param ac
	 *            optional ONDEXConcept
	 */
	public DialogConcept(OVTK2Viewer viewer, ONDEXConcept ac) {
		super("Dialog.Concept.Title", "Properties16.gif");

		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();
		this.concept = ac;

		initProperties();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Concept.Apply", "Dialog.Concept.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Initialises properties fields.
	 * 
	 */
	private void initProperties() {
		if (concept != null) {
			id = new JTextField(String.valueOf(concept.getId()));
			id.setEnabled(false);
			pid = new JTextField(concept.getPID());
			pid.setEnabled(false);

			annotation = new JTextField(concept.getAnnotation());
			description = new JTextField(concept.getDescription());

			dataSource.addItem(concept.getElementOf().getId());
			dataSource.setSelectedIndex(1);
			dataSource.setEnabled(false);

			conceptClass.addItem(concept.getOfType().getId());
			conceptClass.setSelectedIndex(1);
			conceptClass.setEnabled(false);

			// get list of present ets
			String currentEts = null;
			for (EvidenceType et : concept.getEvidence()) {
				if (currentEts == null) {
					currentEts = et.getId();
				} else {
					currentEts = currentEts + "," + et.getId();
				}
			}

			initEvidenceType(null);
			Vector<String> ets = new Vector<String>();
			ets.add(currentEts);
			for (int i = 1; i < evidences.getItemCount(); i++) {
				ets.add((String) evidences.getItemAt(i));
			}
			evidences.removeAllItems();
			for (String et : ets) {
				evidences.addItem(et);
			}
			evidences.revalidate();
			evidences.setActionCommand(EVIDENCETYPE);
			evidences.addActionListener(this);

			initTag();

			String currentTagSet = null;
			Vector<String> currentTags = new Vector<String>();
			for (ONDEXConcept c : concept.getTags()) {
				String id = String.valueOf(c.getId());
				currentTags.add(id);
				if (currentTagSet == null) {
					currentTagSet = id;
				} else {
					currentTagSet = currentTagSet + "," + id;
				}
			}
			Vector<String> allTags = new Vector<String>();
			allTags.add(currentTagSet);
			allTags.addAll(currentTags);
			for (int i = 1; i < tags.getItemCount(); i++) {
				if (!allTags.contains(tags.getItemAt(i)))
					allTags.add((String) tags.getItemAt(i));
			}
			tags.removeAllItems();
			for (int i = 0; i < allTags.size(); i++) {
				tags.addItem(allTags.get(i));
			}
			tags.revalidate();
			tags.setActionCommand(TAG);
			tags.addActionListener(this);
		} else {
			id = new JTextField(Config.language.getProperty("Dialog.Concept.AutoGenerated"));
			id.setEnabled(false);
			pid = new JTextField();

			annotation = new JTextField();
			description = new JTextField();

			initDataSource(null);
			dataSource.setActionCommand(DATASOURCE);
			dataSource.addActionListener(this);

			initConceptClass(null);
			conceptClass.setActionCommand(CONCEPTCLASS);
			conceptClass.addActionListener(this);

			initEvidenceType(null);
			evidences.setActionCommand(EVIDENCETYPE);
			evidences.addActionListener(this);

			initTag();
			tags.setActionCommand(TAG);
			tags.addActionListener(this);
		}
	}

	/**
	 * Get node selection from graph for tag.
	 * 
	 */
	private void initTag() {
		tags.removeAllItems();
		tags.addItem("");
		tags.addItem(Config.language.getProperty("Dialog.Concept.PickNew"));
		for (ONDEXConcept c : viewer.getPickedNodes()) {
			tags.addItem(String.valueOf(c.getId()));
		}
		tags.setSelectedIndex(0);
		tags.revalidate();
	}

	/**
	 * Loads available DataSources into ComboBox.
	 * 
	 */
	protected void initDataSource(String selected) {
		// get list of available data sources
		Vector<String> dataSources = new Vector<String>();
		dataSources.add("");
		dataSources.add(Config.language.getProperty("Dialog.Concept.CreateDataSource"));
		DataSource[] sorted = graph.getMetaData().getDataSources().toArray(new DataSource[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (DataSource dataSource : sorted) {
			dataSources.add(dataSource.getId());
		}
		dataSource.removeAllItems();
		for (String ds : dataSources) {
			dataSource.addItem(ds);
		}
		if (selected != null)
			dataSource.setSelectedItem(selected);
		dataSource.revalidate();
	}

	/**
	 * Loads available ConceptClasses into ComboBox.
	 * 
	 */
	protected void initConceptClass(String selected) {
		// get list of available concept classes
		Vector<String> conceptClasses = new Vector<String>();
		conceptClasses.add("");
		conceptClasses.add(Config.language.getProperty("Dialog.Concept.CreateConceptClass"));
		// sort meta data concept classes
		ConceptClass[] sorted = graph.getMetaData().getConceptClasses().toArray(new ConceptClass[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (ConceptClass cc : sorted) {
			conceptClasses.add(cc.getId());
		}
		conceptClass.removeAllItems();
		for (String cc : conceptClasses) {
			conceptClass.addItem(cc);
		}
		if (selected != null)
			conceptClass.setSelectedItem(selected);
		conceptClass.revalidate();
	}

	/**
	 * Loads available EvidenceTypes into ComboBox.
	 * 
	 */
	protected void initEvidenceType(String selected) {
		// get list of available evidence types
		Vector<String> evidenceTypes = new Vector<String>();
		evidenceTypes.add("");
		evidenceTypes.add(Config.language.getProperty("Dialog.Concept.CreateEvidenceType"));
		// sort meta data evidence types
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
	 * Creates the properties panel for concepts.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Concept.Concept"));
		properties.setBorder(propertiesBorder);

		// concept id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.Concept.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// concept pid
		JLabel pidLabel = new JLabel(Config.language.getProperty("Dialog.Concept.PID"));
		properties.add(pidLabel);
		pid.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(pid);

		// concept annotation
		JLabel annotationLabel = new JLabel(Config.language.getProperty("Dialog.Concept.Annotation"));
		properties.add(annotationLabel);
		annotation.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(annotation);

		// concept description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.Concept.Description"));
		properties.add(descriptionLabel);
		description.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(description);

		// concept elementOf
		JLabel cvLabel = new JLabel(Config.language.getProperty("Dialog.Concept.ElementOf"));
		properties.add(cvLabel);
		dataSource.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		dataSource.setBackground(this.getRequiredColor());
		properties.add(dataSource);

		// concept ofType
		JLabel ccLabel = new JLabel(Config.language.getProperty("Dialog.Concept.OfType"));
		properties.add(ccLabel);
		conceptClass.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		conceptClass.setBackground(this.getRequiredColor());
		properties.add(conceptClass);

		// concept evidence types
		JLabel etLabel = new JLabel(Config.language.getProperty("Dialog.Concept.Evidences"));
		properties.add(etLabel);
		evidences.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		evidences.setBackground(this.getRequiredColor());
		properties.add(evidences);

		// context list
		JLabel contextLabel = new JLabel(Config.language.getProperty("Dialog.Concept.Tag"));
		properties.add(contextLabel);
		tags.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(tags);

		// keep buttons in separate panel
		JPanel buttons = new JPanel(new GridLayout(3, 1));

		// show concept names
		JButton cnButton = new JButton(Config.language.getProperty("Dialog.Concept.ConceptNames"));
		cnButton.setActionCommand(CONCEPTNAME);
		cnButton.addActionListener(this);
		if (concept == null)
			cnButton.setEnabled(false);
		buttons.add(cnButton);

		// show concept accessions
		JButton caButton = new JButton(Config.language.getProperty("Dialog.Concept.ConceptAccessions"));
		caButton.setActionCommand(CONCEPTACCESSION);
		caButton.addActionListener(this);
		if (concept == null)
			caButton.setEnabled(false);
		buttons.add(caButton);

		// show concept attribute
		JButton attributeButton = new JButton(Config.language.getProperty("Dialog.Concept.ConceptAttributes"));
		attributeButton.setActionCommand(ATTRIBUTE);
		attributeButton.addActionListener(this);
		if (concept == null)
			attributeButton.setEnabled(false);
		buttons.add(attributeButton);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(pidLabel).addComponent(annotationLabel).addComponent(descriptionLabel).addComponent(cvLabel).addComponent(ccLabel).addComponent(etLabel).addComponent(contextLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(pid, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(annotation, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(description, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(dataSource, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(conceptClass, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(evidences, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(tags, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(pidLabel).addComponent(pid)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(annotationLabel).addComponent(annotation)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(descriptionLabel).addComponent(description)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(cvLabel).addComponent(dataSource)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(ccLabel).addComponent(conceptClass)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(etLabel).addComponent(evidences)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(contextLabel).addComponent(tags)));

		// bringing it all together
		JPanel upper = new JPanel(new BorderLayout());
		upper.add(properties, BorderLayout.CENTER);
		upper.add(buttons, BorderLayout.SOUTH);

		return upper;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (dataSource.getSelectedItem().equals("") || dataSource.getSelectedItem().equals(Config.language.getProperty("Dialog.Concept.CreateDataSource"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Concept.InvalidDataSource"), Config.language.getProperty("Dialog.Concept.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (conceptClass.getSelectedItem().equals("") || conceptClass.getSelectedItem().equals(Config.language.getProperty("Dialog.Concept.CreateConceptClass"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Concept.InvalidConceptClass"), Config.language.getProperty("Dialog.Concept.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (evidences.getSelectedItem().equals("") || evidences.getSelectedItem().equals(Config.language.getProperty("Dialog.Concept.CreateEvidenceType"))) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Concept.InvalidEvidenceType"), Config.language.getProperty("Dialog.Concept.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create or modify concept
		if (cmd.equals(APPLY)) {
			// notify model of change
			VisualizationViewer<ONDEXConcept, ONDEXRelation> viz = viewer.getVisualizationViewer();

			// new concept
			if (validateEntry() && concept == null) {
				// get DataSource
				DataSource conceptDataSource = graph.getMetaData().getDataSource((String) dataSource.getSelectedItem());
				// get ConceptClass
				ConceptClass conceptConceptClass = graph.getMetaData().getConceptClass((String) conceptClass.getSelectedItem());
				// get Evidences as Collection
				String[] ets = ((String) evidences.getSelectedItem()).split(",");
				Vector<EvidenceType> conceptEvidence = new Vector<EvidenceType>();
				for (int i = 0; i < ets.length; i++) {
					conceptEvidence.add(graph.getMetaData().getEvidenceType(ets[i]));
				}
				// create new concept
				concept = graph.createConcept(pid.getText(), annotation.getText(), description.getText(), conceptDataSource, conceptConceptClass, conceptEvidence);
				if (!tags.getSelectedItem().equals("")) {
					String[] ids = ((String) tags.getSelectedItem()).split(",");
					for (String id : ids) {
						ONDEXConcept tag = graph.getConcept(Integer.parseInt(id));
						concept.addTag(tag);
					}
				}
				graph.setVisibility(concept, true);

				// hide dialog
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}

				// centre new node
				int width = viz.getWidth() / 2;
				int height = viz.getHeight() / 2;
				viz.getGraphLayout().setLocation(concept, new Point2D.Float(width, height));

				// update shape and label of node
				viewer.updateViewer(concept);
				viewer.getMetaGraph().updateMetaData();

				// OVTK-202
				String title = viewer.getTitle();
				if (!title.endsWith(" (modified)")) {
					viewer.setTitle(title + " (modified)");
					viewer.setName(viewer.getName() + " (modified)");
				}
			}

			// modify concept
			else if (validateEntry() && concept != null) {
				// set changed annotation
				if (!annotation.getText().equals(concept.getAnnotation())) {
					concept.setAnnotation(annotation.getText());
				}
				// set changed description
				if (!description.getText().equals(concept.getDescription())) {
					concept.setDescription(description.getText());
				}
				// modified evidences
				Set<String> original = new HashSet<String>();
				for (EvidenceType et : concept.getEvidence()) {
					original.add(et.getId());
				}
				Set<String> modified = new HashSet<String>(Arrays.asList(((String) evidences.getSelectedItem()).split(",")));
				Set<String> delete = new HashSet<String>(original);
				delete.removeAll(modified);
				Set<String> add = new HashSet<String>(modified);
				add.removeAll(original);
				for (String aDelete : delete) {
					concept.removeEvidenceType(graph.getMetaData().getEvidenceType(aDelete));
				}
				for (String anAdd : add) {
					concept.addEvidenceType(graph.getMetaData().getEvidenceType(anAdd));
				}
				// modified tag
				Set<Integer> originalTag = new HashSet<Integer>();
				for (ONDEXConcept c : concept.getTags()) {
					originalTag.add(c.getId());
				}
				Set<Integer> modifiedTag = new HashSet<Integer>();
				if (tags.getItemAt(0) != null && !tags.getItemAt(0).equals("")) {
					for (String id : ((String) tags.getSelectedItem()).split(",")) {
						modifiedTag.add(Integer.parseInt(id));
					}
				}
				Set<Integer> deleteTag = new HashSet<Integer>(originalTag);
				deleteTag.removeAll(modifiedTag);
				Set<Integer> addTag = new HashSet<Integer>(modifiedTag);
				addTag.removeAll(originalTag);
				for (Integer aDeleteTag : deleteTag) {
					concept.removeTag(graph.getConcept(aDeleteTag));
				}
				for (Integer anAddTag : addTag) {
					concept.addTag(graph.getConcept(anAddTag));
				}

				// hide dialog
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}

				// update shape and label of node
				viewer.updateViewer(concept);
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

		// add a new DataSource
		else if (cmd.equals(DATASOURCE)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Concept.CreateDataSource"))) {
				DialogDataSource dialog = new DialogDataSource(graph, this);
				displayCentered(dialog);
			}
		}

		// add a new ConceptClass
		else if (cmd.equals(CONCEPTCLASS)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Concept.CreateConceptClass"))) {
				DialogConceptClass dialog = new DialogConceptClass(graph, this);
				displayCentered(dialog);
			}
		}

		// add new EvidenceType or modify list of Evidences
		else if (cmd.equals(EVIDENCETYPE)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Concept.CreateEvidenceType"))) {
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
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Concept.PickNew"))) {
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

		// view/edit concept names
		else if (cmd.equals(CONCEPTNAME)) {
			DialogConceptName dialog = new DialogConceptName(concept, viewer);
			displayCentered(dialog);
		}

		// view/edit concept accessions
		else if (cmd.equals(CONCEPTACCESSION)) {
			DialogConceptAccession dialog = new DialogConceptAccession(concept, viewer);
			displayCentered(dialog);
		}

		// view/edit concept Attribute
		else if (cmd.equals(ATTRIBUTE)) {
			// wrap into indefinite process
			IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
				public void task() {
					// display the Attribute dialog
					DialogAttribute dialog = new DialogAttribute(graph, concept);
					displayCentered(dialog);
				}
			};

			// kick-off processing
			OVTKProgressMonitor.start(OVTK2Desktop.getInstance().getMainFrame(), "Initialising properties...", p);
			p.start();
		}
	}

	/**
	 * Init new node selection on deiconify.
	 */
	@Override
	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		super.internalFrameDeiconified(arg0);
		initTag();
	}

}
