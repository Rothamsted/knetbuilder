package net.sourceforge.ondex.ovtk2.ui.editor.relation;

import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Displays a table with all relations in the graph.
 * 
 * @author taubertj
 * 
 */
public class RelationPanel extends JPanel {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7009058433093288297L;

	/**
	 * JTable displaying lists of relations
	 */
	JTable table;

	/**
	 * Predefined table headers
	 */
	static Object[] header = { "id", "from concept", "to concept", "of type", "evidence" };

	/**
	 * Initialises table data with the given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph to extract relations from
	 */
	public RelationPanel(ONDEXGraph graph) {

		// iterate over all relations
		Set<ONDEXRelation> view = graph.getRelations();

		Object[][] data = new Object[view.size()][];

		int i = 0;
		for (ONDEXRelation relation : view) {

			// first entry in table row is relation itself
			Object[] row = { relation, relation.getFromConcept(), relation.getToConcept(), relation.getOfType(), relation.getEvidence() };
			data[i] = row;
			i++;
		}

		// initialise table model and populate table
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, header);
		table = new RelationTable(model, graph);

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
