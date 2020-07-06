package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class ResultTableUtil {

	/**
	 * Calculates and sets the width distribution of columns in a given JTable.
	 * 
	 * @param table - JTable
	 */
	public static void calcColumnWidths(JTable table) {

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
				Component c =
					h.getTableCellRendererComponent(
							table,
							column.getHeaderValue(),
							false,
							false,
							-1,
							i);
				// get header width
				width = (int) (c.getPreferredSize().width * 1.1);
			}
			
			// go through all rows
			for (int row = rowCount - 1; row >= 0; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);
				Component c =
					r.getTableCellRendererComponent(
							table,
							data.getValueAt(row, columnIndex),
							false,
							false,
							row,
							i);
				// max of header width and actual component width
				width = Math.max(width, c.getPreferredSize().width);
			}

			if (width > 200)
				width = 200;
			
			if (width >= 0)
				column.setPreferredWidth(width + margin);
			// setting and summing preferred width of each column
			totalWidth += column.getPreferredWidth();
		}
		
		// set width to table
		Dimension size = table.getPreferredScrollableViewportSize();
		size.width = totalWidth;
		table.setPreferredScrollableViewportSize(size);
	}
}
