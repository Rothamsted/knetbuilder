package net.sourceforge.ondex.ovtk2.reusable_functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;

/**
 * Convenience functions for dealing with common visualisation tasks.
 * 
 * @author lysenkoa
 * 
 */
public class VisualisationExtension {

	private VisualisationExtension() {
	}

	/**
	 * Makes all of the relations visible, if their target and source are
	 * currently visible
	 */
	public static void showConnectingRelations(OVTK2PropertiesAggregator viewer) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXRelation r : graph.getRelations()) {
			ONDEXConcept c1 = r.getFromConcept();
			ONDEXConcept c2 = r.getToConcept();
			if (graph.isVisible(c1) && graph.isVisible(c2)) {
				graph.setVisibility(r, true);
			}
		}
		viewer.updateViewer(null);
	}

	/**
	 * Shows all other members of all the contexts that the selected concept(s)
	 * are members of
	 * 
	 * @param viewer
	 */
	public static void showRelevantContexts(OVTK2PropertiesAggregator viewer) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		Set<ONDEXConcept> contexts = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : viewer.getPickedNodes().toArray(new ONDEXConcept[0])) {
			contexts.addAll(c.getTags());
		}
		for (ONDEXConcept contextConcept : contexts) {
			graph.setVisibility(graph.getConceptsOfTag(contextConcept), true);
			graph.setVisibility(graph.getRelationsOfTag(contextConcept), true);
		}
	}

	/**
	 * Shows all of the members of context(s) selected in the viewer
	 * 
	 * @param viewer
	 */
	public static void showContextMembers(OVTK2PropertiesAggregator viewer) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept contextConcept : viewer.getPickedNodes().toArray(new ONDEXConcept[0])) {
			graph.setVisibility(graph.getConceptsOfTag(contextConcept), true);
			graph.setVisibility(graph.getRelationsOfTag(contextConcept), true);
		}
	}

	/**
	 * Shows all other members of all the contexts that the selected concept(s)
	 * are members of, if the contexts are of particular class
	 * 
	 * @param viewer
	 * @param contextClasses
	 */
	public static void showRelevantContextsByTypes(OVTK2PropertiesAggregator viewer, String... contextClasses) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		Set<ONDEXConcept> contexts = new HashSet<ONDEXConcept>();
		Set<ConceptClass> selectedClasses = new HashSet<ConceptClass>();
		for (String s : contextClasses) {
			createCC(viewer.getONDEXJUNGGraph(), s);
		}

		for (ONDEXConcept c : viewer.getPickedNodes().toArray(new ONDEXConcept[0])) {
			for (ONDEXConcept candidate : c.getTags()) {
				if (selectedClasses.contains(candidate.getOfType()))
					contexts.add(candidate);
			}
		}

		for (ONDEXConcept contextConcept : contexts) {
			graph.setVisibility(graph.getConceptsOfTag(contextConcept), true);
			graph.setVisibility(graph.getRelationsOfTag(contextConcept), true);
		}
	}

	/**
	 * Place a set of <code>neighbour</code> concepts in a circular pattern
	 * around a <code>center</code> concept.
	 * 
	 * @param viewer
	 * @param center
	 * @param neighbours
	 */
	public static void layoutNeighbours(OVTK2PropertiesAggregator viewer, int center, int[] neighbours) {
		// System.err.println("Layout method was called");
		try {
			ONDEXConcept centerNode = viewer.getONDEXJUNGGraph().getConcept(center);
			Set<ONDEXConcept> neighbourNodes = new HashSet<ONDEXConcept>(neighbours.length);
			for (int n : neighbours) {
				neighbourNodes.add(viewer.getONDEXJUNGGraph().getConcept(n));
			}
			// System.err.println("Layout for neighbours: " + neighbourNodes);
			LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(), centerNode, neighbourNodes);
			// System.err.println("Layout method finished successfully");
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static void refreshMetaGraphLabels(OVTK2PropertiesAggregator
	 * viewer) { viewer.getMetaGraphPanel() .getVisualizationViewer()
	 * .getRenderContext() .setVertexLabelTransformer( new
	 * ONDEXMetaConceptLabels(viewer.getONDEXJUNGGraph()));
	 * viewer.getMetaGraphPanel() .getVisualizationViewer() .getRenderContext()
	 * .setEdgeLabelTransformer( new
	 * ONDEXMetaRelationLabels(viewer.getONDEXJUNGGraph())); }
	 */

	/*
	 * public static void refreshMetaGraphLabels(OVTK2PropertiesAggregator
	 * viewer) { viewer.getMetaGraphPanel() .getVisualizationViewer()
	 * .getRenderContext() .setVertexLabelTransformer( new
	 * ONDEXMetaConceptLabels(viewer.getONDEXJUNGGraph()));
	 * viewer.getMetaGraphPanel() .getVisualizationViewer() .getRenderContext()
	 * .setEdgeLabelTransformer( new
	 * ONDEXMetaRelationLabels(viewer.getONDEXJUNGGraph())); }
	 */

	public static void setVisibility(final OVTK2PropertiesAggregator viewer, final ONDEXConcept c, final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
				graph.setVisibility(c, visible);
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				viewer.getVisualizationViewer().repaint();
			}
		});

	}

	public static void setVisibility(final OVTK2PropertiesAggregator viewer, final ONDEXRelation r, final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
				graph.setVisibility(r, visible);
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		});
	}

	public static void setVisibility(final OVTK2PropertiesAggregator viewer, final Set<ONDEXEntity> es, final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
				graph.setVisibility(es, visible);
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		});
	}

	public static void setVisibility(final OVTK2PropertiesAggregator viewer, final ONDEXConcept[] cs, final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
				for (ONDEXConcept c : cs) {
					graph.setVisibility(c, visible);
				}
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		});
	}

	public static void setVisibility(final OVTK2PropertiesAggregator viewer, final ONDEXRelation[] rs, final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
				for (ONDEXRelation r : rs) {
					graph.setVisibility(r, visible);
				}
				viewer.getVisualizationViewer().getModel().fireStateChanged();
			}
		});
	}

	public static boolean isVisible(final OVTK2PropertiesAggregator viewer, final ONDEXConcept c) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		return graph.isVisible(c);
	}

	public static boolean isVisible(final OVTK2PropertiesAggregator viewer, final ONDEXRelation r) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		return graph.isVisible(r);
	}
}
