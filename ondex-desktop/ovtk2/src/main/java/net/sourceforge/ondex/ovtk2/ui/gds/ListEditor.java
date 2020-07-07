package net.sourceforge.ondex.ovtk2.ui.gds;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;

/**
 * Editor shows the XML serialisation of the Attribute.
 * 
 * @author taubertj
 */
public class ListEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = -3735335648304140264L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public ListEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
	}

	/**
	 * Represents one single table cell for a Attribute.
	 * 
	 * @author taubertj
	 */
	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -3016595063340755087L;

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
			return "List";
		}

		public Object getValueAt(int row, int col) {
			return attribute.getValue();
		}

		public Class<?> getColumnClass(int c) {
			return ArrayList.class;
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
		return new ArrayList<String>(0);
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}

}
