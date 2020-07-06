package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Uses a red (yellow selected) font colour for preferred concept names.
 * 
 * @author taubertj
 * 
 */
public class ConceptNameTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8828015808135323071L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		// clear previous settings
		super.setForeground(null);

		// catch decision of colour
		Color color = null;

		// check for concept name values
		if (value instanceof ConceptName) {
			ConceptName cn = (ConceptName) value;
			value = cn.getName();
			if (cn.isPreferred()) {
				// different colour depending on selection
				color = isSelected ? Color.YELLOW : Color.RED;
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
