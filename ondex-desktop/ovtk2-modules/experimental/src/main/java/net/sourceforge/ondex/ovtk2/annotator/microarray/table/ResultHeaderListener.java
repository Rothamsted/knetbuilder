package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Listener listens for MouseEvents on the header of table.
 * 
 * @author taubertj
 *
 */
public class ResultHeaderListener extends MouseAdapter {
	
	// associated table
	JTable table;

	// header of table
	JTableHeader header;

	// renderer of header
	ResultButtonHeaderRenderer renderer;

	// state array for reverse sorting
	boolean reverse[];

	/**
	 * Constructor that sets all internal variables.
	 * 
	 * @param header - JTableHeader
	 * @param renderer - ButtonHeaderRenderer
	 * @param table - JTable
	 */
	public ResultHeaderListener(JTableHeader header, ResultButtonHeaderRenderer renderer,
			JTable table) {
		this.table = table;
		this.header = header;
		this.renderer = renderer;
		reverse = new boolean[table.getColumnCount()];
	}
	
	/**
	 * Method from MouseAdapter
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// set column to renderer
		int col = header.columnAtPoint(e.getPoint());
		renderer.setPressedColumn(col);
		header.repaint();
		if (col > -1) {
			// change reverse sorting state
			reverse[col] = !reverse[col];
			String columnName = header.getColumnModel()
					.getColumn(col).getHeaderValue().toString();
			// sort table according to column
			((ResultTableModel)table.getModel()).sort(columnName, reverse[col]);
			table.clearSelection();
		}
	}

	/**
	 * Method from MouseAdapter
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		renderer.setPressedColumn(-1); // clear
		header.repaint();
	}
}
