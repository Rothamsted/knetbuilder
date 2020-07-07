package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * Implementation of a sortable table model.
 * 
 * @author taubertj, hindlem
 * TODO: add remove row and col function
 */
public class ResultTableModel extends AbstractTableModel {
	
	/**
	 * Default serialisation unique id.
	 */
	private static final long serialVersionUID = 1L;
	
	// histogram breaks
	protected double[] breaks = null;
	
	// column names
	private Object[] columnNames;

	// data entries
	protected Object[][] data;

	// debug messages?
	boolean DEBUG = false;

	/**
	 * Creates a new empty table with no data
	 */
	public ResultTableModel() {
		this(new Object[0][0], new String[0]);
	}
	
	/**
	 * Constructs a table model for given data and columnNames.
	 * 
	 * @param data - Object[][]
	 * @param columnNames - Object[]
	 */
	public ResultTableModel(Object[][] data, Object[] columnNames) {
		// get the content
		this.columnNames = columnNames;
		this.data = data;
	}

	/**
	 * adds a Column to the values
	 * @param colValues if size is greater than number of existing rows then rows > will be ignored
	 * @param columnName the new label for this col
	 */
	public void addColumn(Object[] colValues, Object columnTitle) {
		Object[] newColNames = new Object[columnNames.length+1];
		System.arraycopy(columnNames, 0, newColNames, 0, columnNames.length);
		newColNames[columnNames.length] = columnTitle;
		columnNames = newColNames;
	
		for (int i = 0; i < data.length; i++) {
			Object[] row = data[i];
			Object[] newrow = new Object[row.length+1];
			System.arraycopy(row, 0, newrow, 0, row.length);
			newrow[row.length] = colValues[i];
			data[i] = newrow;
		}
		
	}
	
	/**
	 * Adds a new row to the data model
	 * @param rowValues the row
	 */
	public void addRow(Object[] rowValues) {
		Object[][] newData = new Object[data.length+1][];
		System.arraycopy(data, 0, newData, 0, data.length);
		newData[data.length] = rowValues;
	}
	
	/**
	 * Function for sorting table according to a specified column.
	 * 
	 * @param columnName - Object
	 * @param inverse - boolean
	 */
	public void sort(String columnName, boolean inverse) {

		// get column number for column name
		int column = 0;
		for (int i=0; i < columnNames.length; i++) {
			if (columnNames[i].toString().equals(columnName)) {
				column = i;
				break;
			}
		}
		
		// get all rows, that share the same sorting term
		Hashtable<Object,Vector<Integer>> tmp = new Hashtable<Object,Vector<Integer>>();
		for (int i = 0; i < data.length; i++) {
			Object item = getValueAt(i,column);
			if (!tmp.containsKey(item)) {
				Vector<Integer> v = new Vector<Integer>();
				v.add(new Integer(i));
				tmp.put(item, v);
			} else {
				Vector<Integer> v = tmp.get(item);
				v.add(new Integer(i));
				tmp.remove(item);
				tmp.put(item, v);
			}
		}

		// perform simple array sort on sorting terms
		Object[] a = tmp.keySet().toArray();
		Arrays.sort(a);
		if (inverse) {
			List<?> l = Arrays.asList(a);
			Collections.reverse(l);
			a = l.toArray();
		}
		
		// build new order of row in data
		int oldpos, newpos;
		Object[][] newdata = new Object[data.length][];
		newpos = 0;
		for (int i = 0; i < a.length; i++) {
			Vector<Integer> v = tmp.get(a[i]);
			for (int j = 0; j < v.size(); j++) {
				oldpos = v.get(j);
				newdata[newpos] = data[oldpos];
				newpos++;
			}
		}
		
		// set sorted data
		data = newdata;
	}

	/**
	 * Returns the table header.
	 * 
	 * @return Object[]
	 */
	public Object[] getColumnNames() {
		return columnNames;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}
	
	/**
	 * Returns number of columns.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex].toString();
	}

	/**
	 * Returns number of rows.
	 */
	public int getRowCount() {
		return data.length;
	}

	/**
	 * Returns object located at specified row and column.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < data.length){
			Object[] array = data[rowIndex];
			if (array != null && columnIndex < array.length) {
				Object obj = array[columnIndex];
				if (obj != null) {
					return obj;
				}
			}
		}
		return "";
	}
	
	/**
	 * Sets object at specified row and column.
	 */
	@Override
	public void setValueAt(Object obj, int rowIndex, int columnIndex) {
		if (rowIndex < data.length){
			Object[] array = data[rowIndex];
			if (array != null && columnIndex < array.length) {
				array[columnIndex] = obj;
			}
		}
	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
}

