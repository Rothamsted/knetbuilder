package net.sourceforge.ondex.ovtk2.annotator.microarray;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

/**
 * 
 * @author hindlem
 * 
 */
public class MicroarrayAnnotator extends OVTK2Annotator implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private static final String SIGNIFICANT_CHANGES = "Significant Changes";

	private static final String INSIGNIFICANT_CHANGES = "Insignificant Changes";

	private MicroarrayDataIndex dataIndex;

	// the size range for the nodes
	private NodeSizeListener nodeSizes;

	// show only probes with exp. above lsd threshold
	private boolean showOnySignificantProbes;

	// show probes in the graph that are not mapped to an expression level
	private boolean showUnmapedProbes;

	private boolean showSignificantChanges = true;

	private Map<String, ONDEXConcept> probeMapping;

	private ConceptClass targetCc;

	private MicroarrayDataTablePanel dataPanel;
	
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	public MicroarrayAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		JFileChooser chooser = new JFileChooser(new File(
				System.getProperty("user.dir")));

		Border blackline = BorderFactory.createLineBorder(Color.black);
		Border titleBorder = BorderFactory.createTitledBorder(blackline,
				"Import Options");
		JPanel optionsPanel = new JPanel(new SpringLayout());
		optionsPanel.setBorder(titleBorder);

		JCheckBox dataIsLogged = new JCheckBox("Data is logged");
		dataIsLogged.setSelected(true);
		optionsPanel.add(dataIsLogged);

		JPanel ccTargPanel = new JPanel();
		JComboBox conceptClassTargets = new JComboBox();
		for (ConceptClass cc : viewer.getONDEXJUNGGraph().getMetaData()
				.getConceptClasses()) {
			conceptClassTargets.addItem(cc);
		}

		ccTargPanel.add(new JLabel("Target ConceptClass"));
		ccTargPanel.add(conceptClassTargets);
		optionsPanel.add(ccTargPanel);

		SpringUtilities.makeCompactGrid(optionsPanel,
				optionsPanel.getComponentCount(), 1, 0, 0, 5, 5);

		chooser.setAccessory(optionsPanel);

		final JFormattedTextField logBasefield = new JFormattedTextField(
				new Double(2));
		logBasefield.setEnabled(false);

		dataIsLogged.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				logBasefield.setEnabled(((JCheckBox) e.getSource())
						.isSelected());
			}

		});

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// get selected file
			File file = chooser.getSelectedFile();

			Double logBase = (Double) logBasefield.getValue();
			if (!dataIsLogged.isSelected())
				logBase = null;

			MicroarrayDataParser parser = new MicroarrayDataParser(
					file.getName(), logBase);

			dataIndex = parser.parseData(file.getAbsolutePath(), 0, // header
					// conditions
					1, // the data starts
					4);// the lsds

			targetCc = (ConceptClass) conceptClassTargets.getSelectedItem();
			probeMapping = idProbeGraph(graph, targetCc);

		} else {
			return;
		}
		buildGUI();
	}

	/**
	 * ids matches to the loaded probe
	 * 
	 * @param jung
	 *            the current grapg
	 */
	private Map<String, ONDEXConcept> idProbeGraph(ONDEXGraph jung,
			ConceptClass cc) {

		Map<String, ONDEXConcept> targetSeqs = new HashMap<String, ONDEXConcept>();

		// get concepts from graph
		Set<ONDEXConcept> concepts;
		if (cc != null)
			concepts = jung.getConceptsOfConceptClass(cc);
		else
			concepts = jung.getConcepts();

		// map accessions
		if (concepts != null) {
			for (ONDEXConcept concept : concepts) {
				for (ConceptAccession ca : concept.getConceptAccessions()) {
					String acc = ca.getAccession().toUpperCase();
					if (!targetSeqs.containsKey(acc)) {
						targetSeqs.put(acc.toUpperCase(), concept);
					}
				}
				if (concept.getConceptAccessions().isEmpty()) {
					targetSeqs.put(concept.getPID(), concept);
				}
			}
		}
		return targetSeqs;
	}

	/**
	 * Construct the GUI componants
	 */
	public void buildGUI() {
		setEnabled(false);

		setLayout(new SpringLayout());

		add(createOptionsSelectionPanel()); // lsd and mapped probes options

		dataPanel = new MicroarrayDataTablePanel(dataIndex,
				new HashMap<String, Integer>());

		add(dataPanel);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 0,
				0, 5, 5);
		setEnabled(true);
	}

	/**
	 * Creates panel for selecting if the user wants lsd accounted for and
	 * displaying unmapped probes
	 * 
	 * @return the panel
	 */
	public JComponent createOptionsSelectionPanel() {
		JPanel optionsPanel = new JPanel();
		add(optionsPanel);

		optionsPanel.add(createNodeSizePanel());

		JCheckBox onlyShowProbesAboveLSD = new JCheckBox("Account for LSD");
		onlyShowProbesAboveLSD
				.setToolTipText("Only show targets with expression ratios above LSD");
		onlyShowProbesAboveLSD.setSelected(true);
		optionsPanel.add(onlyShowProbesAboveLSD);

		JButton greaterThanLSD = new JButton(SIGNIFICANT_CHANGES);
		greaterThanLSD.setSelected(true);
		greaterThanLSD.setActionCommand(SIGNIFICANT_CHANGES);
		greaterThanLSD.addActionListener(this);

		optionsPanel.add(greaterThanLSD);
		if (dataIndex.getProbeToLSD().size() <= 0) {
			onlyShowProbesAboveLSD.setEnabled(false);
			onlyShowProbesAboveLSD.setSelected(false);
			greaterThanLSD.setEnabled(false);
		}

		onlyShowProbesAboveLSD.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showOnySignificantProbes = ((JCheckBox) e.getSource())
						.isSelected();
			}

		});
		showOnySignificantProbes = onlyShowProbesAboveLSD.isSelected();

		final JCheckBox showUnmappedProbesBox = new JCheckBox(
				"Show Unmapped TargetSequences");
		showUnmappedProbesBox
				.setToolTipText("Show TargetSequnces that are not significant globaly");
		showUnmappedProbesBox.setSelected(false);
		if (targetCc == null) {
			showUnmappedProbesBox.setEnabled(false);
			showUnmappedProbesBox
					.setToolTipText("This is disabled because no target concept class is selected");
		}

		showUnmappedProbesBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				showUnmapedProbes = ((JCheckBox) e.getSource()).isSelected();
			}

		});
		showUnmapedProbes = showUnmappedProbesBox.isSelected();

		optionsPanel.add(showUnmappedProbesBox);

		optionsPanel.add(createRatioSelectionPanel());
		optionsPanel.add(new JLabel());

		return optionsPanel;
	}

	private static String FROM_RATIO_SELECTED = "FROM_RATIO_SELECTED";

	private static String TO_RATIO_SELECTED = "TO_RATIO_SELECTED";

	private static String APPLY_RATIO = "APPLY_RATIO";

	/**
	 * For selection from and to ratios
	 * 
	 * @return
	 */
	private JPanel createRatioSelectionPanel() {

		JComboBox ratioFrom = new JComboBox();
		ratioFrom.setActionCommand(FROM_RATIO_SELECTED);
		ratioFrom.addActionListener(this);

		JComboBox ratioTo = new JComboBox();
		ratioTo.setActionCommand(TO_RATIO_SELECTED);
		ratioTo.addActionListener(this);

		Iterator<String> it = dataIndex.getTreatments().iterator();
		while (it.hasNext()) {
			String treatment = it.next();
			ratioFrom.addItem(treatment);
			ratioTo.addItem(treatment);
		}

		ratioFrom.setSelectedIndex(0);
		ratioTo.setSelectedIndex(1);

		JButton apply = new JButton("Apply");
		apply.setActionCommand(APPLY_RATIO);
		apply.addActionListener(this);

		Border blackline = BorderFactory.createLineBorder(Color.black);
		Border titleBorder = BorderFactory.createTitledBorder(blackline,
				"Select Ratio");

		JPanel selectionPanel = new JPanel();
		selectionPanel.setBorder(titleBorder);
		selectionPanel.add(new JLabel("From:"));
		selectionPanel.add(ratioFrom);
		selectionPanel.add(new JLabel("To:"));
		selectionPanel.add(ratioTo);
		selectionPanel.add(new JLabel());
		selectionPanel.add(apply);

		return selectionPanel;
	}

	/**
	 * creates a panel for changing node size paramiters
	 * 
	 * @return the panel
	 */
	private JComponent createNodeSizePanel() {
		JPanel sizeConstraints = new JPanel(new SpringLayout());
		sizeConstraints.add(new JLabel("Min Node Size"));
		JFormattedTextField minField = new JFormattedTextField(new Integer(5));
		minField.setColumns(5);
		JPanel panel = new JPanel();
		panel.add(minField);
		sizeConstraints.add(panel);
		minField.setActionCommand("MIN");

		sizeConstraints.add(new JLabel("Max Node Size"));
		JFormattedTextField maxField = new JFormattedTextField(new Integer(50));
		maxField.setColumns(5);
		panel = new JPanel();
		panel.add(maxField);
		sizeConstraints.add(panel);
		maxField.setActionCommand("MAX");

		nodeSizes = new NodeSizeListener(minField, maxField, 5, 50);
		SpringUtilities.makeCompactGrid(sizeConstraints,
				sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);
		return sizeConstraints;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.Microarray");
	}

	private String selectedFrom = null;

	private String selectedTo = null;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(FROM_RATIO_SELECTED)) {
			selectedFrom = (String) ((JComboBox) e.getSource())
					.getSelectedItem();
		} else if (e.getActionCommand().equals(TO_RATIO_SELECTED)) {
			selectedTo = (String) ((JComboBox) e.getSource()).getSelectedItem();
		} else if (e.getActionCommand().equals(APPLY_RATIO)) {
			if (selectedFrom != null && selectedTo != null
					&& !selectedFrom.equals(selectedTo)) {
				dataPanel.highlightHeader(selectedFrom + ":" + selectedTo);
				editGraph();
				validate();
				repaint();
			}
		} else if (e.getActionCommand().equals(SIGNIFICANT_CHANGES)) { // invert
																		// on
			// click
			JButton button = (JButton) e.getSource();
			button.setText(INSIGNIFICANT_CHANGES);
			button.setActionCommand(INSIGNIFICANT_CHANGES);
			button.setBackground(Color.ORANGE);
			showSignificantChanges = true;

		} else if (e.getActionCommand().equals(INSIGNIFICANT_CHANGES)) {
			JButton button = (JButton) e.getSource();
			button.setText(SIGNIFICANT_CHANGES);
			button.setActionCommand(SIGNIFICANT_CHANGES);
			button.setBackground(new JButton().getBackground());// TODO: this is
			// inefficiant
			// fix
			showSignificantChanges = false;
		}
		
		used = true;
	}

	/**
	 * re-renders jung graph with the current selected ratio annoations
	 * 
	 */
	private void editGraph() {
		setVisibility();

		int targMin = nodeSizes.getMinSizeOfNode();
		int targMax = nodeSizes.getMaxSizeOfNode();

		int sizeRange = targMax - targMin;

		final Map<ONDEXConcept, Integer> amplification = LazyMap.decorate(
				new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultNodeSize;
					}
				});
		Map<ONDEXConcept, Color> colors = new HashMap<ONDEXConcept, Color>();

		Iterator<String> probesIt = dataIndex.getProbes().iterator();
		while (probesIt.hasNext()) {
			String probe = probesIt.next().toUpperCase();
			ONDEXConcept concept = probeMapping.get(probe);
			if (concept != null) { // is in graph
				Double ratio = dataIndex.getProbe2RatioIndex().get(probe)
						.get(selectedFrom).get(selectedTo);

				if (ratio > 0)
					colors.put(concept, Color.RED);
				else if (ratio < 0)
					colors.put(concept, Color.GREEN);
				else
					colors.put(concept, Color.YELLOW);

				double percentBase = Math.abs(ratio) / 2; // 2 is the absolute
				// size restraint
				double width = targMin + (percentBase * sizeRange);
				amplification.put(concept, (int) Math.rint(width));
			}
		}

		// set node colours for graph
		ONDEXNodeFillPaint nodeColors = viewer.getNodeColors();
		nodeColors.setFillPaintSelection(NodeFillPaintSelection.MANUAL);
		for (ONDEXConcept c : colors.keySet()) {
			nodeColors.updateColor(c, colors.get(c));
		}

		// set node sizes for graph
		ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
		nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				return amplification.get(input);
			}
		});

		// update viewer
		viewer.getVisualizationViewer().getModel().fireStateChanged();
		viewer.getVisualizationViewer().repaint();
	}

	/**
	 * Sets the visibility of jung graph concept based on lsd (if selected)
	 */
	private void setVisibility() {
		NumberFormat valFormatter = new DecimalFormat(".00000");
		NumberFormat lsdFormatter = new DecimalFormat(".000");

		HashSet<ONDEXConcept> probesToShow = new HashSet<ONDEXConcept>();

		if (viewer != null) {

			// make all edges/nodes invisible
			ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
			jung.setVisibility(jung.getEdges(), false);
			jung.setVisibility(jung.getVertices(), false);
			viewer.getVisualizationViewer().getModel().fireStateChanged();

			Map<ONDEXConcept, String> probeToValue = new HashMap<ONDEXConcept, String>();

			Iterator<String> probesIt = dataIndex.getProbes().iterator();
			while (probesIt.hasNext()) {
				String probe = probesIt.next().toUpperCase();
				ONDEXConcept concept = probeMapping.get(probe);
				Double ratio = dataIndex.getProbe2RatioIndex().get(probe)
						.get(selectedFrom).get(selectedTo);
				Double lsd = dataIndex.getProbeToLSD().get(probe);

				if (concept != null) {
					probeToValue.put(concept, valFormatter.format(ratio) + "\t"
							+ lsdFormatter.format(lsd));

					if (showSignificantChanges) {
						if (Math.abs(ratio) >= lsd || !showOnySignificantProbes) {
							// above the threshold ratio is significant
							probesToShow.add(concept);
						}
					} else {
						if (Math.abs(ratio) < lsd || !showOnySignificantProbes) {
							// above the threshold ratio is significant
							probesToShow.add(concept);
						}
					}
				}

			}

			if (showUnmapedProbes && targetCc != null) {
				System.err.println("SHOW ALL UNMAPPED PROBES");
				for (ONDEXConcept concept : jung
						.getConceptsOfConceptClass(targetCc)) {
					if (!probeMapping.values().contains(concept)) {
						probesToShow.add(concept);
					}
				}
			}

			// make probes visible
			for (ONDEXConcept concept : probesToShow) {
				jung.setVisibility(concept, true);

				// getConcepts that are of this probe tag
				Set<ONDEXConcept> depConcepts = jung.getConceptsOfTag(concept);
				for (ONDEXConcept depConcept : depConcepts) {
					jung.setVisibility(depConcept, true);
				}

				// getRelations that are of this probe tag
				Set<ONDEXRelation> relations = jung.getRelationsOfTag(concept);
				for (ONDEXRelation relation : relations) {
					jung.setVisibility(relation, true);
				}
			}

			System.out.println(probesToShow.size() + " probes visible");
			viewer.getVisualizationViewer().getModel().fireStateChanged();
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
