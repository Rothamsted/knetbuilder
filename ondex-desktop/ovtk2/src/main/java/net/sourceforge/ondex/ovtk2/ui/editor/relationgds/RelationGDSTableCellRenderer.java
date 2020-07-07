package net.sourceforge.ondex.ovtk2.ui.editor.relationgds;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Uses a blue (pink selected) font colour for doIndex relation Attribute.
 * 
 * @author taubertj
 */
public class RelationGDSTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -6677703113430332040L;

	/**
	 * colour for indexed Attribute
	 */
	Color unselected = new Color(0, 0, 139);

	/**
	 * selection colour for indexed Attribute
	 */
	Color selected = new Color(255, 182, 193);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		// clear previous settings
		super.setForeground(null);

		// catch decision of colour
		Color color = null;

		// check for relation Attribute values
		if (value instanceof Attribute) {
			Attribute attribute = (Attribute) value;
			value = attribute.getValue();
			if (attribute.isDoIndex()) {
				// different colour depending on selection
				color = isSelected ? selected : unselected;
			}
		}

		// display id of a relation
		else if (value instanceof ONDEXRelation) {
			ONDEXRelation r = (ONDEXRelation) value;
			value = r.getId();
		}

		// default component
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// override default selection foreground
		if (color != null)
			super.setForeground(color);

		return comp;
	}
}
