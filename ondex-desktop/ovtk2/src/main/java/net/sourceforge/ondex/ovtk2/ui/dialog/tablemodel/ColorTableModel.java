package net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel;

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.ondex.ovtk2.config.Config;

/**
 * Wraps the hashtable of mapping id to color into a table model.
 * 
 * @author taubertj
 * 
 */
public class ColorTableModel extends AbstractTableModel {

	// generated
	private static final long serialVersionUID = -7705393633687165764L;

	// table header
	private String[] columnNames;

	// contains id to color mapping
	private Hashtable<String, Color> colors = null;

	// show icon in last column
	private ImageIcon icon = null;

	/**
	 * Constructor for a given mapping the id to the color.
	 * 
	 * @param colors
	 *            Hashtable<String, Color>
	 */
	public ColorTableModel(Hashtable<String, Color> colors, String tableName, String idType) {
		this.colors = colors;
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

	public Hashtable<String, Color> getData() {
		return colors;
	}

	public int getRowCount() {
		return colors.size() + 1;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int row, int col) {
		String[] keys = colors.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		// return new row elements
		if (row == colors.size()) {
			if (col == 0) {
				return "";
			} else if (col == 1) {
				return Config.defaultColor;
			} else if (col == 2) {
				return icon;
			}
		}
		// return existing data
		if (col == 0) {
			return keys[row];
		} else if (col == 1) {
			return colors.get(keys[row]);
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
			return Color.class;
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
		if (row < colors.size()) {
			// change to name itself
			if (col == 0) {
				String[] keys = colors.keySet().toArray(new String[0]);
				Arrays.sort(keys);
				String key = keys[row];
				Color color = colors.get(key);
				colors.remove(key);
				colors.put((String) value, color);
				this.fireTableDataChanged();
			}
			// change to color
			else if (col == 1) {
				String[] keys = colors.keySet().toArray(new String[0]);
				Arrays.sort(keys);
				String key = keys[row];
				colors.put(key, (Color) value);
				this.fireTableDataChanged();
			}
		} else {
			// add new row
			if (col == 0) {
				colors.put((String) value, Config.defaultColor);
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
