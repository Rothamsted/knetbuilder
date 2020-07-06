package net.sourceforge.ondex.ovtk2.ui.editor.conceptgds;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.EditorTableStringConverter;
import net.sourceforge.ondex.ovtk2.ui.editor.util.GDSNumberComparator;
import net.sourceforge.ondex.ovtk2.ui.editor.util.ONDEXEntityComparator;

/**
 * JTable displaying concept Attribute specific content.
 * 
 * @author taubertj
 * 
 */
public class ConceptGDSTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -5360335258044820167L;

	/**
	 * table cell editor for concept Attribute
	 */
	ConceptGDSTableCellEditor cgdsTableCellEditor = null;

	/**
	 * table cell renderer for concept Attribute
	 */
	ConceptGDSTableCellRenderer cgdsTableCellRenderer = new ConceptGDSTableCellRenderer();

	/**
	 * Creates table for TableModel containing concept Attribute.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 * @param dm
	 *            TableModel with concept Attribute
	 */
	public ConceptGDSTable(TableModel dm, ONDEXGraph graph) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
		cgdsTableCellEditor = new ConceptGDSTableCellEditor(graph);

		// check for number Attribute attributes
		for (int i = 0; i < dm.getColumnCount(); i++) {
			AttributeName an = graph.getMetaData().getAttributeName(dm.getColumnName(i));
			if (an != null) {
				Class<?> cl = an.getDataType();
				if (Number.class.isAssignableFrom(cl)) {
					sorter.setComparator(i, new GDSNumberComparator());
				}
			}
		}
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return cgdsTableCellEditor;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return cgdsTableCellRenderer;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
