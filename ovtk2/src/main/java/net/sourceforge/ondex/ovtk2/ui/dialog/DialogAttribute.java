package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.marshal.Marshaller;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.gds.AttributePanel;
import net.sourceforge.ondex.ovtk2.ui.gds.ButtonTabComponent;
import net.sourceforge.ondex.ovtk2.ui.gds.GDSEditor;
import net.sourceforge.ondex.ovtk2.util.DesktopUtils.CaseInsensitiveMetaDataComparator;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;

/**
 * Showing table for view/edit of Attribute or Attribute.
 * 
 * @author taubertj
 */
public class DialogAttribute extends OVTK2Dialog {

	private static final String APPLY = "apply";

	private static final String CANCEL = "cancel";

	private static final String ADD = "add";

	private static final String UNIT = "unit";

	// generated
	private static final long serialVersionUID = 3645126832819276656L;

	// current AbstractConcept
	private ONDEXConcept ac = null;

	// current AbstractONDEXGraph
	private ONDEXGraph aog = null;

	// current ONDEXRelation
	private ONDEXRelation ar = null;

	// closeable tabbedPane
	private JTabbedPane tabbedPane = null;

	// keep track of new entries
	private AttributePanel newPanel = null;

	// contains all combo boxes for attribute names
	private Set<JComboBox> anBoxes = new HashSet<JComboBox>();

	// contains all combo boxes for units
	private Set<JComboBox> unitBoxes = new HashSet<JComboBox>();

	/**
	 * Constructs user input dialog to view or edit Attribute.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param ac
	 *            current AbstractConcept
	 */
	public DialogAttribute(ONDEXGraph aog, ONDEXConcept ac) {
		super("Dialog.Attribute.Title", "Properties16.gif");

		this.aog = aog;
		this.ac = ac;
		this.setPreferredSize(new Dimension(600, 500));
		initUI();
	}

	/**
	 * Constructs user input dialog to view or edit Attribute.
	 * 
	 * @param aog
	 *            AbstractONDEXGraph to add to
	 * @param ar
	 *            current ONDEXRelation
	 */
	public DialogAttribute(ONDEXGraph aog, ONDEXRelation ar) {
		super("Dialog.Attribute.Title", "Properties16.gif");

		this.aog = aog;
		this.ar = ar;

		initUI();
	}

	/**
	 * Set close button to given tab.
	 * 
	 * @param i
	 *            number of tab
	 */
	private void initTabComponent(int i) {
		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
	}

	/**
	 * Setup UI, calling subroutines.
	 */
	private void initUI() {
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.Attribute.Apply", "Dialog.Attribute.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Creates the properties panel for Attribute or Attribute.
	 * 
	 * @return JPanel
	 */
	private JPanel makeProperties() {

		// init properties layout
		JPanel properties = new JPanel(new BorderLayout());
		String title = null;
		if (ac != null)
			title = Config.language.getProperty("Dialog.Attribute.ConceptAttribute");
		if (ar != null)
			title = Config.language.getProperty("Dialog.Attribute.RelationAttribute");
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(title);
		properties.setBorder(propertiesBorder);

		// setup tabbed pane
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		properties.add(tabbedPane, BorderLayout.CENTER);

		// for Attribute
		if (ac != null) {
			Set<Attribute> attributes = ac.getAttributes();
			// if no attribute make new attribute panel
			if (attributes.size() == 0) {
				newPanel = new AttributePanel(this, aog, null);
				tabbedPane.addTab(Config.language.getProperty("Dialog.Attribute.NewAttributeHeader"), newPanel);
				tabbedPane.setSelectedComponent(newPanel);
			}
			// add one tab per Attribute
			Attribute[] array = attributes.toArray(new Attribute[0]);
			Arrays.sort(array);
			for (Attribute attribute : array) {
				tabbedPane.addTab(attribute.getOfType().getId(), new AttributePanel(this, aog, attribute));
			}
		}

		// for Attribute
		if (ar != null) {
			Set<Attribute> attributes = ar.getAttributes();
			// if no attribute make new attribute panel
			if (attributes.size() == 0) {
				newPanel = new AttributePanel(this, aog, null);
				tabbedPane.addTab(Config.language.getProperty("Dialog.Attribute.NewAttributeHeader"), newPanel);
				tabbedPane.setSelectedComponent(newPanel);
			}
			// add one tab per Attribute
			Attribute[] array = attributes.toArray(new Attribute[0]);
			Arrays.sort(array);
			for (Attribute attribute : array) {
				tabbedPane.addTab(attribute.getOfType().getId(), new AttributePanel(this, aog, attribute));
			}
		}

		// add delete button for tabs
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			initTabComponent(i);
		}

		// add new Attribute button
		JPanel addPanel = new JPanel(new BorderLayout());
		JButton addButton = new JButton(Config.language.getProperty("Dialog.Attribute.AddAttribute"));
		addButton.setActionCommand(ADD);
		addButton.addActionListener(this);
		addPanel.add(addButton, BorderLayout.CENTER);
		properties.add(addPanel, BorderLayout.SOUTH);

		return properties;
	}

