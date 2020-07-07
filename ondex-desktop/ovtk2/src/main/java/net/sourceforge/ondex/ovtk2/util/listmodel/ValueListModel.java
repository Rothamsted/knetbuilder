package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

/**
 * Dynamic Value model.
 * 
 * @author lysenkoa, hindlem, taubertj
 */
public class ValueListModel extends AbstractListModel {

	private static final long serialVersionUID = 1L;

	private List<Object> objects = new ArrayList<Object>();

	/**
	 * Adds another Object to the list.
	 * 
	 * @param o
	 *            the Object to add to the list
	 */
	public void addObject(Object o) {
		objects.add(o);
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		objects.clear();
	}

	public Object getElementAt(int index) {
		JLabel label = null;
		if (objects.size() == 0 && index == 0) {
			label = new JLabel("no values present!");
		} else if (index > -1) {
			Object o = objects.get(index);
			label = new JLabel(o.toString());
		}
		return label;
	}

	public Object getObjectAt(int index) {
		if (index > -1) {
			Object o = objects.get(index);
			return o;
		}
		return null;
	}

	@Override
	public int getSize() {
		int size = objects.size() < 1 ? 1 : objects.size();
		return size;
	}

	public boolean isEmpty() {
		return objects.isEmpty();
	}

	public void refresh() {
		fireContentsChanged(this, 0, getSize());
	}
}
