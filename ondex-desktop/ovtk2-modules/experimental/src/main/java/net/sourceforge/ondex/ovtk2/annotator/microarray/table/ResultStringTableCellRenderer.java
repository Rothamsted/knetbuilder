package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders table cells which contain String objects.
 * 
 * @author taubertj
 *
 */
public class ResultStringTableCellRenderer extends DefaultTableCellRenderer {
	
	/**
	 * Default serialisation unique id.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Constructor calls super constructor.
	 * 
	 * @param vog - VisualONDEXGraph
	 */
	public ResultStringTableCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		
		// get component for field rendering
		Component c = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		c.setForeground(new Color(0, 0, 0));

		if (value instanceof String) {
			if (((String)value).equalsIgnoreCase("Not Found")) {
				c.setForeground(new Color(30, 0, 0));
				c.setForeground(new Color(125, 125, 125));
			}
		}
		
		return c;
	}
}
