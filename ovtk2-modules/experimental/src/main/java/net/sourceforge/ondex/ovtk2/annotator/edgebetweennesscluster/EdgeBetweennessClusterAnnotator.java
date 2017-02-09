package net.sourceforge.ondex.ovtk2.annotator.edgebetweennesscluster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.AbstractONDEXGraphMetaData;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.OVTKProgressMonitor;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.tools.threading.monitoring.IndeterminateProcessAdapter;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;

public class EdgeBetweennessClusterAnnotator extends OVTK2Annotator implements
		ActionListener {

	public JSlider slider;

	public JTextField text = new JTextField("has_high_betweenness");

	public JButton go = new JButton("Annotate");

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * generated
	 */
	private static final long serialVersionUID = -1857828349559114298L;

	public EdgeBetweennessClusterAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		// initialise slider according to current graph
		int count = graph.getEdgeCount();
		slider = new JSlider(0, count, count / 40);
		slider.setMajorTickSpacing(count / 4);
		slider.setMinorTickSpacing(count / 16);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		this.setLayout(new SpringLayout());

		this.add(new JLabel("Number of relations to mark:"));

		this.add(slider);

		this.add(new JLabel("Attribute name to assign:"));

		this.add(text);

		this.add(go);

		go.addActionListener(this);
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);

	}

	@Override
	public String getName() {
		return Config.language
				.getProperty("Name.Menu.Annotator.EdgeBetweennessCluster");
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (text.getText().trim().length() > 0) {

			// check for not allowed white spaces in meta data
			if (AbstractONDEXGraphMetaData.WHITESPACE.matcher(text.getText())
					.find()) {
				JOptionPane.showInternalMessageDialog(OVTK2Desktop
						.getInstance().getDesktopPane(),
						"Attribute name not allowed to contain white spaces");
				return;
			}

			if (slider.getValue() > 50) {
				int option = JOptionPane.showInternalConfirmDialog(OVTK2Desktop
						.getInstance().getDesktopPane(),
						"<html>You have selected to mark more than 50 edges."
								+ "<br/>This can take a long time."
								+ "<br/>Do you wish to continue?</html>",
						"Processing time warning", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.NO_OPTION)
					return;
			}

			System.out.println("Marking " + slider.getValue()
					+ " out of a total " + graph.getEdgeCount() + " edges.");

			IndeterminateProcessAdapter p = new IndeterminateProcessAdapter() {
				public void task() {

					// make sure attribute name exist
					AttributeName an = graph.getMetaData().getAttributeName(
							text.getText());
					if (an == null)
						an = graph
								.getMetaData()
								.getFactory()
								.createAttributeName(text.getText(),
										Boolean.class);
					if (!an.getDataType().equals(Boolean.class)) {
						JOptionPane.showInternalMessageDialog(OVTK2Desktop
								.getInstance().getDesktopPane(),
								"Attribute name is not of type Boolean.");
					}

					// score edges by betweenness centrality and remove highest
					Set<ONDEXRelation> edges_removed = new HashSet<ONDEXRelation>();
					for (int k = 0; k < slider.getValue(); k++) {
						BetweennessCentrality<ONDEXConcept, ONDEXRelation> bc = new BetweennessCentrality<ONDEXConcept, ONDEXRelation>(
								graph);
						ONDEXRelation to_remove = null;
						double score = 0;
						for (ONDEXRelation edge : graph.getEdges())
							if (bc.getEdgeScore(edge) > score) {
								to_remove = edge;
								score = bc.getEdgeScore(edge);
							}
						edges_removed.add(to_remove);
						graph.setVisibility(to_remove, false);
					}

					// re-add edges
					graph.setVisibility(edges_removed, true);

					// add attributes to original graph
					for (ONDEXRelation edge : edges_removed) {
						edge.createAttribute(an, Boolean.TRUE, false);
					}

					used = true;
				}
			};
			p.start();
			OVTKProgressMonitor
					.start(OVTK2Desktop.getInstance().getMainFrame(),
							"Calculating", p);
		} else {
			JOptionPane.showInternalMessageDialog(this,
					"No attribute name given.");
		}

	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
