package net.sourceforge.ondex.workflow2.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentCollectionContainer;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentContainer;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentHolder;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightableComponent;

/**
 * @author lysenkoa
 */
public class TablePanel extends JPanel implements KeyListener {
    private static final long serialVersionUID = 1L;
    private static final Color BLUE_GRAY = new Color(120, 120, 255);
    private static final Border border = BorderFactory.createTitledBorder("").getBorder();
    private final List<ArgumentContainer> dataSimple = new LinkedList<ArgumentContainer>();
    private final List<ArgumentCollectionContainer> dataCollection = new LinkedList<ArgumentCollectionContainer>();
    private final Map<Integer, ArgumentHolder> simple = new HashMap<Integer, ArgumentHolder>();
    private final Map<Integer, Map<String, ArgumentHolder>> complex = new HashMap<Integer, Map<String, ArgumentHolder>>();
    private final Map<ArgumentDescription, Component> callbackRefs = new HashMap<ArgumentDescription, Component>();
    private final List<ArgumentDescription> orginalOrder = new LinkedList<ArgumentDescription>();
    private JTable table;
    private DefaultTableModel model;
    private int varWidth;
    private int visibleItemCount = 0;
    private CustomRenderer cr = new CustomRenderer();
    @SuppressWarnings("unused")
    private boolean simpleMode = false;
    private Set<Integer> readOnlyRow = new HashSet<Integer>();

