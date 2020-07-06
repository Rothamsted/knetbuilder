package net.sourceforge.ondex.ovtk2.ui.gds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogAttribute;

/**
 * Panel displaying AttributeName information and GDSEditor.
 * 
 * @author taubertj
 */
public class AttributePanel extends JPanel implements ActionListener {

	// generated
	private static final long serialVersionUID = 5100753014972731016L;

	// current AbstractONDEXGraph
	private ONDEXGraph aog = null;

	// current Attribute
	private Attribute attribute = null;

	// parent DialogAttribute
	private DialogAttribute dialog = null;

	// attribute name id
	private JTextField id = null;

	// full name of attribute name
	private JTextField full = null;

	// Description of attribute name
	private JTextField desc = null;

	// Java class of attribute name
	private JTextField classField = null;

	// unit selection for attribute name
	private JComboBox unit = null;

	// specialisation of attribute name
	private JComboBox specialisationOf = null;

	// whether or not to index this Attribute
	private JCheckBox doIndex = null;

	// contains JAVA data type editor
	private JScrollPane editorScroll = null;

	// the editor component
	private JComponent editor = null;

	// properties of attribute name
	private JPanel anPanel = null;

	// decided about indexing
	private JPanel indexPanel = null;

	/**
	 * Creates a panel displaying the attribute name and value editor.
	 * 
	 * @param dialog
	 *            parent DialogAttribute
	 * @param aog
	 *            AbstractONDEXGraph
	 * @param attribute
	 *            ConceptAttribute or RelationAttribute
	 */
	public AttributePanel(DialogAttribute dialog, ONDEXGraph aog, Attribute attribute) {

		this.dialog = dialog;
		this.aog = aog;
		this.attribute = attribute;

		initUI(attribute == null);
	}

	/**
	 * Return AttributeName id.
	 * 
	 * @return String
	 */
	public String getID() {
		return id.getText();
	}

	/**
	 * Return AttributeName fullname.
	 * 
	 * @return String
	 */
	public String getFullname() {
		return full.getText();
	}

	/**
	 * Return AttributeName description.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return desc.getText();
	}

	/**
	 * Return AttributeName unit id.
	 * 
	 * @return String
	 */
	public String getUnit() {
		return (String) unit.getSelectedItem();
	}

	/**
	 * Return AttributeName specialisationOf id.
	 * 
	 * @return String
	 */
	public String getSpecialisationOf() {
		return (String) specialisationOf.getSelectedItem();
	}

