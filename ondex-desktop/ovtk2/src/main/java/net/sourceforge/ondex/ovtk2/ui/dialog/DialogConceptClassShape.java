package net.sourceforge.ondex.ovtk2.ui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Dialog;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeComboBoxRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ShapeTableModel;
import net.sourceforge.ondex.ovtk2.util.DeleteCellEditor;

/**
 * Showing table for view/edit of mapping cc to shape.
 * 
 * @author taubertj
 * @version 10.07.2008
 */
public class DialogConceptClassShape extends OVTK2Dialog {

	// generated
	private static final long serialVersionUID = -5761400009664245191L;

	private static final String SHAPE_VIS = "ConceptClass.Shape.";

	// model containing mapping cc to shape
	private ShapeTableModel model = null;

	// table displaying choices
	private JTable table = null;

	/**
	 * Constructs user input to view/edit mapping cc to shape.
	 * 
	 * @param selection
	 *            pre-selection in table
	 */
	public DialogConceptClassShape(String selection) {
		super("Dialog.CcShape.Title", "Properties16.gif");
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(makeProperties(selection), BorderLayout.CENTER);
		this.getContentPane().add(makeButtonsPanel("Dialog.CcShape.Apply", "Dialog.CcShape.Cancel"), BorderLayout.SOUTH);
		this.pack();
	}

	/**
	 * Constructs user input to view/edit mapping cc to shape.
	 * 
	 */
	public DialogConceptClassShape() {
		this(null);
	}

	/**
	 * Creates the properties panel for shapes.
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
		TitledBorder propertiesBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.CcShape.ConceptClasses"));
		properties.setBorder(propertiesBorder);

		// get shapes from visual.xml
		Hashtable<String, Shape> shapes = new Hashtable<String, Shape>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(SHAPE_VIS)) {
				int id = Integer.parseInt(Config.visual.getProperty(name));
				name = name.substring(SHAPE_VIS.length(), name.length());
				shapes.put(name, ONDEXNodeShapes.getShape(id));
			}
		}

		// setup table
		model = new ShapeTableModel(shapes, Config.language.getProperty("Dialog.CcShape.TableName"), Config.language.getProperty("Dialog.CcShape.TableShape"));
		table = new JTable(model);
		Shape shape = shapes.values().iterator().next();
		Rectangle2D bounds = shape.getBounds2D();
		table.setRowHeight((int) bounds.getHeight() + 4);
		table.setDefaultRenderer(Shape.class, new ShapeTableCellRenderer(true));

		// set selected item in table
		setSelection(selection);

		TableColumn shapecol = table.getColumnModel().getColumn(1);
		int width = Config.language.getProperty("Dialog.CcShape.TableShape").length() * 9;
		shapecol.setMaxWidth(width);
		shapecol.setMinWidth(width);
		JComboBox comboBox = new JComboBox();
		comboBox.setRenderer(new ShapeComboBoxRenderer());
		for (Shape s : ONDEXNodeShapes.getAvailableShapes()) {
			comboBox.addItem(s);
		}
		shapecol.setCellEditor(new DefaultCellEditor(comboBox));

		TableColumn delete = table.getColumnModel().getColumn(2);
		delete.setMaxWidth(20);
		delete.setMinWidth(20);
		delete.setCellEditor(new DeleteCellEditor<String, Shape>(shapes));

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
		// select a certain concept class
		int index = -1;
		if (selection != null) {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.getValueAt(i, 0).equals(selection))
					index = i;
			}
			if (index > -1)
				table.setRowSelectionInterval(index, index);

			// if concept class not found, insert it
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

	public void setSelection(String selection, Shape value) {
		// select a certain RT
		int index = setSelection(selection);
		if (index > -1) {
			table.setValueAt(value, index, 1);
			perform();
		}
	}

	/**
	 * Performs saving of shape associated with concept class
	 */
	private void perform() {
		// get existing shapes
		Hashtable<String, Shape> existing = new Hashtable<String, Shape>();
		for (Object key : Config.visual.keySet()) {
			String name = (String) key;
			if (name.startsWith(SHAPE_VIS)) {
				int id = Integer.parseInt(Config.visual.getProperty(name));
				name = name.substring(SHAPE_VIS.length(), name.length());
				existing.put(name, ONDEXNodeShapes.getShape(id));
			}
		}
		Hashtable<String, Shape> shapes = model.getData();
		// check for positive changes
		Iterator<String> it = shapes.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (name.trim().length() > 0) {
				// concept class name not existing
				if (!existing.containsKey(name)) {
					// new mapping here
					Config.visual.put(SHAPE_VIS + name, ONDEXNodeShapes.getId(shapes.get(name)).toString());
					existing.put(name, shapes.get(name));
				}
				// shape different
				else if (!existing.get(name).equals(shapes.get(name))) {
					// change shape here
					Config.visual.put(SHAPE_VIS + name, ONDEXNodeShapes.getId(shapes.get(name)).toString());
				}
			}
		}
		// check for deletions
		it = existing.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			if (!shapes.containsKey(name)) {
				// delete name here
				Config.visual.remove(SHAPE_VIS + name);
			}
		}
		Config.saveVisual();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// sync shapes
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
