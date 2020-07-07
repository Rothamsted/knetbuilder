package net.sourceforge.ondex.ovtk2.ui.console.functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;

import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
import net.sourceforge.ondex.tools.subgraph.Subgraph;

/**
 * A collection of application specific functions not worthy of a separate class
 * file.
 * 
 * 
 * @author lysenkoa
 * 
 */
public class Assorted {

	private Assorted() {
	}

	/**
	 * Kegg pathway reduction. Remove all of the pathways that do not contain at
	 * least one concept of the specified context. Create a pathway context for
	 * all of the remaining pathways with an appropriate path concept as a
	 * container.
	 */
	public static void keggFilter(OVTK2PropertiesAggregator viewer, ONDEXConcept list) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		String[][] pattern = new String[][] {};
		List<Subgraph> matches = StandardFunctions.getSubgraphMatch(graph, pattern);
		Set<ONDEXConcept> pathways = graph.getConceptsOfConceptClass(createCC(graph, "Path"));
		Set<ONDEXConcept> listMembers = graph.getConceptsOfTag(list);
		for (Subgraph match : matches) {
			if (BitSetFunctions.and(match.getConcepts(), listMembers).size() == 0) {
				continue;
			}
			ONDEXConcept context = BitSetFunctions.and(match.getConcepts(), pathways).iterator().next();
			match.addTag(context);
		}
		for (Subgraph match : matches) {
			for (ONDEXRelation r : match.getRelations()) {
				if (r.getTags() == null)
					graph.deleteRelation(r.getId());
			}

			for (ONDEXConcept c : match.getConcepts()) {
				if (c.getTags() == null)
					graph.deleteConcept(c.getId());
			}
		}
	}
}