	/**
	 * Constructs the UI for this Attribute.
	 */
	private void initUI(boolean editable) {

		this.setLayout(new BorderLayout());

		// get attribute name and class
		String anID = "";
		String anFull = "";
		String anDesc = "";
		String className = "";
		Unit selectedUnit = null;
		AttributeName selectedAn = null;
		if (!editable) {
			AttributeName an = attribute.getOfType();
			anID = an.getId();
			anFull = an.getFullname();
			anDesc = an.getDescription();
			className = an.getDataTypeAsString();
			selectedUnit = an.getUnit();
			selectedAn = an.getSpecialisationOf();
		}

		// panel for attribute name
		anPanel = new JPanel();
		BoxLayout contentLayout = new BoxLayout(anPanel, BoxLayout.PAGE_AXIS);
		anPanel.setLayout(contentLayout);
		TitledBorder anBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Attribute.AttributeName"));
		anPanel.setBorder(anBorder);

		// attribute name id
		JPanel idPanel = new JPanel(new BorderLayout());
		idPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.ID")), BorderLayout.WEST);
		id = new JTextField(anID);
		id.setBackground(dialog.getRequiredColor());
		id.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
		id.setEnabled(editable);
		idPanel.add(id, BorderLayout.EAST);
		anPanel.add(idPanel);

		// attribute name fullname
		JPanel fullPanel = new JPanel(new BorderLayout());
		fullPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.FullName")), BorderLayout.WEST);
		full = new JTextField(anFull);
		full.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
		fullPanel.add(full, BorderLayout.EAST);
		anPanel.add(fullPanel);

		// attribute name desc
		JPanel descPanel = new JPanel(new BorderLayout());
		descPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Description")), BorderLayout.WEST);
		desc = new JTextField(anDesc);
		desc.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
		descPanel.add(desc, BorderLayout.EAST);
		anPanel.add(descPanel);

		// attribute name class
		JPanel classPanel = new JPanel(new BorderLayout());
		classPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Class")), BorderLayout.WEST);
		classField = new JTextField(className);
		classField.setBackground(dialog.getRequiredColor());
		classField.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
		classField.setEnabled(editable);
		classPanel.add(classField, BorderLayout.EAST);
		anPanel.add(classPanel);

		// attribute name unit
		JPanel unitPanel = new JPanel(new BorderLayout());
		unitPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Unit")), BorderLayout.WEST);
		unit = dialog.makeUnit(selectedUnit);
		unitPanel.add(unit, BorderLayout.EAST);
		anPanel.add(unitPanel);

		// specialisation of
		JPanel specialisationOfPanel = new JPanel(new BorderLayout());
		specialisationOfPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.SpecialisationOf")), BorderLayout.WEST);
		specialisationOf = dialog.makeSpecialisationOf(selectedAn);
		specialisationOfPanel.add(specialisationOf, BorderLayout.EAST);
		anPanel.add(specialisationOfPanel);

		// if not editable add create and select AttributeName button
		if (editable) {
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(anPanel, BorderLayout.CENTER);

			JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleHide"));
			toggle.setActionCommand("toggleHide");
			toggle.addActionListener(this);
			panel.add(toggle, BorderLayout.SOUTH);
			this.add(panel, BorderLayout.NORTH);

			JPanel buttonPanel = new JPanel(new BorderLayout());

			// create AttributeName from entered data
			JButton create = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.Create"));
			create.setActionCommand("create");
			create.addActionListener(this);
			buttonPanel.add(create, BorderLayout.WEST);

			// select existing AttributeName
			JButton select = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.Select"));
			select.setActionCommand("select");
			select.addActionListener(this);
			buttonPanel.add(select, BorderLayout.EAST);

			anPanel.add(buttonPanel);

			// empty place holder
			indexPanel = null;
			editorScroll = new JScrollPane(new JTable());
			this.add(editorScroll, BorderLayout.CENTER);
		} else {
			JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleShow"));
			toggle.setActionCommand("toggleShow");
			toggle.addActionListener(this);
			this.add(toggle, BorderLayout.NORTH);

			// do index checkbox
			indexPanel = new JPanel(new BorderLayout());
			indexPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.DoIndex")), BorderLayout.WEST);
			doIndex = new JCheckBox();
			doIndex.setSelected(attribute.isDoIndex());
			doIndex.setActionCommand("index");
			doIndex.addActionListener(this);
			indexPanel.add(doIndex, BorderLayout.EAST);
			this.add(indexPanel, BorderLayout.SOUTH);

			// get editor for Attribute
			editor = findEditor(attribute);

			// add editor component
			if (editor != null) {
				editorScroll = new JScrollPane(editor);
				this.add(editorScroll, BorderLayout.CENTER);
			} else {
				// empty place holder
				String[] columnNames = { Config.language.getProperty("Dialog.Attribute.EditorError") };
				String[][] data = new String[][] { columnNames };
				editorScroll = new JScrollPane(new JTable(data, columnNames));
				this.add(editorScroll, BorderLayout.CENTER);
			}
		}
	}

