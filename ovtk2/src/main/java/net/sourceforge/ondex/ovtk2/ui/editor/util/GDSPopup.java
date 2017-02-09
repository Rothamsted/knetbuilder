package net.sourceforge.ondex.ovtk2.ui.editor.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;

import net.sourceforge.ondex.core.Attribute;

/**
 * Displays a popup menu with check box for indexing flag.
 * 
 * @author taubertj
 */
public class GDSPopup extends JPopupMenu implements ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8120691497155750889L;

	/**
	 * concept Attribute to modify
	 */
	Attribute attribute;

	/**
	 * check box for do index
	 */
	JCheckBox box;

	/**
	 * Modify given concept Attribute
	 * 
	 * @param attribute
	 *            Attribute to modify
	 */
	public GDSPopup(Attribute attribute) {
		this.attribute = attribute;

		// editor panel
		BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		this.setLayout(layout);

		// check box for do index
		box = new JCheckBox("Do index this Attribute?");
		box.setSelected(attribute.isDoIndex());
		this.add(box);

		// button to close popup
		JButton close = new JButton("close");
		close.addActionListener(this);
		this.add(close);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		attribute.setDoIndex(box.isSelected());
	}
}
