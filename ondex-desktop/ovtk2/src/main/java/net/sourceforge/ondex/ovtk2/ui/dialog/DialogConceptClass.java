package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;

/**
 * ConceptClass properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogConceptClass extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = 6524922985770998845L;

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

	// specialisationOf input box
	private JComboBox specialisationOf = null;

	/**
	 * Constructs user input to add a ConceptClass.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param conceptDialog
	 *            Concept Properties Dialog
	 */
	public DialogConceptClass(ONDEXGraph aog, DialogConcept conceptDialog) {
		super("Dialog.ConceptClass.Title", "Properties16.gif");

		this.aog = aog;
		this.conceptDialog = conceptDialog;

		initSpecialisationOf();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.ConceptClass.Apply", "Dialog.ConceptClass.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to add a ConceptClass.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param conceptClass
	 *            Concept Class to use
	 */
	public DialogConceptClass(ONDEXGraph aog, ConceptClass conceptClass) {
		super("Dialog.ConceptClass.Title", "Properties16.gif");

		this.aog = aog;

		initSpecialisationOf();

		// set existing information
		id.setText(conceptClass.getId());
		fullname.setText(conceptClass.getFullname());
		description.setText(conceptClass.getDescription());
		ConceptClass specialisation = conceptClass.getSpecialisationOf();
		if (specialisation != null)
			specialisationOf.setSelectedItem(specialisation.getId());

		// set everything to disabled
		id.setEditable(false);
		fullname.setEditable(false);
		description.setEnabled(false);
		specialisationOf.setEnabled(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel(null, "Dialog.ConceptClass.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Loads available ConceptClasses into ComboBox.
	 * 
	 */
	private void initSpecialisationOf() {
		// get list of available ccs
		Vector<String> ccs = new Vector<String>();
		ccs.add("");
		ConceptClass[] sorted = aog.getMetaData().getConceptClasses().toArray(new ConceptClass[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (ConceptClass cc : sorted) {
			ccs.add(cc.getId());
		}
		specialisationOf = new JComboBox(ccs);
	}

	/**
	 * Creates the properties panel for concept class.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.ConceptClass.ConceptClass"));
		properties.setBorder(propertiesBorder);

		// CC id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.ConceptClass.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// CC fullname
		JLabel fullnameLabel = new JLabel(Config.language.getProperty("Dialog.ConceptClass.FullName"));
		properties.add(fullnameLabel);
		fullname.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(fullname);

		// CC description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.ConceptClass.Description"));
		properties.add(descriptionLabel);
		JScrollPane scroll = new JScrollPane(description);
		scroll.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight() * 2));
		properties.add(scroll);

		// CC specialisationOf
		JLabel specialisationOfLabel = new JLabel(Config.language.getProperty("Dialog.ConceptClass.SpecialisationOf"));
		properties.add(specialisationOfLabel);
		specialisationOf.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(specialisationOf);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(fullnameLabel).addComponent(descriptionLabel).addComponent(specialisationOfLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(fullname, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(scroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(specialisationOf, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fullnameLabel).addComponent(fullname)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(descriptionLabel).addComponent(scroll)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(specialisationOfLabel).addComponent(specialisationOf)));

		return properties;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.ConceptClass.InvalidID"), Config.language.getProperty("Dialog.ConceptClass.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkConceptClass(id.getText())) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.ConceptClass.DuplicateID"), Config.language.getProperty("Dialog.ConceptClass.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new ConceptClass
		if (cmd.equals(APPLY)) {
			if (validateEntry()) {
				String selection = (String) specialisationOf.getSelectedItem();
				if (selection.length() > 0) {
					ConceptClass cc = aog.getMetaData().getConceptClass(selection);
					aog.getMetaData().createConceptClass(id.getText(), fullname.getText(), description.getText(), cc);
				} else {
					aog.getMetaData().getFactory().createConceptClass(id.getText(), fullname.getText(), description.getText());
				}
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}
				conceptDialog.initConceptClass(id.getText());
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
