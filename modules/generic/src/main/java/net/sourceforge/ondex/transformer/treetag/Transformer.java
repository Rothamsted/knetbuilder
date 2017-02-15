package net.sourceforge.ondex.transformer.treetag;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

@Custodians(custodians = { "Jochen Weile" }, emails = { " \tjweile at users.sourceforge.net" })
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition root = new StringArgumentDefinition(
				ROOT_CC_ARG, ROOT_CC_ARG_DESC, true, null, false);
		StringArgumentDefinition rootCV = new StringArgumentDefinition(
				ROOT_DATASOURCE_ARG, ROOT_DATASOURCE_ARG_DESC, false, null,
				false);
		StringArgumentDefinition first = new StringArgumentDefinition(
				FIRST_CC_ARG, FIRST_CC_ARG_DESC, true, null, true);
		StringArgumentDefinition second = new StringArgumentDefinition(
				SECOND_CC_ARG, SECOND_CC_ARG_DESC, false, null, true);
		StringArgumentDefinition third = new StringArgumentDefinition(
				THIRD_CC_ARG, THIRD_CC_ARG_DESC, false, null, true);
		StringArgumentDefinition fourth = new StringArgumentDefinition(
				FOURTH_CC_ARG, FOURTH_CC_ARG_DESC, false, null, true);
		StringArgumentDefinition fifth = new StringArgumentDefinition(
				FIFTH_CC_ARG, FIFTH_CC_ARG_DESC, false, null, true);
		StringArgumentDefinition sixth = new StringArgumentDefinition(
				SIXTH_CC_ARG, SIXTH_CC_ARG_DESC, false, null, true);
		return new ArgumentDefinition[] { root, rootCV, first, second, third,
				fourth, fifth, sixth };
	}

	@Override
	public String getName() {
		return "Tree Tag";
	}

	@Override
	public String getVersion() {
		return "20.11.2008";
	}

	@Override
	public String getId() {
		return "treetag";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	Map<Integer, Set<ConceptClass>> ccs = new Hashtable<Integer, Set<ConceptClass>>();

	@Override
	public void start() throws Exception {

		// get root concept class
		String rootCCname = (String) args.getUniqueValue(ROOT_CC_ARG);
		ConceptClass rootCC = graph.getMetaData().getConceptClass(rootCCname);
		if (rootCC == null)
			throw new ConceptClassMissingException("root concept class missing");

		// get optional root DataSource
		String rootCVname;
		DataSource rootDataSource = null;
		if (args.getUniqueValue(ROOT_DATASOURCE_ARG) != null) {
			rootCVname = (String) args.getUniqueValue(ROOT_DATASOURCE_ARG);
			rootDataSource = graph.getMetaData().getDataSource(rootCVname);
			if (rootDataSource == null)
				throw new DataSourceMissingException("root DataSource missing");
		}

		// get level one concept classes
		Object[] firstCCnames = args.getObjectValueArray(FIRST_CC_ARG);
		ccs.put(1, new HashSet<ConceptClass>());
		for (Object o : firstCCnames) {
			ConceptClass cc = graph.getMetaData().getConceptClass(o.toString());
			if (cc == null)
				throw new ConceptClassMissingException(
						"level one concept class missing");
			else
				ccs.get(1).add(cc);
		}

		// get level two concept classes
		ccs.put(2, new HashSet<ConceptClass>());
		if (args.getOptions().containsKey(SECOND_CC_ARG)) {
			Object[] secondCCnames = args.getObjectValueArray(SECOND_CC_ARG);
			for (Object o : secondCCnames) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc == null)
					throw new ConceptClassMissingException(
							"level two concept class missing");
				else
					ccs.get(2).add(cc);
			}
		}

		// get level three concept classes
		ccs.put(3, new HashSet<ConceptClass>());
		if (args.getOptions().containsKey(THIRD_CC_ARG)) {
			Object[] thirdCCnames = args.getObjectValueArray(THIRD_CC_ARG);
			for (Object o : thirdCCnames) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc == null)
					throw new ConceptClassMissingException(
							"level three concept class missing");
				else
					ccs.get(3).add(cc);
			}
		}

		// get level four concept classes
		ccs.put(4, new HashSet<ConceptClass>());
		if (args.getOptions().containsKey(FOURTH_CC_ARG)) {
			Object[] fourthCCnames = args.getObjectValueArray(FOURTH_CC_ARG);
			for (Object o : fourthCCnames) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc == null)
					throw new ConceptClassMissingException(
							"level four concept class missing");
				else
					ccs.get(4).add(cc);
			}
		}

		// get level five concept classes
		ccs.put(5, new HashSet<ConceptClass>());
		if (args.getOptions().containsKey(FIFTH_CC_ARG)) {
			Object[] fifthCCnames = args.getObjectValueArray(FIFTH_CC_ARG);
			for (Object o : fifthCCnames) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc == null)
					throw new ConceptClassMissingException(
							"level five concept class missing");
				else
					ccs.get(5).add(cc);
			}
		}

		// get level six concept classes
		ccs.put(6, new HashSet<ConceptClass>());
		if (args.getOptions().containsKey(SIXTH_CC_ARG)) {
			Object[] sixthCCnames = args.getObjectValueArray(SIXTH_CC_ARG);
			for (Object o : sixthCCnames) {
				ConceptClass cc = graph.getMetaData().getConceptClass(
						o.toString());
				if (cc == null)
					throw new ConceptClassMissingException(
							"level six concept class missing");
				else
					ccs.get(6).add(cc);
			}
		}

		// this is a pre-filter step if a DataSource was given
		Set<ONDEXConcept> all = graph.getConcepts();
		if (rootDataSource != null) {
			all = graph.getConceptsOfDataSource(rootDataSource);
		}

		// start at a root concept of given concept class
		Set<ONDEXConcept> roots = BitSetFunctions.copy(all);
		roots.retainAll(graph.getConceptsOfConceptClass(rootCC));
		for (ONDEXConcept root : roots) {
			performSearch(root, root, 0);
		}

		// perform cleanup
		for (ONDEXConcept concept : graph.getConcepts()) {
			for (ONDEXConcept currentContext : concept.getTags().toArray(
					new ONDEXConcept[0])) {
				boolean hasSameContext = false;
				for (ONDEXRelation r : graph.getRelationsOfConcept(concept)) {
					ONDEXConcept from = r.getFromConcept();
					ONDEXConcept to = r.getToConcept();

					if (concept.equals(from)) {
						Set<ONDEXConcept> toContext = new HashSet<ONDEXConcept>(
								to.getTags());
						if (toContext.contains(currentContext))
							hasSameContext = true;
					}
					if (concept.equals(to)) {
						Set<ONDEXConcept> fromContext = new HashSet<ONDEXConcept>(
								from.getTags());
						if (fromContext.contains(currentContext))
							hasSameContext = true;
					}
				}
				if (!hasSameContext)
					concept.removeTag(currentContext);
			}
		}

	}

	/**
	 * Use depth first search to assign context.
	 * 
	 * @param context
	 * @param current
	 * @param depth
	 */
	private void performSearch(ONDEXConcept context, ONDEXConcept current,
			int depth) {
		// recursion base
		if (depth == 6)
			return;
		depth++;

		// add context
		current.addTag(context);

		Set<ConceptClass> set = ccs.get(depth);

		// traverse relations
		for (ONDEXRelation r : graph.getRelationsOfConcept(current)) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();

			if (current.equals(from)) {
				if (set.contains(to.getOfType())) {
					r.addTag(context);
					performSearch(context, to, depth);
				}
			}
			if (current.equals(to)) {
				if (set.contains(from.getOfType())) {
					r.addTag(context);
					performSearch(context, from, depth);
				}
			}
		}
	}
}
