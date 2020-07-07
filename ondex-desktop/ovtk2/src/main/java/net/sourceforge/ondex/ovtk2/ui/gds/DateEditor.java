package net.sourceforge.ondex.ovtk2.ui.gds;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;

/**
 * Editor for java.util.Date value for Attribute.
 * 
 * @author taubertj
 */
public class DateEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = -8820532662455103137L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public DateEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
	}

	/**
	 * Represents one single table cell for a Attribute.
	 * 
	 * @author taubertj
	 */
	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -2889609131590515404L;

		private Attribute attribute = null;

		/**
		 * Constructor sets Attribute.
		 * 
		 * @param attribute
		 *            Attribute
		 */
		public GDSTableModel(Attribute attribute) {
			this.attribute = attribute;
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return 1;
		}

		public String getColumnName(int col) {
			return "Date";
		}

		public Object getValueAt(int row, int col) {
			return attribute.getValue();
		}

		public Class<?> getColumnClass(int c) {
			return Date.class;
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			attribute.setValue(value);
			fireTableCellUpdated(row, col);
		}
	}

	public Object getDefaultValue() {
		return new Date();
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}
}
