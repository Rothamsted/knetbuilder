package net.sourceforge.ondex.transformer.copyattribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * This Transformer copy Attribute on concept of a given conceptClass to all
 * connected concepts (given the specified relation and target ConceptClass).
 * 
 * @author taubertj adapted from copyaccessions
 * @version 09.12.2010
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

		String[] cvs = (String[]) args
				.getObjectValueArray(DATASOURCE_RESTRICTION_ARG);

		// add CC restriction pairs
		Map<ConceptClass, Set<ConceptClass>> ccMapping = new HashMap<ConceptClass, Set<ConceptClass>>();
		if (ccs != null && ccs.length > 0) {
			for (String cc : ccs) {
				String pair = cc.trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					fireEventOccurred(new WrongParameterEvent(
							"Invalid Format for ConceptClass pair " + pair,
							getCurrentMethodName()));
				}
				ConceptClass fromConceptClass = graph.getMetaData()
						.getConceptClass(values[0]);
				ConceptClass toConceptClass = graph.getMetaData()
						.getConceptClass(values[1]);

				if (fromConceptClass != null && toConceptClass != null) {
					if (!ccMapping.containsKey(fromConceptClass))
						ccMapping.put(fromConceptClass,
								new HashSet<ConceptClass>());
					// multiple mappings to different concept classes possible
					ccMapping.get(fromConceptClass).add(toConceptClass);
					fireEventOccurred(new GeneralOutputEvent(
							"Added ConceptClass restriction for "
									+ fromConceptClass.getId() + " ==> "
									+ toConceptClass.getId(),
							getCurrentMethodName()));
				} else {
					if (fromConceptClass == null)
						fireEventOccurred(new WrongParameterEvent(values[0]
								+ " is not a valid from ConceptClass.",
								getCurrentMethodName()));
					if (toConceptClass == null)
						fireEventOccurred(new WrongParameterEvent(values[1]
								+ " is not a valid to ConceptClass.",
								getCurrentMethodName()));
				}
			}
		}

		// add DataSource restriction pairs
		Map<DataSource, Set<DataSource>> dataSourceMapping = new HashMap<DataSource, Set<DataSource>>();
		if (cvs != null && cvs.length > 0) {
			for (String cv : cvs) {
				String pair = cv.trim();
				String[] values = pair.split(",");

				if (values.length != 2) {
					fireEventOccurred(new WrongParameterEvent(
							"Invalid Format for DataSource pair " + pair,
							getCurrentMethodName()));
				}
				DataSource fromDataSource = graph.getMetaData().getDataSource(
						values[0]);
				DataSource toDataSource = graph.getMetaData().getDataSource(
						values[1]);

				if (fromDataSource != null && toDataSource != null) {
					if (!dataSourceMapping.containsKey(fromDataSource))
						dataSourceMapping.put(fromDataSource,
								new HashSet<DataSource>());
					// multiple mappings to different data sources possible
					dataSourceMapping.get(fromDataSource).add(toDataSource);
					fireEventOccurred(new GeneralOutputEvent(
							"Added DataSource restriction for "
									+ fromDataSource.getId() + " ==> "
									+ toDataSource.getId(),
							getCurrentMethodName()));
				} else {
					if (fromDataSource == null)
						fireEventOccurred(new WrongParameterEvent(values[0]
								+ " is not a valid from DataSource.",
								getCurrentMethodName()));
					if (toDataSource == null)
						fireEventOccurred(new WrongParameterEvent(values[1]
								+ " is not a valid to DataSource.",
								getCurrentMethodName()));
				}
			}
		}

		// add relation type restrictions
		Set<RelationType> relationTypes = new HashSet<RelationType>();
		if (validRelationTypes != null && validRelationTypes.length > 0) {
			for (String rts : validRelationTypes) {
				RelationType relationType = graph.getMetaData()
						.getRelationType(rts.trim());
				if (relationType != null) {
					relationTypes.add(relationType);
					fireEventOccurred(new GeneralOutputEvent(
							"Added RelationType restriction for " + rts,
							getCurrentMethodName()));
				} else {
					fireEventOccurred(new WrongParameterEvent(rts
							+ " is not a valid RelationType.",
							getCurrentMethodName()));
				}
			}
		} else {
			fireEventOccurred(new GeneralOutputEvent(
					"!Warning No RelationTypes specified in Attribute copier, this will copy Attribute across all relations.",
					getCurrentMethodName()));
		}

		// check for reverse copying in direction 'to'->'from'
		boolean reverse = (Boolean) args.getUniqueValue(REVERSE_ARG);

		// all valid relations
		Set<ONDEXRelation> relations = null;

		if (relationTypes.size() == 0) {
			relations = BitSetFunctions.copy(graph.getRelations());
		} else {

			// add all relations of the valid relation type sets
			for (RelationType relationType : relationTypes) {
				if (relations == null) {
					relations = BitSetFunctions.copy(graph
							.getRelationsOfRelationType(relationType));
				} else {
					// add relations of another relation type
					relations.addAll(graph
							.getRelationsOfRelationType(relationType));
				}
			}

			if (relations == null || relations.size() == 0) {
				fireEventOccurred(new GeneralOutputEvent(
						"No valid relations in scope of specified relation types.",
						getCurrentMethodName()));
				return;
			}
		}

		// process all found relations
		int relationCounter = 0;
		int attributeCounter = 0;
		for (ONDEXRelation r : relations) {
			relationCounter++;
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			// check data source conditions
			if (dataSourceMapping.size() > 0) {
				DataSource fromDataSource = from.getElementOf();
				DataSource toDataSource = to.getElementOf();
				if (!dataSourceMapping.containsKey(fromDataSource)
						|| !dataSourceMapping.get(fromDataSource).contains(
								toDataSource))
					continue;
			}
			// check concept class conditions
			if (ccMapping.size() > 0) {
				ConceptClass fromCC = from.getOfType();
				ConceptClass toCC = to.getOfType();
				if (!ccMapping.containsKey(fromCC)
						|| !ccMapping.get(fromCC).contains(toCC))
					continue;
			}

			// reverse copy direction
			if (reverse) {
				ONDEXConcept temp = from;
				from = to;
				to = temp;
			}
			for (Attribute attribute : from.getAttributes()) {
				Set<Attribute> targetAttributes = to.getAttributes();
				if (!targetAttributes.contains(attribute)) {
					to.createAttribute(attribute.getOfType(),
							attribute.getValue(), attribute.isDoIndex());
					attributeCounter++;
				}
			}
		}
		fireEventOccurred(new GeneralOutputEvent("Copied " + attributeCounter
				+ " attributes across " + relationCounter + " relations.",
				getCurrentMethodName()));
	}

	/**
	 * Returns name of this transformer.
	 * 
	 * @return String
	 */
	public String getName() {
		return "Copy Attribute";
	}

	/**
	 * Returns version of this transformer.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "11.05.2012";
	}

	@Override
	public String getId() {
		return "copyattribute";
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
						ArgumentNames.DATASOURCE_RESTRICTION_ARG_DESC, false,
						null, true),
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
