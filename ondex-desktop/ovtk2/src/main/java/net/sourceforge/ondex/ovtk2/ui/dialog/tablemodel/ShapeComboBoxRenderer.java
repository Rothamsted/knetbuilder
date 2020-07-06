package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Component;
import java.awt.Shape;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renderer for shapes in ComboBox.
 * 
 * @author taubertj
 * 
 */
public class ShapeComboBoxRenderer extends JLabel implements ListCellRenderer {

	// generated
	private static final long serialVersionUID = -7175082164979272449L;

	public ShapeComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	/*
	 * This method creates the icon corresponding to the selected value and
	 * returns the set up to display the image.
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// Set the icon
		Icon icon = new ShapeIcon((Shape) value);
		setIcon(icon);

		return this;
	}
}
