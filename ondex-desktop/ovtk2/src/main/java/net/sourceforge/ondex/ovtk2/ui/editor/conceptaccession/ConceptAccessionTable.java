package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.EditorTableStringConverter;
import net.sourceforge.ondex.ovtk2.ui.editor.util.ONDEXEntityComparator;

/**
 * JTable displaying concept accession specific content.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -5360335258044820162L;

	/**
	 * table cell renderer for concept accessions
	 */
	ConceptAccessionTableCellRenderer caTableCellRenderer = new ConceptAccessionTableCellRenderer();

	/**
	 * table cell editor for concept accessions
	 */
	ConceptAccessionTableCellEditor caTableCellEditor = null;

	/**
	 * Creates table for TableModel containing concept accessions.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 * @param dm
	 *            TableModel with concept accessions
	 */
	public ConceptAccessionTable(TableModel dm, ONDEXGraph graph) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
		caTableCellEditor = new ConceptAccessionTableCellEditor(graph);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return caTableCellRenderer;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return caTableCellEditor;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
