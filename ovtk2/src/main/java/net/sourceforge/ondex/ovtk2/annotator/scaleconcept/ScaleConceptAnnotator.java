package net.sourceforge.ondex.ovtk2.annotator.scaleconcept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint.NodeFillPaintSelection;
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

import org.apache.commons.collections15.Transformer;

/**
 * Annotator to define node size based on quantitative Attribute value
 * 
 * @author hindlem
 */
public class ScaleConceptAnnotator extends OVTK2Annotator implements ListSelectionListener, ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * multiple selection is enabled
	 */
	private List<AttributeName> targets = null;

	/**
	 * displays selection list
	 */
	private JList list;

	/**
	 * statistical cutoff
	 */
	private JList listSig;

	/**
	 * used to wrap attribute name list
	 */
	private AttributeNameListModel anlm;

	/**
	 * the important button
	 */
	private JButton goButton;

	/**
	 * information about value distribution
	 */
	public JInternalFrame histogram;

	/**
	 * Only for use in applet
	 */
	public JFrame frame;

	/**
	 * min node size
	 */
	private JFormattedTextField minField;

	/**
	 * max node size
	 */
	private JFormattedTextField maxField;

	/**
	 * inverse value scaling
	 */
	private JCheckBox inverse;

	/**
	 * use absolute values
	 */
	private JCheckBox absolute;

	/**
	 * only for visible part of graph
	 */
	private JCheckBox onlyvisible;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Main constructor Constructs this JPanel
	 * 
	 * @param viewer
	 *            the viewer to annotate
	 */
	public ScaleConceptAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		anlm = new AttributeNameListModel();

		// node sizes
		JPanel sizeConstraints = new JPanel(new SpringLayout());
		sizeConstraints.add(new JLabel("Min Concept Size"));
		minField = new JFormattedTextField(15);
		minField.setColumns(5);
		sizeConstraints.add(minField);
		sizeConstraints.add(new JLabel("Max Concept Size"));
		maxField = new JFormattedTextField(40);
		maxField.setColumns(5);
		sizeConstraints.add(maxField);
		SpringUtilities.makeCompactGrid(sizeConstraints, sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);

		add(sizeConstraints);

		// use inverse scaling
		inverse = new JCheckBox("Use inverse scaling");
		add(inverse);

		absolute = new JCheckBox("Use absolute values");
		absolute.setToolTipText("Absolute negative values in regards to graph size");
		add(absolute);

		// restrict to visible concepts
		onlyvisible = new JCheckBox("Resize only visible concepts");
		onlyvisible.setToolTipText("Calculate values using only visible concepts");
		add(onlyvisible);

		// the magic button
		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());
		list.setTransferHandler(new ReportingListTransferHandler());
		list.setDragEnabled(true);

		listSig = new JList(anlm);
		listSig.setCellRenderer(new CustomCellRenderer());
		listSig.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// get all available attribute names
		for (AttributeName attName : graph.getMetaData().getAttributeNames()) {
			// check its numerically comparable
			if (Number.class.isAssignableFrom(attName.getDataType())) {
				Set<ONDEXConcept> concepts = graph.getConceptsOfAttributeName(attName);
				if (concepts != null) {
					// check concepts exists on this attribute name
					if (concepts.size() > 0 && !AppearanceSynchronizer.attr.contains(attName.getId())) {
						anlm.addAttributeName(attName);
					}
				}
			}
		}

		// populate list
		if (anlm.getSize() == 0) {
			add(new JLabel("There are no attributes with numerical values in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select an attribute to annotate concepts with"));
			add(new JScrollPane(list));

			listSig.validate();
			listSig.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listSig.addListSelectionListener(this);

			add(new JLabel("Select an AttributeName representing statistical significance cutoff"));
			add(new JScrollPane(listSig));
			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}

	/**
	 * Resizes nodes of the graph.
	 * 
	 * @param targMin
	 *            the smallest node size
	 * @param targMax
	 *            the largest node size
	 * @param inverse
	 *            is this a inverse relationship
	 * @param onlyvisible
	 *            use only visible concepts
	 */
	private void updateGraph(int targMin, int targMax, boolean inverse, boolean onlyvisible) {

		// reset icon transformer, might have been set by annotator
		viewer.getVisualizationViewer().getRenderContext().setVertexIconTransformer(null);

		int index = listSig.getSelectedIndex();
		AttributeName attForSig = null;
		if (index > 0) {
			attForSig = ((AttributeNameListModel) listSig.getModel()).getAttributeNameAt(index);
		}

		if (targets.size() == 1) {

			Map<ONDEXConcept, Double> toAnnotate = new HashMap<ONDEXConcept, Double>();
			Map<ONDEXConcept, Double> values = new HashMap<ONDEXConcept, Double>();

			// get attribute name values
			for (ONDEXConcept concept : graph.getConceptsOfAttributeName(targets.get(0))) {

				// check visibility of concepts
				if (!onlyvisible || graph.isVisible(concept)) {

					// associate concept to value
					Double value = ((Number) concept.getAttribute(targets.get(0)).getValue()).doubleValue();
					values.put(concept, value);

					if (absolute.isSelected())
						value = Math.abs(value);
					toAnnotate.put(concept, value);
				}
			}

			// resize nodes
			Annotation.annotateOnSizeScale(viewer, toAnnotate, targMin, targMax, inverse);

			// transform colour of nodes
			ONDEXNodeFillPaint nodeColors = viewer.getNodeColors();
			nodeColors.setFillPaintSelection(NodeFillPaintSelection.MANUAL);
			for (ONDEXConcept node : values.keySet()) {
				Double value = values.get(node);
				if (value == 0)
					nodeColors.updateColor(node, Color.YELLOW);
				else if (value < 0)
					nodeColors.updateColor(node, Color.GREEN);
				else
					nodeColors.updateColor(node, Color.RED);
			}

			// update viewer
			viewer.getVisualizationViewer().fireStateChanged();
			viewer.getVisualizationViewer().repaint();

			// remove previous histogram
			if (histogram != null) {
				try {
					histogram.setClosed(true);
					// should not be called in applet
					OVTK2Desktop.getInstance().getDesktopPane().remove(histogram);
				} catch (PropertyVetoException pve) {
					pve.printStackTrace();
				}
			}

			// draw histogram
			JLabel graphic = new Histogram(targets.get(0));
			if (Config.isApplet) {
				if (frame != null) {
					frame.setVisible(false);
					frame.dispose();
				}
				frame = new JFrame("Histogram");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.add(graphic);
				frame.pack();
				frame.setVisible(true);
			} else {
				// show value distribution
				histogram = new RegisteredJInternalFrame("Histogram", "Annotator", this.getName() + " - " + viewer.getTitle() + " (Histogram)", false, true, false, true);
				histogram.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
				histogram.add(graphic);
				OVTK2Desktop.getInstance().getDesktopPane().add(histogram);
				histogram.pack();
				histogram.setVisible(true);
				histogram.toFront();
			}
		} else {

			// first dimension of data, category
			Map<Integer, Map<ONDEXConcept, Double>> values = new HashMap<Integer, Map<ONDEXConcept, Double>>();
			Set<ONDEXConcept> conceptsToUpdate = new HashSet<ONDEXConcept>();

			Map<ONDEXConcept, Double> significanceMap = new HashMap<ONDEXConcept, Double>();

			for (int cat = 0; cat < targets.size(); cat++) {
				// get attribute name values
				Map<ONDEXConcept, Double> temp = new HashMap<ONDEXConcept, Double>();
				for (ONDEXConcept concept : graph.getConceptsOfAttributeName(targets.get(cat))) {

					// check visibility of concepts
					if (!onlyvisible || graph.isVisible(concept)) {
						Double value = ((Number) concept.getAttribute(targets.get(cat)).getValue()).doubleValue();
						temp.put(concept, value);

						if (attForSig != null) {
							Double sigValue = significanceMap.get(concept);
							if (sigValue == null) {
								Attribute sigAttribute = concept.getAttribute(attForSig);
								if (sigAttribute != null) {
									sigValue = ((Number) sigAttribute.getValue()).doubleValue();
									significanceMap.put(concept, sigValue);
								}
							}
						}

						conceptsToUpdate.add(concept);
					}
				}
				values.put(cat, temp);
			}

			// get icon transformer for pie charts
			Transformer<ONDEXConcept, Icon> vertexIconTransformer = new PieNodeIconTransformer(values, significanceMap, targMin, targMax, absolute.isSelected());

			// TODO: use updateViewer
			viewer.getVisualizationViewer().getRenderContext().setVertexIconTransformer(vertexIconTransformer);
			viewer.getVisualizationViewer().getModel().fireStateChanged();
			viewer.getVisualizationViewer().repaint();
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ScaleConcept");
	}

	/**
	 * Checks for selections in ConceptClass list.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);

			List<AttributeName> attNames = new ArrayList<AttributeName>(indices.length);
			for (int i : indices) {
				attNames.add(((AttributeNameListModel) list.getModel()).getAttributeNameAt(i));
			}

			targets = attNames;
		}
	}

	/**
	 * Associated with go button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (targets.size() > 0) {
			Integer min = (Integer) minField.getValue();
			Integer max = (Integer) maxField.getValue();
			if (min < 0) {
				min = 0;
				minField.setValue(min);
			}
			if (max < 0) {
				max = 0;
				maxField.setValue(max);
			}

			// swap values
			if (min > max) {
				Integer temp = min;
				min = max;
				max = temp;
				minField.setValue(min);
				maxField.setValue(max);
			}

			updateGraph(min, max, inverse.isSelected(), onlyvisible.isSelected());

			used = true;
		}
	}

	/**
	 * Creates a histogram of the values distribution of a given attribute name.
	 * 
	 * @author taubertj
	 */
	class Histogram extends JLabel {

		// generated
		private static final long serialVersionUID = -2765521540678267337L;

		// cache number values
		Map<ONDEXConcept, Number> values = new Hashtable<ONDEXConcept, Number>();

		// 256 different degrees of colour
		int[] bins = new int[256];

		// scale max NB to height of window
		int maxnb = 0;

		// set height
		int height = 200;

		float scaleY = 0;

		/**
		 * Sets attribute name to extract values from.
		 * 
		 * @param an
		 *            AttributeName
		 */
		public Histogram(AttributeName an) {
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = c.getAttribute(an);
				values.put(c, (Number) attribute.getValue());
			}

			doBins();
		}

		/**
		 * Get range boundaries and bin values.
		 */
		private void doBins() {
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;

			for (Number number : values.values()) {
				float value = number.floatValue();
				if (value < min) {
					min = value;
				}
				if (value > max) {
					max = value;
				}
			}

			// maximum number in bin
			maxnb = 0;

			// scale to [0,bins.length-1] to sort into bins
			float diff = max - min;
			if (diff != 0) {
				Iterator<Number> it = values.values().iterator();
				while (it.hasNext()) {
					float value = it.next().floatValue();
					value = (value - min) / diff * (bins.length - 1);
					int index = Math.round(value);
					bins[index]++;
					if (bins[index] > maxnb)
						maxnb = bins[index];
				}

				scaleY = (float) height / (float) maxnb;
			}

			// System.out.println(min+" to "+max+" = "+maxnb+" / " +scaleY);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			// anti-alias painting
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// draw white background
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			for (int i = 0; i < bins.length; i++) {
				if (i < (bins.length - 1) / 2)
					g.setColor(Color.GREEN);
				else
					g.setColor(Color.RED);
				int l = Math.round((float) bins[i] * scaleY);
				g.drawRect(i, height - l, 1, l);
			}
		}

		@Override
		public int getHeight() {
			return height + 1;
		}

		@Override
		public int getWidth() {
			return bins.length + 1;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(getWidth(), getHeight());
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}