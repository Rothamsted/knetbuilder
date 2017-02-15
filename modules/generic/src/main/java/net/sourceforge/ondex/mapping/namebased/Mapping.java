package net.sourceforge.ondex.mapping.namebased;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.IncludeType;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.annotations.Webservice;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.lucene.search.Query;

/**
 * Implements a ConceptName based mapping.
 * 
 * @author taubertj
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(description = "Tested December 2012 (Jan Taubert)", status = StatusType.STABLE)
@Webservice(description = "Added at Paul Fishers request! August 2011 Christian", include = IncludeType.ALWAYS)
public class Mapping extends ONDEXMapping implements ArgumentNames {

	private boolean includeUniDirection = true;

	// toggles debug messages to system.out
	private static boolean DEBUG = false;

	// defines, how many conceptNames have to be equal
	private int threshold = 2;

	// use only exact synonyms
	private boolean exactSyn = false;

	private boolean mapWithInDataSource = false;

	private boolean exactNameMapping = false;

	/**
	 * Constructor
	 */
	public Mapping() {
	}

	/**
	 * Returns name of this mapping.
	 * 
	 * @return String
	 */
	public String getName() {
		return new String("Concept name based mapping");
	}

	/**
	 * Returns version of this mapping.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return new String("27.12.2012");
	}

	@Override
	public String getId() {
		return "namebased";
	}

	/**
	 * Specifies all arguments to can be set for this mapping.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		// these are evaluated in the evaluateConcept in the super
		StringArgumentDefinition gdsEquals = new StringArgumentDefinition(
				ATTRIBUTE_EQUALS_ARG, ATTRIBUTE_EQUALS_ARG_DESC, false, null,
				true);

		StringMappingPairArgumentDefinition pairCC = new StringMappingPairArgumentDefinition(
				EQUIVALENT_CC_ARG, EQUIVALENT_CC_ARG_DESC, false, null, true);

		BooleanArgumentDefinition mapWithinCV = new BooleanArgumentDefinition(
				WITHIN_DATASOURCE_ARG, WITHIN_DATASOURCE_ARG_DESC, false, false);

		// these belong to this mapping method
		BooleanArgumentDefinition exactSyn = new BooleanArgumentDefinition(
				EXACT_SYN_ARG, EXACT_SYN_ARG_DESC, false, false);

		RangeArgumentDefinition<Integer> nameThreshold = new RangeArgumentDefinition<Integer>(
				NAME_THRESHOLD_ARG, NAME_THRESHOLD_ARG_DESC, false, 2, 0,
				Integer.MAX_VALUE, Integer.class);

		StringMappingPairArgumentDefinition ccRestriction = new StringMappingPairArgumentDefinition(
				CONCEPTCLASS_RESTRICTION_ARG,
				CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true);

		StringMappingPairArgumentDefinition cvRestriction = new StringMappingPairArgumentDefinition(
				DATASOURCE_RESTRICTION_ARG, DATASOURCE_RESTRICTION_ARG_DESC,
				false, null, true);

		BooleanArgumentDefinition exactNameMapping = new BooleanArgumentDefinition(
				EXACT_NAME_MAPPING_ARG, EXACT_NAME_MAPPING_ARG_DESC, false,
				false);

		return new ArgumentDefinition[] { gdsEquals, pairCC, exactSyn,
				nameThreshold, ccRestriction, cvRestriction, mapWithinCV,
				exactNameMapping };
	}

	private Map<ConceptClass, Set<ConceptClass>> ccRestrictionMap = null;

	@Override
	public void start() throws InvalidPluginArgumentException {

		if (args.getUniqueValue(EXACT_SYN_ARG) != null) {
			this.exactSyn = (Boolean) args.getUniqueValue(EXACT_SYN_ARG);
		}
		fireEventOccurred(new GeneralOutputEvent(
				"Matching of only exact synonyms is " + exactSyn,
				getCurrentMethodName()));
		if (args.getUniqueValue(NAME_THRESHOLD_ARG) != null)
			this.threshold = (Integer) args.getUniqueValue(NAME_THRESHOLD_ARG);
		fireEventOccurred(new GeneralOutputEvent(
				"Use name based mapping with concept name threshold "
						+ this.threshold, getCurrentMethodName()));
		if (args.getUniqueValue(WITHIN_DATASOURCE_ARG) != null) {
			mapWithInDataSource = (Boolean) args
					.getUniqueValue(WITHIN_DATASOURCE_ARG);
		}

		if (args.getUniqueValue(EXACT_NAME_MAPPING_ARG) != null) {
			exactNameMapping = (Boolean) args
					.getUniqueValue(EXACT_NAME_MAPPING_ARG);
		}

		if (ccRestrictionMap != null)
			ccRestrictionMap = null;
		Object[] validCCMap = args
				.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG);
		if (validCCMap != null) {
			for (Object ccPair : validCCMap) {
				String[] pair = ((String) ccPair).split(",");
				if (ccRestrictionMap == null) {
					ccRestrictionMap = new HashMap<ConceptClass, Set<ConceptClass>>();
				}
				ConceptClass fromCC = graph.getMetaData().getConceptClass(
						pair[0]);
				ConceptClass toCC = graph.getMetaData()
						.getConceptClass(pair[1]);

				// fwd mapping
				Set<ConceptClass> map = ccRestrictionMap.get(fromCC);
				if (map == null) {
					map = new HashSet<ConceptClass>();
					ccRestrictionMap.put(fromCC, map);
				}
				map.add(toCC);

				// add complement
				map = ccRestrictionMap.get(toCC);
				if (map == null) {
					map = new HashSet<ConceptClass>();
					ccRestrictionMap.put(toCC, map);
				}
				map.add(fromCC);
			}
		}

		// get restrictions on ConceptClasses or DataSources on relations
		Map<DataSource, DataSource> dataSourceMapping = getAllowedDataSources(graph);
		Map<ConceptClass, ConceptClass> ccMapping = getAllowedCCs(graph);

		// get the relationtype, evidencetype and hitattr for this mapping
		RelationType rtSet = graph.getMetaData().getRelationType(
				MetaData.relType);
		EvidenceType eviType = graph.getMetaData().getEvidenceType(
				MetaData.evidence);
		AttributeName hitAttr = graph.getMetaData().getAttributeName(
				MetaData.synHits);



		// will contain the concept combinations to be used for relations
		Map<ONDEXConcept, Map<ONDEXConcept, Integer>> relations = new HashMap<ONDEXConcept, Map<ONDEXConcept, Integer>>();

		NumberFormat decimalFormat = new DecimalFormat(".00");
		NumberFormat numberFormat = NumberFormat.getInstance();

		Set<ONDEXConcept> itConcept = graph.getConcepts();
		int processed = 0;
		int totals = itConcept.size();
		int increments = totals / 50;
		fireEventOccurred(new GeneralOutputEvent("Name based mapping on "
				+ totals + " Concepts", getCurrentMethodName()));

		for (ONDEXConcept concept : itConcept) {

			if (processed > 0 && processed % increments == 0) {
				fireEventOccurred(new GeneralOutputEvent("Mapping complete on "
						+ decimalFormat.format((double) processed
								/ (double) totals * 100d) + "% ("
						+ numberFormat.format(processed) + " Concepts)",
						getCurrentMethodName()));
				if (processed % 200000 == 0) {
					System.runFinalization();
				}
			}

			DataSource conceptDataSource = concept.getElementOf();
			ConceptClass fromCC = concept.getOfType();

			if (ccRestrictionMap != null
					&& !ccRestrictionMap.containsKey(fromCC)) {
				continue; // does not meet restrictions
			}

			processed++;

			if (processed % 1000 == 0) {
				if (DEBUG)
					System.out.println(processed + " out of " + totals
							+ " Concepts processed.");
			}

			// at least two concept names should be identical
			Set<String> cnames = new HashSet<String>();

			// add all concept names for this concept
			for (ConceptName cn : concept.getConceptNames()) {
				if (!exactSyn || cn.isPreferred()) {
					String name = LuceneEnv.stripText(cn.getName()).trim();
					if (name.length() > 0) {
						cnames.add(name);
					}
				}
			}

			if (cnames.size() >= this.threshold) {

				// map contains hit concept id to occurrence mapping
				Map<ONDEXConcept, Integer> occurrences = LazyMap.decorate(
						new HashMap<ONDEXConcept, Integer>(),
						new Factory<Integer>() {

							@Override
							public Integer create() {
								return 0;
							}
						});

				// iterate over all possible concept class combinations
				for (ConceptClass cc : getCCtoMapTo(graph, concept.getOfType())) {
					if (ccRestrictionMap != null
							&& !ccRestrictionMap.containsKey(cc)) {
						continue; // target cc does not meet restrictions
					}

					// iterate over all striped concept names
					for (String name : cnames) {
						Query query;

						if (mapWithInDataSource) {
							query = LuceneQueryBuilder
									.searchConceptByConceptNameExact(name, cc);
						} else {
							query = LuceneQueryBuilder
									.searchConceptByConceptNameExact(name,
											conceptDataSource, cc);
						}

						LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph
								.getSID());

						// search for concept name
						for (ONDEXConcept hitConcept : lenv
								.searchInConcepts(query)) {
							if (hitConcept instanceof LuceneConcept) {
								hitConcept = ((LuceneConcept) hitConcept)
										.getParent();
							}

							if (exactNameMapping) {
								boolean foundExact = false;
								for (ConceptName cn : hitConcept
										.getConceptNames()) {
									String compName = LuceneEnv.stripText(
											cn.getName()).trim();
									if (!exactSyn || cn.isPreferred()) {
										if (compName.equalsIgnoreCase(name)) {
											foundExact = true;
										}
									}
								}
								if (!foundExact) {
									continue;
								}
							}

							int count = occurrences.get(hitConcept);
							count++;
							occurrences.put(hitConcept, count);
						}
					}
				}

				// look for occurrences greater one
				for (ONDEXConcept hitConcept : occurrences.keySet()) {
					if (hitConcept.equals(concept)) {
						continue;
					}

					// at least two concept names are identical
					Set<String> hitCNames = new HashSet<String>();
					for (ConceptName cn : hitConcept.getConceptNames()) {
						if (!exactSyn || cn.isPreferred()) {
							String name = LuceneEnv.stripText(cn.getName())
									.trim();
							if (name.length() > 0) {
								hitCNames.add(name);
							}
						}
					}

					if (hitCNames.size() >= this.threshold) {

						// get hit concept with attributes
						DataSource hitConceptDataSource = hitConcept
								.getElementOf();

						// only map between different data sources
						if (mapWithInDataSource
								|| !conceptDataSource
										.equals(hitConceptDataSource)) {

							// evaluate mapping for other ds, cc and attr
							// restrictions
							if (evaluateMapping(graph, concept, hitConcept)) {

								Map<ONDEXConcept, Integer> toConceptToScore = relations
										.get(hitConcept);
								if (toConceptToScore == null) {
									toConceptToScore = new HashMap<ONDEXConcept, Integer>();
									relations.put(hitConcept, toConceptToScore);
								}

								// update score for this specific hitconcept,
								// concept
								int score = occurrences.get(hitConcept);
								toConceptToScore.put(concept, score);

							} else {
								if (DEBUG) {
									System.out
											.println("Evalution returned false for "
													+ concept.getId()
													+ " - "
													+ hitConcept.getId());
								}
							}
						} else {
							if (DEBUG) {
								System.out.println("equal DataSource!");
							}
						}
					} else {
						if (DEBUG) {
							System.out.println("Not above threshold "
									+ this.threshold);
						}
					}
				}
			}
		}
		itConcept = null;

		int unidirectional = 0;

		// iterator over all found relations
		if (DEBUG)
			System.out.println(relations.size()
					+ " concepts with hits so far..");
		for (ONDEXConcept fromConcept : relations.keySet().toArray(
				new ONDEXConcept[0])) {

			// get everything, that was mapped to this concept
			Map<ONDEXConcept, Integer> relationHits = relations
					.get(fromConcept);

			// get toConcepts
			for (ONDEXConcept toConcept : relationHits.keySet()) {

				// check for sufficient score
				int toScore = relationHits.get(toConcept);
				if (toScore < threshold) {
					continue;
				}

				// check for bidirectional hits
				if (includeUniDirection
						|| (relations.containsKey(toConcept) && relations.get(
								toConcept).containsKey(fromConcept))) {

					if (!(relations.containsKey(toConcept) && relations.get(
							toConcept).containsKey(fromConcept))) {
						Map<ONDEXConcept, Integer> map = relations
								.get(toConcept);
						if (map == null) {
							map = new HashMap<ONDEXConcept, Integer>();
							relations.put(toConcept, map);
						}
						map.put(fromConcept, toScore);
					}

					// check for sufficient score
					int fromScore = relations.get(toConcept).get(fromConcept);
					if (fromScore < threshold) {
						continue;
					}

					// check DataSource conditions
					DataSource fromDataSource = fromConcept.getElementOf();
					DataSource toDataSource = toConcept.getElementOf();
					if (!mapWithInDataSource
							&& dataSourceMapping.size() == 0
							&& toDataSource.equals(dataSourceMapping
									.get(fromDataSource))) {
						continue;
					}

					// check ConceptClass conditions
					ConceptClass fromCC = fromConcept.getOfType();
					ConceptClass toCC = toConcept.getOfType();
					if (ccMapping.size() == 0
							&& toCC.equals(ccMapping.get(fromCC))) {
						continue;
					}

					// different data sources
					if (mapWithInDataSource
							|| !fromConcept.getElementOf().equals(
									toConcept.getElementOf())) {

						int score = Math.min(toScore, fromScore);

						// get relation if existing
						ONDEXRelation relation = graph.getRelation(fromConcept,
								toConcept, rtSet);
						if (relation == null) {
							// create not existing relation
							relation = graph.getFactory().createRelation(
									fromConcept, toConcept, rtSet, eviType);
							// set confidence value
							relation.createAttribute(hitAttr,
									new Integer(score), false);
						} else {
							Set<EvidenceType> etit = relation.getEvidence();
							if (!etit.contains(eviType)) {
								// existing relations, add evi type
								relation.addEvidenceType(eviType);
								// set confidence value
								relation.createAttribute(hitAttr, new Integer(
										score), false);
							}
							etit = null;
						}

						// always create opposite direction too
						relation = graph.getRelation(toConcept, fromConcept,
								rtSet);
						if (relation == null) {
							// create not existing relation
							relation = graph.getFactory().createRelation(
									toConcept, fromConcept, rtSet, eviType);
							// set confidence value
							relation.createAttribute(hitAttr,
									new Integer(score), false);
						} else {
							Set<EvidenceType> etit = relation.getEvidence();
							if (!etit.contains(eviType)) {
								// existing relations, add evi type
								relation.addEvidenceType(eviType);
								// set confidence value
								relation.createAttribute(hitAttr, new Integer(
										score), false);
							}
							etit = null;
						}
					}
				} else {
					unidirectional++;
				}
			}
		}

		fireEventOccurred(new GeneralOutputEvent(
				"Uni directional hits excluded = " + unidirectional,
				getCurrentMethodName()));
	}

	/**
	 * This mapping requires an index ondex graph.
	 * 
	 * @return true
	 */
	public boolean requiresIndexedGraph() {
		return true;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}