package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;

/**
 * Dynamic ONDEXConcept model.
 * 
 * @author hindlem, taubertj
 */
public class ConceptListModel extends AbstractListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated concepts
	 */
	private List<ONDEXConcept> concepts = new ArrayList<ONDEXConcept>();

	/**
	 * Adds another ONDEXConcept to the list.
	 * 
	 * @param concept
	 *            the ONDEXConcept to add to the list
	 */
	public void addConcept(ONDEXConcept concept) {
		concepts.add(concept);
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		concepts.clear();
	}

	/**
	 * Returns ONDEXConcept at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return ONDEXConcept at index
	 */
	public ONDEXConcept getConceptAt(int index) {
		if (index > -1) {
			ONDEXConcept ac = concepts.get(index);
			return ac;
		}
		return null;
	}

	/**
	 * Returns a JLabel for the concept
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (index > -1) {
			ONDEXConcept ac = concepts.get(index);
			String name = null;
			ConceptName cn = ac.getConceptName();
			if (cn != null)
				name = cn.getName();
			else
				name = String.valueOf(ac.getId());
			label = new JLabel(name);
			label.setName(String.valueOf(ac.getId()));
			label.setToolTipText("(" + ac.getId() + ") " + ac.getDescription());
		}
		return label;
	}

	@Override
	public int getSize() {
		return concepts.size();
	}
}
