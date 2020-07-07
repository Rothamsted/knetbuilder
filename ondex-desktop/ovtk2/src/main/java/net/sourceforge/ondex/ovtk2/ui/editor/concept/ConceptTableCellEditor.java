package net.sourceforge.ondex.ovtk2.ui.editor.concept;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.GraphTableEditor;

/**
 * Editor for properties of a concept, uses text fields and drop-down boxes.
 * 
 * @author taubertj
 * 
 */
public class ConceptTableCellEditor extends DefaultCellEditor {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -3359296179577218746L;

	/**
	 * The editable fields of a concept.
	 * 
	 * @author taubertj
	 * 
	 */
	private enum STATE {
		PID, DESC, ANNO, ELEMENTOF, OFTYPE, EVIDENCE
	};

	/**
	 * Current row to modify
	 */
	int row = -1;

	/**
	 * The concept to modify
	 */
	ONDEXConcept concept = null;

	/**
	 * Current ONDEX graph
	 */
	ONDEXGraph graph = null;

	/**
	 * what state this editor is in
	 */
	private STATE state = null;

	/**
	 * Clones concepts and their attributes
	 */
	private ConceptCloner cloner = null;

	/**
	 * Contains list of available CVs.
	 */
	JComboBox cvComboBox = null;

	/**
	 * Contains list of available CCs.
	 */
	JComboBox ccComboBox = null;

	/**
	 * Contains list of available ETs.
	 */
	JComboBox etComboBox = null;

	/**
	 * Fakes a table cell editor based around a JTextField.
	 * 
	 */
	public ConceptTableCellEditor(ONDEXGraph graph) {
		super(new JTextField());
		// keep local graph reference
		this.graph = graph;
		cloner = new ConceptCloner(graph);
		cvComboBox = new JComboBox(graph.getMetaData().getDataSources().toArray(new DataSource[graph.getMetaData().getDataSources().size()]));
		ccComboBox = new JComboBox(graph.getMetaData().getConceptClasses().toArray(new ConceptClass[graph.getMetaData().getConceptClasses().size()]));
		etComboBox = new JComboBox(graph.getMetaData().getEvidenceTypes().toArray(new EvidenceType[graph.getMetaData().getEvidenceTypes().size()]));
	}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		// setup current row for update purpose
		this.row = row;

		// which concept we are working on
		concept = (ONDEXConcept) table.getValueAt(row, 0);

		// decide which field to edit
		String name = table.getColumnName(column);
		if (name.equals(ConceptPanel.header[1])) {
			state = STATE.PID;
		} else if (name.equals(ConceptPanel.header[2])) {
			state = STATE.ANNO;
		} else if (name.equals(ConceptPanel.header[3])) {
			state = STATE.DESC;
		} else if (name.equals(ConceptPanel.header[4])) {
			state = STATE.ELEMENTOF;
			return cvComboBox;
		} else if (name.equals(ConceptPanel.header[5])) {
			state = STATE.OFTYPE;
			return ccComboBox;
		} else if (name.equals(ConceptPanel.header[6])) {
			state = STATE.EVIDENCE;
			return etComboBox;
		} else {
			// should not happen, but just in case
			state = null;
		}

		// use default text field editor
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {

		// update all other linked tables
		ONDEXConcept newConcept = null;

		// get editor value as text if correct state
		if (state != null) {

			switch (state) {

			case PID:
				// new concept PID, update table
				String text = super.getCellEditorValue().toString();
				// only update if changed
				if (concept.getPID() == null || !concept.getPID().equals(text)) {
					newConcept = cloner.clone(concept, text);
					updateConceptInGraph(newConcept);
				}
				return text;

			case ANNO:
				// new concept annotation
				text = super.getCellEditorValue().toString();
				concept.setAnnotation(text);
				return text;

			case DESC:
				// new concept description
				text = super.getCellEditorValue().toString();
				concept.setDescription(text);
				return text;

			case ELEMENTOF:
				// new concept DataSource, update table
				DataSource newDataSource = (DataSource) cvComboBox.getSelectedItem();
				// only update if changed
				if (!concept.getElementOf().equals(newDataSource)) {
					newConcept = cloner.clone(concept, newDataSource);
					updateConceptInGraph(newConcept);
				}
				return newDataSource;

			case OFTYPE:
				// new concept ConceptClass, update table
				ConceptClass newCC = (ConceptClass) ccComboBox.getSelectedItem();
				// only update if changed
				if (!concept.getOfType().equals(newCC)) {
					newConcept = cloner.clone(concept, newCC);
					updateConceptInGraph(newConcept);
				}
				return newCC;

			case EVIDENCE:
				// change in evidence type, update table
				EvidenceType newET = (EvidenceType) etComboBox.getSelectedItem();
				if (!concept.getEvidence().contains(newET)) {
					// new ET selected, add it to concept
					concept.addEvidenceType(newET);
				} else {
					// existing ET selected, remove it from concept
					// keep at least one evidence type in list
					if (concept.getEvidence().size() > 1)
						concept.removeEvidenceType(newET);
				}
				return concept.getEvidence();
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
	 * @param newConcept
	 *            ONDEXConcept newly created
	 */
	private void updateConceptInGraph(ONDEXConcept newConcept) {
		// update all linked tables
		GraphTableEditor.updateConceptInTables(newConcept, row);

		// delete old concept from graph, also deletes relations etc
		graph.deleteConcept(concept.getId());
	}

}
