package net.sourceforge.ondex.ovtk2.ui.editor.conceptaccession;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Panel to display all concept accessions of all concepts in the graph.
 * 
 * @author taubertj
 * 
 */
public class ConceptAccessionPanel extends JLayeredPane {

	/**
	 * generated
	 */
	private static final long serialVersionUID = 536997163313647820L;

	/**
	 * JTable displaying lists of concept accessions
	 */
	JTable table;

	/**
	 * Initialises table data with the given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph to extract concept accessions from
	 */
	public ConceptAccessionPanel(ONDEXGraph graph) {

		// iterate over all concepts
		Set<ONDEXConcept> view = graph.getConcepts();

		Object[][] data = new Object[view.size()][];

		// maximum number of accessions per concept
		int maxAccs = 0;
		Iterator<ONDEXConcept> viewI = view.iterator();
		for (int j = 0; viewI.hasNext(); j++) {

			// get concept and all accessions of it
			ONDEXConcept concept = viewI.next();
			Set<ConceptAccession> it = concept.getConceptAccessions();
			if (it.size() > maxAccs)
				maxAccs = it.size();

			// sort concept accessions
			ConceptAccession[] accs = it.toArray(new ConceptAccession[0]);
			Arrays.sort(accs, new ConceptAccessionComparator());

			// first entry in table row is concept itself
			Object[] row = new Object[accs.length + 1];
			row[0] = concept;
			System.arraycopy(accs, 0, row, 1, accs.length);
			data[j] = row;
		}
		// to account for concept id
		maxAccs++;

		// construct table header
		String[] header = new String[maxAccs];
		header[0] = "id";
		for (int i = 1; i < maxAccs; i++) {
			header[i] = "accession";
		}

		// initialise table model and populate table
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, header);
		table = new ConceptAccessionTable(model, graph);

		// one extra after maxAccs for new concept accessions
		model.addColumn("accession");

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
