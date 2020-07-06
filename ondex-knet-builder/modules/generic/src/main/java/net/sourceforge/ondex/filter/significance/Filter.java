package net.sourceforge.ondex.filter.significance;

import java.util.BitSet;
import java.util.HashSet;
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
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Removes relations below a set significance value of a Attribute from the graph
 * 
 * @author taubertj, lysenkoa
 * @version 17.04.2008
 */
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Jan Taubert", "Artem Lysenko" }, emails = {
		"jantaubert at users.sourceforge.net",
		"lysenkoa at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	// contains list of visible concepts
	private Set<ONDEXConcept> concepts = null;

	// contains list of visible relations
	private Set<ONDEXRelation> relations = null;

	// contains list of invisible concepts
	private Set<ONDEXConcept> invconcepts = null;

	// contains list of invisible relations
	private Set<ONDEXRelation> invrelations = null;

	/**
	 * Constructor
	 */
	public Filter() {
	}

	@Override
	public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
		ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
		for (ONDEXConcept c : concepts) {
			graphCloner.cloneConcept(c);
		}

		for (ONDEXRelation r : relations) {
			graphCloner.cloneRelation(r);
		}
	}

	@Override
	public Set<ONDEXConcept> getVisibleConcepts() {
		return BitSetFunctions.unmodifiableSet(concepts);
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return BitSetFunctions.unmodifiableSet(relations);
	}

	public Set<ONDEXConcept> getInVisibleConcepts() {
		return BitSetFunctions.unmodifiableSet(invconcepts);
	}

	public Set<ONDEXRelation> getInVisibleRelations() {
		return BitSetFunctions.unmodifiableSet(invrelations);
	}

	/**
	 * Returns the name of this filter.
	 * 
	 * @return name
	 */
	public String getName() {
		return "Significance Filter";
	}

	/**
	 * Returns the version of this filter.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return "17.04.2008";
	}

	@Override
	public String getId() {
		return "significance";
	}

	/**
	 * Two arguments: about attribute name and about significance value.
	 * 
	 * @return two argument definitions
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition an_arg = new StringArgumentDefinition(
				TARGETAN_ARG, TARGETAN_ARG_DESC, true, null, false);
		RangeArgumentDefinition<Double> sig_arg = new RangeArgumentDefinition<Double>(
				SIG_ARG, SIG_ARG_DESC, true, 1.0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.class);
		BooleanArgumentDefinition inv_arg = new BooleanArgumentDefinition(
				INVERSE_ARG, INVERSE_ARG_DESC, false, false);
		BooleanArgumentDefinition abs_arg = new BooleanArgumentDefinition(
				ABSOLUTE_ARG, ABSOLUTE_ARG_DESC, false, false);
		BooleanArgumentDefinition na_arg = new BooleanArgumentDefinition(
				NO_ATT_ARG, NO_ATT_ARG_DESC, false, false);
		BooleanArgumentDefinition mode_arg = new BooleanArgumentDefinition(
				CONCEPTMODE_ARG, CONCEPTMODE_ARG_DESC, false, false);

		return new ArgumentDefinition<?>[] { an_arg, sig_arg, inv_arg, abs_arg,
				na_arg, mode_arg };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 */
	public void start() throws InvalidPluginArgumentException {

		AttributeName attributeName = null;

		// get attribute name from arguments
		String targetAttributeName = (String) args.getUniqueValue(TARGETAN_ARG);
		boolean isConceptMode = (Boolean) args.getUniqueValue(CONCEPTMODE_ARG);
		if (targetAttributeName != null
				&& targetAttributeName.trim().length() > 0) {
			attributeName = graph.getMetaData().getAttributeName(
					targetAttributeName);
			if (attributeName != null) {
				// check for number data type
				Class<?> clazz = attributeName.getDataType();
				if (clazz.getSuperclass().equals(Number.class)) {
					fireEventOccurred(new GeneralOutputEvent(
							"Using AttributeName" + attributeName.getId(),
							"[Filter - start]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(
							targetAttributeName
									+ " is not a subclass of Number.",
							"[Filter - start]"));
					return;
				}
			} else {
				fireEventOccurred(new WrongParameterEvent(targetAttributeName
						+ " is not a valid AttributeName.", "[Filter - start]"));
				return;
			}
		} else {
			fireEventOccurred(new WrongParameterEvent(
					"No target attribute name given.", "[Filter - start]"));
			return;
		}

		// get threshold value
		double threshold = (Double) args.getUniqueValue(SIG_ARG);

		boolean useInverseThreshold = false;
		if (args.getUniqueValue(INVERSE_ARG) != null)
			useInverseThreshold = (Boolean) args.getUniqueValue(INVERSE_ARG);

		boolean useAbsoluteValues = false;
		if (args.getUniqueValue(ABSOLUTE_ARG) != null)
			useAbsoluteValues = (Boolean) args.getUniqueValue(ABSOLUTE_ARG);

		boolean removeNoAttribute = false;
		if (args.getUniqueValue(NO_ATT_ARG) != null)
			removeNoAttribute = (Boolean) args.getUniqueValue(NO_ATT_ARG);

		// get existing concepts and relations
		relations = BitSetFunctions.copy(graph.getRelations());
		concepts = BitSetFunctions.copy(graph.getConcepts());

		// process relations
		if (!isConceptMode) {

			// contains relations not satisfying threshold
			Set<Integer> relationsToBeRemoved = new HashSet<Integer>();
			Set<ONDEXRelation> itRelationsOfAttributeName = graph
					.getRelationsOfAttributeName(attributeName);
			Set<ONDEXRelation> noAttributeRelations = null;

			// collects all relations which do not have attribute
			if (removeNoAttribute) {
				noAttributeRelations = BitSetFunctions.copy(graph
						.getRelations());
				noAttributeRelations.removeAll(itRelationsOfAttributeName);
			}

			// iterate over relations of attribute name
			for (ONDEXRelation r : itRelationsOfAttributeName) {

				// get Attribute and number value for attribute name
				Attribute attribute = r.getAttribute(attributeName);
				Number number = (Number) attribute.getValue();

				// convert to absolute value
				if (useAbsoluteValues) {
					number = Math.abs(number.doubleValue());
				}

				// filter for threshold
				if (!useInverseThreshold) {
					// filter values smaller threshold, i.e. keep relations with
					// value greater or equal than threshold
					if (number.doubleValue() < threshold)
						relationsToBeRemoved.add(r.getId());
				} else {
					// filter values larger threshold, i.e. keep relations with
					// value smaller than threshold
					if (number.doubleValue() >= threshold)
						relationsToBeRemoved.add(r.getId());
				}
			}

			// filter out below threshold relations
			invrelations = BitSetFunctions
					.create(graph, ONDEXRelation.class, 
							relationsToBeRemoved);

			// relations without attribute need to be add to invisible set
			if (removeNoAttribute)
				invrelations.addAll(noAttributeRelations);

			// remove invisible relations from all relations
			relations.removeAll(invrelations);

			// no concept has been made invisible
			invconcepts = BitSetFunctions.create(graph, ONDEXConcept.class,
					new BitSet(0));

		}

		// process concepts
		else {

			// contains concepts not satisfying threshold
			Set<Integer> conceptsToBeRemoved = new HashSet<Integer>();
			Set<Integer> relationsToBeRemoved = new HashSet<Integer>();
			Set<ONDEXConcept> itConceptsOfAttributeName = graph
					.getConceptsOfAttributeName(attributeName);
			Set<ONDEXConcept> noAttributeConcepts = null;

			// collects all concepts which do not have attribute
			if (removeNoAttribute) {
				noAttributeConcepts = BitSetFunctions.copy(graph
						.getConcepts());
				noAttributeConcepts.removeAll(itConceptsOfAttributeName);

				// remove also relations belonging to removed concepts
				for (ONDEXConcept c : noAttributeConcepts) {
					for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
						relationsToBeRemoved.add(r.getId());
					}
				}
			}

			// iterate over concepts of attribute name
			for (ONDEXConcept c : itConceptsOfAttributeName) {

				// get Attribute and number value for attribute name
				Attribute attribute = c.getAttribute(attributeName);
				Number number = (Number) attribute.getValue();

				// convert to absolute value
				if (useAbsoluteValues) {
					number = Math.abs(number.doubleValue());
				}

				// filter for threshold
				if (!useInverseThreshold) {
					// filter values smaller threshold, i.e. keep concepts with
					// value greater or equal than threshold
					if (number.doubleValue() < threshold) {
						conceptsToBeRemoved.add(c.getId());
						// remove also relations belonging to removed concept
						for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
							relationsToBeRemoved.add(r.getId());
						}
					}
				} else {
					// filter values larger threshold, i.e. keep concepts with
					// value smaller than threshold
					if (number.doubleValue() >= threshold) {
						conceptsToBeRemoved.add(c.getId());
						// remove also relations belonging to removed concept
						for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
							relationsToBeRemoved.add(r.getId());
						}
					}
				}
			}

			// filter out below threshold concepts
			invconcepts = BitSetFunctions.create(graph, ONDEXConcept.class,
					conceptsToBeRemoved);

			// concepts without attribute need to be add to invisible set
			if (removeNoAttribute)
				invconcepts.addAll(noAttributeConcepts);

			// remove invisible concepts from all concepts
			concepts.removeAll(invconcepts);

			// filter out relations for which concepts were removed
			invrelations = BitSetFunctions
					.create(graph, ONDEXRelation.class, relationsToBeRemoved);

			// remove invisible relations from all relations
			relations.removeAll(invrelations);
		}
	}

	/**
	 * An indexed graph is not required.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	/**
	 * No validators are required.
	 */
	public String[] requiresValidators() {
		return new String[0];
	}
}
