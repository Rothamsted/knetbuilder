package net.sourceforge.ondex.filter.relationtype;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Removes specified relation type set from the graph.
 * 
 * @author taubertj
 * @version 01.02.2008
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	// contains list of visible concepts
	private Set<ONDEXConcept> concepts = null;

	// contains list of visible relations
	private Set<ONDEXRelation> relations = null;

	private Set<ONDEXConcept> inverseConcepts = null;

	private Set<ONDEXRelation> inverseRelations = null;

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

	public Set<ONDEXConcept> getInVisibleConcepts() {
		return inverseConcepts;
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return relations;
	}

	public Set<ONDEXRelation> getInVisibleRelations() {
		return inverseRelations;
	}

	/**
	 * Returns the name of this filter.
	 * 
	 * @return name
	 */
	public String getName() {
		return "RelationType Filter";
	}

	/**
	 * Returns the version of this filter.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return "21.04.2008";
	}

	@Override
	public String getId() {
		return "relationtype";
	}

	/**
	 * ArgumentDefinitions for RelationType, ConceptClass and DataSource restrictions.
	 * 
	 * @return three argument definition
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition rtset_arg = new StringArgumentDefinition(
				TARGETRTSET_ARG, TARGETRTSET_ARG_DESC, true, null, true);
		StringMappingPairArgumentDefinition ccRestriction = new StringMappingPairArgumentDefinition(
				CONCEPTCLASS_RESTRICTION_ARG,
				CONCEPTCLASS_RESTRICTION_ARG_DESC, false, null, true);
		StringMappingPairArgumentDefinition cvRestriction = new StringMappingPairArgumentDefinition(
				DATASOURCE_RESTRICTION_ARG, DATASOURCE_RESTRICTION_ARG_DESC, false, null, true);
		
		return new ArgumentDefinition<?>[] { rtset_arg, ccRestriction,
				cvRestriction };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 */
	public void start() throws InvalidPluginArgumentException {

		// get restrictions on ConceptClasses or CVs on relations
		Map<DataSource, HashSet<DataSource>> dataSourceMapping = getAllowedDataSources(graph);
		Map<ConceptClass, HashSet<ConceptClass>> ccMapping = getAllowedCCs(graph);
		
		HashSet<RelationType> filterOnRtSet = new HashSet<RelationType>();

		// get rtsets from arguments
		Object[] rtsets = super.args.getObjectValueArray(TARGETRTSET_ARG);
		if (rtsets != null && rtsets.length > 0) {
			for (Object rtset : rtsets) {
				String id = ((String) rtset).trim();
				RelationType relationTypeSet = graph.getMetaData()
						.getRelationType(id);
				if (relationTypeSet != null) {
					filterOnRtSet.add(relationTypeSet);
					fireEventOccurred(new GeneralOutputEvent(
							"Added RelationType " + relationTypeSet.getId(),
							"[Filter - setONDEXGraph]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(id
							+ " is not a valid RelationType.",
							"[Filter - setONDEXGraph]"));
				}
			}
		} else {
			fireEventOccurred(new WrongParameterEvent(
					"No target RelationType(s) given.",
					"[Filter - setONDEXGraph]"));
		}

		// concepts dont get modified
		concepts = graph.getConcepts();
		inverseConcepts = BitSetFunctions.create(graph, ONDEXConcept.class,
				new BitSet());

		// capture modified relations
		relations = BitSetFunctions.copy(graph.getRelations());
		inverseRelations = BitSetFunctions.create(graph,
				ONDEXRelation.class, new BitSet());

		// filter on relation types
		for (RelationType rtset : filterOnRtSet) {
			if (dataSourceMapping.size() == 0 && ccMapping.size() == 0) {
				inverseRelations
						.addAll(graph.getRelationsOfRelationType(rtset));
			} else {
				Set<Integer> filter = new HashSet<Integer>();
				for (ONDEXRelation r : graph.getRelationsOfRelationType(rtset)) {
					ONDEXConcept fromConcept = r.getFromConcept();
					ONDEXConcept toConcept = r.getToConcept();

					// check DataSource conditions
					DataSource fromDataSource = fromConcept.getElementOf();
					DataSource toDataSource = toConcept.getElementOf();
					if (dataSourceMapping.size() > 0
							&& !dataSourceMapping.get(fromDataSource).contains(toDataSource)) {
						continue;
					}

					// check ConceptClass conditions
					ConceptClass fromCC = fromConcept.getOfType();
					ConceptClass toCC = toConcept.getOfType();
					if (ccMapping.size() > 0
							&& (!ccMapping.containsKey(fromCC) || 
							!ccMapping.get(fromCC).contains(toCC))) {
						continue;
					}

					filter.add(r.getId());
				}
				inverseRelations.addAll(BitSetFunctions.create(graph,
						ONDEXRelation.class, filter));
			}
		}

		// remove all invisible relations
		relations.removeAll(inverseRelations);
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
