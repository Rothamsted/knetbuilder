package net.sourceforge.ondex.ovtk2.layout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import edu.uci.ics.jung.graph.Graph;

/**
 * Layout per chromosome and GEMLayout.
 * 
 * @author pakk, taubertj
 */
public class GDSPositionLayout extends OVTK2Layouter {

	// spacing between chromosomes and GEMLayout
	private int constDist = 150;

	// vertical cluster spacing
	private int verticalSpacing = 75;

	// horizontal cluster spacing
	private int horizontalSpacing = 75;

	// multiplicator of largest cluster
	private int multi = 3;

	// constant distance
	private JTextField textDist;

	// attribute names possible for category
	private JComboBox dropCategory;

	// attribute names possible for position
	private JComboBox dropPosition;

	// largest cluster multiplier
	private JTextField textMulti;

	// horizontal spacing
	private JTextField textHorizontal;

	// vertical spacing
	private JTextField textVertical;

	private OVTK2PropertiesAggregator aViewer;

	/**
	 * Set OVTK2PropertiesAggregator to parent and configure local text fields.
	 * 
	 * @param viewer
	 *            OVTK2PropertiesAggregator
	 */
	public GDSPositionLayout(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		aViewer = viewer;
		init();
	}

	private void init() {
		textDist = new JTextField(constDist + "");
		textMulti = new JTextField(multi + "");
		textHorizontal = new JTextField(horizontalSpacing + "");
		textVertical = new JTextField(verticalSpacing + "");

		ONDEXJUNGGraph aog = (ONDEXJUNGGraph) graph;

		// drop down boxes for attribute names
		Collection<AttributeName> ans = aog.getMetaData().getAttributeNames();

		// Category can be anything
		dropCategory = new JComboBox(ans.toArray());

		// only integer attribute names
		ArrayList<AttributeName> integers = new ArrayList<AttributeName>();
		for (AttributeName an : ans) {
			if (Integer.class.isAssignableFrom(an.getDataType()))
				integers.add(an);
		}
		dropPosition = new JComboBox(integers.toArray());

		// default values
		AttributeName anPos = aog.getMetaData().getAttributeName("BEGIN");
		AttributeName anCat = aog.getMetaData().getAttributeName("Chromosome");

		// set if defaults exists, otherwise random choice
		if (anPos != null && anCat != null) {
			dropPosition.setSelectedItem(anPos);
			dropCategory.setSelectedItem(anCat);
		}
	}

