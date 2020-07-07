package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * Using a text field to edit the accession of a concept accession.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionTableCellEditor extends DefaultCellEditor implements MouseListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7503539291654626709L;

	/**
	 * cache for comparison of values
	 */
	ConceptAccession acc = null;

	/**
	 * The concept to which the accession belongs to
	 */
	ONDEXConcept concept = null;

	/**
	 * Current ONDEX graph
	 */
	ONDEXGraph graph = null;

	/**
	 * Default DataSource is set to unknown.
	 */
	DataSource unknown = null;

	/**
	 * Fakes a table cell editor based around a JTextField.
	 * 
	 */
	public ConceptAccessionTableCellEditor(ONDEXGraph graph) {
		super(new JTextField());
		// listen for right clicks
		super.getComponent().addMouseListener(this);
		// keep local graph reference
		this.graph = graph;
		unknown = graph.getMetaData().getDataSource("unknown");
		// if not yet present in meta data
		if (unknown == null)
			unknown = graph.getMetaData().createDataSource("unknown", "unknown datasource", "automatically created");
	}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		// which concept we are working on
		concept = (ONDEXConcept) table.getValueAt(row, 0);

		// check for concept accession values
		if (value instanceof ConceptAccession) {
			acc = (ConceptAccession) value;
			value = acc.getAccession();
		} else {
			acc = null;
		}

		// use default text field editor
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {

		// this is value returned by default editor
		String newAccession = super.getCellEditorValue().toString();

		if (acc == null) {
			// construct new CA with defaults
			if (newAccession.trim().length() > 0)
				acc = concept.createConceptAccession(newAccession, unknown, true);
		} else {
			// existing CA check accession update
			if (!newAccession.equals(acc.getAccession())) {
				// delete old one
				ConceptAccession old = concept.getConceptAccession(acc.getAccession(), acc.getElementOf());
				concept.deleteConceptAccession(acc.getAccession(), acc.getElementOf());
				// keep additional settings and create non empty accession
				if (newAccession.trim().length() > 0)
					acc = concept.createConceptAccession(newAccession, old.getElementOf(), old.isAmbiguous());
				else
					// clear existing concept accession from table
					acc = null;
			}
		}

		// return concept accession instead of string
		return acc;
	}

	/**
	 * Sets a changed concept accession
	 * 
	 * @param acc
	 *            ConceptAccession
	 */
	public void setConceptAccession(ConceptAccession acc) {
		this.acc = acc;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// right mouse click
		if (e.getButton() == MouseEvent.BUTTON3 && acc != null) {
			// additional information / editing popup
			ConceptAccessionPopup popup = new ConceptAccessionPopup(this);
			popup.setLocation(e.getLocationOnScreen());
			popup.setVisible(true);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
