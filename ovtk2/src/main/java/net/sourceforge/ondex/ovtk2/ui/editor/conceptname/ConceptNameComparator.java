package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import java.util.Comparator;

import net.sourceforge.ondex.core.ConceptName;

/**
 * Comparator for concept names based on their names and preferred flag.
 * 
 * @author taubertj
 * 
 */
public class ConceptNameComparator implements Comparator<ConceptName> {

	@Override
	public int compare(ConceptName o1, ConceptName o2) {
		if (o1.isPreferred() && !o2.isPreferred())
			return -1;
		else if (!o1.isPreferred() && o2.isPreferred())
			return 1;
		else
			return o1.getName().compareTo(o2.getName());
	}

}
