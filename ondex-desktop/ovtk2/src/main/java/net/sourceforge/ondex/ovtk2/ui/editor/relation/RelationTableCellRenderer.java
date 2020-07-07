package net.sourceforge.ondex.ovtk2.ui.editor.relation;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;

/**
 * Draw the relation id for the relation itself.
 * 
 * @author taubertj
 * 
 */
public class RelationTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 8828015808135323073L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		// display id of a relation
		if (value instanceof ONDEXRelation) {
			ONDEXRelation r = (ONDEXRelation) value;
			value = r.getId();
		}

		// display id of RelationType
		else if (value instanceof RelationType) {
			RelationType rt = (RelationType) value;
			value = rt.getId();
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
