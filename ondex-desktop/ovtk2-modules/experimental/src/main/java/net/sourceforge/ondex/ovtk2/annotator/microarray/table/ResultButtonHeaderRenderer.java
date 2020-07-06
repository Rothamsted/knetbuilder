package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renders buttons in the table header to enable table sorting. 
 * 
 * @author taubertj
 *
 */
public class ResultButtonHeaderRenderer extends JButton implements TableCellRenderer{
	
	/**
	 * Default serialisation unique id.
	 */
	private static final long serialVersionUID = 1L;
	
	// which column to sort
	int columnPressed;

	private String highlightColumn;

	private Color defaultColor;
	
	/**
	 * Constructor inits default values.
	 *
	 */
	public ResultButtonHeaderRenderer() {
		defaultColor = this.getBackground();
		columnPressed = -1;
		setMargin(new Insets(0, 0, 0, 0));
	}

	/**
	 * Method from TableCellRenderer
	 */
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		setText((value == null) ? "" : value.toString());
		boolean isPressed = (columnPressed == column);
		getModel().setPressed(isPressed);
		getModel().setArmed(isPressed);
		
		if (value.toString() != null && value.toString().equalsIgnoreCase(highlightColumn)) {
			this.setBackground(Color.yellow);
		} else {
			this.setBackground(defaultColor);
		}
		
		return this;
	}

	/**
	 * Sets the pressed header column.
	 * 
	 * @param col - int
	 */
	public void setPressedColumn(int col) {
		columnPressed = col;
	}
	
	/**
	 * Highlight a Column name
	 * @param name to highlight (all occurances)
	 */
	public void highlightColumn(String header) {
		highlightColumn = header;
	}
}
