package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Uses a green (yellow selected) font colour for preferred concept accessions.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8828015808135323072L;

	/**
	 * colour for non-ambiguous accessions
	 */
	Color unselected = new Color(0, 100, 0);

	/**
	 * selection colour for non-ambiguous accessions
	 */
	Color selected = new Color(240, 230, 140);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		// clear previous settings
		super.setForeground(null);

		// catch decision of colour
		Color color = null;

		// check for concept accessions values
		if (value instanceof ConceptAccession) {
			ConceptAccession ca = (ConceptAccession) value;
			// concatenate with element of
			value = ca.getAccession() + " (" + ca.getElementOf().getId() + ")";
			if (!ca.isAmbiguous()) {
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
