package net.sourceforge.ondex.ovtk2.filter.genomic;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public class JTableX extends JTable {

	/**
	 * auto generated
	 */
	private static final long serialVersionUID = 10502170735722444L;
	
	protected RowEditorModel rm;

	public JTableX(TableModel tm) {
		super(tm);
		rm = null;
	}

	public void setRowEditorModel(RowEditorModel rm) {
		this.rm = rm;
	}

	public RowEditorModel getRowEditorModel() {
		return rm;
	}

	public TableCellEditor getCellEditor(int row, int col) {
		TableCellEditor tmpEditor = null;
		if (rm != null && col==4)
			tmpEditor = rm.getEditor(row);
		if (tmpEditor != null)
			return tmpEditor;
		return super.getCellEditor(row, col);
	}

}