	@Override
	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);

		// general layout configuration
		JPanel config = new JPanel();
		BoxLayout configLayout = new BoxLayout(config, BoxLayout.PAGE_AXIS);
		config.setLayout(configLayout);
		config.setBorder(BorderFactory.createTitledBorder("Configuration"));

		textDist.setBorder(BorderFactory
				.createTitledBorder("Distance between Chromosomes and Networks"));
		config.add(textDist);

		dropCategory.setBorder(BorderFactory
				.createTitledBorder("Select Category Attribute"));
		config.add(dropCategory);

		dropPosition.setBorder(BorderFactory
				.createTitledBorder("Select Position Attribute"));
		config.add(dropPosition);

		// GEMLayout specific configuration
		JPanel cluster = new JPanel();
		BoxLayout clusterLayout = new BoxLayout(cluster, BoxLayout.PAGE_AXIS);
		cluster.setLayout(clusterLayout);
		cluster.setBorder(BorderFactory.createTitledBorder("Clustered options"));

		textMulti.setBorder(BorderFactory
				.createTitledBorder("largest cluster multiply"));
		cluster.add(textMulti);

		textHorizontal.setBorder(BorderFactory
				.createTitledBorder("horizontal spacing"));
		cluster.add(textHorizontal);

		textVertical.setBorder(BorderFactory
				.createTitledBorder("vertical spacing"));
		cluster.add(textVertical);

		panel.add(config);
		panel.add(cluster);

		return panel;
	}

	@Override
	public void initialize() {
		try {
			// parse options
			constDist = Integer.parseInt(textDist.getText());
			multi = Integer.parseInt(textMulti.getText());
			horizontalSpacing = Integer.parseInt(textHorizontal.getText());
			verticalSpacing = Integer.parseInt(textVertical.getText());
		} catch (NumberFormatException nfe) {
			// revert to defaults
			constDist = 150;
			verticalSpacing = 75;
			horizontalSpacing = 75;
			multi = 3;
		}

		// necessary because of visibility usage
		ONDEXJUNGGraph graph = (ONDEXJUNGGraph) this.getGraph();
		if (graph != null) {

			// map concept -> node
			Map<ONDEXConcept, ONDEXConcept> reverse = new Hashtable<ONDEXConcept, ONDEXConcept>();

			// construct map
			Iterator<ONDEXConcept> nodes = graph.getVertices().iterator();
			while (nodes.hasNext()) {
				ONDEXConcept node = nodes.next();
				if (graph.isVisible(node))
					reverse.put(node, node);
			}

			// sort by chromosome
			Map<Integer, Set<ONDEXConcept>> map = new TreeMap<Integer, Set<ONDEXConcept>>();

			AttributeName anCat = (AttributeName) dropCategory
					.getSelectedItem();
			AttributeName anPos = (AttributeName) dropPosition
					.getSelectedItem();

			// vertical bounds for chromosome placement
			double minBegin = Double.POSITIVE_INFINITY;
			double maxBegin = Double.NEGATIVE_INFINITY;

			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anCat)) {
				if (reverse.containsKey(c)) {

					// get chromosome from Attribute
					Attribute attributeChrom = c.getAttribute(anCat);
					Integer chrom = (Integer) attributeChrom.getValue();
					if (!map.containsKey(chrom))
						map.put(chrom, new HashSet<ONDEXConcept>());
					map.get(chrom).add(c);

					// get location on chromosome from Attribute
					Attribute begin = c.getAttribute(anPos);
					if (begin != null) {
						double value = ((Integer) begin.getValue())
								.doubleValue();
						if (value < minBegin)
							minBegin = value;
						if (value > maxBegin)
							maxBegin = value;
					}
				}
			}

			// this is the case when only one gene present
			if (minBegin == maxBegin)
				maxBegin = minBegin + 10;

			// now hide all the genes in the ONDEXJUNGGraph
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anCat)) {
				if (reverse.containsKey(c))
					graph.setVisibility(reverse.get(c), false);
			}

			// get GEMLayout working and configured
			GEMLayout gem = null;
			if (aViewer != null)
				gem = new GEMLayout(aViewer);
			gem.horizontalSpacing = this.horizontalSpacing;
			gem.multi = this.multi;
			gem.verticalSpacing = this.verticalSpacing;

			// cluster graph without the genes and run layout
			Set<Graph<ONDEXConcept, ONDEXRelation>> clustered = gem
					.clusterGraph(graph);
			gem.runClustered(clustered);

			// calculate max bounds of GEMLayout part
			double minNetworkX = Double.POSITIVE_INFINITY;
			double maxNetworkX = Double.NEGATIVE_INFINITY;
			double minNetworkY = Double.POSITIVE_INFINITY;
			double maxNetworkY = Double.NEGATIVE_INFINITY;

			// get locations of GEMLayout
			for (ONDEXConcept n : graph.getVertices()) {
				Point2D result = gem.transform(n);
				if (result.getX() < minNetworkX)
					minNetworkX = result.getX();
				if (result.getX() > maxNetworkX)
					maxNetworkX = result.getX();
				if (result.getY() < minNetworkY)
					minNetworkY = result.getY();
				if (result.getY() > maxNetworkY)
					maxNetworkY = result.getY();
			}

			// set location of all other nodes
			int sizeNetwork = 0;
			for (ONDEXConcept n : graph.getVertices()) {
				// set location of node
				Point2D result = gem.transform(n);
				Point2D coord = transform(n);
				coord.setLocation(result.getX(), result.getY());
				sizeNetwork++;
			}

			// reset visibility of all genes in the ONDEXJUNGGraph
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(anCat)) {
				if (reverse.containsKey(c)) {
					graph.setVisibility(reverse.get(c), true);
					// set relations of genes visible too
					for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
						graph.setVisibility(r, true);
					}
				}
			}

			// default size to screen size
			if (sizeNetwork == 0) {
				minNetworkX = 0;
				maxNetworkX = getSize().getWidth();
				minNetworkY = 0;
				maxNetworkY = getSize().getHeight();
			}

			// calculate best position for chromosomes
			double thirdWidth = (maxNetworkX - minNetworkX) / 3;
			double fifthHeight = (maxNetworkY - minNetworkY) / 5;
			double startX = (maxNetworkX - minNetworkX) / 2 - (thirdWidth / 2);
			double startY = minNetworkY - fifthHeight - constDist;

			// normalise to fit max chromosome on screen
			double diff = maxBegin - minBegin;
			double distX = thirdWidth / map.keySet().size();
			double currentX = distX - distX / 2;

			// place genes on each chromosome
			Iterator<Integer> chromsIt = map.keySet().iterator();
			while (chromsIt.hasNext()) {
				Integer chromNr = chromsIt.next();

				int numGenes = 0;
				Iterator<ONDEXConcept> it_genes = map.get(chromNr).iterator();
				while (it_genes.hasNext()) {
					numGenes++;

					// get gene and normalise gene position
					ONDEXConcept gene = it_genes.next();
					Attribute attribute = gene.getAttribute(anPos);
					if (attribute != null) {
						double value = ((Integer) attribute.getValue())
								.doubleValue();
						double newvalue = ((value - minBegin) / diff);

						// set location of node
						Point2D coord = transform(reverse.get(gene));
						double newY = startY + fifthHeight * newvalue;
						double newX = startX + currentX;
						coord.setLocation(newX, newY);
					}
				}
				// System.err.println(chromo + " --> " + numGenes + " genes");
				currentX += distX;
			}
		}
	}

	@Override
	public void reset() {
		initialize();
	}
}
