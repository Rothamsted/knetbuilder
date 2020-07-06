package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Shape;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;

/**
 * Wraps the hashtable of id to shape into a table model.
 * 
 * @author taubertj
 * 
 */
public class ShapeTableModel extends AbstractTableModel {

	// generated
	private static final long serialVersionUID = 3996657642862804984L;

	// table header
	private String[] columnNames = new String[] { Config.language.getProperty("Dialog.CcShape.TableName"), Config.language.getProperty("Dialog.CcShape.TableShape"), "" };

	// contains id to shape mapping
	private Hashtable<String, Shape> shapes = null;

	// show icon in last column
	private ImageIcon icon = null;

	/**
	 * Constructor for a given mapping id to shape.
	 * 
	 * @param shapes
	 *            Hashtable<String, Shape>
	 */
	public ShapeTableModel(Hashtable<String, Shape> shapes, String tableName, String idType) {
		this.shapes = shapes;
		this.columnNames = new String[] { tableName, idType, "" };
		File imgLocation = new File("config/toolbarButtonGraphics/general/delete16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}
		icon = new ImageIcon(imageURL);
	}

	public Hashtable<String, Shape> getData() {
		return shapes;
	}

	public int getRowCount() {
		return shapes.size() + 1;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int row, int col) {
		String[] keys = shapes.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		// return new row elements
		if (row == shapes.size()) {
			if (col == 0) {
				return "";
			} else if (col == 1) {
				return ONDEXNodeShapes.getShape(Config.defaultShape);
			} else if (col == 2) {
				return icon;
			}
		}
		// return existing data
		if (col == 0) {
			return keys[row];
		} else if (col == 1) {
			return shapes.get(keys[row]);
		} else if (col == 2) {
			return icon;
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0) {
			return String.class;
		} else if (col == 1) {
			return Shape.class;
		} else if (col == 2) {
			return ImageIcon.class;
		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (row < shapes.size()) {
			// change to name itself
			if (col == 0) {
				String[] keys = shapes.keySet().toArray(new String[0]);
				Arrays.sort(keys);
				String key = keys[row];
				Shape shape = shapes.get(key);
				shapes.remove(key);
				shapes.put((String) value, shape);
				this.fireTableDataChanged();
			}
			// change to shape
			else if (col == 1) {
				String[] keys = shapes.keySet().toArray(new String[0]);
				Arrays.sort(keys);
				String key = keys[row];
				shapes.put(key, (Shape) value);
				this.fireTableDataChanged();
			}
		} else {
			// add new row
			if (col == 0) {
				shapes.put((String) value, ONDEXNodeShapes.getShape(Config.defaultShape));
				this.fireTableRowsInserted(row, row);
				this.fireTableDataChanged();
			}
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}
}
