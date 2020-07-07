package net.sourceforge.ondex.ovtk2.ui.editor.util;

import java.util.Comparator;

import net.sourceforge.ondex.core.Attribute;

/**
 * Compares to subclasses of Number.
 * 
 * @author taubertj
 */
public class GDSNumberComparator implements Comparator<Attribute> {

	@Override
	public int compare(Attribute o1, Attribute o2) {
		int thisVal = ((Number) o1.getValue()).intValue();
		int anotherVal = ((Number) o2.getValue()).intValue();
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}
}
