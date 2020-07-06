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

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;

/**
 * DataSource properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogDataSource extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = 1598605227584612734L;

	// current AbstractONDEXGraph
	private ONDEXGraph aog = null;

	// current DialogConcept
	private DialogConcept conceptDialog = null;

	// id inpt field
	private JTextField id = new JTextField();

	// fullname input field
	private JTextField fullname = new JTextField();

	// description input field
	private JTextPane description = new JTextPane();

	/**
	 * Constructs user input to add a DataSource.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param conceptDialog
	 *            Concept Properties Dialog
	 */
	public DialogDataSource(ONDEXGraph aog, DialogConcept conceptDialog) {
		super("Dialog.DataSource.Title", "Properties16.gif");

		this.aog = aog;
		this.conceptDialog = conceptDialog;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.DataSource.Apply", "Dialog.DataSource.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to add a DataSource.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param dataSource
	 *            DataSource to use
	 */
	public DialogDataSource(ONDEXGraph aog, DataSource dataSource) {
		super("Dialog.DataSource.Title", "Properties16.gif");

		this.aog = aog;

		// set existing information
		id.setText(dataSource.getId());
		fullname.setText(dataSource.getFullname());
		description.setText(dataSource.getDescription());

		// set everything to disabled
		id.setEditable(false);
		fullname.setEditable(false);
		description.setEnabled(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel(null, "Dialog.DataSource.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for cv.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.DataSource.DataSource"));
		properties.setBorder(propertiesBorder);

		// DataSource id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.DataSource.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// DataSource fullname
		JLabel fullnameLabel = new JLabel(Config.language.getProperty("Dialog.DataSource.FullName"));
		properties.add(fullnameLabel);
		fullname.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(fullname);

		// DataSource description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.DataSource.Description"));
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
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.DataSource.InvalidID"), Config.language.getProperty("Dialog.DataSource.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkDataSource(id.getText())) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.DataSource.DuplicateID"), Config.language.getProperty("Dialog.DataSource.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new DataSource
		if (cmd.equals(APPLY)) {
			if (validateEntry()) {
				aog.getMetaData().createDataSource(id.getText(), fullname.getText(), description.getText());
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}
				conceptDialog.initDataSource(id.getText());
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
