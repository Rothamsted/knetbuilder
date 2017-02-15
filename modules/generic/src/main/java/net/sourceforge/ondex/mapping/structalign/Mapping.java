package net.sourceforge.ondex.mapping.structalign;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
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
 * Implements the StructAlign mapping.
 * 
 * @author taubertj
 * @version 28.12.2012
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(description = "Tested December 2012 (Jan Taubert)", status = StatusType.STABLE)
public class Mapping extends ONDEXMapping implements ArgumentNames {

	// defines depth of neighbourhood
	private int depth = 1;

	// use only exact synonyms
	private boolean exact = false;

	/**
	 * Constructor
	 */
	public Mapping() {
		super();
	}

	/**
	 * Returns the arguments required by this mapping.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		StringMappingPairArgumentDefinition pairCC = new StringMappingPairArgumentDefinition(
				EQUIVALENT_CC_ARG, EQUIVALENT_CC_ARG_DESC, false, null, true);

		StringArgumentDefinition attrEquals = new StringArgumentDefinition(
				ATTRIBUTE_EQUALS_ARG, ATTRIBUTE_EQUALS_ARG_DESC, false, null,
				true);

		BooleanArgumentDefinition exactSyns = new BooleanArgumentDefinition(
				EXACT_SYN_ARG, EXACT_SYN_ARG_DESC, false, false);

		RangeArgumentDefinition<Integer> depthArg = new RangeArgumentDefinition<Integer>(
				DEPTH_ARG, DEPTH_ARG_DESC, true, 2, 0, Integer.MAX_VALUE,
				Integer.class);

		StringMappingPairArgumentDefinition ccRestriction = new StringMappingPairArgumentDefinition(
				CONCEPTCLASS_RESTRICTION_ARG,
				CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true);

		StringMappingPairArgumentDefinition dsRestriction = new StringMappingPairArgumentDefinition(
				DATASOURCE_RESTRICTION_ARG, DATASOURCE_RESTRICTION_ARG_DESC,
				false, null, true);

		return new ArgumentDefinition<?>[] { pairCC, attrEquals, exactSyns,
				depthArg, ccRestriction, dsRestriction };
	}

	/**
	 * Returns the name of this mapping.
	 * 
	 * @return String
	 */
	public String getName() {
		return new String("StructAlign based mapping");
	}

	/**
	 * Returns the version of this mapping.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return new String("28.12.2012");
	}

	@Override
	public String getId() {
		return "structalign";
	}

	/**
	 * Requires a Lucene index.
	 * 
	 * @return true
	 */
	public boolean requiresIndexedGraph() {
		return true;
	}

