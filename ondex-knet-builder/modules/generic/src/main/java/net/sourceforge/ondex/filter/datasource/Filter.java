package net.sourceforge.ondex.filter.datasource;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Extracts from a graph all concepts and relations (internaly connecting) of
 * the given cv/s.
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	private Set<ONDEXConcept> concepts;
	private Set<ONDEXRelation> relations;

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

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition datasourceArg = new StringArgumentDefinition(
				DATASOURCE_ARG, DATASOURCE_ARG_DESC, true, null, true);
		BooleanArgumentDefinition excludeFound = new BooleanArgumentDefinition(
				EXCLUDE_ARG, EXCLUDE_ARG_DESC, true, Boolean.TRUE);

		return new ArgumentDefinition<?>[] { datasourceArg, excludeFound };
	}

	@Override
	public String getName() {
		return "Concept DataSource restrictions";
	}

	@Override
	public String getVersion() {
		return "01.11.2011";
	}

	@Override
	public String getId() {
		return "datasource";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public void start() throws InvalidPluginArgumentException {

		concepts = new HashSet<ONDEXConcept>();
		relations = new HashSet<ONDEXRelation>();

		for (String dsname : (String[]) args
				.getObjectValueArray(DATASOURCE_ARG)) {
			DataSource dataSource = graph.getMetaData().getDataSource(dsname);
			if (dataSource == null) {
				fireEventOccurred(new DataSourceMissingEvent(dataSource
						+ " not found: ignoring", getCurrentMethodName()));
				continue;
			}
			concepts.addAll(graph.getConceptsOfDataSource(dataSource));
			relations.addAll(graph.getRelationsOfDataSource(dataSource));
		}

		// clear up any externally linked relations
		Set<ONDEXRelation> connectedRelations = new HashSet<ONDEXRelation>();
		for (ONDEXRelation relation : relations) {
			if (concepts.contains(relation.getFromConcept())
					&& concepts.contains(relation.getToConcept())) {
				connectedRelations.add(relation);
			}
		}
		relations = connectedRelations;

		// inverse the to data source exclusion if EXCLUDE_ARG is true
		if ((Boolean) args.getUniqueValue(EXCLUDE_ARG)) {
			Set<ONDEXConcept> allConcept = new HashSet<ONDEXConcept>(
					graph.getConcepts());
			allConcept.removeAll(concepts);
			concepts = allConcept;
			Set<ONDEXRelation> allRelations = new HashSet<ONDEXRelation>(
					graph.getRelations());
			allRelations.removeAll(relations);
			relations = allRelations;
		}

	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}
}
