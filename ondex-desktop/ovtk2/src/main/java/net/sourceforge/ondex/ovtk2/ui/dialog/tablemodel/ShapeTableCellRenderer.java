package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Component;
import java.awt.Shape;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Class for rendering shapes in a table.
 * 
 * @author taubertj
 * 
 */
public class ShapeTableCellRenderer extends JLabel implements TableCellRenderer {

	// generated
	private static final long serialVersionUID = -4285722928582370953L;

	Border unselectedBorder = null;

	Border selectedBorder = null;

	boolean isBordered = true;

	public ShapeTableCellRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque(true); // MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object shape, boolean isSelected, boolean hasFocus, int row, int column) {
		ShapeIcon icon = new ShapeIcon((Shape) shape);
		this.setIcon(icon);
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
				setBackground(table.getSelectionBackground());
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				}
				setBorder(unselectedBorder);
				setBackground(table.getBackground());
			}
		}

		setToolTipText("Click to change");
		return this;
	}
}