	@Override
	public void start() throws InvalidPluginArgumentException {

		if (args.getOptions().containsKey(DEPTH_ARG)) {
			fireEventOccurred(new GeneralOutputEvent(
					"Change depth for neighbourhood.. ", getCurrentMethodName()));
			this.depth = ((Integer) args.getUniqueValue(DEPTH_ARG));
		}
		fireEventOccurred(new GeneralOutputEvent(
				"Use StructAlign based mapping with depth for neighbourhood "
						+ this.depth, getCurrentMethodName()));

		if (args.getOptions().containsKey(EXACT_SYN_ARG)) {
			fireEventOccurred(new GeneralOutputEvent(
					"Change exact synonym matching.. ", getCurrentMethodName()));
			this.exact = (Boolean) args.getUniqueValue(EXACT_SYN_ARG);
		}
		fireEventOccurred(new GeneralOutputEvent(
				"Use StructAlign based mapping with exact synonym matching set to "
						+ this.exact, getCurrentMethodName()));

		// get restrictions on ConceptClasses or DataSources on relations
		Map<DataSource, DataSource> dataSourceMapping = getAllowedDataSources(graph);
		Map<ConceptClass, ConceptClass> ccMapping = getAllowedCCs(graph);

		// get the relation type, evidence type and hit count for the mapping
		RelationType relType = graph.getMetaData().getRelationType(
				MetaData.relType);
		EvidenceType eviType = graph.getMetaData().getEvidenceType(
				MetaData.evidence + this.depth);
		if (eviType == null) {
			eviType = graph
					.getMetaData()
					.getFactory()
					.createEvidenceType(MetaData.evidence + this.depth,
							"StuctAlign level " + this.depth);
		}
		AttributeName hitAttr = graph.getMetaData().getAttributeName(
				MetaData.hitAttr);
		if (hitAttr == null) {
			hitAttr = graph.getMetaData().getFactory()
					.createAttributeName(MetaData.hitAttr, Integer.class);
		}

		// contains hits of similar concept class, but different data source
		// using search with concept names, lazy initialisation
		Map<ONDEXConcept, Set<ONDEXConcept>> concept2hitConcepts = LazyMap
				.decorate(new HashMap<ONDEXConcept, Set<ONDEXConcept>>(),
						new Factory<Set<ONDEXConcept>>() {

							@Override
							public Set<ONDEXConcept> create() {
								return new HashSet<ONDEXConcept>();
							}
						});

		// iterate over all concepts
		for (ONDEXConcept concept : graph.getConcepts()) {

			// get actual concept and data source
			DataSource conceptDataSource = concept.getElementOf();

			// add all concept names for this concept
			Set<String> cnames = new HashSet<String>();
			for (ConceptName cn : concept.getConceptNames()) {
				// use only exact synonyms
				if (!exact || cn.isPreferred()) {
					String name = LuceneEnv.stripText(cn.getName());
					cnames.add(name);
				}
			}

			// deal with ConceptClass mapping
			for (ConceptClass cc : getCCtoMapTo(graph, concept.getOfType())) {
				// iterate over all striped concept names
				for (String name : cnames) {
					Query query = LuceneQueryBuilder
							.searchConceptByConceptNameExact(name,
									concept.getElementOf(), cc);

					// iterator over search results
					LuceneEnv lenv = LuceneRegistry.sid2luceneEnv.get(graph
							.getSID());
					// iterate over hit concepts
					for (ONDEXConcept hitConcept : lenv.searchInConcepts(query)) {
						DataSource hitConceptDataSource = hitConcept
								.getElementOf();

						// ConceptClass equal, DataSource not
						if (!conceptDataSource.equals(hitConceptDataSource)) {
							if (this.evaluateMapping(graph, hitConcept, concept)) {
								// add hit concept to set for current concept
								concept2hitConcepts.get(concept).add(
										((LuceneConcept) hitConcept)
												.getParent());
							}
						}
					}
				}
			}
		}

		// count outcome of concept name search
		int hits = 0;
		for (ONDEXConcept c : concept2hitConcepts.keySet()) {
			if (concept2hitConcepts.get(c).size() > 0) {
				hits++;
			}
		}

		fireEventOccurred(new GeneralOutputEvent(
				"Finished looking for ConceptName hits. Found " + hits
						+ " concepts with at least one hit.",
				getCurrentMethodName()));
		fireEventOccurred(new GeneralOutputEvent(
				"Start building connectivity list.", getCurrentMethodName()));

		// all pre-calculated connectivity lists, lazy initialisation
		Map<ONDEXConcept, Map<RelationType, Set<ONDEXConcept>>> connectivity = LazyMap
				.decorate(
						new HashMap<ONDEXConcept, Map<RelationType, Set<ONDEXConcept>>>(),
						new Factory<Map<RelationType, Set<ONDEXConcept>>>() {

							@Override
							public Map<RelationType, Set<ONDEXConcept>> create() {
								return LazyMap
										.decorate(
												new HashMap<RelationType, Set<ONDEXConcept>>(),
												new Factory<Set<ONDEXConcept>>() {

													@Override
													public Set<ONDEXConcept> create() {
														return new HashSet<ONDEXConcept>();
													}
												});
							}
						});

		// iterate over all relations to fill connectivity list
		Set<ONDEXRelation> itRelations = graph.getRelations();

		// format process progress
		NumberFormat decimalFormat = new DecimalFormat(".00");
		NumberFormat numberFormat = NumberFormat.getInstance();
		int processed = 0;
		int totals = itRelations.size();
		int increments = totals / 50;
		fireEventOccurred(new GeneralOutputEvent("StructAlign mapping on "
				+ totals + " Relations", getCurrentMethodName()));
		int nbConnectivity = 0;

		for (ONDEXRelation r : itRelations) {

			if (processed > 0 && processed % increments == 0) {
				fireEventOccurred(new GeneralOutputEvent(
						"Building connectivity list complete on "
								+ decimalFormat.format((double) processed
										/ (double) totals * 100d) + "% ("
								+ numberFormat.format(processed)
								+ " Relations)", getCurrentMethodName()));
				if (processed % 200000 == 0) {
					System.runFinalization();
				}
			}

			processed++;

			// get next relation and associated concepts
			ONDEXConcept fromConcept = r.getFromConcept();
			ONDEXConcept toConcept = r.getToConcept();

			// check for same DataSource and not self-loop
			if (!fromConcept.equals(toConcept)
					&& fromConcept.getElementOf().equals(
							toConcept.getElementOf())) {

				RelationType rt = r.getOfType();

				// add toConcept for this RelationType to fromConcept
				connectivity.get(fromConcept).get(rt).add(toConcept);

				// add fromConcept for this RelationType to toConcept
				connectivity.get(toConcept).get(rt).add(fromConcept);

				nbConnectivity++;
			}
		}

		fireEventOccurred(new GeneralOutputEvent(
				"Finished building bi-directional connectivity list containing "
						+ 2 * nbConnectivity + " connections.",
				getCurrentMethodName()));

		// will contain the concept combinations to be used for relations
		Map<ONDEXConcept, Map<ONDEXConcept, Integer>> relations = LazyMap
				.decorate(
						new HashMap<ONDEXConcept, Map<ONDEXConcept, Integer>>(),
						new Factory<Map<ONDEXConcept, Integer>>() {

							@Override
							public Map<ONDEXConcept, Integer> create() {
								return LazyMap.decorate(
										new HashMap<ONDEXConcept, Integer>(),
										new Factory<Integer>() {

											@Override
											public Integer create() {
												return Integer.valueOf(0);
											}
										});
							}
						});

		int nbMatches = 0;

		// iterate over all concepts
		for (ONDEXConcept fromConcept : concept2hitConcepts.keySet()) {

			List<Map<RelationType, Set<ONDEXConcept>>> fromTypeConcepts = getReachableRelationTypeConcepts(
					connectivity, fromConcept);

			// look at hits for current concept
			for (ONDEXConcept toConcept : concept2hitConcepts.get(fromConcept)) {

				// get intersection of relation types for current pair of
				// concepts
				List<Set<RelationType>> intersection = new ArrayList<Set<RelationType>>();
				List<Map<RelationType, Set<ONDEXConcept>>> toTypeConcepts = getReachableRelationTypeConcepts(
						connectivity, toConcept);

				for (int i = 0; i < depth; i++) {
					// intersection calculated level wise
					Set<RelationType> levelIntersect = new HashSet<RelationType>();
					levelIntersect.addAll(toTypeConcepts.get(i).keySet());
					levelIntersect.retainAll(fromTypeConcepts.get(i).keySet());
					if (!levelIntersect.isEmpty())
						intersection.add(levelIntersect);
				}

				// check if concepts can be reached by the same relType across
				// all levels
				if (intersection.size() == depth) {

					// collect all possible neighbours via same relation types
					Set<ONDEXConcept> fromAllConcepts = new HashSet<ONDEXConcept>();
					Set<ONDEXConcept> toAllConcepts = new HashSet<ONDEXConcept>();

					// iterate over intersection levels of relTypes
					for (int i = 0; i < depth; i++) {

						// neighbours at current level
						Map<RelationType, Set<ONDEXConcept>> fromConcepts = fromTypeConcepts
								.get(i);
						Map<RelationType, Set<ONDEXConcept>> toConcepts = toTypeConcepts
								.get(i);

						// collapse levels
						for (RelationType rt : intersection.get(i)) {
							fromAllConcepts.addAll(fromConcepts.get(rt));
							toAllConcepts.addAll(toConcepts.get(rt));
						}
					}

					// compare neighbourhoods for concept name matches
					for (ONDEXConcept reachableFromConcept : fromAllConcepts) {

						// make sure concept had hits
						if (!concept2hitConcepts
								.containsKey(reachableFromConcept))
							continue;

						// build intersection between concept name matches
						Set<ONDEXConcept> hitIntersection = new HashSet<ONDEXConcept>();
						hitIntersection.addAll(concept2hitConcepts
								.get(reachableFromConcept));
						hitIntersection.retainAll(toAllConcepts);

						// if intersection not empty create relation
						if (!hitIntersection.isEmpty()) {

							nbMatches++;

							// check DataSource conditions
							DataSource fromDataSource = fromConcept
									.getElementOf();
							DataSource toDataSource = toConcept.getElementOf();
							if (dataSourceMapping.size() > 0
									&& !toDataSource.equals(dataSourceMapping
											.get(fromDataSource))) {
								continue;
							}

							// check ConceptClass conditions
							ConceptClass fromCC = fromConcept.getOfType();
							ConceptClass toCC = toConcept.getOfType();
							if (ccMapping.size() > 0
									&& !toCC.equals(ccMapping.get(fromCC))) {
								continue;
							}

							// between different DataSource
							if (!fromConcept.getElementOf().equals(
									toConcept.getElementOf())) {

								// store increment hit value
								relations.get(fromConcept).put(
										toConcept,
										relations.get(fromConcept).get(
												toConcept) + 1);
							}
						}
					}
				}
			}
		}

		fireEventOccurred(new GeneralOutputEvent(
				"Finished graph neighborhood alignment. Found " + nbMatches
						+ " possible matches.", getCurrentMethodName()));

		int unidirectional = 0;

		// iterator over all found relations
		for (ONDEXConcept fromConcept : relations.keySet()) {

			// get toConcept
			for (ONDEXConcept toConcept : relations.get(fromConcept).keySet()) {
				Integer score = relations.get(fromConcept).get(toConcept);

				// check for bidirectional hits
				if (relations.containsKey(toConcept)
						&& relations.get(toConcept).containsKey(fromConcept)) {

					// different data sources
					if (!fromConcept.getElementOf().equals(
							toConcept.getElementOf())) {

						// get relation if existing
						ONDEXRelation relation = graph.getRelation(fromConcept,
								toConcept, relType);

						if (relation == null) {
							// create not existing relation
							relation = graph.getFactory().createRelation(
									fromConcept, toConcept, relType, eviType);
						}

						Set<EvidenceType> etit = relation.getEvidence();
						if (!etit.contains(eviType)) {
							// existing relations, add evi type
							relation.addEvidenceType(eviType);
						}

						// set confidence value
						relation.createAttribute(hitAttr, score, false);
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
	 * Performs a breadth first search to get each connected relation type and
	 * set of concepts ordered by depth.
	 * 
	 * @param connectivity
	 * @param root
	 * @return
	 */
	private List<Map<RelationType, Set<ONDEXConcept>>> getReachableRelationTypeConcepts(
			Map<ONDEXConcept, Map<RelationType, Set<ONDEXConcept>>> connectivity,
			ONDEXConcept root) {

		List<Map<RelationType, Set<ONDEXConcept>>> result = new ArrayList<Map<RelationType, Set<ONDEXConcept>>>();

		Map<RelationType, Set<ONDEXConcept>> neighbours = connectivity
				.get(root);

		// breadth first search
		for (int i = 0; i < depth; i++) {

			result.add(neighbours);

			Map<RelationType, Set<ONDEXConcept>> newNeighbours = LazyMap
					.decorate(new HashMap<RelationType, Set<ONDEXConcept>>(),
							new Factory<Set<ONDEXConcept>>() {

								@Override
								public Set<ONDEXConcept> create() {
									return new HashSet<ONDEXConcept>();
								}
							});

			// iterate over all current connections
			for (RelationType rt : neighbours.keySet()) {
				for (ONDEXConcept to : neighbours.get(rt)) {

					// this is next level down
					for (RelationType rt2 : connectivity.get(to).keySet()) {
						for (ONDEXConcept n : connectivity.get(to).get(rt2)) {
							newNeighbours.get(rt2).add(n);
						}
					}
				}
			}

			neighbours = newNeighbours;
		}

		return result;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}