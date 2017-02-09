package net.sourceforge.ondex.ovtk2.ui.console.functions;

import static net.sourceforge.ondex.ovtk2.reusable_functions.Annotation.setColor;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createDataSource;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createEvidence;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createRT;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getIncomingRelations;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getOtherNode;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.getOutgoingRelations;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.relationsToSources;
import static net.sourceforge.ondex.tools.functions.StandardFunctions.relationsToTargets;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JColorChooser;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.ArrayKey;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.reusable_functions.Annotation;
import net.sourceforge.ondex.ovtk2.reusable_functions.DistinctColourMaker;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.ondex.MdHelper;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;

/**
 * To be sorted, do not use.
 * 
 * @author lysenkoa
 * 
 */
@SuppressWarnings("unchecked")
public class ReportFunctions {

	public static void nullTest() throws NullPointerException {
		throw new NullPointerException();
	}

	private static final void getGDSValues(Collection<ONDEXConcept> cs, AttributeName att, Set set) {
		for (ONDEXConcept c : cs) {
			Attribute attribute = c.getAttribute(att);
			if (attribute != null) {
				set.add(attribute.getValue());
			}
		}
	}

	private static Set<ONDEXRelation> withinGroupRelations(ONDEXGraph graph, Collection<ONDEXConcept> cs) {
		Set<ONDEXRelation> rs = new HashSet<ONDEXRelation>();
		for (ONDEXConcept c : cs) {
			Collection<ONDEXRelation> rsOfc = graph.getRelationsOfConcept(c);
			for (ONDEXRelation rOfc : rsOfc) {
				if (cs.contains(getOtherNode(c, rOfc))) {
					rs.add(rOfc);
				}
			}
		}
		return rs;
	}

	private static class SetMapBuilder<K extends Object, V extends Object> {
		private Map<K, Set<V>> map;

		public SetMapBuilder(Map<K, Set<V>> map) {
			this.map = map;
		}

		public void setMap(Map<K, Set<V>> map) {
			this.map = map;
		}

		public void addEntry(K k, V v) {
			Set<V> n = map.get(k);
			if (n == null) {
				n = new HashSet<V>();
				map.put(k, n);
			}
			n.add(v);
		}

		public void addAll(K k, Set<V> set) {
			Set<V> n = map.get(k);
			if (n == null) {
				n = new HashSet<V>();
				map.put(k, n);
			}
			n.addAll(set);
		}

		public Map<K, Set<V>> getSetMap() {
			return map;
		}

		public void clear() {
			map = null;
		}
	}

