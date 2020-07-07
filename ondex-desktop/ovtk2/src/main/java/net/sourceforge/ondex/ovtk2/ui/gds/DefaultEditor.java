package net.sourceforge.ondex.ovtk2.ui.gds;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.marshal.Marshaller;

/**
 * Editor shows the XML serialisation of the Attribute.
 * 
 * @author taubertj
 */
public class DefaultEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = 2903635363941845737L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public DefaultEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
	}

	/**
	 * Represents one single table cell for a Attribute.
	 * 
	 * @author taubertj
	 */
	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = -1101727517552472815L;

		private Attribute attribute = null;

		private String dataType = null;

		/**
		 * Constructor sets Attribute.
		 * 
		 * @param attribute
		 *            Attribute
		 */
		public GDSTableModel(Attribute attribute) {
			if (attribute != null) {
				this.attribute = attribute;
				this.dataType = attribute.getOfType().getDataTypeAsString();
			}
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return 1;
		}

		public String getColumnName(int col) {
			if (dataType != null)
				return dataType;
			return "";
		}

		public Object getValueAt(int row, int col) {
			if (attribute != null)
				return Marshaller.getMarshaller().toXML(attribute.getValue());
			return "";
		}

		public Class<?> getColumnClass(int c) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			if (attribute != null) {
				attribute.setValue(Marshaller.getMarshaller().fromXML((String) value));
				fireTableCellUpdated(row, col);
			}
		}
	}

	public Object getDefaultValue() {
		return null;
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}
}
