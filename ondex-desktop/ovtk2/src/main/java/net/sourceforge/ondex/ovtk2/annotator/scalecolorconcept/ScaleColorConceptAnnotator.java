package net.sourceforge.ondex.ovtk2.annotator.scalecolorconcept;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
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
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Annotator for visualising continuous attribute values
 * 
 * @author lysenkoa
 */
public class ScaleColorConceptAnnotator extends OVTK2Annotator implements ActionListener, ListSelectionListener {

	/**
	 * used to wrap attribute name list
	 */
	private AttributeNameListModel anlm;

	/**
	 * displays selection list of attribute names
	 */
	private JList list;

	/**
	 * multiple selection is enabled
	 */
	private ArrayList<AttributeName> targets = null;

	/**
	 * the important button
	 */
	private JButton goButton;

	/**
	 * node size input
	 */
	private JFormattedTextField minField, maxField, noAttSize;

	/**
	 * colour input
	 */
	private JFormattedTextField minColour, maxColour, noneColour;

	/**
	 * max and min value cutoffs
	 */
	private JFormattedTextField minValue, maxValue;

	/**
	 * inverse value scaling and other switches
	 */
	private JCheckBox inverse, noAtt, noAttSizeChk, rainbowColor, drawColor, fillColor, logValue;

	/**
	 * track last colour
	 */
	private Color lastMinChoice, lastMaxChoice;

	/**
	 * colour and resize edges choice
	 */
	private JCheckBox color, resize;

	/**
	 * changes colours, e.g. min and max colour
	 */
	private ChangeColourOnClickListener cl = new ChangeColourOnClickListener();

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
	public ScaleColorConceptAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		anlm = new AttributeNameListModel();

		resize = new JCheckBox("Resize concepts");
		resize.setSelected(true);
		add(resize);

		JPanel sizeConstraints = new JPanel(new SpringLayout());
		sizeConstraints.setBorder(BorderFactory.createEtchedBorder());
		sizeConstraints.add(new JLabel("Min Concept size"));
		minField = new JFormattedTextField(1);
		minField.setColumns(5);
		sizeConstraints.add(minField);
		sizeConstraints.add(new JLabel("Max Concept size"));
		maxField = new JFormattedTextField(20);
		maxField.setColumns(5);
		sizeConstraints.add(maxField);
		noAttSizeChk = new JCheckBox("No attribute size:");
		sizeConstraints.add(noAttSizeChk);
		noAttSize = new JFormattedTextField(12);
		noAttSize.setColumns(5);
		sizeConstraints.add(noAttSize);
		// use inverse scaling
		inverse = new JCheckBox("Use inverse scaling");
		sizeConstraints.add(inverse);
		sizeConstraints.add(new JLabel());
		SpringUtilities.makeCompactGrid(sizeConstraints, sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);
		sizeConstraints.setAlignmentX(Component.LEFT_ALIGNMENT);

		add(sizeConstraints);

		// colour concepts by value
		color = new JCheckBox("Colour concepts");
		resize.setSelected(true);
		add(color);

		JPanel groupScale = new JPanel();
		groupScale.setLayout(new BoxLayout(groupScale, BoxLayout.Y_AXIS));
		groupScale.setBorder(BorderFactory.createEtchedBorder());

		JPanel colorConstraints = new JPanel(new SpringLayout());
		colorConstraints.setToolTipText("Click on the coloured field to change colours");
		colorConstraints.setBorder(BorderFactory.createEtchedBorder());

		colorConstraints.add(new JLabel("Min Colour:"));
		minColour = new JFormattedTextField();
		minColour.setBorder(BorderFactory.createLoweredBevelBorder());
		minColour.addMouseListener(cl);
		minColour.setEnabled(false);
		minColour.setBackground(Color.RED);
		colorConstraints.add(minColour);

		colorConstraints.add(new JLabel("Min Value:"));
		minValue = new JFormattedTextField();
		minValue.setBorder(BorderFactory.createLoweredBevelBorder());
		colorConstraints.add(minValue);

		colorConstraints.add(new JLabel("Max Colour:"));
		maxColour = new JFormattedTextField();
		maxColour.setBorder(BorderFactory.createLoweredBevelBorder());
		maxColour.addMouseListener(cl);
		maxColour.setEnabled(false);
		maxColour.setBackground(Color.GREEN);
		colorConstraints.add(maxColour);

		colorConstraints.add(new JLabel("Max Value:"));
		maxValue = new JFormattedTextField();
		maxValue.setBorder(BorderFactory.createLoweredBevelBorder());
		colorConstraints.add(maxValue);

