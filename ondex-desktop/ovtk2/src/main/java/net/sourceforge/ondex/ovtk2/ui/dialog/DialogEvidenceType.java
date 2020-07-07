package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;

/**
 * Evidence properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogEvidenceType extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = 1598605227584612734L;

	// current AbstractONDEXGraph
	private ONDEXGraph aog = null;

	// current OVTK2Dialog
	private OVTK2Dialog dialog = null;

	// id inpt field
	private JTextField id = new JTextField();

	// fullname input field
	private JTextField fullname = new JTextField();

	// description input field
	private JTextPane description = new JTextPane();

	/**
	 * Constructs user input to add a EvidenceType.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param dialog
	 *            Concept/Relation Properties Dialog
	 */
	public DialogEvidenceType(ONDEXGraph aog, OVTK2Dialog dialog) {
		super("Dialog.EvidenceType.Title", "Properties16.gif");

		this.aog = aog;
		this.dialog = dialog;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.EvidenceType.Apply", "Dialog.EvidenceType.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to add a EvidenceType.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param et
	 *            EvidenceType to use
	 */
	public DialogEvidenceType(ONDEXGraph aog, EvidenceType et) {
		super("Dialog.EvidenceType.Title", "Properties16.gif");

		this.aog = aog;

		// set existing information
		id.setText(et.getId());
		fullname.setText(et.getFullname());
		description.setText(et.getDescription());

		// set everything to disabled
		id.setEditable(false);
		fullname.setEditable(false);
		description.setEnabled(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel(null, "Dialog.EvidenceType.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for evidence.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.EvidenceType.EvidenceType"));
		properties.setBorder(propertiesBorder);

		// EvidenceType id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.EvidenceType.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// EvidenceType fullname
		JLabel fullnameLabel = new JLabel(Config.language.getProperty("Dialog.EvidenceType.FullName"));
		properties.add(fullnameLabel);
		fullname.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(fullname);

		// EvidenceType description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.EvidenceType.Description"));
		properties.add(descriptionLabel);
		JScrollPane scroll = new JScrollPane(description);
		scroll.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight() * 2));
		properties.add(scroll);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(fullnameLabel).addComponent(descriptionLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(fullname, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(scroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fullnameLabel).addComponent(fullname)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(descriptionLabel).addComponent(scroll)));

		return properties;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.EvidenceType.InvalidID"), Config.language.getProperty("Dialog.EvidenceType.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkEvidenceType(id.getText())) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.EvidenceType.DuplicateID"), Config.language.getProperty("Dialog.EvidenceType.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new EvidenceType
		if (cmd.equals(APPLY)) {
			if (validateEntry()) {
				aog.getMetaData().createEvidenceType(id.getText(), fullname.getText(), description.getText());
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}
				if (dialog instanceof DialogConcept) {
					((DialogConcept) dialog).initEvidenceType(id.getText());
				} else if (dialog instanceof DialogRelation) {
					((DialogRelation) dialog).initEvidenceTypes(id.getText());
				}
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
