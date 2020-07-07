package net.sourceforge.ondex.ovtk2.ui.editor.util;

import java.util.Comparator;

import net.sourceforge.ondex.core.ONDEXEntity;

/**
 * Compares ONDEXEntity by there Integer ids.
 * 
 * @author taubertj
 * 
 */
public class ONDEXEntityComparator implements Comparator<ONDEXEntity> {

	@Override
	public int compare(ONDEXEntity o1, ONDEXEntity o2) {
		return o2.getId() - o1.getId();
	}
}
