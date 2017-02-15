package net.sourceforge.ondex.filter.relationneighbours;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.algorithm.relationneighbours.DepthSensitiveRTValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.LogicalRelationValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.RelationNeighboursSearch;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.EventType;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

import org.apache.lucene.search.Query;

/**
 * Locates relation neighbours on seed/s defined by ondex id or list of names
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle", "Matthew Pocock" }, emails = {
		"matthew_hindle at users.sourceforge.net",
		"drdozer at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	private static Filter instance;

	/**
	 * NB: Overrides previous instance logging
	 */
	public Filter() {
		instance = this;
	}

	private Set<Integer> visibleIntConcepts;
	private Set<Integer> visibleIntRelations;

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new RangeArgumentDefinition<Integer>(DEPTH_ARG, DEPTH_ARG_DESC,
						true, null, 0, Integer.MAX_VALUE, Integer.class),
				new StringArgumentDefinition(SEEDCONCEPT_ARG,
						SEEDCONCEPT_ARG_DESC, false, null, true),
				new StringArgumentDefinition(CONCEPTACC_ARG,
						CONCEPTACC_ARG_DESC, false, null, true),
				new StringArgumentDefinition(CONCEPTCLASS_ARG,
						CONCEPTCLASS_ARG_DESC, false, null, false),
				new StringArgumentDefinition(CONCEPTACC_CV_ARG,
						CONCEPTACC_CV_ARG_DESC, false, null, false),
				new StringArgumentDefinition(CONCEPTNAME_ARG,
						CONCEPTNAME_ARG_DESC, false, null, true),
				new StringMappingPairArgumentDefinition(
						RELATIONTYPE_RESTRIC_ARG,
						RELATIONTYPE_RESTRIC_ARG_DESC, false, null, true),
				new StringMappingPairArgumentDefinition(
						CONCEPTCLASS_RESTRIC_ARG,
						CONCEPTCLASS_RESTRIC_ARG_DESC, false, null, true),
				new StringMappingPairArgumentDefinition(CONCEPTCV_RESTRIC_ARG,
						CONCEPTCV_RESTRIC_ARG_DESC, false, null, true) };
	}

	public String getName() {
		return "Relation Neighbours Filter";
	}

	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getId() {
		return "relationneighbours";
	}

	public void start() throws Exception {

		Set<Integer> conceptSeeds = new HashSet<Integer>();

		LuceneEnv luceneEnv = LuceneRegistry.sid2luceneEnv.get(graph.getSID());

		Object[] accessions = args.getObjectValueArray(CONCEPTACC_ARG);
		Object ccArg = args.getUniqueValue(CONCEPTCLASS_ARG);
		Object acc_cvArg = args.getUniqueValue(CONCEPTACC_CV_ARG);

		DataSource acc_dataSourceO = null;
		ConceptClass ccO = null;

		if (ccArg != null) {
			ccO = graph.getMetaData().getConceptClass((String) ccArg);
			if (ccO == null) {
				fireEventOccurred(new WrongParameterEvent(
						"Invalid ConceptClass for Accession: " + ccArg,
						getCurrentMethodName()));
				return;
			}
		}

		if (acc_cvArg != null) {
			acc_dataSourceO = graph.getMetaData().getDataSource(
					(String) acc_cvArg);
			if (acc_dataSourceO == null) {
				fireEventOccurred(new WrongParameterEvent(
						"Invalid DataSource for Accession: " + acc_cvArg,
						getCurrentMethodName()));
				return;
			}
		}

		if (accessions != null && accessions.length > 0) {
			for (Object accession : accessions) {
				Set<ONDEXConcept> conceptsToAdd = null;

				if (acc_dataSourceO != null && ccO != null) {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptAccessionExact(
									acc_dataSourceO, (String) accession, ccO,
									true);
					conceptsToAdd = luceneEnv.searchInConcepts(query);
				} else if (acc_dataSourceO != null) {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptAccessionExact(
									acc_dataSourceO, (String) accession, true);
					conceptsToAdd = luceneEnv.searchInConcepts(query);
				} else if (ccO != null) {
					fireEventOccurred(new WrongParameterEvent(
							"A search for ConceptClass on Accession must include a Accession DataSource",
							getCurrentMethodName()));
					return;
				} else {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptAccessionExact(
									(String) accession, true,
									luceneEnv.getListOfConceptAccDataSources());
					conceptsToAdd = luceneEnv.searchInConcepts(query);
				}

				if (conceptsToAdd != null) {
					fireEventOccurred(new GeneralOutputEvent("Found "
							+ conceptsToAdd.size() + " Concepts for accession "
							+ accession, getCurrentMethodName()));
					for (ONDEXConcept c : conceptsToAdd) {
						conceptSeeds.add(c.getId());
					}
				}
			}
		}

		Object[] names = args.getObjectValueArray(CONCEPTNAME_ARG);
		if (names != null && names.length > 0) {
			for (Object name : names) {
				Set<ONDEXConcept> conceptsToAdd = null;

				if (ccO != null) {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptName((String) name, ccO,
									LuceneEnv.DEFAULTANALYZER);
					conceptsToAdd = luceneEnv.searchInConcepts(query);
				} else {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptName((String) name,
									LuceneEnv.DEFAULTANALYZER);
					conceptsToAdd = luceneEnv.searchInConcepts(query);
				}

				if (conceptsToAdd != null) {
					fireEventOccurred(new GeneralOutputEvent("Found "
							+ conceptsToAdd.size() + " Concepts for name "
							+ name, getCurrentMethodName()));
					for (ONDEXConcept c : conceptsToAdd) {
						conceptSeeds.add(c.getId());
					}
				}
			}
		}

		// parse in seed concepts
		for (Object o : args.getObjectValueArray(SEEDCONCEPT_ARG)) {
			try {
				Integer cidSeed = Integer.parseInt(o.toString());
				if (cidSeed != null) {
					conceptSeeds.add(cidSeed);
				}
			} catch (NumberFormatException nfe) {
				throw new WrongParameterException(
						"Error in seed concepts specified: Aborting");
			}
		}

		Integer depth = (Integer) args.getUniqueValue(DEPTH_ARG);

		if (conceptSeeds.size() > 0) {
			fireEventOccurred(new GeneralOutputEvent("Seed Concepts = "
					+ conceptSeeds.size(), getCurrentMethodName()));
		} else {
			throw new WrongParameterException(
					"There are no seeds found or specified: Aborting");
		}

		System.out.println("depth " + depth);

		Map<String, ArrayList<String>> ccRestrictions = null;
		Map<String, ArrayList<String>> ccvRestrictions = null;
		Map<String, ArrayList<String>> rtRestrictions = null;

		try {
			if (args.getObjectValueArray(CONCEPTCLASS_RESTRIC_ARG) != null)
				ccRestrictions = splitArgs(args
						.getObjectValueArray(CONCEPTCLASS_RESTRIC_ARG));

			if (args.getObjectValueArray(CONCEPTCV_RESTRIC_ARG) != null)
				ccvRestrictions = splitArgs(args
						.getObjectValueArray(CONCEPTCV_RESTRIC_ARG));

			if (args.getObjectValueArray(RELATIONTYPE_RESTRIC_ARG) != null)
				rtRestrictions = splitArgs(args
						.getObjectValueArray(RELATIONTYPE_RESTRIC_ARG));

		} catch (InvalidObjectException e) {
			fireEventOccurred(new WrongParameterEvent(e.getMessage(),
					getCurrentMethodName()));
			return;
		}

		LogicalRelationValidator val = null;

		if (ccRestrictions != null) {
			for (String depthString : ccRestrictions.keySet()) {
				int ccDepth = Integer.parseInt(depthString);
				for (String ccId : ccRestrictions.get(depthString)) {
					ConceptClass cc = graph.getMetaData().getConceptClass(ccId);
					if (cc != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addConceptClassConstraint(ccDepth, cc);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown Concept Class :"
								+ ccId);
					}
				}
			}
		}
		if (ccvRestrictions != null) {
			for (String depthString : ccvRestrictions.keySet()) {
				int ccvDepth = Integer.parseInt(depthString);
				for (String cvId : ccvRestrictions.get(depthString)) {
					DataSource dataSource = graph.getMetaData().getDataSource(
							cvId);
					if (dataSource != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addConceptDataSourceConstraint(ccvDepth,
										dataSource);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown DataSource Class :"
								+ cvId);
					}
				}
			}
		}

		if (rtRestrictions != null) {
			for (String depthString : rtRestrictions.keySet()) {
				int rtDepth = Integer.parseInt(depthString);
				for (String rtId : rtRestrictions.get(depthString)) {
					RelationType rt = graph.getMetaData().getRelationType(rtId);
					if (rt != null) {
						if (val == null) {
							val = new DepthSensitiveRTValidator();
						}
						((DepthSensitiveRTValidator) val)
								.addRelationTypeConstraint(rtDepth, rt);
					} else {
						System.err.println(this.getName()
								+ ":Restrictions: Unknown RelationType Class :"
								+ rtId);
					}
				}
			}
		}

		RelationNeighboursSearch rn = new RelationNeighboursSearch(graph);
		if (val != null)
			rn.setValidator(val);

		visibleIntConcepts = new HashSet<Integer>();
		visibleIntRelations = new HashSet<Integer>();

		for (Iterator<Integer> i = conceptSeeds.iterator(); i.hasNext();) {
			int seed = i.next();
			ONDEXConcept concept = graph.getConcept(seed);
			if (concept == null) {
				fireEventOccurred(new WrongParameterEvent("Concept " + seed
						+ " does not exist ignoring", getCurrentMethodName()));
				continue;
			}
			rn.search(concept, depth);

			visibleIntConcepts.add(seed);
			for (ONDEXConcept c : rn.getFoundConcepts())
				visibleIntConcepts.add(c.getId());
			for (ONDEXRelation r : rn.getFoundRelations())
				visibleIntRelations.add(r.getId());
		}

		fireEventOccurred(new GeneralOutputEvent("Finished: Found "
				+ visibleIntConcepts.size() + " Concepts and "
				+ visibleIntRelations.size() + " Relations",
				getCurrentMethodName()));
		rn.shutdown();
	}

	public Set<ONDEXRelation> getVisibleRelations() {
		BitSet set = new BitSet();
		Iterator<Integer> it = visibleIntRelations.iterator();
		while (it.hasNext()) {
			int i = it.next();
			set.set(i);
		}
		assert (set.size() == visibleIntRelations.size());
		return BitSetFunctions.create(graph, ONDEXRelation.class, set);
	}

	public Set<ONDEXConcept> getVisibleConcepts() {
		BitSet set = new BitSet();
		Iterator<Integer> it = visibleIntConcepts.iterator();
		while (it.hasNext()) {
			int i = it.next();
			set.set(i);
		}
		assert (set.size() == visibleIntConcepts.size());
		return BitSetFunctions.create(graph, ONDEXConcept.class, set);
	}

	public boolean requiresIndexedGraph() {
		return true;
	}

	@Override
	public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
		ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
		Iterator<Integer> it = visibleIntConcepts.iterator();
		while (it.hasNext()) {
			graphCloner.cloneConcept(it.next());
		}
		it = visibleIntRelations.iterator();
		while (it.hasNext()) {
			graphCloner.cloneRelation(it.next());
		}
	}

	/**
	 * Returns a slightly more useful array structure from pair values
	 * 
	 * @param objs
	 *            array of pairs
	 * @return names to values hashmap
	 * @throws InvalidObjectException
	 *             if the pairs are not in the expected form
	 */
	public static Map<String, ArrayList<String>> splitArgs(Object[] objs)
			throws InvalidObjectException {
		HashMap<String, ArrayList<String>> args = new HashMap<String, ArrayList<String>>();
		for (Object obj : objs) {
			if (obj instanceof String) {
				String[] pair = ((String) obj).split(",");
				if (pair.length == 2) {
					ArrayList<String> existing = args.get(pair[0]);
					if (existing == null) {
						existing = new ArrayList<String>(1);
					}
					existing.add(pair[1]);
				} else {
					throw new InvalidObjectException(
							"name,value pair invalid: " + obj);
				}
			} else {
				throw new InvalidObjectException("Object is not a String: "
						+ obj);
			}
		}
		return args;
	}

	/**
	 * Convenience method for outputing the current method name in a dynamic way
	 * 
	 * @return the calling method name
	 */
	public static String getCurrentMethodName() {
		Exception e = new Exception();
		StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
		String name = trace.getMethodName();
		String className = trace.getClassName();
		int line = trace.getLineNumber();
		return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line
				+ "]";
	}

	/**
	 * Propagates an event to the last new instance of this Filter class
	 * 
	 * @param et
	 *            event to propagate
	 */
	public static void propagateEventOccurred(EventType et) {
		if (instance != null)
			instance.fireEventOccurred(et);
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
