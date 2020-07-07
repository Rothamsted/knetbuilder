package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.RelationType;

/**
 * Dynamic RelationType model.
 * 
 * @author hindlem, taubertj, weilej
 */
public class RelationTypeListModel extends AbstractListModel implements MutableListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated relation types
	 */
	private List<RelationType> relationTypes = new ArrayList<RelationType>();

	@Override
	public void add(int index, Object element) {
		relationTypes.add(index, (RelationType) element);
		fireContentsChanged(this, 0, relationTypes.size());
	}

	/**
	 * Adds another RelationType to the list.
	 * 
	 * @param rt
	 *            the RelationType to add to the list
	 */
	public void addRelationType(RelationType rt) {
		relationTypes.add(rt);
		Collections.sort(relationTypes, new Comparator<RelationType>() {

			@Override
			public int compare(RelationType o1, RelationType o2) {
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
		fireContentsChanged(this, 0, relationTypes.size());
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		relationTypes.clear();
	}

	/**
	 * Returns a JLabel for the relation type
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (index > -1) {
			RelationType rt = relationTypes.get(index);
			String name = rt.getFullname();
			if (name.trim().length() == 0)
				name = rt.getId();
			label = new JLabel(name);
			label.setName(rt.getId());
			label.setToolTipText("(" + rt.getId() + ") " + rt.getDescription().replaceAll("\\s+", " "));
		}
		return label;
	}

	/**
	 * Returns RelationType at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return RelationType at index
	 */
	public RelationType getRelationTypeAt(int index) {
		if (index > -1) {
			RelationType rt = relationTypes.get(index);
			return rt;
		}
		return null;
	}

	@Override
	public int getSize() {
		return relationTypes.size();
	}

	/**
	 * Whether or not the list of relation types is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return relationTypes.isEmpty();
	}

	/**
	 * Update list
	 * 
	 */
	public void refresh() {
		fireContentsChanged(this, 0, getSize());
	}

	@Override
	public void remove(int index) {
		relationTypes.remove(index);
		fireContentsChanged(this, 0, relationTypes.size());
	}
}
