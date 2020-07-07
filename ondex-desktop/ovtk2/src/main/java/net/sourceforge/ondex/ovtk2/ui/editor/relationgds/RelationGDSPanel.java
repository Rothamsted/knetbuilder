package net.sourceforge.ondex.ovtk2.ui.editor.relationgds;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.editor.util.AttributeNameComparator;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Panel to display all relation Attribute of all relations in the graph.
 * 
 * @author taubertj
 */
public class RelationGDSPanel extends JLayeredPane {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -6295425268830637303L;

	/**
	 * JTable displaying lists of relation Attribute
	 */
	JTable table;

	/**
	 * Initialises table data with the given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph to extract relation Attribute from
	 */
	public RelationGDSPanel(ONDEXGraph graph) {
		// get all attribute names present on relations
		Set<AttributeName> ans = new HashSet<AttributeName>();
		for (ONDEXRelation relation : graph.getRelations()) {
			// get all Attribute for current relation
			for (Attribute attribute : relation.getAttributes()) {
				// add attribute name to set
				ans.add(attribute.getOfType());
			}
		}

		// array for sorting attribute names
		AttributeName[] anArray = ans.toArray(new AttributeName[0]);
		Arrays.sort(anArray, new AttributeNameComparator());

		// populate data model
		Set<ONDEXRelation> view = graph.getRelations();
		Object[][] data = new Object[view.size()][];

		Iterator<ONDEXRelation> viewI = view.iterator();
		for (int j = 0; viewI.hasNext(); j++) {

			// get relation and all Attribute of it
			ONDEXRelation relation = viewI.next();

			// first entry in table row is relation itself
			Object[] row = new Object[anArray.length + 1];
			row[0] = relation;
			for (int i = 0; i < anArray.length; i++) {
				row[i + 1] = relation.getAttribute(anArray[i]);
			}
			data[j] = row;
		}

		// construct table header
		String[] header = new String[anArray.length + 1];
		header[0] = "id";
		for (int i = 0; i < anArray.length; i++) {
			header[i + 1] = anArray[i].getId();
		}

		// initialise table model and populate table
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, header);
		table = new RelationGDSTable(model, graph);

		// add table to panel
		this.setLayout(new GridLayout(1, 1));
		this.add(new JScrollPane(table));
		Util.calcColumnWidths(table, 150);
	}

	/**
	 * Returns the internal JTable.
	 * 
	 * @return JTable
	 */
	public JTable getTable() {
		return table;
	}
}
