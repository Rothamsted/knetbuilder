package net.sourceforge.ondex.ovtk2.ui.editor.relation;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.EditorTableStringConverter;
import net.sourceforge.ondex.ovtk2.ui.editor.util.ONDEXEntityComparator;

/**
 * Table for editing relations, setup special cell renderer and editor.
 * 
 * @author taubertj
 * 
 */
public class RelationTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 4634277447219613148L;

	/**
	 * table cell renderer for relations
	 */
	RelationTableCellRenderer rTableCellRenderer = new RelationTableCellRenderer();

	/**
	 * table cell editor for relations
	 */
	RelationTableCellEditor rTableCellEditor = null;

	/**
	 * Creates table for TableModel containing relations.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 * @param dm
	 *            TableModel with relations
	 */
	public RelationTable(TableModel dm, ONDEXGraph graph) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
		rTableCellEditor = new RelationTableCellEditor(graph);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return rTableCellRenderer;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return rTableCellEditor;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
