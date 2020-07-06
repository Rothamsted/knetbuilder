package net.sourceforge.ondex.ovtk2.ui.editor.util;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Class acts as a simple comparator using the toString method on Object. Also
 * provides table arrangement methods.
 * 
 * @author taubertj
 * 
 */
public class Util implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {
		return o1.toString().compareTo(o2.toString());
	}

	/**
	 * calculates the optimal width of a given table by summing the maximal
	 * width of each column
	 * 
	 * @param table
	 *            table which should be calculated
	 */
	public static void calcColumnWidths(JTable table, int maxColumnWidth) {

		// get table header and renderer
		JTableHeader header = table.getTableHeader();
		TableCellRenderer defaultHeaderRenderer = null;
		if (header != null)
			defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		TableModel data = table.getModel();

		int margin = columns.getColumnMargin();
		int rowCount = data.getRowCount();
		int totalWidth = 0;
		// go through all columns
		for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
			// get current column
			TableColumn column = columns.getColumn(i);
			int columnIndex = column.getModelIndex();
			int width = -1;

			// get header for current column
			TableCellRenderer h = column.getHeaderRenderer();
			if (h == null)
				h = defaultHeaderRenderer;
			if (h != null) { // Not explicitly impossible
				Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, i);
				// get header width
				width = (int) (c.getPreferredSize().width * 1.1);
			}

			// go through all rows
			for (int row = rowCount - 1; row >= 0; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);
				Component c = r.getTableCellRendererComponent(table, data.getValueAt(row, columnIndex), false, false, row, i);
				// max of header width and actual component width
				width = Math.max(width, c.getPreferredSize().width);
			}

			width = Math.min(maxColumnWidth, width);
			if (width >= 0)
				column.setPreferredWidth(width + margin);
			// setting and summing preferred width of each column
			totalWidth += column.getPreferredWidth();
		}
		// set width to table
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		Dimension size = table.getPreferredScrollableViewportSize();
		size.width = totalWidth;
		table.setPreferredScrollableViewportSize(size);
	}

}
