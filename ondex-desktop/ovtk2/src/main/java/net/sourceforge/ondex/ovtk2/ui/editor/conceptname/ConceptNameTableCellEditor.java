package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Using a text field to edit the name of a concept name.
 * 
 * @author taubertj
 * 
 */
public class ConceptNameTableCellEditor extends DefaultCellEditor implements MouseListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7503539291654626708L;

	/**
	 * cache for comparison of values
	 */
	ConceptName name = null;

	/**
	 * The concept to which the name belongs to
	 */
	ONDEXConcept concept = null;

	/**
	 * Fakes a table cell editor based around a JTextField.
	 * 
	 */
	public ConceptNameTableCellEditor() {
		super(new JTextField());
		// listen for right clicks
		super.getComponent().addMouseListener(this);
	}

	// Implement the one method defined by TableCellEditor.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		// which concept we are working on
		concept = (ONDEXConcept) table.getValueAt(row, 0);

		// check for concept name values
		if (value instanceof ConceptName) {
			name = (ConceptName) value;
			value = name.getName();
		} else {
			name = null;
		}

		// use default text field editor
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {

		// this is value returned by default editor
		String newName = super.getCellEditorValue().toString();

		if (name == null) {
			// construct new CN with defaults
			if (newName.trim().length() > 0)
				name = concept.createConceptName(newName, false);
		} else {
			// existing CN check name update
			if (!newName.equals(name.getName())) {
				// delete old one
				ConceptName old = concept.getConceptName(name.getName());
				concept.deleteConceptName(name.getName());
				// keep preferred setting and create non empty name
				if (newName.trim().length() > 0)
					name = concept.createConceptName(newName, old.isPreferred());
				else
					// clear existing concept name from table
					name = null;
			}
		}

		// return concept name instead of string
		return name;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// right mouse click
		if (e.getButton() == MouseEvent.BUTTON3 && name != null) {
			// additional information / editing popup
			ConceptNamePopup popup = new ConceptNamePopup(name);
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
