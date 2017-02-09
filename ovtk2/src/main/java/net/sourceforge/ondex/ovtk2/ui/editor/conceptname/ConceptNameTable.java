package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.ovtk2.ui.editor.util.EditorTableStringConverter;
import net.sourceforge.ondex.ovtk2.ui.editor.util.ONDEXEntityComparator;

/**
 * JTable displaying concept name specific content.
 * 
 * @author taubertj
 * 
 */
public class ConceptNameTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -5360335258044820161L;

	/**
	 * table cell renderer for concept names
	 */
	ConceptNameTableCellRenderer cnTableCellRenderer = new ConceptNameTableCellRenderer();

	/**
	 * table cell editor for concept names
	 */
	ConceptNameTableCellEditor cnTableCellEditor = new ConceptNameTableCellEditor();

	/**
	 * Creates table for TableModel containing concept names.
	 * 
	 * @param dm
	 *            TableModel with concept names
	 */
	public ConceptNameTable(TableModel dm) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cnTableCellRenderer;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return cnTableCellEditor;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