		rainbowColor = new JCheckBox("Colour on rainbow scale");
		rainbowColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == 1001) {
					if (rainbowColor.isSelected()) {
						lastMinChoice = minColour.getBackground();
						minColour.setBackground(ScaleColorConceptAnnotator.this.getBackground());
						minColour.removeMouseListener(cl);
						lastMaxChoice = maxColour.getBackground();
						maxColour.setBackground(ScaleColorConceptAnnotator.this.getBackground());
						maxColour.removeMouseListener(cl);
					} else {
						minColour.setBackground(lastMinChoice);
						minColour.addMouseListener(cl);
						maxColour.setBackground(lastMaxChoice);
						maxColour.addMouseListener(cl);
					}
				}

			}
		});

		noAtt = new JCheckBox("No attribute colour:");
		colorConstraints.add(noAtt);
		noneColour = new JFormattedTextField();
		noneColour.addMouseListener(cl);
		noneColour.setEnabled(false);
		noneColour.setBorder(BorderFactory.createLoweredBevelBorder());
		noneColour.setBackground(Color.WHITE);
		colorConstraints.add(noneColour);

		colorConstraints.add(rainbowColor);
		colorConstraints.add(new JLabel(""));

		fillColor = new JCheckBox("Map to fill color");
		fillColor.setSelected(true);
		colorConstraints.add(fillColor);
		colorConstraints.add(new JLabel(""));

		drawColor = new JCheckBox("Map to draw color");
		colorConstraints.add(drawColor);
		colorConstraints.add(new JLabel(""));

		SpringUtilities.makeCompactGrid(colorConstraints, 8, 2, 5, 5, 5, 5);

		add(colorConstraints);

		// log values first
		logValue = new JCheckBox("Perform log transformation on values");
		add(logValue);

		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		// get all available attribute names
		for (AttributeName attName : graph.getMetaData().getAttributeNames()) {
			// check its numerically comparable
			if (Number.class.isAssignableFrom(attName.getDataType())) {
				Set<ONDEXConcept> concpets = graph.getConceptsOfAttributeName(attName);
				if (concpets != null) {
					// check relations exists on this attribute name
					if (concpets.size() > 0 && !AppearanceSynchronizer.attr.contains(attName.getId())) {
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
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select an attribute to annotate concepts with"));
			add(new JScrollPane(list));

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);

	}

	private static final long serialVersionUID = -1774711090515449500L;

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ScaleColorConcept");
	}

	class ChangeColourOnClickListener implements MouseListener {

		public ChangeColourOnClickListener() {
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			Component c = arg0.getComponent();
			c.setBackground(JColorChooser.showDialog(c, "Select new colour", c.getBackground()));
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Map<ONDEXConcept, Double> toAnnotate = new HashMap<ONDEXConcept, Double>();
		Set<ONDEXConcept> noAttSet = new HashSet<ONDEXConcept>();
		double value;
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute temp = c.getAttribute(targets.get(0));
			if (temp != null) {
				// get value
				value = ((Number) temp.getValue()).doubleValue();
				if (logValue.isSelected())
					value = Math.log(value);
				toAnnotate.put(c, value);
			} else {
				noAttSet.add(c);
				if (noAttSizeChk.isSelected()) {
					toAnnotate.put(c, (Double.valueOf(noAttSize.getText())));
				}
			}
		}

		System.err.println(noAtt.isSelected() + " : " + noAttSet.size());
		if (color.isSelected()) {
			double[] minmax = null;
			try {
				double min = Double.parseDouble(minValue.getText());
				double max = Double.parseDouble(maxValue.getText());
				minmax = new double[2];
				minmax[0] = min;
				minmax[1] = max;
			} catch (NumberFormatException nfe) {
				// ignore
			}
			if (rainbowColor.isSelected()) {
				if (this.fillColor.isSelected())
					Annotation.annotateOnColorScaleFill(viewer, toAnnotate, minmax);
				if (this.drawColor.isSelected())
					Annotation.annotateOnColorScaleDraw(viewer, toAnnotate, minmax);
			} else {
				if (this.fillColor.isSelected())
					Annotation.annotateOnColorScaleFill(viewer, toAnnotate, minmax, minColour.getBackground(), maxColour.getBackground());
				if (this.drawColor.isSelected())
					Annotation.annotateOnColorScaleDraw(viewer, toAnnotate, minmax, minColour.getBackground(), maxColour.getBackground());
			}
		}

		if (resize.isSelected())
			Annotation.annotateOnSizeScale(viewer, toAnnotate, Integer.valueOf(minField.getText()), Integer.valueOf(maxField.getText()), inverse.isSelected());

		if (noAtt.isSelected()) {
			if (this.fillColor.isSelected())
				Annotation.setNodeFillColors(viewer, noAttSet, noneColour.getBackground());
			if (this.drawColor.isSelected())
				Annotation.setNodeDrawColors(viewer, noAttSet, noneColour.getBackground());
		}

		if (noAttSizeChk.isSelected()) {
			if (Double.valueOf(noAttSize.getText()) == 0d) {
				for (ONDEXConcept nc : noAttSet) {
					graph.setVisibility(nc, false);
				}
			}
		}

		used = true;
		viewer.updateViewer(null);
		viewer.getVisualizationViewer().getModel().fireStateChanged();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			goButton.setEnabled(true);
			targets = new ArrayList<AttributeName>();
			for (int i : indices) {
				targets.add(((AttributeNameListModel) list.getModel()).getAttributeNameAt(i));
			}
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
