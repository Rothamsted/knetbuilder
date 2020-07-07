package net.sourceforge.ondex.ovtk2.ui.editor.concept;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Draw the concept id for the concept itself.
 * 
 * @author taubertj
 * 
 */
public class ConceptTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -2937743136929931219L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		// display id of a concept
		if (value instanceof ONDEXConcept) {
			ONDEXConcept c = (ONDEXConcept) value;
			value = c.getId();
		}

		// display id of DataSource
		else if (value instanceof DataSource) {
			DataSource dataSource = (DataSource) value;
			value = dataSource.getId();
		}

		// display id of concept class
		else if (value instanceof ConceptClass) {
			ConceptClass cc = (ConceptClass) value;
			value = cc.getId();
		}

		// display evidence types as list
		else if (value instanceof Collection) {
			StringBuffer list = new StringBuffer();
			for (Object o : (Collection<?>) value) {
				list.append(o.toString());
				list.append(", ");
			}
			int index = list.lastIndexOf(", ");
			if (index > -1)
				list.delete(index, index + 2);
			value = list.toString();
		}

		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
