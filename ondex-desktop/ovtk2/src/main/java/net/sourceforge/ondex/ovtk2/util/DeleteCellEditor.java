package net.sourceforge.ondex.ovtk2.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

/**
 * Implements the table delete button.
 * 
 * @author taubertj
 * 
 */
public class DeleteCellEditor<K extends Comparable<?>, E> extends AbstractCellEditor implements TableCellEditor, ActionListener {

	// generated
	private static final long serialVersionUID = 8483654515815820627L;

	// contains key to element mapping
	private Map<K, E> mapping = null;

	// current table
	private JTable table;

	// button with icon on
	private JButton button;

	/**
	 * Returns a delete button as editor.
	 * 
	 * @param mapping
	 *            Map<K, E>
	 */
	public DeleteCellEditor(Map<K, E> mapping) {
		this.mapping = mapping;
		File imgLocation = new File("config/toolbarButtonGraphics/general/delete16.gif");
		URL imageURL = null;

		try {
			imageURL = imgLocation.toURI().toURL();
		} catch (MalformedURLException mue) {
			System.err.println(mue.getMessage());
		}

		button = new JButton(new ImageIcon(imageURL));
		button.addActionListener(this);
		button.setBorderPainted(false);
	}

	public Object getCellEditorValue() {
		return button.getIcon();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();

		// remove key
		Object[] keys = mapping.keySet().toArray();
		Arrays.sort(keys);
		int row = Integer.parseInt(cmd);
		if (row < keys.length) {
			mapping.remove(keys[row]);
			((AbstractTableModel) table.getModel()).fireTableRowsDeleted(row, row);
			((AbstractTableModel) table.getModel()).fireTableDataChanged();
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.table = table;
		button.setActionCommand("" + row);
		return button;
	}

}
