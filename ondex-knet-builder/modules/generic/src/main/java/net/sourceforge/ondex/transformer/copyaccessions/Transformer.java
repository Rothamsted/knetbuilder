package net.sourceforge.ondex.transformer.copyaccessions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.base.util.BitSetFunctions;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;
import net.sourceforge.ondex.workflow.transformer.ONDEXTransformer;

/**
 * This Transformer copy accessions on concept of a given conceptClass to all
 * connected concepts (given the specified relation and target ConceptClass).
 * 
 * @author hindlem adapted from copycontext by taubertj
 * @version 31.01.2008
 */
@Authors(authors = { "Jan Taubert", "Matthew Hindle" }, emails = {
		"jantaubert at users.sourceforge.net",
		"matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	/**
	 * Starts processing of data.
	 */
	public void start() throws InvalidPluginArgumentException {

		String[] validRelationTypes = (String[]) args
				.getObjectValueArray(RELATION_TYPE_SET_ARG);

		String[] ccs = (String[]) args
				.getObjectValueArray(CONCEPTCLASS_RESTRICTION_ARG);

		String[] cvs = (String[]) args.getObjectValueArray(DATASOURCE_RESTRICTION_ARG);

		// add CC restriction pairs
		HashMap<ConceptClass, ConceptClass> ccMapping = new HashMap<ConceptClass, ConceptClass>();
		if (ccs != null && ccs.length > 0) {
			for (String cc : ccs) {
				String pair = cc.trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new WrongParameterEvent(
											"Invalid Format for ConceptClass pair "
													+ pair,
											"[Transformer - setONDEXGraph]"));
				}
				ConceptClass fromConceptClass = graph.getMetaData()
						.getConceptClass(values[0]);
				ConceptClass toConceptClass = graph.getMetaData()
						.getConceptClass(values[1]);

				if (fromConceptClass != null && toConceptClass != null) {
					ccMapping.put(fromConceptClass, toConceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass restriction for "
									+ fromConceptClass.getId() + " ==> "
									+ toConceptClass.getId(),
							"[Transformer - setONDEXGraph]"));
				} else {
					if (fromConceptClass == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[0]
														+ " is not a valid from ConceptClass.",
												"[Transformer - setONDEXGraph]"));
					if (toConceptClass == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(
												values[1]
														+ " is not a valid to ConceptClass.",
												"[Transformer - setONDEXGraph]"));
				}
			}
		}

		// add DataSource restriction pairs
		HashMap<DataSource, DataSource> dataSourceMapping = new HashMap<DataSource, DataSource>();
		if (cvs != null && cvs.length > 0) {
			for (String cv : cvs) {
				String pair = cv.trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new WrongParameterEvent(
											"Invalid Format for DataSource pair "
													+ pair,
											"[Transformer - setONDEXGraph]"));
				}
				DataSource fromDataSource = graph.getMetaData().getDataSource(values[0]);
				DataSource toDataSource = graph.getMetaData().getDataSource(values[1]);

				if (fromDataSource != null && toDataSource != null) {
					dataSourceMapping.put(fromDataSource, toDataSource);
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new GeneralOutputEvent(
											"Added DataSource restriction for "
													+ fromDataSource.getId() + " ==> "
													+ toDataSource.getId(),
											"[Transformer - setONDEXGraph]"));
				} else {
					if (fromDataSource == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(values[0]
												+ " is not a valid from DataSource.",
												"[Transformer - setONDEXGraph]"));
					if (toDataSource == null)
						ONDEXEventHandler
								.getEventHandlerForSID(graph.getSID())
								.fireEventOccurred(
										new WrongParameterEvent(values[1]
												+ " is not a valid to DataSource.",
												"[Transformer - setONDEXGraph]"));
				}
			}
		}

		// add RTS restrictions
		HashSet<RelationType> relationTypesSets = new HashSet<RelationType>();
		if (validRelationTypes != null && validRelationTypes.length > 0) {
			for (String rts : validRelationTypes) {
				RelationType relationTypeSet = graph.getMetaData()
						.getRelationType(rts.trim());
				if (relationTypeSet != null) {
					relationTypesSets.add(relationTypeSet);
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new GeneralOutputEvent(
											"Added Relation Type Set restriction for "
													+ rts,
											"[Transformer - setONDEXGraph]"));
				} else {
					ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
							.fireEventOccurred(
									new WrongParameterEvent(rts
											+ " is not a valid RelationType ",
											"[Transformer - setONDEXGraph]"));
				}
			}
		} else {
			ONDEXEventHandler
					.getEventHandlerForSID(graph.getSID())
					.fireEventOccurred(
							new GeneralOutputEvent(
									"!Warning No Relation Type Sets specified in context copier, this will copy context of all relations.",
									"[Transformer - setONDEXGraph]"));
		}

		// check for reverse copying in direction 'to'->'from'
		boolean reverse = (Boolean) args.getUniqueValue(REVERSE_ARG);

		// all valid relations
		Set<ONDEXRelation> relations = null;

		if (relationTypesSets.size() == 0) {
			relations = BitSetFunctions.copy(graph.getRelations());
		} else {

			// add all relations of the valid relation type sets
			for (RelationType relationTypesSet : relationTypesSets) {
				if (relations == null) {
					relations = BitSetFunctions.copy(graph
							.getRelationsOfRelationType(relationTypesSet));
				} else {
					Set<ONDEXRelation> additionalRelations = BitSetFunctions
							.copy(graph
									.getRelationsOfRelationType(relationTypesSet));
					relations.retainAll(additionalRelations);
				}
			}

			if (relations == null || relations.size() == 0) {
				ONDEXEventHandler
						.getEventHandlerForSID(graph.getSID())
						.fireEventOccurred(
								new GeneralOutputEvent(
										"No valid relations in scope of specified relations and/or relation type sets.",
										"[Transformer - setONDEXGraph]"));
				return;
			}
		}

		// process all found relations
		int counter = 0;
		for (ONDEXRelation r : relations) {
			counter++;
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			// check cv conditions
			if (dataSourceMapping.size() > 0) {
				DataSource fromDataSource = from.getElementOf();
				DataSource toDataSource = to.getElementOf();
				if (!toDataSource.equals(dataSourceMapping.get(fromDataSource)))
					continue;
			}
			// check cc conditions
			if (ccMapping.size() > 0) {
				ConceptClass fromCC = from.getOfType();
				ConceptClass toCC = to.getOfType();
				if (!toCC.equals(ccMapping.get(fromCC)))
					continue;
			}

			if (reverse) {
				ONDEXConcept temp = from;
				from = to;
				to = temp;
			}
			for (ConceptAccession accession : from.getConceptAccessions()) {
				Set<ConceptAccession> targetAccessions = to
						.getConceptAccessions();
				if (!targetAccessions.contains(accession)) {
					to.createConceptAccession(accession.getAccession(),
							accession.getElementOf(), accession.isAmbiguous());
				}

			}
		}
		ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
				.fireEventOccurred(
						new GeneralOutputEvent("Copied " + counter
								+ " context(s)",
								"[Transformer - setONDEXGraph]"));
	}

	/**
	 * Returns name of this transformer.
	 * 
	 * @return String
	 */
	public String getName() {
		return "Copy accessions";
	}

	/**
	 * Returns version of this transformer.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "31.01.08";
	}

	@Override
	public String getId() {
		return "copyaccessions";
	}

	/**
	 * Returns arguments required by this transformer.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(
						ArgumentNames.RELATION_TYPE_SET_ARG,
						ArgumentNames.RELATION_TYPE_SET_ARG_DESC, false, null,
						true),
				new StringMappingPairArgumentDefinition(
						ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG,
						ArgumentNames.CONCEPTCLASS_RESTRICTION_ARG_DESC, false,
						null, true),
				new StringMappingPairArgumentDefinition(
						ArgumentNames.DATASOURCE_RESTRICTION_ARG,
						ArgumentNames.DATASOURCE_RESTRICTION_ARG_DESC, false, null,
						true),
				new BooleanArgumentDefinition(ArgumentNames.REVERSE_ARG,
						ArgumentNames.REVERSE_ARG_DESC, false, false) };
	}

	/**
	 * Does not require index ondex graph.
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
