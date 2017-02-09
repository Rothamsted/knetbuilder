package net.sourceforge.ondex.ovtk2.filter.genomic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.algorithm.graphquery.GraphTraverser;
import net.sourceforge.ondex.algorithm.graphquery.StateMachine;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidFileException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.flatfile.StateMachineFlatFileParser2;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.filter.OVTK2Filter;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.layout.GDSPositionLayout;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.util.IntegerStringWrapper;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.visualization.VisualizationModel;

/**
 * Filter for the Poplar data.
 * <p/>
 * Combines different Filters and Annotators to efficiently search the poplar
 * graph
 * 
 * @author keywan, taubertj
 */
public class InteractiveGenomicFilter extends OVTK2Filter implements
		ActionListener {

	// table headers
	private static final String HEADER_USE = ".";
	private static final String HEADER_BEGIN = "Begin";
	private static final String HEADER_CHROMOSOME = "Chromosome";
	private static final String HEADER_END = "End";
	private static final String HEADER_QTL = "QTL";

	/**
	 * generated
	 */
	private static final long serialVersionUID = 7921683991540567576L;

	// provides choice of category, e.g. chromosome
	private JComboBox chromosomeBox;

	// contains the table
	private JPanel panelConfig;

	// contains general operation buttons
	private JPanel panelOperations;

	// search options as previously defined
	private JPanel panelSearch;

	private JButton hideGenesButton;

	// algorithm selection
	private ButtonGroup buttonGroup;

	// result for ONDEXConcept
	private Set<ONDEXConcept> resultConcepts;

	// result for ONDEXRelation
	private Set<ONDEXRelation> resultRelations;

	// table specifying category and position filter, e.g. regions on chromosome
	private JTableX searchTable;

	// input a search string
	private JTextField textFieldSearch;
	
	// input a absolute path to the semantic motifs file
	private JTextField filePathField;

	// toggles how to set relations
	private boolean toggleRelations = true;

	// save for each chromosome a JComboBox with all its QTLs
	private HashMap<Integer, JComboBox> chromosome2qtl = new HashMap<Integer, JComboBox>();

	// stores for each row (Integer) of the table
	// its own QTL editor
	private RowEditorModel rm = new RowEditorModel();

	// attributes holding main genomic information
	private AttributeName anBegin, anEnd, anChromosome, anScaffold, attTaxid;

	// ConceptClass QTL and Chromosome
	private ConceptClass ccQTL, ccChromosome, ccScaffold, ccGene;

	// relations found by search
	private Set<ONDEXRelation> rels = null;

	// concepts hidden by toggle search results
	private Set<ONDEXConcept> invisibleConcepts = null;

	// relations hidden by toggle search results
	private Set<ONDEXRelation> invisibleRelations = null;

	// toggles visibility of relations returned by search
	private boolean toggleSearchResults = true;
	
	private Map<ONDEXConcept, Integer> conSizes = LazyMap.decorate(
			new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Config.defaultNodeSize;
				}
			});
	private Map<ONDEXRelation, Integer> relSizes = LazyMap.decorate(
			new HashMap<ONDEXRelation, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Config.defaultEdgeSize;
				}
			});

	/**
	 * Constructor populates GUI with defaults.
	 * 
	 * @param viewer
	 *            OVTK2Viewer
	 */
	public InteractiveGenomicFilter(OVTK2Viewer viewer) {
		super(viewer);

		anBegin = graph.getMetaData().getAttributeName("BEGIN");
		anEnd = graph.getMetaData().getAttributeName("END");
		anChromosome = graph.getMetaData().getAttributeName("Chromosome");
		anScaffold = graph.getMetaData().getAttributeName("Scaffold");
		attTaxid = graph.getMetaData().getAttributeName("TAXID");
		ccQTL = graph.getMetaData().getConceptClass("QTL");
		if (ccQTL == null) {
			ccQTL = graph.getMetaData().createConceptClass("QTL", "QTL",
					"Quantitative Trait Loci", null);
		}
		ccChromosome = graph.getMetaData().getConceptClass("Chromosome");
		ccScaffold = graph.getMetaData().getConceptClass("Scaffold");
		ccGene = graph.getMetaData().getConceptClass("Gene");

		// chooser for chromosome
		chromosomeBox = new JComboBox();
		chromosomeBox.addActionListener(this);
		addContextToComboBox(chromosomeBox);

		// populate table with defaults
		defaultTable();

		// set layout
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);

		/*
		 * general information and operations
		 */
		panelOperations = new JPanel(new BorderLayout());
		panelOperations.setBorder(BorderFactory
				.createTitledBorder("Operations"));

		// simple text
		JLabel label = new JLabel(
				"Please select nodes from graph or populate the table below.");
		panelOperations.add(label, BorderLayout.NORTH);

		// simple panel for all buttons
		JPanel panelButtons = new JPanel(new GridLayout(4, 1));
		panelOperations.add(panelButtons, BorderLayout.CENTER);

		// changes layout of graph to genomic
		JButton buttonView = new JButton("Switch to genomic view");
		buttonView.setActionCommand("view");
		buttonView.addActionListener(this);
		panelButtons.add(buttonView);

		// the update table button
		JButton buttonUpdate = new JButton("Update table from selection");
		buttonUpdate.setActionCommand("update");
		buttonUpdate.addActionListener(this);
		panelButtons.add(buttonUpdate);

		// insert a new row of data
		JButton buttonInsert = new JButton("Insert new row in table");
		buttonInsert.setActionCommand("insert");
		buttonInsert.addActionListener(this);
		panelButtons.add(buttonInsert);

		// clear table
		JButton buttonClear = new JButton("Default table");
		buttonClear.setActionCommand("clear");
		buttonClear.addActionListener(this);
		panelButtons.add(buttonClear);

		/*
		 * table based configuration
		 */
		panelConfig = new JPanel();
		BoxLayout configLayout = new BoxLayout(panelConfig, BoxLayout.PAGE_AXIS);
		panelConfig.setLayout(configLayout);
		panelConfig.setBorder(BorderFactory
				.createTitledBorder("Region Selection"));

		// add table in scroll pane
		JScrollPane jSP = new JScrollPane(searchTable);
		Dimension jSPSize = jSP.getPreferredSize();
		jSP.setPreferredSize(new Dimension((int) jSPSize.getWidth(), 150));
		panelConfig.add(jSP);

		/*
		 * presents search options
		 */
		panelSearch = new JPanel();
		BoxLayout optionsLayout = new BoxLayout(panelSearch,
				BoxLayout.PAGE_AXIS);
		panelSearch.setLayout(optionsLayout);
		panelSearch.setBorder(BorderFactory
				.createTitledBorder("Search options"));
		
		
		// input for keyword
		filePathField = new javax.swing.JTextField();
		filePathField.setColumns(18);
		filePathField
				.setToolTipText("Enter path to semantic motif file");
		filePathField.setText(Config.ovtkDir + "\\SemanticMotifs.txt");
		panelSearch.add(filePathField);

		// input for keyword
		textFieldSearch = new javax.swing.JTextField();
		textFieldSearch.setColumns(18);
		textFieldSearch
				.setToolTipText("Enter keywords e.g.: auxin|shoot branching");
		textFieldSearch.setActionCommand("searchRegion");
		textFieldSearch.addActionListener(this);
		panelSearch.add(textFieldSearch);
		
		// neighbourhood search
		JRadioButton defaultButton = new javax.swing.JRadioButton();
		defaultButton.setText("All motifs");
		defaultButton.setActionCommand("neighbourhood");
		defaultButton.setSelected(true);
		panelSearch.add(defaultButton);

		// shortest path search
		JRadioButton goButton = new javax.swing.JRadioButton();
		goButton.setText("GO Annotation");
		goButton.setActionCommand("goa");
		panelSearch.add(goButton);

		// some other interesting stuff
		JRadioButton pathwayButton = new javax.swing.JRadioButton();
		pathwayButton.setText("Pathway");
		pathwayButton.setActionCommand("pathway");
		panelSearch.add(pathwayButton);

		// group them all together
		buttonGroup = new javax.swing.ButtonGroup();
		buttonGroup.add(defaultButton);
		buttonGroup.add(goButton);
		buttonGroup.add(pathwayButton);

		// the search button
		JButton searchButton = new javax.swing.JButton();
		searchButton.setText("Search Region");
		searchButton.setActionCommand("searchRegion");
		searchButton.addActionListener(this);
		panelSearch.add(searchButton);

		// the genome search button
		JButton searchAllButton = new javax.swing.JButton();
		searchAllButton.setText("Search Genome");
		searchAllButton.setActionCommand("searchGenome");
		searchAllButton.addActionListener(this);
		panelSearch.add(searchAllButton);

		hideGenesButton = new JButton("Show/Hide unrelated genes");
		hideGenesButton.setActionCommand("hide");
		hideGenesButton.addActionListener(this);
		panelSearch.add(hideGenesButton);

		// toggle visibility of inter-relations
