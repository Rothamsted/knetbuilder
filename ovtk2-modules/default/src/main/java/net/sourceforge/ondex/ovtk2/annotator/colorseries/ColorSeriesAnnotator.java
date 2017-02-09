package net.sourceforge.ondex.ovtk2.annotator.colorseries;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
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

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;

/**
 * Cycles through possible colour values of nodes.
 * 
 * @author taubertj
 * 
 */
public class ColorSeriesAnnotator extends OVTK2Annotator implements
		ActionListener, ListSelectionListener {

	/**
	 * Changes colour on click
	 * 
	 * @author taubertj
	 * 
	 */
	private class ChangeColourOnClickListener implements MouseListener {

		public ChangeColourOnClickListener() {
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			Component c = arg0.getComponent();
			c.setBackground(JColorChooser.showDialog(c, "Select new colour",
					c.getBackground()));
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * listen for click events
	 */
	private ChangeColourOnClickListener cl = new ChangeColourOnClickListener();

	/**
	 * no selection
	 */
	private int currentIndex = -1;

	/**
	 * selection list
	 */
	private JList list;

	/**
	 * colour configuration
	 */
	private JFormattedTextField minColour, maxColour, medColour;

	/**
	 * which colour to modify
	 */
	private JCheckBox rainbow, drawColor, fillColor;

	/**
	 * selected attributes
	 */
	private List<AttributeName> selection = new ArrayList<AttributeName>();

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Calls super constructor and builds GUI
	 * 
	 * @param viewer
	 */
	public ColorSeriesAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		initGUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();

		// one step back
		if (cmd.equals("backward")) {
			if (currentIndex > 0 && currentIndex < selection.size()) {
				currentIndex--;
			} else {
				currentIndex = selection.size() - 1;
			}
		}

		// one step forward
		if (cmd.equals("forward")) {
			if (currentIndex < selection.size() - 1) {
				currentIndex++;
			} else {
				currentIndex = 0;
			}
		}

		// safety check
		AttributeName an = selection.get(currentIndex);
		if (an == null)
			return;

		// modify viewer title to reflect current step
		String name = viewer.getTitle();
		name = name.replaceAll(" \\(.+\\)$", "");
		viewer.setTitle(name + " (" + an.getId() + ")");

		// get range of values
		Map<ONDEXConcept, Double> values = new HashMap<ONDEXConcept, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
			double value = ((Number) c.getAttribute(an).getValue())
					.doubleValue();
			if (value < min)
				min = value;
			if (value > max)
				max = value;
			values.put(c, value);
		}

		// normalise to 0..1 interval
		double minmax = max - min;
		if (minmax != 0)
			for (ONDEXConcept c : values.keySet()) {
				double value = values.get(c);
				value = (value - min) / minmax;
				values.put(c, value);
			}

		// annotate graph
		if (rainbow.isSelected()) {
			if (drawColor.isSelected())
				Annotation.annotateOnColorScaleDraw(viewer, values, null);
			if (fillColor.isSelected())
				Annotation.annotateOnColorScaleFill(viewer, values, null);
		} else {
			// linear gradient
			Color[] colors = new Color[512];
			Color c1 = minColour.getBackground();
			Color c2 = medColour.getBackground();
			Color c3 = maxColour.getBackground();
			// first blend min to median
			for (int i = 0; i < 256; i++) {
				float ratio = (float) i / (float) 256;
				int red, green, blue;
				red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
				green = (int) (c2.getGreen() * ratio + c1.getGreen()
						* (1 - ratio));
				blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
				Color c = new Color(red, green, blue);
				colors[i] = c;
			}
			// second blend median to max
			for (int i = 256; i < 512; i++) {
				float ratio = (float) (i - 256) / (float) 256;
				int red, green, blue;
				red = (int) (c3.getRed() * ratio + c2.getRed() * (1 - ratio));
				green = (int) (c3.getGreen() * ratio + c2.getGreen()
						* (1 - ratio));
				blue = (int) (c3.getBlue() * ratio + c2.getBlue() * (1 - ratio));
				Color c = new Color(red, green, blue);
				colors[i] = c;
			}

			if (drawColor.isSelected())
				Annotation.annotateOnColorScaleDraw(viewer, values, null,
						colors);
			if (fillColor.isSelected())
				Annotation.annotateOnColorScaleFill(viewer, values, null,
						colors);
		}

		// trigger re-draw
		viewer.updateViewer(null);
		viewer.getVisualizationViewer().getModel().fireStateChanged();
		viewer.getVisualizationViewer().updateUI();

		used = true;
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ColorSeries");
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		if (lsm.isSelectionEmpty()) {
			selection.clear();
		} else {
			selection.clear();
			// Find out which indexes are selected.
			for (Object o : list.getSelectedValues()) {
				selection.add((AttributeName) o);
			}
		}
	}

	/**
	 * Construct GUI
	 */
	private void initGUI() {

		setLayout(new SpringLayout());

		// selection of attribute names to play
		JPanel anSelection = new JPanel(new GridLayout(1, 1));
		anSelection.setBorder(BorderFactory
				.createTitledBorder("Selection AttributeNames"));
		this.add(anSelection);

		// get valid attribute names
		List<AttributeName> ans = new ArrayList<AttributeName>();
		for (AttributeName an : graph.getMetaData().getAttributeNames()) {
			// check there are populated
			if (graph.getConceptsOfAttributeName(an).size() > 0) {
				// check for number attributes only
				if (Number.class.isAssignableFrom(an.getDataType())
						&& !AppearanceSynchronizer.attr.contains(an.getId())) {
					ans.add(an);
				}
			}
		}
		// sort attribute names
		final Pattern numberPattern = Pattern.compile(".+?(\\d+)$");
		AttributeName[] sort = ans.toArray(new AttributeName[0]);
		Arrays.sort(sort, new Comparator<AttributeName>() {

			@Override
			public int compare(AttributeName o1, AttributeName o2) {
				String an1 = o1.getId();
				String an2 = o2.getId();
				int n1 = 0;
				int n2 = 0;
				try {
					Matcher m1 = numberPattern.matcher(an1);
					if (m1.find()) {
						n1 = Integer.parseInt(m1.group(1));
						an1 = an1.substring(0, an1.length()
								- m1.group(1).length());
					}
					Matcher m2 = numberPattern.matcher(an2);
					if (m2.find()) {
						n2 = Integer.parseInt(m2.group(1));
						an2 = an2.substring(0, an2.length()
								- m2.group(1).length());
					}
				} catch (NumberFormatException nfe) {
					// ignore
				}
				if (an1.compareTo(an2) == 0)
					return n1 - n2;
				else
					return an1.compareTo(an2);
			}
		});

		// list of attribute names
		list = new JList(sort);
		ListSelectionModel listSelectionModel = list.getSelectionModel();
		listSelectionModel.addListSelectionListener(this);
		anSelection.add(new JScrollPane(list));

		JPanel colorConstraints = new JPanel(new SpringLayout());
		colorConstraints
				.setToolTipText("Click on the coloured field to change colours");
		colorConstraints.setBorder(BorderFactory
				.createTitledBorder("Select colours"));
		this.add(colorConstraints);

		colorConstraints.add(new JLabel("Min Colour:"));
		minColour = new JFormattedTextField();
		minColour.setBorder(BorderFactory.createLoweredBevelBorder());
		minColour.addMouseListener(cl);
		minColour.setEnabled(false);
		minColour.setBackground(Color.RED);
		colorConstraints.add(minColour);

		colorConstraints.add(new JLabel("Median Colour:"));
		medColour = new JFormattedTextField();
		medColour.setBorder(BorderFactory.createLoweredBevelBorder());
		medColour.addMouseListener(cl);
		medColour.setEnabled(false);
		medColour.setBackground(Color.YELLOW);
		colorConstraints.add(medColour);

		colorConstraints.add(new JLabel("Max Colour:"));
		maxColour = new JFormattedTextField();
		maxColour.setBorder(BorderFactory.createLoweredBevelBorder());
		maxColour.addMouseListener(cl);
		maxColour.setEnabled(false);
		maxColour.setBackground(Color.GREEN);
		colorConstraints.add(maxColour);

		rainbow = new JCheckBox("Color using rainbow scale");
		colorConstraints.add(rainbow);
		colorConstraints.add(new JLabel("                 "));

		SpringUtilities.makeCompactGrid(colorConstraints, 4, 2, 5, 5, 5, 5);

		JPanel colorControl = new JPanel(new SpringLayout());
		colorControl.setBorder(BorderFactory
				.createTitledBorder("Colour settings"));
		this.add(colorControl);

		drawColor = new JCheckBox("Use draw colour?");
		drawColor.setSelected(true);
		colorControl.add(drawColor);

		fillColor = new JCheckBox("Use fill colour?");
		colorControl.add(fillColor);

		SpringUtilities.makeCompactGrid(colorControl, 2, 1, 5, 5, 5, 5);

		// control flow
		JPanel anControl = new JPanel(new SpringLayout());
		anControl.setBorder(BorderFactory.createTitledBorder("Control"));
		this.add(anControl);

		// backward button
		JButton backward = new JButton("backward");
		backward.setActionCommand("backward");
		backward.addActionListener(this);
		anControl.add(backward);

		// forward button
		JButton forward = new JButton("forward");
		forward.setActionCommand("forward");
		forward.addActionListener(this);
		anControl.add(forward);

		SpringUtilities.makeCompactGrid(anControl, 1, 2, 5, 5, 5, 5);

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
