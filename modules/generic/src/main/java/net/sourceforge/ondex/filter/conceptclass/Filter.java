package net.sourceforge.ondex.filter.conceptclass;

import java.io.BufferedReader;
import java.io.FileReader;
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
import net.sourceforge.ondex.core.ConceptAccession;
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
 * Removes specified concept classes from the graph.
 * 
 * @author taubertj
 * @version 29.02.2012
 */
@Status(description = "Tested December 2013 (Artem Lysenko) ", status = StatusType.STABLE)
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })

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
		return "ConceptClass Filter";
	}

	/**
	 * Returns the version of this filter.
	 * 
	 * @return version
	 */
	public String getVersion() {
		return "29.02.2012";
	}

	@Override
	public String getId() {
		return "conceptclass";
	}

	/**
	 * Only argument is about concept classes/
	 * 
	 * @return single argument definition
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition cc_arg = new StringArgumentDefinition(
				TARGETCC_ARG, TARGETCC_ARG_DESC, true, null, true);
		BooleanArgumentDefinition excludeFound = new BooleanArgumentDefinition(
				EXCLUDE_ARG, EXCLUDE_ARG_DESC, false, Boolean.TRUE);
		StringArgumentDefinition cv_arg = new StringArgumentDefinition(
				FILTER_CV_ARG, FILTER_CV_ARG_DESC, false, null, false);
		StringArgumentDefinition file_arg = new StringArgumentDefinition(
				ACC_FILE_ARG, ACC_FILE_ARG_DESC, false, null, false);
		return new ArgumentDefinition<?>[] { cc_arg, cv_arg, file_arg,
				excludeFound };
	}

	/**
	 * Filters the graph and constructs the lists for visible concepts and
	 * relations.
	 */
	public void start() throws InvalidPluginArgumentException {

		// get data source option
		DataSource dataSource = null;
		String temp = (String) args.getUniqueValue(FILTER_CV_ARG);
		if (temp != null) {
			dataSource = graph.getMetaData().getDataSource(temp);
			if (dataSource == null)
				fireEventOccurred(new WrongParameterEvent("The DataSource "
						+ temp + " does not exist.", getCurrentMethodName()));
		}

		// parse accession restrictions
		Set<String> accRestrictions = new HashSet<String>();
		String file = (String) args.getUniqueValue(ACC_FILE_ARG);
		if (dataSource != null && file != null) {
			try {
				// read accession file
				BufferedReader br = new BufferedReader(new FileReader(file));
				String strLine = "";
				while ((strLine = br.readLine()) != null) {
					accRestrictions.add(strLine.trim().toUpperCase());
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// get concept classes from arguments
		Set<ConceptClass> filterOnCC = new HashSet<ConceptClass>();
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
							getCurrentMethodName()));
				} else {
					fireEventOccurred(new WrongParameterEvent(id
							+ " is not a valid ConceptClass.",
							getCurrentMethodName()));
				}
			}
		} else {
			fireEventOccurred(new WrongParameterEvent(
					"No target concept class(es) given.",
					getCurrentMethodName()));
		}

		// make a copy of current concepts and relations
		concepts = BitSetFunctions.copy(graph.getConcepts());
		relations = BitSetFunctions.copy(graph.getRelations());

		// filter on concept classes
		for (ConceptClass cc : filterOnCC) {
			Set<ONDEXConcept> selectedC;
			Set<ONDEXRelation> selectedR;

			if (dataSource != null && accRestrictions.size() > 0) {
				selectedC = new HashSet<ONDEXConcept>();
				selectedR = new HashSet<ONDEXRelation>();
				for (ONDEXConcept c : graph.getConceptsOfConceptClass(cc)) {
					for (ConceptAccession ca : c.getConceptAccessions()) {
						// only include concepts which carry accessions
						// specified and of specified data source
						if (ca.getElementOf().equals(dataSource)
								&& accRestrictions.contains(ca.getAccession()
										.trim().toUpperCase())) {
							selectedC.add(c);
							selectedR.addAll(graph.getRelationsOfConcept(c));
							break;
						}
					}
				}
			}

			else {
				// just get concepts and relations for concept class
				selectedC = BitSetFunctions.copy(graph
						.getConceptsOfConceptClass(cc));
				selectedR = BitSetFunctions.copy(graph
						.getRelationsOfConceptClass(cc));
				// restrict by data source
				if (dataSource != null) {
					selectedC.retainAll(graph
							.getConceptsOfDataSource(dataSource));
					selectedR.retainAll(graph
							.getRelationsOfDataSource(dataSource));
				}
			}

			// build the complement, by removing found concepts and relations
			concepts.removeAll(selectedC);
			relations.removeAll(selectedR);

			// track what concepts and relations has been removed
			if (invconcepts == null && invrelations == null) {
				invconcepts = selectedC;
				invrelations = selectedR;
			} else {
				invconcepts.addAll(selectedC);
				invrelations.addAll(selectedR);
			}
		}

		// inverse the cc to exclusive inclusion if EXCLUDE_ARG is false
		if (!(Boolean) args.getUniqueValue(EXCLUDE_ARG)) {
			Set<ONDEXConcept> allConcept = BitSetFunctions.copy(graph
					.getConcepts());
			allConcept.removeAll(concepts);
			concepts = allConcept;
			Set<ONDEXRelation> allRelations = BitSetFunctions.copy(graph
					.getRelations());
			allRelations.removeAll(relations);
			relations = allRelations;
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

	public String[] requiresValidators() {
		return new String[0];
	}
}
