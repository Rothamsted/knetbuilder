package net.sourceforge.ondex.filter.accessionregex;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * Changes inclusion/exclusion of specified concepts within a ConceptClass from
 * the graph based on a regex matching an accession
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
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
		return concepts;
	}

	@Override
	public Set<ONDEXRelation> getVisibleRelations() {
		return relations;
	}

	public Set<ONDEXConcept> getInVisibleConcepts() {
		return invconcepts;
	}

	public Set<ONDEXRelation> getInVisibleRelations() {
		return invrelations;
	}

	/**
	 * Returns the name of this filter.
	 * 
	 * @return name
	 */
	public String getName() {
		return "Accession Regex Filter";
	}

	/**
	 * Returns the version of this filter.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return "20.04.2009";
	}

	@Override
	public String getId() {
		return "accessionregex";
	}

	/**
	 * Only argument is about concept classes/
	 * 
	 * @return single argument definition
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition cc_arg = new StringArgumentDefinition(
				TARGETCC_ARG, TARGETCC_ARG_DESC, true, null, true);
		StringArgumentDefinition acc_cv_arg = new StringArgumentDefinition(
				ACC_CV_ARG, ACC_CV_ARG_DESC, true, null, true);
		StringArgumentDefinition regex_arg = new StringArgumentDefinition(
				REGEX_ARG, REGEX_ARG_DESC, true, null, true);
		return new ArgumentDefinition<?>[] { cc_arg, acc_cv_arg, regex_arg };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 * 
	 * @throws DataSourceMissingException
	 */
	public void start() throws DataSourceMissingException,
			InvalidPluginArgumentException {

		HashSet<ConceptClass> filterOnCC = new HashSet<ConceptClass>();

		String accCv = args.getUniqueValue(ACC_CV_ARG).toString();
		DataSource dataSource = graph.getMetaData().getDataSource(accCv);
		if (dataSource == null) {
			throw new DataSourceMissingException(accCv
					+ " not found in the metadata this graph");
		}

		String regEx = args.getUniqueValue(REGEX_ARG).toString();
		Pattern regExPattern = Pattern.compile(regEx);

		// get concept classes from arguments
		String[] ccs = (String[]) super.args.getObjectValueArray(TARGETCC_ARG);
		if (ccs != null && ccs.length > 0) {
			for (Object cc : ccs) {
				String id = ((String) cc).trim();
				ConceptClass conceptClass = graph.getMetaData()
						.getConceptClass(id);
				if (conceptClass != null) {
					filterOnCC.add(conceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass " + conceptClass.getId(),
							"[Filter - setONDEXGraph]"));
				} else {
					fireEventOccurred(new WrongParameterEvent(id
							+ " is not a valid ConceptClass.",
							"[Filter - setONDEXGraph]"));
				}
			}
		} else {
			fireEventOccurred(new WrongParameterEvent(
					"No target concept class(es) given.",
					"[Filter - setONDEXGraph]"));
		}

		BitSet bsetConceptToExclude = new BitSet();
		BitSet bsetRelationsToExclude = new BitSet();

		// filter on concept classes
		for (ConceptClass cc : filterOnCC) {
			for (ONDEXConcept concept : graph.getConceptsOfConceptClass(cc)) {
				boolean exclude = true;
				for (ConceptAccession acc : concept.getConceptAccessions()) {
					if (acc.getElementOf().equals(dataSource)) {
						if (regExPattern.matcher(acc.getAccession()).matches()) {
							exclude = false;
						}
					}
				}
				if (exclude) {
					bsetConceptToExclude.set(concept.getId());
					for (ONDEXRelation r : graph.getRelationsOfConcept(concept)) {
						bsetRelationsToExclude.set(r.getId());
					}
				}
			}

			invconcepts = BitSetFunctions.create(graph, ONDEXConcept.class,
					bsetConceptToExclude);
			invrelations = BitSetFunctions.create(graph, ONDEXRelation.class,
					bsetRelationsToExclude);

			concepts = BitSetFunctions.copy(graph.getConcepts());
			concepts.removeAll(invconcepts);
			relations = BitSetFunctions.copy(graph.getRelations());
			relations.removeAll(invrelations);
		}
		invrelations = BitSetFunctions.create(graph, ONDEXRelation.class,
				bsetRelationsToExclude);
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
