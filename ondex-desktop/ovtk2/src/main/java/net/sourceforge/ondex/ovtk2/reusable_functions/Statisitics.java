package net.sourceforge.ondex.ovtk2.reusable_functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
import net.sourceforge.ondex.tools.subgraph.Subgraph;

/**
 * A collection of methods to output statistics
 * 
 * @author hindlem
 * 
 */
public class Statisitics {

	/**
	 * counts the number of Concepts containing a given accession matching a
	 * regex
	 * 
	 * e.g. countConceptsOnRegex("[0-9]*(\\.[0-9]*){2}.-", "EC", "EC", "EC", "",
	 * 2); //counts all EC accessions of the form n.n.n.- where n is a integer
	 * 
	 * @author hindlem
	 * 
	 * @param viewer
	 *            the viewer containing the graph to work on
	 * @param regex
	 *            the regex to match
	 * @param ccName
	 *            the name of the concept class of concepts to search in (can be
	 *            null)
	 * @param concept_cvName
	 *            the name of the cv of concepts to search in (can be null)
	 * @param accession_cvName
	 *            the name of the cv of accessions to search in (can not be
	 *            null)
	 */
	public static final int countConceptsOnRegex(OVTK2PropertiesAggregator viewer, String regex, String ccName, String concept_cvName, String accession_cvName) {

		ONDEXGraphMetaData md = viewer.getONDEXJUNGGraph().getMetaData();

		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		ConceptClass cc = md.getConceptClass(ccName);
		DataSource concept_dataSource = md.getDataSource(concept_cvName);
		DataSource accession_dataSource = md.getDataSource(accession_cvName);

		Pattern pattern = Pattern.compile(regex);

		Set<ONDEXConcept> concepts = graph.getConcepts();

		if (cc != null) {
			BitSetFunctions.and(concepts, graph.getConceptsOfConceptClass(cc));
		}

		if (concept_dataSource != null) {
			BitSetFunctions.and(concepts, graph.getConceptsOfDataSource(concept_dataSource));
		}

		int count = 0;

		for (ONDEXConcept concept : concepts) {
			HashMap<String, String> replacements = new HashMap<String, String>();

			for (ConceptAccession accession : concept.getConceptAccessions()) {
				if (!accession.getElementOf().equals(accession_dataSource)) {
					continue;
				}
				Matcher match = pattern.matcher(accession.getAccession());
				if (match.matches()) {
					count++;
				}
			}

			for (String oldAccession : replacements.keySet()) {
				boolean ambiguous = concept.getConceptAccession(oldAccession, accession_dataSource).isAmbiguous();
				concept.deleteConceptAccession(oldAccession, accession_dataSource);
				concept.createConceptAccession(replacements.get(oldAccession), accession_dataSource, ambiguous);
			}
		}
		return count;
	}

	/**
	 * 
	 * e.g. enumerateConnectingCVs("Protein", "MyDB", "equ", "Protein") if you
	 * want to find out how many of your Proteins in MyDB have mappings to other
	 * databases and the intersections of those DataSource's
	 * 
	 * @param viewer
	 * @param cc
	 * @param cv
	 * @param rt
	 * @param targetCC
	 */
	public static final void enumerateConnectingCVs(OVTK2PropertiesAggregator viewer, String cc, String cv, String rt, String targetCC) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		ONDEXGraphMetaData md = graph.getMetaData();
		ConceptClass conceptClassSource = md.getConceptClass(cc);
		DataSource dataSourceSource = md.getDataSource(cv);

		Set<ONDEXConcept> cvConcepts = graph.getConceptsOfDataSource(dataSourceSource);
		Set<ONDEXConcept> ccConcepts = graph.getConceptsOfConceptClass(conceptClassSource);

