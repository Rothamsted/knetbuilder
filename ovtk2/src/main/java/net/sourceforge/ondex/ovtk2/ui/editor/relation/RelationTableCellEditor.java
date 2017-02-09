package net.sourceforge.ondex.ovtk2.ui.editor.relation;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.ui.editor.GraphTableEditor;

/**
 * Editor for properties of a relation, uses mainly drop-down boxes.
 * 
 * @author taubertj
 * 
 */
public class RelationTableCellEditor extends DefaultCellEditor {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7503539291654616708L;

	/**
	 * The editable fields of a relation.
	 * 
	 * @author taubertj
	 * 
	 */
	public enum STATE {
		FROM, TO, QUALIFIER, OFTYPE, EVIDENCE
	};

	/**
	 * Current row to modify
	 */
	int row = -1;

	/**
	 * The relation to which to modify
	 */
	ONDEXRelation relation = null;

	/**
	 * Current ONDEX graph, static for access during combo box update
	 */
	private static ONDEXGraph graph = null;

	/**
	 * what state this editor is in
	 */
	private STATE state = null;

	/**
	 * Clones relations and their attributes
	 */
	private RelationCloner cloner = null;

	/**
	 * Contains list of available concepts.
	 */
	private static JComboBox fromComboBox = null;

	/**
	 * Contains list of available concepts.
	 */
	private static JComboBox toComboBox = null;

	/**
	 * Contains list of available concepts.
	 */
	private static JComboBox qualComboBox = null;

	/**
	 * Contains list of available RTs.
	 */
	JComboBox rtComboBox = null;

	/**
	 * Contains list of available ETs.
	 */
	JComboBox etComboBox = null;

	/**
	 * Fakes a table cell editor based around a JTextField.
	 * 
	 */
	public RelationTableCellEditor(ONDEXGraph g) {
		super(new JTextField());
		// keep local graph reference
		graph = g;
		cloner = new RelationCloner(graph);
		updateConceptLists();
		rtComboBox = new JComboBox(graph.getMetaData().getRelationTypes().toArray(new RelationType[0]));
		etComboBox = new JComboBox(graph.getMetaData().getEvidenceTypes().toArray(new EvidenceType[0]));
	}

	/**
	 * Triggers update of concept drop-down boxes.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 */
	public static void updateConceptLists() {
		fromComboBox = new JComboBox(graph.getConcepts().toArray(new ONDEXConcept[0]));
		toComboBox = new JComboBox(graph.getConcepts().toArray(new ONDEXConcept[0]));
		qualComboBox = new JComboBox(graph.getConcepts().toArray(new ONDEXConcept[0]));
	}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		// setup current row for update purpose
		this.row = row;

		// which relation we are working on
		relation = (ONDEXRelation) table.getValueAt(row, 0);

		// decide which field to edit
		String name = table.getColumnName(column);
		if (name.equals(RelationPanel.header[1])) {
			state = STATE.FROM;
			return fromComboBox;
		} else if (name.equals(RelationPanel.header[2])) {
			state = STATE.TO;
			return toComboBox;
		} else if (name.equals(RelationPanel.header[3])) {
			state = STATE.QUALIFIER;
			return qualComboBox;
		} else if (name.equals(RelationPanel.header[4])) {
			state = STATE.OFTYPE;
			return rtComboBox;
		} else if (name.equals(RelationPanel.header[5])) {
			state = STATE.EVIDENCE;
			return etComboBox;
		} else {
			// should not happend, but just in case
			state = null;
		}

		// use default text field editor
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {

		// update all other linked tables
		ONDEXRelation newRelation = null;

		// get editor value as text if correct state
		if (state != null) {

			switch (state) {

			case FROM:
				// new from concept, update table
				ONDEXConcept newFrom = (ONDEXConcept) fromComboBox.getSelectedItem();
				// only update if changed
				if (!relation.getFromConcept().equals(newFrom)) {
					newRelation = cloner.clone(relation, newFrom, state);
					updateRelationInGraph(newRelation);
				}
				return newFrom;

			case TO:
				// new to concept, update table
				ONDEXConcept newTo = (ONDEXConcept) toComboBox.getSelectedItem();
				// only update if changed
				if (!relation.getToConcept().equals(newTo)) {
					newRelation = cloner.clone(relation, newTo, state);
					updateRelationInGraph(newRelation);
				}
				return newTo;

			case OFTYPE:
				// new relation RelationType, update table
				RelationType newRT = (RelationType) rtComboBox.getSelectedItem();
				// only update if changed
				if (!relation.getOfType().equals(newRT)) {
					newRelation = cloner.clone(relation, newRT);
					updateRelationInGraph(newRelation);
				}
				return newRT;

			case EVIDENCE:
				// change in evidence type, update table
				EvidenceType newET = (EvidenceType) etComboBox.getSelectedItem();
				if (!relation.getEvidence().contains(newET)) {
					// new ET selected, add it to relation
					relation.addEvidenceType(newET);
				} else {
					// existing ET selected, remove it from relation
					// keep at least one evidence type in list
					if (relation.getEvidence().size() > 1)
						relation.removeEvidenceType(newET);
				}
				return relation.getEvidence();
			default:
				System.out.println("unknown state for updating cell editor value");
				break;
			}
		}

		// return default editor value
		return super.getCellEditorValue();
	}

	/**
	 * Updates the graph with the new concept and all tables with it.
	 * 
	 * @param newRelation
	 *            ONDEXConcept newly created
	 */
	private void updateRelationInGraph(ONDEXRelation newRelation) {
		// update all linked tables
		GraphTableEditor.updateRelationInTables(newRelation, row);

		// delete old relation from graph
		graph.deleteConcept(relation.getId());
	}

}
