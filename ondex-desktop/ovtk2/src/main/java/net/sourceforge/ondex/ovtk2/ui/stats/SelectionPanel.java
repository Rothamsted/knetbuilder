package net.sourceforge.ondex.ovtk2.ui.stats;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;

/**
 * This class represents the panel that is displayed on the right hand side of
 * the statistics window. It provides two main components:
 * <ul>
 * <li>The variable selection field, which holds the element that was selected
 * to serve as the statistical variable</li>
 * <li>The filter table, which holds in each row one filter element consisting
 * of a selected element and potentially a value. Values are only evaluated for
 * attribute names.</li>
 * </ul>
 * The class also provides methods to set, add and pop elements from both the
 * variable field and the filter table.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class SelectionPanel extends JPanel {

	// ####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 7898905130619651480L;

	/**
	 * the textfield that holds the selected variable.
	 */
	private JTextField variable;

	/**
	 * the table that holds the selected filters.
	 */
	private JTable filters;

	/**
	 * the table model for the filter table.
	 */
	private FilterTableModel model;

	/**
	 * the graph.
	 */
	private ONDEXGraph aog;

	/**
	 * this is the action listener from the main statistics panel. It only
	 * serves the purpose to trigger a recalculation of all statistics once the
	 * value in a filter statement was changed.
	 */
	private ActionListener al;

	// ####CONSTRUCTOR####

	/**
	 * the constructor. sets the graph and the actionlistener and then
	 * initializes the graphics.
	 */
	public SelectionPanel(ONDEXGraph aog, ActionListener al) {
		this.aog = aog;
		this.al = al;

		setupPanel();
	}

	// ####METHODS####

	/**
	 * english
	 */
	private String engl(String s) {
		return Config.language.getProperty("Statistics." + s);
	}

	/**
	 * lays out the panel.
	 */
	private void setupPanel() {
		variable = new JTextField();
		variable.setEditable(false);
		variable.setColumns(20);

		model = new FilterTableModel();
		filters = new JTable(model);
		filters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		filters.setRowSelectionAllowed(true);
		filters.setColumnSelectionAllowed(false);

		JPanel varPanel = new JPanel();
		varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.PAGE_AXIS));
		varPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), engl("Variable")));
		varPanel.add(variable);

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
		filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), engl("Filters")));
		filterPanel.add(new JScrollPane(filters));

		setLayout(new BorderLayout());
		add(varPanel, BorderLayout.NORTH);
		add(filterPanel, BorderLayout.CENTER);
	}

	/**
	 * @return whether the variable is set or not.
	 */
	public boolean isVariableSet() {
		return (variable.getText() != null && !variable.getText().trim().equals(""));
	}

	/**
	 * sets the given object as the new variable.
	 * 
	 * @param o
	 *            either a ConceptClass or an AttributeName or a RelationType or
	 *            a String.
	 */
	public void setVariable(Object o) {
		if (o instanceof AttributeName) {
			AttributeName an = (AttributeName) o;
			if (an.getFullname() == null || an.getFullname().trim().equals(""))
				variable.setText(an.getId());
			else
				variable.setText(an.getFullname());
			variable.setName(an.getId());
			variable.setToolTipText("Attribute");
		} else if (o instanceof ConceptClass) {
			ConceptClass cc = (ConceptClass) o;
			if (cc.getFullname() == null || cc.getFullname().equals(""))
				variable.setText(cc.getId());
			else
				variable.setText(cc.getFullname());
			variable.setName(cc.getId());
			variable.setToolTipText("Concept class");
		} else if (o instanceof RelationType) {
			RelationType rt = (RelationType) o;
			if (rt.getFullname() == null || rt.getFullname().equals(""))
				variable.setText(rt.getId());
			else
				variable.setText(rt.getFullname());
			variable.setName(rt.getId());
			variable.setToolTipText("Relation type");
		} else if (o instanceof String) {
			String s = (String) o;
			variable.setText(s);
			variable.setName(s);
			variable.setToolTipText("Feature");
		}
	}

	/**
	 * @return returns the element that is currently selected as the variable.
	 */
	public Object getVariable() {
		String id = variable.getName();
		String type = variable.getToolTipText();
		if (type.equals("Attribute")) {
			return aog.getMetaData().getAttributeName(id);
		} else if (type.equals("Concept class")) {
			return aog.getMetaData().getConceptClass(id);
		} else if (type.equals("Relation type")) {
			return aog.getMetaData().getRelationType(id);
		} else if (type.equals("Feature")) {
			return id;
		} else {
			return null;
		}
	}

	/**
	 * pops (i.e. removes and returns) the current variable.
	 * 
	 * @return the current variable.
	 */
	public Object popVariable() {
		Object out = getVariable();
		variable.setText("");
		variable.setName("");
		variable.setToolTipText("");
		return out;
	}

	/**
	 * adds a filter to the main filter table
	 * 
	 * @param filter
	 *            either a ConceptClass or a RelationType or an AttributeName
	 */
	public void addFilter(Object filter) {
		model.addFilter(filter);
		filters.clearSelection();
		filters.revalidate();
		filters.repaint();
	}

	/**
	 * @return whether a filter is currently selected or not.
	 */
	public boolean isFilterSelected() {
		return filters.getSelectedRow() != -1;
	}

	/**
	 * pops (i.e. removes and returns) the currently selected filter.
	 * 
	 * @return the filter element.
	 */
	public Object popSelectedFilter() {
		int row = filters.getSelectedRow();
		if (row > -1) {
			filters.clearSelection();
			Object o = model.popElementAtRow(row);
			filters.revalidate();
			filters.repaint();
			return o;
		} else
			return null;
	}

	/**
	 * @return all currently listed filters from the table.
	 */
	public HashMap<Object, Object> getFilters() {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		Object[] filter;
		for (int i = 0; i < model.getRowCount(); i++) {
			filter = model.getFilterAtRow(i);
			map.put(filter[0], filter[1]);
		}
		return map;
	}

	/**
	 * This class is a custom table model that is capable of storing objects of
	 * types ConceptClass, RelationType, AttributeName and Strings. It is
	 * extended with custom methods to return whole rows (which represent filter
	 * statements), removeRows, pop elements, and add filter statements. It
	 * performs an automatic extension to the underlying array when necessary.
	 * 
	 * 
	 * @author Jochen Weile, B.Sc.
	 * 
	 */
	private class FilterTableModel extends AbstractTableModel {

		/**
		 * serial id.
		 */
		private static final long serialVersionUID = 4752351469281796635L;

		/**
		 * the underlying data table.
		 */
		private Object[][] rowData = new Object[20][2];

		/**
		 * the names of the columns in the table.
		 */
		private String[] columnNames = { engl("Element"), engl("Value") };

		/**
		 * the number of currently used rows in the table.
		 */
		private int rowcount = 0;

		/**
		 * returns the name of the column with the given index.
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		/**
		 * returns the current number of used rows.
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return rowcount;
		}

		/**
		 * gets the current number of columns.
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * returns the value that is stored in the table for the given
		 * coordinates (row and column).
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			Object o = rowData[row][col];
			if (o instanceof AttributeName) {
				AttributeName an = (AttributeName) o;
				if (an.getFullname() == null || an.getFullname().trim().equals(""))
					return an.getId();
				else
					return an.getFullname();
			} else if (o instanceof ConceptClass) {
				ConceptClass cc = (ConceptClass) o;
				if (cc.getFullname() == null || cc.getFullname().trim().equals(""))
					return cc.getId();
				else
					return cc.getFullname();
			} else if (o instanceof RelationType) {
				RelationType rt = (RelationType) o;
				if (rt.getFullname() == null || rt.getFullname().trim().equals(""))
					return rt.getId();
				else
					return rt.getFullname();
			} else
				return o;
		}

		/**
		 * returns the filter element that is stored in row of the given index.
		 * For safety reasons it returns a copy instead of a pointer to the raw
		 * data.
		 * 
		 * @param row
		 *            the index of the row to query.
		 * @return an Object[] array that holds either a ConceptClass,
		 *         RelationType, String, or AttributeName in its first slot; and
		 *         the potential Attribute value in its second slot. If no value
		 *         is specified by the user, the second slot is empty.
		 */
		public Object[] getFilterAtRow(int row) {
			Object[] out = new Object[2];
			out[0] = rowData[row][0];
			out[1] = rowData[row][1];
			return out;
		}

		/**
		 * removes one row from the data table all rows with higher indexes will
		 * move up one position.
		 * 
		 * @param row
		 *            the index of the row to delete.
		 */
		private void removeRow(int row) {
			if (row < rowcount - 1) {
				for (int i = row + 1; i < rowcount; i++) {
					rowData[i - 1][0] = rowData[i][0];
					rowData[i - 1][1] = rowData[i][1];
				}
			}
			if (row == rowcount - 1) {
				rowData[row][0] = null;
				rowData[row][1] = null;
			} else {
				throw new ArrayIndexOutOfBoundsException(row);
			}
			rowcount--;
			fireTableDataChanged();
		}

		/**
		 * pops (i.e. removes and returns) content of the row with the given
		 * index.
		 * 
		 * @param row
		 *            the index of the row.
		 * @return the element (Object[] array of size 2).
		 */
		public Object popElementAtRow(int row) {
			Object[] filter = getFilterAtRow(row);
			removeRow(row);
			return filter[0];
		}

		/**
		 * returns whether the selected cell is editable or not.
		 * 
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}

		/**
		 * 
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
		 *      int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			if (row < rowcount && col < 2) {
				rowData[row][col] = value;
				fireTableCellUpdated(row, col);
				al.actionPerformed(new ActionEvent(this, 0, "refresh"));
			}
		}

		/**
		 * adds a filter to the table.
		 * 
		 * @param o
		 */
		public void addFilter(Object o) {
			rowcount++;
			if (rowcount > rowData.length)
				extendTable();
			rowData[rowcount - 1][0] = o;
			fireTableDataChanged();
		}

		/**
		 * extends the size of the underlying data table.
		 */
		private void extendTable() {
			Object[][] newData = new Object[rowData.length + 20][2];
			for (int i = 0; i < rowData.length; i++) {
				newData[i][0] = rowData[i][0];
				newData[i][1] = rowData[i][1];
			}
			rowData = newData;
		}
	}

}
