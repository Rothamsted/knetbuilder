package net.sourceforge.ondex.filter.evidencetype;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Filters concepts and/or relations according to a given EvidenceType.
 * 
 * @author taubertj
 */
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	private Set<ONDEXConcept> concepts;

	private Set<ONDEXRelation> relations;

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
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition et_arg = new StringArgumentDefinition(ET_ARG,
				ET_ARG_DESC, true, null, true);
		BooleanArgumentDefinition excludeFound = new BooleanArgumentDefinition(
				EXCLUDE_ARG, EXCLUDE_ARG_DESC, false, Boolean.FALSE);
		BooleanArgumentDefinition onConcepts = new BooleanArgumentDefinition(
				CONCEPTS_ARG, CONCEPTS_ARG_DESC, true, Boolean.TRUE);

		return new ArgumentDefinition<?>[] { et_arg, excludeFound, onConcepts };
	}

	@Override
	public String getName() {
		return "EvidenceType restrictions";
	}

	@Override
	public String getVersion() {
		return "15.06.2011";
	}

	@Override
	public String getId() {
		return "evidencetype";
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

		boolean onConcepts = (Boolean) args.getUniqueValue(CONCEPTS_ARG);

		// start with empty sets
		concepts = new HashSet<ONDEXConcept>();
		relations = new HashSet<ONDEXRelation>();

		if (onConcepts) {

			// add all concepts of evidence types
			for (Object etname : args.getObjectValueArray(ET_ARG)) {
				EvidenceType et = graph.getMetaData().getEvidenceType(
						etname.toString());
				if (et == null) {
					fireEventOccurred(new EvidenceTypeMissingEvent(et
							+ " not found: ignoring", getCurrentMethodName()));
					continue;
				}
				concepts.addAll(graph.getConceptsOfEvidenceType(et));
			}

			// clear up any externally linked relations
			for (ONDEXRelation relation : graph.getRelations()) {
				if (concepts.contains(relation.getFromConcept())
						&& concepts.contains(relation.getToConcept())) {
					relations.add(relation);
				}
			}

		} else {
			
			// add all relations of evidence types
			for (Object etname : args.getObjectValueArray(ET_ARG)) {
				EvidenceType et = graph.getMetaData().getEvidenceType(
						etname.toString());
				if (et == null) {
					fireEventOccurred(new EvidenceTypeMissingEvent(et
							+ " not found: ignoring", getCurrentMethodName()));
					continue;
				}
				relations.addAll(graph.getRelationsOfEvidenceType(et));
			}
		}

		// inverse the to evidence type exclusion if EXCLUDE_ARG is true
		if ((Boolean) args.getUniqueValue(EXCLUDE_ARG)) {
			Set<ONDEXConcept> cs = BitSetFunctions.copy(graph.getConcepts());
			cs.removeAll(concepts);
			concepts = cs;
			Set<ONDEXRelation> rs = BitSetFunctions.copy(graph.getRelations());
			rs.removeAll(relations);
			relations = rs;
		}
	}

}
