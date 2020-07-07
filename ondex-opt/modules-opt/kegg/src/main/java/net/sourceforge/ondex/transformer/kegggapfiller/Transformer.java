package net.sourceforge.ondex.transformer.kegggapfiller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.tools.auxfunctions.tuples.Pair;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Transformer to process output of KEGG map parser further and transfer generic
 * reactions between compounds of organism specific maps. Uses concept classes
 * Gene, Reaction and Compound. Requires merged output of RN and organism
 * specific KEGG map integration.
 * 
 * @author taubertj
 * 
 */
public class Transformer extends ONDEXTransformer {

	@Override
	public String getId() {
		return "kegggapfiller";
	}

	@Override
	public String getName() {
		return "KEGG gap filling";
	}

	@Override
	public String getVersion() {
		return "22.09.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public void start() throws Exception {

		// genes are from organism specific parser
		ConceptClass gene = graph.getMetaData().getConceptClass("Gene");
		if (gene == null)
			throw new InconsistencyException("Requires concept class Gene.");

		// reactions are from RN parser
		ConceptClass reaction = graph.getMetaData().getConceptClass("Reaction");
		if (reaction == null)
			throw new InconsistencyException("Requires concept class Reaction.");

		// enzymes are from EC parser
		ConceptClass enzyme = graph.getMetaData().getConceptClass("Enzyme");
		if (enzyme == null)
			throw new InconsistencyException("Requires concept class Enzyme.");

		// compounds merged between both
		ConceptClass compound = graph.getMetaData().getConceptClass("Comp");
		if (compound == null)
			throw new InconsistencyException("Requires concept class Comp.");

		// index all enzymes by RN number
		Map<String, Set<ONDEXConcept>> enzymes = new HashMap<String, Set<ONDEXConcept>>();
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(enzyme)) {
			if (!enzymes.containsKey(c.getPID()))
				enzymes.put(c.getPID(), new HashSet<ONDEXConcept>());
			enzymes.get(c.getPID()).add(c);
		}

		// copy ECs to reactions
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(reaction)) {
			if (enzymes.containsKey(c.getPID())) {
				for (ONDEXConcept concept : enzymes.get(c.getPID())) {
					for (ConceptAccession ca : concept.getConceptAccessions()) {
						c.createConceptAccession(ca.getAccession(),
								ca.getElementOf(), ca.isAmbiguous());
					}
				}
			}
		}

		// copy ECs to genes
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(gene)) {
			if (enzymes.containsKey(c.getPID())) {
				for (ONDEXConcept concept : enzymes.get(c.getPID())) {
					for (ConceptAccession ca : concept.getConceptAccessions()) {
						c.createConceptAccession(ca.getAccession(),
								ca.getElementOf(), ca.isAmbiguous());
					}
				}
			}
		}

		// delete all enzyme concepts
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(enzyme).toArray(
				new ONDEXConcept[0])) {
			// delete EC tags as well
			for (ONDEXConcept tag : c.getTags().toArray(new ONDEXConcept[0])) {
				graph.deleteConcept(tag.getId());
			}
			graph.deleteConcept(c.getId());
		}

		// get all compounds and check for links in organism specific parser
		Set<ONDEXConcept> orgComps = new HashSet<ONDEXConcept>();
		for (ONDEXConcept c : graph.getConceptsOfConceptClass(compound)) {
			for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
				if (r.getFromConcept().getOfType().equals(gene)
						|| r.getToConcept().getOfType().equals(gene))
					orgComps.add(c);
			}
		}

		// process all pairs of compounds and get all reactions between them
		ONDEXConcept[] array = orgComps.toArray(new ONDEXConcept[orgComps
				.size()]);
		Map<Pair, Set<ONDEXConcept>> tuples = new HashMap<Pair, Set<ONDEXConcept>>();
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; j < array.length; j++) {
				ONDEXConcept c1 = array[i];
				ONDEXConcept c2 = array[j];
				Pair key = new Pair(c1, c2);
				for (ONDEXRelation r1 : graph.getRelationsOfConcept(c1)) {

					// traverse one step further
					ONDEXConcept from = r1.getFromConcept();
					ONDEXConcept to = r1.getToConcept();
					if (from.equals(c1)) {
						// to concept must be reaction or gene
						for (ONDEXRelation r2 : graph.getRelationsOfConcept(to)) {
							// check for link to c2
							if (r2.getToConcept().equals(c2)) {
								if (!tuples.containsKey(key))
									tuples.put(key, new HashSet<ONDEXConcept>());
								// add reaction or gene concept
								tuples.get(key).add(r2.getFromConcept());
							} else if (r2.getFromConcept().equals(c2)) {
								if (!tuples.containsKey(key))
									tuples.put(key, new HashSet<ONDEXConcept>());
								// add reaction or gene concept
								tuples.get(key).add(r2.getToConcept());
							}
						}

					} else {
						// from concept must be reaction or gene
						for (ONDEXRelation r2 : graph
								.getRelationsOfConcept(from)) {
							// check for link to c2
							if (r2.getToConcept().equals(c2)) {
								if (!tuples.containsKey(key))
									tuples.put(key, new HashSet<ONDEXConcept>());
								// add reaction or gene concept
								tuples.get(key).add(r2.getFromConcept());
							} else if (r2.getFromConcept().equals(c2)) {
								if (!tuples.containsKey(key))
									tuples.put(key, new HashSet<ONDEXConcept>());
								// add reaction or gene concept
								tuples.get(key).add(r2.getToConcept());
							}
						}
					}
				}
			}
		}

		// total set of remaining concepts
		Set<ONDEXConcept> total = new HashSet<ONDEXConcept>(orgComps);
		for (Pair key : tuples.keySet()) {
			// organism specific reactions are preferred
			Set<ONDEXConcept> first = new HashSet<ONDEXConcept>();
			Set<ONDEXConcept> second = new HashSet<ONDEXConcept>();
			for (ONDEXConcept c : tuples.get(key)) {
				if (c.getOfType().equals(gene)) {
					first.add(c);
				} else {
					second.add(c);
				}
			}
			// only add generic reaction if no organism specific ones exist
			if (first.size() > 0)
				total.addAll(first);
			else
				total.addAll(second);
		}

		// add possible tags etc
		for (ONDEXConcept c : total.toArray(new ONDEXConcept[total.size()])) {
			total.addAll(c.getTags());
		}

		// remove all non-matching concepts
		for (ONDEXConcept c : graph.getConcepts().toArray(new ONDEXConcept[0])) {
			if (!total.contains(c))
				graph.deleteConcept(c.getId());
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
