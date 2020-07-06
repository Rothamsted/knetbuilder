package net.sourceforge.ondex.ovtk2.ui.editor.util;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;

/**
 * Makes sure that objects are properly converted to strings for sorting etc.
 * 
 * @author taubertj
 */
public class EditorTableStringConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object o = model.getValueAt(row, column);
		if (o instanceof MetaData)
			return ((MetaData) o).getId();
		else if (o instanceof ONDEXConcept)
			return String.valueOf(((ONDEXConcept) o).getId());
		else if (o instanceof ONDEXRelation)
			return String.valueOf(((ONDEXRelation) o).getId());
		else if (o instanceof ConceptName)
			return ((ConceptName) o).getName();
		else if (o instanceof ConceptAccession)
			return ((ConceptAccession) o).getAccession();
		else if (o instanceof Attribute)
			return ((Attribute) o).getValue().toString();
		else if (o != null)
			return o.toString();
		else
			return "";
	}

}
