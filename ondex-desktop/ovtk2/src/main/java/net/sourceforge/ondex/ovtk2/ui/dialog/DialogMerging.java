package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
import net.sourceforge.ondex.transformer.relationcollapser.ClusterCollapser;

/**
 * Concept merging dialog
 * 
 * @author taubertj
 * 
 */
public class DialogMerging extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = -394997201555003988L;

	/**
	 * Selection of accession data source
	 */
	private JComboBox accessionDataSource;

	/**
	 * contains mapping of concept accession to set of concepts
	 */
	private Map<DataSource, Map<String, Set<ONDEXConcept>>> accessions = new HashMap<DataSource, Map<String, Set<ONDEXConcept>>>();

	/**
	 * whether or not to treat accessions case insensitive
	 */
	private JCheckBox caseInsensitive;

	/**
	 * Whether or not to collapse concepts finally
	 */
	private JCheckBox collapseConcepts;

	/**
	 * where to add GUI
	 */
	private Container contentPane;

	/**
	 * current ONDEXGraph
	 */
	private ONDEXGraph graph = null;

	/**
	 * Contains found possible mappings
	 */
	private Map<ONDEXConcept, Set<ONDEXConcept>> mappings = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();

	/**
	 * possible parent frame
	 */
	private JFrame parent = null;

	/**
	 * Display number of possible mappings
	 */
	private JLabel possibleMappings = new JLabel();

	/**
	 * Map only within this data source
	 */
	private JComboBox restrictDataSource;

	/**
	 * whether or not to use ambiguous accessions
	 */
	private JCheckBox useAmbiguous;

	/**
	 * wrapped OVTK2PropertiesAggregator
	 */
	private OVTK2PropertiesAggregator viewer;

	/**
	 * Map also within a data source
	 */
	private JCheckBox withinDataSource = new JCheckBox();

	/**
	 * Constructs user input to merge similar concepts.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 * @param parent
	 *            JFrame to render at
	 */
	public DialogMerging(OVTK2PropertiesAggregator viewer, JFrame parent) {
		super("Dialog.Merging.Title", "Properties16.gif");

		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();
		this.parent = parent;

		if (parent != null)
			contentPane = parent.getContentPane();
		else
			contentPane = this.getContentPane();

		extractAccessions(false, true);

		contentPane.setLayout(new BorderLayout());
		if (accessions.size() > 0) {
			contentPane.add(initGUI(), BorderLayout.CENTER);
			contentPane.add(makeButtonsPanel("Dialog.Merging.Apply", "Dialog.Merging.Cancel"), BorderLayout.SOUTH);
		} else {
			contentPane.add(new JLabel("No concept accessions found."), BorderLayout.CENTER);
			JButton cancel = new JButton(Config.language.getProperty("Dialog.Merging.Cancel"));
			cancel.setActionCommand("cancel");
			cancel.addActionListener(this);
			contentPane.add(cancel, BorderLayout.SOUTH);
		}
		this.pack();

		if (accessions.size() > 0)
			updatePossibleMappings();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// perform merging
		if (cmd.equals(APPLY)) {

			// Output a warning message
			if (collapseConcepts.isSelected()) {
				ClusterCollapser collapser = new ClusterCollapser(true, true, null);

				int option = JOptionPane.showConfirmDialog(contentPane, Config.language.getProperty("Dialog.Merging.WarningMessage"), Config.language.getProperty("Dialog.Merging.WarningTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				if (option == JOptionPane.YES_OPTION) {
					// process collapsing here
					try {

						// extract clusters from mappings
						Map<Integer, Set<ONDEXConcept>> clusters = new HashMap<Integer, Set<ONDEXConcept>>();
						int i = 0;
						Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
						for (ONDEXConcept c : mappings.keySet()) {
							if (!processed.contains(c)) {
								if (!clusters.containsKey(i))
									clusters.put(i, new HashSet<ONDEXConcept>());
								clusters.get(i).add(c);
								clusters.get(i).addAll(mappings.get(c));
								processed.add(c);
								processed.addAll(mappings.get(c));
							}
							i++;
						}
						System.out.println("Found " + clusters.size() + " clusters.");

						Set<ONDEXConcept> created = new HashSet<ONDEXConcept>();
						for (Integer key : clusters.keySet()) {
							// collapse every cluster
							ONDEXConcept c = collapser.collapseConceptCluster(graph, clusters.get(key));
							created.add(c);
						}

						System.out.println("Created " + created.size() + " cluster concepts.");

						// make new concepts visible
						viewer.getONDEXJUNGGraph().setVisibility(created, true);
						for (ONDEXConcept c : created) {
							// make all relations visible
							viewer.getONDEXJUNGGraph().setVisibility(graph.getRelationsOfConcept(c), true);
						}

						// layout nodes on big circle
						LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), null, created);

						if (viewer.getMetaGraph() != null)
							viewer.getMetaGraph().updateMetaData();
					} catch (InconsistencyException e) {
						e.printStackTrace();
					}
				}

				else if (option == JOptionPane.CANCEL_OPTION) {
					// do nothing on cancel
					return;
				}
			} else {

				// init meta data
				RelationType ofType = graph.getMetaData().getRelationType("equ");
				if (ofType == null)
					ofType = graph.getMetaData().getFactory().createRelationType("equ");
				EvidenceType evidencetype = graph.getMetaData().getEvidenceType("ACC");
				if (evidencetype == null)
					evidencetype = graph.getMetaData().getFactory().createEvidenceType("ACC");

				// simply create equivalent relationship
				Set<ONDEXRelation> created = new HashSet<ONDEXRelation>();
				for (ONDEXConcept fromConcept : mappings.keySet()) {
					for (ONDEXConcept toConcept : mappings.keySet()) {
						if (!fromConcept.equals(toConcept)) {
							// create new relation
							ONDEXRelation r = graph.getFactory().createRelation(fromConcept, toConcept, ofType, evidencetype);
							created.add(r);
						}
					}
				}

				// make new relations visible
				viewer.getONDEXJUNGGraph().setVisibility(created, true);

				if (viewer.getMetaGraph() != null)
					viewer.getMetaGraph().updateMetaData();
			}

			try {
				if (parent == null)
					this.setClosed(true);
				else
					parent.setVisible(false);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}

		// cancel dialog
		else if (cmd.equals(CANCEL)) {
			try {
				if (parent == null)
					this.setClosed(true);
				else
					parent.setVisible(false);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}

		// update counter of mappings
		else if (arg0.getSource().equals(accessionDataSource)) {
			updatePossibleMappings();
		}

		// update accession list
		else if (arg0.getSource().equals(useAmbiguous)) {

			// re-create accession list
			extractAccessions(useAmbiguous.isSelected(), caseInsensitive.isSelected());

			// update data source drop-down box
			populateAccessionDataSource();
		}

		// update accession list
		else if (arg0.getSource().equals(caseInsensitive)) {
			// re-create accession list
			extractAccessions(useAmbiguous.isSelected(), caseInsensitive.isSelected());

			// update data source drop-down box
			populateAccessionDataSource();
		}

		// update accession list
		else if (arg0.getSource().equals(withinDataSource)) {
			restrictDataSource.setEnabled(withinDataSource.isSelected());
			updatePossibleMappings();
		}

		// update accession list
		else if (arg0.getSource().equals(restrictDataSource)) {
			updatePossibleMappings();
		}
	}

	/**
	 * Extracts all concept accessions from concepts
	 * 
	 */
	private void extractAccessions(boolean useAmbiguous, boolean caseInsensitive) {

		// for subsequent calls empty map first
		accessions.clear();

		// process all concepts
		for (ONDEXConcept c : graph.getConcepts()) {

			// get concept accessions
			for (ConceptAccession ca : c.getConceptAccessions()) {

				// whether or not to include ambiguous accessions
				if (useAmbiguous || !ca.isAmbiguous()) {

					// sort by DataSource first
					DataSource ds = ca.getElementOf();
					if (!accessions.containsKey(ds)) {
						accessions.put(ds, new HashMap<String, Set<ONDEXConcept>>());
					}

					// check for case insensitive
					String accession = ca.getAccession();
					if (caseInsensitive)
						accession = accession.toUpperCase();

					// use accession as map key
					if (!accessions.get(ds).containsKey(accession)) {
						accessions.get(ds).put(accession, new HashSet<ONDEXConcept>());
					}

					// finally add concept to map
					accessions.get(ds).get(accession).add(c);
				}
			}
		}
	}

	/**
	 * Creates the GUI design and layout.
	 * 
	 * @return JPanel
	 */
	private JPanel initGUI() {

		// init GUI layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		// nice border
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Merging.Properties"));
		properties.setBorder(propertiesBorder);

		// help text for accession box
		JLabel accessionDataSourceLabel = new JLabel(Config.language.getProperty("Dialog.Merging.AccessionsOf"));
		properties.add(accessionDataSourceLabel);

		// selection of accession data source
		accessionDataSource = new JComboBox();
		populateAccessionDataSource();
		properties.add(accessionDataSource);

		// help text for ambiguous box
		JLabel useAmbiguousLabel = new JLabel(Config.language.getProperty("Dialog.Merging.AmbiguousAccessions"));
		properties.add(useAmbiguousLabel);

		// make ambiguous selection box
		useAmbiguous = new JCheckBox();
		useAmbiguous.addActionListener(this);
		properties.add(useAmbiguous);

		// help text for case insensitive box
		JLabel caseInsensitiveLabel = new JLabel(Config.language.getProperty("Dialog.Merging.CaseInsensitive"));
		properties.add(caseInsensitiveLabel);

		// make case insensitive box
		caseInsensitive = new JCheckBox();
		caseInsensitive.setSelected(true);
		caseInsensitive.addActionListener(this);
		properties.add(caseInsensitive);

		// help text for within data source box
		JLabel withinDataSourceLabel = new JLabel(Config.language.getProperty("Dialog.Merging.WithinDataSource"));
		properties.add(withinDataSourceLabel);

		// make within data source box
		withinDataSource = new JCheckBox();
		withinDataSource.addActionListener(this);
		properties.add(withinDataSource);

		// help text for restrict data source box
		JLabel restrictDataSourceLabel = new JLabel(Config.language.getProperty("Dialog.Merging.RestrictDataSource"));
		properties.add(restrictDataSourceLabel);

		// make restrict data source box
		restrictDataSource = new JComboBox();
		populateRestrictDataSource();
		properties.add(restrictDataSource);

		// help text for collapse concepts bix
		JLabel collapseConceptsLabel = new JLabel(Config.language.getProperty("Dialog.Merging.CollapseConcepts"));
		properties.add(collapseConceptsLabel);

		// make collapse concepts box
		collapseConcepts = new JCheckBox();
		collapseConcepts.setSelected(true);
		properties.add(collapseConcepts);

		// preamble for mapping figure
		JLabel possibleMappingsLabel = new JLabel(Config.language.getProperty("Dialog.Merging.PossibleMappings"));
		properties.add(possibleMappingsLabel);

		// number possible mappings
		properties.add(possibleMappings);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(accessionDataSourceLabel).addComponent(useAmbiguousLabel).addComponent(caseInsensitiveLabel).addComponent(withinDataSourceLabel).addComponent(restrictDataSourceLabel).addComponent(collapseConceptsLabel).addComponent(possibleMappingsLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup().addComponent(accessionDataSource, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(useAmbiguous, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(caseInsensitive, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(withinDataSource, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(restrictDataSource, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(collapseConcepts, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(possibleMappings, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(accessionDataSourceLabel).addComponent(accessionDataSource)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(useAmbiguousLabel).addComponent(useAmbiguous)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(caseInsensitiveLabel).addComponent(caseInsensitive)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(withinDataSourceLabel).addComponent(withinDataSource)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(restrictDataSourceLabel).addComponent(restrictDataSource)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(collapseConceptsLabel).addComponent(collapseConcepts))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(possibleMappingsLabel).addComponent(possibleMappings)));

		return properties;
	}

	/**
	 * Adds concept accession data sources to drop down box
	 */
	private void populateAccessionDataSource() {
		// accessions are first index by data source
		DataSource[] dataSources = accessions.keySet().toArray(new DataSource[0]);
		Arrays.sort(dataSources);

		// update drop-down box
		accessionDataSource.removeActionListener(this);
		accessionDataSource.removeAllItems();
		for (DataSource ds : dataSources) {
			accessionDataSource.addItem(ds);
		}
		accessionDataSource.addActionListener(this);
		accessionDataSource.setSelectedIndex(0);
		accessionDataSource.revalidate();
	}

	/**
	 * Adds concept data sources to drop down box
	 */
	private void populateRestrictDataSource() {

		restrictDataSource.setEnabled(withinDataSource.isSelected());

		// check only for data sources with concepts
		Set<DataSource> dataSources = new HashSet<DataSource>();
		for (DataSource dataSource : graph.getMetaData().getDataSources()) {
			if (graph.getConceptsOfDataSource(dataSource).size() > 0) {
				dataSources.add(dataSource);
			}
		}

		// sort data sources
		DataSource[] array = dataSources.toArray(new DataSource[0]);
		Arrays.sort(array);

		// dummy data source
		DataSource none = graph.getMetaData().getDataSource("NONE");
		if (none == null)
			none = graph.getMetaData().getFactory().createDataSource("NONE");

		// add data source to drop-down box
		restrictDataSource.removeActionListener(this);
		restrictDataSource.removeAll();
		restrictDataSource.addItem(none);
		for (DataSource dataSource : array) {
			restrictDataSource.addItem(dataSource);
		}
		restrictDataSource.addActionListener(this);
		restrictDataSource.setSelectedIndex(0);
		restrictDataSource.revalidate();
	}

	/**
	 * Calculate the number of possible mappings based on current selection
	 * criteria. Number refers to clusters of concepts sharing same accession.
	 * 
	 */
	private void updatePossibleMappings() {

		mappings.clear();

		// get accession data source selection
		DataSource ds = (DataSource) accessionDataSource.getSelectedItem();
		if (ds == null || accessions.get(ds) == null)
			return;

		// get number of accessions matches
		for (String accession : accessions.get(ds).keySet()) {

			// concepts need to be from same concept class and different data
			// source
			Map<ConceptClass, Map<DataSource, Set<ONDEXConcept>>> index = new HashMap<ConceptClass, Map<DataSource, Set<ONDEXConcept>>>();

			// all concepts with one and the same accession
			Set<ONDEXConcept> concepts = accessions.get(ds).get(accession);
			for (ONDEXConcept c : concepts) {

				// index concepts by their concept class
				ConceptClass conceptClass = c.getOfType();
				if (!index.containsKey(conceptClass))
					index.put(conceptClass, new HashMap<DataSource, Set<ONDEXConcept>>());

				// index concepts by their data source
				DataSource dataSource = c.getElementOf();
				if (!index.get(conceptClass).containsKey(dataSource))
					index.get(conceptClass).put(dataSource, new HashSet<ONDEXConcept>());

				// this is a two dimensional index
				index.get(conceptClass).get(dataSource).add(c);
			}

			// check for concepts of same concept class, but different data
			// sources
			for (ConceptClass conceptClass : index.keySet()) {
				Set<DataSource> dataSources = index.get(conceptClass).keySet();

				// only map within data sources
				if (withinDataSource.isSelected()) {

					// map within a particular data source
					if (restrictDataSource != null && restrictDataSource.getSelectedIndex() > 0) {

						// check for particular data source
						Set<ONDEXConcept> set = index.get(conceptClass).get(restrictDataSource.getSelectedItem());
						if (set != null) {

							// all vs. all mapping
							for (ONDEXConcept fromConcept : set) {
								// make sure key exists
								if (!mappings.containsKey(fromConcept))
									mappings.put(fromConcept, new HashSet<ONDEXConcept>());

								// all other concepts
								for (ONDEXConcept toConcept : set) {
									// prevent self-loops
									if (!fromConcept.equals(toConcept)) {
										mappings.get(fromConcept).add(toConcept);
									}
								}
							}
						}
					}

					else {

						// map in each of the data sources
						for (DataSource dataSource : dataSources) {
							Set<ONDEXConcept> set = index.get(conceptClass).get(dataSource);

							// all vs. all mapping
							for (ONDEXConcept fromConcept : set) {
								// make sure key exists
								if (!mappings.containsKey(fromConcept))
									mappings.put(fromConcept, new HashSet<ONDEXConcept>());

								// all other concepts
								for (ONDEXConcept toConcept : set) {
									// prevent self-loops
									if (!fromConcept.equals(toConcept)) {
										mappings.get(fromConcept).add(toConcept);
									}
								}
							}
						}
					}
				}

				// there are at least two different data sources
				else if (dataSources.size() > 1) {

					// map between different data sources
					for (DataSource ds1 : dataSources) {

						// all concepts of first data source
						for (ONDEXConcept fromConcept : index.get(conceptClass).get(ds1)) {

							// make sure key exists
							if (!mappings.containsKey(fromConcept))
								mappings.put(fromConcept, new HashSet<ONDEXConcept>());

							// look at other data sources
							for (DataSource ds2 : dataSources) {

								// concepts from different data source
								if (!ds1.equals(ds2)) {
									mappings.get(fromConcept).addAll(index.get(conceptClass).get(ds2));
								}
							}
						}
					}
				}
			}
		}

		// process count
		int count = 0;
		for (ONDEXConcept key : mappings.keySet()) {
			if (mappings.get(key).size() > 0)
				count++;
		}

		// update label text
		possibleMappings.setText(count + "");
	}
}
