package net.sourceforge.ondex.ovtk2.annotator.colorcategory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import net.sourceforge.ondex.ovtk2.ui.RegisteredJInternalFrame;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableCellRenderer;
import net.sourceforge.ondex.ovtk2.ui.dialog.tablemodel.ColorTableEditor;

/**
 * 
 * @author weilej
 * 
 */
public class LegendFrame extends RegisteredJInternalFrame implements TableModelListener {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -979980121249756769L;

	private String attributeName;
	private TreeSet<Comparable<?>> items;
	private Map<Object, Color> colors;
	private ColorCategoryAnnotator annotator;

	private JTable table;

	/**
	 * constructor.
	 */
	public LegendFrame(ColorCategoryAnnotator annotator) {
		super("Legend " + annotator.getName(), "Annotator", "Legend " + annotator.getName() + " - " + annotator.getViewer().getTitle(), true, true, false, true);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.annotator = annotator;
		this.attributeName = annotator.getAttributeName();
		this.items = annotator.getItemSet();
		this.colors = annotator.getColorTable();

		setupPanels();
		pack();
		setSize(new Dimension(180, getHeight()));
		if (getHeight() > 400)
			setSize(new Dimension(getWidth(), 400));
	}

	private void setupPanels() {
		getContentPane().setLayout(new BorderLayout());

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(attributeName));

		ColorTableModel model = new ColorTableModel();
		model.addTableModelListener(this);

		ColorTableEditor editor = new ColorTableEditor();
		ColorTableCellRenderer renderer = new ColorTableCellRenderer(true);

		table = new JTable(model);
		table.setDefaultEditor(Color.class, editor);
		table.setDefaultRenderer(Color.class, renderer);

		TableColumn color = table.getColumnModel().getColumn(1);
		color.setMaxWidth(40);
		color.setMinWidth(40);
		color.setCellEditor(editor);
		color.setCellRenderer(renderer);

		// TableColumn name = table.getColumnModel().getColumn(0);
		// name.setMaxWidth(100);
		// name.setMinWidth(100);

		panel.add(new JScrollPane(table));

		getContentPane().add(panel, BorderLayout.CENTER);
	}

	private class ColorTableModel extends AbstractTableModel {

		/**
		 * serial id.
		 */
		private static final long serialVersionUID = -8512853987405261485L;

		Object[][] array;

		ColorTableModel() {
			array = new Object[items.size()][2];
			int i = 0;
			for (Object o : items) {
				array[i][0] = o;
				array[i++][1] = colors.get(o);
			}
		}

		public String getColumnName(int col) {
			if (col == 0)
				return attributeName;
			else
				return "Color";
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return items.size();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex > -1 && rowIndex < getRowCount() && columnIndex > -1 && columnIndex < 2)
				return array[rowIndex][columnIndex];
			else
				return null;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (rowIndex > -1 && rowIndex < getRowCount() && columnIndex > -1 && columnIndex < 2)
				array[rowIndex][columnIndex] = aValue;
			fireTableCellUpdated(rowIndex, columnIndex);
		}

	}

	@Override
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int col = e.getColumn();
		if (e.getType() == TableModelEvent.UPDATE && col == 1) {// color updated
			Object id = table.getModel().getValueAt(row, 0);
			Color color = (Color) ((ColorTableModel) table.getModel()).getValueAt(row, col);
			colors.put(id, color);
			annotator.applyColors();
		}
	}

}
