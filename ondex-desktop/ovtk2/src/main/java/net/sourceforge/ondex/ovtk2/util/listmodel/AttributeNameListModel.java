package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.AttributeName;

/**
 * Dynamic AttributeName model
 * 
 * @author hindlem, taubertj
 */
public class AttributeNameListModel extends AbstractListModel implements MutableListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated attribute names
	 */
	private List<AttributeName> attributeNames = new ArrayList<AttributeName>();

	@Override
	public void add(int index, Object element) {
		attributeNames.add(index, (AttributeName) element);
		fireContentsChanged(this, 0, attributeNames.size());
	}

	/**
	 * Adds given attribute name to internal list.
	 * 
	 * @param an
	 *            AttributeName to add to the list
	 */
	public void addAttributeName(AttributeName an) {
		attributeNames.add(an);
		// sort by ID
		Collections.sort(attributeNames, new Comparator<AttributeName>() {

			@Override
			public int compare(AttributeName o1, AttributeName o2) {
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
		fireContentsChanged(this, 0, attributeNames.size());
	}

	/**
	 * Clears this list
	 */
	public void clearList() {
		attributeNames.clear();
		fireContentsChanged(this, 0, attributeNames.size());
	}

	/**
	 * Returns AttributeName at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return AttributeName at index
	 */
	public AttributeName getAttributeNameAt(int index) {
		if (index > -1) {
			AttributeName an = attributeNames.get(index);
			return an;
		}
		return null;
	}

	/**
	 * Returns a JLabel for the attribute name
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (attributeNames.size() == 0 && index == 0) {
			label = new JLabel("no attributes present!");
		} else if (index > -1) {
			AttributeName an = attributeNames.get(index);
			String name = an.getFullname();
			if (name.trim().length() == 0)
				name = an.getId();
			label = new JLabel(name);
			label.setName(an.getId());
			label.setToolTipText("(" + an.getId() + ") " + an.getDescription());
		}
		return label;
	}

	@Override
	public int getSize() {
		return attributeNames.size();
	}

	/**
	 * Whether or not the list of attributes is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return attributeNames.isEmpty();
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
		attributeNames.remove(index);
		fireContentsChanged(this, 0, attributeNames.size());
	}
}
