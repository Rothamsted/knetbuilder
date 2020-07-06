package net.sourceforge.ondex.ovtk2.ui.gds;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;

/**
 * Editor for java.lang.Boolean value for Attribute.
 * 
 * @author taubertj
 */
public class BooleanEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = -425601887057946193L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public BooleanEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
	}

	/**
	 * Represents one single table cell for a Attribute.
	 * 
	 * @author taubertj
	 */
	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -6987488951854664457L;

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
			return "Boolean";
		}

		public Object getValueAt(int row, int col) {
			return attribute.getValue();
		}

		public Class<?> getColumnClass(int c) {
			return Boolean.class;
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
		return Boolean.FALSE;
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}
}
