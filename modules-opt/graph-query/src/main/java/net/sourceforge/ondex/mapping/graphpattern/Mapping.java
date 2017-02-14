package net.sourceforge.ondex.mapping.graphpattern;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.*;
import net.sourceforge.ondex.mapping.ONDEXMapping;

import java.util.*;

/**
 * Creates mappings based on relationships between end entries of a graph query
 * pattern, i.e. pattern = Enzyme, Protein, Gene will map entries of concept
 * class Enzyme only if there is the specified relationship exists between
 * entries of concept class Gene reachable from Enzyme. If no relationship is
 * specified entries are expected to be the same.
 * 
 * @author taubertj
 */
@Custodians(custodians = { "Matthew Hindle" }, emails = { " matthew_hindle at users.sourceforge.net" })
public class Mapping extends ONDEXMapping implements ArgumentNames, MetaData {

	private boolean mapWithInDataSource = false;

	@Override
	public void start() throws Exception {
		if (args.getUniqueValue(WITHIN_DATASOURCE_ARG) != null) {
			mapWithInDataSource = (Boolean) args.getUniqueValue(WITHIN_DATASOURCE_ARG);
		}

		// relation type to create between mapped entries
		RelationType equ = graph.getMetaData().getRelationType(RT);
		if (equ == null) {
			this.fireEventOccurred(new RelationTypeMissingEvent(
					"Required relation type " + RT + " not found in metadata.",
					getCurrentMethodName()));
			equ = graph.getMetaData().getFactory().createRelationType(RT);
		}

		// evidence type to assign to mapping relation
		EvidenceType et = graph.getMetaData().getEvidenceType(ET);
		if (et == null) {
			this.fireEventOccurred(new EvidenceTypeMissingEvent(
					"Required evidence type " + ET + " not found in metadata.",
					getCurrentMethodName()));
			et = graph.getMetaData().getFactory().createEvidenceType(ET);
		}

		// attribute name for score on relations
		AttributeName an = graph.getMetaData().getAttributeName(AN);
		if (an == null) {
			this.fireEventOccurred(new AttributeNameMissingEvent(
					"Required attribute name " + AN + " not found in metadata.",
					getCurrentMethodName()));
			an = graph.getMetaData().getFactory()
					.createAttributeName(AN, Integer.class);
		}

		// relation type to exists between considered entries
		RelationType rt = null;
		if (args.getUniqueValue(RELATIONTYPE_ARG) != null) {
			rt = graph.getMetaData().getRelationType(
					(String) args.getUniqueValue(RELATIONTYPE_ARG));
			if (rt == null) {
				this.fireEventOccurred(new RelationTypeMissingEvent(
						"Specified relation type "
								+ args.getUniqueValue(RELATIONTYPE_ARG)
								+ " not found in metadata.",
								getCurrentMethodName()));
				return;
			}
		}

		// the graph patterns to follow
		List<List<ConceptClass>> patterns = new ArrayList<List<ConceptClass>>();
		for (Object o : args.getObjectValueList(PATTERN_ARG)) {
			List<ConceptClass> pattern = new ArrayList<ConceptClass>();
			// comma separated ids of concept classes
			for (String id : ((String) o).split(",")) {
				ConceptClass cc = graph.getMetaData().getConceptClass(id);
				if (cc == null) {
					this.fireEventOccurred(new ConceptClassMissingEvent(
							"Specified concept class " + id + " in pattern "
									+ o + " not found in metadata.",
									getCurrentMethodName()));
					return;
				}
				pattern.add(cc);
			}
			// add pattern of concept classes to global list
			if (pattern.size() >= 2) {
				this.fireEventOccurred(new GeneralOutputEvent(
						"Adding concept class pattern " + o,
						getCurrentMethodName()));
				patterns.add(pattern);
			} else {
				this.fireEventOccurred(new PluginErrorEvent(
						"Specified pattern "
								+ o
								+ " doesnt fulfill minimal length requirement of 2.",
								getCurrentMethodName()));
				return;
			}
		}

		// construct reachability maps according to pattern
		Map<ONDEXConcept, Set<ONDEXConcept>> reachability = new HashMap<ONDEXConcept, Set<ONDEXConcept>>();
		for (List<ConceptClass> pattern : patterns) {

			// start BFS at concepts of this concept class
			ConceptClass rootCC = pattern.get(0);
			for (ONDEXConcept root : graph.getConceptsOfConceptClass(rootCC)) {
				if (!reachability.containsKey(root))
					reachability.put(root, new HashSet<ONDEXConcept>());

				// go through all levels of concept classes in between
				Set<ONDEXConcept> current = new HashSet<ONDEXConcept>();
				current.add(root);
				for (int i = 1; i < pattern.size(); i++) {
					ConceptClass interCC = pattern.get(i);

					// concepts from last relation level
					Set<ONDEXConcept> temp = new HashSet<ONDEXConcept>();
					for (ONDEXConcept child : current) {
						for (ONDEXRelation r : graph
								.getRelationsOfConcept(child)) {
							// prevent self loops and add other concept
							if (child.equals(r.getFromConcept())
									&& !child.equals(r.getToConcept())) {
								// check intermediate concept class
								if (r.getToConcept().getOfType()
										.equals(interCC))
									temp.add(r.getToConcept());

							} else if (child.equals(r.getToConcept())
									&& !child.equals(r.getFromConcept())) {
								// check intermediate concept class
								if (r.getFromConcept().getOfType()
										.equals(interCC))
									temp.add(r.getFromConcept());

							}

						}
					}

					current = temp;
				}

				// add last set of concepts as results
				reachability.get(root).addAll(current);
			}
		}

		// array of concepts to get mapped
		ONDEXConcept[] keys = reachability.keySet()
				.toArray(new ONDEXConcept[0]);
		for (int i = 0; i < keys.length; i++) {
			ONDEXConcept conceptA = keys[i];
			Set<ONDEXConcept> considerA = reachability.get(conceptA);

			// only compare each pair once and exclude self matches
			for (int j = i + 1; j < keys.length; j++) {
				ONDEXConcept conceptB = keys[j];
				Set<ONDEXConcept> considerB = reachability.get(conceptB);

				// map only within same concept class and different CVs
				if (conceptA.getOfType().equals(conceptB.getOfType())
						&& (!conceptA.getElementOf().equals(
								conceptB.getElementOf()) || mapWithInDataSource)) {

					// no relationship between considered entries given
					if (rt == null) {

						// check if considerA and considerB are not disjoint
						if (!Collections.disjoint(considerA, considerB)) {

							// calculate overlap of both sets
							Set<ONDEXConcept> overlap = new HashSet<ONDEXConcept>(
									considerA);
							overlap.retainAll(considerB);

							// create both directions as per convention
							ONDEXRelation r = graph.getFactory()
									.createRelation(conceptA, conceptB, equ, et);
							r.createAttribute(an, overlap.size(), false);

							r = graph.getFactory().createRelation(conceptB,
									conceptA, equ, et);
							r.createAttribute(an, overlap.size(), false);
						}
					} else {

						// count specified relationships between the two sets
						int count = 0;
						for (ONDEXConcept nA : considerA) {

							// it is enough to check one direction
							for (ONDEXRelation r : graph
									.getRelationsOfConcept(nA)) {
								// confirm relation type
								if (r.getOfType().equals(rt)) {
									ONDEXConcept from = r.getFromConcept();
									ONDEXConcept to = r.getToConcept();

									// check if concepts are present in set B
									if (considerB.contains(from))
										count++;
									if (considerB.contains(to))
										count++;
								}
							}
						}

						// there has to be at least one match
						if (count > 0) {
							// create both directions as per convention
							ONDEXRelation r = graph.getFactory()
									.createRelation(conceptA, conceptB, equ, et);
							r.createAttribute(an, count, false);

							r = graph.getFactory().createRelation(conceptB,
									conceptA, equ, et);
							r.createAttribute(an, count, false);
						}
					}
				}
			}
		}
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringArgumentDefinition pattern = new StringArgumentDefinition(
				PATTERN_ARG, PATTERN_ARG_DESC, true, "", true);
		StringArgumentDefinition relationtype = new StringArgumentDefinition(
				RELATIONTYPE_ARG, RELATIONTYPE_ARG_DESC, false, null, false);
		BooleanArgumentDefinition mapWithinCV = new BooleanArgumentDefinition(
				WITHIN_DATASOURCE_ARG, WITHIN_DATASOURCE_ARG_DESC, false, false);
		return new ArgumentDefinition<?>[] { pattern, relationtype, mapWithinCV };
	}

	@Override
	public String getName() {
		return "Graph pattern mapping";
	}

	@Override
	public String getVersion() {
		return "14.07.2009";
	}

	@Override
	public String getId() {
		return "graphpattern";
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