	public static final void showGOClusters(OVTK2PropertiesAggregator viewer, String ontoCls, String name, String entityCls, String clsuterAtt) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		AttributeName att = MdHelper.createAttName(graph, clsuterAtt, Integer.class);
		Map<Object, Set<ONDEXConcept>> clusterIndex = new HashMap<Object, Set<ONDEXConcept>>();
		SetMapBuilder<Object, ONDEXConcept> sb = new SetMapBuilder<Object, ONDEXConcept>(clusterIndex);
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute attribute = c.getAttribute(att);
			if (attribute != null) {
				sb.addEntry(attribute.getValue(), c);
			}
		}

		ConceptClass cc = createCC(graph, entityCls);
		ONDEXConcept seed = null;
		ConceptClass cls = MdHelper.createCC(graph, ontoCls);
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cls)) {
			for (ConceptName n : c.getConceptNames()) {
				if (n.getName().equals(name)) {
					seed = c;
					break;
				}
			}
		}
		if (seed == null) {
			return;
		}
		Set<ONDEXConcept> terminal = new HashSet<ONDEXConcept>();
		Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
		processed.add(seed);

		Set<ONDEXRelation> rels = getIncomingRelations(graph, seed);
		Set<ONDEXConcept> sources = relationsToSources(rels);

		Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
		while (sources.size() > 0) {
			for (ONDEXConcept z : sources) {
				if (z.getOfType().equals(cc)) {
					terminal.add(z);
					processed.add(z);
				} else if (!processed.contains(z)) {
					rels = getIncomingRelations(graph, z);
					Set<ONDEXConcept> candidates = relationsToSources(rels);
					newSources.addAll(candidates);
				}

			}
			sources = newSources;
			sources.removeAll(processed);
			newSources = new HashSet<ONDEXConcept>();
		}
		Set<Object> set = new HashSet<Object>();
		Set<ONDEXConcept> visibleCs = new HashSet<ONDEXConcept>();
		Set<ONDEXRelation> visibleRs = new HashSet<ONDEXRelation>();
		getGDSValues(terminal, att, set);
		for (Object id : set) {
			Set<ONDEXConcept> group = clusterIndex.get(id);
			visibleCs.addAll(group);
			visibleRs.addAll(withinGroupRelations(graph, group));
		}

		graph.setVisibility(graph.getConcepts(), false);
		graph.setVisibility(graph.getRelations(), false);

		graph.setVisibility(visibleCs, true);
		graph.setVisibility(visibleRs, true);
		viewer.getVisualizationViewer().repaint();
	}

	public static final void filterByGO(OVTK2PropertiesAggregator viewer, String ontoCls, String name, String entityCls) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass cc = createCC(graph, entityCls);
		ONDEXConcept seed = null;
		ConceptClass cls = MdHelper.createCC(graph, ontoCls);
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cls)) {
			for (ConceptName n : c.getConceptNames()) {
				if (n.getName().equals(name)) {
					seed = c;
					break;
				}
			}
		}
		if (seed == null) {
			return;
		}
		Set<ONDEXConcept> terminal = new HashSet<ONDEXConcept>();
		Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
		processed.add(seed);

		Set<ONDEXRelation> rels = getIncomingRelations(graph, seed);
		Set<ONDEXConcept> sources = relationsToSources(rels);

		Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
		while (sources.size() > 0) {
			for (ONDEXConcept z : sources) {
				if (z.getOfType().equals(cc)) {
					terminal.add(z);
					processed.add(z);
				} else if (!processed.contains(z)) {
					rels = getIncomingRelations(graph, z);
					Set<ONDEXConcept> candidates = relationsToSources(rels);
					newSources.addAll(candidates);
				}

			}
			sources = newSources;
			sources.removeAll(processed);
			newSources = new HashSet<ONDEXConcept>();
		}

		Set<ONDEXRelation> terminalrelations = new HashSet<ONDEXRelation>();

		for (ONDEXConcept t : terminal) {
			for (ONDEXRelation tr : graph.getRelationsOfConcept(t)) {
				if (terminal.contains(tr.getToConcept()) && terminal.contains(tr.getFromConcept())) {
					terminalrelations.add(tr);
				}
			}
		}
		System.out.println("Resetting visibility");

		graph.setVisibility(graph.getConcepts(), false);
		graph.setVisibility(graph.getRelations(), false);

		graph.setVisibility(terminal, true);
		graph.setVisibility(terminalrelations, true);
		viewer.getVisualizationViewer().repaint();

	}

	public static final void colorByGO(OVTK2PropertiesAggregator viewer, String ontoCls, String name, String entityCls, int r, int g, int b) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass cc = createCC(graph, entityCls);
		Color color = new Color(r, g, b);
		ONDEXConcept seed = null;
		ConceptClass cls = MdHelper.createCC(graph, ontoCls);
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cls)) {
			for (ConceptName n : c.getConceptNames()) {
				if (n.getName().equals(name)) {
					seed = c;
					break;
				}
			}
		}
		if (seed == null) {
			return;
		}
		Set<ONDEXConcept> terminal = new HashSet<ONDEXConcept>();
		Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
		processed.add(seed);
		setColor(viewer, seed, color);
		Set<ONDEXRelation> rels = getIncomingRelations(graph, seed);
		Set<ONDEXConcept> sources = relationsToSources(rels);
		for (ONDEXConcept c : sources) {
			setColor(viewer, c, color);
		}
		for (ONDEXRelation rel : rels) {
			setColor(viewer, rel, color);
		}
		Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
		while (sources.size() > 0) {
			for (ONDEXConcept z : sources) {
				if (z.getOfType().equals(cc)) {
					terminal.add(z);
					setColor(viewer, z, color);
					processed.add(z);
				} else if (!processed.contains(z)) {
					setColor(viewer, z, color);
					rels = getIncomingRelations(graph, z);
					for (ONDEXRelation rel : rels) {
						setColor(viewer, rel, color);
					}
					Set<ONDEXConcept> candidates = relationsToSources(rels);
					newSources.addAll(candidates);
				}

			}
			sources = newSources;
			sources.removeAll(processed);
			newSources = new HashSet<ONDEXConcept>();
		}

		for (ONDEXConcept t : terminal) {
			for (ONDEXRelation tr : graph.getRelationsOfConcept(t)) {
				if (terminal.contains(tr.getToConcept()) && terminal.contains(tr.getFromConcept())) {
					setColor(viewer, tr, color);
				}
			}
		}
	}

	private static String CGDS = "concept_gds";
	private static String CCV = "concept_cv";
	private static String RELGDS = "relation_gds";
	private static String RELCV = "relation_cv";

	/**
	 * Removes all contexts from the graph and assigns the correct pathway
	 * contexts. Only works for KEGG(graph must be compatible to version at
	 * 12.08.09) Used in Lysenko et. al. (2009)
	 * 
	 * @param viewer
	 *            - ovtk viewer
	 */
	public static void correctKeggContext(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept c : graph.getConcepts()) {
			for (ONDEXConcept co : c.getTags().toArray(new ONDEXConcept[0])) {
				c.removeTag(co);
			}
		}

		ConceptClass path = graph.getMetaData().getConceptClass("Path");
		for (ONDEXConcept c : graph.getConcepts()) {
			if (c.getOfType().equals(path)) {
				c.addTag(c);
			}
		}

		/*
		 * String[][] pattern = new String[][]{ new String[]{"Path"}, new
		 * String[]{"m_isp"}, new String[]{"Reaction"}, new
		 * String[]{"pd_by","cs_by","ca_by","cat_c"}, new
		 * String[]{"Comp","Enzyme","EC"}, new
		 * String[]{"part_of","in_by","ac_by","bi_to","is_a"}, new
		 * String[]{"Protcmplx","Protein"} }; List<Subgraph> subs =
		 * StandardFunctions.getSubgraphMatch(graph, pattern);
		 * System.err.println("Found subnetworks: "+subs.size()); ConceptClass
		 * path = graph.getMetaData().getConceptClass("Path"); for(Subgraph sub
		 * :subs){ Set<ONDEXConcept> ms =sub.getConcepts(); while(ms.hasNext()){
		 * ONDEXConcept m = ms.next(); if(m.getOfType().equals(path)){
		 * sub.addContext(m); break; } } ms.close(); }
		 */
	}

	public static void correctUniprotAccession(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass prot = graph.getMetaData().getConceptClass("Protein");
		DataSource nc_np = graph.getMetaData().getDataSource("NC_NP");
		for (ONDEXConcept p : graph.getConceptsOfConceptClass(prot)) {
			List<String> newAcc = new ArrayList<String>();
			for (ConceptAccession ca : p.getConceptAccessions()) {
				if (ca.getElementOf().equals(nc_np)) {
					if (ca.getAccession().contains(".")) {
						newAcc.add(ca.getAccession().split("\\.")[0]);
					}
				}
			}
			for (String str : newAcc) {
				p.createConceptAccession(str, nc_np, true);
			}
		}
	}

	public static String intersectionReport(ONDEXGraph graph, String type, String... cvs) {
		return _intersectionReport(graph, null, type, cvs);
	}

	public static String intersectionReportForAttName(ONDEXGraph graph, String type, String attName, String... cvs) {
		return _intersectionReport(graph, attName, type, cvs);
	}

	public static void labelPosition(OVTK2PropertiesAggregator viewer, String pos) {
		VisualizationViewer vv = viewer.getVisualizationViewer();
		edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position p = null;
		if (pos.equals("C")) {
			p = Renderer.VertexLabel.Position.CNTR;
		} else if (pos.equals("E")) {
			p = Renderer.VertexLabel.Position.E;
		} else if (pos.equals("W")) {
			p = Renderer.VertexLabel.Position.W;
		} else if (pos.equals("N")) {
			p = Renderer.VertexLabel.Position.N;
		} else if (pos.equals("S")) {
			p = Renderer.VertexLabel.Position.S;
		} else if (pos.equals("A")) {
			p = Renderer.VertexLabel.Position.AUTO;
		}
		viewer.getVisualizationViewer().getRenderer().getVertexLabelRenderer().setPosition(p);
		;
		viewer.getVisualizationViewer().repaint();
	}

	/**
	 * Makes all of the relations visible, if their target and source are
	 * currently visible
	 */
	public static void showAllRelations(OVTK2PropertiesAggregator viewer) {
		net.sourceforge.ondex.ovtk2.reusable_functions.VisualisationExtension.showConnectingRelations(viewer);
	}

	/**
	 * Removes irrelevant parts of the ontology - those branches that do not
	 * have any annotation entries. Only works correctly if the graph contains
	 * just the ontology and annotation entries.
	 * 
	 * @param viewer
	 *            - ovtk viewer
	 * @param copnceptClass
	 *            - seed concept class of the annotated concepts
	 */
	public static void treePruner(OVTK2PropertiesAggregator viewer, String copnceptClass, String... excludeRelationTypes) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		Set<RelationType> toExcludeTmp = new HashSet<RelationType>();
		for (String rt : excludeRelationTypes) {
			toExcludeTmp.add(createRT(graph, rt));
		}
		RelationType[] toExclude = toExcludeTmp.toArray(new RelationType[toExcludeTmp.size()]);
		Set<ONDEXConcept> toKeep = new HashSet<ONDEXConcept>();
		Set<ONDEXConcept> sources = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(createCC(graph, copnceptClass))) {
			toKeep.add(c);
			sources.addAll(relationsToTargets(getOutgoingRelations(graph, c)));
		}

		Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
		while (sources.size() > 0) {
			for (ONDEXConcept z : sources) {
				if (!toKeep.contains(z)) {
					toKeep.add(z);
					newSources.addAll(relationsToTargets(getOutgoingRelations(graph, z, toExclude)));
					// newSources.addAll(relationsToSources(getIncomingRelations(graph,
					// z, toExclude)));
				}
			}
			sources = newSources;
			newSources = new HashSet<ONDEXConcept>();
			sources.removeAll(toKeep);
			System.err.println(sources.size());
		}
		System.err.println("Set contains: " + toKeep.size());
		System.err.println("Finished 1");
		Set<ONDEXConcept> del = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConcepts()) {
			if (!toKeep.contains(c)) {
				del.add(c);
			}
		}
		System.err.println("Finished 2");
		toKeep.clear();
		for (ONDEXConcept c : del) {
			// graph.deleteConcept(c.getId());
			jung.setVisibility(c, false);
		}
		System.err.println("Finished 3");
	}

	public static void shapeAsLabel(OVTK2PropertiesAggregator viewer) {
		VisualizationViewer vv = viewer.getVisualizationViewer();
		viewer.getVisualizationViewer().getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		;
		vv.getRenderContext().setVertexShapeTransformer(new VertexLabelAsShapeRenderer<String, Number>(vv.getRenderContext()));
		viewer.getVisualizationViewer().repaint();
	}

	public static void resizeFont(OVTK2PropertiesAggregator viewer, final float maxSize, final float minSize, String gdsArg, final double gdsMax) {
		VisualizationViewer vv = viewer.getVisualizationViewer();
		final ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		final AttributeName an = graph.getMetaData().getAttributeName(gdsArg);

		final Font newFont = new Font("Calibri", Font.BOLD, (int) maxSize);
		viewer.getVisualizationViewer().getRenderContext().setVertexFontTransformer(new Transformer<ONDEXConcept, Font>() {
			@Override
			public Font transform(ONDEXConcept n) {
				ONDEXConcept c = graph.getConcept(n.getId());
				Attribute attribute = c.getAttribute(an);
				if (attribute != null) {
					Float size = (float) (((Double) attribute.getValue() / (Double) gdsMax) * (maxSize - minSize) + minSize);
					return newFont.deriveFont(size);
				}
				return newFont;
			}
		});
		viewer.getVisualizationViewer().repaint();
	}

	public static void setFontSize(OVTK2PropertiesAggregator viewer, int size) {
		VisualizationViewer vv = viewer.getVisualizationViewer();
		final ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		final Font newFont = new Font("Calibri", Font.BOLD, size);
		viewer.getVisualizationViewer().getRenderContext().setVertexFontTransformer(new Transformer<ONDEXConcept, Font>() {
			@Override
			public Font transform(ONDEXConcept n) {
				return newFont;
			}
		});
		viewer.getVisualizationViewer().repaint();
	}

	public static void showByContext(OVTK2PropertiesAggregator viewer, String conceptClass) {
		ONDEXJUNGGraph graph = viewer.getONDEXJUNGGraph();
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
		Set<ONDEXConcept> set = state.getPicked();
		Set<ONDEXConcept> toShow = new HashSet<ONDEXConcept>();
		DistinctColourMaker dc = new DistinctColourMaker(21);
		Map<ONDEXConcept, Color> colors = new HashMap<ONDEXConcept, Color>();
		ConceptClass cc = graph.getMetaData().getConceptClass(conceptClass);
		Set<ONDEXConcept> exclude = graph.getConceptsOfConceptClass(cc);
		Set<ONDEXConcept> contexts = new HashSet<ONDEXConcept>();
		for (ONDEXConcept n : set) {
			ONDEXConcept context = graph.getConcept(n.getId());
			Color color = dc.getNextColor();
			contexts.add(context);
			Annotation.setColor(viewer, context, color);
			Collection<ONDEXConcept> subset = BitSetFunctions.andNot(graph.getConceptsOfTag(context), exclude);
			for (ONDEXConcept c : subset) {
				Annotation.setColor(viewer, c, color);
				colors.put(c, color);
			}
			toShow.addAll(subset);
		}
		Set<ONDEXRelation> rToShow = new HashSet<ONDEXRelation>();
		for (ONDEXConcept c : toShow) {
			graph.setVisibility(c, true);
			rToShow.addAll(graph.getRelationsOfConcept(c));
		}
		for (ONDEXRelation r : rToShow) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			if (toShow.contains(from) && toShow.contains(to)) {
				graph.setVisibility(r, true);
				Collection<ONDEXConcept> contextsOfC = BitSetFunctions.and(to.getTags(), from.getTags());
				contextsOfC.retainAll(contexts);
				if (contextsOfC.size() > 0) {
					System.err.println(colors.get(r.getFromConcept()));
					Annotation.setColor(viewer, r, colors.get(r.getFromConcept()));
				}
			}
		}
	}

	/**
	 * Counts the number of context members of particular class and stores this
	 * value in a attribute with specified name.
	 * 
	 * @param viewer
	 *            - Ovtk2 viewer
	 * @param membersClass
	 *            - class of members to count
	 * @param arg_name
	 *            - name of an argument for storing the count
	 */
	public static void annotationByContextCounter(OVTK2PropertiesAggregator viewer, String membersClass, String arg_name) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		AttributeName an = createAttName(graph, arg_name, Double.class);
		Set<ONDEXConcept> ccRestriction = graph.getConceptsOfConceptClass(createCC(graph, membersClass));
		for (ONDEXConcept context : graph.getConcepts()) {
			Set<ONDEXConcept> members = graph.getConceptsOfTag(context);
			int count = BitSetFunctions.and(ccRestriction, members).size();
			if (count > 0) {
				context.createAttribute(an, count, false);
			}
		}
	}

	/**
	 * Create annotation counts for a set of selected contexts. Can be used to
	 * produce the annotations counts for any DAG-type structure.
	 * 
	 * @param viewer
	 *            - viewer
	 * @param copnceptClass
	 *            - the concept class to which annotation applies
	 * @param arg_name
	 *            - the name of a attribute argument that will be used to store
	 *            the counts
	 * @param excludeRelationTypes
	 *            - these relation types will NOT be traversed when calculating
	 *            the counts
	 */
	public static void annotationCounter(OVTK2PropertiesAggregator viewer, String copnceptClass, String arg_name, String... excludeRelationTypes) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		AttributeName an = createAttName(graph, arg_name, Double.class);
		Set<RelationType> toExcludeTmp = new HashSet<RelationType>();
		for (String rt : excludeRelationTypes) {
			toExcludeTmp.add(createRT(graph, rt));
		}
		RelationType[] toExclude = toExcludeTmp.toArray(new RelationType[toExcludeTmp.size()]);
		ConceptClass cc = createCC(graph, copnceptClass);
		Set<ONDEXConcept> cs = graph.getConceptsOfConceptClass(cc);
		Map<Integer, Double> counts = new HashMap<Integer, Double>();
		int i = 1;
		int total = cs.size();
		for (ONDEXConcept c : cs) {
			System.err.println("Processing concept " + i + " of " + total + " PID: " + c.getPID());
			Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
			Set<ONDEXConcept> sources = relationsToTargets(getOutgoingRelations(graph, c, toExclude));
			Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
			while (sources.size() > 0) {
				for (ONDEXConcept z : sources) {
					if (!processed.contains(z)) {
						processed.add(z);
						Double count = counts.get(z.getId());
						if (count != null) {
							counts.put(z.getId(), count + 1d);
						} else {
							counts.put(z.getId(), 1d);
						}
						Set<ONDEXConcept> candidates = relationsToTargets(getOutgoingRelations(graph, z, toExclude));
						newSources.addAll(candidates);
					}
				}
				sources = newSources;
				sources.removeAll(processed);
				newSources = new HashSet<ONDEXConcept>();
			}
			i++;
		}
		for (Entry<Integer, Double> ent : counts.entrySet()) {
			if (!graph.getConcept(ent.getKey()).getOfType().equals(cc))
				graph.getConcept(ent.getKey()).createAttribute(an, ent.getValue(), false);
		}
	}

	public static void createContext(OVTK2PropertiesAggregator viewer, String name) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXConcept context = graph.getFactory().createConcept(name, createDataSource(graph, "unknown"), createCC(graph, "Thing"), createEvidence(graph, "manual"));
		context.createConceptName(name, true);
		for (ONDEXConcept c : graph.getConcepts()) {
			c.addTag(context);
		}
	}

	public static void colourByContexts(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		Color color = JColorChooser.showDialog(OVTK2Desktop.getInstance().getMainFrame(), "Choose Background Color", Color.blue);
		if (color == null)
			return;
		System.err.println(color);
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
		Set<ONDEXConcept> set = state.getPicked();
		for (ONDEXConcept n : set) {
			ONDEXConcept context = graph.getConcept(n.getId());
			Annotation.setColor(viewer, context, color);
			for (ONDEXConcept c : graph.getConceptsOfTag(context)) {
				Annotation.setColor(viewer, c, color);
			}
		}
	}

	public static void filterByClassHasRt(OVTK2PropertiesAggregator viewer, String cls, String rt) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		Set<ONDEXConcept> del = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(createCC(graph, cls))) {
			boolean keep = false;
			for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
				if (r.getOfType().getId().equals(rt)) {
					keep = true;
					break;
				}
			}
			if (!keep) {
				del.add(c);
			}
		}
		for (ONDEXConcept c : del) {
			// graph.deleteConcept(c.getId());
			jung.setVisibility(c, false);
		}
	}

	public static void countMembers(OVTK2PropertiesAggregator viewer, String clsOfConctext, String clsOfMemeber) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		AttributeName an = createAttName(graph, clsOfMemeber + "_members", Double.class);
		ConceptClass cls = createCC(graph, clsOfMemeber);
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(createCC(graph, clsOfConctext))) {
			Double count = 0d;
			for (ONDEXConcept oc : graph.getConceptsOfTag(c)) {
				if (oc.getOfType().equals(cls)) {
					count++;
				}
			}
			c.createAttribute(an, count, false);
		}
	}

	public static void setAllToGrey(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		for (ONDEXConcept c : graph.getConcepts()) {
			Annotation.setColor(viewer, c, Color.darkGray);
		}

		for (ONDEXRelation r : graph.getRelations()) {
			Annotation.setColor(viewer, r, Color.darkGray);
		}
	}

	public static void colorByGo(OVTK2PropertiesAggregator viewer, String terminalClass) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ConceptClass cc = createCC(graph, terminalClass);
		Color color = JColorChooser.showDialog(OVTK2Desktop.getInstance().getMainFrame(), "Choose Background Color", Color.blue);
		if (color == null)
			return;
		System.err.println(color);
		PickedState<ONDEXConcept> state = viewer.getVisualizationViewer().getPickedVertexState();
		Set<ONDEXConcept> set = state.getPicked();
		if (set.size() == 0)
			return;
		ONDEXConcept seed = graph.getConcept(set.iterator().next().getId());
		Set<ONDEXConcept> terminal = new HashSet<ONDEXConcept>();
		Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
		processed.add(seed);
		setColor(viewer, seed, color);
		Set<ONDEXRelation> rels = getIncomingRelations(graph, seed);
		Set<ONDEXConcept> sources = relationsToSources(rels);
		for (ONDEXConcept c : sources) {
			setColor(viewer, c, color);
		}
		for (ONDEXRelation rel : rels) {
			setColor(viewer, rel, color);
		}
		Set<ONDEXConcept> newSources = new HashSet<ONDEXConcept>();
		while (sources.size() > 0) {
			for (ONDEXConcept z : sources) {
				if (z.getOfType().equals(cc)) {
					terminal.add(z);
					setColor(viewer, z, color);
					processed.add(z);
				} else if (!processed.contains(z)) {
					setColor(viewer, z, color);
					rels = getIncomingRelations(graph, z);
					for (ONDEXRelation rel : rels) {
						setColor(viewer, rel, color);
					}
					Set<ONDEXConcept> candidates = relationsToSources(rels);
					newSources.addAll(candidates);
				}

			}
			sources = newSources;
			sources.removeAll(processed);
			newSources = new HashSet<ONDEXConcept>();
		}

		for (ONDEXConcept t : terminal) {
			for (ONDEXRelation tr : graph.getRelationsOfConcept(t)) {
				if (terminal.contains(tr.getToConcept()) && terminal.contains(tr.getFromConcept())) {
					setColor(viewer, tr, color);
				}
			}
		}
	}

	public static void goFilter(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		Set<ONDEXConcept> del = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConcepts()) {
			if (c.getPID().equals("GO:0008150")) {
				System.err.println("Found: GO:0008150");
				del.add(c);
			} else if (c.getPID().equals("GO:0005575")) {
				System.err.println("Found: GO:0005575");
				del.add(c);
			} else if (c.getPID().equals("GO:0003674")) {
				System.err.println("Found: GO:0003674");
				del.add(c);
			}
		}
		for (ONDEXConcept c : del) {
			graph.deleteConcept(c.getId());
		}
	}

	public static void writeFasta(ONDEXGraph graph, String fileName, String seqType, String accType) {
		AttributeName st = graph.getMetaData().getAttributeName(seqType);
		DataSource dataSource = graph.getMetaData().getDataSource(accType);
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(st)) {
				String accession = null;
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (ca.getElementOf().equals(dataSource) && !ca.isAmbiguous()) {
						accession = ca.getAccession();
						break;
					}
				}

				if (accession == null) {
					for (ConceptAccession ca : c.getConceptAccessions()) {
						if (ca.getElementOf().equals(dataSource)) {
							accession = ca.getAccession();
							break;
						}
					}
				}
				if (accession == null) {
					System.err.println("Bad entry: " + c.getId());
					continue;
				}
				if (c.getAttribute(st) == null) {
					System.err.println("Skipped: " + accession + " -- no sequence attribute");
					continue;
				} else if (c.getAttribute(st).getValue().toString().trim().equals("")) {
					System.err.println("Skipped: " + accession + " -- empty sequence");
					continue;
				}
				bw.write(">" + accession.trim() + "\n");
				bw.flush();
				bw.write(c.getAttribute(st).getValue().toString().trim() + "\n");
				bw.flush();
			}
			out.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeAccessionList(ONDEXGraph graph, String fileName, String... accTypes) {
		List<DataSource> cvs = new ArrayList<DataSource>();
		for (String accType : accTypes) {
			cvs.add(graph.getMetaData().getDataSource(accType));
		}

		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (ONDEXConcept c : graph.getConcepts()) {
				String accession = null;
				boolean first = true;
				for (DataSource dataSource : cvs) {
					for (ConceptAccession ca : c.getConceptAccessions()) {
						if (ca.getElementOf().equals(dataSource) && !ca.isAmbiguous()) {
							accession = ca.getAccession();
							break;
						}
					}

					if (accession == null) {
						for (ConceptAccession ca : c.getConceptAccessions()) {
							if (ca.getElementOf().equals(dataSource)) {
								accession = ca.getAccession();
								break;
							}
						}
					}
					if (accession == null) {
						if (!first) {
							bw.write("	");
						} else
							first = false;
						bw.write("");
						bw.flush();
					} else {
						if (!first)
							bw.write("	");
						else
							first = false;
						bw.write(accession);
						bw.flush();
					}
				}

				bw.write("\n");
				bw.flush();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void importPsi(ONDEXGraph graph, String dir) {
		Map<String, ONDEXConcept> uIndex = new HashMap<String, ONDEXConcept>();
		Map<String, ONDEXConcept> gIndex = new HashMap<String, ONDEXConcept>();
		DataSource udataSource = graph.getMetaData().getDataSource("UNIPROTKB");
		DataSource gdataSource = graph.getMetaData().getDataSource("GI");
		for (ONDEXConcept c : graph.getConcepts()) {
			String uaccession = null;
			String gaccession = null;
			for (ConceptAccession ca : c.getConceptAccessions()) {
				if (uaccession == null && ca.getElementOf().equals(udataSource) && !ca.isAmbiguous()) {
					uaccession = ca.getAccession();
				}
				if (gaccession == null && ca.getElementOf().equals(gdataSource) && !ca.isAmbiguous()) {
					gaccession = ca.getAccession();
				}
			}

			if (uaccession == null) {
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (ca.getElementOf().equals(udataSource)) {
						uaccession = ca.getAccession();
						break;
					}
				}
			}

			if (gaccession == null) {
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (ca.getElementOf().equals(gdataSource)) {
						gaccession = ca.getAccession();
						break;
					}
				}
			}
			if (uaccession != null && gaccession != null) {
				uIndex.put(uaccession, c);
				gIndex.put(gaccession, c);
			}
		}
		System.err.println("Matching gi numbers: " + gIndex.size());
		System.err.println("Matching uniprot ids: " + uIndex.size());
		int count = 0;
		EvidenceType ev = createEvidence(graph, "PSI_BLAST");
		RelationType rt = createRT(graph, "psi");
		AttributeName eval = createAttName(graph, "BLEV", Double.class);
		File directory = new File(dir);
		Set<String> notFound = new HashSet<String>();
		for (String file : directory.list()) {
			file = directory.getAbsolutePath() + "/" + file;
			System.err.println("Now processing: " + file);
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(file));
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine = null;
				Pattern gip = Pattern.compile("gi\\|([0-9]+)\\|");
				while ((strLine = br.readLine()) != null) {
					String[] input = strLine.split("	");
					Matcher m = gip.matcher(input[1]);
					m.find();
					String gi = m.group(1);
					ONDEXConcept hit = gIndex.get(gi);
					ONDEXConcept source = uIndex.get(input[0].trim().toUpperCase());
					if (hit != null && source != null) {
						ONDEXRelation r = graph.getRelation(source, hit, rt);
						if (r == null) {
							r = graph.getRelation(hit, source, rt);
						}
						if (r == null) {
							r = graph.getFactory().createRelation(source, hit, rt, ev);
							r.createAttribute(eval, Double.valueOf(input[10]), false);
							count++;
						}
					} else {
						if (source == null)
							notFound.add(input[0]);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.err.println("Relations created: " + count);
		for (String str : notFound)
			System.err.println(str);
	}

	public static String _intersectionReport(ONDEXGraph graph, String attName, String type, String... cvs) {
		if (cvs.length < 2)
			return "There is no intersection possible with this input - so there are no results.";
		Map<ArrayKey, ValueHolder> counters = new HashMap<ArrayKey, ValueHolder>();
		for (int i = 0; i < cvs.length; i++) {
			CombinationGenerator cg = new CombinationGenerator(cvs.length, i + 1);
			while (cg.hasMore()) {
				String[] values = new String[i + 1];
				int[] map = cg.getNext();
				for (int z = 0; z < map.length; z++)
					values[z] = cvs[map[z]];
				Arrays.sort(values);
				counters.put(new ArrayKey(values), new ValueHolder(values));
			}
		}
		AttributeName an = null;
		if (attName != null) {
			an = graph.getMetaData().getAttributeName(attName);
			if (an == null)
				return attName + " does not exist in this graph, so there are no results.";
		}

		ArrayKey reusableKey = new ArrayKey();
		ListOfAttributes processor = null;
		if (type.startsWith("concept")) {
			Set<ONDEXConcept> vc = null;
			if (attName == null) {
				vc = graph.getConcepts();
			} else {
				vc = graph.getConceptsOfAttributeName(an);
			}
			if (type.equals(CGDS))
				processor = new ConcpetGDSProcessor(vc.iterator(), cvs);
			else if (type.equals(CCV)) {
				processor = new ConcpetCvProcessor(vc.iterator(), cvs);
			}

		} else if (type.startsWith("relation")) {
			Set<ONDEXRelation> vr = null;
			if (attName == null) {
				vr = graph.getRelations();
			} else {
				vr = graph.getRelationsOfAttributeName(an);
			}
			if (type.equals(RELGDS))
				processor = new RelationGDSProcessor(vr.iterator(), cvs);
			else if (type.equals(RELCV))
				processor = new RelationCvProcessor(vr.iterator(), cvs);
		} else
			return "The option " + type + " is not valid.";
		while (processor.hasNext()) {
			String[] ofInterest = processor.next();
			Arrays.sort(ofInterest);
			reusableKey.setArray(ofInterest);
			counters.get(reusableKey).increment();
		}

		for (ValueHolder hv : counters.values()) {
			for (ValueHolder vh : counters.values()) {
				hv.incorporteMatchingSubset(vh);
			}
		}
		String report = "\n" + type + "\n	Category	Total\n";
		for (ValueHolder hv : counters.values()) {
			report = report + hv.getReport() + "\n";
		}
		System.err.println(report);
		return report;
	}

	private static class ValueHolder {
		private String name = "";
		private Set<String> code = new HashSet<String>();
		private int countUnique = 0;
		private int countTotal = 0;

		public ValueHolder(String... names) {
			for (String s : names) {
				this.name = this.name + s + ":";
				code.add(s);
			}
		}

		public void increment() {
			countUnique++;
		}

		public String getReport() {
			return name + "	" + countUnique + "	" + countTotal;
		}

		public void incorporteMatchingSubset(ValueHolder vh) {
			if (vh.code.containsAll(code))
				this.countTotal = this.countTotal + vh.countUnique;
		}
	}

	/**
	 * Freely available code snippet from Michael Gilleland
	 * http://www.merriampark.com/comb.htm
	 */
	private static class CombinationGenerator {

		private int[] a;
		private int n;
		private int r;
		private BigInteger numLeft;
		private BigInteger total;

		// ------------
		// Constructor
		// ------------

		public CombinationGenerator(int n, int r) {
			if (r > n) {
				throw new IllegalArgumentException();
			}
			if (n < 1) {
				throw new IllegalArgumentException();
			}
			this.n = n;
			this.r = r;
			a = new int[r];
			BigInteger nFact = getFactorial(n);
			BigInteger rFact = getFactorial(r);
			BigInteger nminusrFact = getFactorial(n - r);
			total = nFact.divide(rFact.multiply(nminusrFact));
			reset();
		}

		// ------
		// Reset
		// ------

		public void reset() {
			for (int i = 0; i < a.length; i++) {
				a[i] = i;
			}
			numLeft = new BigInteger(total.toString());
		}

		// ------------------------------------------------
		// Return number of combinations not yet generated
		// ------------------------------------------------

		public BigInteger getNumLeft() {
			return numLeft;
		}

		// -----------------------------
		// Are there more combinations?
		// -----------------------------

		public boolean hasMore() {
			return numLeft.compareTo(BigInteger.ZERO) == 1;
		}

		// ------------------------------------
		// Return total number of combinations
		// ------------------------------------

		public BigInteger getTotal() {
			return total;
		}

		// --------------------------------------------------------
		// Generate next combination (algorithm from Rosen p. 286)
		// --------------------------------------------------------

		public int[] getNext() {

			if (numLeft.equals(total)) {
				numLeft = numLeft.subtract(BigInteger.ONE);
				return a;
			}

			int i = r - 1;
			while (a[i] == n - r + i) {
				i--;
			}
			a[i] = a[i] + 1;
			for (int j = i + 1; j < r; j++) {
				a[j] = a[i] + j - i;
			}

			numLeft = numLeft.subtract(BigInteger.ONE);
			return a;

		}
	}

	public static BigInteger getFactorial(int n) {
		BigInteger fact = BigInteger.ONE;
		for (int i = n; i > 1; i--) {
			fact = fact.multiply(new BigInteger(Integer.toString(i)));
		}
		return fact;
	}

	private static class ConcpetCvProcessor implements ListOfAttributes {
		private Set<String> validArgs = new HashSet<String>();
		private Iterator<ONDEXConcept> view;
		private String[] next = null;

		public ConcpetCvProcessor(Iterator<ONDEXConcept> view, String[] valid) {
			this.view = view;
			validArgs.addAll(Arrays.asList(valid));
			step();
		}

		public void close() {
		}

		public boolean hasNext() {
			return next != null;
		}

		public String[] next() {
			String[] result = next;
			step();
			return result;
		}

		private void step() {
			boolean match = false;
			next = null;
			List<String> ofInterest = new ArrayList<String>();
			while (!match && view.hasNext()) {
				ONDEXConcept c = view.next();
				String[] cCvs = c.getElementOf().getId().split(":");
				for (String cCv : cCvs) {
					// System.err.println(cCv+" :: "+validArgs+" :: "+validArgs.contains(cCv.trim()));
					if (validArgs.contains(cCv)) {
						match = true;
						ofInterest.add(cCv);
					}
				}
			}
			if (ofInterest.size() == 0) {
				next = null;
			} else {
				next = ofInterest.toArray(new String[ofInterest.size()]);
			}
		}
	}

	private static class RelationCvProcessor implements ListOfAttributes {
		private Set<String> validArgs = new HashSet<String>();
		private Iterator<ONDEXRelation> view;
		private String[] next = null;

		public RelationCvProcessor(Iterator<ONDEXRelation> view, String[] valid) {
			this.view = view;
			validArgs.addAll(Arrays.asList(valid));
			step();
		}

		public boolean hasNext() {
			return next != null;
		}

		public String[] next() {
			String[] result = next;
			step();
			return result;
		}

		private void step() {
			boolean match = false;
			next = null;
			List<String> ofInterest = new ArrayList<String>();
			while (!match && view.hasNext()) {
				ONDEXRelation r = view.next();
				String[] cCvs = arrAnd(r.getFromConcept().getElementOf().getId().split(":"), r.getToConcept().getElementOf().getId().split(":"), validArgs);
				if (cCvs.length > 0) {
					match = true;
					next = cCvs;
				}
			}
		}
	}

	private static class RelationGDSProcessor implements ListOfAttributes {
		private Set<String> validArgs = new HashSet<String>();
		private Iterator<ONDEXRelation> view;
		private String[] next = null;

		public RelationGDSProcessor(Iterator<ONDEXRelation> view, String[] valid) {
			this.view = view;
			validArgs.addAll(Arrays.asList(valid));
			step();
		}

		public boolean hasNext() {
			return next != null;
		}

		public String[] next() {
			String[] result = next;
			step();
			return result;
		}

		private void step() {
			boolean match = false;
			next = null;
			while (!match && view.hasNext()) {
				ONDEXRelation r = view.next();
				List<String> gds = new ArrayList<String>();
				for (Attribute g : r.getAttributes()) {
					String name = g.getOfType().getId();
					if (validArgs.contains(name))
						gds.add(name);
				}
				if (gds.size() > 0) {
					match = true;
					next = gds.toArray(new String[gds.size()]);
				}
			}
		}
	}

	private static class ConcpetGDSProcessor implements ListOfAttributes {
		private Set<String> validArgs = new HashSet<String>();
		private Iterator<ONDEXConcept> view;
		private String[] next = null;

		public ConcpetGDSProcessor(Iterator<ONDEXConcept> view, String[] valid) {
			this.view = view;
			validArgs.addAll(Arrays.asList(valid));
			step();
		}

		public boolean hasNext() {
			return next != null;
		}

		public String[] next() {
			String[] result = next;
			step();
			return result;
		}

		private void step() {
			boolean match = false;
			next = null;
			while (!match && view.hasNext()) {
				ONDEXConcept r = view.next();
				List<String> gds = new ArrayList<String>();
				for (Attribute g : r.getAttributes()) {
					String name = g.getOfType().getId();
					if (validArgs.contains(name))
						gds.add(name);
				}
				if (gds.size() > 0) {
					match = true;
					next = gds.toArray(new String[gds.size()]);
				}
			}
		}
	}

	public static void exportNodeAttributes(ONDEXGraph aog, String file, List<String> attributes) {
		DataSource tair = aog.getMetaData().getDataSource("TAIR");
		DataSource uniprot = aog.getMetaData().getDataSource("UNIPROTKB");
		List<AttributeName> atts = new ArrayList<AttributeName>();
		for (String a : attributes) {
			AttributeName at = aog.getMetaData().getAttributeName(a);
			if (at != null)
				atts.add(at);
		}
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write("Accession\tName");
			bw.flush();
			for (String a : attributes) {
				bw.write("\t" + a);
				bw.flush();
			}
			bw.write("\n");
			bw.flush();
			for (ONDEXConcept c : aog.getConcepts()) {
				boolean hasTair = false;
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (ca.getElementOf().equals(tair)) {
						bw.write(ca.getAccession());
						bw.flush();
						hasTair = true;
						break;
					}
				}
				if (!hasTair) {
					for (ConceptAccession ca : c.getConceptAccessions()) {
						if (ca.getElementOf().equals(uniprot)) {
							bw.write("\t" + ca.getAccession());
							bw.flush();
							break;
						}
					}
				}
				if (c.getConceptName() == null) {
					bw.write("\t");
					bw.flush();
				} else {
					bw.write("\t" + c.getConceptName().getName());
					bw.flush();
				}

				for (AttributeName att : atts) {
					Attribute attribute = c.getAttribute(att);
					String toWrite = "";
					if (attribute != null)
						toWrite = attribute.getValue().toString();
					bw.write("\t" + toWrite);
					bw.flush();
				}
				bw.write("\n");
				bw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportEdgeAttributes(ONDEXGraph aog, String... attributes) {
		List<AttributeName> atts = new ArrayList<AttributeName>();
		for (String a : attributes) {
			AttributeName at = aog.getMetaData().getAttributeName(a);
			if (at != null)
				atts.add(at);
		}
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("edge_att.tab"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (String a : attributes) {
				bw.write("\t" + a);
				bw.flush();
			}
			bw.write("\n");
			bw.flush();
			for (ONDEXRelation r : aog.getRelations()) {
				for (AttributeName att : atts) {
					Attribute attribute = r.getAttribute(att);
					String toWrite = "";
					if (attribute != null)
						toWrite = attribute.getValue().toString();
					bw.write("\t" + toWrite);
					bw.flush();
				}
				bw.write("\n");
				bw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] arrAnd(String[] aArr, String[] bArr, Set<String> mask) {
		List<String> result = new ArrayList<String>();
		for (String a : aArr) {
			for (String b : bArr) {
				if (a.equals(b) && mask.contains(b)) {
					result.add(b);
					break;
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}

	interface ListOfAttributes {
		public String[] next();

		public boolean hasNext();
	}
}
