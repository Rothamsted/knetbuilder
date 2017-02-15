package net.sourceforge.ondex.transformer.relationdatasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Tool to copy datasource (DataSource) info from a concept to a relation gds
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames,
		MetaData {

	@Override
	public String getId() {
		return "create_relation_data_source";
	}

	@Override
	public String getName() {
		return "copy data source to relation gds";
	}

	@Override
	public String getVersion() {
		return "alpha";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] {
				new StringArgumentDefinition(RELATION_TYPE_ARG,
						RELATION_TYPE_ARG_DESC, false, null, true),
				new StringArgumentDefinition(CONCEPT_CLASS_ARG,
						CONCEPT_CLASS_ARG_DESC, false, null, true), };
	}

	@Override
	public void start() throws Exception {

		AttributeName att = graph.getMetaData()
				.getAttributeName(ATT_DATASOURCE);
		if (att == null) {
			att = graph.getMetaData().getFactory().createAttributeName(
					ATT_DATASOURCE,
					"Datasource where this ONDEXEntity originated",
					String.class);
		}

		String[] relationTypeIds = (String[]) args
				.getObjectValueArray(RELATION_TYPE_ARG);
		String[] conceptClassIds = (String[]) args
				.getObjectValueArray(CONCEPT_CLASS_ARG);

		Set<RelationType> relationTypes = new HashSet<RelationType>(
				relationTypeIds.length);
		Set<ConceptClass> conceptClasses = new HashSet<ConceptClass>(
				conceptClassIds.length);

		if (relationTypeIds.length > 0) {
			for (String relationTypeId : relationTypeIds) {
				RelationType relationType = graph.getMetaData()
						.getRelationType(relationTypeId);
				if (relationType != null)
					relationTypes.add(relationType);
				else
					System.err
							.println(relationTypeId
									+ " was not found as a relation type in the metadata");
			}
			if (relationTypes.size() == 0) {
				System.err
						.println("None of the relation types where found in the metadata");
				return;
			}
		}

		if (conceptClassIds.length > 0) {
			for (String conceptClassId : conceptClassIds) {
				ConceptClass conceptClass = graph.getMetaData()
						.getConceptClass(conceptClassId);
				if (conceptClass != null)
					conceptClasses.add(conceptClass);
				else
					System.err
							.println(conceptClassId
									+ " was not found as a concept class in the metadata");
			}
			if (conceptClasses.size() == 0) {
				System.err
						.println("None of the concept classes where found in the metadata");
				return;
			}
		}

		Set<ONDEXRelation> relations = null;

		if (relationTypes.size() == 0) {
			relations = BitSetFunctions.copy(graph.getRelations());
		} else {
			for (RelationType relationType : relationTypes) {
				if (relations == null) {
					relations = BitSetFunctions.copy(graph
							.getRelationsOfRelationType(relationType));
				} else {
					relations.addAll(graph
							.getRelationsOfRelationType(relationType));
				}
			}
		}

		int copied = 0;

		for (ONDEXRelation relation : relations) {
			ONDEXConcept from = relation.getFromConcept();
			if (conceptClasses.size() == 0
					|| conceptClasses.contains(from.getOfType())) {
				copyCVToRelation(from, relation, att);
				copied++;
			}
			ONDEXConcept to = relation.getToConcept();
			if (conceptClasses.size() == 0
					|| conceptClasses.contains(to.getOfType())) {
				copyCVToRelation(to, relation, att);
				copied++;
			}
		}

	}

	private void copyCVToRelation(ONDEXConcept to, ONDEXRelation relation,
			AttributeName att) {
		DataSource dataSource = to.getElementOf();

		String[] typeIds = dataSource.getId().split(":");
		for (String typeId : typeIds) {
			Attribute attribute = relation.getAttribute(att);
			if (attribute == null) {
				relation.createAttribute(att, typeId, false);
			} else if (attribute.getValue() instanceof Collection) {
				Collection collection = (Collection) attribute.getValue();
				if (!collection.contains(typeId)) {
					collection.add(typeId);
					attribute.setValue(collection);
				}
			} else if (!attribute.getValue().equals(typeId)) {
				List<String> cvs = new ArrayList<String>(2);
				cvs.add(typeId);
				cvs.add((String) attribute.getValue());
				attribute.setValue(cvs);
			}
		}
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}
}
