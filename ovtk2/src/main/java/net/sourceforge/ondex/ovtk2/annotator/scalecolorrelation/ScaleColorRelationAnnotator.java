package net.sourceforge.ondex.ovtk2.annotator.scalecolorrelation;

import java.awt.BorderLayout;
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
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

import org.apache.commons.collections15.Transformer;

/**
 * Maps Attribute values on relations to Edge Strokes.
 * 
 * @author taubertj, lysenkoa
 * @version 06.04.2011
 */
public class ScaleColorRelationAnnotator extends OVTK2Annotator implements ListSelectionListener, ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * multiple selection is enabled
	 */
	private ArrayList<AttributeName> targets = null;

	/**
	 * displays selection list of attribute names
	 */
	private JList list;

	/**
	 * used to wrap attribute name list
	 */
	private AttributeNameListModel anlm;

	/**
	 * the important button
	 */
	private JButton goButton;

	/**
	 * edge size input
	 */
	private JFormattedTextField minField, maxField;

	/**
	 * colour input
	 */
	private JFormattedTextField minColour, maxColour, noneColour;

	/**
	 * inverse value scaling and other switches
	 */
	private JCheckBox inverse, noAtt, rainbowColor, preserveNoAtt, logValue;

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
	public ScaleColorRelationAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		setLayout(new SpringLayout());
		anlm = new AttributeNameListModel();

		// option to resize edges
		resize = new JCheckBox("Resize relations");
		resize.setSelected(true);
		add(resize);

		// node sizes
		JPanel sizeConstraints = new JPanel(new SpringLayout());
		JPanel groupScale = new JPanel();
		groupScale.setLayout(new BoxLayout(groupScale, BoxLayout.Y_AXIS));
		groupScale.setBorder(BorderFactory.createEtchedBorder());
		sizeConstraints.add(new JLabel("Min relation size"));
		minField = new JFormattedTextField(1);
		minField.setColumns(5);
		sizeConstraints.add(minField);
		sizeConstraints.add(new JLabel("Max relation size"));
		maxField = new JFormattedTextField(20);
		maxField.setColumns(5);
		sizeConstraints.add(maxField);
		SpringUtilities.makeCompactGrid(sizeConstraints, sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);
		sizeConstraints.setAlignmentX(Component.LEFT_ALIGNMENT);

		groupScale.add(sizeConstraints);

		// use inverse scaling
		inverse = new JCheckBox("Use inverse scaling");
		inverse.setAlignmentX(Component.LEFT_ALIGNMENT);
		groupScale.add(inverse, BorderLayout.PAGE_END);

		// preserve formatting of non-matching edges
		preserveNoAtt = new JCheckBox("Preserve non-matching relations");
		preserveNoAtt.setAlignmentX(Component.LEFT_ALIGNMENT);
		groupScale.add(preserveNoAtt, BorderLayout.PAGE_END);

		add(groupScale);

		// colour edges by value
		color = new JCheckBox("Colour relations");
		add(color);

		// node sizes
		JPanel colorConstraints = new JPanel(new SpringLayout());
		colorConstraints.setToolTipText("Click on the coloured field to change colours");
		colorConstraints.setBorder(BorderFactory.createEtchedBorder());

		colorConstraints.add(new JLabel("Min colour:"));
		minColour = new JFormattedTextField();
		minColour.addMouseListener(cl);
		minColour.setEnabled(false);
		minColour.setBackground(Color.BLUE);
		colorConstraints.add(minColour);

		colorConstraints.add(new JLabel("Max colour:"));
		maxColour = new JFormattedTextField();
		maxColour.addMouseListener(cl);
		maxColour.setEnabled(false);
		maxColour.setBackground(Color.RED);
		colorConstraints.add(maxColour);

		noAtt = new JCheckBox("No attribute colour:");
		colorConstraints.add(noAtt);
		noneColour = new JFormattedTextField();
		noneColour.addMouseListener(cl);
		noneColour.setEnabled(false);
		noneColour.setBackground(Color.BLACK);
		colorConstraints.add(noneColour);

		rainbowColor = new JCheckBox("Colour on rainbow scale");
		rainbowColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == 1001) {
					if (rainbowColor.isSelected()) {
						lastMinChoice = minColour.getBackground();
						minColour.setBackground(ScaleColorRelationAnnotator.this.getBackground());
						minColour.removeMouseListener(cl);
						lastMaxChoice = maxColour.getBackground();
						maxColour.setBackground(ScaleColorRelationAnnotator.this.getBackground());
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

		colorConstraints.add(rainbowColor);
		colorConstraints.add(new JLabel(""));

		SpringUtilities.makeCompactGrid(colorConstraints, colorConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);
		add(colorConstraints);

		// log values first
		logValue = new JCheckBox("Perform log transformation on values");
		add(logValue);

		// the magic button
		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		// get all available attribute names
		for (AttributeName attName : graph.getMetaData().getAttributeNames()) {
			// check its numerically comparable
			if (Number.class.isAssignableFrom(attName.getDataType())) {
				Set<ONDEXRelation> relations = graph.getRelationsOfAttributeName(attName);
				if (relations != null) {
					// check relations exists on this attribute name
					if (relations.size() > 0 && !AppearanceSynchronizer.attr.contains(attName.getId())) {
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

			add(new JLabel("Select an attribute to annotate relations with"));
			add(new JScrollPane(list));

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);
	}

	/**
	 * Resizes edges of the graph.
	 * 
	 * @param targMin
	 *            the smallest edge size
	 * @param targMax
	 *            the largest edge size
	 * @param inverse
	 *            is this a inverse relationship
	 * @param color
	 *            colour edges red/green
	 */
	private void updateGraph(final int targMin, int targMax, boolean inverse, boolean color, boolean resize, boolean useNoAttColour, Color... colors) {

		int sizeRange = targMax - targMin;
		System.out.println("Annotation range: " + sizeRange);

		if (colors[2] != null) {
			// setting special alpha value for none matching relations?
			colors[2] = new Color(colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue(), 200);
		}

		if (targets.size() == 1) {
			// everything can be converted to a double :-)
			double minimum = Double.POSITIVE_INFINITY;
			double maximum = Double.NEGATIVE_INFINITY;

			Map<ONDEXRelation, Double> values = new HashMap<ONDEXRelation, Double>();
			Set<ONDEXRelation> noAttvalues = new HashSet<ONDEXRelation>();

			// get attribute name values
			double value;
			for (ONDEXRelation relation : graph.getRelations()) {
				Attribute attr = relation.getAttribute(targets.get(0));
				if (attr == null) {
					noAttvalues.add(relation);
					continue;
				}

				// get value
				value = ((Number) attr.getValue()).doubleValue();
				if (logValue.isSelected())
					value = Math.log(value);

				// store value per relation
				values.put(relation, value);

				// set min and max of values
				if (value < minimum) {
					minimum = value;
				}
				if (value > maximum) {
					maximum = value;
				}
			}

			System.out.println("Min: " + minimum + " Max: " + maximum);

			// transform size of edges
			final Map<ONDEXRelation, Integer> amplification = new HashMap<ONDEXRelation, Integer>();
			Map<ONDEXRelation, Double> percent = new HashMap<ONDEXRelation, Double>();

			// calculate amplification values
			for (ONDEXRelation r : values.keySet()) {
				double percentBase = ((values.get(r) - minimum) / (maximum - minimum));
				// this is the case when there is only one value and range is
				// zero
				if (percentBase > 1) {
					percentBase = 1;
				}
				// this is the base size
				if (percentBase == 0) {
					amplification.put(r, (int) Math.floor(targMin + (sizeRange / 2)));
				}
				percent.put(r, percentBase);
				if (resize) {
					if (inverse)
						percentBase = 1 - percentBase;
					double width = targMin + (percentBase * sizeRange);
					if (width <= 0)
						width = 1;
					amplification.put(r, (int) Math.floor(width));
				}
			}

			if (preserveNoAtt.isSelected()) {
				// update amplifications for non-matching edges
				ONDEXEdgeStrokes edgeStrokes = viewer.getEdgeStrokes();
				Transformer<ONDEXRelation, Integer> old = edgeStrokes.getEdgeSizeTransformer();
				if (old != null)
					for (ONDEXRelation r : noAttvalues) {
						amplification.put(r, old.transform(r));
					}
			}

			if (resize) {
				// set amplification of new edge stroke size
				ONDEXEdgeStrokes edgeStrokes = viewer.getEdgeStrokes();
				Transformer<ONDEXRelation, Integer> esf = new Transformer<ONDEXRelation, Integer>() {
					@Override
					public Integer transform(ONDEXRelation arg0) {
						Integer value = amplification.get(arg0);
						if (value == null)
							value = targMin;
						return value;
					}
				};
				edgeStrokes.setEdgeSizes(esf);
			}

			// colour edges
			if (color) {
				for (ONDEXRelation r : values.keySet()) {
					// decide on colouring scheme
					if (rainbowColor.isSelected()) {
						Annotation.setColor(viewer, r, Annotation.getRainbowColor(percent.get(r)));
					} else {
						Annotation.setColor(viewer, r, Annotation.getColorRatio(colors[0], colors[1], percent.get(r)));
					}
				}

				// separate colour for edges without attribute
				if (useNoAttColour) {
					for (ONDEXRelation r : noAttvalues) {
						Annotation.setColor(viewer, r, colors[2]);
					}
				}
			}

			viewer.getVisualizationViewer().getModel().fireStateChanged();

			// update viewer
			viewer.updateViewer(null);

			// clean up
			values.clear();
			noAttvalues.clear();
			percent.clear();
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ScaleColorRelation");
	}

	/**
	 * Checks for selections in ConceptClass list.
	 */
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

			// swap around
			if (min > max) {
				Integer temp = min;
				min = max;
				max = temp;
				minField.setValue(min);
				maxField.setValue(max);
			}

			Color[] colours = new Color[3];
			if (color.isSelected()) {
				if (noAtt.isSelected()) {
					colours[2] = noneColour.getBackground();
				}
				colours[0] = minColour.getBackground();
				colours[1] = maxColour.getBackground();
			}
			updateGraph(min, max, inverse.isSelected(), color.isSelected(), resize.isSelected(), noAtt.isSelected(), colours);

			used = true;
		}
	}

	private class ChangeColourOnClickListener implements MouseListener {

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
	public boolean hasBeenUsed() {
		return used;
	}
}
