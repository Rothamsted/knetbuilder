package net.sourceforge.ondex.ovtk2.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;

/**
 * Spring Layouter uses different edge length for relation types.
 * 
 * @author taubertj
 * 
 */
public class SpringRelationTypeLayout extends OVTK2Layouter implements
		IterativeContext, ChangeListener {

	private static final boolean DEBUG = false;

	private ONDEXConcept[] vertices = null;

	private ONDEXRelation[] edges = null;

	// sum of x forces on a node
	private Map<ONDEXConcept, Double> forces_x = LazyMap.decorate(
			new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
				@Override
				public Double create() {
					return Double.valueOf(0);
				}
			});

	// sum of y forces on a node
	private Map<ONDEXConcept, Double> forces_y = LazyMap.decorate(
			new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
				@Override
				public Double create() {
					return Double.valueOf(0);
				}
			});

	// scoring for relation type sets
	private Map<ConceptClass, Map<ConceptClass, Map<RelationType, Double>>> scores = new HashMap<ConceptClass, Map<ConceptClass, Map<RelationType, Double>>>();

	// preferred edge length
	private Map<ONDEXConcept, Map<ONDEXConcept, Double>> length = new HashMap<ONDEXConcept, Map<ONDEXConcept, Double>>();

	// damping factor
	private double c = 0.007;

	// constant for spring
	private double k1 = 20;

	// constant for electrics
	private double k2 = 60000;

	// default edge length
	private double default_length = 75;

	// min edge length
	private double min_length = 50;

	// max edge length
	private double max_length = 200;

	// maximal movement per step
	private int max_vertex_movement = 25;

	// cool network until standstill
	private double cooldown_to = 0.1;

	// global temperature of system
	private double temperature = 0;

	// monitor changes in temperature
	private double last_temp = 0;

	// count iterations
	private int iterations = 0;

	// iterations until cool down of network starts
	private int iterations_until_cooldown = 100;

	// local temperature at each node
	private Map<Integer, Double> heatmap = LazyMap.decorate(
			new HashMap<Integer, Double>(), new Factory<Double>() {
				@Override
				public Double create() {
					return Double.valueOf(0);
				}
			});

	// randomizer used for noise
	private Random rand = new Random();

	// slider default length
	private JSlider sliderDefaultLength = null;

	// slider min length
	private JSlider sliderMinLength = null;

	// slider max length
	private JSlider sliderMaxLength = null;

	// slider iterations until cooldown
	private JSlider sliderCoolDownIter = null;

	// slider cooldown to temp
	private JSlider sliderCoolDownTemp = null;

	// slider damping factor
	private JSlider sliderDamping = null;

	// slider attractive force
	private JSlider sliderAttractive = null;

	// slider repulsive force
	private JSlider sliderRepulsive = null;

	// slider vertex movement
	private JSlider sliderMovement = null;

	/**
	 * Constructor sets OVTK2PropertiesAggregator.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public SpringRelationTypeLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		// deterministic behaviour
		rand.setSeed(1);
	}

	/**
	 * For now, we pretend it never finishes.
	 */
	public boolean done() {
		return false;
	}

	public void initialize() {
		if (graph != null) {
			// get vertices and edges from graph
			vertices = graph.getVertices().toArray(new ONDEXConcept[0]);
			edges = graph.getEdges().toArray(new ONDEXRelation[0]);
		}
		Dimension dim = getSize();
		if (graph != null && dim != null && iterations == 0) {

			// clear length and forces
			forces_x.clear();
			forces_y.clear();
			length.clear();
			scores.clear();
			iterations = 0;

			// randomize position, but deterministic behaviour
			setInitializer(new RandomLocationTransformer<ONDEXConcept>(dim, 1));

			// initialize rt scores
			for (ONDEXRelation e : edges) {
				ConceptClass sourceCC = graph.getSource(e).getOfType();
				ConceptClass destCC = graph.getDest(e).getOfType();
				RelationType rt = e.getOfType();
				if (!scores.containsKey(sourceCC))
					scores.put(
							sourceCC,
							new HashMap<ConceptClass, Map<RelationType, Double>>());
				if (!scores.get(sourceCC).containsKey(destCC))
					scores.get(sourceCC).put(
							destCC,
							LazyMap.decorate(
									new HashMap<RelationType, Double>(),
									new Factory<Double>() {
										@Override
										public Double create() {
											return Double.valueOf(0);
										}
									}));
				scores.get(sourceCC)
						.get(destCC)
						.put(rt, scores.get(sourceCC).get(destCC).get(rt) + 1.0);
			}

			// get min and max edge lengths
			double min = Double.MAX_VALUE;
			double max = 0.0;
			for (ConceptClass source : scores.keySet()) {
				for (ConceptClass dest : scores.get(source).keySet()) {
					for (RelationType rt : scores.get(source).get(dest)
							.keySet()) {
						double value = scores.get(source).get(dest).get(rt);
						// inverse scale of default_length
						value = Math.log((double) edges.length / value);
						scores.get(source).get(dest).put(rt, value);
						if (value < min)
							min = value;
						if (value > max)
							max = value;
					}
				}
			}

			Set<Double> lengths = new HashSet<Double>();

			// initialise edge length
			for (ONDEXRelation e : edges) {
				ONDEXConcept source = graph.getSource(e);
				ONDEXConcept dest = graph.getDest(e);
				if (!length.containsKey(source))
					length.put(source, new HashMap<ONDEXConcept, Double>());
				if (!length.containsKey(dest))
					length.put(dest, new HashMap<ONDEXConcept, Double>());
				ConceptClass sourceCC = source.getOfType();
				ConceptClass destCC = dest.getOfType();
				RelationType rt = e.getOfType();
				// calculate ratio of default length
				double actual_length = scores.get(sourceCC).get(destCC).get(rt);
				// scale for min_length to max_length
				if (min != max)
					actual_length = ((actual_length - min) / (max - min))
							* (max_length - min_length) + min_length;
				if (actual_length == 0)
					actual_length = default_length;
				lengths.add(actual_length);
				// make undirected edges
				length.get(source).put(dest, actual_length);
			}
			if (DEBUG)
				System.out.println("Edge lengths: " + lengths);
		}
	}

	/**
	 * This one is an incremental visualization
	 */
	public boolean isIncremental() {
		return true;
	}

	public void reset() {
		iterations = 0;
		initialize();
	}

	/**
	 * Random function returns an random double value.
	 * 
	 * @return double
	 */
	private double rand() {
		return rand.nextDouble();
	}

	/**
	 * Relaxation step. Moves all nodes a smidge.
	 */
	public void step() {
		try {
			if (vertices != null) {

				temperature = 0;
				heatmap.clear();

				// calculate current temperature of system
				for (ONDEXConcept u : vertices) {
					for (ONDEXConcept v : vertices) {
						// get current coordinates
						double dx = getX(v) - getX(u);
						double dy = getY(v) - getY(u);

						// calculate euclidian distance approx.
						double absdx = Math.abs(dx);
						double absdy = Math.abs(dy);
						double d = 0;
						if (absdy > absdx)
							d = 0.41 * absdx + 0.941246 * absdy;
						else
							d = 0.941246 * absdx + 0.41 * absdy;

						int vid = v.getId();
						int uid = u.getId();

						// connecting edge
						if (length.containsKey(vid)
								&& length.get(vid).containsKey(uid)) {

							double l = length.get(vid).get(uid);
							double abs = Math.abs(d - l);
							temperature += abs;
							// local heat at concerned nodes
							heatmap.put(vid, heatmap.get(vid) + abs);
							heatmap.put(uid, heatmap.get(uid) + abs);
						}
					}
				}

				// cool network globally after max of iterations
				double cooldown = 1;
				if (iterations > iterations_until_cooldown) {
					cooldown = (double) iterations_until_cooldown
							/ (double) iterations;
					temperature = temperature * cooldown;
				}

				// if not cool enough
				if (cooldown > cooldown_to) {
					if (DEBUG)
						System.out.println("Temperature: " + temperature
								+ " Cooldown: " + cooldown);
					for (ONDEXConcept u : vertices) {
						for (ONDEXConcept v : vertices) {

							// get current coordinates
							double dx = getX(v) - getX(u);
							double dy = getY(v) - getY(u);

							// calculate euclidian distance approx.
							double absdx = Math.abs(dx);
							double absdy = Math.abs(dy);
							double d = 0;
							if (absdy > absdx)
								d = 0.41 * absdx + 0.941246 * absdy;
							else
								d = 0.941246 * absdx + 0.41 * absdy;

							// skip selfmovement
							if (!v.equals(u)) {

								double force_x = k2 / (d * d) * (dx / d);
								double force_y = k2 / (d * d) * (dy / d);

								forces_x.put(v, forces_x.get(v) + force_x);
								forces_y.put(v, forces_y.get(v) + force_y);
								forces_x.put(u, forces_x.get(u) - force_x);
								forces_y.put(u, forces_y.get(u) - force_y);

								// connecting edge as spring
								if (length.containsKey(v)
										&& length.get(v).containsKey(u)) {

									double l = length.get(v).get(u);

									// heat depends on edge lengths
									double heat = heatmap.get(v)
											+ heatmap.get(u) / 2;
									double randX = rand() - 0.5;
									double randY = rand() - 0.5;

									// introduce some random noise
									double rand_x = c * randX * temperature
											* heat;
									double rand_y = c * randY * temperature
											* heat;

									force_x = k1 * (d - l) * (dx / d) + rand_x;
									force_y = k1 * (d - l) * (dy / d) + rand_y;

									forces_x.put(v, forces_x.get(v) - force_x);
									forces_y.put(v, forces_y.get(v) - force_y);
									forces_x.put(u, forces_x.get(u) + force_x);
									forces_y.put(u, forces_y.get(u) + force_y);
								}
							}
						}
					}

					// Move by the given force
					for (ONDEXConcept vertex : vertices) {
						double xmove = c * forces_x.get(vertex);
						double ymove = c * forces_y.get(vertex);

						double max = max_vertex_movement;
						if (xmove > max)
							xmove = max;
						if (xmove < -1 * max)
							xmove = -1 * max;
						if (ymove > max)
							ymove = max;
						if (ymove < -1 * max)
							ymove = -1 * max;

						Point2D coord = transform(vertex);
						coord.setLocation(coord.getX() + xmove, coord.getY()
								+ ymove);
						forces_x.put(vertex, 0.0);
						forces_y.put(vertex, 0.0);
					}
					iterations++;
				} else {
					// check if there are still turbulences in the network
					if (Math.abs(temperature - last_temp) > 4) {
						// start cooldown again
						iterations -= iterations / 4;
					}
				}
				last_temp = temperature;
			}
		} catch (ConcurrentModificationException cme) {
			step();
		}
	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// adjust damping factor
		sliderDamping = new JSlider();
		sliderDamping.setBorder(BorderFactory
				.createTitledBorder("Damping factor"));
		sliderDamping.setMinimum(0);
		sliderDamping.setMaximum(100);
		sliderDamping.setValue((int) (c * 1000));
		sliderDamping.setMajorTickSpacing(20);
		sliderDamping.setMinorTickSpacing(5);
		sliderDamping.setPaintTicks(true);
		sliderDamping.setPaintLabels(true);
		sliderDamping.addChangeListener(this);

		// adjust vertex movement
		sliderMovement = new JSlider();
		sliderMovement.setBorder(BorderFactory
				.createTitledBorder("Vertex max movement"));
		sliderMovement.setMinimum(0);
		sliderMovement.setMaximum(200);
		sliderMovement.setValue(max_vertex_movement);
		sliderMovement.setMajorTickSpacing(40);
		sliderMovement.setMinorTickSpacing(10);
		sliderMovement.setPaintTicks(true);
		sliderMovement.setPaintLabels(true);
		sliderMovement.addChangeListener(this);

		// forces options
		JPanel forces = new JPanel();
		BoxLayout forcesLayout = new BoxLayout(forces, BoxLayout.PAGE_AXIS);
		forces.setLayout(forcesLayout);
		forces.setBorder(BorderFactory.createTitledBorder("Forces"));

		sliderAttractive = new JSlider();
		sliderAttractive.setBorder(BorderFactory
				.createTitledBorder("attractive"));
		sliderAttractive.setMinimum(0);
		sliderAttractive.setMaximum(100);
		sliderAttractive.setValue((int) k1);
		sliderAttractive.setMajorTickSpacing(20);
		sliderAttractive.setMinorTickSpacing(5);
		sliderAttractive.setPaintTicks(true);
		sliderAttractive.setPaintLabels(true);
		sliderAttractive.addChangeListener(this);
		forces.add(sliderAttractive);

		sliderRepulsive = new JSlider();
		sliderRepulsive
				.setBorder(BorderFactory.createTitledBorder("repulsive"));
		sliderRepulsive.setMinimum(0);
		sliderRepulsive.setMaximum(100);
		sliderRepulsive.setValue((int) k2 / 1000);
		sliderRepulsive.setMajorTickSpacing(20);
		sliderRepulsive.setMinorTickSpacing(5);
		sliderRepulsive.setPaintTicks(true);
		sliderRepulsive.setPaintLabels(true);
		sliderRepulsive.addChangeListener(this);
		forces.add(sliderRepulsive);

		// edge length options
		JPanel length = new JPanel();
		BoxLayout lengthLayout = new BoxLayout(length, BoxLayout.PAGE_AXIS);
		length.setLayout(lengthLayout);
		length.setBorder(BorderFactory.createTitledBorder("Edge length"));

		sliderDefaultLength = new JSlider();
		sliderDefaultLength.setBorder(BorderFactory
				.createTitledBorder("default"));
		sliderDefaultLength.setMinimum(50);
		sliderDefaultLength.setMaximum(250);
		sliderDefaultLength.setValue((int) default_length);
		sliderDefaultLength.setMajorTickSpacing(40);
		sliderDefaultLength.setMinorTickSpacing(10);
		sliderDefaultLength.setPaintTicks(true);
		sliderDefaultLength.setPaintLabels(true);
		sliderDefaultLength.addChangeListener(this);
		length.add(sliderDefaultLength);

		sliderMinLength = new JSlider();
		sliderMinLength.setBorder(BorderFactory.createTitledBorder("min"));
		sliderMinLength.setMinimum(50);
		sliderMinLength.setMaximum(250);
		sliderMinLength.setValue((int) min_length);
		sliderMinLength.setMajorTickSpacing(40);
		sliderMinLength.setMinorTickSpacing(10);
		sliderMinLength.setPaintTicks(true);
		sliderMinLength.setPaintLabels(true);
		sliderMinLength.addChangeListener(this);
		length.add(sliderMinLength);

		sliderMaxLength = new JSlider();
		sliderMaxLength.setBorder(BorderFactory.createTitledBorder("max"));
		sliderMaxLength.setMinimum(50);
		sliderMaxLength.setMaximum(250);
		sliderMaxLength.setValue((int) max_length);
		sliderMaxLength.setMajorTickSpacing(40);
		sliderMaxLength.setMinorTickSpacing(10);
		sliderMaxLength.setPaintTicks(true);
		sliderMaxLength.setPaintLabels(true);
		sliderMaxLength.addChangeListener(this);
		length.add(sliderMaxLength);

		// cooldown options
		JPanel cooldown = new JPanel();
		BoxLayout cooldownLayout = new BoxLayout(cooldown, BoxLayout.PAGE_AXIS);
		cooldown.setLayout(cooldownLayout);
		cooldown.setBorder(BorderFactory.createTitledBorder("Cooldown"));

		sliderCoolDownIter = new JSlider();
		sliderCoolDownIter.setBorder(BorderFactory
				.createTitledBorder("iterations until"));
		sliderCoolDownIter.setMinimum(0);
		sliderCoolDownIter.setMaximum(200);
		sliderCoolDownIter.setValue(iterations_until_cooldown);
		sliderCoolDownIter.setMajorTickSpacing(40);
		sliderCoolDownIter.setMinorTickSpacing(10);
		sliderCoolDownIter.setPaintTicks(true);
		sliderCoolDownIter.setPaintLabels(true);
		sliderCoolDownIter.addChangeListener(this);
		cooldown.add(sliderCoolDownIter);

		sliderCoolDownTemp = new JSlider();
		sliderCoolDownTemp.setBorder(BorderFactory
				.createTitledBorder("to temperature"));
		sliderCoolDownTemp.setMinimum(0);
		sliderCoolDownTemp.setMaximum(100);
		sliderCoolDownTemp.setValue((int) (cooldown_to * 100));
		sliderCoolDownTemp.setMajorTickSpacing(20);
		sliderCoolDownTemp.setMinorTickSpacing(5);
		sliderCoolDownTemp.setPaintTicks(true);
		sliderCoolDownTemp.setPaintLabels(true);
		sliderCoolDownTemp.addChangeListener(this);
		cooldown.add(sliderCoolDownTemp);

		panel.add(sliderDamping);
		panel.add(sliderMovement);
		panel.add(forces);
		panel.add(length);
		panel.add(cooldown);

		return panel;
	}

	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(sliderDefaultLength)) {
			default_length = sliderDefaultLength.getValue();
		} else if (arg0.getSource().equals(sliderMinLength)) {
			min_length = sliderMinLength.getValue();
		} else if (arg0.getSource().equals(sliderMaxLength)) {
			max_length = sliderMaxLength.getValue();
		} else if (arg0.getSource().equals(sliderCoolDownIter)) {
			iterations_until_cooldown = sliderCoolDownIter.getValue();
		} else if (arg0.getSource().equals(sliderCoolDownTemp)) {
			cooldown_to = (double) sliderCoolDownTemp.getValue() / 100.0;
		} else if (arg0.getSource().equals(sliderDamping)) {
			c = (double) sliderDamping.getValue() / 1000.0;
		} else if (arg0.getSource().equals(sliderAttractive)) {
			k1 = sliderAttractive.getValue();
		} else if (arg0.getSource().equals(sliderRepulsive)) {
			k2 = sliderRepulsive.getValue() * 1000;
		} else if (arg0.getSource().equals(sliderMovement)) {
			max_vertex_movement = sliderMovement.getValue();
		}
	}
}
