package net.sourceforge.ondex.mapping.attributeequality;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.AttributeNameMissingEvent;
import net.sourceforge.ondex.event.type.ConceptClassMissingEvent;
import net.sourceforge.ondex.event.type.EvidenceTypeMissingEvent;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.mapping.ONDEXMapping;

/**
 * Creates relation where all specified Attribute are equal
 * 
 * @author hindlem
 */
@Authors(authors = { "Matthew Hindle" }, emails = { "matthew_hindle at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Mapping extends ONDEXMapping implements MetaData, ArgumentNames {

	private EvidenceType eviType;

	private RelationType relationType;

	@Override
	public void start() throws NullValueException, EmptyStringException,
			InvalidPluginArgumentException {

		Boolean mapWithInDataSource = (Boolean) args.getUniqueValue(WITHIN_DATASOURCE_ARG);
		Boolean ignoreCase = (Boolean) args.getUniqueValue(IGNORE_CASE_ARG);

		String replacePatternString = (String) args
				.getUniqueValue(REPLACE_PATTERN_ARG);

		ConceptClass conceptClass = null;
		String conceptClassName = (String) args
				.getUniqueValue(CONCEPT_CLASS_ARG);
		if (conceptClassName != null) {
			conceptClass = graph.getMetaData()
					.getConceptClass(conceptClassName);
			if (conceptClass == null) {
				fireEventOccurred(new ConceptClassMissingEvent("Name:"
						+ conceptClassName, Mapping.getCurrentMethodName()));
			}
		}

		Pattern replacePattern = null;
		if (replacePatternString != null
				&& replacePatternString.trim().length() > 0) {
			replacePattern = Pattern.compile(replacePatternString);
		}

		if (mapWithInDataSource == null)
			mapWithInDataSource = false;

		// get the relationtype and evidencetype for this mapping
		relationType = graph.getMetaData().getRelationType(
				(String) args.getUniqueValue(RELATION_ARG));
		if (relationType == null) {
			relationType = graph
					.getMetaData()
					.getFactory()
					.createRelationType(
							(String) args.getUniqueValue(RELATION_ARG));
		}

		eviType = graph.getMetaData().getEvidenceType(EVI_GDSEQUAL);
		if (eviType == null) {
			fireEventOccurred(new EvidenceTypeMissingEvent(EVI_GDSEQUAL,
					Mapping.getCurrentMethodName()));
			eviType = graph
					.getMetaData()
					.getFactory()
					.createEvidenceType(EVI_GDSEQUAL,
							"A specified Attribute value was equal", getName());
		}

		Set<AttributeName> attNames = new HashSet<AttributeName>();
		Object[] gdssArgs = args.getObjectValueArray(ATTRIBUTE_ARG);
		for (Object gdsArg : gdssArgs) {
			AttributeName att = graph.getMetaData().getAttributeName(
					(String) gdsArg);
			if (att == null) {
				fireEventOccurred(new AttributeNameMissingEvent(
						(String) gdsArg, Mapping.getCurrentMethodName()));
			} else {
				attNames.add(att);
			}
		}

		Set<ONDEXConcept> concepts = null;

		for (AttributeName att : attNames) {
			Set<ONDEXConcept> attConcepts = graph
					.getConceptsOfAttributeName(att);
			if (concepts == null) {
				concepts = new HashSet<ONDEXConcept>(attConcepts);
			} else {
				concepts.addAll(attConcepts);
			}
		}

		if (conceptClass != null) {
			Set<ONDEXConcept> conceptsOfCC = graph
					.getConceptsOfConceptClass(conceptClass);
			concepts.retainAll(conceptsOfCC);
		}

		int found = 0;

		fireEventOccurred(new GeneralOutputEvent("Mapping on "
				+ concepts.size() + " concepts.",
				Mapping.getCurrentMethodName()));

		if (concepts != null && concepts.size() > 0) {
			for (ONDEXConcept concept : concepts) {
				for (ONDEXConcept cloneConcept : concepts) {
					if (cloneConcept.getId() != concept.getId()) {
						if (!evaluateMapping(graph, concept, cloneConcept)) {
							continue;
						}

						boolean matches = true;
						for (AttributeName att : attNames) {

							Attribute attribute = concept.getAttribute(att);
							Attribute cloneAttribute = cloneConcept
									.getAttribute(att);

							Object value1 = attribute.getValue();
							Object value2 = cloneAttribute.getValue();

							if (replacePattern != null
									&& value1 instanceof String
									&& value2 instanceof String) {
								value1 = replacePattern
										.matcher((String) value1)
										.replaceAll("");
								value2 = replacePattern
										.matcher((String) value2)
										.replaceAll("");
								if (ignoreCase != null
										&& ignoreCase
										&& !((String) value1)
												.equalsIgnoreCase((String) value2)) {
									matches = false;
									break;
								}
							} else if (!value1.equals(value2)) {
								matches = false;
								break;
							}
						}

						if (matches) {
							ONDEXRelation relation = graph.getRelation(concept,
									cloneConcept, relationType);
							if (relation == null) {
								relation = graph.getFactory().createRelation(
										concept, cloneConcept, relationType,
										eviType);
								found++;
							} else {
								relation.addEvidenceType(eviType);
							}

						}
					}
				}
			}

		} else {
			fireEventOccurred(new GeneralOutputEvent(
					"No concepts meet conditions",
					Mapping.getCurrentMethodName()));
		}
		fireEventOccurred(new GeneralOutputEvent("Created " + found
				+ " new relations of type " + relationType.getId(),
				Mapping.getCurrentMethodName()));

	}

	public ArgumentDefinition<?>[] getArgumentDefinitions() {

		HashSet<ArgumentDefinition<?>> extendedDefinition = new HashSet<ArgumentDefinition<?>>();

		// Add blast program params
		extendedDefinition.add(new StringArgumentDefinition(RELATION_ARG,
				RELATION_DESC, true, null, false));

		extendedDefinition.add(new StringArgumentDefinition(ATTRIBUTE_ARG,
				ATTRIBUTE_DESC, true, null, true));

		extendedDefinition.add(new StringArgumentDefinition(CONCEPT_CLASS_ARG,
				CONCEPT_CLASS_ARG_DESC, false, null, false));

		extendedDefinition.add(new StringArgumentDefinition(
				ATTRIBUTE_EQUALS_ARG, ATTRIBUTE_EQUALS_ARG_DESC, false, null,
				true));

		extendedDefinition.add(new StringArgumentDefinition(
				REPLACE_PATTERN_ARG, REPLACE_PATTERN_ARG_DESC, false, null,
				false));

		extendedDefinition
				.add(new BooleanArgumentDefinition(WITHIN_DATASOURCE_ARG,
						WITHIN_DATASOURCE_ARG_DESC, false, false));

		extendedDefinition.add(new BooleanArgumentDefinition(IGNORE_CASE_ARG,
				IGNORE_CASE_ARG_DESC, false, false));

		extendedDefinition.add(new StringMappingPairArgumentDefinition(
				EQUIVALENT_CC_ARG, EQUIVALENT_CC_ARG_DESC, false, null, true));

		return extendedDefinition
				.toArray(new ArgumentDefinition<?>[extendedDefinition.size()]);

	}

	@Override
	public String getName() {
		return "Attribute equality mapping";
	}

	@Override
	public String getVersion() {
		return "11.05.2012";
	}

	@Override
	public String getId() {
		return "attributeEquality";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	public String[] requiresValidators() {
		return new String[0];
	}
}
