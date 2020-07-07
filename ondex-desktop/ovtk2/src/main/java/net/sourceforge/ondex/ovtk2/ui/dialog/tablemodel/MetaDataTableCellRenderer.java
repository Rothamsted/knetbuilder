package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import net.sourceforge.ondex.core.MetaData;

/**
 * Class for rendering tooltips for meta data in a table.
 * 
 * @author taubertj
 * 
 */
public class MetaDataTableCellRenderer extends JLabel implements TableCellRenderer {

	// generated
	private static final long serialVersionUID = -4285722928582370953L;

	Border unselectedBorder = null;

	Border selectedBorder = null;

	boolean isBordered = true;

	public MetaDataTableCellRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque(true); // MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus, int row, int column) {
		MetaData m = (MetaData) object;
		this.setText(m.toString() + " (" + m.getId() + ")");
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				}
				setBorder(unselectedBorder);
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
		}

		setToolTipText(m.getDescription().trim());
		return this;
	}
}
