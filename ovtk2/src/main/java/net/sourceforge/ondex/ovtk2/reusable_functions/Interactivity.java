package net.sourceforge.ondex.ovtk2.reusable_functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;

import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.subgraph.Subgraph;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Collection of functions to do with user interaction with the graph.
 * 
 * @author lysenkoa
 * 
 */
public class Interactivity {

	private Interactivity() {
	}

	/**
	 * Get a set of currently selected concepts in the viewer
	 * 
	 * @return set of Ondex concepts
	 */
	public static Set<ONDEXConcept> getPickedConcepts(OVTK2PropertiesAggregator viewer) {
		return viewer.getPickedNodes();
	}

	/**
	 * Get a set of currently selected concepts in the viewer
	 * 
	 * @return set of Ondex relations
	 */
	public static Set<ONDEXRelation> getPickedRelations(OVTK2PropertiesAggregator viewer) {
		return viewer.getPickedEdges();
	}

	/**
	 * Get a subgraph of concepts and relations currently selected in the viewer
	 * 
	 * @return subgraph object
	 */
	public static Subgraph getPickedSubgraph(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		return new Subgraph(viewer.getPickedNodes(), viewer.getPickedEdges(), graph);
	}

	/**
	 * This function will create a context from a set of concepts and relations
	 * currently selected in the graph
	 * 
	 * @param viewer
	 *            - viewer
	 * @param name
	 *            - name that the new context will have
	 */
	public static void createContextFromSelection(OVTK2PropertiesAggregator viewer, String name) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXConcept context = graph.getFactory().createConcept(name, createDataSource(graph, "unknown"), createCC(graph, "Thing"), createEvidence(graph, "manual"));
		context.createConceptName(name, true);
		PickedState<ONDEXRelation> stateE = viewer.getVisualizationViewer().getPickedEdgeState();

		Set<ONDEXRelation> setE = stateE.getPicked();
		for (ONDEXRelation e : setE) {
			graph.getRelation(e.getId()).addTag(context);
		}

		PickedState<ONDEXConcept> stateN = viewer.getVisualizationViewer().getPickedVertexState();
		Set<ONDEXConcept> setN = stateN.getPicked();
		for (ONDEXConcept n : setN) {
			graph.getConcept(n.getId()).addTag(context);
		}
	}
}
