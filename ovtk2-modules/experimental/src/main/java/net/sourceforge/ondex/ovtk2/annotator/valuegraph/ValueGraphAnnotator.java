package net.sourceforge.ondex.ovtk2.annotator.valuegraph;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.annotator.scaleconcept.ReportingListTransferHandler;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableEditor;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

import org.apache.commons.collections15.Transformer;

/**
 * Renders time series or other values as little graphs on nodes.
 * 
 * @author taubertj
 * 
 */
public class ValueGraphAnnotator extends OVTK2Annotator implements
		ActionListener, ListSelectionListener {

	class DeleteCellEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {

		// generated
		private static final long serialVersionUID = 8483654515815820628L;

		// button with icon on
		private JButton button;

		// current table
		private JTable table;

		/**
		 * Returns a delete button as editor.
		 */
		public DeleteCellEditor() {

			File imgLocation = new File(
					"config/toolbarButtonGraphics/general/delete16.gif");
			URL imageURL = null;

			try {
				imageURL = imgLocation.toURI().toURL();
			} catch (MalformedURLException mue) {
				System.err.println(mue.getMessage());
			}

			button = new JButton(new ImageIcon(imageURL));
			button.addActionListener(this);
			button.setBorderPainted(false);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();

			// remove row if not last one
			int row = Integer.parseInt(cmd);
			if (table.getRowCount() > 0) {
				series.remove(table.getValueAt(row, 0));
				((DefaultTableModel) table.getModel()).removeRow(row);
			}

			if (table.getRowCount() == 0)
				goButton.setEnabled(false);
		}

		public Object getCellEditorValue() {
			return button.getIcon();
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.table = table;
			button.setActionCommand("" + row);
			return button;
		}

	}

	/**
	 * Only allow edit of colour field.
	 * 
	 * @author taubertj
	 * 
	 */
	class NonEditTableModel extends DefaultTableModel {

		/**
		 * generated
		 */
		private static final long serialVersionUID = 5353125188118750131L;

		// show icon in last column
		private ImageIcon icon = null;

		public NonEditTableModel() {
			File imgLocation = new File(
					"config/toolbarButtonGraphics/general/delete16.gif");
			URL imageURL = null;

			try {
				imageURL = imgLocation.toURI().toURL();
			} catch (MalformedURLException mue) {
				System.err.println(mue.getMessage());
			}
			icon = new ImageIcon(imageURL);
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 3)
				return ImageIcon.class;
			return super.getColumnClass(col);
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 3)
				return icon;
			return super.getValueAt(row, col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 2 || columnIndex == 3;
		}
	}

	/**
	 * generated
	 */
	private static final long serialVersionUID = 6790201866929855338L;

	/**
	 * to add selection as series
	 */
	private JButton addButton;

	/**
	 * used to wrap attribute name list
	 */
	private AttributeNameListModel anlm;

	/**
	 * display bar charts instead
	 */
	private JCheckBox barcharts;

	/**
	 * contains colour mapping
	 */
	private Map<Integer, Color> colours = new HashMap<Integer, Color>();

	/**
	 * calculate value range global
	 */
	private JCheckBox globalRange;

	/**
	 * the important button
	 */
	private JButton goButton;

	/**
	 * displays selection list
	 */
	private JList list;

	/**
	 * chart maximum diameter
	 */
	private JFormattedTextField maxField;

	/**
	 * Capture series
	 */
	private NonEditTableModel model = new NonEditTableModel();

	/**
	 * for generating random colours
	 */
	private Random numGen = new Random();

	/**
	 * Save previous shapes used
	 */
	private Transformer<ONDEXConcept, Shape> oldShapes;

	/**
	 * contains series data
	 */
	private Map<Integer, List<AttributeName>> series = new HashMap<Integer, List<AttributeName>>();

	/**
	 * multiple selection is enabled
	 */
	private List<AttributeName> targets = null;

	/**
	 * draw charts in 3D mode
	 */
	private JCheckBox use3D;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	/**
	 * Initialises GUI
	 * 
	 * @param viewer
	 */
	public ValueGraphAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		// save the shapes
		oldShapes = viewer.getVisualizationViewer().getRenderContext()
				.getVertexShapeTransformer();

		setLayout(new SpringLayout());

		anlm = new AttributeNameListModel();

		// node sizes
		JPanel sizeConstraints = new JPanel(new SpringLayout());
		sizeConstraints.add(new JLabel("Size of chart"));
		maxField = new JFormattedTextField(100);
		maxField.setColumns(5);
		sizeConstraints.add(maxField);
		SpringUtilities.makeCompactGrid(sizeConstraints,
				sizeConstraints.getComponentCount() / 2, 2, 5, 5, 5, 5);

		// all settings as head row
		JPanel settings = new JPanel(new FlowLayout());
		add(settings);

		settings.add(sizeConstraints);

		// toggle 3D mode for charts
		use3D = new JCheckBox("Use 3D effect");
		use3D.setToolTipText("Draw charts using 3D effect instead of plain 2D");
		use3D.setSelected(true);
		settings.add(use3D);

		// display bar charts for values
		barcharts = new JCheckBox("Display barcharts");
		barcharts
				.setToolTipText("Replaces node shapes with barcharts displaying selected values");
		settings.add(barcharts);

		// how to calculate value range
		globalRange = new JCheckBox("Calculate range globally");
		globalRange
				.setToolTipText("Calculate value range globally across all visible concepts");
		settings.add(globalRange);

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());
		list.setTransferHandler(new ReportingListTransferHandler());
		list.setDragEnabled(true);

		// to add list selection as new series
		addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.setEnabled(false);
		addButton.addActionListener(this);

		// captures series data
		model.setColumnIdentifiers(new String[] { "Series", "Attributes",
				"Color", "" });
		JTable table = new JTable(model);

		ColorTableEditor editor = new ColorTableEditor();
		ColorTableCellRenderer renderer = new ColorTableCellRenderer(true);
		table.setDefaultEditor(Color.class, editor);
		table.setDefaultRenderer(Color.class, renderer);

		TableColumn id = table.getColumnModel().getColumn(0);
		id.setMaxWidth(50);
		id.setMinWidth(50);

		TableColumn color = table.getColumnModel().getColumn(2);
		color.setMaxWidth(40);
		color.setMinWidth(40);
		color.setCellEditor(editor);
		color.setCellRenderer(renderer);

		TableColumn delete = table.getColumnModel().getColumn(3);
		delete.setMaxWidth(20);
		delete.setMinWidth(20);
		delete.setCellEditor(new DeleteCellEditor());

		// simple button
		goButton = new JButton("Annotate Graph");
		goButton.setActionCommand("go");
		goButton.setEnabled(false);
		goButton.addActionListener(this);

		// get all available attribute names
		for (AttributeName attName : graph.getMetaData().getAttributeNames()) {
			// check its numerically comparable
			if (Number.class.isAssignableFrom(attName.getDataType())) {
				Set<ONDEXConcept> concepts = graph
						.getConceptsOfAttributeName(attName);
				if (concepts != null) {
					// check concepts exists on this attribute name
					if (concepts.size() > 0
							&& !AppearanceSynchronizer.attr.contains(attName
									.getId())) {
						anlm.addAttributeName(attName);
					}
				}
			}
		}

		// populate list
		if (anlm.getSize() == 0) {
			add(new JLabel(
					"There are no attributes with numerical values in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.addListSelectionListener(this);

			add(new JLabel("Select attribute(s) to annotate concepts with"));

			JPanel split = new JPanel();
			BoxLayout layout = new BoxLayout(split, BoxLayout.LINE_AXIS);
			split.setLayout(layout);
			add(split);

			split.add(new JScrollPane(list));

			split.add(addButton);

			split.add(new JScrollPane(table));

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5,
				5, 5, 5);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("go")) {

			if (series.size() > 0) {
				Integer max = (Integer) maxField.getValue();
				if (max < 0) {
					max = 0;
					maxField.setValue(max);
				}

				updateGraph(max, use3D.isSelected(), barcharts.isSelected(),
						globalRange.isSelected());

				used = true;
			}
		} else if (cmd.equals("add")) {

			// get current keys to increment by 1
			Integer seriesKey = 0;
			if (series.size() > 0) {
				Integer[] keys = series.keySet().toArray(new Integer[0]);
				Arrays.sort(keys);
				seriesKey = keys[keys.length - 1] + 1;
			}

			// add current selection as new series
			series.put(seriesKey, targets);

			// get new random colour
			Color colour = new Color(numGen.nextInt(256), numGen.nextInt(256),
					numGen.nextInt(256));

			// add to table
			model.addRow(new Object[] { seriesKey, targets.toString(), colour,
					"" });

			goButton.setEnabled(true);
		}
	}

	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ValueGraph");
	}

	@Override
	public boolean hasBeenUsed() {
		return used;
	}

	/**
	 * Replaces nodes of the graph with little value graphs
	 * 
	 * @param targMax
	 *            the largest node size
	 * @param use3D
	 *            use 3D chart effect
	 * @param barcharts
	 *            draw barcharts instead of nodes
	 * @param globalRange
	 *            use all concepts for value range
	 */
	private void updateGraph(int targMax, boolean use3D, boolean barcharts,
			boolean globalRange) {

		// reset icon transformer, might have been set by annotator
		viewer.getVisualizationViewer().getRenderContext()
				.setVertexIconTransformer(null);

		// dimensions: series, category, data
		Map<Integer, Map<Integer, Map<ONDEXConcept, Double>>> seriesCatValues = new HashMap<Integer, Map<Integer, Map<ONDEXConcept, Double>>>();

		// process each available series
		for (Integer key : series.keySet()) {
			targets = series.get(key);
			Map<Integer, Map<ONDEXConcept, Double>> catValues = new HashMap<Integer, Map<ONDEXConcept, Double>>();
			for (int cat = 0; cat < targets.size(); cat++) {
				// get attribute name values
				Map<ONDEXConcept, Double> values = new HashMap<ONDEXConcept, Double>();
				for (ONDEXConcept concept : graph
						.getConceptsOfAttributeName(targets.get(cat))) {

					if (graph.isVisible(concept)) {
						Double value = ((Number) concept.getAttribute(
								targets.get(cat)).getValue()).doubleValue();
						values.put(concept, value);
					}
				}
				catValues.put(cat, values);
			}
			seriesCatValues.put(key, catValues);
		}

		// get new colour mapping from table
		colours.clear();
		for (int row = 0; row < model.getRowCount(); row++) {
			Integer key = (Integer) model.getValueAt(row, 0);
			Color color = (Color) model.getValueAt(row, 2);
			colours.put(key, color);
		}

		Transformer<ONDEXConcept, Icon> vertexIconTransformer;

		if (!barcharts) {

			// get icon transformer for line charts
			vertexIconTransformer = new LineChartNodeIconTransformer(
					seriesCatValues, colours, targMax, use3D, globalRange);

		} else {

			// get icon transformer for bar charts
			vertexIconTransformer = new BarChartNodeIconTransformer(
					seriesCatValues, colours, targMax, use3D, globalRange);
		}

		// pick support uses shapes, so requires same size of shape too
		Transformer<ONDEXConcept, Shape> vertexShapeTransformer = new Transformer<ONDEXConcept, Shape>() {

			@Override
			public Shape transform(ONDEXConcept arg0) {
				if (viewer.getVisualizationViewer().getRenderContext()
						.getVertexIconTransformer() != null) {
					Icon icon = viewer.getVisualizationViewer()
							.getRenderContext().getVertexIconTransformer()
							.transform(arg0);
					if (icon != null) {
						int w = icon.getIconWidth();
						int h = icon.getIconHeight();
						return new Rectangle(-w / 2, -h / 2, w, h);
					}
				}
				return oldShapes.transform(arg0);
			}
		};

		// update visualisation
		viewer.getVisualizationViewer().getRenderContext()
				.setVertexIconTransformer(vertexIconTransformer);
		viewer.getVisualizationViewer().getRenderContext()
				.setVertexShapeTransformer(vertexShapeTransformer);
		viewer.getVisualizationViewer().getModel().fireStateChanged();
		viewer.getVisualizationViewer().repaint();
	}

	/**
	 * Checks for selections in ConceptClass list.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int[] indices = list.getSelectedIndices();
		if (indices.length > 0) {
			addButton.setEnabled(true);

			List<AttributeName> attNames = new ArrayList<AttributeName>(
					indices.length);
			for (int i : indices) {
				attNames.add(((AttributeNameListModel) list.getModel())
						.getAttributeNameAt(i));
			}

			targets = attNames;
		} else {
			addButton.setEnabled(false);
		}
	}

}
