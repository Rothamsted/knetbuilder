package net.sourceforge.ondex.transformer.relationmerger;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * This transformer creates new relations by merging two existing ones. The
 * relations will be of type FirstRelationType and will have all Attribute
 * assigned. Exclusive mode removes all relation, which are not merged of both
 * relation types.
 * 
 * @author taubertj
 * @version 31.01.2008
 */
@Authors(authors = { "Jan Taubert" }, emails = { "jantaubert at users.sourceforge.net" })
@Custodians(custodians = { "Jochen Weile" }, emails = { " \tjweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	/**
	 * Starts processing of data.
	 * 
	 * @throws EvidenceTypeMissingException
	 */
	public void start() throws EvidenceTypeMissingException,
			InvalidPluginArgumentException {

		// create new evidence type
		EvidenceType et = graph.getMetaData().getEvidenceType("TRANS");
		if (et == null)
			throw new EvidenceTypeMissingException("TRANS");

		// all valid relations
		Set<ONDEXRelation> relations;

		// which relation type are we working with
		RelationType rt1 = graph.getMetaData().getRelationType(
				(String) args.getUniqueValue(FIRST_RELATION_TYPE_ARG));
		if (rt1 == null)
			throw new WrongParameterException(
					args.getUniqueValue(FIRST_RELATION_TYPE_ARG)
							+ " is not a valid RelationType.");

		// get relations of relation type
		relations = new HashSet<ONDEXRelation>(
				graph.getRelationsOfRelationType(rt1));

		// which relation type are we working with
		RelationType rt2 = graph.getMetaData().getRelationType(
				(String) args.getUniqueValue(SECOND_RELATION_TYPE_ARG));
		if (rt2 == null)
			throw new WrongParameterException(
					args.getUniqueValue(SECOND_RELATION_TYPE_ARG)
							+ " is not a valid RelationType.");

		// which from and to concept class
		for (String id : args.getObjectValueList(CONCEPT_CLASS_ARG,
				String.class)) {
			// get concept class
			ConceptClass cc = graph.getMetaData().getConceptClass(id);
			if (cc == null)
				throw new WrongParameterException(
						args.getUniqueValue(CONCEPT_CLASS_ARG)
								+ " is not a valid ConceptClass.");
			else
				// get relations of concept class
				relations.retainAll(graph.getRelationsOfConceptClass(cc));
		}

		System.out.println("Performing merging on " + relations.size()
				+ " relations of first relation type.");

		boolean exclusive = false;
		if (args.getUniqueValue(EXCLUSIVE_ARG) != null) {
			exclusive = (Boolean) args.getUniqueValue(EXCLUSIVE_ARG);
		}

		// relations to remove
		Set<ONDEXRelation> remove = new HashSet<ONDEXRelation>();

		// iterate over all relations
		int hits = 0;
		for (ONDEXRelation first : relations) {

			ONDEXConcept from = first.getFromConcept();
			ONDEXConcept to = first.getToConcept();

			// check for second relation of rt2 type
			ONDEXRelation second = graph.getRelation(from, to, rt2);
			if (exclusive && second == null) {
				remove.add(first);
			} else if (second != null) {
				hits++;

				for (Attribute attribute : second.getAttributes()) {
					// only merge not yet existing Attribute
					if (first.getAttribute(attribute.getOfType()) == null) {
						first.createAttribute(attribute.getOfType(),
								attribute.getValue(), attribute.isDoIndex());
					}
				}

				// get potentially new context
				Set<ONDEXConcept> remaining = new HashSet<ONDEXConcept>(
						second.getTags());

				// remove all existing context
				remaining.removeAll(first.getTags());

				// add remaining context
				for (ONDEXConcept c : remaining) {
					first.addTag(c);
				}

				remove.add(second);
			}
		}
		System.out.println("Merged a total of " + hits + " relations.");

		// possible not merged ones as well
		if (exclusive) {
			remove.addAll(graph.getRelationsOfRelationType(rt2));
		}

		// remove all merged relations
		for (ONDEXRelation rel : remove) {
			graph.deleteRelation(rel.getId());
		}
	}

	/**
	 * Returns name of this transformer.
	 * 
	 * @return String
	 */
	public String getName() {
		return "Relation merger";
	}

	/**
	 * Returns version of this transformer.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return "11.07.11";
	}

	@Override
	public String getId() {
		return "relationmerger";
	}

	/**
	 * Returns arguments required by this transformer.
	 * 
	 * @return ArgumentDefinition<?>[]
	 */
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(
						ArgumentNames.FIRST_RELATION_TYPE_ARG,
						ArgumentNames.FIRST_RELATION_TYPE_ARG_DESC, true, null,
						false),
				new StringArgumentDefinition(
						ArgumentNames.SECOND_RELATION_TYPE_ARG,
						ArgumentNames.SECOND_RELATION_TYPE_ARG_DESC, true,
						null, false),
				new StringArgumentDefinition(ArgumentNames.CONCEPT_CLASS_ARG,
						ArgumentNames.CONCEPT_CLASS_ARG_DESC, true, null, true),
				new BooleanArgumentDefinition(EXCLUSIVE_ARG,
						EXCLUSIVE_ARG_DESC, false, false) };

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
