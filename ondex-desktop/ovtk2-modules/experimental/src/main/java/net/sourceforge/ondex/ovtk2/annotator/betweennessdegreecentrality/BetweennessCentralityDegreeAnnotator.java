package net.sourceforge.ondex.ovtk2.annotator.betweennessdegreecentrality;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.functions.StandardFunctions;

/**
 * Annotates both with degree and betweenness to detect bridges in the network
 * 
 * @author lysenkoa
 */
public class BetweennessCentralityDegreeAnnotator extends OVTK2Annotator {

	// generated
	private static final long serialVersionUID = 1L;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	// status messages
	StyledDocument doc;

	public BetweennessCentralityDegreeAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		this.setLayout(new BorderLayout());

		JTextPane textPane = new JTextPane();
		doc = textPane.getStyledDocument();

		JScrollPane scroll = new JScrollPane(textPane);
		scroll.setBorder(BorderFactory.createTitledBorder("Status"));

		this.add(scroll, BorderLayout.CENTER);

		// triggers calculation
		JButton annotate = new JButton("Annotate");
		annotate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				calculate();
				used = true;
			}
		});
		this.add(annotate, BorderLayout.SOUTH);

		this.setPreferredSize(new Dimension(250, 200));
	}

	private void calculate() {
		try {
			// check for meta data, if not exist create
			doc.insertString(doc.getLength(), "Creating meta data.\n", null);
			AttributeName abc = graph.getMetaData().getAttributeName(
					"Betweenness_centrality");
			if (abc == null)
				abc = graph
						.getMetaData()
						.getFactory()
						.createAttributeName("Betweenness_centrality",
								Double.class);
			AttributeName adc = graph.getMetaData().getAttributeName(
					"Degree_centrality");
			if (adc == null)
				adc = graph.getMetaData().getFactory()
						.createAttributeName("Degree_centrality", Double.class);

			// calculate betweeness only on visible part of graph
			doc.insertString(doc.getLength(), "Calculating Betweeness...\n",
					null);
			Set<ONDEXConcept> oc = new HashSet<ONDEXConcept>(
					graph.getVertices());
			Set<ONDEXRelation> or = new HashSet<ONDEXRelation>(graph.getEdges());
			Map<ONDEXConcept, Double> bc = StandardFunctions
					.getBetweenessCentrality(oc, or, graph);
			
			// save betweeness as Attribute
			for (Entry<ONDEXConcept, Double> ent : bc.entrySet()) {
				Attribute attribute = ent.getKey().getAttribute(abc);
				if (attribute == null)
					ent.getKey().createAttribute(abc, ent.getValue(), false);
				else
					attribute.setValue(ent.getValue());
			}

			// colour concept by betweeness
			doc.insertString(doc.getLength(),
					"changing node color by Betweeness.\n", null);
			Annotation.annotateOnColorScaleFill(viewer, bc, null);

			// calculate degree centrality on visible graph
			doc.insertString(doc.getLength(),
					"Calculating Degree Centrality...\n", null);
			Map<ONDEXConcept, Double> dc = StandardFunctions
					.getDegreeCentrality(oc, or, graph);
			// save degree centrality as Attribute
			for (Entry<ONDEXConcept, Double> ent : dc.entrySet()) {
				Attribute attribute = ent.getKey().getAttribute(adc);
				if (attribute == null)
					ent.getKey().createAttribute(adc, ent.getValue(), false);
				else
					attribute.setValue(ent.getValue());
			}

			// scale concept by degree
			doc.insertString(doc.getLength(),
					"changing node size by Degree Centrality.\n", null);
			Annotation.annotateOnSizeScale(viewer, dc, 20, 120, false);

			// finished
			doc.insertString(doc.getLength(), "Finished.", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.BetweennessCentralityDegree");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
