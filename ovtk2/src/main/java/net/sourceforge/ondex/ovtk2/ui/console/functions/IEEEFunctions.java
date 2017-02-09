package net.sourceforge.ondex.ovtk2.ui.console.functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingUtilities;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sourceforge.ondex.algorithm.dijkstra.DijkstraQueue;
import net.sourceforge.ondex.algorithm.dijkstra.PathNode;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.graph.ONDEXEdgeStrokes;
import net.sourceforge.ondex.ovtk2.graph.ONDEXJUNGGraph;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeShapes;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.console.OverviewWindow;
import net.sourceforge.ondex.ovtk2.ui.console.SetSelector;

import org.apache.commons.collections15.Transformer;

/**
 * 
 * @author lysenkoa
 * 
 */
public class IEEEFunctions {

	private static OverviewWindow z;
	private static final String COLOR_VIS = "EvidenceType.Color.";

	private IEEEFunctions() {
	}

	public static final void setSelector() throws Exception {
		new SetSelector();
	}

	public static final void filterKeggPathway(Set<ONDEXConcept> cs, final OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		Set<ONDEXConcept> pathways = graph.getConceptsOfConceptClass(createCC(graph, "Path"));
		Set<ONDEXConcept> pathwaysToRetain = new HashSet<ONDEXConcept>();
		for (ONDEXConcept path : pathways) {
			Set<ONDEXConcept> reactions = new HashSet<ONDEXConcept>();

		}

	}

