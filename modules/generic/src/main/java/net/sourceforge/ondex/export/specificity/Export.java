package net.sourceforge.ondex.export.specificity;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.export.specificity.ThresholdArgumentDefinition.ThresholdType;

import java.util.*;

import static net.sourceforge.ondex.export.specificity.ArgumentNames.*;

/**
 * Attaches a numerical specificity measure on a set of relation for the
 * specificity of a given concept instance to its annotations. i.e. the degree
 * of the node relative to a set measure or a mean degree 1 = very specific 0 =
 * very general
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
@Status(description = "Set to DISCONTINUED 4 May 2010 due to System.out usage. (Christian)", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport {

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition target = new StringArgumentDefinition(
				TARGET_CC_ARG, TARGET_CC_ARG_DESC, true, null, true);

		StringArgumentDefinition source = new StringArgumentDefinition(
				SOURCE_CC_ARG, SOURCE_CC_ARG_DESC, true, null, true);

		StringArgumentDefinition rt = new StringArgumentDefinition(
				RELATION_TYPE_ARG, RELATION_TYPE_ARG_DESC, true, null, false);

		RangeArgumentDefinition<Double> thresh = new RangeArgumentDefinition<Double>(
				THRESHOLD_ARG, THRESHOLD_ARG_DESC, true, 5d, 0d,
				Double.MAX_VALUE, Double.class);

		StringArgumentDefinition thresh_type = new StringArgumentDefinition(
				THRESHOLD_TYPE_ARG, THRESHOLD_TYPE_ARG_DESC, true, "all", false);

		StringArgumentDefinition specificity_att = new StringArgumentDefinition(
				SPEC_ATT_TYPE_ARG, SPEC_ATT_TYPE_ARG_DESC, false,
				"Specificity", false);

		StringArgumentDefinition degree_att = new StringArgumentDefinition(
				DEGREE_ATT_TYPE_ARG, DEGREE_ATT_TYPE_ARG_DESC, false, "Degree",
				false);

		BooleanArgumentDefinition add2concept = new BooleanArgumentDefinition(
				ADD_2_CONCEPT_ARG, ADD_2_CONCEPT_ARG_DESC, false, false);

		BooleanArgumentDefinition add2relation = new BooleanArgumentDefinition(
				ADD_2_RELATION_ARG, ADD_2_RELATION_ARG_DESC, false, true);

		return new ArgumentDefinition<?>[] { target, source, rt, thresh_type,
				thresh, specificity_att, degree_att, add2concept, add2relation };
	}

	@Override
	public String getName() {
		return "Specificity measure";
	}

	@Override
	public String getVersion() {
		return "alpha";
	}

	@Override
	public String getId() {
		return "specificity";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {
		List<Object> cc_targ = (List<Object>) getArguments()
				.getObjectValueList(TARGET_CC_ARG);
		List<Object> cc_source = (List<Object>) getArguments()
				.getObjectValueList(SOURCE_CC_ARG);
		String rt = (String) getArguments().getUniqueValue(RELATION_TYPE_ARG);
		String spec_att = (String) getArguments().getUniqueValue(
				SPEC_ATT_TYPE_ARG);
		String degree_att = (String) getArguments().getUniqueValue(
				DEGREE_ATT_TYPE_ARG);

		Boolean addToConcept = (Boolean) getArguments().getUniqueValue(
				ADD_2_CONCEPT_ARG);
		Boolean addToRelation = (Boolean) getArguments().getUniqueValue(
				ADD_2_RELATION_ARG);

		List<ConceptClass> targets = new ArrayList<ConceptClass>();
		List<ConceptClass> sources = new ArrayList<ConceptClass>();

		for (Object targetCC : cc_targ) {
			ConceptClass target = getConceptClass(targetCC.toString(),
					graph.getMetaData());
			targets.add(target);
		}

		for (Object sourceCC : cc_source) {
			ConceptClass source = getConceptClass(sourceCC.toString(),
					graph.getMetaData());
			sources.add(source);
		}

		RelationType relation_type = getRelationType(rt, graph.getMetaData());

		Double thresh = (Double) getArguments().getUniqueValue(THRESHOLD_ARG);
		String threshType = (String) getArguments().getUniqueValue(
				THRESHOLD_TYPE_ARG);

		ThresholdType type = ThresholdArgumentDefinition
				.translateType(threshType);

		AttributeName specAtt = graph.getMetaData().getAttributeName(spec_att);
		if (specAtt == null) {
			specAtt = graph
					.getMetaData()
					.getFactory()
					.createAttributeName(
							spec_att,
							"Specificity measure where 1 is very specific and 0 is not at all specific",
							Double.class);
		}

		AttributeName degreeAtt = graph.getMetaData().getAttributeName(
				degree_att);
		if (degreeAtt == null) {
			degreeAtt = graph
					.getMetaData()
					.getFactory()
					.createAttributeName(
							degree_att,
							"Degree (number of edges with respect to a concept)",
							Integer.class);
		}

		// get relations specific to type
		Set<ONDEXRelation> allRelations = graph.getRelations();
		if (relation_type != null)
			allRelations = graph.getRelationsOfRelationType(relation_type);

		Set<ONDEXConcept> sourceConcepts = null;
		for (ConceptClass source : sources) {
			if (sourceConcepts == null) {
				sourceConcepts = new HashSet<ONDEXConcept>(
						graph.getConceptsOfConceptClass(source));
			} else {
				sourceConcepts.retainAll(graph
						.getConceptsOfConceptClass(source));
			}
		}

		Map<Integer, Integer> conceptToRelationCount = new HashMap<Integer, Integer>(
				sourceConcepts.size());
		Map<Integer, List<Integer>> conceptToRelations = new HashMap<Integer, List<Integer>>(
				sourceConcepts.size());

		List<Number> degreesOfNodes = new ArrayList<Number>(
				sourceConcepts.size());

		for (ONDEXConcept concept : sourceConcepts) {
			Set<ONDEXRelation> relations = new HashSet<ONDEXRelation>(
					graph.getRelationsOfConcept(concept));
			relations.retainAll(allRelations);
			List<Integer> relationIds = new ArrayList<Integer>(relations.size());

			for (ONDEXRelation relation : relations) {
				// both cases to account for both direction
				if ((targets.contains(relation.getFromConcept().getOfType()) || sources
						.contains(relation.getFromConcept().getOfType()))
						&& (targets.contains(relation.getToConcept()
								.getOfType()) || sources.contains(relation
								.getToConcept().getOfType()))) {
					relationIds.add(relation.getId());
				}
			}

			conceptToRelationCount.put(concept.getId(), relationIds.size());
			conceptToRelations.put(concept.getId(), relationIds);
			degreesOfNodes.add(relationIds.size());

		}

		List<Integer> concepts = new ArrayList<Integer>(
				conceptToRelationCount.keySet());
		Collections.sort(concepts,
				new MapSortComparator(conceptToRelationCount));

		double meanDegree = mean(degreesOfNodes);
		double stdev = stdev(degreesOfNodes);

		switch (type) {
		case ALL:
			break;
		case COUNT:
			normalizeAndInsertSpecificity(thresh.intValue(), specAtt,
					degreeAtt, conceptToRelations, addToConcept, addToRelation);
			break;
		case EXCLUDING_TOP_PERCENT:
			int numberOfConcept = concepts.size();
			double percent = thresh;
			int cutPoint = (int) Math.round(numberOfConcept * (percent / 100d));

			int highestValue = conceptToRelationCount.get(cutPoint);

			normalizeAndInsertSpecificity(highestValue, specAtt, degreeAtt,
					conceptToRelations, addToConcept, addToRelation);
			break;
		case STDEVS_ABOVE_MEAN:
			int stdevs = thresh.intValue();

			double high = meanDegree + (stdevs * stdev);
			normalizeAndInsertSpecificity(high, specAtt, degreeAtt,
					conceptToRelations, addToConcept, addToRelation);

			break;
		case STDEVS_BELOW_MEAN:
			stdevs = thresh.intValue();

			high = meanDegree - (stdevs * stdev);
			if (high < 0)
				high = 0;
			normalizeAndInsertSpecificity(high, specAtt, degreeAtt,
					conceptToRelations, addToConcept, addToRelation);
			break;
		default:
			throw new RuntimeException("Unknown " + type.getClass().getName()
					+ " " + type);
		}
	}

	/**
	 * Writes the specialization measure to the given nodes using the provided
	 * max degree normalization measure
	 * 
	 * @param norm
	 *            the max degree value to normalize on
	 * @param specAtt
	 *            the attribute to write values as
	 * @param conceptToRelations
	 *            the map of source nodes and the relevent edges
	 */
	private void normalizeAndInsertSpecificity(double norm,
			AttributeName specAtt, AttributeName degreeAtt,
			Map<Integer, List<Integer>> conceptToRelations,
			boolean addToConcept, boolean addToRelation) {
		for (Integer concept : conceptToRelations.keySet()) {
			List<Integer> relationIds = conceptToRelations.get(concept);
			double numRelations = Integer.valueOf(relationIds.size())
					.doubleValue();
			double specificity;
			if (numRelations > norm) {
				specificity = 0;
			} else {
				specificity = 1d - (numRelations / norm);
			}

			if (addToRelation) {
				for (Integer relationId : relationIds) {
					ONDEXRelation relation = graph.getRelation(relationId);
					relation.createAttribute(specAtt, specificity, false);
					relation.createAttribute(degreeAtt, (int) numRelations,
							false);
				}
			}
			if (addToConcept) {
				ONDEXConcept conceptEnt = graph.getConcept(concept);
				conceptEnt.createAttribute(specAtt, specificity, false);
				conceptEnt
						.createAttribute(degreeAtt, (int) numRelations, false);
			}
		}
	}

	/**
	 * Sorts values in a list by there lookup values in a map
	 * 
	 * @author hindlem
	 */
	private class MapSortComparator implements Comparator<Integer> {

		private Map<Integer, Integer> map;

		public MapSortComparator(Map<Integer, Integer> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return map.get(o1).compareTo(map.get(o2));
		}

	}

	/**
	 * Calculates Arithmetic Mean on values
	 * 
	 * @param values
	 * @return the mean
	 */
	private static double mean(List<Number> values) {
		double sum = 0; // sum of all the elements
		for (Number i : values) {
			sum += i.doubleValue();
		}
		return sum / values.size();
	}

	public static double stdev(List<Number> values) {
		double r = Double.NaN;
		if (values.size() > 1) {
			r = Math.sqrt(devsq(values) / (values.size() - 1));
		}
		return r;
	}

	public static double devsq(List<Number> values) {
		double r = Double.NaN;
		if (values != null && values.size() >= 1) {
			double m = 0;
			double sum = 0;
			int n = values.size();
			for (Number i : values) {
				sum += i.doubleValue();
			}
			m = sum / n;
			sum = 0;
			for (Number i : values) {
				double val = i.doubleValue();
				sum += (val - m) * (val - m);
			}

			r = (n == 1) ? 0 : sum;
		}
		return r;
	}

	/**
	 * @param ccName
	 *            the name of the concept class to retreave
	 * @param metaData
	 *            the metadata of the target graph
	 * @return the relevent ConceptClass if found
	 * @throws IllegalArgumentException
	 *             if the ConceptClass is not present in the graph
	 */
	private static ConceptClass getConceptClass(String ccName,
			ONDEXGraphMetaData metaData) throws IllegalArgumentException {
		ConceptClass target = metaData.getConceptClass(ccName);
		if (target == null) {
			throw new IllegalArgumentException(
					ccName
							+ " is not a ConceptClass found in the metadata of the target graph");
		}
		return target;
	}

	/**
	 * @param rtName
	 *            the name of the concept class to retreave
	 * @param metaData
	 *            the metadata of the target graph
	 * @return the relevent RelationType if found
	 * @throws IllegalArgumentException
	 *             if the ConceptClass is not present in the graph
	 */
	private static RelationType getRelationType(String rtName,
			ONDEXGraphMetaData metaData) throws IllegalArgumentException {
		RelationType target = metaData.getRelationType(rtName);
		if (target == null) {
			throw new IllegalArgumentException(
					rtName
							+ " is not a RelationType found in the metadata of the target graph");
		}
		return target;
	}

}
