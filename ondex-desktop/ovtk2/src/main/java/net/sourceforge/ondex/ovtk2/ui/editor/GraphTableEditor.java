package net.sourceforge.ondex.ovtk2.ui.editor;

import java.awt.GridLayout;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.editor.concept.ConceptPanel;
import net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession.ConceptAccessionPanel;
import net.sourceforge.ondex.ovtk2.ui.editor.conceptgds.ConceptGDSPanel;
import net.sourceforge.ondex.ovtk2.ui.editor.conceptname.ConceptNamePanel;
import net.sourceforge.ondex.ovtk2.ui.editor.relation.RelationPanel;
import net.sourceforge.ondex.ovtk2.ui.editor.relation.RelationTableCellEditor;
import net.sourceforge.ondex.ovtk2.ui.editor.relationgds.RelationGDSPanel;

/**
 * Presents a table view of the contents of a graph. Can modify the graph
 * contents.
 * 
 * @author taubertj
 * 
 */
public class GraphTableEditor extends RegisteredJInternalFrame implements ChangeListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 6130945542374431988L;

	/**
	 * Kinds of different tables presenting different information. Used for
	 * central register as keys.
	 * 
	 * @author taubertj
	 * 
	 */
	public enum CATEGORY {
		CONCEPT, CONCEPT_NAME, CONCEPT_ACCESSION, CONCEPT_GDS, RELATION, RELATION_GDS
	};

	/**
	 * Keep track of all present table to be updated if concept changes
	 */
	public static Map<CATEGORY, JTable> conceptRegister = new Hashtable<CATEGORY, JTable>();

	/**
	 * Keep track of all present table to be updated if relation changes
	 */
	public static Map<CATEGORY, JTable> relationRegister = new Hashtable<CATEGORY, JTable>();

	/**
	 * Keep track when tab selection changes
	 */
	JTabbedPane pane = null;

	/**
	 * Previous selected tab, default 0
	 */
	CATEGORY previousSelection = CATEGORY.CONCEPT;

	/**
	 * Initialises GUI and triggers tabbed pane construction.
	 * 
	 * @param graph
	 *            ONDEXGraph to work with
	 */
	public GraphTableEditor(ONDEXGraph graph) {
		super(graph.getName(), "Editor", "ONDEX Graph Editor", true, true, true, true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// default size
		this.setSize(800, 600);

		// only one root component
		this.setLayout(new GridLayout(1, 1));

		// everything goes into the tabbed pane
		pane = new JTabbedPane();
		populatePane(graph, pane);
		this.add(pane);

		// listen to changes in tabs for updating selection
		pane.addChangeListener(this);
	}

	/**
	 * Populates tabbed pane with tabs.
	 * 
	 * @param pane
	 *            JTabbedPane to populate
	 */
	private void populatePane(ONDEXGraph graph, JTabbedPane pane) {

		ConceptPanel conceptPanel = new ConceptPanel(graph);
		conceptPanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Concepts", conceptPanel);
		conceptRegister.put(CATEGORY.CONCEPT, conceptPanel.getTable());

		ConceptNamePanel conceptNamePanel = new ConceptNamePanel(graph);
		conceptNamePanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Concept Names", conceptNamePanel);
		conceptRegister.put(CATEGORY.CONCEPT_NAME, conceptNamePanel.getTable());

		ConceptAccessionPanel conceptAccessionPanel = new ConceptAccessionPanel(graph);
		conceptAccessionPanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Concept Accessions", conceptAccessionPanel);
		conceptRegister.put(CATEGORY.CONCEPT_ACCESSION, conceptAccessionPanel.getTable());

		ConceptGDSPanel conceptGDSPanel = new ConceptGDSPanel(graph);
		conceptGDSPanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Concept Attribute", conceptGDSPanel);
		conceptRegister.put(CATEGORY.CONCEPT_GDS, conceptGDSPanel.getTable());

		RelationPanel relationPanel = new RelationPanel(graph);
		relationPanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Relations", relationPanel);
		relationRegister.put(CATEGORY.RELATION, relationPanel.getTable());

		RelationGDSPanel relatioGDSPanel = new RelationGDSPanel(graph);
		relatioGDSPanel.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pane.addTab("Relation Attribute", relatioGDSPanel);
		relationRegister.put(CATEGORY.RELATION_GDS, relatioGDSPanel.getTable());
	}

	/**
	 * Updates first column of all tables with modified concept.
	 * 
	 * @param newC
	 *            updated ONDEXConcept
	 * @param row
	 *            number of row
	 */
	public static void updateConceptInTables(ONDEXConcept newC, int row) {
		for (JTable table : conceptRegister.values()) {
			table.setValueAt(newC, row, 0);
		}
		RelationTableCellEditor.updateConceptLists();
	}

	/**
	 * Updates first column of all tables with modified relation.
	 * 
	 * @param newR
	 *            updated ONDEXRelation
	 * @param row
	 *            number of row
	 */
	public static void updateRelationInTables(ONDEXRelation newR, int row) {
		for (JTable table : relationRegister.values()) {
			table.setValueAt(newR, row, 0);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		// get current table to get selection from
		JTable oldTable = conceptRegister.get(previousSelection);
		if (oldTable == null)
			oldTable = relationRegister.get(previousSelection);

		// update with new selection
		switch (pane.getSelectedIndex()) {
		case 0:
			previousSelection = CATEGORY.CONCEPT;
			break;
		case 1:
			previousSelection = CATEGORY.CONCEPT_NAME;
			break;
		case 2:
			previousSelection = CATEGORY.CONCEPT_ACCESSION;
			break;
		case 3:
			previousSelection = CATEGORY.CONCEPT_GDS;
			break;
		case 4:
			previousSelection = CATEGORY.RELATION;
			break;
		case 5:
			previousSelection = CATEGORY.RELATION_GDS;
			break;
		default:
			previousSelection = CATEGORY.CONCEPT;
			break;
		}

		// new table to set selection to, clear first
		JTable newTable = conceptRegister.get(previousSelection);
		if (newTable == null)
			newTable = relationRegister.get(previousSelection);
		newTable.getSelectionModel().clearSelection();
		int[] selection = oldTable.getSelectedRows();
		for (int i = 0; i < selection.length; i++) {
			// conversion necessary because of possible row sorting differently
			selection[i] = newTable.convertRowIndexToView(oldTable.convertRowIndexToModel(selection[i]));
			newTable.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}
}