		Set<ONDEXConcept> concepts = BitSetFunctions.and(cvConcepts, ccConcepts);
		int allTotal = concepts.size();
		Map<String, Integer> countOnCvs = new HashMap<String, Integer>();
		System.out.println("---Intersectons---");
		for (ONDEXConcept queryConcept : concepts) {
			Set<String> cvs = new HashSet<String>();

			for (ONDEXRelation relation : graph.getRelationsOfConcept(queryConcept)) {
				if (relation.getOfType().getId().equals(rt)) {
					ONDEXConcept targetConcept = null;

					if (relation.getKey().getFromID() == queryConcept.getId()) {
						targetConcept = relation.getFromConcept();
					}
					if (relation.getKey().getToID() == queryConcept.getId()) {
						targetConcept = relation.getFromConcept();
					} else {
						continue;
					}

					if (!targetConcept.equals(queryConcept) && targetConcept.getOfType().getId().equals(targetCC)) {
						DataSource targetDataSource = targetConcept.getElementOf();
						String[] indivCVs = targetDataSource.getId().split(":");
						cvs.addAll(Arrays.asList(indivCVs));
					}

				}
			}
			if (cvs.size() > 0) {
				String cvString = cvSetToSortedString(cvs);

				Integer count = countOnCvs.get(cvString);
				if (count == null)
					count = 1;
				else
					count = count + 1;

				countOnCvs.put(cvString, count);
			}
		}
		int total = 0;
		for (String cvIntersect : countOnCvs.keySet()) {
			int count = countOnCvs.get(cvIntersect);
			System.out.println(cvIntersect + "\t" + count);
			total = total + count;
		}
		System.out.println("Total " + total + " with relations out of " + allTotal);

	}

	public static final void enumerateECAnnotation(OVTK2PropertiesAggregator viewer, String cc, String rt) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		ONDEXGraphMetaData md = graph.getMetaData();
		ConceptClass conceptClassSource = md.getConceptClass(cc);

		Set<ONDEXConcept> concepts = graph.getConceptsOfConceptClass(conceptClassSource);

		int allTotal = concepts.size();
		Map<Integer, Map<String, Integer>> digitsToDB = new HashMap<Integer, Map<String, Integer>>();
		Map<String, List<Integer>> countsOfAnnotations = new HashMap<String, List<Integer>>();
		Map<Integer, Map<String, Integer>> proteinsNoCVAnnotation = new HashMap<Integer, Map<String, Integer>>();

		int conceptsToDo = concepts.size();
		System.out.println("-----" + conceptsToDo);
		for (ONDEXConcept queryConcept : concepts) {
			HashSet<String> queryCVs = new HashSet<String>();
			DataSource queryDataSource = queryConcept.getElementOf();
			String[] splitCVs = queryDataSource.getId().split(":");
			queryCVs.addAll(Arrays.asList(splitCVs));
			HashMap<String, Integer> cvToAnnotations = new HashMap<String, Integer>();

			Map<Integer, Set<String>> cvs = new HashMap<Integer, Set<String>>();
			cvs.put(1, new HashSet<String>());
			cvs.put(2, new HashSet<String>());
			cvs.put(3, new HashSet<String>());
			cvs.put(4, new HashSet<String>());

			for (ONDEXRelation relation : graph.getRelationsOfConcept(queryConcept)) {
				if (relation.getOfType().getId().equals(rt)) {
					ONDEXConcept targetConcept = null;
					if (relation.getKey().getFromID() == queryConcept.getId()) {
						targetConcept = relation.getToConcept();
					} else if (relation.getKey().getToID() == queryConcept.getId()) {
						targetConcept = relation.getFromConcept();
					} else {
						continue;
					}

					if (!targetConcept.equals(queryConcept) && targetConcept.getOfType().getId().equals("EC")) {
						String ec = getAccessionOfCV(targetConcept.getConceptAccessions(), "EC");

						int digit = 4 - countChar(ec, '-');

						DataSource targetDataSource = targetConcept.getElementOf();
						String[] indivCVs = targetDataSource.getId().split(":");
						for (String tcv : indivCVs) {
							Set<String> cvList = cvs.get(digit);
							if (cvList == null) {
								cvList = new HashSet<String>();
								cvs.put(digit, cvList);
							}
							cvList.add(tcv);
						}
					} else {
						System.out.println("invalid " + targetConcept.getOfType().getId());
					}
				}
			}
			for (String cv : cvToAnnotations.keySet()) {
				Integer count = cvToAnnotations.get(cv);
				List<Integer> countList = countsOfAnnotations.get(cv);
				if (countList == null) {
					countList = new ArrayList<Integer>(conceptsToDo);
					countsOfAnnotations.put(cv, countList);
				}
				countList.add(count);
			}

			for (Integer digit : cvs.keySet()) {

				Set<String> cvs4Protein = cvs.get(digit);

				Map<String, Integer> ecLevel = digitsToDB.get(Integer.valueOf(digit));
				if (ecLevel == null) {
					ecLevel = new HashMap<String, Integer>();
					digitsToDB.put(Integer.valueOf(digit), ecLevel);
				}

				for (String cv : cvs4Protein) {
					Integer count = ecLevel.get(cv);
					if (count == null) {
						count = 0;
					}
					count = count + 1;
					ecLevel.put(cv, count);

					Integer countPerConcept = cvToAnnotations.get(cv);
					if (countPerConcept == null) {
						countPerConcept = 0;
					}
					countPerConcept = countPerConcept + 1;
					cvToAnnotations.put(cv, countPerConcept);
				}

				Map<String, Integer> cvCountMap = proteinsNoCVAnnotation.get(digit);
				if (cvCountMap == null) {
					cvCountMap = new HashMap<String, Integer>();
					proteinsNoCVAnnotation.put(digit, cvCountMap);
				}
				for (DataSource cvo : graph.getMetaData().getDataSources()) {
					String cv = cvo.getId();
					if (!cvs4Protein.contains(cv) && queryCVs.contains(cv)) {
						Integer count = cvCountMap.get(cv);
						if (count == null) {
							count = 0;
						}
						count = count + 1;
						cvCountMap.put(cv, count);
					}
				}
			}
		}

		for (Integer ecLevel : digitsToDB.keySet()) {
			System.out.println("EC level " + ecLevel);
			Map<String, Integer> cvs = digitsToDB.get(ecLevel);
			for (String cv : cvs.keySet()) {
				Integer count = cvs.get(cv);
				Map<String, Integer> proteinsNoCount = proteinsNoCVAnnotation.get(ecLevel);
				Integer emptyProteins = proteinsNoCount.get(cv);
				if (emptyProteins == null)
					emptyProteins = 0;
				System.out.println(cv + "\t" + count + "\t" + emptyProteins);
			}
		}
		System.out.println("");
		for (String cv : countsOfAnnotations.keySet()) {
			List<Integer> list = countsOfAnnotations.get(cv);
			int runningTotal = 0;
			for (Integer value : list) {
				runningTotal = runningTotal + value;
			}
			System.out.println(cv + "\tAnnotations:" + runningTotal + "\tMean:" + (double) runningTotal / (double) list.size());
		}
	}

	public static final int transferECtoProtein2(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		String[][] metaRoute1 = new String[][] { new String[] { "EC" }, new String[] { "cat_c" }, new String[] { "Reaction" }, new String[] { "cat_by" }, new String[] { "Enzyme" }, new String[] { "is_a" }, new String[] { "Protein" } };

		String[][] metaRoute2 = new String[][] { new String[] { "EC" }, new String[] { "cat_c" }, new String[] { "Enzyme" }, new String[] { "is_a" }, new String[] { "Protein" } };

		String[][] metaRoute3 = new String[][] { new String[] { "EC" }, new String[] { "cat_c" }, new String[] { "Reaction" }, new String[] { "cat_by" }, new String[] { "Enzyme" }, new String[] { "is_a" }, new String[] { "ProteinCplx" }, new String[] { "part_of" }, new String[] { "Protein" } };

		String[][] metaRoute4 = new String[][] { new String[] { "EC" }, new String[] { "cat_c" }, new String[] { "Enzyme" }, new String[] { "is_a" }, new String[] { "ProteinCplx" }, new String[] { "part_of" }, new String[] { "Protein" } };

		String[][] metaRoute5 = new String[][] { new String[] { "EC" }, new String[] { "cat_c" }, new String[] { "ProteinCplx" }, new String[] { "part_of" }, new String[] { "Protein" } };

		Object[] metaRoutes = new Object[] { metaRoute1, metaRoute2, metaRoute3, metaRoute4, metaRoute5 };

		int relationsTrasfered = 0;
		for (Object metaRoute : metaRoutes) {

			List<Subgraph> subgraphs = StandardFunctions.getSubgraphMatch(graph, (String[][]) metaRoute);

			for (Subgraph subgraph : subgraphs) {

				HashSet<ONDEXConcept> proteins = new HashSet<ONDEXConcept>();

				for (ONDEXConcept concept : subgraph.getConcepts()) {
					if (concept.getOfType().getId().equals("Protein")) {
						proteins.add(concept);
					}
				}

				for (ONDEXRelation relation : subgraph.getRelations()) {
					if (relation.getOfType().getId().equals("cat_c")) {
						ONDEXConcept fromConcept = relation.getFromConcept();
						ONDEXConcept toConcept = relation.getToConcept();

						ONDEXConcept oldConcept = null;
						if (fromConcept.getOfType().getId().equals("EC")) {
							oldConcept = fromConcept;
						} else if (toConcept.getOfType().getId().equals("EC")) {
							oldConcept = toConcept;
						} else {
							continue;
						}
						for (ONDEXConcept protein : proteins) {
							GraphElementManipulation.changeRelationVertex(graph, fromConcept, protein, relation);
							relationsTrasfered++;
						}

					}
				}
			}
		}
		return relationsTrasfered;
	}

	private static final void transferECtoProtein(OVTK2PropertiesAggregator viewer) {
		ONDEXGraph graph = viewer.getONDEXJUNGGraph();

		ONDEXGraphMetaData md = graph.getMetaData();
		ConceptClass conceptClassSource = md.getConceptClass("Reaction");

		for (ONDEXConcept reaction : graph.getConceptsOfConceptClass(conceptClassSource)) {

			HashSet<ONDEXConcept> proteins = new HashSet<ONDEXConcept>();
			HashSet<ONDEXRelation> ecRelations = new HashSet<ONDEXRelation>();

			for (ONDEXRelation relation : graph.getRelationsOfConcept(reaction)) {
				if (relation.getOfType().getId().equals("cat_c")) {
					ONDEXConcept ecTerm = null;
					if (relation.getFromConcept().getOfType().getId().equals("EC")) {
						ecTerm = relation.getFromConcept();
					} else if (relation.getToConcept().getOfType().getId().equals("EC")) {
						ecTerm = relation.getFromConcept();
					} else {
						continue;
					}
					ecRelations.add(relation);

				} else if (relation.getOfType().getId().equals("ca_by")) {
					ONDEXConcept enzyme = null;
					if (relation.getFromConcept().getOfType().getId().equals("Enzyme")) {
						enzyme = relation.getFromConcept();
					} else if (relation.getToConcept().getOfType().getId().equals("Enzyme")) {
						enzyme = relation.getFromConcept();
					} else {
						continue;
					}
					for (ONDEXRelation relationE : graph.getRelationsOfConcept(enzyme)) {
						if (relationE.getOfType().getId().equals("cat_c")) {
							ONDEXConcept ecTerm = null;
							if (relationE.getFromConcept().getOfType().getId().equals("EC")) {
								ecTerm = relationE.getFromConcept();
							} else if (relationE.getToConcept().getOfType().getId().equals("EC")) {
								ecTerm = relationE.getFromConcept();
							} else {
								continue;
							}
							ecRelations.add(relationE);
						} else if (relationE.getOfType().getId().equals("is_a")) {
							ONDEXConcept protein = null;
							if (relationE.getFromConcept().getOfType().getId().equals("Protein")) {
								protein = relationE.getFromConcept();
							} else if (relationE.getToConcept().getOfType().getId().equals("Protein")) {
								protein = relationE.getFromConcept();
							} else {
								continue;
							}
							proteins.add(protein);
						}
					}
				}
			}
			for (ONDEXConcept protein : proteins) {
				for (ONDEXRelation ec : ecRelations) {
					if (ec.getKey().getFromID() == protein.getId()) {
						GraphElementManipulation.changeRelationVertex(graph, ec.getToConcept(), protein, ec);
					} else {
						GraphElementManipulation.changeRelationVertex(graph, ec.getFromConcept(), protein, ec);
					}
				}

			}

		}

	}

	private static final int countChar(String ec, char c) {
		char[] chars = ec.toCharArray();
		int count = 0;
		for (char n : chars) {
			if (c == n)
				count++;
		}
		return count;
	}

	/**
	 * 
	 * @param conceptAccessions
	 * @return
	 */
	private static final String getAccessionOfCV(Set<ConceptAccession> conceptAccessions, String cv) {
		for (ConceptAccession acc : conceptAccessions) {
			if (acc.getElementOf().getId().equals(cv)) {
				return acc.getAccession();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param cvs
	 * @return
	 */
	private static final String cvSetToSortedString(Set<String> cvs) {
		String[] values = cvs.toArray(new String[cvs.size()]);
		Arrays.sort(values);
		StringBuilder builder = new StringBuilder(values.length * 10);
		for (String cv : values) {
			builder.append(cv + ":");
		}
		if (builder.length() > 0)
			builder.setLength(builder.length() - 1);
		return builder.toString();
	}

}
