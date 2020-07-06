package net.sourceforge.ondex.ovtk2.ui.editor.conceptgds;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Uses a blue (pink selected) font colour for doIndex concept Attribute.
 * 
 * @author taubertj
 */
public class ConceptGDSTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8828015808135323072L;

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

		// check for concept Attribute values
		if (value instanceof Attribute) {
			Attribute attribute = (Attribute) value;
			value = attribute.getValue();
			if (attribute.isDoIndex()) {
				// different colour depending on selection
				color = isSelected ? selected : unselected;
			}
		}

		// display id of a concept
		else if (value instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) value;
			value = c.getId();
		}

		// default component
		Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// override default selection foreground
		if (color != null)
			super.setForeground(color);

		return comp;
	}
}
