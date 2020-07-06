package net.sourceforge.ondex.ovtk2.ui.editor.concept;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.editor.util.Util;

/**
 * Contains list of all concepts in graph.
 * 
 * @author taubertj
 * 
 */
public class ConceptPanel extends JPanel {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -2856081726319394153L;

	/**
	 * JTable displaying lists of concept
	 */
	JTable table;

	/**
	 * Predefined table headers
	 */
	static Object[] header = { "id", "pid", "annotation", "description", "element of", "of type", "evidence" };

	/**
	 * Initialises table data with the given ONDEXGraph.
	 * 
	 * @param graph
	 *            ONDEXGraph to extract concepts from
	 */
	public ConceptPanel(ONDEXGraph graph) {

		Object[][] data = new Object[graph.getConcepts().size()][];

		int i = 0;
		for (ONDEXConcept concept : graph.getConcepts()) {
			// first entry in table row is concept itself
			Object[] row = { concept, concept.getPID(), concept.getAnnotation(), concept.getDescription(), concept.getElementOf(), concept.getOfType(), concept.getEvidence() };
			data[i] = row;
			i++;
		}

		// initialise table model and populate table
		DefaultTableModel model = new DefaultTableModel();
		model.setDataVector(data, header);
		table = new ConceptTable(model, graph);

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
