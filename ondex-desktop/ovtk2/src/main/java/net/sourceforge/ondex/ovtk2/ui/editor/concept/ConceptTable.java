package net.sourceforge.ondex.ovtk2.ui.editor.concept;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.EditorTableStringConverter;
import net.sourceforge.ondex.ovtk2.ui.editor.util.ONDEXEntityComparator;

/**
 * JTable for concepts sets cell renderer and editor.
 * 
 * @author taubertj
 * 
 */
public class ConceptTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 1714570808770405993L;

	/**
	 * table cell renderer for concepts
	 */
	ConceptTableCellRenderer cTableCellRenderer = new ConceptTableCellRenderer();

	/**
	 * table cell editor for concepts
	 */
	ConceptTableCellEditor cTableCellEditor = null;

	/**
	 * Creates table for TableModel containing concepts.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 * @param dm
	 *            TableModel with concept
	 */
	public ConceptTable(TableModel dm, ONDEXGraph graph) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
		cTableCellEditor = new ConceptTableCellEditor(graph);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cTableCellRenderer;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return cTableCellEditor;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
