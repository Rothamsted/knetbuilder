package net.sourceforge.ondex.workflow2.gui.components;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentCollectionContainer;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightHandler;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightManager;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightableComponent;

/**
 * @author lysenkoa
 */
public class TableList extends JTable implements ArgumentCollectionContainer, HighlightableComponent {

    private static final long serialVersionUID = 1018916928512590290L;
    private DefaultTableModel dm = new DefaultTableModel();
    private ArgumentDescription at;
    private final HighlightHandler hlh;

    public TableList(ArgumentDescription at) {
        super();
        this.hlh = HighlightManager.getInstance().getHighlighter(this);
        this.setModel(dm);
        this.at = at;
        this.setShowGrid(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //this.getSelectionModel().addListSelectionListener(new SelectionListener(this));
        this.addFocusListener(new ClearSelectionFocusListener());
        this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "selectNextRowCell");
        dm.setRowCount(1);
        dm.addColumn("");
        this.getColumnModel().getColumn(0).setCellRenderer(new NoHighlightRenderer());
        if (at.getDescription() != null && !at.getDescription().equals("")){
            //this.setToolTipText(at.getDescription());
            this.getModel().addTableModelListener(new AddRows());
        }

    }

    @SuppressWarnings("unchecked")
    public void setValue(String value) {
        if (value == null || value.length() == 0)
            return;
        DefaultTableModel tm = (DefaultTableModel) this.getModel();
        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0) == null || tm.getValueAt(i, 0).equals("")) {
                tm.setValueAt(value, i, 0);
                return;
            }
        }
        tm.addRow(new Vector(Arrays.asList(value)));
        tm.addRow(new Vector());
    }

    /*private class SelectionListener implements ListSelectionListener {
         private final JTable table;
         SelectionListener(JTable table) {
             this.table = table;
         }
         @SuppressWarnings("unchecked")
         public void valueChanged(ListSelectionEvent e) {
             int last = e.getLastIndex();
             TableModel tm = table.getModel();
             boolean incraseCellSize = false;
             if(last == tm.getRowCount()-1){
                 ((DefaultTableModel)tm).addRow(new Vector());
                 incraseCellSize = true;
             }
             if(incraseCellSize){
                 JTable pTable = (JTable)table.getParent();
                 if(pTable == null)return;
                 TableModel m = pTable.getModel();
                 int i = 0;
                 while(!m.getValueAt(i, 1).equals(table))i++;
                 pTable.setRowHeight(i,(int)table.getPreferredSize().getHeight());
             }
         }
     }*/

    private class AddRows implements TableModelListener {

        public void tableChanged(TableModelEvent arg0) {
            DefaultTableModel tm = (DefaultTableModel) TableList.this.getModel();
            int rowCount = tm.getRowCount();
            if (rowCount == 0) return;
            tm.removeTableModelListener(this);
            for (int i = rowCount - 1; i >= 0; i--) {
                if (tm.getValueAt(i, 0) == null || tm.getValueAt(i, 0).toString().equals("")) {
                    tm.removeRow(i);
                } else {
                    if (tm.getValueAt(i, 0) instanceof String) {
                        String strValue = (String) tm.getValueAt(i, 0);
                        tm.setValueAt(strValue, i, 0);
                    }
                }
            }
            tm.addRow(new Vector());
            JTable pTable = (JTable) TableList.this.getParent();
            if (pTable == null) {
                tm.addTableModelListener(this);
                return;
            }
            TableModel m = pTable.getModel();
            int i = 0;
            while (!m.getValueAt(i, 1).equals(TableList.this)) i++;
            pTable.setRowHeight(i, (int) TableList.this.getPreferredSize().getHeight());
            int row = TableList.this.getEditingRow()+1;
            TableList.this.editCellAt(row, 0);
            Component c = TableList.this.getCellEditor(row, 0).getTableCellEditorComponent(TableList.this, "", true, row, 0);
            c.dispatchEvent(new FocusEvent(c, FocusEvent.FOCUS_GAINED));
            TableList.this.changeSelection(row, 0, false, false);
            tm.addTableModelListener(this);
        }
    }

    private class ClearSelectionFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            JTable pTable = (JTable) TableList.this.getParent();
            if (pTable == null) return;
            TableModel m = pTable.getModel();
            int i = 0;
            while (!m.getValueAt(i, 1).equals(TableList.this)) i++;
            pTable.setRowHeight(i, (int) TableList.this.getPreferredSize().getHeight());
            TableList.this.getSelectionModel().clearSelection();
        }
    }

    @Override
    public ArgumentDescription getArgumentTemplate() {
        return at;
    }

    @Override
    public List<BoundArgumentValue> getContentList() {
        List<BoundArgumentValue> result = new ArrayList<BoundArgumentValue>();
        TableModel m = this.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            Object value = m.getValueAt(i, 0);
            if (value != null) {
                String temp = value.toString();
                if (temp.length() > 0) {
                    result.add(new BoundArgumentValue(at, temp));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("serial")
    private class NoHighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            return super.getTableCellRendererComponent(table, value, false, false, row, column);
        }
    }

    @Override
    public String[] getValuesAsArray() {
        String[] result = new String[dm.getRowCount()];
        for (int i = 0; i < result.length; i++) {
            Object value = this.getValueAt(i, 0);
            if (value != null) result[i] = value.toString();
        }
        return result;
    }

    @Override
    public void setValuesAsArray(String[] values) {
        dm.setRowCount(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null)
                this.setValueAt(values[i], i, 0);
        }
    }

    @Override
    public void highlight() {
        hlh.highlight();
    }

	@Override
	public void sendFocus() {
		int lastRow = this.getModel().getRowCount()-1;
		this.changeSelection(lastRow, 0, false, false);
		this.dispatchEvent(new FocusEvent(this, FocusEvent.FOCUS_GAINED));
		this.editCellAt(lastRow, 0);
		this.requestFocus();
	}

}
