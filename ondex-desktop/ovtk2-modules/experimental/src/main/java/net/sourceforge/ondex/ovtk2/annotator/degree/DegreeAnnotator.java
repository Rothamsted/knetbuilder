package net.sourceforge.ondex.ovtk2.annotator.degree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

/**
 * Annotates graph with in and out degree of nodes
 * 
 * @author taubertj
 * 
 */
public class DegreeAnnotator extends OVTK2Annotator implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8925802097579439295L;

	private ButtonGroup selection = new ButtonGroup();

	private JButton button = new JButton("Annotate Graph");
	
	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	public DegreeAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		
		initGUI();
	}

	private void initGUI() {

		setLayout(new SpringLayout());

		add(new JLabel("Choose which concept degree to use:"));

		JRadioButton inButton = new JRadioButton("in degree");
		inButton.setSelected(true);
		inButton.setActionCommand("IN");
		selection.add(inButton);
		add(inButton);

		JRadioButton outButton = new JRadioButton("out degree");
		outButton.setActionCommand("OUT");
		selection.add(outButton);
		add(outButton);

		JRadioButton bothButton = new JRadioButton("in and out degree");
		bothButton.setActionCommand("BOTH");
		selection.add(bothButton);
		add(bothButton);

		button.addActionListener(this);
		add(button);
		
		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
                5, 5, 5);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		AttributeName anIn = graph.getMetaData().getAttributeName("IN_DEGREE");
		if (anIn == null)
			anIn = graph.getMetaData().getFactory()
					.createAttributeName("IN_DEGREE", Integer.class);

		AttributeName anOut = graph.getMetaData().getAttributeName("OUT_DEGREE");
		if (anOut == null)
			anOut = graph.getMetaData().getFactory()
					.createAttributeName("OUT_DEGREE", Integer.class);

		AttributeName anBoth = graph.getMetaData().getAttributeName(
				"BOTH_DEGREE");
		if (anBoth == null)
			anBoth = graph.getMetaData().getFactory()
					.createAttributeName("BOTH_DEGREE", Integer.class);

		String cmd = selection.getSelection().getActionCommand();
		if (cmd.equals("IN")) {
			for (ONDEXConcept c : graph.getVertices()) {
				Attribute in = c.getAttribute(anIn);
				if (in == null) {
					c.createAttribute(anIn,
							Integer.valueOf(graph.getInEdges(c).size()), false);
				} else {
					in.setValue(Integer.valueOf(graph.getInEdges(c).size()));
				}
			}
		}

		else if (cmd.equals("OUT")) {
			for (ONDEXConcept c : graph.getVertices()) {
				Attribute out = c.getAttribute(anOut);
				if (out == null) {
					c.createAttribute(anOut,
							Integer.valueOf(graph.getOutEdges(c).size()), false);
				} else {
					out.setValue(Integer.valueOf(graph.getOutEdges(c).size()));
				}
			}
		}

		else if (cmd.equals("BOTH")) {
			for (ONDEXConcept c : graph.getVertices()) {
				Attribute both = c.getAttribute(anBoth);
				if (both == null) {
					c.createAttribute(anBoth,
							Integer.valueOf(graph.getIncidentEdges(c).size()),
							false);
				} else {
					both.setValue(Integer.valueOf(graph.getIncidentEdges(c)
							.size()));
				}
			}
		}
		
		used = true;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.Degree");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

}
