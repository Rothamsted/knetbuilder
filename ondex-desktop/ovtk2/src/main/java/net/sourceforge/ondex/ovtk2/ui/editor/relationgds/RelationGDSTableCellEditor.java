package net.sourceforge.ondex.ovtk2.ui.editor.relationgds;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.marshal.Marshaller;
import net.sourceforge.ondex.ovtk2.ui.editor.util.GDSPopup;

/**
 * Using a text field to edit the XML string of a relation Attribute.
 * 
 * @author taubertj
 */
public class RelationGDSTableCellEditor extends DefaultCellEditor implements MouseListener, CaretListener {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 3915665316694480479L;

	/**
	 * Type represents a list of custom made data classes editors.
	 * 
	 * @author taubertj
	 */
	private enum TYPE {
		INTEGER, DOUBLE, FLOAT, STRING, BOOLEAN, UNKNOWN
	};

	/**
	 * attribute name derived from current column
	 */
	AttributeName an = null;

	/**
	 * cache for comparison of values
	 */
	Attribute attribute = null;

	/**
	 * By default unknown type
	 */
	TYPE type = TYPE.UNKNOWN;

	/**
	 * The relation to modify
	 */
	ONDEXRelation relation = null;

	/**
	 * Current ONDEX graph
	 */
	ONDEXGraph graph = null;

	/**
	 * check if text field entry was valid
	 */
	boolean validEntry = true;

	/**
	 * Fakes a table cell editor based around a JTextField.
	 */
	public RelationGDSTableCellEditor(ONDEXGraph graph) {
		super(new JTextField());
		// listen for right clicks
		super.getComponent().addMouseListener(this);
		// keep local graph reference
		this.graph = graph;
	}

	// Implement the one method defined by TableCellEditor.

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		// which relation and attribute name we are working on
		relation = (ONDEXRelation) table.getValueAt(row, 0);
		an = graph.getMetaData().getAttributeName(table.getColumnName(column));

		// check for relation accession values
		if (value instanceof Attribute) {
			attribute = (Attribute) value;
			Object o = attribute.getValue();
			value = o;
			if (o instanceof Integer)
				type = TYPE.INTEGER;
			else if (o instanceof Double)
				type = TYPE.DOUBLE;
			else if (o instanceof Float)
				type = TYPE.FLOAT;
			else if (o instanceof String)
				type = TYPE.STRING;
			else if (o instanceof Boolean) {
				type = TYPE.BOOLEAN;
			} else {
				type = TYPE.UNKNOWN;
				value = Marshaller.getMarshaller().toXML(o);
			}
		} else {
			attribute = null;
		}

		// use default text field editor
		JTextField field = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
		field.addCaretListener(this);
		return field;
	}

	@Override
	public Object getCellEditorValue() {

		// this is value returned by default editor
		String editorValue = super.getCellEditorValue().toString();
		if (editorValue.trim().length() > 0) {
			// extra check for validity of entry
			if (validEntry) {
				Object o = null;
				switch (type) {
				case INTEGER:
					o = Integer.valueOf(editorValue);
					break;
				case DOUBLE:
					o = Double.valueOf(editorValue);
					break;
				case FLOAT:
					o = Float.valueOf(editorValue);
					break;
				case STRING:
					o = editorValue;
					break;
				case BOOLEAN:
					o = Boolean.valueOf(editorValue);
					break;
				default:
					o = Marshaller.getMarshaller().fromXML(editorValue);
					break;
				}

				// check existing Attribute
				if (attribute == null) {
					// construct new Attribute with column attribute name
					attribute = relation.createAttribute(an, o, false);
				} else {
					// existing Attribute check value update
					if (!attribute.getValue().equals(o))
						attribute.setValue(o);
				}
			}
		} else {
			if (attribute != null) {
				// delete relation Attribute for empty string editor value
				relation.deleteAttribute(an);
				attribute = null;
			}
		}

		// return relation Attribute instead of string
		return attribute;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// right mouse click
		if (e.getButton() == MouseEvent.BUTTON3 && attribute != null) {
			// additional information / editing popup
			GDSPopup popup = new GDSPopup(attribute);
			popup.setLocation(e.getLocationOnScreen());
			popup.setVisible(true);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void caretUpdate(CaretEvent e) {
		// get text field which is used
		JTextField field = (JTextField) e.getSource();
		field.setBackground(Color.WHITE);
		String text = field.getText();
		validEntry = true;

		// empty field colour red
		if (text.trim().length() == 0) {
			field.setBackground(Color.RED);
		} else {
			// check for right formating of values entered
			switch (type) {
			case INTEGER:
				try {
					Integer.parseInt(text);
				} catch (NumberFormatException nfe) {
					field.setBackground(Color.RED);
					validEntry = false;
				}
				break;

			case DOUBLE:
				try {
					Double.parseDouble(text);
				} catch (NumberFormatException nfe) {
					field.setBackground(Color.RED);
					validEntry = false;
				}
				break;

			case FLOAT:
				try {
					Float.parseFloat(text);
				} catch (NumberFormatException nfe) {
					field.setBackground(Color.RED);
					validEntry = false;
				}
				break;

			case UNKNOWN:
				try {
					Marshaller.getMarshaller().fromXML(text);
				} catch (Exception any) {
					field.setBackground(Color.RED);
					validEntry = false;
				}
				break;
			}
		}
	}

}
