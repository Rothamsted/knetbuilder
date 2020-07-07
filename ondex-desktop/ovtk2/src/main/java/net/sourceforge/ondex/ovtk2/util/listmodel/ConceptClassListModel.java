package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.ConceptClass;

/**
 * Dynamic ConceptClass model.
 * 
 * @author hindlem, taubertj
 */
public class ConceptClassListModel extends AbstractListModel implements MutableListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated concept classes
	 */
	private List<ConceptClass> conceptClasses = new ArrayList<ConceptClass>();

	@Override
	public void add(int index, Object element) {
		conceptClasses.add(index, (ConceptClass) element);
		fireContentsChanged(this, 0, conceptClasses.size());
	}

	/**
	 * Adds another ConceptClass to the list.
	 * 
	 * @param cc
	 *            the ConceptClass to add to the list
	 */
	public void addConceptClass(ConceptClass cc) {
		conceptClasses.add(cc);
		Collections.sort(conceptClasses, new Comparator<ConceptClass>() {

			@Override
			public int compare(ConceptClass o1, ConceptClass o2) {
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
		fireContentsChanged(this, 0, conceptClasses.size());
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		conceptClasses.clear();
		fireContentsChanged(this, 0, conceptClasses.size());
	}

	/**
	 * Returns ConceptClass at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return ConceptClass at index
	 */
	public ConceptClass getConceptClassAt(int index) {
		if (index > -1) {
			ConceptClass cc = conceptClasses.get(index);
			return cc;
		}
		return null;
	}

	/**
	 * Returns a JLabel for the concept class
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (index > -1) {
			ConceptClass cc = conceptClasses.get(index);
			String name = cc.getFullname();
			if (name.trim().length() == 0)
				name = cc.getId();
			label = new JLabel(name);
			label.setName(cc.getId());
			label.setToolTipText("(" + cc.getId() + ") " + cc.getDescription());
		}
		return label;
	}

	@Override
	public int getSize() {
		return conceptClasses.size();
	}

	/**
	 * Whether or not the list of concept classes is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return conceptClasses.isEmpty();
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
		conceptClasses.remove(index);
		fireContentsChanged(this, 0, conceptClasses.size());
	}
}