	/**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
	private boolean validateEntry() {
		if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
			JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidID"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (aog.getMetaData().checkAttributeName(id.getText())) {
			JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.DuplicateID"), Config.language.getProperty("Dialog.Attribute.AttributeName.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (classField.getText().trim().length() == 0) {
			JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidClass"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// create new AttributeName
		if (cmd.equals("create") && validateEntry()) {
			try {
				AttributeName an = aog.getMetaData().getFactory().createAttributeName(id.getText(), full.getText(), desc.getText(), Class.forName(classField.getText()));

				if (!unit.getSelectedItem().equals("")) {
					Unit anUnit = aog.getMetaData().getUnit((String) unit.getSelectedItem());
					an.setUnit(anUnit);
				}
				if (!specialisationOf.getSelectedItem().equals("")) {
					AttributeName anAn = aog.getMetaData().getAttributeName((String) specialisationOf.getSelectedItem());
					an.setSpecialisationOf(anAn);
				}
				dialog.createNewAttribute(an, true);
			} catch (ClassNotFoundException cnfe) {
				JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidClass"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
			}
		}

		// show AttributeName selection dialog
		else if (cmd.equals("select")) {

			AttributeName[] ans = aog.getMetaData().getAttributeNames().toArray(new AttributeName[0]);
			Arrays.sort(ans, new Comparator<AttributeName>() {

				@Override
				public int compare(AttributeName o1, AttributeName o2) {
					return o1.toString().toUpperCase().compareTo(o2.toString().toUpperCase());
				}
			});

			AttributeName an = (AttributeName) JOptionPane.showInternalInputDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.SelectText"), Config.language.getProperty("Dialog.Attribute.AttributeName.SelectTitle"), JOptionPane.PLAIN_MESSAGE, dialog.getFrameIcon(), ans, null);

			// check if attribute returned.
			if (an != null) {
				dialog.createNewAttribute(an, false);
			}
		}

		// change index flag of Attribute
		else if (cmd.equals("index")) {
			attribute.setDoIndex(doIndex.isSelected());
		}

		// show advanced information panel
		else if (cmd.equals("toggleShow")) {
			this.removeAll();
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(anPanel, BorderLayout.CENTER);
			JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleHide"));
			toggle.setActionCommand("toggleHide");
			toggle.addActionListener(this);
			panel.add(toggle, BorderLayout.SOUTH);
			this.add(panel, BorderLayout.NORTH);
			this.add(editorScroll, BorderLayout.CENTER);
			if (indexPanel != null)
				this.add(indexPanel, BorderLayout.SOUTH);
			this.updateUI();
		}

		// hide advanced information panel
		else if (cmd.equals("toggleHide")) {
			this.removeAll();
			JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleShow"));
			toggle.setActionCommand("toggleShow");
			toggle.addActionListener(this);
			this.add(toggle, BorderLayout.NORTH);
			this.add(editorScroll, BorderLayout.CENTER);
			if (indexPanel != null)
				this.add(indexPanel, BorderLayout.SOUTH);
			this.updateUI();
		}
	}

	/**
	 * forces changes in progress to be flushed to attribute
	 */
	public void flushChanges() {
		((GDSEditor) editor).flushChanges();
	}

	/**
	 * Load GDSEditor for displaying purpose
	 * 
	 * @param attribute
	 */
	public static JComponent findEditor(Attribute attribute) {

		JComponent editor = null;

		// get attribute name
		AttributeName an = attribute.getOfType();
		String className = an.getDataTypeAsString();

		// select editor name first by attribute name
		String editorName = Config.config.getProperty("Dialog.Attribute.Editor." + attribute.getOfType().getId());

		// select editor name for class from Config
		if (editorName == null)
			editorName = Config.config.getProperty("Dialog.Attribute." + className);

		// no editor found
		if (editorName == null)
			editorName = "DefaultEditor";

		// find editor for class
		try {

			// check current class loader
			Class<?> editorClass = null;
			try {
				editorClass = Class.forName("net.sourceforge.ondex.ovtk2.ui.gds." + editorName);
			} catch (ClassNotFoundException cnfe) {
				// ignore, that is to be expected when running not as applet
			}

			if (editorClass == null) {
				// use plugin loader
				editor = (JComponent) OVTK2PluginLoader.getInstance().loadAttributeEditor(editorName, attribute);
			} else {
				// found on class path
				Class<?>[] args = new Class<?>[] { Attribute.class };
				Constructor<?> constr = editorClass.getConstructor(args);
				editor = (JComponent) constr.newInstance(attribute);
			}

			if (!(editor instanceof GDSEditor)) {
				throw new RuntimeException(editor.getClass().getName() + " does not implement required " + GDSEditor.class + " interface");
			}
			editor.setMinimumSize(new Dimension(100, 100));
			editor.setPreferredSize(new Dimension(100, 100));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return editor;
	}
}
