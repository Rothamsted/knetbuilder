package net.sourceforge.ondex.ovtk2.ui.popup.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;

/**
 * Ondex queries the Syngenta webservice with the accession numbers for
 * chemicals, and then creates concepts from the results and adds them to the
 * graph
 * 
 * @author taubertj
 * 
 */
public class LinkLookupByCSNItem extends EntityMenuItem<ONDEXConcept> {

	@Override
	public boolean accepts() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		DataSource dsSyngenta = graph.getMetaData().getDataSource("Syngenta");

		// look at all selected concepts
		for (ONDEXConcept c : entities) {
			for (ConceptAccession ca : c.getConceptAccessions()) {
				// at least one accession with source Syngenta
				if (ca.getElementOf().equals(dsSyngenta)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected void doAction() {

		// get meta data
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		DataSource dsSyngenta = graph.getMetaData().getDataSource("Syngenta");

		// parse all accessions contained in graph
		Map<String, Set<ONDEXConcept>> accessions = new HashMap<String, Set<ONDEXConcept>>();
		for (ONDEXConcept c : entities) {
			for (ConceptAccession ca : c.getConceptAccessions()) {
				if (ca.getElementOf().equals(dsSyngenta)) {
					if (!accessions.containsKey(ca.getAccession()))
						accessions.put(ca.getAccession(),
								new HashSet<ONDEXConcept>());
					accessions.get(ca.getAccession()).add(c);
				}
			}
		}

		for (String accession : accessions.keySet()) {
			System.out.println(accession);
		}
	}

	@Override
	public MENUCATEGORY getCategory() {
		return MENUCATEGORY.LINK;
	}

	@Override
	protected String getMenuPropertyName() {
		return "Viewer.VertexMenu.LinkLookupByCSN";
	}

	@Override
	protected String getUndoPropertyName() {
		return "";
	}

}
