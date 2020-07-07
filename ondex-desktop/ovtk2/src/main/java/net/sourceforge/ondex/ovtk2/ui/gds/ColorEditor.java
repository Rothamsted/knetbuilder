package net.sourceforge.ondex.ovtk2.ui.gds;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableEditor;

/**
 * Editor for java.awt.Color value for Attribute.
 * 
 * @author taubertj
 */
public class ColorEditor extends JTable implements GDSEditor {

	// generated
	private static final long serialVersionUID = 3758352220354964984L;

	/**
	 * Constructor for given Attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	public ColorEditor(Attribute attribute) {
		this.setModel(new GDSTableModel(attribute));
		this.setDefaultEditor(Color.class, new ColorTableEditor());
		this.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
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
			return "Color";
		}

		public Object getValueAt(int row, int col) {
			return attribute.getValue();
		}

		public Class<?> getColumnClass(int c) {
			return Color.class;
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
		return Color.BLACK;
	}

	public void flushChanges() {
		TableCellEditor ce = getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}
}
