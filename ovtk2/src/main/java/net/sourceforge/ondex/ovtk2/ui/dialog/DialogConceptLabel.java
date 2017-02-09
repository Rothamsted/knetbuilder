package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeLabels.LabelCompositionRule;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.xml.ConceptLabelXMLReader;
import net.sourceforge.ondex.ovtk2.util.xml.ConceptLabelXMLWriter;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.io.CharsetNames;

import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * Dialog to configure combinations for concept labels.
 * 
 * @author taubertj
 * 
 */
public class DialogConceptLabel extends OVTK2Dialog {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 1927615194290133310L;

	/**
	 * Relative position of label for JUNG
	 */
	public JComboBox labelPositions = null;

	/**
	 * Current viewer
	 */
	private OVTK2PropertiesAggregator viewer = null;

	/**
	 * Current ONDEXJUNGGraph
	 */
	private ONDEXJUNGGraph graph = null;

	/**
	 * Key for annotation retrieval
	 */
	public static final String KEY = "dialogconceptlabel";

	/**
	 * Table to hold label rules
	 */
	public JTable table = null;

	/**
	 * Constructs the settings dialog as internal frame.
	 * 
	 */
	public DialogConceptLabel(final OVTK2PropertiesAggregator viewer) {
		super("Dialog.ConceptLabel.Title", "Preferences16.gif");

		this.viewer = viewer;
		this.fieldWidth = 75;
		this.graph = viewer.getONDEXJUNGGraph();

		// panel for label positions, from demo copied
		JPanel positionPanel = new JPanel(new FlowLayout());
		positionPanel.setBorder(BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.ConceptLabel.LabelPosition")));
		labelPositions = new JComboBox();
		labelPositions.addItem(Renderer.VertexLabel.Position.N);
		labelPositions.addItem(Renderer.VertexLabel.Position.NE);
		labelPositions.addItem(Renderer.VertexLabel.Position.E);
		labelPositions.addItem(Renderer.VertexLabel.Position.SE);
		labelPositions.addItem(Renderer.VertexLabel.Position.S);
		labelPositions.addItem(Renderer.VertexLabel.Position.SW);
		labelPositions.addItem(Renderer.VertexLabel.Position.W);
		labelPositions.addItem(Renderer.VertexLabel.Position.NW);
		labelPositions.addItem(Renderer.VertexLabel.Position.N);
		labelPositions.addItem(Renderer.VertexLabel.Position.CNTR);
		labelPositions.addItem(Renderer.VertexLabel.Position.AUTO);
		labelPositions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Renderer.VertexLabel.Position position = (Renderer.VertexLabel.Position) e.getItem();
				viewer.getVisualizationViewer().getRenderer().getVertexLabelRenderer().setPosition(position);
				viewer.getVisualizationViewer().repaint();
			}
		});
		labelPositions.setSelectedItem(Renderer.VertexLabel.Position.AUTO);
		positionPanel.add(labelPositions);

		// add components to content pane
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeConfigPanel(), BorderLayout.CENTER);
		JPanel south = new JPanel();
		BoxLayout layout = new BoxLayout(south, BoxLayout.PAGE_AXIS);
		south.setLayout(layout);
		south.add(positionPanel);
		south.add(makeButtonsPanel("Dialog.ConceptLabel.Apply", "Dialog.ConceptLabel.Cancel"));
		this.add(south, BorderLayout.SOUTH);
		this.pack();

		getSettings();
	}

	/**
	 * Load possible label settings from annotations
	 * 
	 */
	private void getSettings() {

		Map<String, String> annotations = this.viewer.getONDEXJUNGGraph().getAnnotations();
		if (annotations != null && annotations.containsKey(KEY)) {

			// get settings from graph
			String xml = annotations.get(KEY);
			if (xml == null || xml.trim().length() == 0)
				return;

			// configure XML input
			System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
			XMLInputFactory2 xmlInput = (XMLInputFactory2) XMLInputFactory2.newInstance();
			xmlInput.configureForSpeed();

			// parse from a String
			final ByteArrayInputStream inStream = new ByteArrayInputStream(xml.getBytes());
			try {
				// configure Parser
				XMLStreamReader2 xmlReadStream = (XMLStreamReader2) xmlInput.createXMLStreamReader(inStream, CharsetNames.CS_UTF8);

				// de-serialise annotations from XML
				ConceptLabelXMLReader.read(xmlReadStream, this);

				xmlReadStream.close();

			} catch (XMLStreamException e1) {
				ErrorDialog.show(e1);
			}

			// editors used for meta data selection etc.
			JComboBox accessionDataSources = populateAccessionDataSources();
			JComboBox filterDataSources = populateFilterDataSources();
			JComboBox filterConceptClasses = populatFilterConceptClasses();
			JComboBox separator = populateSeparator();

			// set data source editor
			TableColumn dataSourceColumn = table.getColumnModel().getColumn(0);
			dataSourceColumn.setCellEditor(new DefaultCellEditor(filterDataSources));

			// set concept class editor
			TableColumn conceptClassColumn = table.getColumnModel().getColumn(1);
			conceptClassColumn.setCellEditor(new DefaultCellEditor(filterConceptClasses));

			// set accession editor
			TableColumn accessionColumn = table.getColumnModel().getColumn(2);
			accessionColumn.setCellEditor(new DefaultCellEditor(accessionDataSources));

			// set separator editor
			TableColumn separatorColumn = table.getColumnModel().getColumn(3);
			separatorColumn.setCellEditor(new DefaultCellEditor(separator));
		}
	}

	/**
	 * Populates a alphabetically sorted box of non empty data sources and ALL
	 * label.
	 * 
	 * @return JComboBox of String
	 */
	private JComboBox populateFilterDataSources() {

		// only display non-empty data sources
		Set<String> nonEmpty = new HashSet<String>();
		for (DataSource ds : graph.getMetaData().getDataSources()) {
			if (graph.getConceptsOfDataSource(ds).size() > 0)
				nonEmpty.add(ds.getId());
		}

		// sort data sources
		String[] sorted = nonEmpty.toArray(new String[0]);
		Arrays.sort(sorted);

		// add default label
		String[] all = new String[nonEmpty.size() + 1];
		all[0] = Config.language.getProperty("Dialog.ConceptLabel.All");
		for (int i = 0; i < sorted.length; i++) {
			all[i + 1] = sorted[i];
		}

		// select default case
		JComboBox box = new JComboBox(all);
		box.setSelectedIndex(0);

		return box;
	}

	/**
	 * Populates a alphabetically sorted box of non empty concept classes and
	 * ALL label.
	 * 
	 * @return JComboBox of String
	 */
	private JComboBox populatFilterConceptClasses() {

		// only display non-empty concept classes
		Set<String> nonEmpty = new HashSet<String>();
		for (ConceptClass cc : graph.getMetaData().getConceptClasses()) {
			if (graph.getConceptsOfConceptClass(cc).size() > 0)
				nonEmpty.add(cc.getId());
		}

		// sort concept classes
		String[] sorted = nonEmpty.toArray(new String[0]);
		Arrays.sort(sorted);

		// add default label
		String[] all = new String[nonEmpty.size() + 1];
		all[0] = Config.language.getProperty("Dialog.ConceptLabel.All");
		for (int i = 0; i < sorted.length; i++) {
			all[i + 1] = sorted[i];
		}

		// select default case
		JComboBox box = new JComboBox(all);
		box.setSelectedIndex(0);

		return box;
	}

	/**
	 * Populates a alphabetically sorted box of all data sources and NONE label.
	 * 
	 * @return JComboBox of String
	 */
	private JComboBox populateAccessionDataSources() {

		// get all data sources
		Set<String> dataSources = new HashSet<String>();
		for (DataSource ds : graph.getMetaData().getDataSources()) {
			dataSources.add(ds.getId());
		}

		// sort data sources
		String[] sorted = dataSources.toArray(new String[0]);
		Arrays.sort(sorted);

		// add default label
		String[] all = new String[sorted.length + 1];
		all[0] = Config.language.getProperty("Dialog.ConceptLabel.None");
		for (int i = 0; i < sorted.length; i++) {
			all[i + 1] = sorted[i];
		}

		// select default case
		JComboBox box = new JComboBox(all);
		box.setSelectedIndex(0);

		return box;
	}

	/**
	 * Populates selection box for separator string.
	 * 
	 * @return JComboBox of String
	 */
	private JComboBox populateSeparator() {
		// editor for name concatenation string
		JComboBox separator = new JComboBox();
		separator.addItem(" | ");
		separator.addItem("/");
		separator.addItem("\\");
		separator.addItem(" - ");
		separator.addItem("_");
		separator.addItem(":");
		separator.addItem(";");
		separator.addItem(".");
		separator.addItem("#");
		separator.setSelectedIndex(0);
		return separator;
	}

	/**
	 * Returns the correct class according to row 0 objects.
	 * 
	 * @author taubertj
	 * 
	 */
	private class MyTableModel extends DefaultTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyTableModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return this.getValueAt(0, columnIndex).getClass();
		}
	}

	/**
	 * Make Login input panel.
	 * 
	 * @return JPanel
	 */
	private JPanel makeConfigPanel() {

		// editors used for meta data selection etc.
		JComboBox accessionDataSources = populateAccessionDataSources();
		JComboBox filterDataSources = populateFilterDataSources();
		JComboBox filterConceptClasses = populatFilterConceptClasses();
		JComboBox separator = populateSeparator();

		// editors for choices
		JCheckBox includeParserID = new JCheckBox();
		JCheckBox preferredName = new JCheckBox();
		preferredName.setSelected(true);

		// table headers
		String[] columnNames = new String[] { Config.language.getProperty("Dialog.ConceptLabel.DataSource"), Config.language.getProperty("Dialog.ConceptLabel.ConceptClass"), Config.language.getProperty("Dialog.ConceptLabel.Accession"), Config.language.getProperty("Dialog.ConceptLabel.Separator"), Config.language.getProperty("Dialog.ConceptLabel.Prefix"), Config.language.getProperty("Dialog.ConceptLabel.RestrictLength"), Config.language.getProperty("Dialog.ConceptLabel.PreferredName"), Config.language.getProperty("Dialog.ConceptLabel.IncludeParserID") };

		// table data in first row
		Object[][] data = new Object[][] { { filterConceptClasses.getSelectedItem(), filterDataSources.getSelectedItem(), accessionDataSources.getSelectedItem(), separator.getSelectedItem(), "", Integer.valueOf(0), Boolean.valueOf(preferredName.isSelected()), Boolean.valueOf(includeParserID.isSelected()) } };

		// put everything in a table
		table = new JTable(new MyTableModel(data, columnNames));

		// set data source editor
		TableColumn dataSourceColumn = table.getColumnModel().getColumn(0);
		dataSourceColumn.setCellEditor(new DefaultCellEditor(filterDataSources));

		// set concept class editor
		TableColumn conceptClassColumn = table.getColumnModel().getColumn(1);
		conceptClassColumn.setCellEditor(new DefaultCellEditor(filterConceptClasses));

		// set accession editor
		TableColumn accessionColumn = table.getColumnModel().getColumn(2);
		accessionColumn.setCellEditor(new DefaultCellEditor(accessionDataSources));

		// set separator editor
		TableColumn separatorColumn = table.getColumnModel().getColumn(3);
		separatorColumn.setCellEditor(new DefaultCellEditor(separator));

		// layout and put a border around the table
		JPanel content = new JPanel(new BorderLayout());
		TitledBorder contentBorder;
		contentBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.ConceptLabel.Label"));
		content.setBorder(contentBorder);
		content.add(BorderLayout.CENTER, new JScrollPane(table));

		// add buttons for table control
		JPanel buttons = new JPanel(new GridLayout(1, 2));

		JButton addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
		buttons.add(addButton);

		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);
		buttons.add(removeButton);

		content.add(BorderLayout.SOUTH, buttons);

		return content;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

		String cmd = arg0.getActionCommand();

		if (cmd.equals("apply")) {

			// get values for all rows
			for (int row = 0; row < table.getRowCount(); row++) {

				// check if something different than NONE was selected
				DataSource accessionDataSource = null;
				if (!table.getValueAt(row, 2).equals(Config.language.getProperty("Dialog.ConceptLabel.None"))) {
					// get DataSource for id of drop-down box
					accessionDataSource = viewer.getONDEXJUNGGraph().getMetaData().getDataSource((String) table.getValueAt(row, 2));
				}

				// get more data from table
				boolean includeParserID = (Boolean) table.getValueAt(row, 7);
				boolean includePreferredName = (Boolean) table.getValueAt(row, 6);
				int length = (Integer) table.getValueAt(row, 5);
				String prefix = (String) table.getValueAt(row, 4);
				String separator = (String) table.getValueAt(row, 3);

				// contruct new rule
				LabelCompositionRule rule = new LabelCompositionRule(accessionDataSource, includeParserID, includePreferredName, length, prefix, separator);

				// get index by values and add rule
				String dataSource = (String) table.getValueAt(row, 0);
				String conceptClass = (String) table.getValueAt(row, 1);
				viewer.getNodeLabels().clearRules();
				viewer.getNodeLabels().addRule(dataSource, conceptClass, rule);
			}

			viewer.getNodeLabels().updateAll();
			viewer.getVisualizationViewer().fireStateChanged();
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}

			// configure XML output
			XMLOutputFactory2 xmlOutput = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
			xmlOutput.configureForSpeed();
			xmlOutput.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);

			// output goes into a String
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			try {
				// configure writer
				XMLStreamWriter2 xmlWriteStream = (XMLStreamWriter2) xmlOutput.createXMLStreamWriter(outStream, CharsetNames.CS_UTF8);

				// serialise settings to XML
				ConceptLabelXMLWriter.write(xmlWriteStream, this);

				xmlWriteStream.flush();
				xmlWriteStream.close();

				// set annotation data to graph
				viewer.getONDEXJUNGGraph().getAnnotations().put(KEY, outStream.toString());

			} catch (XMLStreamException e1) {
				ErrorDialog.show(e1);
			}

		} else if (cmd.equals("cancel")) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		} else if (cmd.equals("add")) {
			MyTableModel model = (MyTableModel) table.getModel();
			// clone last row
			int row = table.getRowCount() - 1;
			Object[] newRow = new Object[table.getColumnCount()];
			for (int col = 0; col < table.getColumnCount(); col++) {
				newRow[col] = table.getValueAt(row, col);
			}
			model.addRow(newRow);

		} else if (cmd.equals("remove")) {
			// check if table has at least one row left
			if (table.getRowCount() > 1) {
				MyTableModel model = (MyTableModel) table.getModel();
				model.removeRow(table.getRowCount() - 1);
			}
		}
	}
}
