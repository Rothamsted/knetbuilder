package net.sourceforge.ondex.ovtk2.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

public class SugiyamaLayout extends OVTK2Layouter implements ChangeListener {

	private static final boolean DEBUG = false;

	private int spacing_x = 50;

	private int spacing_y = 100;

	private int max_iterations = 1000;

	private int max_spacing_x = 200;

	private Map<ONDEXConcept, Set<ONDEXConcept>> out_edges = null;

	private Map<ONDEXConcept, Set<ONDEXConcept>> in_edges = null;

	private Map<Integer, List<ONDEXConcept>> levels = null;

	private Map<ONDEXConcept, Integer> positions = null;

	// slider spacing x
	private JSlider sliderSpacingX = null;

	// slider spacing y
	private JSlider sliderSpacingY = null;

	// slider max spacing x
	private JSlider sliderMaxSpacingX = null;

	// slider max iterations
	private JSlider sliderMaxIterations = null;

	public SugiyamaLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
	}

	/**
	 * Topological search ignoring back edges.
	 * 
	 * @param tree
	 *            nodes on each level
	 * @param visited
	 *            marked nodes
	 * @param root
	 *            current root
	 * @param level
	 *            current level
	 */
	private void dfs(Map<ONDEXConcept, Integer> tree,
			Set<ONDEXConcept> visited, ONDEXConcept root, int level) {
		if (!visited.contains(root)) {
			visited.add(root);
			tree.put(root, level);
			for (ONDEXConcept to : out_edges.get(root)) {
				dfs(tree, visited, to, level + 1);
			}
		}
	}

	/**
	 * Dummy concept implementation.
	 * 
	 * @author taubertj
	 * 
	 */
	private class DummyConcept implements ONDEXConcept {

		@Override
		public void addEvidenceType(EvidenceType evidencetype) {
		}

		@Override
		public void addTag(ONDEXConcept ac) throws AccessDeniedException,
				NullValueException {
		}

		@Override
		public Attribute createAttribute(AttributeName attrname, Object value,
				boolean doIndex) throws AccessDeniedException,
				NullValueException {
			return null;
		}

		@Override
		public boolean deleteAttribute(AttributeName attrname) {
			return false;
		}

		@Override
		public Attribute getAttribute(AttributeName attrname) {
			return null;
		}

		@Override
		public Set<Attribute> getAttributes() {
			return null;
		}

		@Override
		public Set<EvidenceType> getEvidence() {
			return null;
		}

		@Override
		public int getId() {
			return 0;
		}

		@Override
		public Set<ONDEXConcept> getTags() {
			return null;
		}

		@Override
		public boolean removeEvidenceType(EvidenceType evidencetype) {
			return false;
		}

		@Override
		public boolean removeTag(ONDEXConcept ac) {
			return false;
		}

		@Override
		public long getSID() {
			return 0;
		}

		@Override
		public boolean inheritedFrom(ConceptClass h) {
			return false;
		}

		@Override
		public ConceptClass getOfType() {
			return null;
		}

		@Override
		public ConceptAccession createConceptAccession(String accession,
				DataSource elementOf, boolean ambiguous) {
			return null;
		}

		@Override
		public ConceptName createConceptName(String name, boolean isPreferred) {
			return null;
		}

		@Override
		public boolean deleteConceptAccession(String accession,
				DataSource elementOf) {
			return false;
		}

		@Override
		public boolean deleteConceptName(String name) {
			return false;
		}

		@Override
		public String getAnnotation() {
			return null;
		}

		@Override
		public ConceptAccession getConceptAccession(String accession,
				DataSource elementOf) {
			return null;
		}

		@Override
		public Set<ConceptAccession> getConceptAccessions() {
			return null;
		}

		@Override
		public ConceptName getConceptName() {
			return null;
		}

		@Override
		public ConceptName getConceptName(String name) {
			return null;
		}

		@Override
		public Set<ConceptName> getConceptNames() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public DataSource getElementOf() {
			return null;
		}

		@Override
		public String getPID() {
			return null;
		}

		@Override
		public void setPID(String pid) {

		}

		@Override
		public void setAnnotation(String annotation) {

		}

		@Override
		public void setDescription(String description) {

		}

		@Override
		public String toString() {
			return "D";
		}
	}

	/**
	 * Inserting dummies on levels in between two node on more distant levels
	 * than 1.
	 * 
	 * @param templevels
	 *            Map<ONDEXConcept, Integer>
	 */
	private void insertDummies(Map<ONDEXConcept, Integer> templevels) {
		// insert dummy nodes
		for (ONDEXConcept v : templevels.keySet()) {
			int level_v = templevels.get(v);
			for (ONDEXConcept u : out_edges.get(v).toArray(new ONDEXConcept[0])) {
				int level_u = templevels.get(u);
				int level_diff = level_u - level_v;
				// nodes u and v are not on neighbour levels
				if (level_diff > 1) {
					// remove edge v,u
					out_edges.get(v).remove(u);
					in_edges.get(u).remove(v);
					// create dummy following v
					ONDEXConcept dummy = new DummyConcept();
					out_edges.get(v).add(dummy);
					in_edges.put(dummy, new HashSet<ONDEXConcept>());
					in_edges.get(dummy).add(v);
					// hack for null pointer exception when not all levels
					// filled
					if (!levels.containsKey(level_v + 1))
						levels.put(level_v + 1, new ArrayList<ONDEXConcept>());
					levels.get(level_v + 1).add(dummy);
					// create all dummies in between
					for (int i = 1; i < level_diff - 1; i++) {
						ONDEXConcept newdummy = new DummyConcept();
						out_edges.put(dummy, new HashSet<ONDEXConcept>());
						out_edges.get(dummy).add(newdummy);
						in_edges.put(newdummy, new HashSet<ONDEXConcept>());
						in_edges.get(newdummy).add(dummy);
						// hack for null pointer exception when not all levels
						// filled
						if (!levels.containsKey(level_v + (i + 1)))
							levels.put(level_v + (i + 1),
									new ArrayList<ONDEXConcept>());
						levels.get(level_v + (i + 1)).add(newdummy);
						dummy = newdummy;
					}
					// create edge between last dummy and u
					out_edges.put(dummy, new HashSet<ONDEXConcept>());
					out_edges.get(dummy).add(u);
					in_edges.get(u).add(dummy);
				}
			}
		}
	}

	/**
	 * Scan levels in downward direction and order according to barycenters.
	 * 
	 * @return whether there were changes
	 */
	private boolean downwardScan() {
		boolean change = false;
		// downward scan
		for (int level : levels.keySet()) {
			List<ONDEXConcept> parents = levels.get(level);
			// check if next level is present
			if (levels.containsKey(level + 1)) {
				ONDEXConcept[] oldorder = levels.get(level + 1).toArray(
						new ONDEXConcept[0]);
				Map<ONDEXConcept, Double> barycenter = new HashMap<ONDEXConcept, Double>();
				for (ONDEXConcept node : levels.get(level + 1)) {
					// downward use in_edges (think reverse)
					barycenter.put(node, 0.0);
					int nb = 0;
					for (ONDEXConcept parent : in_edges.get(node)) {
						int index = parents.indexOf(parent);
						if (index > -1) {
							barycenter.put(node, barycenter.get(node) + index);
							nb++;
						}
					}
					if (barycenter.get(node) > 0)
						barycenter.put(node, barycenter.get(node) / nb);
				}
				if (DEBUG)
					System.out.println("Barycenter downwards: " + barycenter);
				// reverse barycenter map
				Map<Double, List<ONDEXConcept>> reverse = new HashMap<Double, List<ONDEXConcept>>();
				for (ONDEXConcept node : barycenter.keySet()) {
					double center = barycenter.get(node);
					if (!reverse.containsKey(center))
						reverse.put(center, new ArrayList<ONDEXConcept>());
					reverse.get(center).add(node);
				}
				if (DEBUG)
					System.out.println("Reverse: " + reverse);
				// sort according to barycenters
				Double[] centers = reverse.keySet().toArray(new Double[0]);
				Arrays.sort(centers);
				// set new order of nodes in level
				levels.put(level + 1, new ArrayList<ONDEXConcept>());
				for (int i = 0; i < centers.length; i++) {
					levels.get(level + 1).addAll(reverse.get(centers[i]));
				}
				ONDEXConcept[] neworder = levels.get(level + 1).toArray(
						new ONDEXConcept[0]);
				change = !Arrays.equals(oldorder, neworder);
			}
		}
		return change;
	}

	/**
	 * Scan levels in upward direction and order according to barycenters.
	 * 
	 * @return whether there were changes
	 */
	private boolean upwardScan() {
		boolean change = false;
		// upward scan
		for (int level = levels.keySet().size() - 1; level >= 0; level--) {
			List<ONDEXConcept> parents = levels.get(level);
			// check if previous level is present
			if (levels.containsKey(level - 1)) {
				ONDEXConcept[] oldorder = levels.get(level - 1).toArray(
						new ONDEXConcept[0]);
				Map<ONDEXConcept, Double> barycenter = new HashMap<ONDEXConcept, Double>();
				for (ONDEXConcept node : levels.get(level - 1)) {
					// upward use out_edges (think reverse)
					barycenter.put(node, 0.0);
					int nb = 0;
					for (ONDEXConcept parent : out_edges.get(node)) {
						if (parents != null) {
							int index = parents.indexOf(parent);
							if (index > -1) {
								barycenter.put(node, barycenter.get(node)
										+ index);
								nb++;
							}
						}
					}
					if (barycenter.get(node) > 0)
						barycenter.put(node, barycenter.get(node) / nb);
				}
				if (DEBUG)
					System.out.println("Barycenter upwards: " + barycenter);
				// reverse barycenter map
				Map<Double, List<ONDEXConcept>> reverse = new HashMap<Double, List<ONDEXConcept>>();
				for (ONDEXConcept node : barycenter.keySet()) {
					double center = barycenter.get(node);
					if (!reverse.containsKey(center))
						reverse.put(center, new ArrayList<ONDEXConcept>());
					reverse.get(center).add(node);
				}
				if (DEBUG)
					System.out.println("Reverse: " + reverse);
				// sort according to barycenters
				Double[] centers = reverse.keySet().toArray(new Double[0]);
				Arrays.sort(centers);
				// set new order of nodes in level
				levels.put(level - 1, new ArrayList<ONDEXConcept>());
				for (int i = 0; i < centers.length; i++) {
					levels.get(level - 1).addAll(reverse.get(centers[i]));
				}
				ONDEXConcept[] neworder = levels.get(level - 1).toArray(
						new ONDEXConcept[0]);
				change = !Arrays.equals(oldorder, neworder);
			}
		}
		return change;
	}

	/**
	 * Rearrange nodes in downward direction according to barycenters. During
	 * downward arrange nodes will be moved closer together.
	 * 
	 * @return whether there were changes
	 */
	private boolean downwardArrange() {
		boolean change = false;
		// downward scan
		for (int level : levels.keySet()) {
			List<ONDEXConcept> parents = levels.get(level);
			// check if next level exists
			if (levels.containsKey(level + 1)) {
				int last_x = 0;
				if (DEBUG) {
					System.out.print("Before Downward " + (level + 1) + ": ");
					for (ONDEXConcept n : levels.get(level + 1)) {
						System.out.print(positions.get(n) + " ");
					}
					System.out.println();
				}
				for (ONDEXConcept n : levels.get(level + 1)) {
					int barycenter = 0;
					int nb = 0;
					// downward use in_edges (think reverse)
					for (ONDEXConcept parent : in_edges.get(n)) {
						if (parents.contains(parent)) {
							barycenter += positions.get(parent);
							nb++;
						}
					}
					if (nb > 1) {
						barycenter = barycenter / nb;
						// keep minimal spacing
						if (barycenter <= last_x + spacing_x) {
							barycenter = last_x + spacing_x;
						}
						// keep maximal spacing
						if (barycenter >= last_x + max_spacing_x) {
							barycenter = last_x + max_spacing_x;
						}
						if (barycenter > positions.get(n)) {
							change = true;
							positions.put(n, barycenter);
						}
					} else {
						if (positions.get(n) <= last_x + spacing_x) {
							barycenter = last_x + spacing_x;
						}
						if (barycenter > positions.get(n)) {
							change = true;
							positions.put(n, barycenter);
						}
					}
					last_x = positions.get(n);
				}
				if (DEBUG) {
					System.out.print("Downward " + (level + 1) + ": ");
					for (ONDEXConcept n : levels.get(level + 1)) {
						System.out.print(positions.get(n) + " ");
					}
					System.out.println();
				}
			}
		}
		return change;
	}

	/**
	 * Rearrange nodes in upward direction according to node spacing. During
	 * upward arrange nodes will be moved further apart.
	 * 
	 * @return whether there were changes
	 */
	private boolean upwardArrange() {
		boolean change = false;
		// upward scan
		for (int level = levels.keySet().size() - 1; level >= 0; level--) {
			List<ONDEXConcept> parents = levels.get(level);
			// check if previous level exists
			if (levels.containsKey(level - 1)) {
				int last_x = 0;
				for (ONDEXConcept n : levels.get(level - 1)) {
					int barycenter = 0;
					int nb = 0;
					// upward use out_edges (think reverse)
					for (ONDEXConcept parent : out_edges.get(n)) {
						if (parents != null && parents.contains(parent)) {
							barycenter += positions.get(parent);
							nb++;
						}
					}
					if (nb > 1) {
						barycenter = barycenter / nb;
						// keep minimal spacing
						if (barycenter <= last_x + spacing_x) {
							barycenter = last_x + spacing_x;
						}
						// keep maximal spacing
						if (barycenter >= last_x + max_spacing_x) {
							barycenter = last_x + max_spacing_x;
						}
						if (barycenter > positions.get(n)) {
							change = true;
							positions.put(n, barycenter);
						}
					} else {
						if (positions.get(n) <= last_x + spacing_x) {
							barycenter = last_x + spacing_x;
						}
						if (barycenter > positions.get(n)) {
							change = true;
							positions.put(n, barycenter);
						}
					}
					last_x = positions.get(n);
				}
				if (DEBUG) {
					System.out.print("Upward " + (level - 1) + ": ");
					for (ONDEXConcept n : levels.get(level - 1)) {
						System.out.print(positions.get(n) + " ");
					}
					System.out.println();
				}
			}
		}
		return change;
	}

	/**
	 * Layout graph.
	 */
	public void initialize() {

		// can't continue if is null
		if (graph == null || viewer == null) {
			return;
		}

		// initialize data structures
		out_edges = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();
		in_edges = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();
		levels = new HashMap<Integer, List<ONDEXConcept>>();
		positions = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(),
				new Factory<Integer>() {

					@Override
					public Integer create() {
						return 0;
					}
				});

		// nodes by integer id
		for (ONDEXConcept n : graph.getVertices()) {
			out_edges.put(n, new HashSet<ONDEXConcept>());
			in_edges.put(n, new HashSet<ONDEXConcept>());
			// make sure position of every node is initialised
			positions.put(n, 0);
		}

		// structure in out- and in-edges
		for (ONDEXRelation e : graph.getEdges()) {
			ONDEXConcept source = graph.getSource(e);
			ONDEXConcept dest = graph.getDest(e);
			out_edges.get(source).add(dest);
			in_edges.get(dest).add(source);
		}

		// assign levels by maximal dfs tree
		Map<ONDEXConcept, Integer> templevels = new HashMap<ONDEXConcept, Integer>();
		for (ONDEXConcept root : graph.getVertices()) {
			Map<ONDEXConcept, Integer> tree = new HashMap<ONDEXConcept, Integer>();
			dfs(tree, new HashSet<ONDEXConcept>(), root, 0);
			for (ONDEXConcept n : tree.keySet()) {
				// default return is 0
				int oldlevel = templevels.containsKey(n) ? templevels.get(n)
						: 0;
				int newlevel = tree.get(n);
				// >= to have also zero level explicitly represented
				if (newlevel >= oldlevel) {
					templevels.put(n, newlevel);
				}
			}
		}
		if (DEBUG)
			System.out.println("{node=>level}: " + templevels);

		// initialize level structure
		for (ONDEXConcept n : templevels.keySet()) {
			int level = templevels.get(n);
			if (!levels.containsKey(level))
				levels.put(level, new ArrayList<ONDEXConcept>());
			levels.get(level).add(n);
		}
		if (DEBUG)
			System.out.println("Levels: " + levels);

		// careful, lastid passed as value
		insertDummies(templevels);
		if (DEBUG)
			System.out.println("Levels: " + levels);

		// perform rearrangement of node as long as there are changes
		boolean change = true;
		int iteration = 0;
		while (change && iteration < max_iterations) {
			iteration++;
			boolean down = downwardScan();
			if (DEBUG)
				System.out.println("Down Levels: " + levels + " " + down);
			boolean up = upwardScan();
			if (DEBUG)
				System.out.println("Up Levels: " + levels + " " + up);
			change = down || up;
		}

		// initialize spacing of top row
		int current_x = spacing_x;
		if (levels.containsKey(0)) {
			if (DEBUG)
				System.out.println("First row spacing: ");
			for (ONDEXConcept n : levels.get(0)) {
				positions.put(n, current_x);
				if (DEBUG)
					System.out.println(n + "->" + current_x + " ");
				current_x += spacing_x;
			}
		}

		change = true;
		iteration = 0;
		while (change && iteration < max_iterations) {
			iteration++;
			boolean down = downwardArrange();
			boolean up = upwardArrange();
			change = down || up;
		}

		// positioning of nodes
		Dimension d = viewer.getSize();
		int current_y = (int) d.getHeight() / levels.keySet().size();
		for (int level : levels.keySet()) {
			if (DEBUG)
				System.out.print("Level " + level + " pos: ");
			for (ONDEXConcept n : levels.get(level)) {
				// ignore dummy nodes
				if (!(n instanceof DummyConcept)) {
					Point2D coord = this.transform(n);
					coord.setLocation(positions.get(n), current_y);
					if (DEBUG)
						System.out.print(positions.get(n) + " ");
				}
			}
			if (DEBUG)
				System.out.println();
			current_y += spacing_y;
		}
	}

	public void reset() {
		initialize();
	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// adjust iterations
		sliderMaxIterations = new JSlider();
		sliderMaxIterations.setBorder(BorderFactory
				.createTitledBorder("Max iterations"));
		sliderMaxIterations.setMinimum(0);
		sliderMaxIterations.setMaximum(10000);
		sliderMaxIterations.setValue(max_iterations);
		sliderMaxIterations.setMajorTickSpacing(2000);
		sliderMaxIterations.setMinorTickSpacing(500);
		sliderMaxIterations.setPaintTicks(true);
		sliderMaxIterations.setPaintLabels(true);
		sliderMaxIterations.addChangeListener(this);

		// spacing options
		JPanel spacings = new JPanel();
		BoxLayout spacingsLayout = new BoxLayout(spacings, BoxLayout.PAGE_AXIS);
		spacings.setLayout(spacingsLayout);
		spacings.setBorder(BorderFactory.createTitledBorder("Spacings"));

		sliderSpacingX = new JSlider();
		sliderSpacingX
				.setBorder(BorderFactory.createTitledBorder("horizontal"));
		sliderSpacingX.setMinimum(50);
		sliderSpacingX.setMaximum(250);
		sliderSpacingX.setValue(spacing_x);
		sliderSpacingX.setMajorTickSpacing(40);
		sliderSpacingX.setMinorTickSpacing(10);
		sliderSpacingX.setPaintTicks(true);
		sliderSpacingX.setPaintLabels(true);
		sliderSpacingX.addChangeListener(this);
		spacings.add(sliderSpacingX);

		sliderSpacingY = new JSlider();
		sliderSpacingY.setBorder(BorderFactory.createTitledBorder("vertical"));
		sliderSpacingY.setMinimum(50);
		sliderSpacingY.setMaximum(250);
		sliderSpacingY.setValue(spacing_y);
		sliderSpacingY.setMajorTickSpacing(40);
		sliderSpacingY.setMinorTickSpacing(10);
		sliderSpacingY.setPaintTicks(true);
		sliderSpacingY.setPaintLabels(true);
		sliderSpacingY.addChangeListener(this);
		spacings.add(sliderSpacingY);

		sliderMaxSpacingX = new JSlider();
		sliderMaxSpacingX.setBorder(BorderFactory
				.createTitledBorder("max horizontal"));
		sliderMaxSpacingX.setMinimum(100);
		sliderMaxSpacingX.setMaximum(500);
		sliderMaxSpacingX.setValue(max_spacing_x);
		sliderMaxSpacingX.setMajorTickSpacing(80);
		sliderMaxSpacingX.setMinorTickSpacing(20);
		sliderMaxSpacingX.setPaintTicks(true);
		sliderMaxSpacingX.setPaintLabels(true);
		sliderMaxSpacingX.addChangeListener(this);
		spacings.add(sliderMaxSpacingX);

		panel.add(sliderMaxIterations);
		panel.add(spacings);
		return panel;
	}

	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(sliderSpacingX)) {
			spacing_x = sliderSpacingX.getValue();
		} else if (arg0.getSource().equals(sliderSpacingY)) {
			spacing_y = sliderSpacingY.getValue();
		} else if (arg0.getSource().equals(sliderMaxSpacingX)) {
			max_spacing_x = sliderMaxSpacingX.getValue();
		} else if (arg0.getSource().equals(sliderMaxIterations)) {
			max_iterations = sliderMaxIterations.getValue();
		}
	}
}