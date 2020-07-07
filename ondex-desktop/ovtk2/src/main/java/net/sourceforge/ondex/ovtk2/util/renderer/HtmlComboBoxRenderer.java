package net.sourceforge.ondex.ovtk2.util.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.ondex.ovtk2.ui.toolbars.MenuGraphSearchBox.MetaDataWrapper;
import net.sourceforge.ondex.ovtk2.util.IntegerStringWrapper;

/**
 * Class for rendering Html content in a JComboBox.
 * 
 * @author taubertj
 * 
 */
public class HtmlComboBoxRenderer extends JLabel implements ListCellRenderer {

	// generated
	private static final long serialVersionUID = 6496157460497809308L;

	/**
	 * Constructor to set initial alignment.
	 * 
	 */
	public HtmlComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// Set the text wrapped as html.
		String text = "";
		if (value != null) {
			if (value instanceof IntegerStringWrapper) {
				IntegerStringWrapper wrapper = (IntegerStringWrapper) value;
				text = wrapper.toString();
				setToolTipText(wrapper.getDescription());
			} else if (value instanceof MetaDataWrapper) {
				MetaDataWrapper wrapper = (MetaDataWrapper) value;
				text = wrapper.toString();
				setToolTipText(wrapper.getDescription());
			} else {
				text = value.toString();
				setToolTipText(text);
				if (text.length() > 15)
					text = text.substring(0, 15) + "...";
			}
		}

		setText("<html>" + text + "</html>");

		return this;
	}

}
