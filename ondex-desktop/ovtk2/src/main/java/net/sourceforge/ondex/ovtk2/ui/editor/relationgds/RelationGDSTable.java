package net.sourceforge.ondex.ovtk2.ui.editor.relationgds;

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
 * JTable displaying relation Attribute specific content.
 * 
 * @author taubertj
 * 
 */
public class RelationGDSTable extends JTable {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -6526395333669797696L;

	/**
	 * table cell editor for relation Attribute
	 */
	RelationGDSTableCellEditor rgdsTableCellEditor = null;

	/**
	 * table cell renderer for relation Attribute
	 */
	RelationGDSTableCellRenderer rgdsTableCellRenderer = new RelationGDSTableCellRenderer();

	/**
	 * Creates table for TableModel containing relation Attribute.
	 * 
	 * @param graph
	 *            current ONDEXGraph
	 * @param dm
	 *            TableModel with relation Attribute
	 */
	public RelationGDSTable(TableModel dm, ONDEXGraph graph) {
		super(dm);
		this.setAutoCreateRowSorter(true);
		TableRowSorter<?> sorter = (TableRowSorter<?>) this.getRowSorter();
		sorter.setStringConverter(new EditorTableStringConverter());
		sorter.setComparator(0, new ONDEXEntityComparator());
		rgdsTableCellEditor = new RelationGDSTableCellEditor(graph);

		// check for number Attribute attributes
		for (int i = 0; i < dm.getColumnCount(); i++) {
			AttributeName an = graph.getMetaData().getAttributeName(dm.getColumnName(i));
			if (an != null && Number.class.isAssignableFrom(an.getDataType())) {
				Class<?> cl = an.getDataType();
				if (Number.class.isAssignableFrom(cl)) {
					sorter.setComparator(i, new GDSNumberComparator());
				}
			}
		}
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		return rgdsTableCellEditor;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		return rgdsTableCellRenderer;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return false;
		return super.isCellEditable(row, column);
	}
}
