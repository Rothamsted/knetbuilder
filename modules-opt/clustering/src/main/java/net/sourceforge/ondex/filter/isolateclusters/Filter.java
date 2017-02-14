package net.sourceforge.ondex.filter.isolateclusters;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Filter isolates clusters containing concepts satisfying the given
 * requirements.
 * 
 * @author taubertj
 * @version 17.03.2008
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Filter extends ONDEXFilter implements ArgumentNames {

	// contains list of visible concepts
	private Set<ONDEXConcept> concepts = null;

	// contains list of visible relations
	private Set<ONDEXRelation> relations = null;

	/**
	 * Constructor sets session context.
	 */
	public Filter() {
		super();
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

	/**
	 * Returns the name of this filter.
	 * 
	 * @return name
	 */
	public String getName() {
		return "IsolateClusters Filter";
	}

	/**
	 * Returns the version of this filter.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return "17.06.2010";
	}

	@Override
	public String getId() {
		return "isolateclusters";
	}

	/**
	 * Only argument is about concept classes.
	 * 
	 * @return single argument definition
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition cc_arg = new StringArgumentDefinition(
				TARGETCC_ARG, TARGETCC_ARG_DESC, false, null, true);
		StringArgumentDefinition cv_arg = new StringArgumentDefinition(
				TARGET_DATASOURCE_ARG, TARGET_DATASOURCE_ARG_DESC, false, null, true);
		return new ArgumentDefinition<?>[] { cc_arg, cv_arg };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 */
	public void start() throws InvalidPluginArgumentException {

		Set<ConceptClass> filterOnCC = new HashSet<ConceptClass>();
		Set<DataSource> filterOnDataSource = new HashSet<DataSource>();

		// get concept classes from arguments
		String[] ccs = (String[]) super.args.getObjectValueArray(TARGETCC_ARG);
		if (ccs != null && ccs.length > 0) {
			for (String cc : ccs) {
				String id = cc.trim();
				ConceptClass conceptClass = graph.getMetaData()
						.getConceptClass(id);
				if (conceptClass != null) {
					filterOnCC.add(conceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass " + conceptClass.getId(),
							"[Filter - start]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(id
							+ " is not a valid ConceptClass.",
							"[Filter - start]"));
				}
			}
		}

		// get cvs from arguments
		String[] cvs = (String[]) super.args.getObjectValueArray(TARGET_DATASOURCE_ARG);
		if (cvs != null && cvs.length > 0) {
			for (String cv : cvs) {
				String id = cv.trim();
				DataSource dataSource = graph.getMetaData().getDataSource(id);
				if (dataSource != null) {
					filterOnDataSource.add(dataSource);
					fireEventOccurred(new GeneralOutputEvent(
							"Added DataSource " + dataSource.getId(),
							"[Filter - start]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(id
							+ " is not a valid DataSource.", "[Filter - start]"));
				}
			}
		}

		// one of the two arguments have to be set at least
		if (filterOnCC.size() == 0 && filterOnDataSource.size() == 0) {
			fireEventOccurred(new WrongParameterEvent(
					"No target concept class(es) or data source(s) given.",
					"[Filter - start]"));
			return;
		}

		// construct seed set for concept classes
		for (ConceptClass cc : filterOnCC) {
			if (concepts == null) {
				concepts = BitSetFunctions.copy(graph
						.getConceptsOfConceptClass(cc));
			} else {
				concepts.addAll(graph.getConceptsOfConceptClass(cc));
			}
		}

		// construct seed set for data sources
		for (DataSource dataSource : filterOnDataSource) {
			if (concepts == null) {
				concepts = BitSetFunctions.copy(graph.getConceptsOfDataSource(dataSource));
			} else {
				concepts.addAll(graph.getConceptsOfDataSource(dataSource));
			}
		}

		// contains connected concepts
		Set<Integer> connected = new HashSet<Integer>();
		for (ONDEXConcept root : concepts) {
			visited = new HashSet<Integer>();
			connected.addAll(traverseGraph(root));
		}

		// merge seed concepts with connected ones
		concepts.addAll(BitSetFunctions.create(graph, ONDEXConcept.class, connected));

		// get inverse of found concepts and remove them
		Set<ONDEXConcept> all = graph.getConcepts();
		Set<ONDEXConcept> remove = BitSetFunctions.andNot(all, concepts);
		for (ONDEXConcept c : remove) {
			graph.deleteConcept(c.getId());
		}

		relations = graph.getRelations();
	}

	/**
	 * An indexed graph is not required.
	 * 
	 * @return false
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	// set of visited concepts
	private Set<Integer> visited = new HashSet<Integer>();

	/**
	 * Traverse graph to find connected concepts.
	 * 
	 * @param root
	 *            root concept to start at
	 * @return Set<Integer>
	 */
	private Set<Integer> traverseGraph(ONDEXConcept root) {

		// iterate over all relations of root
		visited.add(root.getId());
		for (ONDEXRelation r : graph.getRelationsOfConcept(root)) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			if (!from.equals(to)) {
				if (from.equals(root) && !visited.contains(to.getId()))
					traverseGraph(to);
				else if (to.equals(root) && !visited.contains(from.getId()))
					traverseGraph(from);
			}
		}

		return visited;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
