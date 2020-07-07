package net.sourceforge.ondex.ovtk2.filter.genomic;

import java.util.Hashtable;

import javax.swing.table.TableCellEditor;

/**
 * A row specific table cell editor
 * 
 * @author keywan
 *
 */
public class RowEditorModel {

	//save for each row its TableCellEditor
	private Hashtable<Integer, TableCellEditor> data;

	public RowEditorModel() {
		data = new Hashtable<Integer, TableCellEditor>();
	}

	public void addEditorForRow(int row, TableCellEditor e) {
		data.put(new Integer(row), e);
	}

	public void removeEditorForRow(int row) {
		data.remove(new Integer(row));
	}

	public TableCellEditor getEditor(int row) {
		return data.get(new Integer(row));
	}

}
