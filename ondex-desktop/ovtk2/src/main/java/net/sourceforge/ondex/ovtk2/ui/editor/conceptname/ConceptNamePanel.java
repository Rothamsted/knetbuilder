package net.sourceforge.ondex.ovtk2.ui.editor.conceptname;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Panel to display all concept names of all concepts in the graph.
 * 
 * @author taubertj
 * 
 */
public class ConceptNamePanel extends JLayeredPane {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 536997163313647819L;

	/**
	 * JTable displaying lists of concept names
	 */
	JTable table;

	/**
	 * Initialises table data with the given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph to extract concept names from
	 */
	public ConceptNamePanel(ONDEXGraph graph) {

		// iterate over all concepts
		Set<ONDEXConcept> view = graph.getConcepts();

		Object[][] data = new Object[view.size()][];

		// maximum number of names per concept
		int maxNames = 0;
		Iterator<ONDEXConcept> viewI = view.iterator();
		for (int j = 0; viewI.hasNext(); j++) {

			// get concept and all names of it
			ONDEXConcept concept = viewI.next();
			Set<ConceptName> it = concept.getConceptNames();
			if (it.size() > maxNames)
				maxNames = it.size();

			// sort concept names before
			ConceptName[] names = it.toArray(new ConceptName[0]);
			Arrays.sort(names, new ConceptNameComparator());

			// first entry in table row is concept itself
			Object[] row = new Object[names.length + 1];
			row[0] = concept;
			System.arraycopy(names, 0, row, 1, names.length);
			data[j] = row;
		}
		// to account for concept id
		maxNames++;

		// construct table header
		String[] header = new String[maxNames];
		header[0] = "id";
		for (int i = 1; i < maxNames; i++) {
			header[i] = "name";
		}

		// initialise table model and populate table
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, header);
		table = new ConceptNameTable(model);

		// one extra after maxNames for new concept names
		model.addColumn("name");

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
