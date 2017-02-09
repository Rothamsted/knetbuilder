package net.sourceforge.ondex.ovtk2.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Java implementation of the gem 2D layout. <br>
 * The algorithm needs to get various subgraphs and traversals. The recursive
 * nature of the algorithm is totally captured within those subgraphs and
 * traversals. The main loop of the algorithm is then expressed using the
 * iterator feature, which makes it look like a simple flat iteration over
 * nodes.
 * 
 * @author David Duke
 * @author Hacked by Eytan Adar for Guess
 * @author Hacked by taubertj for OVTK2
 */
public class GEMLayout extends OVTK2Layouter implements ChangeListener, Monitorable {

	/**
	 * Class containing properties per node.
	 * 
	 * @author taubertj
	 * 
	 */
	private class GemP {

		public int x, y; // position

		public int in;

		public int iX, iY; // impulse

		public float dir; // direction

		public float heat; // heat

		public float mass; // weight = nr edges

		public boolean mark;

		public GemP(int m) {
			x = y = 0;
			iX = iY = 0;
			dir = (float) 0.0;
			heat = 0;
			mass = m;
			mark = false;
		}
	}

	/**
	 * Compares graphs according to vertices count.
	 * 
	 * @author taubertj
	 * 
	 */
	private class SubGraphComparator implements Comparator<Graph<?, ?>> {

		@Override
		public int compare(Graph<?, ?> o1, Graph<?, ?> o2) {
			return o2.getVertexCount() - o1.getVertexCount();
		}

	}

	/**
	 * Current progress made for Monitorable
	 */
	private int progress = 0;

	/**
	 * Current state for Monitorable
	 */
	private String state = Monitorable.STATE_IDLE;

	/**
	 * If the process gets cancelled
	 */
	private boolean cancelled = false;

	// number of nodes in the graph
	private int nodeCount;

	// use clustered approach
	private boolean clustered = true;

	// number of clusters
	private int nbClusters = 1;

	// vertical cluster spacing
	public int verticalSpacing = 75;

	// horizontal cluster spacing
	public int horizontalSpacing = 75;

	// multiplicator of largest cluster
	public int multi = 3;

	//
	// GEM Constants
	//
	private int ELEN = 128;

	private int ELENSQR = ELEN * ELEN;

	private int MAXATTRACT = 1048576;

	//
	// GEM variables
	//
	private long iteration;

	private long temperature;

	private int centerX, centerY;

	private long maxtemp;

	private float oscillation, rotation;

	//
	// GEM Default Parameter Values
	//
	private float i_maxtemp = (float) 1.0;

	private float a_maxtemp = (float) 1.5;

	private float o_maxtemp = (float) 0.25;

	private float i_starttemp = (float) 0.3;

	private float a_starttemp = (float) 1.0;

	private float o_starttemp = (float) 1.0;

	private float i_finaltemp = (float) 0.05;

	private float a_finaltemp = (float) 0.02;

	private float o_finaltemp = (float) 1.0;

	private int i_maxiter = 10;

	private int a_maxiter = 3;

	private int o_maxiter = 3;

	private float i_gravity = (float) 0.05;

	private float i_oscillation = (float) 0.4;

	private float i_rotation = (float) 0.5;

	private float i_shake = (float) 0.2;

	private float a_gravity = (float) 0.1;

	private float a_oscillation = (float) 0.4;

	private float a_rotation = (float) 0.9;

	private float a_shake = (float) 0.3;

	private float o_gravity = (float) 0.1;

	private float o_oscillation = (float) 0.4;

	private float o_rotation = (float) 0.9;

	private float o_shake = (float) 0.3;

	// list of properties for each node
	private GemP gemProp[];

	// inverse map from int id to ONDEXConcept
	private ONDEXConcept invmap[];

	// adjacent int ids for a given ONDEXConcept int id
	private Map<Integer, List<Integer>> adjacent;

	// map from ONDEXConcept to int id
	private Map<ONDEXConcept, Integer> nodeNumbers;

	// randomizer used for node selection
	private Random rand = new Random();

	// map used for current random set of nodes
	private int map[];

	// priority queue for BFS
	private Queue<Integer> q;

	// slider for edge length
	private JSlider sliderElen = null;

	// slider for i_maxiter
	private JSlider sliderIMaxIter = null;

	// slider for i_gravity
	private JSlider sliderIGravity = null;

	// slider for i_shake
	private JSlider sliderIShake = null;

	// slider for a_maxiter
	private JSlider sliderAMaxIter = null;

	// slider for a_gravity
	private JSlider sliderAGravity = null;

	// slider for a_shake
	private JSlider sliderAShake = null;

	// slider for o_maxiter
	private JSlider sliderOMaxIter = null;

	// slider for o_gravity
	private JSlider sliderOGravity = null;

	// slider for o_shake
	private JSlider sliderOShake = null;

