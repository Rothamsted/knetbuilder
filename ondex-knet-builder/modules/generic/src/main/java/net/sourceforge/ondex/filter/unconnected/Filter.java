package net.sourceforge.ondex.filter.unconnected;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Removes all concepts without connections to relations in the specified graph.
 * 
 * @author taubertj
 * @version 31.01.2008
 */
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	// contains list of visible concepts
	private Set<ONDEXConcept> concepts = null;

	// contains list of visible relations
	private Set<ONDEXRelation> relations = null;

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
		return concepts;
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return relations;
	}

	@Override
	public String getName() {
		return "Unconnected Filter";
	}

	@Override
	public String getVersion() {
		return "04.04.2011";
	}

	@Override
	public String getId() {
		return "unconnected";
	}

	/**
	 * No arguments required.
	 * 
	 * @return null
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		return new ArgumentDefinition<?>[] {
				new BooleanArgumentDefinition(ArgumentNames.REMOVE_TAG_ARG,
						ArgumentNames.REMOVE_TAG_ARG_DESC, false, false),
				new StringArgumentDefinition(CONCEPTCLASS_RESTRICTION_ARG,
						CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true) };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 */
	public void start() throws InvalidPluginArgumentException {
		filterOnGraph(graph.getConcepts(), graph.getRelations());
	}

	/**
	 * filters on a subset of a graph rather than the whole
	 * 
	 * @param conceptSubset
	 * @param relationsSubset
	 */
	public void filterOnGraph(Set<ONDEXConcept> conceptSubset,
			Set<ONDEXRelation> relationsSubset)
			throws InvalidPluginArgumentException {

		// get remove tag concepts
		boolean removeTag = false;
		if (args.getUniqueValue(ArgumentNames.REMOVE_TAG_ARG) != null)
			removeTag = (Boolean) args
					.getUniqueValue(ArgumentNames.REMOVE_TAG_ARG);
		fireEventOccurred(new GeneralOutputEvent("Removing tag concepts is "
				+ removeTag, "[Filter - filterOnGraph]"));

		// get concept class restrictions
		Set<ConceptClass> ccFilter = null;
		if (args.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG).length > 0) {
			ccFilter = new HashSet<ConceptClass>();
			for (Object o : args
					.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG)) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc != null) {
					ccFilter.add(cc);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass " + cc.getId(),
							"[Filter - filterOnGraph]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(o
							+ " is not a valid ConceptClass.",
							"[Filter - filterOnGraph]"));
				}

			}
		}

		// collect all concepts to filter
		Set<ONDEXConcept> unconnected = new HashSet<ONDEXConcept>();
		int notRemoved = 0;

		// iterate over all concepts
		for (ONDEXConcept c : conceptSubset) {

			// keep all relations of subset
			Set<ONDEXRelation> relations = BitSetFunctions.copy(graph
					.getRelationsOfConcept(c));
			relations.retainAll(relationsSubset);

			// check size of left over relations
			int size = relations.size();

			// filter only within given concept classes
			if (size == 0
					&& (ccFilter == null || ccFilter.contains(c.getOfType()))) {

				Set<ONDEXConcept> conceptsOfTag = BitSetFunctions.copy(graph
						.getConceptsOfTag(c));
				
				Set<ONDEXRelation> relationsOfTag = BitSetFunctions.copy(graph
						.getRelationsOfTag(c));

				// if concept is actually a tag for some other concept
				if (!removeTag && conceptsOfTag.size() > 0) {
					// prevent self-tags
					if (conceptsOfTag.size() == 1
							&& conceptsOfTag.iterator().next().equals(c))
						unconnected.add(c);
					else
						notRemoved++;
				} else {
					unconnected.add(c);
				}

				if (removeTag) {
					// TODO: Why is this necessary?
					for (ONDEXConcept tag : c.getTags().toArray(
							new ONDEXConcept[0])) {
						c.removeTag(tag);
					}

					// remove concept from all other concept tag lists
					for (ONDEXConcept depConcepts : conceptsOfTag) {
						depConcepts.removeTag(c);
					}
					
					
					// remove concept from all other relation tag lists
					for (ONDEXRelation depRelation : relationsOfTag) {
						depRelation.removeTag(c);
					}
				}
			}
		}

		if (!removeTag)
			fireEventOccurred(new GeneralOutputEvent("Not removed "
					+ notRemoved + " concepts because they are used as tag.",
					"[Filter - filterOnGraph]"));

		// translate results
		concepts = BitSetFunctions.copy(conceptSubset);
		concepts.removeAll(unconnected);
		relations = relationsSubset;
	}

	/**
	 * An indexed graph is not required.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