	public static void collateToExcel(String... dirs) {
		for (String dir : dirs) {
			File directory = new File(dir);
			try {
				WritableWorkbook workbook = Workbook.createWorkbook(new File(dir + File.separator + "Analysis.xls"));
				for (File f : directory.listFiles()) {
					int sheets = 0;
					if (f.getName().endsWith(".tab") || f.getName().endsWith(".txt")) {

						String sheetname = f.getName().substring(0, f.getName().length() - 4);
						WritableSheet sheet = workbook.createSheet(sheetname, sheets);
						DataInputStream in = new DataInputStream(new FileInputStream(f));
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String line = "";
						int row = 0;
						while ((line = br.readLine()) != null) {
							String[] split = line.split("\t");
							int col = 0;
							for (String item : split) {
								try {
									int i = Integer.valueOf(item);
									Number n = new Number(col, row, i);
									sheet.addCell(n);
									col++;
									continue;
								} catch (NumberFormatException e) {
								}

								try {
									double i = Double.valueOf(item);
									Number n = new Number(col, row, i);
									sheet.addCell(n);
									col++;
									continue;
								} catch (NumberFormatException e) {
								}
								Label l = new Label(col, row, item);
								sheet.addCell(l);
								col++;
							}
							row++;
						}
						in.close();
					}
				}
				workbook.write();
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void exportToCFinder(ONDEXGraph graph, String file, String nodeCV, String attribute) {
		AttributeName an = graph.getMetaData().getAttributeName(attribute);
		Set<ONDEXRelation> relations = graph.getRelationsOfAttributeName(an);
		DataSource dataSource = graph.getMetaData().getDataSource(nodeCV);
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (ONDEXRelation r : relations) {
				String[] line = new String[3];
				ONDEXConcept to = r.getToConcept();
				for (ConceptAccession ac : to.getConceptAccessions()) {
					if (ac.getElementOf().equals(dataSource))
						line[0] = ac.getAccession();
				}
				ONDEXConcept from = r.getFromConcept();
				for (ConceptAccession ac : from.getConceptAccessions()) {
					if (ac.getElementOf().equals(dataSource))
						line[1] = ac.getAccession();
				}
				line[2] = r.getAttribute(an).getValue().toString();

				bw.write(line[0] + "\t" + line[1] + "\t" + line[2] + "\n");
				bw.flush();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void removeRelationsWithGDSValue(final OVTK2PropertiesAggregator viewer, String attributeName, double value) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		AttributeName an = graph.getMetaData().getFactory().createAttributeName(attributeName, Double.class);
		int n = 0;
		for (ONDEXRelation r : graph.getRelations()) {
			Attribute attribute = r.getAttribute(an);

			if (attribute == null) {
				jung.setVisibility(r, false);
			} else if (((Double) attribute.getValue()) == value) {
				jung.setVisibility(r, false);
			} else {
				jung.setVisibility(r, true);
				n++;
			}
		}

		System.err.println("Relations remaining: " + n);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				viewer.getVisualizationViewer().getModel().fireStateChanged();
				// viewer.validate();
			}
		});
	}

	public static void setRelationWidth(OVTK2PropertiesAggregator viewer, final int size) {
		try {
			ONDEXEdgeStrokes edgeStrokes = viewer.getEdgeStrokes();
			edgeStrokes.setEdgeSizes(new Transformer<ONDEXRelation, Integer>() {
				@Override
				public Integer transform(ONDEXRelation input) {
					return size;
				}
			});
			viewer.getVisualizationViewer().getModel().fireStateChanged();
			viewer.updateViewer(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setConceptSize(OVTK2PropertiesAggregator viewer, final int size) {
		try {
			ONDEXNodeShapes nodeShapes = viewer.getNodeShapes();
			nodeShapes.setNodeSizes(new Transformer<ONDEXConcept, Integer>() {
				@Override
				public Integer transform(ONDEXConcept input) {
					return size;
				}
			});
			viewer.getVisualizationViewer().getModel().fireStateChanged();
			viewer.updateViewer(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printRelationState(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();
		ONDEXJUNGGraph jung = viewer.getONDEXJUNGGraph();
		for (ONDEXRelation r : graph.getRelations()) {
			System.err.println(viewer.getONDEXJUNGGraph().isVisible(jung.getRelation(r.getId())));
		}
	}

	// public static void countQualifiers(ONDEXGraph graph) {
	// Map<Integer, Set<Integer>> pubToQual = new HashMap<Integer,
	// Set<Integer>>();
	// for (ONDEXRelation r : graph.getRelations()) {
	// Integer c = r.getQualifier().getId();
	// if (c != null) {
	// Set<Integer> count = pubToQual.get(c);
	// if (count == null) {
	// count = new HashSet<Integer>();
	// pubToQual.put(c, count);
	// }
	// count.add(r.getFromConcept().getId());
	// count.add(r.getToConcept().getId());
	// }
	// }
	// AttributeName an =
	// graph.getMetaData().getFactory().createAttributeName("EXPERIMENT_SIZE",
	// Double.class);
	// for (Entry<Integer, Set<Integer>> ent : pubToQual.entrySet()) {
	// graph.getConcept(ent.getKey()).createAttribute(an, ent.getValue().size(),
	// false);
	// String acc = "";
	// if (graph.getConcept(ent.getKey()).getConceptAccessions().size() > 0) {
	// acc =
	// graph.getConcept(ent.getKey()).getConceptAccessions().iterator().next().getAccession();
	// }
	// System.err.println(ent.getKey() + "	" + ent.getValue().size() + "	" +
	// acc);
	// System.err.println();
	// }
	// System.err.println("_________________________________________________________");
	// Map<Integer, Integer> total = new HashMap<Integer, Integer>();
	// for (Entry<Integer, Set<Integer>> ent : pubToQual.entrySet()) {
	// Integer count = total.get(ent.getValue().size());
	// if (count != null) {
	// total.put(ent.getValue().size(), count + 1);
	// } else {
	// total.put(ent.getValue().size(), 1);
	// }
	// }
	// for (Entry<Integer, Integer> ent : total.entrySet()) {
	// System.err.println(ent.getKey() + "	" + ent.getValue());
	// }
	// }

	public static void assignInteractionWeight(ONDEXGraph graph) {
		Map<String, Double> conf_map = new HashMap<String, Double>();
		conf_map.put("2 hybrid", 0.5d);
		conf_map.put("biochemical", 0.8d);
		conf_map.put("tap", 0.7d);
		conf_map.put("bn-page", 0.7d);
		conf_map.put("protein array", 0.8d);
		conf_map.put("IMPD", 0d);
		Set<ONDEXRelation> itr = graph.getRelationsOfRelationType(graph.getMetaData().getRelationType("it_wi"));
		// Set<ONDEXRelation>itr = graph.getRelations();
		AttributeName an = graph.getMetaData().getFactory().createAttributeName("INTERACTION_WEIGHT", Double.class);
		int count = 0;
		// for (ONDEXRelation r : itr) {
		// Attribute expSize =
		// r.getQualifier().getAttribute(graph.getMetaData().getAttributeName("EXPERIMENT_SIZE"));
		// Double conf = convertExperimentToConf(r.getEvidence(), conf_map);
		// if (expSize != null && ((Integer) expSize.getValue()) >= 20) {
		// if (conf == 0d)
		// System.err.println("Error");
		// r.createAttribute(an, conf, false);
		// count++;
		// } else {
		// r.createAttribute(an, 1d, false);
		// count++;
		// }
		// }
		System.err.println("Values assigned: " + count);
	}

	/*
	 * private static Double convertExperimentToConf(Set<EvidenceType> ev,
	 * Map<String, Double> rule) { Double max = 0d; for (EvidenceType et : ev) {
	 * String evidence = et.getId(); Double tmp = rule.get(evidence); if (tmp ==
	 * null) return 1d; else if (tmp > max) max = tmp; } return max; }
	 */

	public static void assignBlastWeight(ONDEXGraph graph) {
		AttributeName an = graph.getMetaData().getFactory().createAttributeName("BLAST_WEIGHT", Double.class);
		for (ONDEXRelation r : graph.getRelations()) {
			Attribute attribute = r.getAttribute(graph.getMetaData().getAttributeName("BLEV"));
			if (attribute == null)
				continue;
			Float score = (float) (double) (Double) attribute.getValue();
			Double value = (double) 1f / (1f + (float) Math.exp(((((float) Math.log10(score) + 3f) * 6.66667f) + 10f)));
			r.createAttribute(an, value, false);
		}
	}

	private static void assignCoexWeight(ONDEXGraph graph) {
		AttributeName an = graph.getMetaData().getFactory().createAttributeName("COEX_WEIGHT", Double.class);
		for (ONDEXRelation r : graph.getRelations()) {
			Attribute attribute = r.getAttribute(graph.getMetaData().getAttributeName("Correlation"));
			if (attribute == null)
				continue;
			Double score = (Double) attribute.getValue();
			r.createAttribute(an, (Math.abs(score) - 0.6) / 0.4, false);
		}
	}

	public static void assignAllWeights(ONDEXGraph graph) {
		assignBlastWeight(graph);
		assignCoexWeight(graph);
	}

	public static void assignCombinedWeight(ONDEXGraph graph) {

		AttributeName coex = graph.getMetaData().getAttributeName("COEX_WEIGHT");
		AttributeName blast = graph.getMetaData().getAttributeName("BLAST_WEIGHT");
		AttributeName itwi = graph.getMetaData().getAttributeName("INTERACTION_WEIGHT");
		AttributeName tm = graph.getMetaData().getAttributeName("Co-citation_weight");
		AttributeName all = graph.getMetaData().getFactory().createAttributeName("COMBINED_WEIGHT", Double.class);
		for (ONDEXRelation r : graph.getRelations()) {
			Double value = ((getNumber(coex, r) + getNumber(blast, r) + getNumber(itwi, r) + getNumber(tm, r)) / 4d);
			if (r.getAttribute(all) != null) {
				r.deleteAttribute(all);
			}
			r.createAttribute(all, value, false);
		}
	}

	public static String assignAverageWeight(ONDEXGraph graph, List<String> atts) {
		List<AttributeName> ans = new ArrayList<AttributeName>();
		String name = "";
		for (String a : atts) {
			ans.add(graph.getMetaData().getAttributeName(a));
			if (!name.equals("")) {
				name = name + ",";
			}
			name = name + a;
		}
		AttributeName av = graph.getMetaData().getFactory().createAttributeName("COMBINED[" + name + "]", Double.class);
		for (ONDEXRelation r : graph.getRelations()) {
			Double sum = 0d;
			for (AttributeName an : ans) {
				sum = sum + getNumber(an, r);
			}
			Double average = sum / ((double) ans.size());
			if (r.getAttribute(av) != null) {
				r.deleteAttribute(av);
			}
			r.createAttribute(av, average, false);
		}
		return "COMBINED[" + name + "]";
	}

	private static double getNumber(AttributeName an, ONDEXRelation r) {
		Attribute attribute = r.getAttribute(an);
		if (attribute == null)
			return 0d;
		else
			return (Double) attribute.getValue();
	}

	public static void cleanGraph(ONDEXGraph graph) {
		Set<String> attNames = new HashSet<String>();
		for (ONDEXRelation r : graph.getRelations()) {
			ONDEXConcept c1 = r.getFromConcept();
			ONDEXConcept c2 = r.getToConcept();
			if (c1.equals(c2)) {
				graph.deleteRelation(r.getId());
			} else {
				Map<String, Object> best = new HashMap<String, Object>();
				Set<AttributeName> thisAttNames = new HashSet<AttributeName>();
				for (Attribute attribute : r.getAttributes()) {
					thisAttNames.add(attribute.getOfType());
					if (attribute.getOfType().getId().contains(":")) {
						attNames.add(attribute.getOfType().getId());
						if (Number.class.isAssignableFrom(attribute.getOfType().getDataType())) {
							String name = attribute.getOfType().getId().split(":")[0];
							if (best.get(name) == null) {
								best.put(name, attribute.getValue());
							} else {
								if (name.equals("BLEV")) {
									if (((Double) best.get(name)) > ((Double) attribute.getValue())) {
										best.put(name, attribute.getValue());
									}
								} else {
									if (((Double) best.get(name)) < ((Double) attribute.getValue())) {
										best.put(name, attribute.getValue());
									}
								}
							}
						}
					} else {
						best.put(attribute.getOfType().getId(), attribute.getValue());
					}
				}
				for (AttributeName an : thisAttNames) {
					r.deleteAttribute(an);
				}

				for (Entry<String, Object> ent : best.entrySet()) {
					if (ent.getKey().equals("BLEV")) {
						System.err.println(ent.getValue());
					}
					r.createAttribute(graph.getMetaData().getAttributeName(ent.getKey()), ent.getValue(), false);
				}
			}
		}

		for (ONDEXConcept c : graph.getConcepts()) {
			if (graph.getRelationsOfConcept(c) == null || graph.getRelationsOfConcept(c).size() == 0) {
				graph.deleteConcept(c.getId());
			} else {
				Set<AttributeName> thisAttNames = new HashSet<AttributeName>();
				Map<String, Object> best = new HashMap<String, Object>();
				for (Attribute attribute : c.getAttributes()) {
					thisAttNames.add(attribute.getOfType());
					if (attribute.getOfType().getId().contains(":")) {
						attNames.add(attribute.getOfType().getId());
						if (Number.class.isAssignableFrom(attribute.getOfType().getDataType())) {
							String name = attribute.getOfType().getId().split(":")[0].trim();
							if (best.get(name) == null) {
								best.put(name, attribute.getValue());
							} else {
								if (name.equals("TAXID")) {
									continue;
								} else {
									if (((Double) best.get(name)) < ((Double) attribute.getValue())) {
										best.put(name, attribute.getValue());
									}
								}
							}
						}
					} else {
						best.put(attribute.getOfType().getId().trim(), attribute.getValue());
					}
				}
				for (AttributeName an : thisAttNames) {
					c.deleteAttribute(an);
				}

				for (Entry<String, Object> ent : best.entrySet()) {
					c.createAttribute(graph.getMetaData().getAttributeName(ent.getKey()), ent.getValue(), false);
				}
			}
		}
		for (String id : attNames) {
			graph.getMetaData().deleteAttributeName(id);
		}
	}

	public static void exportAccessions(ONDEXGraph graph, String... acc) {
		Set<DataSource> acs = new HashSet<DataSource>();
		for (String a : acc) {
			DataSource dataSource = graph.getMetaData().getDataSource(a);
			if (dataSource != null) {
				acs.add(dataSource);
			}
		}
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("accessions.tab"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (ONDEXConcept c : graph.getConcepts()) {
				for (ConceptAccession ca : c.getConceptAccessions()) {
					if (acs.contains(ca.getElementOf())) {
						bw.write(ca.getAccession() + "\n");
						bw.flush();
					}
				}
			}
			out.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportRelationGDS(ONDEXGraph graph, String file, List<String> an) {
		List<AttributeName> acs = new ArrayList<AttributeName>();
		for (String a : an) {
			AttributeName atn = graph.getMetaData().getAttributeName(a);
			if (atn != null) {
				acs.add(atn);
			}
		}
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (ONDEXRelation c : graph.getRelations()) {
				bw.write(c.getId() + "\t");
				bw.flush();
				for (AttributeName atr : acs) {
					if (c.getAttribute(atr) != null) {
						bw.write(c.getAttribute(atr).getValue() + "\t");
						bw.flush();
					} else {
						bw.write("\t");
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

	public static double round(double Rval, int Rpl) {
		double p = Math.pow(10, Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (double) tmp / p;
	}

	public static void annotateWeightedBW(ONDEXGraph graph, String weightAtt, String bwAtt) {
		AttributeName an = graph.getMetaData().getAttributeName(weightAtt);
		AttributeName anbw = graph.getMetaData().getFactory().createAttributeName(bwAtt, Double.class);
		Map<ONDEXConcept, Double> validCs = new HashMap<ONDEXConcept, Double>();
		Double totalPaths = 0d;
		for (ONDEXConcept c : graph.getConcepts()) {
			Set<ONDEXRelation> rofc = getRelationsWithAttribute(graph, c, an);
			if (rofc.size() != 0)
				validCs.put(c, 0d);
		}
		for (ONDEXConcept c : validCs.keySet()) {
			Set<ONDEXRelation> rofc = getRelationsWithAttribute(graph, c, an);
			Set<Integer> conceptBitSet = new HashSet<Integer>();
			Iterator<PathNode> resultIt = search(graph, c, an).iterator();
			PathNode curr;
			while (resultIt.hasNext()) {
				curr = resultIt.next();
				if (!conceptBitSet.contains(curr.getCid())) {
					conceptBitSet.add(curr.getCid());
					traceBack(curr, conceptBitSet);
				}
			}
			totalPaths = totalPaths + conceptBitSet.size() - 1;
			for (Integer a : conceptBitSet) {
				if (a != c.getId()) {
					ONDEXConcept conc = graph.getConcept(a);
					Set<ONDEXRelation> rs = getRelationsWithAttribute(graph, conc, an);
					if (rs.size() > 1) {
						validCs.put(conc, (validCs.get(conc) + 1d));
					}
				}
			}
		}
		totalPaths = totalPaths / 2d;
		System.err.println(totalPaths);
		for (Entry<ONDEXConcept, Double> ent : validCs.entrySet()) {
			ent.getKey().createAttribute(anbw, (ent.getValue() / 2d) / totalPaths, false);
		}
	}

	private static Collection<PathNode> search(ONDEXGraph graph, ONDEXConcept startConcept, AttributeName an) {

		PathNode node_curr, node_succ;
		ONDEXConcept c_curr, c_succ;

		PathNode node_root = new PathNode(startConcept.getId());
		Set<ONDEXRelation> relations;
		DijkstraQueue queue = new DijkstraQueue(node_root);

		while (queue.moreOpenElements()) {
			// get next
			node_curr = queue.dequeue();
			c_curr = graph.getConcept(node_curr.getCid());
			// check if goal reached
			// if (targetCCs.contains(c_curr.getOfType(s)))
			// return node_curr;
			if ((relations = getRelationsWithAttribute(graph, c_curr, an)) != null) {
				for (ONDEXRelation r_curr : relations) {
					if (r_curr.getFromConcept().equals(r_curr.getToConcept()))
						continue; // loops of size one are evil ;)
					c_succ = getOppositeConcept(c_curr, r_curr);

					node_succ = new PathNode(c_succ.getId());
					node_succ.setParent(node_curr, r_curr.getId());
					node_succ.setG(node_curr.getG() + getWeight(r_curr, an));
					queue.enqueueIfBetterOrNew(node_succ);
				}
			}
			queue.considerClosed(node_curr);
		}
		return queue.getResultSet();
	}

	private static Set<ONDEXRelation> getRelationsWithAttribute(ONDEXGraph graph, ONDEXConcept c, AttributeName an) {
		BitSet set = new BitSet();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (r.getFromConcept().equals(r.getToConcept()))
				continue;
			if (r.getAttribute(an) != null)
				set.set(r.getId());
		}
		Set<ONDEXRelation> result = BitSetFunctions.create(graph, ONDEXRelation.class, set);
		return result;
	}

	/**
	 * Returns a concept's opposite one on a relation.
	 * 
	 * @param c_curr
	 *            the concept on the one end.
	 * @param r_curr
	 *            the relation.
	 * @return the concept on the other end.
	 */
	private static ONDEXConcept getOppositeConcept(ONDEXConcept c_curr, ONDEXRelation r_curr) {
		return (r_curr.getFromConcept().equals(c_curr)) ? r_curr.getToConcept() : r_curr.getFromConcept();
	}

	/**
	 * returns the weight of the given edge.
	 * 
	 * @param r_curr
	 *            the relation for which to retrieve the edge weight.
	 * @param weightAttributeName
	 * @return the edge weight of the given relation.
	 */
	private static double getWeight(ONDEXRelation r_curr, AttributeName weightAttributeName) {
		double out;
		Attribute weightAttribute = r_curr.getAttribute(weightAttributeName);
		out = ((java.lang.Number) weightAttribute.getValue()).doubleValue();
		return out;
	}

	/**
	 * recursive method for backtracing inside the result set of the algorithm.
	 * 
	 * @param n
	 *            the current node.
	 * @param conceptBitSet
	 */
	private static void traceBack(PathNode n, Set<Integer> conceptBitSet) {
		if (n.getParent() != null) {
			if (!conceptBitSet.contains(n.getParent().getCid())) {
				conceptBitSet.add(n.getParent().getCid());
				traceBack(n.getParent(), conceptBitSet);
			}
		}
	}

	/**
	 * Tries to get the most worthwhile identifier for the concept
	 * 
	 * @param c
	 *            - concept
	 * @return - some id (in order of decreasing preference: name, pid,
	 *         description, concepts class+is)
	 */
	private static String getSomething(ONDEXConcept c) {
		if (c.getConceptName() != null) {
			return c.getConceptName().getName();
		}
		if (c.getPID() != null && c.getPID().equals("")) {
			return c.getPID();
		}
		if (c.getDescription() != null && c.getDescription().equals("")) {
			return c.getDescription();
		}
		return "[" + c.getOfType().getId() + " " + c.getId() + "]";
	}

	public static void createContextId(ONDEXGraph graph, String conceptClass, String prefix) throws Exception {
		ConceptClass cc = graph.getMetaData().getConceptClass(conceptClass);
		if (cc == null) {
			throw new Exception("Incorrect concept class specified: " + conceptClass);
		}
		Integer i = 0;
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
			c.createConceptName(prefix + "_" + i.toString(), true);
			i++;
		}
	}
	/*
	 * public static final String runMCL(OutputPrinter out,
	 * OVTK2PropertiesAggregator viewer, String attributeName, String
	 * clusterName, double inflation){ String path =
	 * Config.properties.getProperty("Extension.MCL"); if(path == null) return
	 * "A valid path to mcl executable should be added to the config.xml for this function to work \n"
	 * + " e.g.: <entry key=\"Extension.MCL\">D:/mcl/src/shmcl</entry>";
	 * Clustering.runMCL(out, viewer.getJUNGGraph(), path, attributeName,
	 * clusterName, inflation); return "Clustering complete"; }
	 */
	/*
	 * public static void bellmanFord(Vector<Edge> edges, int nnodes, int
	 * source) { groupCategory2 // the 'distance' array contains the distances
	 * from the main source to all other nodes int[] distance = new int[nnodes];
	 * // at the start - all distances are initiated to infinity
	 * Arrays.fill(distance, INF); // the distance from the main source to
	 * itself is 0 distance[source] = 0; // in the next loop we run the
	 * relaxation 'nnodes' times to ensure that // we have found new distances
	 * for ALL nodes for (int i = 0; i < nnodes; ++i) // relax every edge in
	 * 'edges' for (int j = 0; j < edges.size(); ++j) { // analyze the current
	 * edge (SOURCE == edges.get(j).source, DESTINATION ==
	 * edges.get(j).destination): // if the distance to the SOURCE node is equal
	 * to INF then there's no shorter path from our main source to DESTINATION
	 * through SOURCE if (distance[edges.get(j).source] == INF) continue; //
	 * newDistance represents the distance from our main source to DESTINATION
	 * through SOURCE (i.e. using current edge - 'edges.get(j)') int newDistance
	 * = distance[edges.get(j).source] + edges.get(j).weight; // if the
	 * newDistance is less than previous shortest distance from our main source
	 * to DESTINATION // then record that new shortest distance from the main
	 * source to DESTINATION if (newDistance <
	 * distance[edges.get(j).destination]) distance[edges.get(j).destination] =
	 * newDistance; } // next loop analyzes the graph for cycles for (int i = 0;
	 * i < edges.size(); ++i) // 'if (distance[edges.get(i).source] != INF)'
	 * means: // " // if the distance from the main source node to the
	 * DESTINATION node is equal to infinity then there's no path between them
	 * // " // 'if (distance[edges.get(i).destination] >
	 * distance[edges.get(i).source] + edges.get(i).weight)' says that there's a
	 * negative edge weight cycle in the graph if (distance[edges.get(i).source]
	 * != INF && distance[edges.get(i).destination] >
	 * distance[edges.get(i).source] + edges.get(i).weight) {
	 * System.out.println("Negative edge weight cycles detected!"); return; } //
	 * this loop outputs the distances from the main source node to all other
	 * nodes of the graph for (int i = 0; i < distance.length; ++i) if
	 * (distance[i] == INF) System.out.println("There's no path between " +
	 * source + " and " + i); else
	 * System.out.println("The shortest distance between nodes " + source +
	 * " and " + i + " is " + distance[i]); }
	 */
}