	// largest cluster multiplier
	private JTextField textMulti = new JTextField(multi + "");

	// horizontal spacing
	private JTextField textHorizontal = new JTextField(horizontalSpacing + "");

	// vertical spacing
	private JTextField textVertical = new JTextField(verticalSpacing + "");

	// use cluster approach ?
	private JCheckBox boxCluster = new JCheckBox("Arrange clusters?", clustered);

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public GEMLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
	}

	/**
	 * Constructor
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 * @param seed
	 *            Random seed
	 */
	public GEMLayout(OVTK2PropertiesAggregator viewer, long seed) {
		this(viewer);
		rand.setSeed(seed);
	}

	private void a_round() {

		Iterator<Integer> nodeSet;
		int v;

		int iX, iY, dX, dY;
		int n;
		int pX, pY;
		GemP p, q;

		for (int i = 0; i < nodeCount; i++) {
			v = select();
			p = gemProp[v];

			pX = p.x;
			pY = p.y;

			n = (int) (a_shake * ELEN);
			iX = rand() % (2 * n + 1) - n;
			iY = rand() % (2 * n + 1) - n;
			iX += (centerX / nodeCount - pX) * p.mass * a_gravity;
			iY += (centerY / nodeCount - pY) * p.mass * a_gravity;

			for (int u = 0; u < nodeCount; u++) {
				q = gemProp[u];
				dX = pX - q.x;
				dY = pY - q.y;
				n = dX * dX + dY * dY;
				if (n > 0) {
					iX += dX * ELENSQR / n;
					iY += dY * ELENSQR / n;
				}
			}
			nodeSet = adjacent.get(v).iterator();
			int u;
			while (nodeSet.hasNext()) {
				u = nodeSet.next();
				q = gemProp[u];
				dX = pX - q.x;
				dY = pY - q.y;
				n = (int) ((dX * dX + dY * dY) / p.mass);
				n = (int) Math.min(n, MAXATTRACT);
				iX -= dX * n / ELENSQR;
				iY -= dY * n / ELENSQR;
			}
			displace(v, iX, iY);
			iteration++;
		}
	}

	private void arrange() {

		long stop_temperature;
		long stop_iteration;

		vertexdata_init(a_starttemp);

		oscillation = a_oscillation;
		rotation = a_rotation;
		maxtemp = (int) (a_maxtemp * ELEN);
		stop_temperature = (int) (a_finaltemp * a_finaltemp * ELENSQR * nodeCount);
		stop_iteration = a_maxiter * nodeCount * nodeCount;
		iteration = 0;

		// System.out.print( "arrange phase -- temp " );
		// System.out.print( stop_temperature + " iter ");
		// System.out.println ( stop_iteration );

		while (temperature > stop_temperature && iteration < stop_iteration) {
			// com.hp.hpl.guess.ui.StatusBar.setValue((int)stop_iteration,
			// (int)iteration);
			a_round();
			if (cancelled)
				return;
		}
		// com.hp.hpl.guess.ui.StatusBar.setValue(100,0);
	}

	/**
	 * Performs a BFS on the graph
	 * 
	 * @param root
	 *            int
	 * @return node id
	 */
	private int bfs(int root) {

		Iterator<Integer> nodeSet;
		int v, ui;

		if (root >= 0) {
			q = new LinkedList<Integer>();
			if (!gemProp[root].mark) { // root > 0
				for (int vi = 0; vi < nodeCount; vi++) {
					gemProp[vi].in = 0;
				}
			} else
				gemProp[root].mark = true; // root = -root;
			q.add(root);
			gemProp[root].in = 1;
		}
		if (q.size() == 0)
			return -1; // null
		v = q.poll();

		nodeSet = adjacent.get(v).iterator();
		while (nodeSet.hasNext()) {
			ui = nodeSet.next();
			if (gemProp[ui].in != 0) {
				q.add(ui);
				gemProp[ui].in = gemProp[v].in + 1;
			}
		}

		return v;
	}

	/**
	 * Calculates actual bounds of a painted graph.
	 * 
	 * @return Point2D[]
	 */
	private Point2D[] calcBounds(Graph<ONDEXConcept, ONDEXRelation> graph, Map<ONDEXConcept, Point2D> coords) {
		Point2D[] result = new Point2D[2];
		Point2D min = null;
		Point2D max = null;
		Iterator<ONDEXConcept> it = graph.getVertices().iterator();
		while (it.hasNext()) {
			Point2D point = coords.get(it.next());
			if (min == null) {
				min = new Point2D.Double(0, 0);
				min.setLocation(point);
			}
			if (max == null) {
				max = new Point2D.Double(0, 0);
				max.setLocation(point);
			}
			min.setLocation(Math.min(min.getX(), point.getX()), Math.min(min.getY(), point.getY()));
			max.setLocation(Math.max(max.getX(), point.getX()), Math.max(max.getY(), point.getY()));
		}
		result[0] = min;
		result[1] = max;
		return result;
	}

	/**
	 * Clusters given graph into subgraphs.
	 */
	public Set<Graph<ONDEXConcept, ONDEXRelation>> clusterGraph(Graph<ONDEXConcept, ONDEXRelation> original) {
		state = "layout phase cluster graph";

		try {
			// parse options
			multi = Integer.parseInt(textMulti.getText());
			horizontalSpacing = Integer.parseInt(textHorizontal.getText());
			verticalSpacing = Integer.parseInt(textVertical.getText());
		} catch (NumberFormatException nfe) {
			// revert to defaults
			verticalSpacing = 75;
			horizontalSpacing = 75;
			multi = 3;
		}

		// contains all possible subgraphs
		Set<Graph<ONDEXConcept, ONDEXRelation>> subgraphs = new HashSet<Graph<ONDEXConcept, ONDEXRelation>>();

		// sort each vertex into one subgraph
		Set<ONDEXConcept> sorted = new HashSet<ONDEXConcept>();
		for (ONDEXConcept n : original.getVertices()) {

			// Orphan node
			if (!sorted.contains(n)) {

				// create new cluster starting at this node
				Graph<ONDEXConcept, ONDEXRelation> cluster = new SparseGraph<ONDEXConcept, ONDEXRelation>();
				subgraphs.add(cluster);

				// add node to new cluster and mark as sorted
				cluster.addVertex(n);
				sorted.add(n);

				// inspect neighbours of n do BFS
				Queue<ONDEXConcept> queue = new LinkedList<ONDEXConcept>();
				Collection<ONDEXConcept> neigbours = original.getNeighbors(n);
				queue.addAll(neigbours);

				// process queue
				while (!queue.isEmpty()) {
					ONDEXConcept next = queue.poll();
					if (!sorted.contains(next)) {

						// add to cluster and mark as sorted
						cluster.addVertex(next);
						sorted.add(next);

						// add edges to cluster
						Collection<ONDEXRelation> nextEdges = original.getIncidentEdges(next);
						for (ONDEXRelation edge : nextEdges) {
							cluster.addEdge(edge, original.getSource(edge), original.getDest(edge));
						}

						// proceed to next level
						queue.addAll(original.getNeighbors(next));
					}
				}
			}

			if (cancelled)
				return subgraphs;
		}
		// System.out.println(subgraphs);

		return subgraphs;
	}

	private void displace(int v, int iX, int iY) {

		int t;
		int n;
		GemP p;

		if (iX != 0 || iY != 0) {
			n = Math.max(Math.abs(iX), Math.abs(iY)) / 16384;
			if (n > 1) {
				iX /= n;
				iY /= n;
			}
			p = gemProp[v];
			t = (int) p.heat;
			n = (int) Math.sqrt(iX * iX + iY * iY);
			iX = iX * t / n;
			iY = iY * t / n;
			p.x += iX;
			p.y += iY;
			centerX += iX;
			centerY += iY;
			// imp = &vi[v].imp;
			n = t * (int) Math.sqrt(p.iX * p.iX + p.iY * p.iY);
			if (n > 0) {
				temperature -= t * t;
				t += t * oscillation * (iX * p.iX + iY * p.iY) / n;
				t = (int) Math.min(t, maxtemp);
				p.dir += rotation * (iX * p.iY - iY * p.iX) / n;
				t -= t * Math.abs(p.dir) / nodeCount;
				t = Math.max(t, 2);
				temperature += t * t;
				p.heat = t;
			}
			p.iX = iX;
			p.iY = iY;
		}
	}

	/*
	 * Optimisation Code
	 */
	private int[] EVdistance(int thisNode, int thatNode, int v) {

		GemP thisGP = gemProp[thisNode];
		GemP thatGP = gemProp[thatNode];
		GemP nodeGP = gemProp[v];

		int aX = thisGP.x;
		int aY = thisGP.y;
		int bX = thatGP.x;
		int bY = thatGP.y;
		int cX = nodeGP.x;
		int cY = nodeGP.y;

		long m, n;

		bX -= aX;
		bY -= aY; /* b' = b - a */
		m = bX * (cX - aX) + bY * (cY - aY); /* m = <b'|c-a> = <b-a|c-a> */
		n = bX * bX + bY * bY; /* n = |b'|^2 = |b-a|^2 */
		if (m < 0)
			m = 0;
		if (m > n)
			m = n = 1;
		if ((m >> 17) > 0) { /* prevent integer overflow */
			n /= m >> 16;
			m /= m >> 16;
		}
		if (n != 0) {
			aX += (int) (bX * m / n); /* a' = m/n b' = a + m/n (b-a) */
			aY += (int) (bY * m / n);
		}
		return new int[] { aX, aY };
	}

	@Override
	public int getMaxProgress() {
		if (!clustered)
			return 4;
		else {
			int size = nbClusters;
			size += size * 4;
			return size;
		}
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// global edge length
		sliderElen = new JSlider();
		sliderElen.setBorder(BorderFactory.createTitledBorder("Preferred edge length"));
		sliderElen.setMinimum(50);
		sliderElen.setMaximum(200);
		sliderElen.setValue(ELEN);
		sliderElen.setMajorTickSpacing(20);
		sliderElen.setMinorTickSpacing(5);
		sliderElen.setPaintTicks(true);
		sliderElen.setPaintLabels(true);
		sliderElen.addChangeListener(this);

		// insertion phase options
		JPanel insert = new JPanel();
		BoxLayout insertLayout = new BoxLayout(insert, BoxLayout.PAGE_AXIS);
		insert.setLayout(insertLayout);
		insert.setBorder(BorderFactory.createTitledBorder("Insert phase"));

		sliderIMaxIter = new JSlider();
		sliderIMaxIter.setBorder(BorderFactory.createTitledBorder("max iterations"));
		sliderIMaxIter.setMinimum(0);
		sliderIMaxIter.setMaximum(20);
		sliderIMaxIter.setValue(i_maxiter);
		sliderIMaxIter.setMajorTickSpacing(5);
		sliderIMaxIter.setMinorTickSpacing(1);
		sliderIMaxIter.setPaintTicks(true);
		sliderIMaxIter.setPaintLabels(true);
		sliderIMaxIter.addChangeListener(this);
		insert.add(sliderIMaxIter);

		sliderIGravity = new JSlider();
		sliderIGravity.setBorder(BorderFactory.createTitledBorder("gravity"));
		sliderIGravity.setMinimum(0);
		sliderIGravity.setMaximum(100);
		sliderIGravity.setValue((int) (i_gravity * 100));
		sliderIGravity.setMajorTickSpacing(20);
		sliderIGravity.setMinorTickSpacing(5);
		sliderIGravity.setPaintTicks(true);
		sliderIGravity.setPaintLabels(true);
		sliderIGravity.addChangeListener(this);
		insert.add(sliderIGravity);

		sliderIShake = new JSlider();
		sliderIShake.setBorder(BorderFactory.createTitledBorder("shake"));
		sliderIShake.setMinimum(0);
		sliderIShake.setMaximum(100);
		sliderIShake.setValue((int) (i_shake * 100));
		sliderIShake.setMajorTickSpacing(20);
		sliderIShake.setMinorTickSpacing(5);
		sliderIShake.setPaintTicks(true);
		sliderIShake.setPaintLabels(true);
		sliderIShake.addChangeListener(this);
		insert.add(sliderIShake);

		// arrange phase options
		JPanel arrange = new JPanel();
		BoxLayout arrangeLayout = new BoxLayout(arrange, BoxLayout.PAGE_AXIS);
		arrange.setLayout(arrangeLayout);
		arrange.setBorder(BorderFactory.createTitledBorder("Arrange phase"));

		sliderAMaxIter = new JSlider();
		sliderAMaxIter.setBorder(BorderFactory.createTitledBorder("max iterations"));
		sliderAMaxIter.setMinimum(0);
		sliderAMaxIter.setMaximum(20);
		sliderAMaxIter.setValue(a_maxiter);
		sliderAMaxIter.setMajorTickSpacing(5);
		sliderAMaxIter.setMinorTickSpacing(1);
		sliderAMaxIter.setPaintTicks(true);
		sliderAMaxIter.setPaintLabels(true);
		sliderAMaxIter.addChangeListener(this);
		arrange.add(sliderAMaxIter);

		sliderAGravity = new JSlider();
		sliderAGravity.setBorder(BorderFactory.createTitledBorder("gravity"));
		sliderAGravity.setMinimum(0);
		sliderAGravity.setMaximum(100);
		sliderAGravity.setValue((int) (a_gravity * 100));
		sliderAGravity.setMajorTickSpacing(20);
		sliderAGravity.setMinorTickSpacing(5);
		sliderAGravity.setPaintTicks(true);
		sliderAGravity.setPaintLabels(true);
		sliderAGravity.addChangeListener(this);
		arrange.add(sliderAGravity);

		sliderAShake = new JSlider();
		sliderAShake.setBorder(BorderFactory.createTitledBorder("shake"));
		sliderAShake.setMinimum(0);
		sliderAShake.setMaximum(100);
		sliderAShake.setValue((int) (a_shake * 100));
		sliderAShake.setMajorTickSpacing(20);
		sliderAShake.setMinorTickSpacing(5);
		sliderAShake.setPaintTicks(true);
		sliderAShake.setPaintLabels(true);
		sliderAShake.addChangeListener(this);
		arrange.add(sliderAShake);

		// optimize phase options
		JPanel optimize = new JPanel();
		BoxLayout optimizeLayout = new BoxLayout(optimize, BoxLayout.PAGE_AXIS);
		optimize.setLayout(optimizeLayout);
		optimize.setBorder(BorderFactory.createTitledBorder("Optimize phase"));

		sliderOMaxIter = new JSlider();
		sliderOMaxIter.setBorder(BorderFactory.createTitledBorder("max iterations"));
		sliderOMaxIter.setMinimum(0);
		sliderOMaxIter.setMaximum(20);
		sliderOMaxIter.setValue(o_maxiter);
		sliderOMaxIter.setMajorTickSpacing(5);
		sliderOMaxIter.setMinorTickSpacing(1);
		sliderOMaxIter.setPaintTicks(true);
		sliderOMaxIter.setPaintLabels(true);
		sliderOMaxIter.addChangeListener(this);
		optimize.add(sliderOMaxIter);

		sliderOGravity = new JSlider();
		sliderOGravity.setBorder(BorderFactory.createTitledBorder("gravity"));
		sliderOGravity.setMinimum(0);
		sliderOGravity.setMaximum(100);
		sliderOGravity.setValue((int) (o_gravity * 100));
		sliderOGravity.setMajorTickSpacing(20);
		sliderOGravity.setMinorTickSpacing(5);
		sliderOGravity.setPaintTicks(true);
		sliderOGravity.setPaintLabels(true);
		sliderOGravity.addChangeListener(this);
		optimize.add(sliderOGravity);

		sliderOShake = new JSlider();
		sliderOShake.setBorder(BorderFactory.createTitledBorder("shake"));
		sliderOShake.setMinimum(0);
		sliderOShake.setMaximum(100);
		sliderOShake.setValue((int) (o_shake * 100));
		sliderOShake.setMajorTickSpacing(20);
		sliderOShake.setMinorTickSpacing(5);
		sliderOShake.setPaintTicks(true);
		sliderOShake.setPaintLabels(true);
		sliderOShake.addChangeListener(this);
		optimize.add(sliderOShake);

		panel.add(boxCluster);
		panel.add(sliderElen);
		panel.add(insert);
		panel.add(arrange);
		panel.add(optimize);

		JPanel cluster = new JPanel();
		BoxLayout clusterLayout = new BoxLayout(cluster, BoxLayout.PAGE_AXIS);
		cluster.setLayout(clusterLayout);
		cluster.setBorder(BorderFactory.createTitledBorder("Clustered options"));

		textMulti.setBorder(BorderFactory.createTitledBorder("largest cluster multiply"));
		cluster.add(textMulti);

		textHorizontal.setBorder(BorderFactory.createTitledBorder("horizontal spacing"));
		cluster.add(textHorizontal);

		textVertical.setBorder(BorderFactory.createTitledBorder("vertical spacing"));

		cluster.add(textVertical);
		panel.add(cluster);

		return panel;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		return null;
	}

	/**
	 * Returns node for the graph center.
	 * 
	 * @return int
	 */
	private int graph_center() {
		GemP p;
		int c, u, v, w; // nodes
		int h;

		c = -1; // for a contented compiler.
		u = -1;

		h = nodeCount + 1;
		for (w = 0; w < nodeCount; w++) {
			v = bfs(w);
			while (v >= 0 && gemProp[v].in < h) {
				u = v;
				v = bfs(-1); // null
			}
			p = gemProp[u];
			if (p.in < h) {
				h = p.in;
				c = w;
			}
		}

		// randomly choose a centre node if graph doesn't have a centre
		if (c == -1)
			return (int) Math.rint((nodeCount - 1) * Math.random());

		return c;
	}

	/*
	 * INSERT code from GEM
	 */
	private int[] i_impulse(int v) {

		Iterator<Integer> nodeSet;

		int iX, iY, dX, dY, pX, pY;
		int n;
		GemP p, q;

		p = gemProp[v];
		pX = p.x;
		pY = p.y;

		n = (int) (i_shake * ELEN);
		iX = rand() % (2 * n + 1) - n;
		iY = rand() % (2 * n + 1) - n;
		iX += (centerX / nodeCount - pX) * p.mass * i_gravity;
		iY += (centerY / nodeCount - pY) * p.mass * i_gravity;

		for (int u = 0; u < nodeCount; u++) {
			q = gemProp[u];
			if (q.in > 0) {
				dX = pX - q.x;
				dY = pY - q.y;
				n = dX * dX + dY * dY;
				if (n > 0) {
					iX += dX * ELENSQR / n;
					iY += dY * ELENSQR / n;
				}
			}
		}
		nodeSet = adjacent.get(v).iterator();
		int u;
		while (nodeSet.hasNext()) {
			u = nodeSet.next();
			q = gemProp[u];
			if (q.in > 0) {
				dX = pX - q.x;
				dY = pY - q.y;
				n = (int) ((dX * dX + dY * dY) / p.mass);
				n = Math.min(n, MAXATTRACT);
				iX -= dX * n / ELENSQR;
				iY -= dY * n / ELENSQR;
			}
		}

		return new int[] { iX, iY };
	}

	/**
	 * Runs the layout.
	 */
	public void initialize() {

		cancelled = false;
		progress = 0;
		state = Monitorable.STATE_IDLE;

		long startTime, endTime;

		state = "initialize layout";

		startTime = System.currentTimeMillis();

		clustered = boxCluster.isSelected();

		if (clustered) {
			Set<Graph<ONDEXConcept, ONDEXRelation>> clusters = clusterGraph(getGraph());
			nbClusters = clusters.size();
			runClustered(clusters);
		} else {
			runNormal(getGraph());

			// set location of nodes in graph
			for (int i = 0; i < nodeCount; i++) {
				GemP p = gemProp[i];
				ONDEXConcept n = invmap[i];

				Point2D coord = this.transform(n);
				coord.setLocation(p.x, p.y);
			}
		}
		endTime = System.currentTimeMillis();

		System.out.println("Took: " + (endTime - startTime) + "msec");
		state = Monitorable.STATE_TERMINAL;
	}

	private void insert() {

		Iterator<Integer> nodeSet;
		GemP p, q;
		int startNode;

		int v, w;

		int d;

		// System.out.println( "insert phase" );

		vertexdata_init(i_starttemp);

		oscillation = i_oscillation;
		rotation = i_rotation;
		maxtemp = (int) (i_maxtemp * ELEN);

		v = graph_center();

		for (int ui = 0; ui < nodeCount; ui++) {
			gemProp[ui].in = 0;
		}

		gemProp[v].in = -1;

		startNode = -1;
		for (int i = 0; i < nodeCount; i++) {
			d = 0;
			for (int u = 0; u < nodeCount; u++) {
				if (gemProp[u].in < d) {
					d = gemProp[u].in;
					v = u;
				}
			}
			gemProp[v].in = 1;

			nodeSet = adjacent.get(v).iterator();
			int u;
			while (nodeSet.hasNext()) {
				u = nodeSet.next();
				if (gemProp[u].in <= 0)
					gemProp[u].in--;
			}
			p = gemProp[v];
			p.x = p.y = 0;

			if (startNode >= 0) {
				d = 0;
				p = gemProp[v];
				nodeSet = adjacent.get(v).iterator();
				while (nodeSet.hasNext()) {
					w = nodeSet.next();
					q = gemProp[w];
					if (q.in > 0) {
						p.x += q.x;
						p.y += q.y;
						d++;
					}
				}
				if (d > 1) {
					p.x /= d;
					p.y /= d;
				}
				d = 0;
				while ((d++ < i_maxiter) && (p.heat > i_finaltemp * ELEN)) {
					int[] i_impulse = i_impulse(v);
					displace(v, i_impulse[0], i_impulse[1]);
				}

			} else {
				startNode = i;
			}

			if (cancelled)
				return;
		}
	}

	@Override
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	private int[] o_impulse(Graph<ONDEXConcept, ONDEXRelation> graph, int v) {

		int u, w;
		int iX, iY, dX, dY;
		int n;
		GemP p, up, wp;
		int pX, pY;

		p = gemProp[v];
		pX = p.x;
		pY = p.y;

		n = (int) (o_shake * ELEN);
		iX = rand() % (2 * n + 1) - n;
		iY = rand() % (2 * n + 1) - n;
		iX += (centerX / nodeCount - pX) * p.mass * o_gravity;
		iY += (centerY / nodeCount - pY) * p.mass * o_gravity;

		for (ONDEXRelation e : graph.getEdges()) {
			Pair<ONDEXConcept> ends = graph.getEndpoints(e);
			u = nodeNumbers.get(ends.getFirst());
			w = nodeNumbers.get(ends.getSecond());
			if (u != v && w != v) {
				up = gemProp[u];
				wp = gemProp[w];
				dX = (up.x + wp.x) / 2 - pX;
				dY = (up.y + wp.y) / 2 - pY;
				n = dX * dX + dY * dY;
				if (n < 8 * ELENSQR) {
					int[] evdist = EVdistance(u, w, v); // source, dest, vert
					dX = evdist[0];
					dY = evdist[1];
					dX -= pX;
					dY -= pY;
					n = dX * dX + dY * dY;
				}
				if (n > 0) {
					iX -= dX * ELENSQR / n;
					iY -= dY * ELENSQR / n;
				}
			} else {
				if (u == v)
					u = w;
				up = gemProp[u];
				dX = pX - up.x;
				dY = pY - up.y;
				n = (int) ((dX * dX + dY * dY) / p.mass);
				n = Math.min(n, MAXATTRACT);
				iX -= dX * n / ELENSQR;
				iY -= dY * n / ELENSQR;
			}
		}
		return new int[] { iX, iY };
	}

	private void o_round(Graph<ONDEXConcept, ONDEXRelation> graph) {

		int v;
		for (int i = 0; i < nodeCount; i++) {
			v = select();
			int[] o_impulse = o_impulse(graph, v);
			displace(v, o_impulse[0], o_impulse[1]);
			iteration++;
		}
	}

	private void optimize(Graph<ONDEXConcept, ONDEXRelation> graph) {

		long stop_temperature;
		long stop_iteration;

		vertexdata_init(o_starttemp);
		oscillation = o_oscillation;
		rotation = o_rotation;
		maxtemp = (int) (o_maxtemp * ELEN);
		stop_temperature = (int) (o_finaltemp * o_finaltemp * ELENSQR * nodeCount);
		stop_iteration = o_maxiter * nodeCount * nodeCount;

		// System.out.print( "optimise phase -- temp " );
		// System.out.print( stop_temperature + " iter ");
		// System.out.println ( stop_iteration );

		while (temperature > stop_temperature && iteration < stop_iteration) {
			o_round(graph);
			if ((iteration % 20000) == 0) {
				// System.out.println( iteration + "\t" + temperature );
			}
			if (cancelled)
				return;
		}
	}

	/**
	 * Random function returns an random int value.
	 * 
	 * @return int
	 */
	private int rand() {
		return (int) (rand.nextDouble() * Integer.MAX_VALUE);
	}

	/**
	 * Run the layout again.
	 */
	public void reset() {
		initialize();
	}

	/**
	 * Layout subgraphs on separate places.
	 */
	@SuppressWarnings("unchecked")
	public void runClustered(Set<Graph<ONDEXConcept, ONDEXRelation>> subgraphs) {

		// sort subgraphs according to size
		Graph<?, ?>[] sortedSubgraphs = subgraphs.toArray(new Graph<?, ?>[0]);
		Arrays.sort(sortedSubgraphs, new SubGraphComparator());

		// cache local layout
		Map<Graph<ONDEXConcept, ONDEXRelation>, Map<ONDEXConcept, Point2D>> localLayouts = new HashMap<Graph<ONDEXConcept, ONDEXRelation>, Map<ONDEXConcept, Point2D>>();

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		// apply algorithm to sorted graphs
		int j = 0;
		for (Graph<?, ?> subgraph : sortedSubgraphs) {
			j++;
			state = "processing subgraph " + j + " of " + sortedSubgraphs.length;

			// set subgraph as normal and run GEM layout on it
			runNormal((Graph<ONDEXConcept, ONDEXRelation>) subgraph);

			// set location of nodes in subgraph
			localLayouts.put((Graph<ONDEXConcept, ONDEXRelation>) subgraph, new HashMap<ONDEXConcept, Point2D>());
			for (int i = 0; i < nodeCount; i++) {
				GemP p = gemProp[i];
				ONDEXConcept n = invmap[i];

				Point2D coord = this.transform(n);
				coord.setLocation(p.x, p.y);

				if (p.x < minX)
					minX = p.x;
				if (p.x > maxX)
					maxX = p.x;
				if (p.y < minY)
					minY = p.y;
				if (p.y > maxY)
					maxY = p.y;

				localLayouts.get(subgraph).put(n, coord);
			}

			progress++;
			if (cancelled)
				return;
		}

		// System.out.println(minX + " " + maxX + " ; " + minY + " " + maxY);

		int width = (Math.abs(minX) + Math.abs(maxX)) * multi;

		double offsetX = 0;
		double offsetY = 0;
		double maxposY = 0;
		for (Graph<?, ?> sub : sortedSubgraphs) {
			Graph<ONDEXConcept, ONDEXRelation> subgraph = (Graph<ONDEXConcept, ONDEXRelation>) sub;
			Map<ONDEXConcept, Point2D> coords = localLayouts.get(subgraph);

			// calculate bounds required for normalisation
			Point2D[] result = calcBounds(subgraph, coords);
			Point2D min = result[0];

			// current expansion
			double tmpY = 0;
			double tmpX = 0;

			// offset all nodes of local layout
			Iterator<ONDEXConcept> keys = coords.keySet().iterator();
			while (keys.hasNext()) {
				ONDEXConcept n = keys.next();
				Point2D coord = coords.get(n);
				// centre at 0,0 and offset
				double newX = offsetX + coord.getX() - min.getX();
				double newY = offsetY + coord.getY() - min.getY();

				// calculate maximum boundaries
				if (newX > tmpX)
					tmpX = newX;
				if (newY > tmpY)
					tmpY = newY;
				coord.setLocation(newX, newY);
			}

			// shift horizontally keep track of vertical
			offsetX = tmpX + horizontalSpacing;
			if (tmpY > maxposY) {
				maxposY = tmpY;
			}

			// line break here
			if (offsetX > width) {
				offsetY = maxposY + verticalSpacing;
				offsetX = 0;
				maxposY = 0;
			}
		}

	}

	/**
	 * Normal bubble like GEM layout.
	 */
	private void runNormal(Graph<ONDEXConcept, ONDEXRelation> graph) {

		Collection<ONDEXConcept> nodes = graph.getVertices();

		nodeCount = nodes.size();

		// ignore empty graphs
		if (nodeCount == 0)
			return;

		gemProp = new GemP[nodeCount];
		invmap = new ONDEXConcept[nodeCount];
		adjacent = new HashMap<Integer, List<Integer>>(nodeCount);
		nodeNumbers = new HashMap<ONDEXConcept, Integer>();

		// initialize node lists and gemProp
		Iterator<ONDEXConcept> nodeSet = nodes.iterator();
		for (int i = 0; nodeSet.hasNext(); i++) {
			ONDEXConcept n = nodeSet.next();
			gemProp[i] = new GemP(graph.getOutEdges(n).size());
			invmap[i] = n;
			nodeNumbers.put(n, i);
		}

		// fill adjacent lists
		Collection<ONDEXConcept> neighbors;
		for (int i = 0; i < nodeCount; i++) {
			neighbors = graph.getNeighbors(invmap[i]);
			adjacent.put(i, new ArrayList<Integer>(neighbors.size()));
			for (ONDEXConcept n : neighbors) {
				adjacent.get(i).add(nodeNumbers.get(n));
			}
		}
		progress++;
		if (cancelled)
			return;

		// actual layout
		if (i_finaltemp < i_starttemp) {
			state = "layout phase insert";
			insert();
			progress++;
			if (cancelled)
				return;
		}
		if (a_finaltemp < a_starttemp) {
			state = "layout phase arrange";
			arrange();
			progress++;
			if (cancelled)
				return;
		}
		if (o_finaltemp < o_starttemp) {
			state = "layout phase optimize";
			optimize(graph);
			progress++;
			if (cancelled)
				return;
		}
	}

	/**
	 * Randomize selection of nodes.
	 * 
	 * @return node id
	 */
	private int select() {

		int u;
		int n, v;

		if (iteration == 0) {
			// System.out.print( "New map for " + nodeCount );
			map = new int[nodeCount];
			for (int i = 0; i < nodeCount; i++)
				map[i] = i;
		}
		n = (int) (nodeCount - iteration % nodeCount);
		v = rand() % n; // was 1 + rand() % n due to numbering in GEM
		if (v == nodeCount)
			v--;
		if (n == nodeCount)
			n--;
		// System.out.println( "Access n = " + n + " v = " + v );
		u = map[v];
		map[v] = map[n];
		map[n] = u;
		return u;
	}

	@Override
	public void setCancelled(boolean c) {
		cancelled = c;
	}

	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(sliderElen)) {
			ELEN = sliderElen.getValue();
			ELENSQR = ELEN * ELEN;
		} else if (arg0.getSource().equals(sliderIMaxIter)) {
			i_maxiter = sliderIMaxIter.getValue();
		} else if (arg0.getSource().equals(sliderAMaxIter)) {
			a_maxiter = sliderAMaxIter.getValue();
		} else if (arg0.getSource().equals(sliderOMaxIter)) {
			o_maxiter = sliderOMaxIter.getValue();
		} else if (arg0.getSource().equals(sliderIGravity)) {
			i_gravity = (float) sliderIGravity.getValue() / 100f;
		} else if (arg0.getSource().equals(sliderAGravity)) {
			a_gravity = (float) sliderAGravity.getValue() / 100f;
		} else if (arg0.getSource().equals(sliderOGravity)) {
			o_gravity = (float) sliderOGravity.getValue() / 100f;
		} else if (arg0.getSource().equals(sliderIShake)) {
			i_shake = (float) sliderIShake.getValue() / 100f;
		} else if (arg0.getSource().equals(sliderAShake)) {
			a_shake = (float) sliderAShake.getValue() / 100f;
		} else if (arg0.getSource().equals(sliderOShake)) {
			o_shake = (float) sliderOShake.getValue() / 100f;
		}
	}

	/**
	 * Initialize properties of nodes.
	 * 
	 * @param starttemp
	 *            given start temperature
	 */
	private void vertexdata_init(float starttemp) {

		temperature = 0;
		centerX = centerY = 0;

		for (int v = 0; v < nodeCount; v++) {
			GemP p = gemProp[v];
			p.heat = starttemp * ELEN;
			temperature += p.heat * p.heat;
			p.iX = p.iY = 0;
			p.dir = 0;
			p.mass = 1 + gemProp[v].mass / 3;
			centerX += p.x;
			centerY += p.y;
		}
	}
}