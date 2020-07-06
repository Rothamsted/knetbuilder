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

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;

/**
 * RelationType properties dialog.
 * 
 * @author taubertj
 * 
 */
public class DialogRelationType extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	// generated
	private static final long serialVersionUID = 6524922985770998845L;

	// current ONDEXGraph
	private ONDEXGraph aog = null;

	// current DialogRelation
	private DialogRelation dialog = null;

	// id inpt field
	private JTextField id = new JTextField();

	// fullname input field
	private JTextField fullname = new JTextField();

	// description input field
	private JTextPane description = new JTextPane();

	// inverseName input field
	private JTextField inverse = new JTextField();

	// antisymmetric input box
	private JComboBox antisym = new JComboBox(new Boolean[] { Boolean.FALSE, Boolean.TRUE });

	// reflexive input box
	private JComboBox reflexive = new JComboBox(new Boolean[] { Boolean.FALSE, Boolean.TRUE });

	// symmetric input box
	private JComboBox sym = new JComboBox(new Boolean[] { Boolean.FALSE, Boolean.TRUE });

	// transitive input box
	private JComboBox trans = new JComboBox(new Boolean[] { Boolean.FALSE, Boolean.TRUE });

	// specialisationOf input box
	private JComboBox specialisationOf = null;

	/**
	 * Constructs user input to add a RelationType.
	 * 
	 * @param aog
	 *            ONDEXGraph to add to
	 * @param relationDialog
	 *            Relation Properties Dialog
	 */
	public DialogRelationType(ONDEXGraph aog, DialogRelation relationDialog) {
		super("Dialog.RelationType.Title", "Properties16.gif");

		this.aog = aog;
		this.dialog = relationDialog;

		initSpecialisationOf();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.RelationType.Apply", "Dialog.RelationType.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to add a RelationType.
	 * 
	 * @param aog
	 *            ONDEXGraph to add to
	 * @param relationType
	 *            RelationType given, go to read only view
	 */
	public DialogRelationType(ONDEXGraph aog, RelationType relationType) {
		super("Dialog.RelationType.Title", "Properties16.gif");

		this.aog = aog;

		initSpecialisationOf();

		if (relationType != null) {
			id.setText(relationType.getId());

			if (relationType.getFullname() != null)
				fullname.setText(relationType.getFullname());

			if (relationType.getDescription() != null)
				description.setText(relationType.getDescription());

			if (relationType.getInverseName() != null)
				inverse.setText(relationType.getInverseName());

			if (relationType.getSpecialisationOf() != null)
				specialisationOf.setSelectedItem(relationType.getSpecialisationOf().getId());

			antisym.setSelectedItem(Boolean.valueOf(relationType.isAntisymmetric()));
			reflexive.setSelectedItem(Boolean.valueOf(relationType.isReflexive()));
			sym.setSelectedItem(Boolean.valueOf(relationType.isSymmetric()));
			trans.setSelectedItem(Boolean.valueOf(relationType.isTransitiv()));
		}

		// set everything to disabled
		id.setEnabled(false);
		fullname.setEnabled(false);
		description.setEnabled(false);
		inverse.setEnabled(false);
		antisym.setEnabled(false);
		reflexive.setEnabled(false);
		sym.setEnabled(false);
		trans.setEnabled(false);
		specialisationOf.setEnabled(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel(null, "Dialog.RelationType.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Loads available RelationTypes into ComboBox.
	 * 
	 */
	private void initSpecialisationOf() {
		// get list of available rts
		Vector<String> relationTypes = new Vector<String>();
		relationTypes.add("");
		RelationType[] sorted = aog.getMetaData().getRelationTypes().toArray(new RelationType[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (RelationType rt : sorted) {
			relationTypes.add(rt.getId());
		}
		specialisationOf = new JComboBox(relationTypes);
	}

	/**
	 * Creates the properties panel for relation type.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel();
		GroupLayout layout = new GroupLayout(properties);
		properties.setLayout(layout);

		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.RelationType.RelationType"));
		properties.setBorder(propertiesBorder);

		// RT id
		JLabel idLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.ID"));
		properties.add(idLabel);
		id.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		id.setBackground(this.getRequiredColor());
		properties.add(id);

		// RT fullname
		JLabel fullnameLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.FullName"));
		properties.add(fullnameLabel);
		fullname.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(fullname);

		// RT description
		JLabel descriptionLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.Description"));
		properties.add(descriptionLabel);
		JScrollPane scroll = new JScrollPane(description);
		scroll.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight() * 2));
		properties.add(scroll);

		// RT inverseName
		JLabel inverseLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.InverseName"));
		properties.add(inverseLabel);
		inverse.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(inverse);

		// RT antisymmetric
		JLabel antisymLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.AntiSym"));
		properties.add(antisymLabel);
		antisym.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(antisym);

		// RT reflexive
		JLabel reflexiveLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.Reflexive"));
		properties.add(reflexiveLabel);
		reflexive.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(reflexive);

		// RT symmetric
		JLabel symLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.Symmetric"));
		properties.add(symLabel);
		sym.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(sym);

		// RT transitive
		JLabel transLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.Transitive"));
		properties.add(transLabel);
		trans.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(trans);

		// RT specialisationOf
		JLabel specialisationOfLabel = new JLabel(Config.language.getProperty("Dialog.RelationType.SpecialisationOf"));
		properties.add(specialisationOfLabel);
		specialisationOf.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		properties.add(specialisationOf);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(idLabel).addComponent(fullnameLabel).addComponent(descriptionLabel).addComponent(inverseLabel).addComponent(antisymLabel).addComponent(reflexiveLabel).addComponent(symLabel).addComponent(transLabel).addComponent(specialisationOfLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup().addComponent(id, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(fullname, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(scroll, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(inverse, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(antisym, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(reflexive, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(sym, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(trans, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(specialisationOf, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fullnameLabel).addComponent(fullname)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(descriptionLabel).addComponent(scroll)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(inverseLabel).addComponent(inverse)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(antisymLabel).addComponent(antisym)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(reflexiveLabel).addComponent(reflexive)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(symLabel).addComponent(sym)).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(transLabel).addComponent(trans))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(specialisationOfLabel).addComponent(specialisationOf)));

		return properties;
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.RelationType.InvalidID"), Config.language.getProperty("Dialog.RelationType.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkRelationType(id.getText())) {
			JOptionPane.showInternalMessageDialog(this, Config.language.getProperty("Dialog.RelationType.DuplicateID"), Config.language.getProperty("Dialog.RelationType.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new RelationType
		if (cmd.equals(APPLY)) {
			if (validateEntry()) {
				String selection = (String) specialisationOf.getSelectedItem();
				if (selection.length() > 0) {
					RelationType rt = aog.getMetaData().getRelationType(selection);
					aog.getMetaData().createRelationType(id.getText(), fullname.getText(), description.getText(), inverse.getText(), (Boolean) antisym.getSelectedItem(), (Boolean) reflexive.getSelectedItem(), (Boolean) sym.getSelectedItem(), (Boolean) trans.getSelectedItem(), rt);
				} else {
					aog.getMetaData().getFactory().createRelationType(id.getText(), fullname.getText(), description.getText(), inverse.getText(), (Boolean) antisym.getSelectedItem(), (Boolean) reflexive.getSelectedItem(), (Boolean) sym.getSelectedItem(), (Boolean) trans.getSelectedItem());
				}
				try {
					this.setClosed(true);
				} catch (PropertyVetoException e) {
					// ignore
				}
				if (dialog != null)
					dialog.initRelationType(id.getText());
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