    @SuppressWarnings("serial")
    public TablePanel(String title) {
        super(new BorderLayout());
        this.setBorder(border);
        model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col) {
                return col == 1 && !readOnlyRow.contains(row);
            }
        };
        model.setColumnCount(2);
        table = new JTable(model);
        table.setFont(table.getFont().deriveFont(12f));
        table.setGridColor(this.getBackground());
        table.setBorder(BorderFactory.createEmptyBorder());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setRowHeight(18);
        setVarNameWidth(1);
        table.getColumnModel().getColumn(1).setCellRenderer(cr);
        table.getColumnModel().getColumn(1).setCellEditor(cr);
        table.getColumnModel().getColumn(0).setCellRenderer(cr);
        //table.setFillsViewportHeight(true);
        //JScrollPane scrollPane = new JScrollPane(table);
        this.add(table, BorderLayout.CENTER);
        this.validate();
    }

    public void setVarNameWidth(int width) {
        table.getColumnModel().getColumn(0).setMaxWidth(width);
        table.getColumnModel().getColumn(0).setPreferredWidth(width);
        table.getColumnModel().getColumn(0).setMinWidth(width);
        varWidth = width;
    }

    public ArgumentHolder addItem(ArgumentDescription at) {
        orginalOrder.add(at);
        return addItem(createComponent(at), null);
    }

    public ArgumentHolder addItem(Object c, String passName) {
        if (c == null)
            return null;
        ArgumentHolder result = null;
        ArgumentDescription at = null;
        String name = passName;
        //String tooltip = null;
        boolean isRequired = true;
        if (c instanceof ArgumentHolder) {
            ArgumentHolder ac = (ArgumentHolder) c;
            if (c instanceof ArgumentContainer) {
                dataSimple.add((ArgumentContainer) ac);
            } else if (c instanceof ArgumentCollectionContainer) {
                dataCollection.add((ArgumentCollectionContainer) c);
            }
            at = ac.getArgumentTemplate();
            name = at.getName();
           // tooltip = at.getDescription();
            isRequired = at.getIsRequired();
            if (at.getInteranlName() == null || at.getInteranlName().length() == 0) {
                simple.put(at.getInputId(), ac);
            } else {
                Map<String, ArgumentHolder> sub = complex.get(at.getInputId());
                if (sub == null) {
                    sub = new HashMap<String, ArgumentHolder>();
                    complex.put(at.getInputId(), sub);
                }
                sub.put(at.getInteranlName(), ac);
            }
            result = ac;
        }
        if (c instanceof Component) {
        	Component comp =((Component) c);
        	comp.setFont(table.getFont());
            JLabel l = new JLabel(name);
            //if(tooltip != null)l.setToolTipText(tooltip);
            model.addRow(new Object[]{l, c});
            visibleItemCount++;
            if (isRequired) {
                if (at.isOutputObject() || at.isInputObject())
                    l.setForeground(Color.BLUE);
                else
                    l.setForeground(Color.BLACK);
            } else {
                if (at.isOutputObject() || at.isInputObject())
                    l.setForeground(BLUE_GRAY);
                else
                    l.setForeground(Color.GRAY);
            }
            if ((l.getPreferredSize().getWidth() + 11) > varWidth) {
                setVarNameWidth((int) l.getPreferredSize().getWidth() + 11);
            }
            table.validate();
        }
        return result;
        //sub.gridy++;
    }

    public JTable getTable() {
        return table;
    }

    private Object createComponent(ArgumentDescription at) {
        Component result = null;
        if (at.getDoNotRender()) {
            return new Intenal(at, at.getDefaultValue());
        }
        String type = at.getType().trim();
        if (type.equals("field")) {
            result = new TableField(at);
        } else if (type.equals("list")) {
            result = new TableList(at);
        } else if (type.equals("checkbox")) {
            result = new TableCheckbox(at);
        } else if (type.equals("combobox")) {
            result = new TableCombobox(at);
        } else if (type.toLowerCase().startsWith("custom:")) {
            try {
                result = (Component)Class.forName(type.substring(7, type.length())).getConstructor(ArgumentDescription.class).newInstance(at);
                if (result instanceof ExtendedTableInputComponent) {
                    ((ExtendedTableInputComponent) result).setParent(this);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        result.addKeyListener(this);
        callbackRefs.put(at, (Component) result);
        return result;
    }

    public void setValue(Integer id, String internalId, String value) {
        if (simple.containsKey(id)) {
            simple.get(id).setValue(value);
        } else if (complex.containsKey(id)) {
            ArgumentHolder ac = complex.get(id).get(internalId);
            if (ac != null) ac.setValue(value);
        }
    }

    public void setGeneratedDefaultValue(String internalId, String value) {
        for (ArgumentContainer d : dataSimple) {
            if (d.getArgumentTemplate().getInteranlName().equals(internalId) && (d.getContent().getValue().length() == 0)) {
                d.setValue(value);
            }
        }
        for (ArgumentCollectionContainer d : dataCollection) {
            if (d.getArgumentTemplate().getInteranlName().equals(internalId)) {
                d.setValue(value);
            }
        }
    }

    public Map<String, ArgumentDescription> getTempaltes() {
        return null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component c : this.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container) propagateEnabled((Container) c, enabled);
        }
    }

    private void propagateEnabled(Container comp, boolean enabled) {
        for (Component c : comp.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container) propagateEnabled((Container) c, enabled);
        }
    }

    private class Intenal implements ArgumentContainer {
        private ArgumentDescription at;
        private String content;

        public Intenal(ArgumentDescription at, String content) {
            this.at = at;
            this.content = content;
        }

        public BoundArgumentValue getContent() {
            return new BoundArgumentValue(at, content);
        }

        public void setValue(String value) {
        }

        public ArgumentDescription getArgumentTemplate() {
            return at;
        }

        @Override
        public String toString() {
            return content;
        }

		@Override
		public void sendFocus() {
			// TODO Auto-generated method stub
			
		}
    }

    public List<BoundArgumentValue> getContainerData() {
        List<BoundArgumentValue> result = new LinkedList<BoundArgumentValue>();
        for (ArgumentContainer d : dataSimple) {
            BoundArgumentValue ap = d.getContent();
            String v = ap.getValue();
            if (v == null || v.equals("")) {
                if (ap.getArg().getIsRequired() == false) {
                    ap.setValue(null);
                }
            }
            result.add(ap);
        }
        for (ArgumentCollectionContainer z : dataCollection) {
            result.addAll(z.getContentList());
        }
        return result;
    }

    private class CustomRenderer extends DefaultTableCellRenderer implements TableCellEditor {
        private static final long serialVersionUID = -1319732832820525154L;
        private Map<Integer, Color> map = new HashMap<Integer, Color>();

        public CustomRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component result = (Component) value;
            if (column == 1) {
                Color c = map.get(row);
                if (c != null) {
                    result.setForeground(c);
                }
            }
            return result;
        }

        public void setColor(Integer row, Color color) {
            map.put(row, color);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value, boolean isSelected, int row, int column) {
            Component result = (Component) value;
            if (column == 1) {
                Color c = map.get(row);
                if (c != null) {
                    result.setForeground(c);
                }
            }
            return result;
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
        }

        @Override
        public void cancelCellEditing() {
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            return true;
        }
    }

    public void setByName(String rowName, String value) {
        if (value == null)
            return;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((JLabel) model.getValueAt(i, 0)).getText().equals(rowName)) {
                Object obj = model.getValueAt(i, 1);
                Class<?> cls = obj.getClass();
                /*else if(JTextField.class.isAssignableFrom(cls)){
                        ((JTextField)obj).setText(value);
                    }
                    else if(JTextArea.class.isAssignableFrom(cls)){
                        ((JTextArea)obj).setText(value);
                    }
                    else if(JComboBox.class.isAssignableFrom(cls)){
                        JComboBox cb = ((JComboBox)obj);
                        cb.setSelectedItem(value);
                        if(!cb.getSelectedItem().equals(value)){
                            cb.addItem(value);
                            cb.setSelectedItem(value);
                        }
                    }
                    else if(JCheckBox.class.isAssignableFrom(cls)){
                        ((JCheckBox)obj).setSelected(Boolean.valueOf(value));
                    }
                    else if(JScrollPane.class.isAssignableFrom(cls)){
                        ((JTextArea)((JScrollPane)obj).getViewport().getView()).setText(value);
                    }*/

                if (ArgumentContainer.class.isAssignableFrom(cls)) {
                    ((ArgumentContainer) obj).setValue(value);
                } else if (ArgumentCollectionContainer.class.isAssignableFrom(cls)) {
                    ((ArgumentCollectionContainer) obj).setValue(value);
                } else {
                    //System.err.println("Warning vlaue "+ rowName+" may not have been found correctly!");
                    //model.setValueAt(value, i, 1);
                }
            }
        }
    }

    public void disableRowEditing(int... row) {
        for (int i : row) readOnlyRow.add(i);

    }

    public void enableRowEditing(int... row) {
        for (int i : row) readOnlyRow.remove(i);
    }

    public String getByName(String rowName) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (((JLabel) model.getValueAt(i, 0)).getText().equals(rowName)) {
                Object obj = model.getValueAt(i, 1);
                Class<?> cls = obj.getClass();
                if (ArgumentContainer.class.isAssignableFrom(cls)) {
                    return ((ArgumentContainer) obj).getContent().getValue();
                }
                /*
                    if(obj instanceof TableField){
                        return ((TableField)obj).getValue();
                    }
                    else if(JTextField.class.isAssignableFrom(cls)){
                        return ((JTextField)obj).getText();
                    }
                    else if(JTextArea.class.isAssignableFrom(cls)){
                        return ((JTextArea)obj).getText();
                    }
                    else if(JComboBox.class.isAssignableFrom(cls)){
                        return ((JComboBox)obj).getSelectedItem().toString();
                    }
                    else if(JCheckBox.class.isAssignableFrom(cls)){
                        return String.valueOf(((JCheckBox)obj).isSelected());
                    }
                    else if(JScrollPane.class.isAssignableFrom(cls)){
                        return ((JTextArea)((JScrollPane)obj).getViewport().getView()).getText();
                    }*/
                //System.err.println("Warning vlaue "+ rowName+" may not have been found correctly!");
                return model.getValueAt(i, 1).toString();
            }
        }
        return null;
    }

    public void fitContent() {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1) instanceof TableList) {
                table.setRowHeight(i, (int) ((Component) model.getValueAt(i, 1)).getPreferredSize().getHeight());
            }
        }
    }

    public void copyContent(TablePanel dest) {
        for (ArgumentDescription at : orginalOrder) {
            dest.addItem(at);
        }
        for (ArgumentContainer ac : dataSimple) {
            dest.setValue(ac.getArgumentTemplate().getInputId(), ac.getArgumentTemplate().getInteranlName(), ac.getContent().getValue());
        }
        for (ArgumentCollectionContainer acc : dataCollection) {
            for (BoundArgumentValue vt : acc.getContentList())
                dest.setValue(acc.getArgumentTemplate().getInputId(), acc.getArgumentTemplate().getInteranlName(), vt.getValue());
        }
        dest.validate();
    }

    public boolean hasUserInputs() {
        return visibleItemCount > 0;
    }

    /*protected void copyTable(JTable source, JTable target){
         for(int i = 0; i < source.getModel().getRowCount(); i++){
             Object sObj = source.getModel().getValueAt(i, 1);
             Object tObj = target.getModel().getValueAt(i, 1);
             if(sObj instanceof ArgumentCollectionContainer){
                 ((ArgumentCollectionContainer)tObj).setValuesAsArray(((ArgumentCollectionContainer)sObj).getValuesAsArray());
             }
             else if(sObj instanceof ArgumentContainer){
                 ((ArgumentContainer)tObj).setValue(((ArgumentContainer)sObj).getContent().getValue().toString());
             }
         }
     }*/

    @Override
    public String toString() {
        String result = "";
        for (BoundArgumentValue v : this.getContainerData()) {
            result = result + v.getValue() + " ";
        }
        result = result + "\n";
        return result;
    }

    public void focusOn(ArgumentDescription ab) {
        if (ab == null)
            return;
        final Component c = callbackRefs.get(ab);
        if (c == null) {
            throw new RuntimeException("Invalid argument for this object");
        }
        c.requestFocusInWindow();
        final Color old = c.getBackground();
        if (c instanceof HighlightableComponent) {
            ((HighlightableComponent) c).highlight();
        }
    }

	@Override
	public void keyTyped(KeyEvent e) {
		Object o = model.getValueAt(table.getEditingRow(), 1);
		if(o instanceof KeyListener){
			((KeyListener)o).keyTyped(e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == 40){
			int row = table.getEditingRow() +1 ;
			//System.err.println(row);
			if(row < table.getRowCount()){
				table.getCellEditor().stopCellEditing();
				table.editCellAt(row, 1);
				((ArgumentHolder)model.getValueAt(row, 1)).sendFocus();
			}
		}
		else if(e.getKeyCode() == 38){
			int row = table.getEditingRow() - 1 ;
			//System.err.println(row);
			if(row >= 0){
				table.getCellEditor().stopCellEditing();
				table.editCellAt(row, 1);
				((ArgumentHolder)model.getValueAt(row, 1)).sendFocus();
			}
		}
	}
}
