package net.sourceforge.ondex.ovtk2.annotator.shapeconcept;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * Shape by value annotator Allows the selection of shape for a given Attribute
 * value
 * 
 * @author taubertj, hindlem
 */
public class ShapeConceptAnnotator extends OVTK2Annotator implements ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7956800932204481645L;

	/**
	 * Selected attribute name
	 */
	private AttributeName an;

	/**
	 * List of attribute names
	 */
	private JList list;

	/**
	 * selection model for attribute names
	 */
	private AttributeNameListModel anlm;

	/**
	 * the important button
	 */
	private JButton goButton;

	/**
	 * Values for that attribute name
	 */
	private JTextArea text;

	/**
	 * Shape selection box
	 */
	private JComboBox comboBox;

	/**
	 * Shape ID mapping
	 */
	private Map<Shape, Integer> mapping = new Hashtable<Shape, Integer>();

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Constructor initialises internal data structures.
	 * 
	 * @param viewer
	 *            current OVTK2Viewer
	 */
	public ShapeConceptAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);
		setLayout(new SpringLayout());

		comboBox = new JComboBox();
		comboBox.setRenderer(new ComboBoxRenderer());

		for (int i : ONDEXNodeShapes.getAvailableIds()) {
			mapping.put(ONDEXNodeShapes.getShape(i), Integer.valueOf(i));
		}

		// populat shape selection
		for (Shape s : mapping.keySet()) {
			comboBox.addItem(s);
		}

		anlm = new AttributeNameListModel();

		// The magic button
		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		// fill attribute name list
		addAttributeNamesToList();

		if (anlm.getSize() == 0) {
			add(new JLabel("There are no valid attributes in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(new TextAreaFiller(viewer, text));

			add(new JLabel("Select an attribute to annotate concepts with"));
			add(new JScrollPane(list));

			// empty text box for values
			text = new JTextArea(6, 20);
			add(new JScrollPane(text));
			add(comboBox);

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);

	}

	/**
	 * Fills attribute selection list
	 */
	private void addAttributeNamesToList() {
		for (AttributeName attn : graph.getMetaData().getAttributeNames()) {
			if (Comparable.class.isAssignableFrom(attn.getDataType())) {
				Set<ONDEXConcept> concepts = graph.getConceptsOfAttributeName(attn);
				if (concepts != null && concepts.size() > 0 && !AppearanceSynchronizer.attr.contains(attn.getId()))
					anlm.addAttributeName(attn);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = list.getSelectedIndex();
		if (index > -1) {
			// get attribute name
			String attNameId = ((JLabel) list.getModel().getElementAt(index)).getName();
			an = graph.getMetaData().getAttributeName(attNameId);

			// get value list
			String[] values = text.getText().split("\n");
			Set<String> set = new HashSet<String>();
			set.addAll(Arrays.asList(values));

			// get selected shape
			Shape shape = (Shape) comboBox.getSelectedItem();

			// get node shape transformer
			ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
			nodeShapes.setNodeShapeSelection(NodeShapeSelection.MANUAL);

			// iterate over concepts of attribute name
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = c.getAttribute(an);
				if (set.contains(attribute.getValue().toString())) {
					// preserve current shape size
					AffineTransform af = new AffineTransform();
					Rectangle bounds = nodeShapes.transform(c).getBounds();
					double x = bounds.height;
					double y = bounds.width;
					af.scale(x / shape.getBounds().height, y / shape.getBounds().width);
					Shape shape1 = af.createTransformedShape(shape);
					nodeShapes.updateShape(c, shape1, mapping.get(shape));
				}
			}

			// update viewer
			viewer.getVisualizationViewer().getModel().fireStateChanged();
			viewer.getVisualizationViewer().repaint();

			used = true;
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ShapeConcept");
	}

	/**
	 * Paints a given shape as an icon.
	 * 
	 * @author taubertj
	 */
	private class ShapeIcon implements Icon {

		private int width = 20;

		private int height = 20;

		private Shape shape = null;

		public ShapeIcon(Shape shape) {
			this.shape = shape;
			Rectangle2D bounds = shape.getBounds2D();
			this.width = (int) bounds.getWidth();
			this.height = (int) bounds.getHeight();
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();

			AffineTransform transform = g2d.getTransform();
			transform.translate((double) width / 2.0 + 1, (double) height / 2.0 + 1);
			g2d.setTransform(transform);

			g2d.setColor(Color.BLUE);
			g2d.fill(shape);

			g2d.dispose();
		}

		public int getIconWidth() {
			return width;
		}

		public int getIconHeight() {
			return height;
		}
	}

	/**
	 * Renderer for shapes in ComboBox.
	 * 
	 * @author taubertj
	 */
	private class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		// generated
		private static final long serialVersionUID = -7175082164979272459L;

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		/*
		 * This method creates the icon corresponding to the selected value and
		 * returns the set up to display the image.
		 */

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// Set the icon
			Icon icon = new ShapeIcon((Shape) value);
			setIcon(icon);

			return this;
		}
	}

	private class TextAreaFiller implements ListSelectionListener {
		private ONDEXGraph graph = null;

		public TextAreaFiller(OVTK2PropertiesAggregator viewer, JTextArea text) {
			graph = viewer.getONDEXJUNGGraph();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			int index = list.getSelectedIndex();
			if (index > -1) {
				goButton.setEnabled(true);
				AttributeName attNameName = graph.getMetaData().getAttributeName(((JLabel) list.getModel().getElementAt(index)).getName());
				Set<String> attValues = new HashSet<String>();
				for (ONDEXConcept c : graph.getConceptsOfAttributeName(attNameName))
					attValues.add(c.getAttribute(attNameName).getValue().toString());
				text.setText("");
				for (String value : attValues) {
					text.append(value + "\n");
				}
			}
		}
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}
}
