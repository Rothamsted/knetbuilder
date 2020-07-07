package net.sourceforge.ondex.ovtk2.ui.gds;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;

/**
 * Editor for java.lang.Integer value for Attribute.
 * 
 * @author taubertj
 */
public class IntegerEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = 8061334962472136671L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public IntegerEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
	}

	/**
	 * Represents one single table cell for a Attribute.
	 * 
	 * @author taubertj
	 */
	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = 8128041526332734412L;

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
			return "Integer";
		}

		public Object getValueAt(int row, int col) {
			return attribute.getValue();
		}

		public Class<?> getColumnClass(int c) {
			return Integer.class;
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
		return new Integer(0);
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}
}
