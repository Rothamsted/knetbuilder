package net.sourceforge.ondex.ovtk2.filter.threshold;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.significance.Filter;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.stats.Statistic;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.ui.RectangleInsets;

/**
 * Filters relations based on a significance value for Attribute.
 * 
 * @author taubertj, lysenkoa
 * @version 20.03.2008
 */
public class ThresholdFilter extends OVTK2Filter implements ActionListener,
		ListSelectionListener, ChangeListener, ChartMouseListener {

	/**
	 * Creates a dataset, might be empty.
	 * 
	 * @return The dataset.
	 */
	private class Extractor {

		double max = Double.NEGATIVE_INFINITY;

		double min = Double.POSITIVE_INFINITY;

		// counts occurrences of each Attribute value
		Collection<Number> occurrences = new ArrayList<Number>();

		public Extractor(AttributeName target) {
			if (!conceptModeSelected) {
				// iterate over all relations of attribute name
				for (ONDEXRelation r : graph
						.getRelationsOfAttributeName(target)) {

					// get relation Attribute
					Attribute attribute = r.getAttribute(target);
					Number number = (Number) attribute.getValue();

					if (useAbsoluteValues.isSelected()) { // absolute all if
															// asked
						number = Math.abs(number.doubleValue());
					}

					// count up occurrences
					occurrences.add(number);
					if (number.doubleValue() > max)
						max = number.doubleValue();
					if (number.doubleValue() < min)
						min = number.doubleValue();
				}
			} else {
				// iterate over all relations of attribute name
				for (ONDEXConcept r : graph.getConceptsOfAttributeName(target)) {

					// get relation Attribute
					Attribute attribute = r.getAttribute(target);
					Number number = (Number) attribute.getValue();

					if (useAbsoluteValues.isSelected()) { // absolute all if
															// asked
						number = Math.abs(number.doubleValue());
					}

					// count up occurrences
					occurrences.add(number);
					if (number.doubleValue() > max)
						max = number.doubleValue();
					if (number.doubleValue() < min)
						min = number.doubleValue();
				}
			}
		}

		/**
		 * All attribute values as a List.
		 * 
		 * @return
		 */
		public Collection<Number> getDataSet() {
			return occurrences;
		}

		/**
		 * Maximum value found
		 * 
		 * @return
		 */
		public double getMaximalValue() {
			return max;
		}

		/**
		 * Minimum value found
		 * 
		 * @return
		 */
		public double getMinimalValue() {
			return min;
		}
	}

	/**
	 * GUI for subset selection
	 * 
	 * @author lysenkoa
	 * 
	 */
	private class SubsetPanel extends JPanel {

		private static final long serialVersionUID = 4301359981277949126L;

		private JTextField subsetName = new JTextField();

		private JCheckBox useThis = new JCheckBox("Assign subsets");

		public SubsetPanel() {
			this.setLayout(new GridBagLayout());
			GridBagConstraints con = new GridBagConstraints();
			con.fill = GridBagConstraints.HORIZONTAL;
			useThis.setSelected(false);
			con.gridx = 0;
			con.gridy = 0;
			con.weighty = 1;
			con.weightx = 0;
			this.add(useThis, con);
			con.gridx = 1;
			con.weightx = 0;
			con.weightx = 1;

			this.add(subsetName, con);
		}

		public String getCategoryName() {
			return subsetName.getText();
		}

		public boolean isAssignCategories() {
			return useThis.isSelected();
		}
	}

	private static final String ABSOLUTE = "Absolute";

	private static final String GO = "GO";

	private static final long serialVersionUID = -7992395983692245135L;

	// used to wrap concept an list
	private AttributeNameListModel anlmConcepts = null;

	// used to wrap relation an list
	private AttributeNameListModel anlmRelations = null;

	// chart of Attribute value occurrences
	private JFreeChart chart = null;

	// the chart panel for histogram
	private ChartPanel chartPanel = null;

	// toggle between concept and relation
	private boolean conceptModeSelected = true;

	// the important button
	private JButton goButton = null;

	// no attribute
	private JCheckBox hideNoAttributeElements = null;

	// check box for unconnected hiding
	private JCheckBox hideUnconnectedConcepts = null;

	// interactive filtering of graph
	private JCheckBox interactiveFiltering = null;

	// check box for inverse behaviour
	private JCheckBox inverseFiltering = null;

	// displays selection list
	private JList listConceptAttributeNames = null;

	// displays selection list
	private JList listRelationAttributeNames = null;

	// slider for resolution
	private JSlider resolution;

	// create subset options
	private SubsetPanel subsets = new SubsetPanel();

	// consolidates concepts, relations and options
	private JTabbedPane tabbedPane = null;

	// which attribute name to use
	private AttributeName targetAttributeName = null;

	// check both to absolute Attribute values
	private JCheckBox useAbsoluteValues = null;

	/**
	 * Filter has been used
	 */
	private boolean used = false;

	/**
	 * Constructs GUI of Filter and initialises with given viewer.
	 * 
	 * @param viewer
	 */
	public ThresholdFilter(OVTK2Viewer viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		// The magic button
		goButton = new JButton("Filter Graph");
		goButton.setEnabled(false);
		goButton.setActionCommand(GO);
		goButton.addActionListener(this);

		// new lists of concepts and relations attribute names
		anlmConcepts = new AttributeNameListModel();
		anlmRelations = new AttributeNameListModel();

		listConceptAttributeNames = new JList(anlmConcepts);
		listConceptAttributeNames.setCellRenderer(new CustomCellRenderer());

		listRelationAttributeNames = new JList(anlmRelations);
		listRelationAttributeNames.setCellRenderer(new CustomCellRenderer());

		// get attribute names from meta data
		populateConceptAttributeNameList();
		populateRelationAttributeNameList();

		listConceptAttributeNames.validate();
		listConceptAttributeNames
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listConceptAttributeNames.addListSelectionListener(this);

		listRelationAttributeNames.validate();
		listRelationAttributeNames
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listRelationAttributeNames.addListSelectionListener(this);

		// tabbed pane for choosing concept or relation attributes
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);

		JPanel panelConcepts = new JPanel(new GridLayout(1, 1));
		panelConcepts.add(new JScrollPane(listConceptAttributeNames));
		tabbedPane.addTab("Attributes on Concepts", null, panelConcepts,
				"Select attributes on concepts");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_C);

		JPanel panelRelations = new JPanel(new GridLayout(1, 1));
		panelRelations.add(new JScrollPane(listRelationAttributeNames));
		tabbedPane.addTab("Attributes on Relations", null, panelRelations,
				"Select attributes on relations");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_R);

		JPanel panelOptions = new JPanel();
		BoxLayout layout = new BoxLayout(panelOptions, BoxLayout.PAGE_AXIS);
		panelOptions.setLayout(layout);
		tabbedPane.addTab("Filter options", null, panelOptions,
				"Configure filter options");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_O);

		add(tabbedPane);

		// here add empty chart
		JFreeChart chart = createChart(createDataset());
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		chartPanel.addChartMouseListener(this);
		// OVTK-323
		chartPanel.setLocale(Locale.ENGLISH);
		add(chartPanel);

		resolution = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
		resolution.setToolTipText("Length of quantification interval.");
		resolution.addChangeListener(this);
		resolution.setMajorTickSpacing(25);
		resolution.setMinorTickSpacing(5);
		resolution.setPaintTicks(true);
		resolution.setPaintLabels(false);
		// OVTK-323
		resolution.setLocale(Locale.ENGLISH);
		JPanel sliderpanel = new JPanel(new BorderLayout());
		sliderpanel.add(new JLabel("Resolution"), BorderLayout.WEST);
		sliderpanel.add(resolution, BorderLayout.CENTER);
		add(sliderpanel);

		// filter options below
		JPanel checkBoxes = new JPanel(new GridLayout(3, 3));

		hideUnconnectedConcepts = new JCheckBox("Hide unconnected nodes");
		hideUnconnectedConcepts
				.setToolTipText("Hides nodes that got unconnected after applying the filter.");
		hideUnconnectedConcepts.setSelected(true);
		checkBoxes.add(hideUnconnectedConcepts);

		inverseFiltering = new JCheckBox("Inverse threshold");
		inverseFiltering
				.setToolTipText("Filter elements which have values smaller than selected threshold.");
		checkBoxes.add(inverseFiltering);

		hideNoAttributeElements = new JCheckBox(
				"Hide elements with no attribute");
		hideNoAttributeElements
				.setToolTipText("Hides all elements of the graph which do not share the selected attribute.");
		hideNoAttributeElements.setSelected(true);
		checkBoxes.add(hideNoAttributeElements);

		useAbsoluteValues = new JCheckBox("Absolute values");
		useAbsoluteValues.addActionListener(this);
		useAbsoluteValues.setToolTipText("Absolute all attribute values.");
		useAbsoluteValues.setActionCommand(ABSOLUTE);
		checkBoxes.add(useAbsoluteValues);

		interactiveFiltering = new JCheckBox("Interactive filtering");
		interactiveFiltering
				.setToolTipText("Filter the graph when selecting a threshold.");
		checkBoxes.add(interactiveFiltering);

		checkBoxes.add(subsets);

		panelOptions.add(checkBoxes);

		add(goButton);
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 7,
				5, 5, 5);
	}

	/**
	 * Associated with buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(GO)) {
			try {
				callFilter();
				used = true;
			} catch (InvalidPluginArgumentException e1) {
				ErrorDialog.show(e1);
			}
		} else if (e.getActionCommand().equals(ABSOLUTE)) {
			chart.getXYPlot().setDataset(createDataset());
		}
	}

	/**
	 * Calls backend filter.
	 */
	private void callFilter() throws InvalidPluginArgumentException {

		StateEdit edit = new StateEdit(new VisibilityUndo(
				viewer.getONDEXJUNGGraph()), this.getName());
		OVTK2Desktop desktop = OVTK2Desktop.getInstance();
		desktop.setRunningProcess(this.getName());

		// setup filter and start
		Filter filter = new Filter();

		// compile filter arguments
		ONDEXPluginArguments fa = new ONDEXPluginArguments(
				filter.getArgumentDefinitions());
		fa.addOption(Filter.CONCEPTMODE_ARG, conceptModeSelected);
		fa.addOption(Filter.TARGETAN_ARG, targetAttributeName.getId());
		fa.addOption(Filter.SIG_ARG,
				(double) ((XYPlot) chart.getPlot()).getDomainCrosshairValue());
		fa.addOption(Filter.INVERSE_ARG, inverseFiltering.isSelected());
		fa.addOption(Filter.ABSOLUTE_ARG, useAbsoluteValues.isSelected());
		fa.addOption(Filter.NO_ATT_ARG, hideNoAttributeElements.isSelected());
		filter.setONDEXGraph(graph);
		filter.setArguments(fa);
		filter.start();

		// set visible concepts again visible
		Set<ONDEXConcept> concepts = filter.getVisibleConcepts();
		Set<ONDEXRelation> relations = filter.getVisibleRelations();

		// remove unconnected nodes
		if (hideUnconnectedConcepts.isSelected()) {
			concepts = StandardFunctions.filteroutUnconnected(graph, concepts,
					relations);
		}

		// assigns a number in Attribute per connected component
		if (subsets.isAssignCategories()) {
			AttributeName att = createAttName(graph, subsets.getCategoryName(),
					Integer.class);
			Set<ONDEXConcept> unprocessedConcepts = BitSetFunctions
					.copy(concepts);
			Set<ONDEXRelation> unprocessedRelations = BitSetFunctions
					.copy(relations);
			Set<Integer> takenIds = new HashSet<Integer>();
			Integer lastId = 1;
			while (unprocessedConcepts.size() > 0) {
				BitSet[] group = StandardFunctions.getAllConnected(
						unprocessedConcepts.iterator().next(), graph,
						unprocessedRelations);
				Set<ONDEXConcept> concGroup = BitSetFunctions.create(graph,
						ONDEXConcept.class, group[0]);
				unprocessedConcepts = BitSetFunctions.andNot(
						unprocessedConcepts, concGroup);
				SortedMap<Integer, Object> prevalence = StandardFunctions
						.gdsRanking(graph, concGroup, att.getId());
				Integer groupID = 1;
				if (prevalence.size() != 0)
					groupID = Integer.valueOf(prevalence.get(
							prevalence.lastKey()).toString());
				if (groupID == null || takenIds.contains(groupID)) {
					groupID = lastId + 1;
					while (takenIds.contains(groupID))
						groupID++;
					lastId = groupID;
				}
				takenIds.add(groupID);

				// taken from JAVA Doc how to iterate a BitSet
				for (int i = group[0].nextSetBit(0); i >= 0; i = group[0]
						.nextSetBit(i + 1)) {
					ONDEXConcept c = graph.getConcept(i);
					if (c.getAttribute(att) != null) {
						c.deleteAttribute(att);
					}
					c.createAttribute(att, groupID, false);
				}
			}
		}

		Set<ONDEXConcept> invisibleConcepts = BitSetFunctions.andNot(
				graph.getConcepts(), concepts);
		Set<ONDEXRelation> invisibleRelations = BitSetFunctions.andNot(
				graph.getRelations(), relations);

		// set visible concepts again visible
		for (ONDEXConcept c : concepts) {
			graph.setVisibility(c, true);
		}

		// set visible relations again visible
		for (ONDEXRelation r : relations) {
			graph.setVisibility(r, true);
		}

		// set invisible relations invisible
		for (ONDEXRelation r : invisibleRelations) {
			graph.setVisibility(r, false);
		}

		// set invisible concepts invisible
		for (ONDEXConcept c : invisibleConcepts) {
			graph.setVisibility(c, false);
		}

		// propagate change to viewer
		viewer.getVisualizationViewer().getModel().fireStateChanged();

		edit.end();
		viewer.getUndoManager().addEdit(edit);
		desktop.getOVTK2Menu().updateUndoRedo(viewer);
		desktop.notifyTerminationOfProcess();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent arg0) {
		if (interactiveFiltering.isSelected()) {
			// this is a hack to allow propagation and setting of domain value
			Thread update = new Thread() {
				@Override
				public void run() {
					synchronized (this) {
						try {
							this.wait(100);
						} catch (InterruptedException ie) {
							// do nothing
						}
						try {
							callFilter();
						} catch (InvalidPluginArgumentException e) {
							ErrorDialog.show(e);
						}
					}
				}
			};
			update.start();
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0) {

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            a dataset.
	 * @return A chart.
	 */
	private JFreeChart createChart(SimpleHistogramDataset dataset) {
		String label = "Value";
		if (targetAttributeName != null) {
			label = targetAttributeName.getFullname();
			if (label == null || label.trim().length() == 0)
				label = targetAttributeName.getId();
		}
		chart = ChartFactory.createHistogram(null, label, "Histogram", dataset,
				PlotOrientation.VERTICAL, false, true, false);

		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.getRenderer().setSeriesPaint(0, new Color(0x7f9f51));
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		return chart;
	}

	/**
	 * performs the data extraction and quantification.
	 * 
	 * @return a histogram dataset.
	 */
	private SimpleHistogramDataset createDataset() {
		SimpleHistogramDataset set = new SimpleHistogramDataset("empty");
		if (targetAttributeName != null) {
			Extractor extractor = new Extractor(targetAttributeName);

			double resolution = getResolution(extractor);

			Statistic statistic = new Statistic(extractor.getDataSet(),
					resolution);

			set = new SimpleHistogramDataset(targetAttributeName.getId());
			set.setAdjustForBinSize(false);
			SimpleHistogramBin bin;
			double[][] his = statistic.getHistogramValues();
			double x_start, x_end;
			int count = 0;
			for (int i = 0; i < his.length; i++) {
				x_start = his[i][0];
				x_end = his[i][0] + resolution;
				count = (int) his[i][1];
				bin = new SimpleHistogramBin(x_start, x_end, true, false);
				bin.setItemCount(count);
				set.addBin(bin);
			}

		}
		return set;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Filter.Threshold");
	}

	/**
	 * @return the length of the quantification interval as chosen by the user.
	 */
	private double getResolution(Extractor extractor) {
		double selectedVal;
		if (resolution != null)
			selectedVal = (double) resolution.getValue();
		else
			selectedVal = 25.0;
		double max = extractor.getMaximalValue();
		double min = extractor.getMinimalValue();
		double range = max - min;
		double pot = 2.0 * (selectedVal / 100.0);
		double perc = Math.pow(10.0, pot) - 1;
		double result = (perc / 100.0) * range;
		resolution.setToolTipText("Current: " + result);
		return result;
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

	/**
	 * Fills list of concept attribute names.
	 * 
	 */
	private void populateConceptAttributeNameList() {
		anlmConcepts.clearList();
		for (AttributeName an : graph.getMetaData().getAttributeNames()) {
			Set<ONDEXConcept> concepts = graph.getConceptsOfAttributeName(an);
			if (concepts != null) {
				// check concepts exists on this AttributeName
				Class<?> cl = an.getDataType();
				if (concepts.size() > 0 && Number.class.isAssignableFrom(cl)
						&& !AppearanceSynchronizer.attr.contains(an.getId())) {
					anlmConcepts.addAttributeName(an);
				}
			}

		}
		anlmConcepts.refresh();
		listConceptAttributeNames.setEnabled(!anlmConcepts.isEmpty());
		listConceptAttributeNames.repaint();
	}

	/**
	 * Fills list of relation attribute names.
	 * 
	 */
	private void populateRelationAttributeNameList() {
		anlmRelations.clearList();
		for (AttributeName an : graph.getMetaData().getAttributeNames()) {
			Set<ONDEXRelation> relations = graph
					.getRelationsOfAttributeName(an);
			if (relations != null) {
				// check relations exists on this AttributeName
				Class<?> cl = an.getDataType();
				if (relations.size() > 0 && Number.class.isAssignableFrom(cl)
						&& !AppearanceSynchronizer.attr.contains(an.getId())) {
					anlmRelations.addAttributeName(an);
				}
			}
		}
		anlmRelations.refresh();
		listRelationAttributeNames.setEnabled(!anlmRelations.isEmpty());
		listRelationAttributeNames.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			// resolution adapting
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				updateChart();
			}
		} else {
			// change between concept and relation attributes
			if (tabbedPane.getSelectedIndex() == 0) {
				conceptModeSelected = true;
			}
			if (tabbedPane.getSelectedIndex() == 1) {
				conceptModeSelected = false;
			}
		}
	}

	/**
	 * Creates a new chart object and replaces the old one.
	 */
	private void updateChart() {
		chart = createChart(createDataset());

		chartPanel.setChart(chart);

		repaint();
	}

	/**
	 * Checks for selections in AttributeName list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		JList list = ((JList) e.getSource());
		AttributeNameListModel model = (AttributeNameListModel) list.getModel();
		int index = list.getSelectedIndex();
		if (index > -1 && !model.isEmpty()) {
			goButton.setEnabled(true);
			targetAttributeName = model.getAttributeNameAt(index);
			chart.getXYPlot().setDataset(createDataset());
			String label = targetAttributeName.getFullname();
			if (label == null || label.trim().length() == 0)
				label = targetAttributeName.getId();
			chart.getXYPlot().getDomainAxis().setLabel(label);
			chart.fireChartChanged();
		}
	}

}
