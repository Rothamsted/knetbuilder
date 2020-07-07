package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.EvidenceType;

/**
 * Dynamic EvidenceType model.
 * 
 * @author hindlem, taubertj
 */
public class EvidenceTypeListModel extends AbstractListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated evidence types
	 */
	private List<EvidenceType> al = new ArrayList<EvidenceType>();

	/**
	 * Adds another EvidenceType to the list.
	 * 
	 * @param et
	 *            the EvidenceType to add to the list
	 */
	public void addEvidenceType(EvidenceType et) {
		al.add(et);
		Collections.sort(al, new Comparator<EvidenceType>() {

			@Override
			public int compare(EvidenceType o1, EvidenceType o2) {
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		al.clear();
	}

	/**
	 * Returns a JLabel for the evidence type
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (index > -1) {
			EvidenceType et = al.get(index);
			String name = et.getFullname();
			if (name.trim().length() == 0)
				name = et.getId();
			label = new JLabel(name);
			label.setName(et.getId());
			label.setToolTipText("(" + et.getId() + ") " + et.getDescription());
		}
		return label;
	}

	/**
	 * Returns EvidenceType at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return EvidenceType at index
	 */
	public EvidenceType getEvidenceTypeAt(int index) {
		if (index > -1) {
			EvidenceType et = al.get(index);
			return et;
		}
		return null;
	}

	@Override
	public int getSize() {
		return al.size();
	}
}