//		JButton relationButton = new JButton("Toggle inter-relations");
//		relationButton.setActionCommand("relations");
//		relationButton.addActionListener(this);
//		panelSearch.add(relationButton);

		// add to filter panel
		this.add(panelOperations);
		this.add(panelConfig);
		this.add(panelSearch);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		/*
		 * change graph to genomic view
		 */
		if (cmd.equals("view")) {
			switchToGenomicLayout();
			if (viewer.isIcon()) {
				try {
					viewer.setIcon(false);
				} catch (PropertyVetoException pve) {
					// ignore, can't help
				}
			}
		}

		/*
		 * populate table with user selection
		 */
		else if (cmd.equals("update")) {
			panelConfig.removeAll();
			getSelection();
			panelConfig.add(new JScrollPane(searchTable));
			this.revalidate();
		}

		/*
		 * insert a default row
		 */
		else if (cmd.equals("insert")) {
			insertRow();
		}

		/*
		 * reset table to default
		 */
		else if (cmd.equals("clear")) {
			panelConfig.removeAll();
			defaultTable();
			panelConfig.add(new JScrollPane(searchTable));
			this.revalidate();
		}

		else if (cmd.equals("hide")) {

			StateEdit edit = new StateEdit(new VisibilityUndo(
					viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());

			if (toggleSearchResults) {

				// preserve for making visible again
				invisibleConcepts = new HashSet<ONDEXConcept>();
				invisibleRelations = new HashSet<ONDEXRelation>();

				// set relations that are NOT on a path to invisible
				for (ONDEXRelation r : graph.getEdges()) {
					if (rels != null && !rels.contains(r)) {
						graph.setVisibility(r, false);
						invisibleRelations.add(r);
					}
				}

				// now set concepts that become unconnected to invisible
				for (ONDEXConcept c : graph.getVertices()) {
					if (graph.getNeighborCount(c) == 0) {
						graph.setVisibility(c, false);
						invisibleConcepts.add(c);
					}
				}
				toggleSearchResults = false;
			} else {
				graph.setVisibility(invisibleConcepts, true);
				graph.setVisibility(invisibleRelations, true);
				toggleSearchResults = true;
			}

			viewer.getVisualizationViewer().getModel().fireStateChanged();
			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
		}

		/*
		 * do the search
		 */
		else if (cmd.equals("searchRegion")) {
			StateEdit edit = new StateEdit(new VisibilityUndo(
					viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());
			toggleSearchResults = true;
			invisibleConcepts = null;
			invisibleRelations = null;
			
			performSearch();
			
			//if keywords are provided set not related genes to invisible
			if(!textFieldSearch.getText().trim().isEmpty())
				hideGenesButton.doClick();

			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
			if (viewer.isIcon()) {
				try {
					viewer.setIcon(false);
				} catch (PropertyVetoException pve) {
					// ignore, can't help
				}
			}
		}

		/*
		 * do the search
		 */
		else if (cmd.equals("searchGenome")) {

			// get search keyword
			String keyword = textFieldSearch.getText();

			if (!keyword.isEmpty()) {
				
				toggleSearchResults = true;
				invisibleConcepts = null;
				invisibleRelations = null;
				
				StateEdit edit = new StateEdit(new VisibilityUndo(
						viewer.getONDEXJUNGGraph()), this.getName());
				OVTK2Desktop desktop = OVTK2Desktop.getInstance();
				desktop.setRunningProcess(this.getName());

				// set first entire graph to invisible
				setGraphVisible(false);

				searchGenome(keyword);

				// new Attribute layout
				GDSPositionLayout layout = new GDSPositionLayout(viewer);

				// propagate change to viewer
				VisualizationModel<ONDEXConcept, ONDEXRelation> model = viewer
						.getVisualizationViewer().getModel();
				model.setGraphLayout(layout);
				model.fireStateChanged();
				viewer.center();

				edit.end();
				viewer.getUndoManager().addEdit(edit);
				desktop.getOVTK2Menu().updateUndoRedo(viewer);
				desktop.notifyTerminationOfProcess();
				if (viewer.isIcon()) {
					try {
						viewer.setIcon(false);
					} catch (PropertyVetoException pve) {
						// ignore, can't help
					}
				}
			}

		}

		/*
		 * toggle inter-relations
		 */
		else if (cmd.equals("relations")) {
			StateEdit edit = new StateEdit(new VisibilityUndo(
					viewer.getONDEXJUNGGraph()), this.getName());
			OVTK2Desktop desktop = OVTK2Desktop.getInstance();
			desktop.setRunningProcess(this.getName());

			RelationType rt = graph.getMetaData().getRelationType("enc");
			toggleRelations = !toggleRelations;

			for (ONDEXRelation r : graph.getRelationsOfRelationType(rt)) {
				graph.setVisibility(r, toggleRelations);
			}

			viewer.getVisualizationViewer().getModel().fireStateChanged();
			edit.end();
			viewer.getUndoManager().addEdit(edit);
			desktop.getOVTK2Menu().updateUndoRedo(viewer);
			desktop.notifyTerminationOfProcess();
		}
		/*
		 * actions that happen when changing chromosome or QTL column
		 */
		else if (e.getSource() instanceof JComboBox) {
			Object obj = ((JComboBox) e.getSource()).getSelectedItem();
			if (obj instanceof String || searchTable == null) {
				return;
			}
			DefaultTableModel model = (DefaultTableModel) searchTable
					.getModel();
			int rowNum = searchTable.getSelectedRow();
			int colNum = searchTable.getSelectedColumn();
			IntegerStringWrapper isw = (IntegerStringWrapper) obj;
			// chromosome column

			if (colNum == 1) {
				ONDEXConcept chrom = graph.getConcept(isw.getValue());
				// ignore scaffolds
				if (chrom.getAttribute(anChromosome) != null) {
					int chromNum = (Integer) chrom.getAttribute(anChromosome)
							.getValue();
					JComboBox qtlBox = createQTLComboBox(chromNum);
					DefaultCellEditor qtlEditor = new DefaultCellEditor(qtlBox);
					rm.addEditorForRow(rowNum, qtlEditor);
				}
			}
			// QTL column
			else if (colNum == 4) {
				ONDEXConcept qtl = graph.getConcept(isw.getValue());
				// System.out.println("QTL changed: "+qtl.getConceptName().getName());
				// TODO: why is colNum 4 sometimes of Type Chromosome?
				if (qtl.getOfType().getId().equals("QTL")) {
					Integer newBeg = (Integer) qtl.getAttribute(anBegin)
							.getValue();
					Integer newEnd = (Integer) qtl.getAttribute(anEnd)
							.getValue();
					model.setValueAt(newBeg, rowNum, 2);
					model.setValueAt(newEnd, rowNum, 3);
				} else {
					// System.out.println("COLUMN IS 4 BUT ELEMENT: "+
					// qtl.getOfType().getId());
				}
			}
		}

	}

	/**
	 * Adds the context (Chromosome) items to the drop-down list and sorts them
	 * alpha-numerically
	 * 
	 * @param chromosomeBox
	 *            JComboBox for category
	 */
	private void addContextToComboBox(JComboBox chromosomeBox) {
		// add context items to combo box
		chromosomeBox.removeAllItems();
		if (ccGene != null && ccChromosome != null) {
			Set<ONDEXConcept> viewGene = graph.getConceptsOfConceptClass(ccGene);
			Set<ONDEXConcept> viewChro = graph
					.getConceptsOfConceptClass(ccChromosome);
			Set<ONDEXConcept> viewScaf = graph
					.getConceptsOfConceptClass(ccScaffold);

			// iterate over all chromosomes which are used as context
			for (ONDEXConcept c : BitSetFunctions.or(viewChro, viewScaf)) {
				// take chromosome only if it's the context of any gene
				if (BitSetFunctions.and(graph.getConceptsOfTag(c), viewGene)
						.size() == 0)
					continue;
				chromosomeBox.addItem(new IntegerStringWrapper(c.getId(),
						getDefaultNameForConcept(c)));
				// QTLs are only on chromosomes and not on scaffolds
				if (c.getOfType().equals(ccChromosome)) {
					JComboBox jCB = createQTLComboBox((Integer) c.getAttribute(
							anChromosome).getValue());
					chromosome2qtl.put(c.getId(), jCB);
				}
			}
			sortContexts(chromosomeBox);
			chromosomeBox.revalidate();
		}
	}

	/**
	 * Creates a JComboBox containing all QTLs for the given Chromosome The
	 * validation if a QTL is located on the given chromosome number is based on
	 * comparison of AttributeName "Chromosome" (Integer)
	 * 
	 * @param chromosome
	 * @return
	 */
	private JComboBox createQTLComboBox(int chromosome) {

		JComboBox qtlBox = new JComboBox();
		qtlBox.removeAllItems();
		qtlBox.addItem("...");
		// iterate over all QTLs
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(ccQTL)) {
			// get QTL's AttributeName "Chromosome"
			int qtlPos = (Integer) c.getAttribute(anChromosome).getValue();
			// check if QTL is on given chromosome
			if (qtlPos == chromosome) {
				// System.out.println("QTL was added to chromosome");
				qtlBox.addItem(new IntegerStringWrapper(c.getId(),
						getDefaultNameForConcept(c)));
			}
		}
		qtlBox.revalidate();
		qtlBox.addActionListener(this);

		return qtlBox;
	}

	/**
	 * calculates the optimal width of a given table by summing the maximal
	 * width of each column
	 * 
	 * @param table
	 *            table which should be calculated
	 */
	private void calcColumnWidths(JTable table) {

		// get table header and renderer
		JTableHeader header = table.getTableHeader();
		TableCellRenderer defaultHeaderRenderer = null;
		if (header != null)
			defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		TableModel data = table.getModel();

		int margin = columns.getColumnMargin();
		int rowCount = data.getRowCount();
		int totalWidth = 0;
		// go through all columns
		for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
			// get current column
			TableColumn column = columns.getColumn(i);
			int columnIndex = column.getModelIndex();
			int width = -1;

			// get header for current column
			TableCellRenderer h = column.getHeaderRenderer();
			if (h == null)
				h = defaultHeaderRenderer;
			if (h != null) { // Not explicitly impossible
				Component c = h.getTableCellRendererComponent(table,
						column.getHeaderValue(), false, false, -1, i);
				// get header width
				width = (int) (c.getPreferredSize().width * 1.1);
			}

			// go through all rows
			for (int row = rowCount - 1; row >= 0; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);
				Component c = r
						.getTableCellRendererComponent(table,
								data.getValueAt(row, columnIndex), false,
								false, row, i);
				// max of header width and actual component width
				width = Math.max(width, c.getPreferredSize().width);
			}

			if (width >= 0)
				column.setPreferredWidth(width + margin);
			// setting and summing preferred width of each column
			totalWidth += column.getPreferredWidth();
		}
		// set width to table
		Dimension size = table.getPreferredScrollableViewportSize();
		size.width = totalWidth;
		table.setPreferredScrollableViewportSize(size);
	}
	
	private void searchGenome(String regex){
		
		// empty views to begin with
		resultConcepts = new HashSet<ONDEXConcept>();
		resultRelations = new HashSet<ONDEXRelation>();
		
		conSizes = LazyMap.decorate(
				new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultNodeSize;
					}
				});
		relSizes = LazyMap.decorate(
				new HashMap<ONDEXRelation, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultEdgeSize;
					}
				});
		
		// reset node and edge sizes
		viewer.getNodeShapes().setNodeSizes(
				new Transformer<ONDEXConcept, Integer>() {
					@Override
					public Integer transform(ONDEXConcept input) {
						return Config.defaultNodeSize;
					}
				});
		viewer.getEdgeStrokes().setEdgeSizes(
				new Transformer<ONDEXRelation, Integer>() {
					@Override
					public Integer transform(ONDEXRelation input) {
						return Config.defaultEdgeSize;
					}
				});
		
		Integer annotateSize = 50;
		
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		Set<ONDEXConcept> genes = graph.getConceptsOfConceptClass(ccGene);
		Set<ONDEXConcept> seed = new HashSet<ONDEXConcept>();
		for (ONDEXConcept gene : genes){
			if((gene.getAttribute(attTaxid)!=null) &&
					gene.getAttribute(attTaxid).getValue().equals("3702"))
				seed.add(gene);
		}
		
		
		System.out.println("Number of genes in network: "+genes.size());
		System.out.println("Number of seed genes with taxid 3702: "+seed.size());
		
		
    	StateMachineFlatFileParser2 smp = new StateMachineFlatFileParser2();
    	try {
    		smp.parseFile(new File(filePathField.getText()), graph);
    	} catch (InvalidFileException e) {
    		e.printStackTrace();
    	} catch (StateMachineInvalidException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	StateMachine sm = smp.getStateMachine();


    	//then we create a crawler using our rules
    	GraphTraverser gt =  new GraphTraverser(sm);

    	//the results give us a map of every starting concept to every valid path
    	Map<ONDEXConcept,List<EvidencePathNode>> results = gt.traverseGraph(graph, seed, null);
    	Set<ONDEXConcept> keywordConcepts = new HashSet<ONDEXConcept>();
    	Set<ONDEXConcept> candidateGenes = new HashSet<ONDEXConcept>();
    	
    	for(List<EvidencePathNode> paths : results.values()){
    		for(EvidencePathNode  path : paths){
    			
    			//search last concept of semantic motif for keyword
    			int indexLastCon = path.getConceptsInPositionOrder().size() -1;
    			ONDEXConcept gene = (ONDEXConcept) path.getStartingEntity();
    			ONDEXConcept lastConcept = (ONDEXConcept) path.getConceptsInPositionOrder().get(indexLastCon);
    			if (!regex.isEmpty() && OndexSearch.find(lastConcept, p)) {
    				resultConcepts.addAll(path.getAllConcepts());
    				resultRelations.addAll(path.getAllRelations());
    				conSizes.put(lastConcept, annotateSize);
    				candidateGenes.add(gene);
					keywordConcepts.add(lastConcept);
					if (!conSizes.containsKey(gene)) {
						// initial size
						conSizes.put(gene, Config.defaultNodeSize + 2);
					} else {
						// linear increase of size for each supporting evidence path
						int size = conSizes.get(gene);
						size = size + 2;
						conSizes.put(gene, size);
					}
					
    				Set<ONDEXRelation> rels = path.getAllRelations();
					for (ONDEXRelation r : rels) {
						
						if (!relSizes.containsKey(r)) {
							// initial size
							relSizes.put(r, Config.defaultEdgeSize + 3);
						} else {
							// increase size for more supporting evidence
							int size = relSizes.get(r);
							size = size + 0;
							relSizes.put(r, size);
						}
					}
    			}	
    		}
    	}
    	
  	
		
		// copy before use in transformer
		rels = new HashSet<ONDEXRelation>(relSizes.keySet());


		System.out.println("Keyword(s) were found in " + keywordConcepts.size() + " concepts.");
		System.out.println("Numer of candidate genes " + candidateGenes.size());

		// update graphs node shapes
		ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
		nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				return conSizes.get(input);
			}
		});

		// set amplification of new edge stroke size
		ONDEXEdgeStrokes edgeStrokes = viewer.getEdgeStrokes();
		edgeStrokes.setEdgeSizes(new Transformer<ONDEXRelation, Integer>() {
			@Override
			public Integer transform(ONDEXRelation input) {
				return relSizes.get(input);
			}
		});

		viewer.updateViewer(null);
		
		// set first entire graph to invisible
		setGraphVisible(false);

		// set only graph returned by filter to visible
		setGraphVisible(resultConcepts, resultRelations);

		// new Attribute layout
		GDSPositionLayout layout = new GDSPositionLayout(viewer);

		// propagate change to viewer
		VisualizationModel<ONDEXConcept, ONDEXRelation> model = viewer
				.getVisualizationViewer().getModel();
		model.setGraphLayout(layout);
		model.fireStateChanged();
		viewer.center();
    	
    	
		
	}


	/**
	 * Constructs filter arguments from a table row.
	 * 
	 * @param chromCon
	 *            Category, e.g. chromosome
	 * @param beg
	 *            Begin of positions
	 * @param end
	 *            End of positions
	 * @param keyword 
	 */
	private void composeArguments(Integer chromCon, Integer beg, Integer end,
			String algorithm, String keyword) {
		
		Integer annotateSize = 100;
		
		Pattern p = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);

		
		AttributeName attStart = graph.getMetaData().getAttributeName("BEGIN");
		
		// TODO: smarter behaviour for strange user input
		if (beg > end) {
			Integer tmp = beg;
			beg = end;
			end = tmp;
		}

		// get concept of the context
		ONDEXConcept chrom = graph.getConcept(chromCon);
        Set<ONDEXConcept> conceptsOfContext = graph.getConceptsOfTag(chrom);
        

        //check if concepts within the min/max region
        int geneCount = 0;
        Set<ONDEXConcept> seed = new HashSet<ONDEXConcept>();
        for(ONDEXConcept con : conceptsOfContext){
            Integer start = (Integer) con.getAttribute(attStart).getValue();

            if (start != null && start >= beg && start <= end) {
                seed.add(con);
                geneCount++;
            }
        }
        
    	StateMachineFlatFileParser2 smp = new StateMachineFlatFileParser2();
    	try {
    		smp.parseFile(new File(filePathField.getText()), graph);
    	} catch (InvalidFileException e) {
    		e.printStackTrace();
    	} catch (StateMachineInvalidException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	StateMachine sm = smp.getStateMachine();


    	//then we create a crawler using our rules
    	GraphTraverser gt =  new GraphTraverser(sm);

    	//the results give us a map of every starting concept to every valid path
    	Map<ONDEXConcept,List<EvidencePathNode>> results = gt.traverseGraph(graph, seed, null);
    	Set<ONDEXConcept> concepts = new HashSet<ONDEXConcept>();
    	Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>();
    	Set<ONDEXConcept> keywordConcepts = new HashSet<ONDEXConcept>();
    	Set<ONDEXConcept> candidateGenes = new HashSet<ONDEXConcept>();

    	
    	for(List<EvidencePathNode> paths : results.values()){
    		for(EvidencePathNode  path : paths){
    			
    			concepts.addAll(path.getAllConcepts());
    			relations.addAll(path.getAllRelations());
    			//search last concept of semantic motif for keyword
    			int indexLastCon = path.getConceptsInPositionOrder().size() -1;
    			ONDEXConcept gene = (ONDEXConcept) path.getStartingEntity();
    			ONDEXConcept lastConcept = (ONDEXConcept) path.getConceptsInPositionOrder().get(indexLastCon);
    			if (!keyword.isEmpty() && OndexSearch.find(lastConcept, p)) {
    				conSizes.put(lastConcept, annotateSize);
    				candidateGenes.add(gene);
					keywordConcepts.add(lastConcept);
					if (!conSizes.containsKey(gene)) {
						// initial size
						conSizes.put(gene, Config.defaultNodeSize + 1);
					} else {
						// linear increase of size for each supporting evidence path
						int size = conSizes.get(gene);
						size = size + 1;
						conSizes.put(gene, size);
					}
					
    				Set<ONDEXRelation> rels = path.getAllRelations();
					for (ONDEXRelation r : rels) {
						
						if (!relSizes.containsKey(r)) {
							// initial size
							relSizes.put(r, Config.defaultEdgeSize + 7);
						} else {
							// increase size for more supporting evidence
							int size = relSizes.get(r);
							size = size + 0;
							relSizes.put(r, size);
						}
					}
    			}	
    		}
    	}
    	
    	resultConcepts.addAll(concepts);
    	resultRelations.addAll(relations);
    	
    	System.out.println(algorithm+" found " + geneCount + " genes. Neighbourhood has " + concepts.size() + " concepts; " + relations.size() + " relations.");
		
		// copy before use in transformer
		rels = new HashSet<ONDEXRelation>(relSizes.keySet());


		System.out.println("Keyword(s) were found in " + keywordConcepts.size() + " concepts.");
		System.out.println("Numer of candidate genes " + candidateGenes.size());

		// update graphs node shapes
		ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
		nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
			@Override
			public Integer transform(ONDEXConcept input) {
				return conSizes.get(input);
			}
		});

		// set amplification of new edge stroke size
		ONDEXEdgeStrokes edgeStrokes = viewer.getEdgeStrokes();
		edgeStrokes.setEdgeSizes(new Transformer<ONDEXRelation, Integer>() {
			@Override
			public Integer transform(ONDEXRelation input) {
				return relSizes.get(input);
			}
		});

		viewer.updateViewer(null);

	}

	/**
	 * Populate default table with row specific QTL cell editor.
	 */
	private void defaultTable() {

		// static content of header
		String[] columnNames = { HEADER_USE, HEADER_CHROMOSOME, HEADER_BEGIN,
				HEADER_END, HEADER_QTL };

		int limit = 5;
		if (chromosomeBox.getItemCount() < 5) {
			limit = chromosomeBox.getItemCount();
		}

		// default data, every possible chromosome one row
		Object[][] data = new Object[limit][];

		for (int i = 0; i < limit; i++) {
			Object[] row = new Object[5];
			IntegerStringWrapper ch = (IntegerStringWrapper) chromosomeBox
					.getItemAt(i);
			row[0] = new Boolean(true);
			row[1] = ch;
			row[2] = new Integer(1);
			row[3] = new Integer(100000);

			JComboBox qtlJBox = chromosome2qtl.get(ch.getValue());
			if (qtlJBox != null)
				row[4] = qtlJBox.getItemAt(0);
			else
				row[4] = "NA";
			DefaultCellEditor qtlEditor = new DefaultCellEditor(qtlJBox);
			rm.addEditorForRow(i, qtlEditor);
			data[i] = row;
		}

		setupTable(data, columnNames, rm);
	}

	/**
	 * create a name for concepts
	 * 
	 * @param c
	 *            ONDEXConcept
	 * @return normalised name
	 */
	private String getDefaultNameForConcept(ONDEXConcept c) {
		String name = null;
		ConceptName cn = c.getConceptName();

		Set<ConceptAccession> accs = c.getConceptAccessions();

		if (cn != null && cn.getName().trim().length() > 0)
			name = cn.getName().trim();
		else if (accs.size() > 0)
			for (ConceptAccession acc : accs) {
				if (acc.getAccession().trim().length() > 0) {
					if (acc.getElementOf().equals(c.getElementOf())) {
						// prefer native accession
						name = acc.getAccession().trim();
						break;
					}
					name = acc.getAccession().trim();
				}

			}
		else if (c.getAnnotation().length() > 0)
			name = c.getAnnotation().trim();
		else if (c.getDescription().length() > 0)
			name = c.getDescription().trim();
		else
			name = "n/a";

		if (name.length() > 25) {
			// then trim it to a reasonable size
			name = name.substring(0, 22) + "...";
		}
		return name;
	}

	@Override
	public String getName() {
		return "Genomics Filter";
	}

	/**
	 * Queries graph selection and populates table with it.
	 */
	private void getSelection() {

		// genes sorted by chromosome
		Map<IntegerStringWrapper, List<ONDEXConcept>> map = new Hashtable<IntegerStringWrapper, List<ONDEXConcept>>();

		// check for pick status
		Set<ONDEXConcept> picked = new HashSet<ONDEXConcept>();
		for (ONDEXConcept node : viewer.getPickedNodes()) {
			picked.add(node);
		}

		int count = 0;
		for (ONDEXConcept context : graph.getAllTags()) {
			// check concept class of context
			if (context.getOfType().getId().equalsIgnoreCase("Chromosome")) {
				List<ONDEXConcept> list = new ArrayList<ONDEXConcept>();
				for (ONDEXConcept concept : graph.getConceptsOfTag(context)) {
					// only look for genes and QTLs
					if ((concept.getOfType().getId().equals("Gene") || concept
							.getOfType().getId().equals("QTL"))
							&& picked.contains(concept)) {
						list.add(concept);
						count++;
					}
				}
				map.put(new IntegerStringWrapper(context.getId(),
						getDefaultNameForConcept(context)), list);
			}
		}

		// static content of header
		String[] columnNames = { HEADER_USE, HEADER_CHROMOSOME, HEADER_BEGIN,
				HEADER_END, HEADER_QTL };

		// default data, every possible chromosome one row
		Object[][] data = new Object[count][];

		// first sort by chromosome
		IntegerStringWrapper[] keys = map.keySet().toArray(
				new IntegerStringWrapper[0]);
		Arrays.sort(keys);
		int i = 0;
		for (IntegerStringWrapper key : keys) {
			// then sort by begin Attribute
			ONDEXConcept[] genes = map.get(key).toArray(new ONDEXConcept[0]);
			Arrays.sort(genes, new GDSComparator(anBegin));
			for (ONDEXConcept c : genes) {
				Object[] row = new Object[5];
				row[0] = new Boolean(true);
				row[1] = key;
				row[2] = c.getAttribute(anBegin).getValue();
				row[3] = c.getAttribute(anEnd).getValue();

				ONDEXConcept qtl = graph.getConcept(key.getValue());
				int chromNum = (Integer) qtl.getAttribute(anChromosome)
						.getValue();
				JComboBox qtlJBox = createQTLComboBox(chromNum);
				DefaultCellEditor qtlEditor = new DefaultCellEditor(qtlJBox);
				rm.addEditorForRow(i, qtlEditor);
				// TODO select the correct QTL from list, and not just the first
				// one
				row[4] = qtlJBox.getItemAt(0);

				data[i] = row;
				i++;
			}
		}

		setupTable(data, columnNames, rm);
	}

	/**
	 * Inserts a default row at the end of table.
	 */
	private void insertRow() {
		DefaultTableModel model = (DefaultTableModel) searchTable.getModel();
		Object[] row = new Object[5];
		row[0] = new Boolean(true);
		row[1] = chromosomeBox.getItemAt(0);
		row[2] = new Integer(1);
		row[3] = new Integer(100000);

		JComboBox qtlJBox = chromosome2qtl
				.get(((IntegerStringWrapper) chromosomeBox.getItemAt(0))
						.getValue());
		DefaultCellEditor qtlEditor = new DefaultCellEditor(qtlJBox);
		rm.addEditorForRow(model.getRowCount(), qtlEditor);
		row[4] = qtlJBox.getItemAt(0);
		model.insertRow(model.getRowCount(), row);
	}

	@SuppressWarnings("unchecked")
	private void performSearch() {

		// require here for deterministic behaviour
		toggleRelations = true;

		// get selected algorithm
		String algorithm = buttonGroup.getSelection().getActionCommand();
		
		// get search keyword
		String keyword = textFieldSearch.getText();

		// empty views to begin with
		resultConcepts = new HashSet<ONDEXConcept>();
		resultRelations = new HashSet<ONDEXRelation>();
		
		conSizes = LazyMap.decorate(
				new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultNodeSize;
					}
				});
		relSizes = LazyMap.decorate(
				new HashMap<ONDEXRelation, Integer>(), new Factory<Integer>() {
					@Override
					public Integer create() {
						return Config.defaultEdgeSize;
					}
				});
		
		// reset node and edge sizes
		viewer.getNodeShapes().setNodeSizes(
				new Transformer<ONDEXConcept, Integer>() {
					@Override
					public Integer transform(ONDEXConcept input) {
						return Config.defaultNodeSize;
					}
				});
		viewer.getEdgeStrokes().setEdgeSizes(
				new Transformer<ONDEXRelation, Integer>() {
					@Override
					public Integer transform(ONDEXRelation input) {
						return Config.defaultEdgeSize;
					}
				});

		// browse table data
		DefaultTableModel tableModel = (DefaultTableModel) searchTable
				.getModel();
		Vector<Vector<Object>> data = tableModel.getDataVector();
		for (Vector<Object> row : data) {
			Boolean use = (Boolean) row.get(0);
			IntegerStringWrapper chr = (IntegerStringWrapper) row.get(1);
			Integer begin = (Integer) row.get(2);
			Integer end = (Integer) row.get(3);

			// System.out.println(chr + " " + begin + " " + end + " " + use);
			// compose for each line
			if (use)
				composeArguments(chr.getValue(), begin, end, algorithm, keyword);
		}

		// set first entire graph to invisible
		setGraphVisible(false);

		// set only graph returned by filter to visible
		setGraphVisible(resultConcepts, resultRelations);

		// new Attribute layout
		GDSPositionLayout layout = new GDSPositionLayout(viewer);

		// propagate change to viewer
		VisualizationModel<ONDEXConcept, ONDEXRelation> model = viewer
				.getVisualizationViewer().getModel();
		model.setGraphLayout(layout);
		model.fireStateChanged();
		viewer.center();



	}

	/**
	 * set visibility for entire graph
	 * 
	 * @param isVisible
	 *            if true, entire graph is visible
	 */
	private void setGraphVisible(boolean isVisible) {

		// make all nodes visible
		for (ONDEXConcept ac : graph.getConcepts()) {
			graph.setVisibility(ac, isVisible);
		}

		// make all edges visible
		for (ONDEXRelation ar : graph.getRelations()) {
			graph.setVisibility(ar, isVisible);
		}
	}

	/**
	 * Set visibility of graph using a filter
	 * 
	 * @param viewC
	 *            concepts
	 * @param viewR
	 *            relations
	 */
	private void setGraphVisible(Set<ONDEXConcept> viewC,
			Set<ONDEXRelation> viewR) {

		// show concepts of filter
		for (ONDEXConcept ac : viewC) {
			graph.setVisibility(ac, true);
		}

		// show relations of filter
		if (viewR != null) {
			for (ONDEXRelation ar : viewR) {
				graph.setVisibility(ar, true);
			}
		}
	}

	/**
	 * Setup table for given data.
	 * 
	 * @param data
	 *            data to fill table
	 * @param columnNames
	 *            names of all columns
	 */
	private void setupTable(Object[][] data, String[] columnNames,
			RowEditorModel rm) {
		// create a new table with specific properties
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		searchTable = new JTableX(model);
		searchTable.setDefaultEditor(Integer.class, new IntegerEditor(1,
				Integer.MAX_VALUE));

		TableColumn useColumn = searchTable.getColumnModel().getColumn(0);
		useColumn
				.setCellRenderer(searchTable.getDefaultRenderer(Boolean.class));
		useColumn.setCellEditor(searchTable.getDefaultEditor(Boolean.class));

		// set JComboBox renderer
		TableColumn catColumn = searchTable.getColumnModel().getColumn(1);
		catColumn.setCellEditor(new DefaultCellEditor(chromosomeBox));

		// JTable is too stupid to resolve the classes correctly
		TableColumn beginColumn = searchTable.getColumnModel().getColumn(2);
		beginColumn.setCellRenderer(searchTable
				.getDefaultRenderer(Integer.class));
		beginColumn.setCellEditor(searchTable.getDefaultEditor(Integer.class));

		TableColumn endColumn = searchTable.getColumnModel().getColumn(3);
		endColumn
				.setCellRenderer(searchTable.getDefaultRenderer(Integer.class));
		endColumn.setCellEditor(searchTable.getDefaultEditor(Integer.class));

		// set row specific editor for the QTL column
		if (rm != null)
			searchTable.setRowEditorModel(rm);

		// now table editing should be prohibited
		searchTable.setRowSelectionAllowed(false);
		searchTable.setColumnSelectionAllowed(false);
		searchTable.setCellSelectionEnabled(false);

		// perfectly fit the content into the table
		calcColumnWidths(searchTable);
		/*
		 * if content width is greater than the minimal width of the table
		 * disable the auto resizing function of the table and use the scroll
		 * bars of the scroll pane containing this table instead
		 */
		if (searchTable.getPreferredSize().width > 460) {
			searchTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}
	}

	/**
	 * Sorts the context drop down list
	 * 
	 * @param contextBox
	 */
	private void sortContexts(JComboBox contextBox) {
		List<IntegerStringWrapper> contexts = new ArrayList<IntegerStringWrapper>();

		for (int i = 0; i < contextBox.getModel().getSize(); i++) {
			// ignore the none selection
			if (contextBox.getModel().getElementAt(i) instanceof IntegerStringWrapper) {
				IntegerStringWrapper wrapper = (IntegerStringWrapper) contextBox
						.getModel().getElementAt(i);
				contexts.add(wrapper);
			}
		}

		// sort scaffolds by their number (saved as Attribute: Chromosome or
		// Scaffold)
		Collections.sort(contexts, new Comparator<IntegerStringWrapper>() {

			@Override
			public int compare(IntegerStringWrapper o1, IntegerStringWrapper o2) {
				Integer c1 = null;
				if (graph.getConcept(o1.getValue()).getOfType()
						.equals(ccChromosome))
					c1 = (Integer) graph.getConcept(o1.getValue())
							.getAttribute(anChromosome).getValue();
				else
					c1 = (Integer) graph.getConcept(o1.getValue())
							.getAttribute(anScaffold).getValue();
				Integer c2 = null;
				if (graph.getConcept(o2.getValue()).getOfType()
						.equals(ccChromosome))
					c2 = (Integer) graph.getConcept(o2.getValue())
							.getAttribute(anChromosome).getValue();
				else
					c2 = (Integer) graph.getConcept(o2.getValue())
							.getAttribute(anScaffold).getValue();

				return c1.compareTo(c2);
			}

		});

		// remove and re-add
		contextBox.removeAllItems();

		Iterator<IntegerStringWrapper> contextsToAdd = contexts.iterator();
		while (contextsToAdd.hasNext()) {
			IntegerStringWrapper contextWrapped = contextsToAdd.next();
			contextBox.addItem(contextWrapped);
		}
	}

	/**
	 * Switches graph to genomic layout.
	 */
	private void switchToGenomicLayout() {

		// hide all concepts and relations
		setGraphVisible(false);

		// show only genes/QTLs and NOT Chromosome concepts
		Set<ONDEXConcept> chroView = graph
				.getConceptsOfConceptClass(ccChromosome);
		Set<ONDEXConcept> toShow = BitSetFunctions.andNot(
				graph.getConceptsOfAttributeName(anChromosome), chroView);
		setGraphVisible(toShow, null);

		// new Attribute layout
		GDSPositionLayout layout = new GDSPositionLayout(viewer);

		// set layout
		VisualizationModel<ONDEXConcept, ONDEXRelation> model = viewer
				.getVisualizationViewer().getModel();
		model.setGraphLayout(layout);
		model.fireStateChanged();
		viewer.center();

		// set nice shape for genes and QTLs
		ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
		Iterator<ONDEXConcept> nodesIt = viewer.getONDEXJUNGGraph()
				.getVertices().iterator();
		while (nodesIt.hasNext()) {
			ONDEXConcept node = nodesIt.next();
			ONDEXConcept concept = node;
			if (concept.getOfType().getId().equals("Gene")) {
				nodeShapes.updateShape(node, new Rectangle(0, 0, 10, 2));
			} else {
				// shift QTLs to left
				nodeShapes.updateShape(node, new Rectangle(-10, 0, 3, 40));
			}
		}
	}

	@Override
	public boolean hasBeenUsed() {
		//TODO: Return true when filter has really been used.
		return true;
	}
}