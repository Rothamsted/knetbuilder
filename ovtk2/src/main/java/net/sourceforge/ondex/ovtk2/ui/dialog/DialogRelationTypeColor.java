package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableModel;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableEditor;
import net.sourceforge.ondex.ovtk2.util.DeleteCellEditor;

/**
 * Showing table for view/edit of mapping RT to colour.
 * 
 * @author taubertj
 * @version 10.07.2008
 */
public class DialogRelationTypeColor extends OVTK2Dialog {

	// generated
	private static final long serialVersionUID = 3736855620784360109L;

	// model containing mapping RT to colour mapping
	private ColorTableModel model = null;

	// table displaying choices
	private JTable table = null;

	private static final String COLOR_VIS = "RelationType.Color.";

	/**
	 * Constructs user input to view/edit mapping RT to colour.
	 * 
	 * @param selection
	 *            pre-selection in table
	 */
	public DialogRelationTypeColor(String selection) {
		super("Dialog.RtColor.Title", "Properties16.gif");
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(selection), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.RtColor.Apply", "Dialog.RtColor.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to view/edit mapping RT to colour.
	 * 
	 */
	public DialogRelationTypeColor() {
		this(null);
	}

	/**
	 * Creates the properties panel for mapping RT to colour.
	 * 
	 * @param selection
	 *            what is highlighted
	 * @return JPanel
	 */
	private JPanel makeProperties(String selection) {

		// init properties layout
		JPanel properties = new JPanel();
		BoxLayout contentLayout = new BoxLayout(properties, BoxLayout.PAGE_AXIS);
		properties.setLayout(contentLayout);
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.RtColor.RT"));
		properties.setBorder(propertiesBorder);

		// get mapping to colour from visual.xml
		Hashtable<String, Color> colors = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(COLOR_VIS.length(), name.length());
				colors.put(name, Config.convertToColor(color));
			}
		}

		// setup table
		model = new ColorTableModel(colors, Config.language.getProperty("Dialog.RtColor.TableName"), Config.language.getProperty("Dialog.RtColor.TableColor"));
		table = new JTable(model);
		table.setDefaultEditor(Color.class, new ColorTableEditor());
		table.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));

		// set selected item in table
		setSelection(selection);

		TableColumn color = table.getColumnModel().getColumn(1);
		int width = Config.language.getProperty("Dialog.RtColor.TableColor").length() * 7;
		color.setMaxWidth(width);
		color.setMinWidth(width);

		TableColumn delete = table.getColumnModel().getColumn(2);
		delete.setMaxWidth(20);
		delete.setMinWidth(20);
		delete.setCellEditor(new DeleteCellEditor<String, Color>(colors));

		// add table to properties
		JScrollPane scrollPane = new JScrollPane(table);
		JPanel tablePanel = new JPanel(new GridLayout(1, 1));
		tablePanel.add(scrollPane);
		properties.add(tablePanel);

		return properties;
	}

	/**
	 * Sets selection in table to given item name. Inserts item if name doesn't
	 * exist.
	 * 
	 * @param selection
	 *            item name
	 */
	public int setSelection(String selection) {
		// select a certain RT
		int index = -1;
		if (selection != null) {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.getValueAt(i, 0).equals(selection))
					index = i;
			}
			if (index > -1)
				table.setRowSelectionInterval(index, index);

			// if RT not found, insert it
			else {
				index = table.getRowCount() - 1;
				table.setValueAt(selection, index, 0);
				for (int i = 0; i < table.getRowCount(); i++) {
					if (table.getValueAt(i, 0).equals(selection))
						index = i;
				}
				table.setRowSelectionInterval(index, index);
			}
		}
		return index;
	}

	public void setSelection(String selection, Color value) {
		// select a certain RT
		int index = setSelection(selection);
		if (index > -1) {
			table.setValueAt(value, index, 1);
			perform();
		}
	}

	/**
	 * Performs saving of colour associated with RT
	 */
	private void perform() {
		// get existing colours
		Hashtable<String, Color> existing = new Hashtable<String, Color>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(COLOR_VIS)) {
				String color = Config.visual.getProperty(name);
				name = name.substring(COLOR_VIS.length(), name.length());
				existing.put(name, Config.convertToColor(color));
			}
		}
		Hashtable<String, Color> colors = model.getData();
		// check for positive changes
		Iterator<String> it = colors.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (name.trim().length() > 0) {
				// RT name not existing
				if (!existing.containsKey(name)) {
					// new mapping here
					Config.visual.put(COLOR_VIS + name, Config.convertToString(colors.get(name)));
					existing.put(name, colors.get(name));
				}
				// colour different
				else if (!existing.get(name).equals(colors.get(name))) {
					// change colour here
					Config.visual.put(COLOR_VIS + name, Config.convertToString(colors.get(name)));
				}
			}
		}
		// check for deletions
		it = existing.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (!colors.containsKey(name)) {
				// delete name here
				Config.visual.remove(COLOR_VIS + name);
			}
		}
		Config.saveVisual();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// sync colours
		if (cmd.equals("apply")) {
			perform();
			this.setVisible(false);
		}

		// cancel dialog
		else if (cmd.equals("cancel")) {
			this.setVisible(false);
		}
	}

}
