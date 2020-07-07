package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Copied from JAVA tutorial.
 * 
 * @author taubertj
 * 
 */
public class ColorTableEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	// generated
	private static final long serialVersionUID = -5544221335269114637L;

	Color currentColor;

	JButton button;

	JColorChooser colorChooser;

	JDialog dialog;

	protected static final String EDIT = "edit";

	public ColorTableEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

		// Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
				colorChooser, this, // OK button handler
				null); // no CANCEL button handler
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			// The user has clicked the cell, so
			// bring up the dialog.
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);

			fireEditingStopped(); // Make the renderer reappear.

		} else { // User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		currentColor = (Color) value;
		return button;
	}
}