	/**
	 * Updates existing Unit JComboBoxes.
	 * 
	 * @param add
	 *            Unit id to be added
	 */
	public void updateUnits(String add) {
		Iterator<JComboBox> it = unitBoxes.iterator();
		while (it.hasNext()) {
			it.next().addItem(add);
		}
	}

	/**
	 * Creates a combo box displaying available units.
	 * 
	 * @param select
	 *            Unit to be selected
	 * @return JComboBox
	 */
	public JComboBox makeUnit(Unit select) {
		Vector<String> units = new Vector<String>();
		units.add("");
		units.add(Config.language.getProperty("Dialog.Attribute.CreateUnit"));
		Unit[] sorted = aog.getMetaData().getUnits().toArray(new Unit[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (Unit u : sorted) {
			units.add(u.getId());
		}
		JComboBox unit = new JComboBox(units);
		unit.setActionCommand(UNIT);
		unit.addActionListener(this);
		if (select != null)
			unit.setSelectedItem(select.getId());
		unit.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		unitBoxes.add(unit);
		return unit;
	}

	/**
	 * Creates a combo box displaying available attribute names.
	 * 
	 * @param select
	 *            AttributeName to be selected
	 * @return JComboBox
	 */
	public JComboBox makeSpecialisationOf(AttributeName select) {
		Vector<String> ans = new Vector<String>();
		ans.add("");
		AttributeName[] sorted = aog.getMetaData().getAttributeNames().toArray(new AttributeName[0]);
		Arrays.sort(sorted, new CaseInsensitiveMetaDataComparator());
		for (AttributeName an : sorted) {
			ans.add(an.getId());
		}
		JComboBox an = new JComboBox(ans);
		if (select != null)
			an.setSelectedItem(select.getId());
		an.setPreferredSize(new Dimension(this.getFieldWidth(), this.getFieldHeight()));
		anBoxes.add(an);
		return an;
	}

	/**
	 * Updates combo boxes for attribute name and create a new Attribute panel.
	 * 
	 * @param an
	 *            AttributeName
	 * @param newAn
	 *            whether it is a new AttributeName
	 */
	public void createNewAttribute(AttributeName an, boolean newAn) {
		// add to all AttributeName comboBoxes if new
		if (newAn) {
			Iterator<JComboBox> it = anBoxes.iterator();
			while (it.hasNext()) {
				it.next().addItem(an.getId());
			}
		}

		// remove the new panel
		tabbedPane.removeTabAt(tabbedPane.indexOfComponent(newPanel));
		newPanel = null;

		// get class of Attribute
		GDSEditor editor = null;

		// select editor name from Config
		String editorName = Config.config.getProperty("Dialog.Attribute." + an.getDataTypeAsString());
		if (editorName == null)
			editorName = "DefaultEditor";

		// find editor for class
		try {
			editor = OVTK2PluginLoader.getInstance().loadAttributeEditor(editorName, null);
			if (!(editor instanceof GDSEditor)) {
				throw new RuntimeException(editor.getClass().getName() + " does not implement required " + GDSEditor.class + " interface");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// for Attribute
		if (ac != null) {
			Object o = null;

			// if editor found get default Attribute value
			if (editor != null && editor.getDefaultValue() != null) {
				o = editor.getDefaultValue();
			} else { // ask for XStream xml code
				String xml = (String) JOptionPane.showInternalInputDialog(this, Config.language.getProperty("Dialog.Attribute.XStreamText"), Config.language.getProperty("Dialog.Attribute.XStreamTitle"), JOptionPane.PLAIN_MESSAGE, this.getFrameIcon(), null, null);
				if ((xml != null) && (xml.length() > 0)) {
					try {
						o = Marshaller.getMarshaller().fromXML(xml);
					} catch (com.thoughtworks.xstream.mapper.CannotResolveClassException crce) {
						ErrorDialog.show(crce);
					} catch (com.thoughtworks.xstream.converters.ConversionException ce) {
						ErrorDialog.show(ce);
					}
				}
			}

			// create Attribute
			if (o != null) {
				Attribute attribute = ac.createAttribute(an, o, false);
				AttributePanel panel = new AttributePanel(this, aog, attribute);
				tabbedPane.addTab(an.getId(), panel);
				tabbedPane.setSelectedComponent(panel);
				initTabComponent(tabbedPane.indexOfComponent(panel));
			}
		}

		// for Attribute
		if (ar != null) {
			Object o = null;

			// if editor found get default Attribute value
			if (editor != null && editor.getDefaultValue() != null) {
				o = editor.getDefaultValue();
			} else { // ask for XStream xml code
				String xml = (String) JOptionPane.showInternalInputDialog(this, Config.language.getProperty("Dialog.Attribute.XStreamText"), Config.language.getProperty("Dialog.Attribute.XStreamTitle"), JOptionPane.PLAIN_MESSAGE, this.getFrameIcon(), null, null);
				if ((xml != null) && (xml.length() > 0)) {
					o = Marshaller.getMarshaller().fromXML(xml);
				}
			}

			// create Attribute
			if (o != null) {
				Attribute attribute = ar.createAttribute(an, o, false);
				AttributePanel panel = new AttributePanel(this, aog, attribute);
				tabbedPane.addTab(an.getId(), panel);
				tabbedPane.setSelectedComponent(panel);
				initTabComponent(tabbedPane.indexOfComponent(panel));
			}
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// sync Attribute
		if (cmd.equals(APPLY)) {
			// contains current view on attributes
			HashSet<AttributeName> modified = new HashSet<AttributeName>();
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				AttributePanel panel = (AttributePanel) tabbedPane.getComponentAt(i);
				panel.flushChanges();
				// sync AttributeName in ONDEX meta data
				String id = panel.getID();
				AttributeName an = aog.getMetaData().getAttributeName(id);
				an.setDescription(panel.getDescription());
				an.setFullname(panel.getFullname());
				if (panel.getSpecialisationOf().length() > 0) {
					an.setSpecialisationOf(aog.getMetaData().getAttributeName(panel.getSpecialisationOf()));
				}
				if (panel.getUnit().length() > 0) {
					an.setUnit(aog.getMetaData().getUnit(panel.getUnit()));
				}
				modified.add(an);
			}
			// sync AbstractConcept
			if (ac != null) {
				// contains Attribute to be deleted
				HashSet<AttributeName> delete = new HashSet<AttributeName>();
				for (Attribute attribute : ac.getAttributes()) {
					if (!modified.contains(attribute.getOfType())) {
						delete.add(attribute.getOfType());
					}
				}
				// delete Attribute from concept
				for (AttributeName aDelete : delete) {
					ac.deleteAttribute(aDelete);
				}
			}
			// sync ONDEXRelation
			if (ar != null) {
				// contains Attribute to be deleted
				HashSet<AttributeName> delete = new HashSet<AttributeName>();
				for (Attribute attribute : ar.getAttributes()) {
					if (!modified.contains(attribute.getOfType())) {
						delete.add(attribute.getOfType());
					}
				}
				// delete Attribute from relation
				for (AttributeName aDelete : delete) {
					ar.deleteAttribute(aDelete);
				}
			}
			try {
				this.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
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

		// add a new Attribute panel if not already exists
		else if (cmd.equals(ADD)) {
			if (newPanel == null) {
				newPanel = new AttributePanel(this, aog, null);
				tabbedPane.addTab(Config.language.getProperty("Dialog.Attribute.NewAttributeHeader"), newPanel);
				tabbedPane.setSelectedComponent(newPanel);
			}
		}

		// add a new Unit
		else if (cmd.equals(UNIT)) {
			JComboBox cb = (JComboBox) arg0.getSource();
			String selection = (String) cb.getSelectedItem();
			if (selection != null && selection.equals(Config.language.getProperty("Dialog.Attribute.CreateUnit"))) {
				DialogUnit dialog = new DialogUnit(aog, this, cb);
				displayCentered(dialog);
			}
		}
	}
}
