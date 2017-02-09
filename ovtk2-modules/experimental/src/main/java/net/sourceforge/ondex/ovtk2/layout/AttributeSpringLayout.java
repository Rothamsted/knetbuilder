package net.sourceforge.ondex.ovtk2.layout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

/**
 * Layout based on the SpringLayout2 from JUNG taking Attribute values into
 * account for edge weights.
 * 
 * @author taubertj
 * @version 03.10.2012
 */
public class AttributeSpringLayout extends OVTK2Layouter implements
		ActionListener, ChangeListener, IterativeContext {

	/**
	 * Class for wrapping edge legnth returned from Attribute.
	 * 
	 * @author taubertj
	 * @version 03.10.2012
	 */
	private class EdgeLength implements Transformer<ONDEXRelation, Integer> {

		// cache for edge length
		Map<ONDEXRelation, Integer> cache;

		// maximum for scaling
		double maximum = Double.NEGATIVE_INFINITY;

		// minimum for scaling
		double minimum = Double.POSITIVE_INFINITY;

		/**
		 * Extract edge weights from Attribute for a given AttributeName.
		 * 
		 * @param an
		 *            AttributeName
		 * @param inverse
		 *            boolean
		 */
		public EdgeLength(AttributeName an, boolean inverse) {
			ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;
			cache = new HashMap<ONDEXRelation, Integer>();

			// calculate normalisation per rt
			Map<RelationType, Map<ONDEXRelation, Double>> temp = new HashMap<RelationType, Map<ONDEXRelation, Double>>();

			// iterate over all relations that have this Attribute
			for (ONDEXRelation r : aog.getRelationsOfAttributeName(an)) {
				RelationType rt = r.getOfType();
				Attribute attribute = r.getAttribute(an);
				// add cast to Number to cache
				if (!temp.containsKey(rt))
					temp.put(rt, new HashMap<ONDEXRelation, Double>());
				temp.get(rt).put(r,
						((Number) attribute.getValue()).doubleValue());
			}

			// process every rt
			for (RelationType rt : temp.keySet()) {

				// get minimum and maximum
				for (ONDEXRelation key : temp.get(rt).keySet()) {
					double value = temp.get(rt).get(key);
					if (value < minimum)
						minimum = value;
					if (value > maximum)
						maximum = value;
				}

				// scale to [0,1]
				double diff = maximum - minimum;
				double value, newvalue;
				if (diff != 0) {
					for (ONDEXRelation key : temp.get(rt).keySet()) {
						value = temp.get(rt).get(key);
						// normalise length of edge
						if (inverse)
							newvalue = 2 - ((value - minimum) / diff);
						else
							newvalue = 1 + ((value - minimum) / diff);
						newvalue *= UNITLENGTHFUNCTION.transform(key);
						// System.out.println(rt.getId() + ": " + value + " ->"
						// + newvalue);
						cache.put(key, (int) newvalue);
					}
				}
			}
		}

		@Override
		public Integer transform(ONDEXRelation e) {
			if (!cache.containsKey(e))
				return UNITLENGTHFUNCTION.transform(e);
			return cache.get(e);
		}
	}

	// default edge length
	public static final Transformer<ONDEXRelation, Integer> UNITLENGTHFUNCTION = new ConstantTransformer(
			20);

	// attribute name for length value
	private AttributeName an = null;

	// value from JUNG
	protected double force_multiplier = 1.0 / 3.0;

	// checkbox to normalise length of edges
	private JCheckBox inverseScaleBox = null;

	/**
	 * Wrapped JUNG layout
	 */
	private SpringLayout2<ONDEXConcept, ONDEXRelation> layout = null;

	// value from JUNG
	protected int repulsion_range = 100 * 100;

	// for increasing canvas size
	private JSlider sizeMultiplier = null;

	// slider for force
	private JSlider sliderForce = null;

	// slider for repulsion range
	private JSlider sliderRepulsive = null;

	// slider for stretch
	private JSlider sliderStretch = null;

	// value from JUNG
	protected double stretch = 0.70;

	/**
	 * Initialises unit distance measure.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public AttributeSpringLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph);
	}

	/**
	 * Check for selection of an AttributeName.
	 */
	public void actionPerformed(ActionEvent arg0) {
		ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;
		JComboBox box = (JComboBox) arg0.getSource();
		String name = (String) box.getSelectedItem();
		an = aog.getMetaData().getAttributeName(name);
		if (an == null) {
			layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph,
					UNITLENGTHFUNCTION);
		} else {
			layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph,
					new EdgeLength(an, inverseScaleBox.isSelected()));
		}
		// to scale layout a bit larger
		Dimension newSize = new Dimension(size.width
				* sizeMultiplier.getValue(), size.height
				* sizeMultiplier.getValue());
		layout.setForceMultiplier((double) sliderForce.getValue() / 100.0);
		layout.setRepulsionRange(sliderRepulsive.getValue());
		layout.setStretch((double) sliderStretch.getValue() / 100.0);
		layout.setSize(newSize);
	}

	@Override
	public boolean done() {
		return layout.done();
	}

	@Override
	public Graph<ONDEXConcept, ONDEXRelation> getGraph() {
		return layout.getGraph();
	}

	@Override
	public JPanel getOptionPanel() {

		ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;

		// new option panel
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// ComboBox for attribute names
		JComboBox box = new JComboBox();
		box.addItem("None");
		box.setSelectedIndex(0);

		Set<String> ans = new HashSet<String>();
		for (AttributeName an : aog.getMetaData().getAttributeNames()) {
			Class<?> cl = an.getDataType();
			if (cl != null && Number.class.isAssignableFrom(cl)) {
				if (aog.getRelationsOfAttributeName(an).size() > 0
						&& !AppearanceSynchronizer.attr.contains(an.getId()))
					ans.add(an.getId());
			}
		}

		// sort attribute names
		String[] sorted = ans.toArray(new String[0]);
		Arrays.sort(sorted);
		for (String s : sorted)
			box.addItem(s);

		box.addActionListener(this);
		panel.add(new JLabel("Select AttributeName:"));
		panel.add(box);

		inverseScaleBox = new JCheckBox("inverse scaling");
		inverseScaleBox.setSelected(false);
		inverseScaleBox.addChangeListener(this);
		panel.add(inverseScaleBox);

		sizeMultiplier = new JSlider();
		sizeMultiplier = new JSlider();
		sizeMultiplier.setBorder(BorderFactory
				.createTitledBorder("size multiplier"));
		sizeMultiplier.setMinimum(0);
		sizeMultiplier.setMaximum(100);
		sizeMultiplier.setValue(1);
		sizeMultiplier.setMajorTickSpacing(20);
		sizeMultiplier.setMinorTickSpacing(5);
		sizeMultiplier.setPaintTicks(true);
		sizeMultiplier.setPaintLabels(true);
		sizeMultiplier.addChangeListener(this);
		panel.add(sizeMultiplier);

		sliderStretch = new JSlider();
		sliderStretch.setBorder(BorderFactory
				.createTitledBorder("stretch multiplier"));
		sliderStretch.setMinimum(0);
		sliderStretch.setMaximum(100);
		sliderStretch.setValue((int) (stretch * 100));
		sliderStretch.setMajorTickSpacing(20);
		sliderStretch.setMinorTickSpacing(5);
		sliderStretch.setPaintTicks(true);
		sliderStretch.setPaintLabels(true);
		sliderStretch.addChangeListener(this);
		panel.add(sliderStretch);

		sliderForce = new JSlider();
		sliderForce.setBorder(BorderFactory
				.createTitledBorder("force multiplier"));
		sliderForce.setMinimum(0);
		sliderForce.setMaximum(100);
		sliderForce.setValue((int) (force_multiplier * 100));
		sliderForce.setMajorTickSpacing(20);
		sliderForce.setMinorTickSpacing(5);
		sliderForce.setPaintTicks(true);
		sliderForce.setPaintLabels(true);
		sliderForce.addChangeListener(this);
		panel.add(sliderForce);

		sliderRepulsive = new JSlider();
		sliderRepulsive.setBorder(BorderFactory
				.createTitledBorder("repulsion range"));
		sliderRepulsive.setMinimum(0);
		sliderRepulsive.setMaximum(50000);
		sliderRepulsive.setValue(repulsion_range);
		sliderRepulsive.setMajorTickSpacing(10000);
		sliderRepulsive.setMinorTickSpacing(2000);
		sliderRepulsive.setPaintTicks(true);
		sliderRepulsive.setPaintLabels(true);
		sliderRepulsive.addChangeListener(this);
		panel.add(sliderRepulsive);

		return panel;
	}

	@Override
	public Dimension getSize() {
		return super.getSize();
	}

	@Override
	public double getX(ONDEXConcept v) {
		return layout.getX(v);
	}

	@Override
	public double getY(ONDEXConcept v) {
		return layout.getY(v);
	}

	@Override
	public void initialize() {
		layout.initialize();
	}

	/**
	 * This one is an incremental visualisation
	 */
	public boolean isIncremental() {
		return layout.isIncremental();
	}

	@Override
	public boolean isLocked(ONDEXConcept v) {
		return layout.isLocked(v);
	}

	@Override
	public void lock(boolean lock) {
		layout.lock(lock);
	}

	@Override
	public void lock(ONDEXConcept v, boolean state) {
		layout.lock(v, state);
	}

	@Override
	public void reset() {
		layout.reset();
	}

	@Override
	public void setGraph(Graph<ONDEXConcept, ONDEXRelation> graph) {
		layout.setGraph(graph);
	}

	@Override
	public void setInitializer(Transformer<ONDEXConcept, Point2D> initializer) {
		layout.setInitializer(initializer);
	}

	@Override
	public void setLocation(ONDEXConcept picked, double x, double y) {
		layout.setLocation(picked, x, y);
	}

	@Override
	public void setLocation(ONDEXConcept picked, Point2D p) {
		layout.setLocation(picked, p);
	}

	@Override
	public void setSize(Dimension size) {
		super.setSize(size);
		// to scale layout a bit larger
		if (sizeMultiplier != null) {
			Dimension newSize = new Dimension(size.width
					* sizeMultiplier.getValue(), size.height
					* sizeMultiplier.getValue());
			layout.setSize(newSize);
		} else {
			layout.setSize(size);
		}
	}

	@Override
	public void setViewer(OVTK2PropertiesAggregator viewer) {
		super.setViewer(viewer);
		layout.setGraph(viewer.getONDEXJUNGGraph());
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (layout.done()) {
			// start a new layout for changed settings
			Dimension newSize = new Dimension(size.width
					* sizeMultiplier.getValue(), size.height
					* sizeMultiplier.getValue());
			if (an != null)
				layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph,
						new EdgeLength(an, inverseScaleBox.isSelected()));
			else
				layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph,
						UNITLENGTHFUNCTION);
			layout.setForceMultiplier((double) sliderForce.getValue() / 100.0);
			layout.setRepulsionRange(sliderRepulsive.getValue());
			layout.setStretch((double) sliderStretch.getValue() / 100.0);
			layout.setSize(newSize);
		} else if (arg0.getSource().equals(sliderForce)) {
			layout.setForceMultiplier((double) sliderForce.getValue() / 100.0);
		} else if (arg0.getSource().equals(sliderRepulsive)) {
			layout.setRepulsionRange(sliderRepulsive.getValue());
		} else if (arg0.getSource().equals(sliderStretch)) {
			layout.setStretch((double) sliderStretch.getValue() / 100.0);
		} else if (arg0.getSource().equals(inverseScaleBox)) {
			if (an != null)
				layout = new SpringLayout2<ONDEXConcept, ONDEXRelation>(graph,
						new EdgeLength(an, inverseScaleBox.isSelected()));
		} else if (arg0.getSource().equals(sizeMultiplier)) {
			// to scale layout a bit larger
			Dimension newSize = new Dimension(size.width
					* sizeMultiplier.getValue(), size.height
					* sizeMultiplier.getValue());
			layout.setSize(newSize);
		}
	}

	@Override
	public void step() {
		try {
			layout.step();
		} catch (NullPointerException npe) {
			step();
		}
	}

	@Override
	public Point2D transform(ONDEXConcept v) {
		return layout.transform(v);
	}

}
