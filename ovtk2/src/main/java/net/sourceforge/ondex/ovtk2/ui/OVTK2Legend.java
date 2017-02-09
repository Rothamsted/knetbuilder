package net.sourceforge.ondex.ovtk2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes.NodeShapeSelection;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaConcept;
import net.sourceforge.ondex.ovtk2.metagraph.ONDEXMetaRelation;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptClassColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptClassShape;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogDataSourceColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogEvidenceTypeColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogRelationTypeColor;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.BooleanTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableEditor;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.MetaDataTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeComboBoxRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeTableCellRenderer;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

/**
 * JInternalFrame, which provides an overview of the MetaData currently
 * contained in the AbstractONDEXGraph of a given OVTK2Viewer.
 * 
 * @author taubertj
 * @author Matthew Pocock
 * @version 23.05.2008
 */
public class OVTK2Legend extends RegisteredJInternalFrame implements ActionListener, TableModelListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 2828607201776001672L;

	/**
	 * prefix from config file for concept class colours
	 */
	private static final String CONCEPTCLASS_COLOR_VIS = "ConceptClass.Color.";

	/**
	 * prefix from config file for concept class shapes
	 */
	private static final String CONCEPTCLASS_SHAPE_VIS = "ConceptClass.Shape.";

	/**
	 * prefix from config file for data sources colours
	 */
	private static final String DATASOURCE_COLOR_VIS = "DataSource.Color.";

	/**
	 * prefix from config file for relation types colours
	 */
	private static final String RELATIONTYPE_COLOR_VIS = "RelationType.Color.";

	/**
	 * prefix from config file for evidence types colours
	 */
	private static final String EVIDENCETYPE_COLOR_VIS = "EvidenceType.Color.";

	/**
	 * these static variables are prefixes for action commands
	 */
	public static final String REFRESH = "refresh";

	/**
	 * singletons for dialogs
	 */
	private DialogConceptClassColor dialogConceptClassColor = null;

	private DialogConceptClassShape dialogConceptClassShape = null;

	private DialogDataSourceColor dialogDataSourceColor = null;

	private DialogRelationTypeColor dialogRelationTypeColor = null;

	private DialogEvidenceTypeColor dialogEvidenceTypeColor = null;

	/**
	 * current OVTK2Viewer
	 */
	private OVTK2Viewer viewer;

	/**
	 * current ONDEXJUNGGraph
	 */
	private ONDEXJUNGGraph graph;

	/**
	 * track last tabbed pane user selected
	 */
	private int lastPane = 0;

	/**
	 * have same height between all tables
	 */
	private int tableHeight;

	/**
	 * all concepts of current graph
	 */
	private int totalConcepts = 0;

	/**
	 * visible concepts of current graph
	 */
	private int visibleConcepts = 0;

	/**
	 * all relations of current graph
	 */
	private int totalRelations = 0;

	/**
	 * visible relations of current graph
	 */
	private int visibleRelations = 0;

	/**
	 * total number of concepts per concept class
	 */
	private Map<ConceptClass, Integer> conceptClassesTotal = new HashMap<ConceptClass, Integer>();

	/**
	 * number of visible concepts per concept class
	 */
	private Map<ConceptClass, Integer> conceptClassesVisible = LazyMap.decorate(new HashMap<ConceptClass, Integer>(), new Factory<Integer>() {
		@Override
		public Integer create() {
			return 0;
		}
	});

	/**
	 * total number of concepts per data source
	 */
	private Map<DataSource, Integer> dataSourcesTotal = new HashMap<DataSource, Integer>();

	/**
	 * number of visible concepts per data source
	 */
	private Map<DataSource, Integer> dataSourcesVisible = LazyMap.decorate(new HashMap<DataSource, Integer>(), new Factory<Integer>() {
		@Override
		public Integer create() {
			return 0;
		}
	});

	/**
	 * total number of relations per relation type
	 */
	private Map<RelationType, Integer> relationTypesTotal = new HashMap<RelationType, Integer>();

	/**
	 * number of visible relations per relation type
	 */
	private Map<RelationType, Integer> relationTypesVisible = LazyMap.decorate(new HashMap<RelationType, Integer>(), new Factory<Integer>() {
		@Override
		public Integer create() {
			return 0;
		}
	});

	/**
	 * total number of concepts per evidence type
	 */
	private Map<EvidenceType, Integer> evidenceTypeTotalConcepts = new HashMap<EvidenceType, Integer>();

	/**
	 * total number of relations per evidence type
	 */
	private Map<EvidenceType, Integer> evidenceTypeTotalRelations = new HashMap<EvidenceType, Integer>();

	/**
	 * number of visible concepts per evidence type
	 */
	private Map<EvidenceType, Integer> evidenceTypeVisibleConcepts = LazyMap.decorate(new HashMap<EvidenceType, Integer>(), new Factory<Integer>() {
		@Override
		public Integer create() {
			return 0;
		}
	});

	/**
	 * number of visible relations per evidence type
	 */
	private Map<EvidenceType, Integer> evidenceTypeVisibleRelations = LazyMap.decorate(new HashMap<EvidenceType, Integer>(), new Factory<Integer>() {
		@Override
		public Integer create() {
			return 0;
		}
	});

	/**
	 * Set first OVTK2Viewer, initialises layout.
	 * 
	 * @param viewer
	 *            OVTK2Viewer to use
	 */
	public OVTK2Legend(OVTK2Viewer viewer) {
		super(Config.language.getProperty("Legend.Title"), "MetaGraph", Config.language.getProperty("Legend.Title"), true, true, true, true);

		// dispose legend on close
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// set frame icon
		initIcon();

		// set layout
		this.getContentPane().setLayout(new BorderLayout());
		this.setViewer(viewer);
		this.setSize(450, 300);
	}

	/**
	 * Updates the GUI for a given OVTK2Viewer.
	 * 
	 * @param viewer
	 *            new OVTK2Viewer
	 */
	public void setViewer(OVTK2Viewer viewer) {
		this.viewer = viewer;
		this.graph = viewer.getONDEXJUNGGraph();
		this.getContentPane().removeAll();

		// reset counters for concepts
		totalConcepts = 0;
		visibleConcepts = 0;

		// case for concept classes
		this.conceptClassesTotal.clear();
		this.conceptClassesVisible.clear();
		for (ConceptClass conceptClass : graph.getMetaData().getConceptClasses()) {

			// get all concepts of concept class
			Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(conceptClass);
			int conceptSize = concepts.size();
			if (conceptSize > 0) {
				totalConcepts += conceptSize;
				conceptClassesTotal.put(conceptClass, conceptSize);
				for (ONDEXConcept concept : concepts) {
					// count only visible now
					if (graph.isVisible(concept)) {
						visibleConcepts++;
						conceptClassesVisible.put(conceptClass, conceptClassesVisible.get(conceptClass) + 1);
					}
				}
			}
		}

		// case for data sources
		this.dataSourcesTotal.clear();
		this.dataSourcesVisible.clear();
		for (DataSource dataSource : graph.getMetaData().getDataSources()) {

			// get all concepts of data source
			Set<ONDEXConcept> concepts = graph.getConceptsOfDataSource(dataSource);
			int conceptSize = concepts.size();
			if (conceptSize > 0) {
				dataSourcesTotal.put(dataSource, conceptSize);
				for (ONDEXConcept concept : concepts) {
					// count only visible now
					if (graph.isVisible(concept)) {
						dataSourcesVisible.put(dataSource, dataSourcesVisible.get(dataSource) + 1);
					}
				}
			}
		}

		// reset counters for relations
		totalRelations = 0;
		visibleRelations = 0;

		// case for relation types
		this.relationTypesTotal.clear();
		this.relationTypesVisible.clear();
		for (RelationType relationType : graph.getMetaData().getRelationTypes()) {

			// get all relations of type
			Set<ONDEXRelation> relations = graph.getRelationsOfRelationType(relationType);
			int relationSize = relations.size();
			if (relationSize > 0) {
				totalRelations += relationSize;
				relationTypesTotal.put(relationType, relationSize);
				for (ONDEXRelation relation : relations) {
					// count only visible now
					if (graph.isVisible(relation)) {
						visibleRelations++;
						relationTypesVisible.put(relationType, relationTypesVisible.get(relationType) + 1);
					}
				}
			}
		}

		// case for evidence types
		this.evidenceTypeTotalConcepts.clear();
		this.evidenceTypeVisibleConcepts.clear();
		this.evidenceTypeTotalRelations.clear();
		this.evidenceTypeVisibleRelations.clear();
		for (EvidenceType evidenceType : graph.getMetaData().getEvidenceTypes()) {

			// get evidence on concepts
			Set<ONDEXConcept> concepts = graph.getConceptsOfEvidenceType(evidenceType);
			int conceptSize = concepts.size();
			if (conceptSize > 0) {
				evidenceTypeTotalConcepts.put(evidenceType, conceptSize);
				for (ONDEXConcept concept : concepts) {
					if (graph.isVisible(concept))
						evidenceTypeVisibleConcepts.put(evidenceType, evidenceTypeVisibleConcepts.get(evidenceType) + 1);
				}
			}

			// get evidence on relations
			Set<ONDEXRelation> relations = graph.getRelationsOfEvidenceType(evidenceType);
			int relationSize = relations.size();
			if (relationSize > 0) {
				evidenceTypeTotalRelations.put(evidenceType, relationSize);
				for (ONDEXRelation relation : relations) {
					if (graph.isVisible(relation))
						evidenceTypeVisibleRelations.put(evidenceType, evidenceTypeVisibleRelations.get(evidenceType) + 1);
				}
			}
		}

		final JTabbedPane tabbedPane = new JTabbedPane();

		// tab for concept classes
		JComponent conceptClasses = new JScrollPane();
		if (totalConcepts > 0)
			conceptClasses = initConceptClasses();
		tabbedPane.add(Config.language.getProperty("Legend.ConceptClasses"), conceptClasses);

		// tab for data sources
		JComponent dataSources = new JScrollPane();
		if (totalConcepts > 0)
			dataSources = initDataSources();
		tabbedPane.add(Config.language.getProperty("Legend.DataSources"), dataSources);

		// tab for relation types
		JComponent relationTypes = new JScrollPane();
		if (totalRelations > 0)
			relationTypes = initRelationTypes();
		tabbedPane.add(Config.language.getProperty("Legend.RelationTypes"), relationTypes);

		// tab for evidence types
		JComponent evidenceTypes = new JScrollPane();
		if (totalConcepts > 0)
			evidenceTypes = initEvidenceTypes();
		tabbedPane.add(Config.language.getProperty("Legend.EvidenceTypes"), evidenceTypes);

		tabbedPane.setSelectedIndex(lastPane);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				lastPane = tabbedPane.getSelectedIndex();
			}
		});
		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		// refreshes current view on meta data
		JButton refresh = new JButton(Config.language.getProperty("Legend.Refresh"));
		refresh.addActionListener(this);
		refresh.setActionCommand(REFRESH);
		this.getContentPane().add(refresh, BorderLayout.SOUTH);

		this.updateUI();
	}

	/**
	 * Returns the current OVTK2Viewer.
	 * 
	 * @return current OVTK2Viewer
	 */
	public OVTK2Viewer getViewer() {
		return viewer;
	}

	private JTable tableConceptClasses;

	/**
	 * Construct panel for concept classes.
	 * 
	 * @return JComponent for concept classes
	 */
	private JComponent initConceptClasses() {

		// get mapping to colour from visual.xml
		Map<String, Color> colors = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(CONCEPTCLASS_COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(CONCEPTCLASS_COLOR_VIS.length(), name.length());
				colors.put(name, Config.convertToColor(color));
			}
		}

		// get shapes from visual.xml
		Map<String, Shape> shapes = new Hashtable<String, Shape>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(CONCEPTCLASS_SHAPE_VIS)) {
				int id = Integer.parseInt(Config.visual.getProperty(name));
				name = name.substring(CONCEPTCLASS_SHAPE_VIS.length(), name.length());
				shapes.put(name, ONDEXNodeShapes.getShape(id));
			}
		}

		// add all available shapes
		JComboBox comboBox = new JComboBox();
		comboBox.setRenderer(new ShapeComboBoxRenderer());
		for (Shape s : ONDEXNodeShapes.getAvailableShapes()) {
			comboBox.addItem(s);
		}

		// construct column names
		String[] columnNames = new String[] { Config.language.getProperty("Legend.Colour"), Config.language.getProperty("Legend.Shape"), Config.language.getProperty("Legend.Size"), Config.language.getProperty("Legend.Name"), Config.language.getProperty("Legend.Visible"), Config.language.getProperty("Legend.VisibleAll"), Config.language.getProperty("Legend.Visibility") };

		// specific table model for classes
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0)
					return Color.class;
				if (columnIndex == 1)
					return Shape.class;
				if (columnIndex == 2)
					return Integer.class;
				if (columnIndex == 3)
					return ConceptClass.class;
				if (columnIndex == 4)
					return Integer.class;
				if (columnIndex == 5)
					return Integer.class;
				if (columnIndex == 6)
					return Boolean.class;
				return super.getColumnClass(columnIndex);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 3)
					return false;
				if (column == 4)
					return false;
				if (column == 5)
					return false;
				return super.isCellEditable(row, column);
			}
		};

		// new table for all concept classes
		tableConceptClasses = new JTable(model);

		// set all renderer to table
		tableConceptClasses.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
		tableConceptClasses.setDefaultEditor(Color.class, new ColorTableEditor());
		tableConceptClasses.setDefaultRenderer(Shape.class, new ShapeTableCellRenderer(true));
		tableConceptClasses.setDefaultEditor(Shape.class, new DefaultCellEditor(comboBox));
		tableConceptClasses.setDefaultRenderer(ConceptClass.class, new MetaDataTableCellRenderer(true));
		BooleanTableCellRenderer renderer = new BooleanTableCellRenderer();
		renderer.setToolTipText("Change visibility");
		tableConceptClasses.setDefaultRenderer(Boolean.class, renderer);

		// set proper height of table for icons
		Shape shape = shapes.values().iterator().next();
		Rectangle2D bounds = shape.getBounds2D();
		tableHeight = (int) bounds.getHeight() + 5;
		tableConceptClasses.setRowHeight(tableHeight);

		// count total number of concepts
		final StringBuilder textualRepresentation = new StringBuilder();

		// iterate over contained concept classes
		for (ConceptClass cc : conceptClassesTotal.keySet()) {

			// count visible concepts
			int visibleNodes = conceptClassesVisible.get(cc);

			// construct clip board representation
			if (textualRepresentation.length() > 0) {
				textualRepresentation.append('\n');
			}
			textualRepresentation.append(cc.getId()).append("\t").append(visibleNodes).append("\t").append(conceptClassesTotal.get(cc));

			// set colour square
			Color color = null;
			if (colors.containsKey(cc.getId()))
				color = colors.get(cc.getId());
			else
				color = Config.getDefaultColor();

			// set shape outline
			shape = null;
			if (shapes.containsKey(cc.getId()))
				shape = shapes.get(cc.getId());
			else
				shape = ONDEXNodeShapes.getShape(Config.defaultShape);

			// set size
			Integer size = Config.getSizeForConceptClass(cc);

			// add new column
			Object[] rowData = new Object[] { color, shape, size, cc, visibleNodes, conceptClassesTotal.get(cc), visibleNodes > 0 };
			model.addRow(rowData);
		}

		// construct label and clip board representation for total
		JLabel total = new JLabel(Config.language.getProperty("Legend.TotalConcepts") + " " + visibleConcepts + " (" + totalConcepts + ")");
		textualRepresentation.append("\n").append(Config.language.getProperty("Legend.TotalConcepts")).append("\t").append(visibleConcepts).append("\t").append(totalConcepts);

		// button to copy to clip board
		JButton copyToClipBoardButton = new JButton("Copy to clipboard");
		copyToClipBoardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setSystemClipboard(textualRepresentation.toString());
			}
		});

		// bottom panel of totals and copy clip board
		JPanel panelBottom = new JPanel(new BorderLayout());
		panelBottom.add(BorderLayout.WEST, total);
		panelBottom.add(BorderLayout.EAST, copyToClipBoardButton);

		// panel containing boxes
		JPanel panel = new JPanel(new BorderLayout());

		// add table and bottom panel to panel
		panel.add(BorderLayout.CENTER, new JScrollPane(tableConceptClasses));
		panel.add(BorderLayout.SOUTH, panelBottom);

		// listen to changes made by the user
		tableConceptClasses.getModel().addTableModelListener(this);

		// Change auto resizing and pack table
		tableConceptClasses.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		packColumns(tableConceptClasses);

		// fix first three and last column
		TableColumn columnColor = tableConceptClasses.getColumnModel().getColumn(0);
		columnColor.setMaxWidth(columnColor.getPreferredWidth());
		columnColor.setMinWidth(columnColor.getPreferredWidth());
		TableColumn columnShape = tableConceptClasses.getColumnModel().getColumn(1);
		// extra margin to better fit drop-down box
		columnShape.setMaxWidth(columnShape.getPreferredWidth() + 5);
		columnShape.setMinWidth(columnShape.getPreferredWidth() + 5);
		TableColumn columnSize = tableConceptClasses.getColumnModel().getColumn(2);
		columnSize.setMaxWidth(columnSize.getPreferredWidth());
		columnSize.setMinWidth(columnSize.getPreferredWidth());
		TableColumn columnVisible = tableConceptClasses.getColumnModel().getColumn(6);
		columnVisible.setMaxWidth(columnVisible.getPreferredWidth());

		// enable sorting of table
		tableConceptClasses.setAutoCreateRowSorter(true);

		return panel;
	}

	private JTable tableDataSources;

	/**
	 * Construct panel for data sources.
	 * 
	 * @return JComponent for data sources
	 */
	private JComponent initDataSources() {

		// get mapping to colour from visual.xml
		Hashtable<String, Color> colors = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(DATASOURCE_COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(DATASOURCE_COLOR_VIS.length(), name.length());
				colors.put(name, Config.convertToColor(color));
			}
		}

		// construct column names
		String[] columnNames = new String[] { Config.language.getProperty("Legend.Colour"), Config.language.getProperty("Legend.Name"), Config.language.getProperty("Legend.Visible"), Config.language.getProperty("Legend.VisibleAll"), Config.language.getProperty("Legend.Visibility") };

		// specific table model for classes
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0)
					return Color.class;
				if (columnIndex == 1)
					return DataSource.class;
				if (columnIndex == 2)
					return Integer.class;
				if (columnIndex == 3)
					return Integer.class;
				if (columnIndex == 4)
					return Boolean.class;
				return super.getColumnClass(columnIndex);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 1)
					return false;
				if (column == 2)
					return false;
				if (column == 3)
					return false;
				return super.isCellEditable(row, column);
			}
		};

		// new table for all data sources
		tableDataSources = new JTable(model);

		// set all renderer to table
		tableDataSources.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
		tableDataSources.setDefaultEditor(Color.class, new ColorTableEditor());
		tableDataSources.setDefaultRenderer(DataSource.class, new MetaDataTableCellRenderer(true));
		BooleanTableCellRenderer renderer = new BooleanTableCellRenderer();
		renderer.setToolTipText("Change visibility");
		tableDataSources.setDefaultRenderer(Boolean.class, renderer);

		// count total number of concepts
		final StringBuilder textualRepresentation = new StringBuilder();

		// iterate over contained data sources
		for (DataSource ds : dataSourcesTotal.keySet()) {

			// count visible concepts
			int visibleNodes = dataSourcesVisible.get(ds);

			// construct clip board representation
			if (textualRepresentation.length() > 0) {
				textualRepresentation.append('\n');
			}
			textualRepresentation.append(ds.getId()).append("\t").append(visibleNodes).append("\t").append(dataSourcesTotal.get(ds));

			// set colour square
			Color color = null;
			if (colors.containsKey(ds.getId()))
				color = colors.get(ds.getId());
			else
				color = Config.getDefaultColor();

			// add new column
			Object[] rowData = new Object[] { color, ds, visibleNodes, dataSourcesTotal.get(ds), visibleNodes > 0 };
			model.addRow(rowData);
		}

		// construct label and clip board representation for total
		JLabel total = new JLabel(Config.language.getProperty("Legend.TotalConcepts") + " " + visibleConcepts + " (" + totalConcepts + ")");
		textualRepresentation.append("\n").append(Config.language.getProperty("Legend.TotalConcepts")).append("\t").append(visibleConcepts).append("\t").append(totalConcepts);

		// button to copy to clip board
		JButton copyToClipBoardButton = new JButton("Copy to clipboard");
		copyToClipBoardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setSystemClipboard(textualRepresentation.toString());
			}
		});

		// bottom panel of totals and copy clip board
		JPanel panelBottom = new JPanel(new BorderLayout());
		panelBottom.add(BorderLayout.WEST, total);
		panelBottom.add(BorderLayout.EAST, copyToClipBoardButton);

		// panel containing boxes
		JPanel panel = new JPanel(new BorderLayout());

		// add table and bottom panel to panel
		panel.add(BorderLayout.CENTER, new JScrollPane(tableDataSources));
		panel.add(BorderLayout.SOUTH, panelBottom);

		// listen to changes made by the user
		tableDataSources.getModel().addTableModelListener(this);

		// Change auto resizing and pack table
		tableDataSources.setRowHeight(tableHeight);
		tableDataSources.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		packColumns(tableDataSources);

		// fix first two and last column
		TableColumn columnColor = tableDataSources.getColumnModel().getColumn(0);
		columnColor.setMaxWidth(columnColor.getPreferredWidth());
		columnColor.setMinWidth(columnColor.getPreferredWidth());
		TableColumn columnVisible = tableDataSources.getColumnModel().getColumn(4);
		columnVisible.setMaxWidth(columnVisible.getPreferredWidth());

		// enable sorting of table
		tableDataSources.setAutoCreateRowSorter(true);

		return panel;
	}

	private JTable tableRelationType;

	/**
	 * Construct panel for relation types
	 * 
	 * @return JComponet for relation types
	 */
	private JComponent initRelationTypes() {

		// get mapping to colour from config
		Hashtable<String, Color> colors = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(RELATIONTYPE_COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(RELATIONTYPE_COLOR_VIS.length(), name.length());
				colors.put(name, Config.convertToColor(color));
			}
		}

		// construct column names
		String[] columnNames = new String[] { Config.language.getProperty("Legend.Colour"), Config.language.getProperty("Legend.Size"), Config.language.getProperty("Legend.Name"), Config.language.getProperty("Legend.Visible"), Config.language.getProperty("Legend.VisibleAll"), Config.language.getProperty("Legend.Visibility") };

		// specific table model for classes
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0)
					return Color.class;
				if (columnIndex == 1)
					return Integer.class;
				if (columnIndex == 2)
					return RelationType.class;
				if (columnIndex == 3)
					return Integer.class;
				if (columnIndex == 4)
					return Integer.class;
				if (columnIndex == 5)
					return Boolean.class;
				return super.getColumnClass(columnIndex);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 2)
					return false;
				if (column == 3)
					return false;
				if (column == 4)
					return false;
				return super.isCellEditable(row, column);
			}
		};

		// new table for all relation types
		tableRelationType = new JTable(model);

		// set all renderer to table
		tableRelationType.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
		tableRelationType.setDefaultEditor(Color.class, new ColorTableEditor());
		tableRelationType.setDefaultRenderer(RelationType.class, new MetaDataTableCellRenderer(true));
		BooleanTableCellRenderer renderer = new BooleanTableCellRenderer();
		renderer.setToolTipText("Change visibility");
		tableRelationType.setDefaultRenderer(Boolean.class, renderer);

		// count total number of relations
		final StringBuilder textualRepresentation = new StringBuilder();

		// iterate over contained relation types
		for (RelationType rt : relationTypesTotal.keySet()) {

			// count visible relations
			int visibleEdges = relationTypesVisible.get(rt);

			// construct clip board representation
			if (textualRepresentation.length() > 0) {
				textualRepresentation.append('\n');
			}
			textualRepresentation.append(rt.getId()).append("\t").append(visibleEdges).append("\t").append(relationTypesTotal.get(rt));

			// set colour square
			Color color = null;
			if (colors.containsKey(rt.getId()))
				color = colors.get(rt.getId());
			else
				color = Config.getDefaultColor();

			// set edge size
			Integer size = Config.getSizeForRelationType(rt);

			// add new column
			Object[] rowData = new Object[] { color, size, rt, visibleEdges, relationTypesTotal.get(rt), visibleEdges > 0 };
			model.addRow(rowData);
		}

		// construct label and clip board representation for total
		JLabel total = new JLabel(Config.language.getProperty("Legend.TotalRelations") + " " + visibleRelations + " (" + totalRelations + ")");
		textualRepresentation.append("\n").append(Config.language.getProperty("Legend.TotalRelations")).append("\t").append(visibleRelations).append("\t").append(totalRelations);

		// button to copy to clip board
		JButton copyToClipBoardButton = new JButton("Copy to clipboard");
		copyToClipBoardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setSystemClipboard(textualRepresentation.toString());
			}
		});

		// bottom panel of totals and copy clip board
		JPanel panelBottom = new JPanel(new BorderLayout());
		panelBottom.add(BorderLayout.WEST, total);
		panelBottom.add(BorderLayout.EAST, copyToClipBoardButton);

		// panel containing boxes
		JPanel panel = new JPanel(new BorderLayout());

		// add table and bottom panel to panel
		panel.add(BorderLayout.CENTER, new JScrollPane(tableRelationType));
		panel.add(BorderLayout.SOUTH, panelBottom);

		// listen to changes made by the user
		tableRelationType.getModel().addTableModelListener(this);

		// Change auto resizing and pack table
		tableRelationType.setRowHeight(tableHeight);
		tableRelationType.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		packColumns(tableRelationType);

		// fix first two and last column
		TableColumn columnColor = tableRelationType.getColumnModel().getColumn(0);
		columnColor.setMaxWidth(columnColor.getPreferredWidth());
		columnColor.setMinWidth(columnColor.getPreferredWidth());
		TableColumn columnSize = tableRelationType.getColumnModel().getColumn(1);
		columnSize.setMaxWidth(columnSize.getPreferredWidth());
		columnSize.setMinWidth(columnSize.getPreferredWidth());
		TableColumn columnVisible = tableRelationType.getColumnModel().getColumn(5);
		columnVisible.setMaxWidth(columnVisible.getPreferredWidth());

		// enable sorting of table
		tableRelationType.setAutoCreateRowSorter(true);

		return panel;
	}

	private JTable tableRelationType1;

	/**
	 * Construct panel for evidence types
	 * 
	 * @return JComponent for evidence types
	 */
	private JComponent initEvidenceTypes() {

		// get mapping to colour from config
		Hashtable<String, Color> colors = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(EVIDENCETYPE_COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(EVIDENCETYPE_COLOR_VIS.length(), name.length());
				colors.put(name, Config.convertToColor(color));
			}
		}

		// construct column names
		String[] columnNames = new String[] { Config.language.getProperty("Legend.Colour"), Config.language.getProperty("Legend.Name"), Config.language.getProperty("Legend.EvidenceTypes.VisibleConcepts"), Config.language.getProperty("Legend.EvidenceTypes.VisibleConceptsAll"), Config.language.getProperty("Legend.EvidenceTypes.VisibleRelations"), Config.language.getProperty("Legend.EvidenceTypes.VisibleRelationsAll"), Config.language.getProperty("Legend.EvidenceTypes.VisibilityConcepts"), Config.language.getProperty("Legend.EvidenceTypes.VisibilityRelations") };

		// specific table model for classes
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0)
					return Color.class;
				if (columnIndex == 1)
					return EvidenceType.class;
				if (columnIndex == 2)
					return Integer.class;
				if (columnIndex == 3)
					return Integer.class;
				if (columnIndex == 4)
					return Integer.class;
				if (columnIndex == 5)
					return Integer.class;
				if (columnIndex == 6)
					return Boolean.class;
				if (columnIndex == 7)
					return Boolean.class;
				return super.getColumnClass(columnIndex);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 1)
					return false;
				if (column == 2)
					return false;
				if (column == 3)
					return false;
				if (column == 4)
					return false;
				if (column == 5)
					return false;
				return super.isCellEditable(row, column);
			}
		};

		// new table for all relation types
		tableRelationType1 = new JTable(model);

		// set all renderer to table
		tableRelationType1.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
		tableRelationType1.setDefaultEditor(Color.class, new ColorTableEditor());
		tableRelationType1.setDefaultRenderer(EvidenceType.class, new MetaDataTableCellRenderer(true));
		BooleanTableCellRenderer renderer = new BooleanTableCellRenderer();
		renderer.setToolTipText("Change visibility");
		tableRelationType1.setDefaultRenderer(Boolean.class, renderer);

		// count total number of concepts
		int visibleEntities = visibleConcepts + visibleRelations;
		;
		int totalEntities = totalConcepts + totalRelations;
		final StringBuilder textualRepresentation = new StringBuilder();

		// iterate over contained ETs
		Set<EvidenceType> mergedKeys = new HashSet<EvidenceType>(evidenceTypeTotalConcepts.keySet());
		mergedKeys.addAll(evidenceTypeTotalRelations.keySet());
		for (EvidenceType et : mergedKeys) {

			// count visible concepts
			int visibleNodes = evidenceTypeVisibleConcepts.get(et);

			// construct clip board representation
			if (textualRepresentation.length() > 0) {
				textualRepresentation.append('\n');
			}
			textualRepresentation.append(et.getId()).append("\t").append(visibleNodes).append("\t").append(evidenceTypeTotalConcepts.get(et));

			// count visible relations
			int visibleEdges = evidenceTypeVisibleRelations.get(et);

			// construct clip board representation
			textualRepresentation.append("\t").append(visibleEdges).append("\t").append(evidenceTypeTotalRelations.get(et));

			// set colour square
			Color color = null;
			if (colors.containsKey(et.getId()))
				color = colors.get(et.getId());
			else
				color = Config.getDefaultColor();

			// add new column
			Object[] rowData = new Object[] { color, et, visibleNodes, evidenceTypeTotalConcepts.get(et) != null ? evidenceTypeTotalConcepts.get(et) : 0, visibleEdges, evidenceTypeTotalRelations.get(et) != null ? evidenceTypeTotalRelations.get(et) : 0, visibleNodes > 0, visibleEdges > 0 };
			model.addRow(rowData);
		}

		// construct label and clip board representation for total
		JLabel total = new JLabel(Config.language.getProperty("Legend.TotalEntities") + " " + visibleEntities + " (" + totalEntities + ")");
		textualRepresentation.append("\n").append(Config.language.getProperty("Legend.TotalEntities")).append("\t").append(visibleEntities).append("\t").append(totalEntities);

		// button to copy to clip board
		JButton copyToClipBoardButton = new JButton("Copy to clipboard");
		copyToClipBoardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setSystemClipboard(textualRepresentation.toString());
			}
		});

		// bottom panel of totals and copy clip board
		JPanel panelBottom = new JPanel(new BorderLayout());
		panelBottom.add(BorderLayout.WEST, total);
		panelBottom.add(BorderLayout.EAST, copyToClipBoardButton);

		// panel containing boxes
		JPanel panel = new JPanel(new BorderLayout());

		// add table and bottom panel to panel
		panel.add(BorderLayout.CENTER, new JScrollPane(tableRelationType1));
		panel.add(BorderLayout.SOUTH, panelBottom);

		// listen to changes made by the user
		tableRelationType1.getModel().addTableModelListener(this);

		// Change auto resizing and pack table
		tableRelationType1.setRowHeight(tableHeight);
		tableRelationType1.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		packColumns(tableRelationType1);

		// fix first two and last column
		TableColumn columnColor = tableRelationType1.getColumnModel().getColumn(0);
		columnColor.setMaxWidth(columnColor.getPreferredWidth());
		columnColor.setMinWidth(columnColor.getPreferredWidth());
		TableColumn columnVisibleC = tableRelationType1.getColumnModel().getColumn(6);
		columnVisibleC.setMaxWidth(columnVisibleC.getPreferredWidth());
		TableColumn columnVisibleR = tableRelationType1.getColumnModel().getColumn(7);
		columnVisibleR.setMaxWidth(columnVisibleR.getPreferredWidth());

		// enable sorting of table
		tableRelationType1.setAutoCreateRowSorter(true);

		return panel;
	}

	/**
	 * Transfers text to the clipboard in the simplest way
	 * 
	 * @param text
	 *            the text to send to the clipboard
	 */
	private void setSystemClipboard(String text) {
		Clipboard clipboard = getToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(text);
		clipboard.setContents(stringSelection, new ClipboardOwner() {

			@Override
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				// do nothing
			}

		});
	}

	/**
	 * Sets frame icon from file.
	 * 
	 */
	private void initIcon() {
		File imgLocation = new File("config/toolbarButtonGraphics/development/Application16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		this.setFrameIcon(new ImageIcon(imageURL));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();

		// refresh complete legend
		if (REFRESH.equals(cmd)) {
			tableConceptClasses.getCellEditor().stopCellEditing();
			tableDataSources.getCellEditor().stopCellEditing();
			tableRelationType.getCellEditor().stopCellEditing();
			tableRelationType1.getCellEditor().stopCellEditing();
			this.setViewer(getViewer());
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();
		TableModel model = (TableModel) e.getSource();
		String columnName = model.getColumnName(column);
		Object data = model.getValueAt(row, column);

		// for concept classes
		if (lastPane == 0) {
			ConceptClass cc = (ConceptClass) model.getValueAt(row, 3);
			String id = cc.getId();

			// update concept class colour
			if (columnName.equals(Config.language.getProperty("Legend.Colour"))) {
				if (dialogConceptClassColor == null) {
					dialogConceptClassColor = new DialogConceptClassColor(id);
				}
				Color c = (Color) data;
				dialogConceptClassColor.setSelection(id, c);
				viewer.getNodeColors().updateAll();
				viewer.getVisualizationViewer().repaint();
				viewer.getMetaGraphPanel().repaint();
				try {
					dialogConceptClassColor.setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
				dialogConceptClassColor = null;
			}

			// update concept class shape
			if (columnName.equals(Config.language.getProperty("Legend.Shape"))) {
				if (dialogConceptClassShape == null) {
					dialogConceptClassShape = new DialogConceptClassShape(id);
				}
				Shape s = (Shape) data;
				dialogConceptClassShape.setSelection(id, s);
				viewer.getNodeShapes().updateAll();
				viewer.getVisualizationViewer().repaint();
				viewer.getMetaGraphPanel().repaint();
				try {
					dialogConceptClassShape.setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
				dialogConceptClassShape = null;
			}

			// update concept class size
			if (columnName.equals(Config.language.getProperty("Legend.Size"))) {
				Integer size = (Integer) data;

				// set new node size for concept class
				Config.visual.setProperty("ConceptClass.Size." + cc.getId(), size.toString());

				// update view
				viewer.getNodeShapes().setNodeShapeSelection(NodeShapeSelection.NONE);
				viewer.getNodeShapes().updateAll();
				viewer.getVisualizationViewer().repaint();
				Config.saveVisual();
			}

			// change concept class visibility
			if (columnName.equals(Config.language.getProperty("Legend.Visibility"))) {
				// change visibility button using meta graph functions
				String cmd = (Boolean) data ? "show" : "hide";
				viewer.getMetaGraph().actionPerformed(new ActionEvent(new ONDEXMetaConcept(graph, cc), ActionEvent.ACTION_PERFORMED, cmd));
			}
		}

		// for data sources
		else if (lastPane == 1) {
			DataSource ds = (DataSource) model.getValueAt(row, 1);
			String id = ds.getId();

			// update data source colour
			if (columnName.equals(Config.language.getProperty("Legend.Colour"))) {
				if (dialogDataSourceColor == null) {
					dialogDataSourceColor = new DialogDataSourceColor(id);
				}
				Color c = (Color) data;
				dialogDataSourceColor.setSelection(id, c);
				viewer.getNodeColors().updateAll();
				viewer.getVisualizationViewer().repaint();
				viewer.getMetaGraphPanel().repaint();
				try {
					dialogDataSourceColor.setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
				dialogDataSourceColor = null;
			}

			// change data source visibility
			if (columnName.equals(Config.language.getProperty("Legend.Visibility"))) {
				// similar behaviour to meta graph when hiding concept classes
				boolean visible = (Boolean) data;
				if (!visible)
					graph.setVisibility(graph.getRelationsOfDataSource(ds), visible);
				graph.setVisibility(graph.getConceptsOfDataSource(ds), visible);
				// update everything of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				viewer.getMetaGraphPanel().repaint();
				this.setViewer(this.getViewer());
			}
		}

		// for relation types
		else if (lastPane == 2) {
			RelationType rt = (RelationType) model.getValueAt(row, 2);
			String id = rt.getId();

			// update relation type colour
			if (columnName.equals(Config.language.getProperty("Legend.Colour"))) {
				if (dialogRelationTypeColor == null) {
					dialogRelationTypeColor = new DialogRelationTypeColor(id);
				}
				Color c = (Color) data;
				dialogRelationTypeColor.setSelection(id, c);
				viewer.getEdgeColors().updateAll();
				viewer.getVisualizationViewer().repaint();
				viewer.getMetaGraphPanel().repaint();
				try {
					dialogRelationTypeColor.setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
				dialogRelationTypeColor = null;
			}

			// update relation type size
			if (columnName.equals(Config.language.getProperty("Legend.Size"))) {
				Integer size = (Integer) data;

				// set new edge size for relation type
				Config.visual.setProperty("RelationType.Size." + rt.getId(), size.toString());
				viewer.getEdgeStrokes().updateAll();
				viewer.getVisualizationViewer().repaint();
				Config.saveVisual();
			}

			// change relation type visibility
			if (columnName.equals(Config.language.getProperty("Legend.Visibility"))) {
				// change visibility button using meta graph functions
				String cmd = (Boolean) data ? "show" : "hide";
				viewer.getMetaGraph().actionPerformed(new ActionEvent(new ONDEXMetaRelation(graph, rt, null), ActionEvent.ACTION_PERFORMED, cmd));
			}
		}

		// for evidence types
		else if (lastPane == 3) {
			EvidenceType et = (EvidenceType) model.getValueAt(row, 1);
			String id = et.getId();

			// update evidence type colour
			if (columnName.equals(Config.language.getProperty("Legend.Colour"))) {
				if (dialogEvidenceTypeColor == null) {
					dialogEvidenceTypeColor = new DialogEvidenceTypeColor(id);
				}
				Color c = (Color) data;
				dialogEvidenceTypeColor.setSelection(id, c);
				viewer.getNodeColors().updateAll();
				viewer.getEdgeColors().updateAll();
				viewer.getVisualizationViewer().repaint();
				viewer.getMetaGraphPanel().repaint();
				try {
					dialogEvidenceTypeColor.setClosed(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
				dialogEvidenceTypeColor = null;
			}

			// change evidence type visibility concepts
			if (columnName.equals(Config.language.getProperty("Legend.EvidenceTypes.VisibilityConcepts"))) {
				graph.setVisibility(graph.getConceptsOfEvidenceType(et), (Boolean) data);
				// update everything of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				viewer.getMetaGraphPanel().repaint();
				this.setViewer(this.getViewer());
			}

			// change evidence type visibility relations
			if (columnName.equals(Config.language.getProperty("Legend.EvidenceTypes.VisibilityRelations"))) {
				graph.setVisibility(graph.getRelationsOfEvidenceType(et), (Boolean) data);
				// update everything of change
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				viewer.getMetaGraphPanel().repaint();
				this.setViewer(this.getViewer());
			}
		}
	}

	/**
	 * Pack all columns in table
	 * 
	 * @param table
	 */
	public void packColumns(JTable table) {
		for (int c = 0; c < table.getColumnCount(); c++) {
			packColumn(table, c, 2);
		}
	}

	/**
	 * Sets the preferred width of the visible column specified by vColIndex.
	 * The column will be just wide enough to show the column head and the
	 * widest cell in the column. margin pixels are added to the left and right
	 * (resulting in an additional width of 2*margin pixels).
	 */
	public void packColumn(JTable table, int vColIndex, int margin) {
		DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);
		int width = 0;

		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null) {
			renderer = table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
		width = comp.getPreferredSize().width;

		// Get maximum width of column data
		for (int r = 0; r < table.getRowCount(); r++) {
			renderer = table.getCellRenderer(r, vColIndex);
			comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}

		// Add margin
		width += 2 * margin;

		// Set the width
		col.setPreferredWidth(width);
	}
}
