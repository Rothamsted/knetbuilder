package net.sourceforge.ondex.mapping.accessionbased;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.config.LuceneRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.searchable.LuceneConcept;
import net.sourceforge.ondex.core.searchable.LuceneEnv;
import net.sourceforge.ondex.core.searchable.LuceneQueryBuilder;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.event.type.RelationTypeMissingEvent;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import org.apache.lucene.search.Query;

/**
 * Implements a ConceptAccession based mapping.
 * 
 * @author taubertj
 * @version 21.01.2008
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Status(description = "Found to be buggy and not considered worth checking fully August 2011 Christian", 
        status = StatusType.DISCONTINUED)
public class Mapping extends ONDEXMapping implements ArgumentNames {

	// toggles debug messages to system.out
	private static boolean DEBUG = false;

	// default relation type set
	private static String DEFAULT_RELATION = "equ";

	// ignore ambiguous flag of concept accessions
	private boolean ignoreAmbiguous = false;

	// stores DataSource mappings
	private Map<String, Set<DataSource>> dataSource2cvEquals = null;

	// for self DataSource mappings
	private Map<String, Set<DataSource>> dataSource2cvSelf = new HashMap<String, Set<DataSource>>();

	private boolean mapWithinCV = false;

	/**
	 * Simply calls super constructor.
	 */
	public Mapping() {
	}

	/**
	 * Specifies neccessary arguments for this mapping.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringMappingPairArgumentDefinition pairCC = new StringMappingPairArgumentDefinition(
				EQUIVALENT_CC_ARG, EQUIVALENT_CC_ARG_DESC, false, null, true);

		StringMappingPairArgumentDefinition pairCV = new StringMappingPairArgumentDefinition(
				EQUIVALENT_CV_ARG, EQUIVALENT_CV_ARG_DESC, false, null, true);

		StringArgumentDefinition gdsEquals = new StringArgumentDefinition(
				ATTRIBUTE_EQUALS_ARG, ATTRIBUTE_EQUALS_ARG_DESC, false, null,
				true);

		BooleanArgumentDefinition ignoreAmbiguousEquals = new BooleanArgumentDefinition(
				IGNORE_AMBIGUOUS_ARG, IGNORE_AMBIGUOUS_ARG_DESC, false, false);

		BooleanArgumentDefinition mapWithinCV = new BooleanArgumentDefinition(
				WITHIN_DATASOURCE_ARG, WITHIN_DATASOURCE_ARG_DESC, false, false);

		StringArgumentDefinition relationType = new StringArgumentDefinition(
				RELATION_TYPE_ARG, RELATION_TYPE_ARG_DESC, false, "equ", false);

		StringMappingPairArgumentDefinition ccRestriction = new StringMappingPairArgumentDefinition(
				CONCEPTCLASS_RESTRICTION_ARG,
				CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true);

		StringMappingPairArgumentDefinition cvRestriction = new StringMappingPairArgumentDefinition(
				DATASOURCE_RESTRICTION_ARG, DATASOURCE_RESTRICTION_ARG_DESC,
				false, null, true);

		return new ArgumentDefinition<?>[] { pairCC, pairCV, gdsEquals,
				ignoreAmbiguousEquals, relationType, ccRestriction,
				cvRestriction, mapWithinCV };
	}

	@Override
	public String getName() {
		return new String("ConceptAccession based mapping");
	}

	@Override
	public String getVersion() {
		return new String("21.01.2008");
	}

	@Override
	public String getId() {
		return "accessionbased";
	}

	/**
	 * Requires an indexed ondex graph.
	 * 
	 * @return true
	 */
	public boolean requiresIndexedGraph() {
		return true;
	}

	public void start() throws InvalidPluginArgumentException {

		if (args.getUniqueValue(WITHIN_DATASOURCE_ARG) != null) {
			mapWithinCV = (Boolean) args.getUniqueValue(WITHIN_DATASOURCE_ARG);
		}

		// get mapping options
		String relationType = DEFAULT_RELATION;
		if (args.getOptions().containsKey(RELATION_TYPE_ARG))
			relationType = args.getUniqueValue(RELATION_TYPE_ARG).toString();

		if (args.getOptions().containsKey(IGNORE_AMBIGUOUS_ARG))
			this.ignoreAmbiguous = (Boolean) args
					.getUniqueValue(IGNORE_AMBIGUOUS_ARG);

		fireEventOccurred(new GeneralOutputEvent(
				"ACC mapping ignore ambiguous is " + this.ignoreAmbiguous,
				"[Mapping - setONDEXGraph]"));

		// get the relationtypeset, evidencetype and hit count for the mapping
		RelationType rtSet = graph.getMetaData().getRelationType(relationType);
		EvidenceType eviType = graph.getMetaData().getEvidenceType(
				MetaData.evidence);
		AttributeName hitAttr = graph.getMetaData().getAttributeName(
				MetaData.accHit);

		if (rtSet == null)
			fireEventOccurred(new RelationTypeMissingEvent(MetaData.relType,
					"[Mapping - setONDEXGraph]"));
		if (eviType == null)
			fireEventOccurred(new EvidenceTypeMissingEvent(MetaData.evidence,
					"[Mapping - setONDEXGraph]"));
		if (hitAttr == null)
			fireEventOccurred(new AttributeNameMissingEvent(MetaData.accHit,
					"[Mapping - setONDEXGraph]"));

		int mappedConceptsCount = 0;

		// iterate over all concepts
		Set<ONDEXConcept> itConcepts = graph.getConcepts();
		double total = itConcepts.size();

		NumberFormat formatter = new DecimalFormat(".00");
		NumberFormat format = NumberFormat.getInstance();

		fireEventOccurred(new GeneralOutputEvent("Accession based mapping on "
				+ total + " concepts", "[accessionbased.Mapping - start]"));

		double current = 0;
		for (ONDEXConcept concept : itConcepts) {

			// get actual concept, cv and corresponding concept class
			current++;

			if ((current / total * 100 % 10) == 0) {
				System.out.println("Mapping complete on "
						+ formatter.format(current / total * 100d) + "% ("
						+ format.format(current) + " Concepts)");
				if (current % 200000 == 0) {
					System.runFinalization();
				}
			}

			DataSource conceptDataSource = concept.getElementOf();

			// map contains hit concept id to occurrence mapping
			Map<ONDEXConcept, Integer> occurrences = new HashMap<ONDEXConcept, Integer>();

			// iterate over all accession of this concept
			for (ConceptAccession conceptAcc : concept.getConceptAccessions()) {
				// accession must not be ambiguous or ignore ambiguous
				if (ignoreAmbiguous || !conceptAcc.isAmbiguous()) {
					Set<DataSource> dataSourceToMapTo = getCVtoMapTo(graph,
							conceptAcc.getElementOf());
					for (ConceptClass cc : getCCtoMapTo(graph,
							concept.getOfType())) {
						// possible DataSource for concept accessions
						for (DataSource dataSource : dataSourceToMapTo) {
							Query query;

							if (mapWithinCV) {
								query = LuceneQueryBuilder
										.searchConceptByConceptAccessionExact(
												dataSource,
												conceptAcc.getAccession(),
												null, cc, !ignoreAmbiguous);
							} else {
								query = LuceneQueryBuilder
										.searchConceptByConceptAccessionExact(
												dataSource,
												conceptAcc.getAccession(),
												concept.getElementOf(), cc,
												!ignoreAmbiguous);
							}
							LuceneEnv lenv = LuceneRegistry.sid2luceneEnv
									.get(graph.getSID());
							Set<ONDEXConcept> itResults = lenv
									.searchInConcepts(query);

							// look for the whole concept acc

							// the next valid ConceptClass & DataSource
							if (itResults != null) {
								// search for this concept accession
								for (ONDEXConcept hitConcept : itResults) {

									// get hit concept and relavent attributes
									// from
									// results
									if (hitConcept instanceof LuceneConcept) {
										hitConcept = ((LuceneConcept) hitConcept)
												.getParent();
									}

									DataSource hitConceptDataSource = hitConcept
											.getElementOf();

									// only map between different concept cvs or
									// equal CVS
									if (this.mapWithinCV
											|| !conceptDataSource
													.equals(hitConceptDataSource)) {
										// concept accession has to be from same
										// cv
										for (ConceptAccession hitConceptAcc : hitConcept
												.getConceptAccessions()) {

											// case insensitive search
											if (dataSourceToMapTo
													.contains(hitConceptAcc
															.getElementOf())
													&& hitConceptAcc
															.getAccession()
															.equalsIgnoreCase(
																	conceptAcc
																			.getAccession())) {

												if (this.evaluateMapping(graph,
														hitConcept, concept)) {
													// DataSource must be the
													// same and
													// accession must not be
													// ambiguous or ignore
													// ambigous
													if (ignoreAmbiguous
															|| !hitConceptAcc
																	.isAmbiguous()) {
														// get counts for hit
														// concept
                                                        //Untested bugfix August 2011
                                                        Integer theCount = occurrences.get(hitConcept);                                                       
														int count;
                                                        if (theCount == null) {
                                                            count = 1;
                                                        } else {
                                                            count = theCount.intValue() + 1;
                                                        }
														occurrences.put(
																hitConcept,
																count);
													}
												} else {
													if (DEBUG) {
														fireEventOccurred(new GeneralOutputEvent(
																"Not matching Attribute: "
																		+ concept
																				.getPID()
																		+ " and "
																		+ hitConcept
																				.getPID(),
																"[Mapping - setONDEXGraph]"));
													}
												}
											} else {
												if (DEBUG) {
													fireEventOccurred(new GeneralOutputEvent(
															"Not matching Accessions: "
																	+ conceptAcc.getAccession()
																	+ " ("
																	+ conceptAcc
																			.getElementOf()
																			.getId()
																	+ ") and "
																	+ hitConceptAcc.getAccession()
																	+ " ("
																	+ hitConceptAcc
																			.getElementOf()
																			.getId()
																	+ ")",
															"[Mapping - setONDEXGraph]"));
												}
											}
										}
									} else {
										if (DEBUG) {
											fireEventOccurred(new GeneralOutputEvent(
													"Not matching CVs between hitconcepts: "
															+ concept.getPID()
															+ " with "
															+ concept
																	.getElementOf()
																	.getId()
															+ " and "
															+ hitConcept.getPID()
															+ " with "
															+ hitConcept
																	.getElementOf()
																	.getId(),
													"[Mapping - setONDEXGraph]"));
										}
									}
								}
							} else {
								fireEventOccurred(new InconsistencyEvent(
										"Error with ConceptAccession Query '"
												+ conceptAcc.getAccession()
												+ "' for Concept with PID "
												+ concept.getPID() + ".",
										"[Mapping - setONDEXGraph]"));
							}
						}
					}
				}
			}

			// look for occurrences

			for (ONDEXConcept hitConcept : occurrences.keySet()) {
				// try if relation was already created
				ONDEXConcept fromConcept = concept;
				if (concept instanceof LuceneConcept) {
					fromConcept = ((LuceneConcept) concept).getParent();
				}
				ONDEXConcept toConcept = hitConcept;
				if (hitConcept instanceof LuceneConcept) {
					toConcept = ((LuceneConcept) hitConcept).getParent();
				}

				ONDEXRelation relation = graph.getRelation(fromConcept,
						toConcept, rtSet);
				if (relation == null) {
					// create a new relation between the two concepts
					relation = graph.getFactory().createRelation(fromConcept,
							toConcept, rtSet, eviType);
					relation.createAttribute(hitAttr,
							occurrences.get(hitConcept), false);
				} else {
					Set<EvidenceType> etit = relation.getEvidence();
					if (!etit.contains(eviType)) {
						relation.addEvidenceType(eviType);
					}

					// increase the number of acc hits
					Attribute attribute = relation.getAttribute(hitAttr);
					if (attribute == null) {
						attribute = relation.createAttribute(hitAttr,
								occurrences.get(hitConcept), false);
					} else {
						int count = ((Integer) attribute.getValue())
								+ occurrences.get(hitConcept);
						attribute.setValue(count);
					}

				}
				mappedConceptsCount++;
			}
		}

		fireEventOccurred(new GeneralOutputEvent("Mapped accessions "
				+ mappedConceptsCount + ".", "[Mapping - setONDEXGraph]"));
	}

	/**
	 * Returns the set of DataSource to which are equivalent to the current
	 * DataSource.
	 * <p/>
	 * By default the "from" DataSource is included in the result.
	 * 
	 * @param graph
	 *            AbstractONDEXGraph
	 * @param from
	 *            DataSource
	 * @return Set<DataSource>
	 */
	private Set<DataSource> getCVtoMapTo(ONDEXGraph graph, DataSource from)
			throws InvalidPluginArgumentException {

		// parse mapping arguments for CVs
		if (args.getOptions().containsKey(EQUIVALENT_CV_ARG)
				&& dataSource2cvEquals == null) {
			dataSource2cvEquals = new HashMap<String, Set<DataSource>>(5);
			List<String> cvSet = args.getObjectValueList(EQUIVALENT_CV_ARG,
					String.class);
			for (String cv : cvSet) {
				String[] mapping = cv.split(",");
				if (mapping.length == 2) {
					if (!dataSource2cvEquals.containsKey(mapping[0]))
						dataSource2cvEquals.put(mapping[0],
								new HashSet<DataSource>());
					if (!dataSource2cvEquals.containsKey(mapping[1]))
						dataSource2cvEquals.put(mapping[1],
								new HashSet<DataSource>());
					DataSource dataSource0 = graph.getMetaData().getDataSource(
							mapping[0]);
					DataSource dataSource1 = graph.getMetaData().getDataSource(
							mapping[1]);
					dataSource2cvEquals.get(mapping[0]).add(dataSource0);
					dataSource2cvEquals.get(mapping[0]).add(dataSource1);
					dataSource2cvEquals.get(mapping[1]).add(dataSource0);
					dataSource2cvEquals.get(mapping[1]).add(dataSource1);
				}
			}
		}

		// return equivalent CVs or only selfmatch
		if (dataSource2cvEquals != null
				&& dataSource2cvEquals.containsKey(from.getId())) {
			return dataSource2cvEquals.get(from.getId());
		} else {
			if (!dataSource2cvSelf.containsKey(from.getId())) {
				dataSource2cvSelf.put(from.getId(), new HashSet<DataSource>());
				dataSource2cvSelf.get(from.getId()).add(from);
			}
			return dataSource2cvSelf.get(from.getId());
		}
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}