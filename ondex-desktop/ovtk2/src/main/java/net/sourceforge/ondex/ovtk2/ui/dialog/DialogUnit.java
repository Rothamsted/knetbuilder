package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;

/**
 * Unit properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogUnit extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = -394997201555003986L;

	// current ONDEXGraph
	private ONDEXGraph aog = null;

	// current DialogAttribute
	private DialogAttribute dialog = null;

	// calling JComboBox
	private JComboBox box = null;

	// id inpt field
	private JTextField id = new JTextField();

	// fullname input field
	private JTextField fullname = new JTextField();

	// description input field
	private JTextField description = new JTextField();

	/**
	 * Constructs user input to add a Unit.
	 * 
	 * @param aog
	 *            ONDEXGraph to add to
	 * @param dialog
	 *            Attribute Properties Dialog
	 * @param box
	 *            actual JComboBox
	 */
	public DialogUnit(ONDEXGraph aog, DialogAttribute dialog, JComboBox box) {
		super("Dialog.Unit.Title", "Properties16.gif");

		this.aog = aog;
		this.dialog = dialog;
		this.box = box;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Unit.Apply", "Dialog.Unit.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for unit.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Unit.Unit"));
		properties.setBorder(propertiesBorder);

		// Unit id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.Unit.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// Unit fullname
		JLabel fullnameLabel = new JLabel(Config.language.getProperty("Dialog.Unit.FullName"));
		properties.add(fullnameLabel);
		fullname.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(fullname);

		// Unit description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.Unit.Description"));
		properties.add(descriptionLabel);
		description.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(description);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(fullnameLabel).addComponent(descriptionLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(fullname, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(description, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fullnameLabel).addComponent(fullname)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(descriptionLabel).addComponent(description)));

		return properties;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Unit.InvalidID"), Config.language.getProperty("Dialog.Unit.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkUnit(id.getText())) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.Unit.DuplicateID"), Config.language.getProperty("Dialog.Unit.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new Unit
		if (cmd.equals(APPLY)) {
			if (validateEntry()) {
				aog.getMetaData().createUnit(id.getText(), fullname.getText(), description.getText());
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}
				dialog.updateUnits(id.getText());
				box.setSelectedItem(id.getText());
			}
		}

		// cancel dialog
		else if (cmd.equals(CANCEL)) {
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}
	}
}
