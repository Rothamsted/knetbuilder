package net.sourceforge.ondex.ovtk2.ui.stats;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.ConceptClassListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.RelationTypeListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * This class represents the panel on the left side of the statistics window. It
 * provides four lists that carry entries for attribute names, concept classes,
 * relation types and graph features. all lists work as one unit, i.e. only one
 * element can be selected at a time. the panel also provides methods to pop out
 * the selected element or to automatically add an element to the correct list.
 * 
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class ElementChooserPanel extends JPanel implements ListSelectionListener {

	// ####FIELDS####

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -5801650787533576479L;

	/**
	 * the listmodel for the concept class list.
	 */
	private ConceptClassListModel ccListModel;

	/**
	 * the list model for the relation type list.
	 */
	private RelationTypeListModel rtListModel;

	/**
	 * the attribute name list model
	 */
	private AttributeNameListModel anListModel;

	/**
	 * the graph feature list model
	 */
	private DefaultListModel oListModel;

	/**
	 * the four lists containing concept classes relation types, attribute names
	 * and graph features.
	 */
	private JList ccList, rtList, anList, oList;

	/**
	 * the currently selected object (either a ConceptClass, RelationType,
	 * AttributeName or a String).
	 */
	private Object currentSelection;

	/**
	 * the graph.
	 */
	private ONDEXGraph aog;

	// ####CONSTRUCTOR####

	/**
	 * constructor. initializes the complete gui.
	 */
	public ElementChooserPanel(ONDEXGraph aog) {
		this.aog = aog;
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), engl("Fields")));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		createLists();

		add(new JLabel(engl("ANs")));
		add(new JScrollPane(anList));

		add(new JLabel(engl("CCs")));
		add(new JScrollPane(ccList));

		add(new JLabel(engl("RTs")));
		add(new JScrollPane(rtList));

		add(new JLabel(engl("Features")));
		add(new JScrollPane(oList));
	}

	// ####METHODS####

	private String engl(String s) {
		return Config.language.getProperty("Statistics." + s);
	}

	/**
	 * creates and fills all four lists.
	 */
	private void createLists() {
		anListModel = new AttributeNameListModel();
		anList = new JList(anListModel);
		anList.setCellRenderer(new CustomCellRenderer());
		anList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		anList.addListSelectionListener(this);

		for (AttributeName an : aog.getMetaData().getAttributeNames()) {
			if (aog.getConceptsOfAttributeName(an).size() > 0 || aog.getRelationsOfAttributeName(an).size() > 0)
				anListModel.addAttributeName(an);
		}

		ccListModel = new ConceptClassListModel();
		ccList = new JList(ccListModel);
		ccList.setCellRenderer(new CustomCellRenderer());
		ccList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ccList.addListSelectionListener(this);

		for (ConceptClass cc : aog.getMetaData().getConceptClasses()) {
			if (aog.getConceptsOfConceptClass(cc).size() > 0)
				ccListModel.addConceptClass(cc);
		}

		rtListModel = new RelationTypeListModel();
		rtList = new JList(rtListModel);
		rtList.setCellRenderer(new CustomCellRenderer());
		rtList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rtList.addListSelectionListener(this);

		for (RelationType rt : aog.getMetaData().getRelationTypes()) {
			if (aog.getRelationsOfRelationType(rt).size() > 0)
				rtListModel.addRelationType(rt);
		}

		oListModel = new DefaultListModel();
		oList = new JList(oListModel);
		oList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		oList.addListSelectionListener(this);
		oListModel.addElement("degree");
	}

	/**
	 * takes care of the current selection. whenever the user clicks on a graph
	 * element in any of the four lists this methods makes sure that the entry
	 * is made to the currentSelection variable and also that all other
	 * selections are removed.
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			JList list = (JList) e.getSource();
			if (list.getSelectedValue() != null) {
				Object val = list.getSelectedValue();
				String id = (val instanceof JLabel) ? ((JLabel) val).getName() : (String) val;
				if (list.equals(anList)) {
					currentSelection = aog.getMetaData().getAttributeName(id);
					ccList.clearSelection();
					rtList.clearSelection();
					oList.clearSelection();
				} else if (list.equals(ccList)) {
					anList.clearSelection();
					currentSelection = aog.getMetaData().getConceptClass(id);
					rtList.clearSelection();
					oList.clearSelection();
				} else if (list.equals(rtList)) {
					anList.clearSelection();
					ccList.clearSelection();
					currentSelection = aog.getMetaData().getRelationType(id);
					oList.clearSelection();
				} else if (list.equals(oList)) {
					anList.clearSelection();
					ccList.clearSelection();
					rtList.clearSelection();
					currentSelection = id;
				}
			}
		}
	}

	/**
	 * automatically adds a given element to the correct list.
	 * 
	 * @param o
	 *            either a ConceptClass, RelationType, AttributeName or a
	 *            String.
	 */
	public void addElement(Object o) {
		if (o instanceof AttributeName) {
			anListModel.addAttributeName((AttributeName) o);
			anList.clearSelection();
			anList.revalidate();
			anList.repaint();
		} else if (o instanceof ConceptClass) {
			ccListModel.addConceptClass((ConceptClass) o);
			ccList.clearSelection();
			ccList.revalidate();
			ccList.repaint();
		} else if (o instanceof RelationType) {
			rtListModel.addRelationType((RelationType) o);
			rtList.clearSelection();
			rtList.revalidate();
			rtList.repaint();
		} else if (o instanceof String) {
			oListModel.addElement(o);
			oList.clearSelection();
			oList.revalidate();
			oList.repaint();
		}
		currentSelection = null;
	}

	/**
	 * pops the currently selected element, i.e. removes it from the list clears
	 * the selection and returns it to the querying method.
	 * 
	 * @return the currently selected element.
	 */
	public Object popElement() {
		int index;
		if (currentSelection instanceof AttributeName) {
			index = anList.getSelectedIndex();
			anListModel.remove(index);
			anList.clearSelection();
			anList.revalidate();
			anList.repaint();
		} else if (currentSelection instanceof ConceptClass) {
			index = ccList.getSelectedIndex();
			ccListModel.remove(index);
			ccList.clearSelection();
			ccList.revalidate();
			ccList.repaint();
		} else if (currentSelection instanceof RelationType) {
			index = rtList.getSelectedIndex();
			rtListModel.remove(index);
			rtList.clearSelection();
			rtList.revalidate();
			rtList.repaint();
		} else if (currentSelection instanceof String) {
			index = oList.getSelectedIndex();
			oListModel.remove(index);
			oList.clearSelection();
			oList.revalidate();
			oList.repaint();
		}
		Object out = currentSelection;
		currentSelection = null;
		return out;

	}

	/**
	 * @return whether or not a selection currently exists.
	 */
	public boolean isElementSelected() {
		return currentSelection != null;
	}

}
