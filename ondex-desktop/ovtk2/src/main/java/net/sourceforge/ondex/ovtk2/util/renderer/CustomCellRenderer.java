package net.sourceforge.ondex.ovtk2.util.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Customised JList to accept Component items
 * 
 * @author hindlem
 */
public class CustomCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {

		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);

		if (value instanceof JLabel) {
			JLabel labelNew = (JLabel) value;
			label.setText(labelNew.getText());
			label.setToolTipText(labelNew.getToolTipText());
		}
		return label;
	}
}
