package net.sourceforge.ondex.transformer.relationreverter;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.WrongParameterException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;


@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new StringArgumentDefinition(ArgumentNames.RELATION_TYPE_ARG,
						ArgumentNames.RELATION_TYPE_ARG_DESC, true, null, true),
				new BooleanArgumentDefinition(ArgumentNames.KEEP_OLD_ARG,
						ArgumentNames.KEEP_OLD_ARG_DESC, false, false) };
	}

	@Override
	public String getName() {
		return "Relation Reverter";
	}

	@Override
	public String getVersion() {
		return "04.01.2013";
	}

	@Override
	public String getId() {
		return "relatonreverter";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	@Override
	public void start() throws Exception {

		RelationType rt = graph.getMetaData().getRelationType(
				(String) args.getUniqueValue(ArgumentNames.RELATION_TYPE_ARG));

		if (rt == null) {
			fireEventOccurred(new WrongParameterEvent(
					args.getUniqueValue(ArgumentNames.RELATION_TYPE_ARG)
							+ " is not a valid RelationType.",
					getCurrentMethodName()));
			throw new WrongParameterException(
					args.getUniqueValue(ArgumentNames.RELATION_TYPE_ARG)
							+ " is not a valid RelationType.");
		}

		Set<Integer> toDelete = new HashSet<Integer>();

		// iterate over all relations of given type
		for (ONDEXRelation r : graph.getRelationsOfRelationType(rt).toArray(
				new ONDEXRelation[0])) {

			// get involved concepts
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();

			// create reverse relation
			ONDEXRelation newr = graph.createRelation(to, from, rt,
					r.getEvidence());
			// copy tags
			for (ONDEXConcept c : r.getTags()) {
				newr.addTag(c);
			}
			// copy attributes
			for (Attribute attr : r.getAttributes()) {
				newr.createAttribute(attr.getOfType(), attr.getValue(),
						attr.isDoIndex());
			}

			// add old relation to be deleted
			toDelete.add(r.getId());
		}

		boolean keepOld = (Boolean) args.getUniqueValue("keepOld");

		// remove old relations if requested
		if (!keepOld) {
			// remove relations by their ID
			for (Integer i : toDelete) {
				graph.deleteRelation(i);
			}
		}
	}

}
