package net.sourceforge.ondex.ovtk2.ui.editor.util;

import java.util.Comparator;

import net.sourceforge.ondex.core.AttributeName;

/**
 * Compares attribute names according to their IDs.
 * 
 * @author taubertj
 * 
 */
public class AttributeNameComparator implements Comparator<AttributeName> {

	@Override
	public int compare(AttributeName o1, AttributeName o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
