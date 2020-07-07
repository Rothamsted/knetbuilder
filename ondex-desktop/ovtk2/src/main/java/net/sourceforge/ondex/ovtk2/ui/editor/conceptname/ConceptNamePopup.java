package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;

import net.sourceforge.ondex.core.ConceptName;

/**
 * Displays a popup menu with check box for preferred name.
 * 
 * @author taubertj
 * 
 */
public class ConceptNamePopup extends JPopupMenu implements ActionListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8120691497155750889L;

	/**
	 * concept name to modify
	 */
	ConceptName name;

	/**
	 * check box for preferred
	 */
	JCheckBox box;

	/**
	 * Modify given concept name
	 * 
	 * @param name
	 *            ConceptName to modify
	 */
	public ConceptNamePopup(ConceptName name) {
		this.name = name;

		// editor panel
		BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		this.setLayout(layout);

		// check box for preferred
		box = new JCheckBox("Is a preferred concept name?");
		box.setSelected(name.isPreferred());
		this.add(box);

		// button to close popup
		JButton close = new JButton("close");
		close.addActionListener(this);
		this.add(close);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		name.setPreferred(box.isSelected());
	}
}
